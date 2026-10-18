package com.insureease.policy.service;

import com.insureease.audit.service.AuditService;
import com.insureease.billing.entity.CdAccount;
import com.insureease.billing.entity.Invoice;
import com.insureease.billing.service.BillingService;
import com.insureease.claims.entity.Claim;
import com.insureease.claims.repository.ClaimRepository;
import com.insureease.common.exception.BusinessRuleException;
import com.insureease.common.exception.ResourceNotFoundException;
import com.insureease.common.tenant.TenantContext;
import com.insureease.notification.service.NotificationService;
import com.insureease.policy.entity.Dependent;
import com.insureease.policy.entity.Endorsement;
import com.insureease.policy.entity.EndorsementItem;
import com.insureease.policy.entity.MasterPolicy;
import com.insureease.policy.entity.Member;
import com.insureease.policy.entity.Nominee;
import com.insureease.policy.repository.DependentRepository;
import com.insureease.policy.repository.EndorsementItemRepository;
import com.insureease.policy.repository.EndorsementRepository;
import com.insureease.policy.repository.MasterPolicyRepository;
import com.insureease.policy.repository.MemberRepository;
import com.insureease.policy.repository.NomineeRepository;
import com.insureease.product.entity.Plan;
import com.insureease.product.entity.Product;
import com.insureease.product.entity.RateRow;
import com.insureease.product.service.ProductService;
import com.insureease.quote.entity.Quote;
import com.insureease.quote.repository.QuoteRepository;
import com.insureease.quote.service.PremiumEngine;
import com.insureease.quote.service.QuoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PolicyService {

    private static final AtomicInteger POL_SEQ = new AtomicInteger(2000);
    private static final AtomicInteger END_SEQ = new AtomicInteger(3000);
    private static final AtomicInteger MEM_ADD_SEQ = new AtomicInteger(50000);

    private final MasterPolicyRepository masterPolicyRepository;
    private final MemberRepository memberRepository;
    private final DependentRepository dependentRepository;
    private final NomineeRepository nomineeRepository;
    private final EndorsementRepository endorsementRepository;
    private final EndorsementItemRepository endorsementItemRepository;
    private final QuoteRepository quoteRepository;
    private final ClaimRepository claimRepository;
    private final QuoteService quoteService;
    private final ProductService productService;
    private final PremiumEngine premiumEngine;
    private final BillingService billingService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public PolicyService(MasterPolicyRepository masterPolicyRepository,
                         MemberRepository memberRepository,
                         DependentRepository dependentRepository,
                         NomineeRepository nomineeRepository,
                         EndorsementRepository endorsementRepository,
                         EndorsementItemRepository endorsementItemRepository,
                         QuoteRepository quoteRepository,
                         ClaimRepository claimRepository,
                         QuoteService quoteService,
                         ProductService productService,
                         PremiumEngine premiumEngine,
                         BillingService billingService,
                         AuditService auditService,
                         NotificationService notificationService) {
        this.masterPolicyRepository = masterPolicyRepository;
        this.memberRepository = memberRepository;
        this.dependentRepository = dependentRepository;
        this.nomineeRepository = nomineeRepository;
        this.endorsementRepository = endorsementRepository;
        this.endorsementItemRepository = endorsementItemRepository;
        this.quoteRepository = quoteRepository;
        this.claimRepository = claimRepository;
        this.quoteService = quoteService;
        this.productService = productService;
        this.premiumEngine = premiumEngine;
        this.billingService = billingService;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    public record IssuePolicyRequest(
            UUID quoteId,
            LocalDate startDate,
            String paymentFrequency,
            BigDecimal initialCdFloatDeposit,
            UUID tpaTenantId
    ) {
    }

    public record EndorsementMemberActionInput(
            String action, // ADD_MEMBER, DELETE_MEMBER, UPDATE_SI, CORRECT_DATA
            String employeeCode,
            String name,
            LocalDate dob,
            String gender,
            String relation,
            String occupationClass,
            BigDecimal annualCtc,
            BigDecimal newSumInsured,
            String nomineeName,
            String nomineeRelation,
            LocalDate nomineeDob,
            BigDecimal nomineeSharePct
    ) {
    }

    public record SubmitEndorsementRequest(
            String endorsementType,
            LocalDate effectiveDate,
            String fundingMode, // CD_ACCOUNT, INVOICE
            String remarks,
            List<EndorsementMemberActionInput> items
    ) {
    }

    @Transactional
    public Map<String, Object> issuePolicyFromQuote(IssuePolicyRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        Quote quote = quoteService.getQuote(req.quoteId());

        if (!"ACCEPTED".equals(quote.getStatus())) {
            throw new BusinessRuleException("FR-4.1",
                    "Quote must be in ACCEPTED state before issuing a policy (current: " + quote.getStatus() + ")");
        }

        masterPolicyRepository.findByTenantIdAndQuoteId(tenantId, quote.getId()).ifPresent(existing -> {
            throw new BusinessRuleException("FR-4.1", "Policy already issued for quote: " + existing.getPolicyNo());
        });

        Plan plan = productService.getPlan(quote.getPlanId());
        Product product = productService.getProduct(plan.getProductId());

        LocalDate start = req.startDate() != null ? req.startDate() : LocalDate.now();
        LocalDate end = start.plusYears(1).minusDays(1);

        MasterPolicy policy = new MasterPolicy();
        policy.setTenantId(tenantId);
        policy.setPolicyNo("POL-" + product.getProductType() + "-" + start.getYear() + "-" + POL_SEQ.incrementAndGet());
        policy.setQuoteId(quote.getId());
        policy.setPolicyholderId(quote.getPolicyholderId());
        policy.setPlanId(plan.getId());
        policy.setTpaTenantId(req.tpaTenantId());
        policy.setParentPolicyId(quote.getParentPolicyId());
        policy.setStartDate(start);
        policy.setEndDate(end);
        policy.setGracePeriodDays(30);
        policy.setStatus("PENDING_PAYMENT");
        policy.setPaymentFrequency(req.paymentFrequency() != null ? req.paymentFrequency().toUpperCase() : "ANNUAL");
        policy.setTotalSumInsured(quote.getTotalApprovedSi());
        policy.setNetAnnualPremium(quote.getNetPremium());
        policy.setGstAmount(quote.getGstAmount());
        policy.setTotalAnnualPremium(quote.getTotalPremium());
        MasterPolicy savedPolicy = masterPolicyRepository.save(policy);

        // Mark Quote CONVERTED
        quote.setStatus("CONVERTED");
        quoteRepository.save(quote);

        // Create Section 64VB Cash Deposit (CD) Float Account
        CdAccount cdAccount = billingService.createInitialCdAccount(
                tenantId, savedPolicy.getId(), savedPolicy.getPolicyNo(), req.initialCdFloatDeposit());

        // Generate Initial GST Tax Invoice (FR-7.1)
        Invoice invoice = billingService.generateInvoice(
                tenantId,
                savedPolicy.getId(),
                null,
                "TAX_INVOICE",
                savedPolicy.getNetAnnualPremium(),
                product.getGstPct(),
                "Annual Group Insurance Premium for Master Policy " + savedPolicy.getPolicyNo(),
                false
        );

        auditService.logEvent("MasterPolicy", savedPolicy.getId(), "POLICY_ISSUED",
                Map.of("policyNo", savedPolicy.getPolicyNo(), "status", savedPolicy.getStatus(),
                        "invoiceNo", invoice.getInvoiceNo(), "cdAccountNo", cdAccount.getAccountNo()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("policy", savedPolicy);
        result.put("invoice", invoice);
        result.put("cdAccount", cdAccount);
        return result;
    }

    @Transactional(readOnly = true)
    public MasterPolicy getPolicy(UUID policyId) {
        return masterPolicyRepository.findByIdAndTenantId(policyId, TenantContext.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("MasterPolicy", policyId));
    }

    @Transactional(readOnly = true)
    public List<MasterPolicy> listPolicies() {
        return masterPolicyRepository.findByTenantIdOrderByCreatedAtDesc(TenantContext.requireTenantId());
    }

    @Transactional(readOnly = true)
    public List<Member> getPolicyMembers(UUID policyId) {
        getPolicy(policyId);
        return memberRepository.findByTenantIdAndPolicyIdOrderByEmployeeCodeAsc(TenantContext.requireTenantId(), policyId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMemberProfile(UUID memberId) {
        UUID tenantId = TenantContext.requireTenantId();
        Member member = memberRepository.findByIdAndTenantId(memberId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));
        List<Dependent> dependents = dependentRepository.findByTenantIdAndMemberId(tenantId, memberId);
        List<Nominee> nominees = nomineeRepository.findByTenantIdAndMemberId(tenantId, memberId);
        return Map.of("member", member, "dependents", dependents, "nominees", nominees);
    }

    @Transactional
    public Map<String, Object> submitEndorsement(UUID policyId, SubmitEndorsementRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        MasterPolicy policy = getPolicy(policyId);

        if (!"ACTIVE".equals(policy.getStatus())) {
            throw new BusinessRuleException("FR-4.3",
                    "Endorsements can only be submitted on an ACTIVE policy (current: " + policy.getStatus() + ")");
        }

        LocalDate effDate = req.effectiveDate() != null ? req.effectiveDate() : LocalDate.now();
        if (effDate.isBefore(policy.getStartDate()) || effDate.isAfter(policy.getEndDate())) {
            throw new BusinessRuleException("FR-4.3",
                    "Endorsement effectiveDate " + effDate + " must be within policy term ["
                            + policy.getStartDate() + " to " + policy.getEndDate() + "]");
        }

        long totalPolicyDays = ChronoUnit.DAYS.between(policy.getStartDate(), policy.getEndDate()) + 1;
        long remainingDays = ChronoUnit.DAYS.between(effDate, policy.getEndDate()) + 1;

        Plan plan = productService.getPlan(policy.getPlanId());
        Product product = productService.getProduct(plan.getProductId());
        List<RateRow> rateRows = productService.getActiveRateRows(plan.getId());

        Endorsement endorsement = new Endorsement();
        endorsement.setTenantId(tenantId);
        endorsement.setEndorsementNo("END-" + LocalDate.now().getYear() + "-" + END_SEQ.incrementAndGet());
        endorsement.setPolicyId(policy.getId());
        endorsement.setEndorsementType(req.endorsementType() != null ? req.endorsementType().toUpperCase() : "BATCH");
        endorsement.setEffectiveDate(effDate);
        endorsement.setStatus("SUBMITTED");
        endorsement.setFundingMode(req.fundingMode() != null ? req.fundingMode().toUpperCase() : "CD_ACCOUNT");
        endorsement.setRemarks(req.remarks());
        Endorsement savedEndorsement = endorsementRepository.save(endorsement);

        BigDecimal totalNetDelta = BigDecimal.ZERO;
        List<EndorsementItem> savedItems = new ArrayList<>();

        for (EndorsementMemberActionInput itemIn : req.items()) {
            String action = itemIn.action().toUpperCase();
            EndorsementItem item = new EndorsementItem();
            item.setTenantId(tenantId);
            item.setEndorsementId(savedEndorsement.getId());
            item.setAction(action);
            item.setEmployeeCode(itemIn.employeeCode());
            item.setName(itemIn.name());
            item.setDob(itemIn.dob());
            item.setGender(itemIn.gender() != null ? itemIn.gender().toUpperCase() : "M");
            item.setRelation(itemIn.relation() != null ? itemIn.relation().toUpperCase() : "SELF");
            item.setOccupationClass(itemIn.occupationClass() != null ? itemIn.occupationClass().toUpperCase() : "CLASS_I");
            item.setAnnualCtc(itemIn.annualCtc());
            item.setNewSumInsured(itemIn.newSumInsured());
            item.setNomineeName(itemIn.nomineeName());
            item.setNomineeRelation(itemIn.nomineeRelation());
            item.setNomineeDob(itemIn.nomineeDob());
            item.setNomineeSharePct(itemIn.nomineeSharePct());
            item.setDaysRemaining((int) remainingDays);

            if ("ADD_MEMBER".equals(action)) {
                int age = itemIn.dob() != null ? Period.between(itemIn.dob(), effDate).getYears() : 30;
                PremiumEngine.MemberPricingResult pricing = premiumEngine.priceSingleMember(
                        product, plan, rateRows, age, item.getGender(), item.getRelation(),
                        item.getOccupationClass(), item.getAnnualCtc(), item.getNewSumInsured(), "AUTO_APPROVED");

                item.setNewSumInsured(pricing.approvedSi());
                item.setAnnualPremiumBase(pricing.basePremium());
                // BR-2: Pro-rata premium = annual * remainingDays / totalPolicyDays
                BigDecimal proRata = pricing.basePremium()
                        .multiply(BigDecimal.valueOf(remainingDays))
                        .divide(BigDecimal.valueOf(totalPolicyDays), 2, RoundingMode.HALF_UP);
                item.setProRataNetDelta(proRata);
                item.setValidationMessage("Joiner pro-rata addition for " + remainingDays + "/" + totalPolicyDays + " days");
                totalNetDelta = totalNetDelta.add(proRata);
            } else if ("DELETE_MEMBER".equals(action)) {
                Member existingMember = memberRepository.findByTenantIdAndPolicyIdAndEmployeeCode(
                                tenantId, policy.getId(), itemIn.employeeCode())
                        .orElseThrow(() -> new ResourceNotFoundException("Member", itemIn.employeeCode()));

                item.setMemberId(existingMember.getId());
                item.setName(existingMember.getName());
                item.setAnnualPremiumBase(existingMember.getAnnualMemberPremium());

                // BR-11: Check if member has any non-rejected claim in the current policy term
                List<Claim> memberClaims = claimRepository.findByTenantIdAndMemberIdOrderByCreatedAtDesc(
                        tenantId, existingMember.getId());
                boolean hasActiveOrSettledClaim = memberClaims.stream()
                        .anyMatch(c -> !"REJECTED".equalsIgnoreCase(c.getStatus()));

                item.setPriorClaimExists(hasActiveOrSettledClaim);
                if (hasActiveOrSettledClaim) {
                    item.setProRataNetDelta(BigDecimal.ZERO);
                    item.setValidationMessage("BR-11: Member has incurred claim(s) in policy term; deletion refund is INR 0.00");
                } else {
                    BigDecimal refund = existingMember.getAnnualMemberPremium()
                            .multiply(BigDecimal.valueOf(remainingDays))
                            .divide(BigDecimal.valueOf(totalPolicyDays), 2, RoundingMode.HALF_UP)
                            .negate();
                    item.setProRataNetDelta(refund);
                    item.setValidationMessage("Leaver pro-rata refund for " + remainingDays + "/" + totalPolicyDays + " days");
                    totalNetDelta = totalNetDelta.add(refund);
                }
            } else if ("UPDATE_SI".equals(action)) {
                Member existingMember = memberRepository.findByTenantIdAndPolicyIdAndEmployeeCode(
                                tenantId, policy.getId(), itemIn.employeeCode())
                        .orElseThrow(() -> new ResourceNotFoundException("Member", itemIn.employeeCode()));
                item.setMemberId(existingMember.getId());
                PremiumEngine.MemberPricingResult newPricing = premiumEngine.priceSingleMember(
                        product, plan, rateRows, existingMember.getAge(), existingMember.getGender(), "SELF",
                        existingMember.getOccupationClass(), itemIn.annualCtc() != null ? itemIn.annualCtc() : existingMember.getAnnualCtc(),
                        itemIn.newSumInsured(), "APPROVED");
                BigDecimal annualDiff = newPricing.basePremium().subtract(existingMember.getAnnualMemberPremium());
                BigDecimal proRataDiff = annualDiff
                        .multiply(BigDecimal.valueOf(remainingDays))
                        .divide(BigDecimal.valueOf(totalPolicyDays), 2, RoundingMode.HALF_UP);
                item.setAnnualPremiumBase(annualDiff);
                item.setProRataNetDelta(proRataDiff);
                item.setValidationMessage("Mid-term SI revision from " + existingMember.getSumInsured() + " to " + newPricing.approvedSi());
                totalNetDelta = totalNetDelta.add(proRataDiff);
            } else {
                // CORRECT_DATA
                Member existingMember = memberRepository.findByTenantIdAndPolicyIdAndEmployeeCode(
                                tenantId, policy.getId(), itemIn.employeeCode())
                        .orElseThrow(() -> new ResourceNotFoundException("Member", itemIn.employeeCode()));
                item.setMemberId(existingMember.getId());
                item.setProRataNetDelta(BigDecimal.ZERO);
                item.setValidationMessage("Non-financial member demographic correction");
            }

            savedItems.add(endorsementItemRepository.save(item));
        }

        BigDecimal gstDelta = totalNetDelta.multiply(product.getGstPct())
                .divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP);
        BigDecimal totalDelta = totalNetDelta.add(gstDelta).setScale(2, RoundingMode.HALF_UP);

        savedEndorsement.setNetPremiumDelta(totalNetDelta);
        savedEndorsement.setGstDelta(gstDelta);
        savedEndorsement.setTotalPremiumDelta(totalDelta);
        endorsementRepository.save(savedEndorsement);

        auditService.logEvent("Endorsement", savedEndorsement.getId(), "ENDORSEMENT_SUBMITTED",
                Map.of("endorsementNo", savedEndorsement.getEndorsementNo(), "itemCount", savedItems.size(),
                        "totalPremiumDelta", totalDelta));

        return Map.of("endorsement", savedEndorsement, "items", savedItems);
    }

    @Transactional
    public Map<String, Object> approveAndApplyEndorsement(UUID endorsementId) {
        UUID tenantId = TenantContext.requireTenantId();
        Endorsement endorsement = endorsementRepository.findByIdAndTenantId(endorsementId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Endorsement", endorsementId));

        if (!"SUBMITTED".equals(endorsement.getStatus())) {
            throw new BusinessRuleException("FR-4.3", "Endorsement is already processed: " + endorsement.getStatus());
        }

        MasterPolicy policy = getPolicy(endorsement.getPolicyId());
        Plan plan = productService.getPlan(policy.getPlanId());
        Product product = productService.getProduct(plan.getProductId());
        List<EndorsementItem> items = endorsementItemRepository.findByTenantIdAndEndorsementId(tenantId, endorsementId);

        BigDecimal totalDelta = endorsement.getTotalPremiumDelta();
        Invoice noteInvoice = null;
        boolean fundedImmediately = true;

        if (totalDelta.compareTo(BigDecimal.ZERO) > 0) {
            // Additional premium required (Section 64VB check - BR-12)
            boolean debitedCd = false;
            if ("CD_ACCOUNT".equalsIgnoreCase(endorsement.getFundingMode())) {
                debitedCd = billingService.tryDebitCdAccountForEndorsement(
                        tenantId, policy.getId(), endorsement.getId(), totalDelta,
                        "Auto-debit for Endorsement " + endorsement.getEndorsementNo());
            }
            noteInvoice = billingService.generateInvoice(
                    tenantId, policy.getId(), endorsement.getId(), "DEBIT_NOTE",
                    endorsement.getNetPremiumDelta(), product.getGstPct(),
                    "Endorsement Debit Note for " + endorsement.getEndorsementNo(),
                    debitedCd
            );
            endorsement.setInvoiceId(noteInvoice.getId());
            if (!debitedCd) {
                endorsement.setFundingMode("INVOICE");
                fundedImmediately = false;
            }
        } else if (totalDelta.compareTo(BigDecimal.ZERO) < 0) {
            // Pro-rata refund (Credit Note + Credit CD Float Account)
            BigDecimal refundAbs = totalDelta.abs();
            billingService.creditCdAccountForEndorsement(
                    tenantId, policy.getId(), endorsement.getId(), refundAbs,
                    "Pro-rata refund credited for Endorsement " + endorsement.getEndorsementNo());
            noteInvoice = billingService.generateInvoice(
                    tenantId, policy.getId(), endorsement.getId(), "CREDIT_NOTE",
                    endorsement.getNetPremiumDelta().abs(), product.getGstPct(),
                    "Endorsement Credit Note for " + endorsement.getEndorsementNo(),
                    true
            );
            endorsement.setInvoiceId(noteInvoice.getId());
        }

        if (fundedImmediately) {
            applyEndorsementItemsToMembers(policy, items, endorsement.getEffectiveDate());
            endorsement.setStatus("APPROVED_APPLIED");
        } else {
            endorsement.setStatus("APPROVED_PENDING_PAYMENT");
        }

        endorsement.setApprovedBy(TenantContext.getUserId());
        endorsement.setApprovedAt(Instant.now());
        Endorsement saved = endorsementRepository.save(endorsement);

        auditService.logEvent("Endorsement", saved.getId(), "ENDORSEMENT_APPROVED",
                Map.of("endorsementNo", saved.getEndorsementNo(), "status", saved.getStatus(),
                        "fundedImmediately", fundedImmediately));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("endorsement", saved);
        response.put("items", items);
        if (noteInvoice != null) {
            response.put("noteInvoice", noteInvoice);
        }
        return response;
    }

    private void applyEndorsementItemsToMembers(MasterPolicy policy, List<EndorsementItem> items, LocalDate effectiveDate) {
        UUID tenantId = policy.getTenantId();
        for (EndorsementItem item : items) {
            String action = item.getAction();
            if ("ADD_MEMBER".equals(action)) {
                int seq = MEM_ADD_SEQ.incrementAndGet();
                Member m = new Member();
                m.setTenantId(tenantId);
                m.setPolicyId(policy.getId());
                m.setEmployeeCode(item.getEmployeeCode());
                m.setMemberNo("MEM-" + seq);
                m.setCertificateNo("COI-" + policy.getPolicyNo() + "-" + item.getEmployeeCode());
                m.setName(item.getName());
                LocalDate dob = item.getDob() != null ? item.getDob() : LocalDate.of(1995, 1, 1);
                m.setDob(dob);
                m.setAge(Period.between(dob, effectiveDate).getYears());
                m.setGender(item.getGender() != null ? item.getGender() : "M");
                m.setOccupationClass(item.getOccupationClass() != null ? item.getOccupationClass() : "CLASS_I");
                m.setAnnualCtc(item.getAnnualCtc());
                m.setRequestedSi(item.getNewSumInsured());
                m.setSumInsured(item.getNewSumInsured());
                m.setUtilizedSi(BigDecimal.ZERO);
                m.setAnnualMemberPremium(item.getAnnualPremiumBase());
                m.setUwStatus("AUTO_APPROVED");
                // BR-3: Newly added member waiting periods start from member cover start date
                m.setCoverageStartDate(effectiveDate);
                m.setCoverageEndDate(policy.getEndDate());
                m.setContinuousCoverSince(effectiveDate);
                m.setStatus("ACTIVE");
                Member savedMember = memberRepository.save(m);

                if (item.getNomineeName() != null && !item.getNomineeName().isBlank()) {
                    Nominee nom = new Nominee();
                    nom.setTenantId(tenantId);
                    nom.setMemberId(savedMember.getId());
                    nom.setName(item.getNomineeName());
                    nom.setRelation(item.getNomineeRelation() != null ? item.getNomineeRelation() : "SPOUSE");
                    nom.setDob(item.getNomineeDob() != null ? item.getNomineeDob() : LocalDate.of(1995, 1, 1));
                    nom.setSharePct(item.getNomineeSharePct() != null ? item.getNomineeSharePct() : new BigDecimal("100.00"));
                    nomineeRepository.save(nom);
                }

                policy.setTotalSumInsured(policy.getTotalSumInsured().add(savedMember.getSumInsured()));
            } else if ("DELETE_MEMBER".equals(action)) {
                Member m = memberRepository.findByIdAndTenantId(item.getMemberId(), tenantId).orElseThrow();
                m.setStatus("DELETED");
                m.setCoverageEndDate(effectiveDate);
                memberRepository.save(m);
                policy.setTotalSumInsured(policy.getTotalSumInsured().subtract(m.getSumInsured()).max(BigDecimal.ZERO));
            } else if ("UPDATE_SI".equals(action)) {
                Member m = memberRepository.findByIdAndTenantId(item.getMemberId(), tenantId).orElseThrow();
                BigDecimal oldSi = m.getSumInsured();
                m.setSumInsured(item.getNewSumInsured());
                m.setRequestedSi(item.getNewSumInsured());
                m.setAnnualMemberPremium(m.getAnnualMemberPremium().add(item.getAnnualPremiumBase()));
                memberRepository.save(m);
                policy.setTotalSumInsured(policy.getTotalSumInsured().subtract(oldSi).add(item.getNewSumInsured()));
            } else if ("CORRECT_DATA".equals(action)) {
                Member m = memberRepository.findByIdAndTenantId(item.getMemberId(), tenantId).orElseThrow();
                if (item.getName() != null && !item.getName().isBlank()) {
                    m.setName(item.getName());
                }
                if (item.getGender() != null && !item.getGender().isBlank()) {
                    m.setGender(item.getGender());
                }
                memberRepository.save(m);
            }
            item.setStatus("APPLIED");
            endorsementItemRepository.save(item);
        }
        masterPolicyRepository.save(policy);
    }

    @Transactional(readOnly = true)
    public List<Endorsement> listEndorsements(UUID policyId) {
        getPolicy(policyId);
        return endorsementRepository.findByTenantIdAndPolicyIdOrderByCreatedAtDesc(TenantContext.requireTenantId(), policyId);
    }
}
