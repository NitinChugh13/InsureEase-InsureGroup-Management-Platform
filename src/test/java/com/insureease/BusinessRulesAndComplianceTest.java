package com.insureease;

import com.insureease.audit.entity.ConsentRecord;
import com.insureease.audit.service.AuditService;
import com.insureease.billing.entity.Invoice;
import com.insureease.billing.service.BillingService;
import com.insureease.claims.entity.Claim;
import com.insureease.claims.service.ClaimService;
import com.insureease.common.crypto.PiiCryptoService;
import com.insureease.common.exception.BusinessRuleException;
import com.insureease.common.tenant.TenantContext;
import com.insureease.iam.service.IamService;
import com.insureease.policy.controller.PolicyController;
import com.insureease.policy.entity.MasterPolicy;
import com.insureease.policy.entity.Member;
import com.insureease.policy.service.PolicyService;
import com.insureease.product.entity.Plan;
import com.insureease.product.service.ProductService;
import com.insureease.quote.controller.QuoteController;
import com.insureease.quote.entity.Policyholder;
import com.insureease.quote.entity.Quote;
import com.insureease.quote.service.QuoteService;
import com.insureease.renewal.service.RenewalService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class BusinessRulesAndComplianceTest {

    @Autowired
    private IamService iamService;
    @Autowired
    private ProductService productService;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private QuoteController quoteController;
    @Autowired
    private PolicyService policyService;
    @Autowired
    private PolicyController policyController;
    @Autowired
    private BillingService billingService;
    @Autowired
    private ClaimService claimService;
    @Autowired
    private RenewalService renewalService;
    @Autowired
    private AuditService auditService;

    private UUID acmeTenantId;

    @BeforeEach
    void setUp() {
        IamService.AuthTokensResponse auth = iamService.login(new IamService.LoginRequest(
                "ACME-INSURE", "admin@acmeinsure.com", "Admin@123"
        ));
        acmeTenantId = auth.tenantId();
        TenantContext.setTenantId(acmeTenantId);
        TenantContext.setUserId(auth.userId());
        TenantContext.setUserEmail(auth.email());
        TenantContext.setRoles(auth.roles());
        TenantContext.setCorrelationId("br-test-" + UUID.randomUUID());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("BR-4 & BR-8: Policy Grace Period holds eligible claims as ON_HOLD_PREMIUM_PENDING and releases them upon payment")
    @SuppressWarnings("unchecked")
    void gracePeriodClaimHoldAndReleaseTest() {
        Policyholder employer = quoteService.listPolicyholders().getFirst();
        Plan gmcPlan = productService.listPlans(null).stream()
                .filter(p -> "GMC-GOLD-FLOATER".equals(p.getCode()))
                .findFirst().orElseThrow();

        Quote quote = quoteService.createQuote(new QuoteService.CreateQuoteRequest(
                employer.getId(), gmcPlan.getId(), BigDecimal.ZERO, LocalDate.now().plusDays(30)
        ));
        quoteService.uploadCensusJson(quote.getId(), List.of(
                new QuoteService.CensusMemberInput(
                        "GRACE-01", "Ananya Rao", LocalDate.of(1991, 4, 10), "F", "SELF",
                        "CLASS_I", new BigDecimal("1400000"), new BigDecimal("500000"),
                        "ananya@hcl-demo.com", "9800011122", null, null, null, null, null
                )
        ));
        quoteService.calculatePremiumAndEvaluateRules(quote.getId());
        quoteService.acceptQuote(quote.getId());

        Map<String, Object> issued = policyService.issuePolicyFromQuote(new PolicyService.IssuePolicyRequest(
                quote.getId(), LocalDate.now().minusDays(45), "ANNUAL", BigDecimal.ZERO, null
        ));
        MasterPolicy policy = (MasterPolicy) issued.get("policy");
        Invoice initialInvoice = (Invoice) issued.get("invoice");

        // Activate policy first
        billingService.recordManualPayment(new BillingService.ManualPaymentRequest(
                initialInvoice.getId(), initialInvoice.getTotalAmount(), "NEFT", "UTR-INIT-GRACE"
        ));
        Member member = policyService.getPolicyMembers(policy.getId()).getFirst();

        // Transition policy to GRACE_PERIOD (simulating unpaid instalment/renewal)
        renewalService.updateGraceOrLapseStatus(policy.getId(), "GRACE_PERIOD");
        Invoice instalmentInvoice = billingService.generateInvoice(
                acmeTenantId, policy.getId(), null, "DEBIT_NOTE",
                new BigDecimal("10000.00"), new BigDecimal("18.00"),
                "Overdue Quarterly Instalment", false
        );

        // Submit claim during GRACE_PERIOD -> Must be placed in ON_HOLD_PREMIUM_PENDING (BR-4)
        Map<String, Object> claimOut = claimService.submitClaim(new ClaimService.SubmitClaimRequest(
                policy.getId(), member.getId(), null, null,
                "REIMBURSEMENT", "HOSPITALISATION", null, null,
                LocalDate.now().minusDays(2), LocalDate.now().minusDays(4), LocalDate.now().minusDays(2),
                false, "Viral Fever Hospitalisation", new BigDecimal("45000.00"), BigDecimal.ZERO
        ));
        Claim heldClaim = (Claim) claimOut.get("claim");
        assertThat(heldClaim.getStatus()).isEqualTo("ON_HOLD_PREMIUM_PENDING");

        // Attempting to adjudicate while ON_HOLD_PREMIUM_PENDING must fail with BR-4
        assertThatThrownBy(() -> claimService.adjudicateClaim(heldClaim.getId(),
                new ClaimService.AdjudicateClaimRequest("APPROVE", null, null, "OK", "Try settle")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("GRACE_PERIOD");

        // Pay the overdue instalment invoice -> Policy returns to ACTIVE and held claim is released to UNDER_REVIEW!
        billingService.recordManualPayment(new BillingService.ManualPaymentRequest(
                instalmentInvoice.getId(), instalmentInvoice.getTotalAmount(), "NEFT", "UTR-GRACE-CLEAR"
        ));

        assertThat(policyService.getPolicy(policy.getId()).getStatus()).isEqualTo("ACTIVE");
        assertThat(claimService.getClaim(heldClaim.getId()).getStatus()).isEqualTo("UNDER_REVIEW");
    }

    @Test
    @DisplayName("BR-9 & BR-10: Maker-Checker Senior Underwriter & Two-Level Senior Claims Approver Enforcement")
    @SuppressWarnings("unchecked")
    void makerCheckerAndTwoLevelClaimApprovalTest() {
        Policyholder employer = quoteService.listPolicyholders().getFirst();
        Plan gpaPlan = productService.listPlans(null).stream()
                .filter(p -> "GPA-SHIELD".equals(p.getCode()))
                .findFirst().orElseThrow();

        Quote quote = quoteService.createQuote(new QuoteService.CreateQuoteRequest(
                employer.getId(), gpaPlan.getId(), BigDecimal.ZERO, LocalDate.now().plusDays(30)
        ));
        quoteService.uploadCensusJson(quote.getId(), List.of(
                new QuoteService.CensusMemberInput(
                        "MK-01", "Karan Malhotra", LocalDate.of(1987, 1, 10), "M", "SELF",
                        "CLASS_I", new BigDecimal("2500000"), new BigDecimal("1000000"),
                        "karan@hcl-demo.com", "9812345678",
                        "Ritu Malhotra", "SPOUSE", LocalDate.of(1990, 5, 5), new BigDecimal("100.00"), null
                )
        ));
        quoteService.calculatePremiumAndEvaluateRules(quote.getId());

        // Switch context to standard junior ROLE_UNDERWRITER (without ROLE_SENIOR_UNDERWRITER)
        TenantContext.setRoles(Set.of("ROLE_UNDERWRITER"));

        // Requesting 15% discount (> 10% max-auto-discount-pct) must fail with BR-9 for junior underwriter
        assertThatThrownBy(() -> quoteService.recordUnderwriterDecision(quote.getId(),
                new QuoteService.UnderwriterDecisionRequest("COUNTER_OFFER", BigDecimal.ZERO, new BigDecimal("15.00"), "High discount")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Maker-checker violation");

        // Elevate to ROLE_SENIOR_UNDERWRITER -> Succeeds!
        TenantContext.setRoles(Set.of("ROLE_SENIOR_UNDERWRITER"));
        Quote counterOffered = quoteService.recordUnderwriterDecision(quote.getId(),
                new QuoteService.UnderwriterDecisionRequest("COUNTER_OFFER", BigDecimal.ZERO, new BigDecimal("15.00"), "Senior approved 15% discount"));
        assertThat(counterOffered.getStatus()).isEqualTo("COUNTER_OFFERED");

        quoteService.acceptQuote(quote.getId());
        Map<String, Object> issued = policyService.issuePolicyFromQuote(new PolicyService.IssuePolicyRequest(
                quote.getId(), LocalDate.now().minusDays(20), "ANNUAL", BigDecimal.ZERO, null
        ));
        MasterPolicy policy = (MasterPolicy) issued.get("policy");
        Invoice invoice = (Invoice) issued.get("invoice");
        billingService.recordManualPayment(new BillingService.ManualPaymentRequest(
                invoice.getId(), invoice.getTotalAmount(), "NEFT", "UTR-MK-01"
        ));

        Member karan = policyService.getPolicyMembers(policy.getId()).getFirst();

        // Submit PTD Claim for 10,00,000 (> 2,00,000 senior-approval-amount-threshold)
        Map<String, Object> claimOut = claimService.submitClaim(new ClaimService.SubmitClaimRequest(
                policy.getId(), karan.getId(), null, null,
                "DISABILITY_BENEFIT", "PTD", new BigDecimal("100.00"), null,
                LocalDate.now().minusDays(2), null, null, false,
                "Total paraplegia following road accident", new BigDecimal("1000000.00"), BigDecimal.ZERO
        ));
        Claim highValueClaim = (Claim) claimOut.get("claim");
        assertThat(highValueClaim.isRequiresSeniorApproval()).isTrue();

        // Junior ROLE_CLAIMS_OFFICER cannot settle > 2,00,000 alone (BR-10)
        TenantContext.setRoles(Set.of("ROLE_CLAIMS_OFFICER"));
        assertThatThrownBy(() -> claimService.adjudicateClaim(highValueClaim.getId(),
                new ClaimService.AdjudicateClaimRequest("APPROVE", null, null, "PTD_OK", "Junior review")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ROLE_SENIOR_CLAIMS_OFFICER");

        // Senior ROLE_SENIOR_CLAIMS_OFFICER approves -> Settled!
        TenantContext.setRoles(Set.of("ROLE_SENIOR_CLAIMS_OFFICER"));
        Map<String, Object> settledOut = claimService.adjudicateClaim(highValueClaim.getId(),
                new ClaimService.AdjudicateClaimRequest("APPROVE", null, null, "PTD_SENIOR_OK", "Senior sign-off"));
        Claim settled = (Claim) settledOut.get("claim");
        assertThat(settled.getStatus()).isEqualTo("SETTLED");
    }

    @Test
    @DisplayName("FR-3.2, FR-4.7, FR-10.2: CSV Census Upload, OpenPDF Document Generation, AES-256-GCM PII Encryption & DPDP Consent")
    @SuppressWarnings("unchecked")
    void csvCensusPdfAndDpdpComplianceTest() {
        // 1. Verify AES-256-GCM Encryption & Decryption
        String encryptedPan = PiiCryptoService.encryptStatic("ABCDE1234F");
        assertThat(encryptedPan).startsWith("ENC:v1:");
        assertThat(PiiCryptoService.decryptStatic(encryptedPan)).isEqualTo("ABCDE1234F");
        assertThat(PiiCryptoService.maskPan("ABCDE1234F")).isEqualTo("XXXXXX234F");

        // 2. Test Multipart CSV Census Upload with Row-Level Validation
        Policyholder employer = quoteService.listPolicyholders().getFirst();
        Plan gmcPlan = productService.listPlans(null).stream()
                .filter(p -> "GMC-GOLD-FLOATER".equals(p.getCode()))
                .findFirst().orElseThrow();

        Quote quote = quoteService.createQuote(new QuoteService.CreateQuoteRequest(
                employer.getId(), gmcPlan.getId(), BigDecimal.ZERO, LocalDate.now().plusDays(30)
        ));

        String csvContent = """
                employee_code,name,dob,gender,relation,occupation_class,annual_ctc,sum_insured,email,mobile
                CSV-01,Deepak Joshi,1990-03-15,M,SELF,CLASS_I,1600000,500000,deepak@hcl-demo.com,9876500001
                CSV-01,Sneha Joshi,1992-07-22,F,SPOUSE,CLASS_I,,500000,,
                """;
        MockMultipartFile csvFile = new MockMultipartFile(
                "file", "census.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );
        Map<String, Object> csvResult = quoteService.uploadCensusCsv(quote.getId(), csvFile);
        assertThat(csvResult.get("totalRows")).isEqualTo(2);
        assertThat(csvResult.get("invalidRows")).isEqualTo(0);

        quoteService.calculatePremiumAndEvaluateRules(quote.getId());
        quoteService.acceptQuote(quote.getId());

        // 3. Verify OpenPDF Proposal, Policy Schedule, and Member E-Card PDFs
        ResponseEntity<byte[]> quotePdf = quoteController.exportQuotePdf(quote.getId());
        assertThat(quotePdf.getBody()).isNotNull();
        assertThat(new String(quotePdf.getBody(), 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");

        Map<String, Object> issued = policyService.issuePolicyFromQuote(new PolicyService.IssuePolicyRequest(
                quote.getId(), LocalDate.now(), "ANNUAL", BigDecimal.ZERO, null
        ));
        MasterPolicy policy = (MasterPolicy) issued.get("policy");
        Invoice invoice = (Invoice) issued.get("invoice");
        billingService.recordManualPayment(new BillingService.ManualPaymentRequest(
                invoice.getId(), invoice.getTotalAmount(), "NEFT", "UTR-PDF-01"
        ));

        ResponseEntity<byte[]> schedulePdf = policyController.downloadPolicySchedule(policy.getId());
        assertThat(new String(schedulePdf.getBody(), 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");

        Member deepak = policyService.getPolicyMembers(policy.getId()).getFirst();
        ResponseEntity<byte[]> ecardPdf = policyController.downloadMemberECard(deepak.getId());
        assertThat(new String(ecardPdf.getBody(), 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");

        // 4. Verify DPDP Act 2023 Consent Record
        ConsentRecord consent = auditService.recordConsent(deepak.getId(), "CLAIM_PROCESSING", true, "127.0.0.1");
        assertThat(consent.isGranted()).isTrue();
        assertThat(auditService.getMemberConsents(deepak.getId())).hasSize(1);
    }
}
