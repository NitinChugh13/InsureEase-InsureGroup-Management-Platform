package com.insureease;

import com.insureease.audit.entity.AuditLog;
import com.insureease.audit.service.AuditService;
import com.insureease.billing.entity.CdAccount;
import com.insureease.billing.entity.Invoice;
import com.insureease.billing.entity.LedgerEntry;
import com.insureease.billing.service.BillingService;
import com.insureease.claims.entity.Claim;
import com.insureease.claims.entity.Hospital;
import com.insureease.claims.entity.Settlement;
import com.insureease.claims.service.ClaimService;
import com.insureease.common.exception.ResourceNotFoundException;
import com.insureease.common.tenant.TenantContext;
import com.insureease.iam.entity.Tenant;
import com.insureease.iam.service.IamService;
import com.insureease.policy.entity.Endorsement;
import com.insureease.policy.entity.EndorsementItem;
import com.insureease.policy.entity.MasterPolicy;
import com.insureease.policy.entity.Member;
import com.insureease.policy.service.PolicyService;
import com.insureease.product.entity.Plan;
import com.insureease.product.service.ProductService;
import com.insureease.quote.entity.Policyholder;
import com.insureease.quote.entity.Quote;
import com.insureease.quote.entity.QuoteMember;
import com.insureease.quote.service.QuoteService;
import com.insureease.renewal.entity.RenewalCase;
import com.insureease.renewal.service.RenewalService;
import com.insureease.reporting.controller.ReportingController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class InsureEaseLifecycleIntegrationTest {

    @Autowired
    private IamService iamService;
    @Autowired
    private ProductService productService;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private PolicyService policyService;
    @Autowired
    private BillingService billingService;
    @Autowired
    private ClaimService claimService;
    @Autowired
    private RenewalService renewalService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private ReportingController reportingController;

    private IamService.AuthTokensResponse adminAuth;
    private UUID acmeTenantId;

    @BeforeEach
    void setUp() {
        adminAuth = iamService.login(new IamService.LoginRequest(
                "ACME-INSURE", "admin@acmeinsure.com", "Admin@123"
        ));
        acmeTenantId = adminAuth.tenantId();
        TenantContext.setTenantId(acmeTenantId);
        TenantContext.setUserId(adminAuth.userId());
        TenantContext.setUserEmail(adminAuth.email());
        TenantContext.setRoles(adminAuth.roles());
        TenantContext.setCorrelationId("test-corr-" + UUID.randomUUID());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("1. Full Group Health (GMC) Lifecycle: Quote -> Razorpay Sandbox Webhook -> Policy -> 64VB CD Endorsement -> Claim -> BR-11 Check -> ICR Renewal Rollover")
    @SuppressWarnings("unchecked")
    void fullGroupHealthLifecycleTest() {
        Policyholder employer = quoteService.listPolicyholders().getFirst();
        Plan gmcPlan = productService.listPlans(null).stream()
                .filter(p -> "GMC-GOLD-FLOATER".equals(p.getCode()))
                .findFirst().orElseThrow();
        Hospital hospital = claimService.listHospitals().getFirst();

        // 1. Create Quote & Upload Census (Employee + Spouse + Child)
        Quote quote = quoteService.createQuote(new QuoteService.CreateQuoteRequest(
                employer.getId(), gmcPlan.getId(), BigDecimal.ZERO, LocalDate.now().plusDays(30)
        ));

        Map<String, Object> censusResult = quoteService.uploadCensusJson(quote.getId(), List.of(
                new QuoteService.CensusMemberInput(
                        "EMP-101", "Rohan Verma", LocalDate.of(1992, 5, 10), "M", "SELF",
                        "CLASS_I", new BigDecimal("1800000"), new BigDecimal("500000"),
                        "rohan@hcl-demo.com", "9811122233", null, null, null, null, null
                ),
                new QuoteService.CensusMemberInput(
                        "EMP-101", "Priya Verma", LocalDate.of(1994, 8, 15), "F", "SPOUSE",
                        "CLASS_I", null, new BigDecimal("500000"),
                        null, null, null, null, null, null, null
                ),
                new QuoteService.CensusMemberInput(
                        "EMP-102", "Vikram Singh", LocalDate.of(1988, 3, 20), "M", "SELF",
                        "CLASS_I", new BigDecimal("2200000"), new BigDecimal("500000"),
                        "vikram@hcl-demo.com", "9822233344", null, null, null, null, null
                )
        ));
        assertThat(censusResult.get("invalidRows")).isEqualTo(0);

        // 2. Calculate Premium & Auto-Approve Quote (BR-1)
        Map<String, Object> calcResult = quoteService.calculatePremiumAndEvaluateRules(quote.getId());
        Quote pricedQuote = (Quote) calcResult.get("quote");
        assertThat(pricedQuote.getStatus()).isEqualTo("APPROVED");
        assertThat(pricedQuote.getTotalPremium()).isGreaterThan(BigDecimal.ZERO);

        // 3. Employer Accepts Quote & Issues MasterPolicy (with INR 50,000 Section 64VB CD Float)
        quoteService.acceptQuote(quote.getId());
        LocalDate policyStart = LocalDate.now().minusDays(60); // Start 60 days ago so 30-day initial waiting period is met
        Map<String, Object> issueResult = policyService.issuePolicyFromQuote(new PolicyService.IssuePolicyRequest(
                quote.getId(), policyStart, "ANNUAL", new BigDecimal("50000.00"), null
        ));
        MasterPolicy policy = (MasterPolicy) issueResult.get("policy");
        Invoice initialInvoice = (Invoice) issueResult.get("invoice");
        CdAccount cdAccount = (CdAccount) issueResult.get("cdAccount");

        assertThat(policy.getStatus()).isEqualTo("PENDING_PAYMENT");
        assertThat(cdAccount.getCurrentBalance()).isEqualByComparingTo("50000.00");

        // 4. Create Razorpay Sandbox Order & Reconcile via HMAC-SHA256 Verified Webhook (FR-7.3, FR-7.4)
        Map<String, Object> orderResp = billingService.createSandboxPaymentOrder(
                initialInvoice.getId(), new BillingService.CreatePaymentOrderRequest("RAZORPAY", "NETBANKING")
        );
        String gatewayOrderId = (String) orderResp.get("gatewayOrderId");
        Map<String, String> helper = (Map<String, String>) orderResp.get("sandboxWebhookHelper");
        String samplePaymentId = helper.get("sampleGatewayPaymentId");
        String validHmac = helper.get("computedHmacSignature");

        Map<String, Object> webhookResp = billingService.processGatewayWebhook(
                "RAZORPAY",
                validHmac,
                new BillingService.PaymentWebhookPayload(gatewayOrderId, samplePaymentId, "payment.captured", initialInvoice.getTotalAmount(), "NETBANKING")
        );
        assertThat(webhookResp.get("idempotentReplay")).isEqualTo(false);

        MasterPolicy activePolicy = policyService.getPolicy(policy.getId());
        assertThat(activePolicy.getStatus()).isEqualTo("ACTIVE");

        List<Member> enrolledMembers = policyService.getPolicyMembers(policy.getId());
        assertThat(enrolledMembers).hasSize(2); // 2 primary employees (dependents stored in dependent table)

        // 5. Submit & Approve Mid-Term Endorsement (ADD_MEMBER) Funded by Section 64VB CD Account (BR-2, BR-12)
        Map<String, Object> endSub = policyService.submitEndorsement(policy.getId(),
                new PolicyService.SubmitEndorsementRequest(
                        "ADD_MEMBER", LocalDate.now(), "CD_ACCOUNT", "Mid-term new joiner",
                        List.of(new PolicyService.EndorsementMemberActionInput(
                                "ADD_MEMBER", "EMP-103", "Neha Gupta", LocalDate.of(1996, 4, 12),
                                "F", "SELF", "CLASS_I", new BigDecimal("1500000"), new BigDecimal("500000"),
                                null, null, null, null
                        ))
                ));
        Endorsement endorsement = (Endorsement) endSub.get("endorsement");
        assertThat(endorsement.getTotalPremiumDelta()).isGreaterThan(BigDecimal.ZERO);

        Map<String, Object> endApp = policyService.approveAndApplyEndorsement(endorsement.getId());
        Endorsement approvedEnd = (Endorsement) endApp.get("endorsement");
        assertThat(approvedEnd.getStatus()).isEqualTo("APPROVED_APPLIED");
        assertThat(policyService.getPolicyMembers(policy.getId())).hasSize(3);

        // 6. Submit & Adjudicate a GMC Hospitalisation Claim (EligibilityChain + Deductible + 10% Co-pay)
        Member rohan = enrolledMembers.stream()
                .filter(m -> "EMP-101".equals(m.getEmployeeCode()))
                .findFirst().orElseThrow();

        Map<String, Object> claimSub = claimService.submitClaim(new ClaimService.SubmitClaimRequest(
                policy.getId(), rohan.getId(), null, hospital.getId(),
                "CASHLESS", "HOSPITALISATION", null, null,
                LocalDate.now().minusDays(2), LocalDate.now().minusDays(3), LocalDate.now().minusDays(1),
                false, "Acute Appendicitis Surgical Management",
                new BigDecimal("105000.00"), new BigDecimal("0.00")
        ));
        Claim submittedClaim = (Claim) claimSub.get("claim");
        // Admissible = 105,000 - 5,000 deductible = 100,000; 10% co-pay = 10,000 => Approved = 90,000.00
        assertThat(submittedClaim.getApprovedAmount()).isEqualByComparingTo("90000.00");

        // Upload mandatory checklist docs (PRE_AUTH_FORM, ID_PROOF)
        claimService.uploadClaimDocument(submittedClaim.getId(),
                new ClaimService.UploadDocumentRequest("PRE_AUTH_FORM", "preauth.pdf", "application/pdf", 2048L));
        Map<String, Object> docRes = claimService.uploadClaimDocument(submittedClaim.getId(),
                new ClaimService.UploadDocumentRequest("ID_PROOF", "aadhaar_masked.pdf", "application/pdf", 2048L));
        assertThat(docRes.get("checklistComplete")).isEqualTo(true);
        assertThat(docRes.get("claimStatus")).isEqualTo("UNDER_REVIEW");

        // Adjudicate & Settle Claim
        Map<String, Object> adjRes = claimService.adjudicateClaim(submittedClaim.getId(),
                new ClaimService.AdjudicateClaimRequest("APPROVE", null, null, "IRDAI_APPROVED", "Cashless hospital settlement"));
        Claim settledClaim = (Claim) adjRes.get("claim");
        List<Settlement> settlements = (List<Settlement>) adjRes.get("settlements");
        assertThat(settledClaim.getStatus()).isEqualTo("SETTLED");
        assertThat(settlements).hasSize(1);
        assertThat(settlements.getFirst().getPayeeType()).isEqualTo("HOSPITAL");

        // 7. Verify BR-11: Deleting a member who has already claimed yields INR 0.00 pro-rata refund!
        Map<String, Object> delEndSub = policyService.submitEndorsement(policy.getId(),
                new PolicyService.SubmitEndorsementRequest(
                        "DELETE_MEMBER", LocalDate.now(), "CD_ACCOUNT", "Rohan resignation after claim",
                        List.of(new PolicyService.EndorsementMemberActionInput(
                                "DELETE_MEMBER", "EMP-101", null, null, null, "SELF", null,
                                null, null, null, null, null, null
                        ))
                ));
        List<EndorsementItem> delItems = (List<EndorsementItem>) delEndSub.get("items");
        assertThat(delItems.getFirst().isPriorClaimExists()).isTrue();
        assertThat(delItems.getFirst().getProRataNetDelta()).isEqualByComparingTo("0.00");

        // 8. Experience-Rated Renewal Quote & Rollover (FR-5.2, FR-5.3, FR-5.5, BR-7)
        Map<String, Object> renOut = renewalService.analyzeExperienceAndGenerateRenewalQuote(policy.getId());
        RenewalCase renewalCase = (RenewalCase) renOut.get("renewalCase");
        assertThat(renewalCase.getIncurredClaims()).isEqualByComparingTo("90000.00");
        assertThat(renewalCase.getLossRatioPct()).isGreaterThan(new BigDecimal("100.00"));
        assertThat(renewalCase.getRecommendedRateChangePct()).isEqualByComparingTo("20.00"); // BR-7 (>100% ICR -> +20%)

        Map<String, Object> rolloverOut = renewalService.acceptAndRolloverRenewal(renewalCase.getId());
        MasterPolicy renewedTermPolicy = (MasterPolicy) rolloverOut.get("policy");
        assertThat(renewedTermPolicy.getParentPolicyId()).isEqualTo(policy.getId());
        assertThat(policyService.getPolicy(policy.getId()).getStatus()).isEqualTo("RENEWED");

        // 9. Verify Double-Entry Ledger Balance Invariant: SUM(debit) == SUM(credit)
        List<LedgerEntry> ledger = billingService.listLedgerEntries(policy.getId());
        BigDecimal totalDebits = ledger.stream().map(LedgerEntry::getDebitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredits = ledger.stream().map(LedgerEntry::getCreditAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalDebits).isEqualByComparingTo(totalCredits);
    }

    @Test
    @DisplayName("2. Group Term Life (GTL) Lifecycle: Salary-Multiple SI, Free Cover Limit (FCL) Cap, Medical UW Upgrade (BR-12), and Nominee Death Payout")
    @SuppressWarnings("unchecked")
    void groupTermLifeFclAndNomineeDeathClaimTest() {
        Policyholder employer = quoteService.listPolicyholders().getFirst();
        Plan gtlPlan = productService.listPlans(null).stream()
                .filter(p -> "GTL-EXEC-3X".equals(p.getCode()))
                .findFirst().orElseThrow();

        // Create GTL Quote
        Quote quote = quoteService.createQuote(new QuoteService.CreateQuoteRequest(
                employer.getId(), gtlPlan.getId(), BigDecimal.ZERO, LocalDate.now().plusDays(30)
        ));

        // Upload Executive with CTC = 30,00,000 -> 3x CTC = 90,00,000 (> 50,00,000 FCL)
        quoteService.uploadCensusJson(quote.getId(), List.of(
                new QuoteService.CensusMemberInput(
                        "EXEC-01", "Arjun Mehta", LocalDate.of(1985, 6, 15), "M", "SELF",
                        "CLASS_I", new BigDecimal("3000000.00"), null,
                        "arjun@hcl-demo.com", "9899988877",
                        "Kavita Mehta", "SPOUSE", LocalDate.of(1988, 9, 10), new BigDecimal("100.00"), null
                )
        ));

        // Calculate Premium -> Verify FCL Capping at 50,00,000 & MEDICAL_REQUIRED status (BR-12)
        quoteService.calculatePremiumAndEvaluateRules(quote.getId());
        QuoteMember qm = quoteService.getQuoteMembers(quote.getId()).getFirst();
        assertThat(qm.getRequestedSi()).isEqualByComparingTo("9000000.00");
        assertThat(qm.getApprovedSi()).isEqualByComparingTo("5000000.00"); // Capped at 50L FCL!
        assertThat(qm.getExcessFclSi()).isEqualByComparingTo("4000000.00");
        assertThat(qm.getUwStatus()).isEqualTo("MEDICAL_REQUIRED");

        // Underwriter Approves Medical Underwriting for Excess FCL -> Upgraded to full 90,00,000!
        quoteService.decideMemberMedicalUnderwriting(quote.getId(), qm.getId(),
                new QuoteService.MemberUwDecisionRequest("APPROVE", "Tele-medical & ECG normal"));
        QuoteMember upgradedQm = quoteService.getQuoteMembers(quote.getId()).getFirst();
        assertThat(upgradedQm.getApprovedSi()).isEqualByComparingTo("9000000.00");
        assertThat(upgradedQm.getUwStatus()).isEqualTo("APPROVED");

        // Approve & Accept Quote -> Issue Policy -> Pay via Manual UTR
        quoteService.recordUnderwriterDecision(quote.getId(),
                new QuoteService.UnderwriterDecisionRequest("APPROVE", BigDecimal.ZERO, BigDecimal.ZERO, "GTL Approved"));
        quoteService.acceptQuote(quote.getId());

        Map<String, Object> issueRes = policyService.issuePolicyFromQuote(new PolicyService.IssuePolicyRequest(
                quote.getId(), LocalDate.now().minusDays(10), "ANNUAL", BigDecimal.ZERO, null
        ));
        MasterPolicy policy = (MasterPolicy) issueRes.get("policy");
        Invoice invoice = (Invoice) issueRes.get("invoice");

        billingService.recordManualPayment(new BillingService.ManualPaymentRequest(
                invoice.getId(), invoice.getTotalAmount(), "NEFT", "UTR-GTL-998877"
        ));

        Member arjun = policyService.getPolicyMembers(policy.getId()).getFirst();
        assertThat(arjun.getSumInsured()).isEqualByComparingTo("9000000.00");

        // Submit Death Benefit Claim & Settle -> Verify Payout goes to Nominee (Kavita Mehta)
        Map<String, Object> claimRes = claimService.submitClaim(new ClaimService.SubmitClaimRequest(
                policy.getId(), arjun.getId(), null, null,
                "DEATH_BENEFIT", "DEATH", null, null,
                LocalDate.now().minusDays(1), null, null, false,
                "Sudden Cardiac Arrest", new BigDecimal("9000000.00"), BigDecimal.ZERO
        ));
        Claim deathClaim = (Claim) claimRes.get("claim");

        Map<String, Object> settledRes = claimService.adjudicateClaim(deathClaim.getId(),
                new ClaimService.AdjudicateClaimRequest("APPROVE", null, null, "DEATH_VERIFIED", "Settled to nominee"));
        List<Settlement> settlements = (List<Settlement>) settledRes.get("settlements");
        assertThat(settlements).hasSize(1);
        assertThat(settlements.getFirst().getPayeeType()).isEqualTo("NOMINEE");
        assertThat(settlements.getFirst().getPayeeName()).contains("Kavita Mehta");
        assertThat(settlements.getFirst().getAmount()).isEqualByComparingTo("9000000.00");
    }

    @Test
    @DisplayName("3. Group Personal Accident (GPA) Lifecycle: Occupation Class III Rating & Table C PPD Disability Scale Payout")
    @SuppressWarnings("unchecked")
    void groupPersonalAccidentDisabilityScaleTest() {
        Policyholder employer = quoteService.listPolicyholders().getFirst();
        Plan gpaPlan = productService.listPlans(null).stream()
                .filter(p -> "GPA-SHIELD".equals(p.getCode()))
                .findFirst().orElseThrow();

        Quote quote = quoteService.createQuote(new QuoteService.CreateQuoteRequest(
                employer.getId(), gpaPlan.getId(), BigDecimal.ZERO, LocalDate.now().plusDays(30)
        ));

        quoteService.uploadCensusJson(quote.getId(), List.of(
                new QuoteService.CensusMemberInput(
                        "ENG-501", "Suresh Nair", LocalDate.of(1990, 2, 14), "M", "SELF",
                        "CLASS_III", new BigDecimal("1200000.00"), new BigDecimal("2000000.00"),
                        "suresh@hcl-demo.com", "9844455566",
                        "Meena Nair", "SPOUSE", LocalDate.of(1992, 7, 20), new BigDecimal("100.00"), null
                )
        ));

        quoteService.calculatePremiumAndEvaluateRules(quote.getId());
        QuoteMember qm = quoteService.getQuoteMembers(quote.getId()).getFirst();
        // CLASS_III rate is 2.25 per mille on 20,00,000 SI => (2000000/1000) * 2.25 = 4500.00
        assertThat(qm.getBasePremium()).isEqualByComparingTo("4500.00");

        quoteService.acceptQuote(quote.getId());
        Map<String, Object> issueRes = policyService.issuePolicyFromQuote(new PolicyService.IssuePolicyRequest(
                quote.getId(), LocalDate.now().minusDays(15), "ANNUAL", BigDecimal.ZERO, null
        ));
        MasterPolicy policy = (MasterPolicy) issueRes.get("policy");
        Invoice invoice = (Invoice) issueRes.get("invoice");
        billingService.recordManualPayment(new BillingService.ManualPaymentRequest(
                invoice.getId(), invoice.getTotalAmount(), "NEFT", "UTR-GPA-112233"
        ));

        Member suresh = policyService.getPolicyMembers(policy.getId()).getFirst();

        // Submit Table C Permanent Partial Disability (PPD) Claim at 40% disability scale on 20L SI => 8,00,000 payable
        Map<String, Object> claimRes = claimService.submitClaim(new ClaimService.SubmitClaimRequest(
                policy.getId(), suresh.getId(), null, null,
                "DISABILITY_BENEFIT", "PPD", new BigDecimal("40.00"), null,
                LocalDate.now().minusDays(2), null, null, false,
                "Industrial crush injury with 40% certified PPD", new BigDecimal("1000000.00"), BigDecimal.ZERO
        ));
        Claim ppdClaim = (Claim) claimRes.get("claim");
        assertThat(ppdClaim.getApprovedAmount()).isEqualByComparingTo("800000.00");
    }

    @Test
    @DisplayName("4. Multi-Tenant Isolation & Audit Trail Verification (FR-1.1, FR-10.1)")
    void multiTenantIsolationAndAuditLogTest() {
        List<MasterPolicy> acmePolicies = policyService.listPolicies();
        List<AuditLog> acmeLogs = auditService.getTenantAuditLogs(null, null);
        assertThat(acmeLogs).isNotEmpty();

        Map<String, Object> lossRatioReport = reportingController.getLossRatioReport();
        assertThat(lossRatioReport).containsKeys("portfolioEarnedPremium", "portfolioIncurredClaims", "portfolioLossRatioPct");

        // Onboard a second tenant (BETA-TPA) and switch TenantContext
        Tenant betaTenant = iamService.createTenant(new IamService.CreateTenantRequest(
                "BETA-TPA-" + UUID.randomUUID().toString().substring(0, 4),
                "Beta Health TPA Services", "TPA", "STARTER",
                "ops@betatpa.com", "Admin@123", "Beta Admin"
        ));

        TenantContext.setTenantId(betaTenant.getId());
        TenantContext.setRoles(Set.of("ROLE_INSURER_ADMIN"));

        // Beta tenant sees zero policies from Acme tenant
        assertThat(policyService.listPolicies()).isEmpty();
        if (!acmePolicies.isEmpty()) {
            UUID acmePolicyId = acmePolicies.getFirst().getId();
            assertThatThrownBy(() -> policyService.getPolicy(acmePolicyId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
