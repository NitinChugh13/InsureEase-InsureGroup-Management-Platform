package com.insureease.renewal.service;

import com.insureease.audit.service.AuditService;
import com.insureease.claims.entity.Claim;
import com.insureease.claims.repository.ClaimRepository;
import com.insureease.common.exception.BusinessRuleException;
import com.insureease.common.exception.ResourceNotFoundException;
import com.insureease.common.tenant.TenantContext;
import com.insureease.policy.entity.Dependent;
import com.insureease.policy.entity.MasterPolicy;
import com.insureease.policy.entity.Member;
import com.insureease.policy.entity.Nominee;
import com.insureease.policy.repository.DependentRepository;
import com.insureease.policy.repository.MasterPolicyRepository;
import com.insureease.policy.repository.MemberRepository;
import com.insureease.policy.repository.NomineeRepository;
import com.insureease.policy.service.PolicyService;
import com.insureease.quote.entity.Quote;
import com.insureease.quote.repository.QuoteRepository;
import com.insureease.quote.service.QuoteService;
import com.insureease.renewal.entity.RenewalCase;
import com.insureease.renewal.repository.RenewalCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RenewalService {

    private static final BigDecimal HUNDRED = new BigDecimal("100.00");

    private final RenewalCaseRepository renewalCaseRepository;
    private final MasterPolicyRepository masterPolicyRepository;
    private final MemberRepository memberRepository;
    private final DependentRepository dependentRepository;
    private final NomineeRepository nomineeRepository;
    private final ClaimRepository claimRepository;
    private final QuoteRepository quoteRepository;
    private final QuoteService quoteService;
    private final PolicyService policyService;
    private final AuditService auditService;

    public RenewalService(RenewalCaseRepository renewalCaseRepository,
                          MasterPolicyRepository masterPolicyRepository,
                          MemberRepository memberRepository,
                          DependentRepository dependentRepository,
                          NomineeRepository nomineeRepository,
                          ClaimRepository claimRepository,
                          QuoteRepository quoteRepository,
                          QuoteService quoteService,
                          PolicyService policyService,
                          AuditService auditService) {
        this.renewalCaseRepository = renewalCaseRepository;
        this.masterPolicyRepository = masterPolicyRepository;
        this.memberRepository = memberRepository;
        this.dependentRepository = dependentRepository;
        this.nomineeRepository = nomineeRepository;
        this.claimRepository = claimRepository;
        this.quoteRepository = quoteRepository;
        this.quoteService = quoteService;
        this.policyService = policyService;
        this.auditService = auditService;
    }

    /**
     * FR-5.2, FR-5.3, BR-7: Compute policy Incurred Claims Ratio (ICR) and generate experience-rated renewal quote.
     */
    @Transactional
    public Map<String, Object> analyzeExperienceAndGenerateRenewalQuote(UUID policyId) {
        UUID tenantId = TenantContext.requireTenantId();
        MasterPolicy policy = masterPolicyRepository.findByIdAndTenantId(policyId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("MasterPolicy", policyId));

        List<Claim> claims = claimRepository.findByTenantIdAndPolicyIdOrderByCreatedAtDesc(tenantId, policyId);
        BigDecimal paidClaims = BigDecimal.ZERO;
        BigDecimal reservedClaims = BigDecimal.ZERO;
        int validClaimCount = 0;

        for (Claim c : claims) {
            if ("REJECTED".equalsIgnoreCase(c.getStatus())) {
                continue;
            }
            validClaimCount++;
            if (List.of("SETTLED", "APPROVED", "PARTIALLY_APPROVED", "CLOSED").contains(c.getStatus().toUpperCase())) {
                paidClaims = paidClaims.add(c.getApprovedAmount());
            } else {
                reservedClaims = reservedClaims.add(c.getApprovedAmount());
            }
        }

        BigDecimal incurredClaims = paidClaims.add(reservedClaims).setScale(2, RoundingMode.HALF_UP);
        BigDecimal earnedPremium = policy.getNetAnnualPremium().max(BigDecimal.ONE);
        BigDecimal lossRatioPct = incurredClaims.multiply(HUNDRED).divide(earnedPremium, 2, RoundingMode.HALF_UP);

        // BR-7: Experience rating rate-change curve
        BigDecimal rateChangePct = computeBr7RateChangePct(lossRatioPct);

        // Build Renewal Quote carrying forward all ACTIVE members, dependents, and nominees
        Quote renewalQuote = quoteService.createQuote(new QuoteService.CreateQuoteRequest(
                policy.getPolicyholderId(),
                policy.getPlanId(),
                rateChangePct.max(BigDecimal.ZERO),
                policy.getEndDate().plusDays(15)
        ));
        renewalQuote.setParentPolicyId(policy.getId());
        if (rateChangePct.compareTo(BigDecimal.ZERO) < 0) {
            // Apply low-ICR experience discount (-5%) via uwAdjustmentPct
            renewalQuote.setUwAdjustmentPct(rateChangePct);
        }
        quoteRepository.save(renewalQuote);

        List<Member> activeMembers = memberRepository.findByTenantIdAndPolicyIdAndStatus(tenantId, policyId, "ACTIVE");
        List<QuoteService.CensusMemberInput> censusInputs = new ArrayList<>();

        for (Member m : activeMembers) {
            List<Nominee> nominees = nomineeRepository.findByTenantIdAndMemberId(tenantId, m.getId());
            Nominee primaryNom = nominees.isEmpty() ? null : nominees.getFirst();
            censusInputs.add(new QuoteService.CensusMemberInput(
                    m.getEmployeeCode(),
                    m.getName(),
                    m.getDob(),
                    m.getGender(),
                    "SELF",
                    m.getOccupationClass(),
                    m.getAnnualCtc(),
                    m.getSumInsured(),
                    m.getEmail(),
                    m.getMobile(),
                    primaryNom != null ? primaryNom.getName() : "Legal Heir",
                    primaryNom != null ? primaryNom.getRelation() : "SPOUSE",
                    primaryNom != null ? primaryNom.getDob() : LocalDate.of(1992, 1, 1),
                    primaryNom != null ? primaryNom.getSharePct() : HUNDRED,
                    primaryNom != null ? primaryNom.getAppointeeName() : null
            ));

            List<Dependent> deps = dependentRepository.findByTenantIdAndMemberId(tenantId, m.getId());
            for (Dependent d : deps) {
                if (!"ACTIVE".equalsIgnoreCase(d.getStatus())) {
                    continue;
                }
                censusInputs.add(new QuoteService.CensusMemberInput(
                        m.getEmployeeCode(),
                        d.getName(),
                        d.getDob(),
                        d.getGender(),
                        d.getRelation(),
                        m.getOccupationClass(),
                        null,
                        d.getIndividualSi() != null ? d.getIndividualSi() : m.getSumInsured(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ));
            }
        }

        quoteService.uploadCensusJson(renewalQuote.getId(), censusInputs);
        Map<String, Object> pricingOut = quoteService.calculatePremiumAndEvaluateRules(renewalQuote.getId());
        Quote pricedRenewalQuote = (Quote) pricingOut.get("quote");

        // If ICR > 100%, require Underwriter review
        if (lossRatioPct.compareTo(HUNDRED) > 0) {
            pricedRenewalQuote.setStatus("UNDER_REVIEW");
            quoteRepository.save(pricedRenewalQuote);
        }

        int daysToExpiry = (int) Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), policy.getEndDate()));
        String window = daysToExpiry <= 30 ? "T_30" : (daysToExpiry <= 60 ? "T_60" : "T_90");

        RenewalCase rc = renewalCaseRepository.findFirstByTenantIdAndPolicyIdOrderByCreatedAtDesc(tenantId, policyId)
                .orElseGet(RenewalCase::new);
        rc.setTenantId(tenantId);
        rc.setPolicyId(policy.getId());
        rc.setExpiryDate(policy.getEndDate());
        rc.setDaysToExpiry(daysToExpiry);
        rc.setTriggerWindow(window);
        rc.setEarnedPremium(earnedPremium);
        rc.setPaidClaims(paidClaims);
        rc.setReservedClaims(reservedClaims);
        rc.setIncurredClaims(incurredClaims);
        rc.setClaimCount(validClaimCount);
        rc.setLossRatioPct(lossRatioPct);
        rc.setRecommendedRateChangePct(rateChangePct);
        rc.setRenewalQuoteId(pricedRenewalQuote.getId());
        rc.setStatus("UNDER_REVIEW".equals(pricedRenewalQuote.getStatus()) ? "UW_REVIEW_REQUIRED" : "QUOTE_GENERATED");
        RenewalCase savedCase = renewalCaseRepository.save(rc);

        auditService.logEvent("RenewalCase", savedCase.getId(), "RENEWAL_QUOTE_GENERATED",
                Map.of("policyNo", policy.getPolicyNo(), "lossRatioPct", lossRatioPct,
                        "rateChangePct", rateChangePct, "renewalQuoteNo", pricedRenewalQuote.getQuoteNo()));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("renewalCase", savedCase);
        response.put("renewalQuote", pricedRenewalQuote);
        return response;
    }

    /**
     * FR-5.5: On acceptance of renewal quote, create renewed policy term carrying forward members & waiting period continuity.
     */
    @Transactional
    public Map<String, Object> acceptAndRolloverRenewal(UUID renewalCaseId) {
        UUID tenantId = TenantContext.requireTenantId();
        RenewalCase rc = renewalCaseRepository.findByIdAndTenantId(renewalCaseId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("RenewalCase", renewalCaseId));
        MasterPolicy oldPolicy = masterPolicyRepository.findByIdAndTenantId(rc.getPolicyId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("MasterPolicy", rc.getPolicyId()));
        Quote renewalQuote = quoteService.getQuote(rc.getRenewalQuoteId());

        if ("UNDER_REVIEW".equals(renewalQuote.getStatus())) {
            quoteService.recordUnderwriterDecision(renewalQuote.getId(),
                    new QuoteService.UnderwriterDecisionRequest("APPROVE", BigDecimal.ZERO, BigDecimal.ZERO, "Approved for renewal rollover"));
        }
        if (!"ACCEPTED".equals(renewalQuote.getStatus())) {
            quoteService.acceptQuote(renewalQuote.getId());
        }

        Map<String, Object> issued = policyService.issuePolicyFromQuote(new PolicyService.IssuePolicyRequest(
                renewalQuote.getId(),
                oldPolicy.getEndDate().plusDays(1),
                oldPolicy.getPaymentFrequency(),
                BigDecimal.ZERO,
                oldPolicy.getTpaTenantId()
        ));
        MasterPolicy newPolicy = (MasterPolicy) issued.get("policy");

        oldPolicy.setStatus("RENEWED");
        masterPolicyRepository.save(oldPolicy);

        rc.setNewPolicyId(newPolicy.getId());
        rc.setStatus("RENEWED");
        renewalCaseRepository.save(rc);

        auditService.logEvent("MasterPolicy", newPolicy.getId(), "POLICY_RENEWED_ROLLOVER",
                Map.of("oldPolicyNo", oldPolicy.getPolicyNo(), "newPolicyNo", newPolicy.getPolicyNo()));

        Map<String, Object> res = new LinkedHashMap<>(issued);
        res.put("renewalCase", rc);
        res.put("priorPolicyStatus", oldPolicy.getStatus());
        return res;
    }

    /**
     * FR-5.6, BR-8: Transition policies to GRACE_PERIOD or LAPSED.
     */
    @Transactional
    public MasterPolicy updateGraceOrLapseStatus(UUID policyId, String targetStatus) {
        UUID tenantId = TenantContext.requireTenantId();
        MasterPolicy policy = masterPolicyRepository.findByIdAndTenantId(policyId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("MasterPolicy", policyId));

        String normalized = targetStatus.toUpperCase();
        if (!List.of("GRACE_PERIOD", "LAPSED").contains(normalized)) {
            throw new BusinessRuleException("BR-8", "targetStatus must be GRACE_PERIOD or LAPSED");
        }
        String prev = policy.getStatus();
        policy.setStatus(normalized);
        MasterPolicy saved = masterPolicyRepository.save(policy);
        auditService.logEvent("MasterPolicy", saved.getId(), "POLICY_STATUS_" + normalized,
                Map.of("from", prev, "to", normalized));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<RenewalCase> listRenewalCases() {
        return renewalCaseRepository.findByTenantIdOrderByCreatedAtDesc(TenantContext.requireTenantId());
    }

    public BigDecimal computeBr7RateChangePct(BigDecimal lossRatioPct) {
        if (lossRatioPct.compareTo(new BigDecimal("60.00")) < 0) {
            return new BigDecimal("-5.00");
        }
        if (lossRatioPct.compareTo(new BigDecimal("80.00")) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (lossRatioPct.compareTo(HUNDRED) <= 0) {
            return new BigDecimal("10.00");
        }
        return new BigDecimal("20.00");
    }
}
