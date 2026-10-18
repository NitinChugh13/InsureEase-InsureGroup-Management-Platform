package com.insureease.claims.service;

import com.insureease.audit.service.AuditService;
import com.insureease.billing.service.BillingService;
import com.insureease.claims.entity.Claim;
import com.insureease.claims.entity.ClaimDocument;
import com.insureease.claims.entity.ClaimEvent;
import com.insureease.claims.entity.Hospital;
import com.insureease.claims.entity.Settlement;
import com.insureease.claims.repository.ClaimDocumentRepository;
import com.insureease.claims.repository.ClaimEventRepository;
import com.insureease.claims.repository.ClaimRepository;
import com.insureease.claims.repository.HospitalRepository;
import com.insureease.claims.repository.SettlementRepository;
import com.insureease.common.exception.BusinessRuleException;
import com.insureease.common.exception.ResourceNotFoundException;
import com.insureease.common.tenant.TenantContext;
import com.insureease.notification.service.NotificationService;
import com.insureease.policy.entity.Dependent;
import com.insureease.policy.entity.MasterPolicy;
import com.insureease.policy.entity.Member;
import com.insureease.policy.entity.Nominee;
import com.insureease.policy.repository.DependentRepository;
import com.insureease.policy.repository.MasterPolicyRepository;
import com.insureease.policy.repository.MemberRepository;
import com.insureease.policy.repository.NomineeRepository;
import com.insureease.product.entity.Benefit;
import com.insureease.product.entity.Plan;
import com.insureease.product.entity.Product;
import com.insureease.product.service.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ClaimService {

    private static final AtomicInteger CLM_SEQ = new AtomicInteger(7000);
    private static final BigDecimal HUNDRED = new BigDecimal("100.00");

    private final ClaimRepository claimRepository;
    private final ClaimDocumentRepository claimDocumentRepository;
    private final ClaimEventRepository claimEventRepository;
    private final SettlementRepository settlementRepository;
    private final HospitalRepository hospitalRepository;
    private final MasterPolicyRepository masterPolicyRepository;
    private final MemberRepository memberRepository;
    private final DependentRepository dependentRepository;
    private final NomineeRepository nomineeRepository;
    private final ProductService productService;
    private final BillingService billingService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final BigDecimal seniorApprovalAmountThreshold;

    public ClaimService(ClaimRepository claimRepository,
                        ClaimDocumentRepository claimDocumentRepository,
                        ClaimEventRepository claimEventRepository,
                        SettlementRepository settlementRepository,
                        HospitalRepository hospitalRepository,
                        MasterPolicyRepository masterPolicyRepository,
                        MemberRepository memberRepository,
                        DependentRepository dependentRepository,
                        NomineeRepository nomineeRepository,
                        ProductService productService,
                        BillingService billingService,
                        AuditService auditService,
                        NotificationService notificationService,
                        @Value("${insureease.claims.senior-approval-amount-threshold:200000.00}") BigDecimal seniorApprovalAmountThreshold) {
        this.claimRepository = claimRepository;
        this.claimDocumentRepository = claimDocumentRepository;
        this.claimEventRepository = claimEventRepository;
        this.settlementRepository = settlementRepository;
        this.hospitalRepository = hospitalRepository;
        this.masterPolicyRepository = masterPolicyRepository;
        this.memberRepository = memberRepository;
        this.dependentRepository = dependentRepository;
        this.nomineeRepository = nomineeRepository;
        this.productService = productService;
        this.billingService = billingService;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.seniorApprovalAmountThreshold = seniorApprovalAmountThreshold;
    }

    public record CreateHospitalRequest(
            String code,
            String name,
            String city,
            String state,
            boolean networkFlag,
            String bankAccount,
            String ifscCode
    ) {
    }

    public record SubmitClaimRequest(
            UUID policyId,
            UUID memberId,
            UUID dependentId,
            UUID hospitalId,
            String claimCategory, // CASHLESS, REIMBURSEMENT, DEATH_BENEFIT, DISABILITY_BENEFIT
            String benefitCode,   // HOSPITALISATION, MATERNITY, DEATH, PTD, PPD, TTD
            BigDecimal disabilityPct,
            Integer disablementWeeks,
            LocalDate incidentDate,
            LocalDate admissionDate,
            LocalDate dischargeDate,
            boolean preExistingDisease,
            String diagnosis,
            BigDecimal claimedAmount,
            BigDecimal nonPayableAmount
    ) {
    }

    public record UploadDocumentRequest(
            String docType,
            String fileName,
            String contentType,
            Long fileSize
    ) {
    }

    public record AdjudicateClaimRequest(
            String decision, // APPROVE, PARTIAL_APPROVE, QUERY, REJECT
            BigDecimal overrideApprovedAmount,
            BigDecimal nonPayableAmount,
            String reasonCode,
            String remarks
    ) {
    }

    @Transactional
    public Hospital createHospital(CreateHospitalRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        Hospital h = new Hospital();
        h.setTenantId(tenantId);
        h.setCode(req.code().toUpperCase());
        h.setName(req.name());
        h.setCity(req.city());
        h.setState(req.state());
        h.setNetworkFlag(req.networkFlag());
        h.setBankAccount(req.bankAccount());
        h.setIfscCode(req.ifscCode());
        return hospitalRepository.save(h);
    }

    @Transactional(readOnly = true)
    public List<Hospital> listHospitals() {
        return hospitalRepository.findByTenantId(TenantContext.requireTenantId());
    }

    @Transactional
    public Map<String, Object> submitClaim(SubmitClaimRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        MasterPolicy policy = masterPolicyRepository.findByIdAndTenantId(req.policyId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("MasterPolicy", req.policyId()));
        Member member = memberRepository.findByIdAndTenantId(req.memberId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", req.memberId()));

        if (!member.getPolicyId().equals(policy.getId())) {
            throw new BusinessRuleException("FR-6.3", "Member does not belong to policy " + policy.getPolicyNo());
        }

        Plan plan = productService.getPlan(policy.getPlanId());
        Product product = productService.getProduct(plan.getProductId());
        List<Benefit> benefits = productService.getPlanBenefits(plan.getId());

        String category = req.claimCategory().toUpperCase();
        String benefitCode = req.benefitCode() != null ? req.benefitCode().toUpperCase() : "HOSPITALISATION";
        LocalDate incidentDate = req.incidentDate() != null ? req.incidentDate() : LocalDate.now();

        Claim claim = new Claim();
        claim.setTenantId(tenantId);
        claim.setClaimNo("CLM-" + product.getProductType() + "-" + LocalDate.now().getYear() + "-" + CLM_SEQ.incrementAndGet());
        claim.setPolicyId(policy.getId());
        claim.setMemberId(member.getId());
        claim.setDependentId(req.dependentId());
        claim.setHospitalId(req.hospitalId());
        claim.setClaimCategory(category);
        claim.setBenefitCode(benefitCode);
        claim.setDisabilityPct(req.disabilityPct());
        claim.setDisablementWeeks(req.disablementWeeks());
        claim.setIncidentDate(incidentDate);
        claim.setAdmissionDate(req.admissionDate());
        claim.setDischargeDate(req.dischargeDate());
        claim.setPreExistingDisease(req.preExistingDisease());
        claim.setDiagnosis(req.diagnosis() != null ? req.diagnosis() : "Clinical evaluation");
        claim.setClaimedAmount(req.claimedAmount().setScale(2, RoundingMode.HALF_UP));
        claim.setNonPayableAmount(req.nonPayableAmount() != null ? req.nonPayableAmount().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // Set IRDAI 2024 Master Circular SLA timer (FR-6.8): 1h for Cashless pre-auth, 15d for others
        if ("CASHLESS".equals(category)) {
            claim.setSlaDueAt(Instant.now().plus(1, ChronoUnit.HOURS));
        } else {
            claim.setSlaDueAt(Instant.now().plus(15, ChronoUnit.DAYS));
        }

        // Run 5-Step Eligibility Chain (FR-6.3, BR-3, BR-4, BR-5)
        List<String> notes = new ArrayList<>();
        String rejectionCode = null;

        // Step 1: Policy Status Check (BR-4)
        boolean graceHold = false;
        if ("ACTIVE".equals(policy.getStatus())) {
            notes.add("STEP1_POLICY_ACTIVE: Passed");
        } else if ("GRACE_PERIOD".equals(policy.getStatus())) {
            graceHold = true;
            notes.add("STEP1_BR4_GRACE_PERIOD: Policy in grace period; claim held pending overdue premium realisation");
        } else {
            rejectionCode = "BR4_POLICY_NOT_ACTIVE";
            notes.add("STEP1_FAILED: Policy status is " + policy.getStatus());
        }

        // Step 2: Member Coverage Period Check
        if (rejectionCode == null) {
            if (!"ACTIVE".equals(member.getStatus())
                    || incidentDate.isBefore(member.getCoverageStartDate())
                    || incidentDate.isAfter(member.getCoverageEndDate())) {
                rejectionCode = "MEMBER_NOT_COVERED_ON_INCIDENT_DATE";
                notes.add("STEP2_FAILED: Incident date " + incidentDate + " outside member active coverage ["
                        + member.getCoverageStartDate() + " to " + member.getCoverageEndDate() + "]");
            } else {
                notes.add("STEP2_MEMBER_COVERED: Passed");
            }
        }

        // Step 3: Waiting Period Check (BR-3)
        if (rejectionCode == null && "HEALTH".equalsIgnoreCase(product.getProductType())) {
            long tenureDays = ChronoUnit.DAYS.between(member.getContinuousCoverSince(), incidentDate);
            if (req.preExistingDisease() && tenureDays < plan.getPedWaitingDays()) {
                rejectionCode = "BR3_PED_WAITING_PERIOD_NOT_MET";
                notes.add("STEP3_FAILED: PED waiting period requires " + plan.getPedWaitingDays()
                        + " days, continuous tenure is " + tenureDays + " days");
            } else if (tenureDays < plan.getInitialWaitingDays()) {
                rejectionCode = "BR3_INITIAL_WAITING_PERIOD_NOT_MET";
                notes.add("STEP3_FAILED: Initial waiting period requires " + plan.getInitialWaitingDays()
                        + " days, continuous tenure is " + tenureDays + " days");
            } else {
                notes.add("STEP3_WAITING_PERIOD: Passed (continuous tenure " + tenureDays + " days)");
            }
        }

        // Step 4: Remaining Sum Insured Balance Check (BR-5)
        BigDecimal remainingSi = member.getRemainingSi();
        if (rejectionCode == null) {
            if (remainingSi.compareTo(BigDecimal.ZERO) <= 0) {
                rejectionCode = "BR5_SUM_INSURED_EXHAUSTED";
                notes.add("STEP4_FAILED: Member/Floater Sum Insured balance is 0.00");
            } else {
                notes.add("STEP4_SI_BALANCE: Available SI = INR " + remainingSi);
            }
        }

        // Step 5: Compute Payable Benefit Amount per Product Strategy (FR-6.5)
        if (rejectionCode != null) {
            claim.setStatus("REJECTED");
            claim.setReasonCode(rejectionCode);
            claim.setApprovedAmount(BigDecimal.ZERO);
            claim.setDecidedAt(Instant.now());
        } else {
            calculateBenefitAndDeductions(claim, product, plan, benefits, member, remainingSi);
            if (claim.getApprovedAmount().compareTo(seniorApprovalAmountThreshold) > 0) {
                claim.setRequiresSeniorApproval(true);
                notes.add("BR10_SENIOR_APPROVAL_REQUIRED: Approved amount " + claim.getApprovedAmount()
                        + " > threshold " + seniorApprovalAmountThreshold);
            }
            claim.setStatus(graceHold ? "ON_HOLD_PREMIUM_PENDING" : "DOCS_PENDING");
        }

        claim.setEligibilityNotes(String.join(" | ", notes));
        Claim saved = claimRepository.save(claim);

        recordClaimEvent(saved, null, saved.getStatus(), "CLAIM_SUBMITTED", claim.getEligibilityNotes());

        notificationService.sendNotification(tenantId, "EMAIL", "CLAIM_SUBMITTED",
                member.getEmail() != null ? member.getEmail() : "claims@policyholder.com",
                "Claim Intimation " + saved.getClaimNo() + " - " + saved.getStatus(),
                "Claim submitted for INR " + saved.getClaimedAmount() + ". Current status: " + saved.getStatus(),
                "Claim", saved.getId());

        auditService.logEvent("Claim", saved.getId(), "CLAIM_SUBMITTED",
                Map.of("claimNo", saved.getClaimNo(), "status", saved.getStatus(),
                        "claimedAmount", saved.getClaimedAmount(), "calculatedPayable", saved.getApprovedAmount()));

        return Map.of(
                "claim", saved,
                "requiredDocumentChecklist", getMandatoryChecklistForCategory(saved.getClaimCategory())
        );
    }

    @Transactional
    public Map<String, Object> uploadClaimDocument(UUID claimId, UploadDocumentRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        Claim claim = getClaim(claimId);

        if (req.fileSize() != null && req.fileSize() > 10 * 1024 * 1024L) {
            throw new BusinessRuleException("FR-6.2", "Claim document exceeds maximum allowed size of 10 MB");
        }

        ClaimDocument doc = new ClaimDocument();
        doc.setTenantId(tenantId);
        doc.setClaimId(claim.getId());
        doc.setDocType(req.docType().toUpperCase());
        doc.setFileName(req.fileName());
        doc.setContentType(req.contentType() != null ? req.contentType() : "application/pdf");
        doc.setFileSize(req.fileSize() != null ? req.fileSize() : 1024L);
        doc.setStorageRef("s3://insureease-claims/" + tenantId + "/" + claim.getClaimNo() + "/" + req.fileName());
        doc.setVerified(true);
        doc.setVerifiedBy(TenantContext.getUserId());
        ClaimDocument savedDoc = claimDocumentRepository.save(doc);

        List<ClaimDocument> allDocs = claimDocumentRepository.findByTenantIdAndClaimId(tenantId, claimId);
        Set<String> uploadedTypes = allDocs.stream().map(ClaimDocument::getDocType).collect(Collectors.toSet());
        List<String> requiredTypes = getMandatoryChecklistForCategory(claim.getClaimCategory());
        boolean checklistComplete = uploadedTypes.containsAll(requiredTypes);

        if (checklistComplete && List.of("DOCS_PENDING", "QUERY_RAISED").contains(claim.getStatus())) {
            String prev = claim.getStatus();
            claim.setStatus("UNDER_REVIEW");
            claimRepository.save(claim);
            recordClaimEvent(claim, prev, "UNDER_REVIEW", "DOCS_COMPLETE", "All mandatory checklist documents uploaded");
        }

        return Map.of(
                "document", savedDoc,
                "checklistComplete", checklistComplete,
                "missingDocuments", requiredTypes.stream().filter(r -> !uploadedTypes.contains(r)).toList(),
                "claimStatus", claim.getStatus()
        );
    }

    @Transactional
    public Map<String, Object> adjudicateClaim(UUID claimId, AdjudicateClaimRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        Claim claim = getClaim(claimId);

        if (List.of("SETTLED", "CLOSED").contains(claim.getStatus())) {
            throw new BusinessRuleException("FR-6.4", "Claim is already settled/closed");
        }
        if ("ON_HOLD_PREMIUM_PENDING".equals(claim.getStatus())) {
            throw new BusinessRuleException("BR-4",
                    "Claim cannot be settled while policy is in unpaid GRACE_PERIOD (ON_HOLD_PREMIUM_PENDING)");
        }

        String decision = req.decision().toUpperCase();
        String prevStatus = claim.getStatus();

        if ("QUERY".equals(decision) || "QUERY_RAISED".equals(decision)) {
            claim.setStatus("QUERY_RAISED");
            claim.setReasonCode(req.reasonCode() != null ? req.reasonCode() : "ADDITIONAL_DOCS_REQUIRED");
            claim.setDecisionRemarks(req.remarks());
            claim.setAdjudicatorId(TenantContext.getUserId());
            Claim saved = claimRepository.save(claim);
            recordClaimEvent(saved, prevStatus, "QUERY_RAISED", "QUERY_RAISED", req.remarks());
            return Map.of("claim", saved, "settlements", List.of());
        }

        if ("REJECT".equals(decision) || "REJECTED".equals(decision)) {
            if (req.reasonCode() == null || req.reasonCode().isBlank()) {
                throw new BusinessRuleException("FR-6.4", "Reason code is mandatory when rejecting a claim");
            }
            claim.setStatus("REJECTED");
            claim.setApprovedAmount(BigDecimal.ZERO);
            claim.setReasonCode(req.reasonCode());
            claim.setDecisionRemarks(req.remarks());
            claim.setAdjudicatorId(TenantContext.getUserId());
            claim.setDecidedAt(Instant.now());
            Claim saved = claimRepository.save(claim);
            recordClaimEvent(saved, prevStatus, "REJECTED", "CLAIM_REJECTED", req.remarks());
            auditService.logEvent("Claim", saved.getId(), "CLAIM_REJECTED",
                    Map.of("claimNo", saved.getClaimNo(), "reasonCode", req.reasonCode()));
            return Map.of("claim", saved, "settlements", List.of());
        }

        // APPROVE or PARTIAL_APPROVE
        Member member = memberRepository.findByIdAndTenantId(claim.getMemberId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", claim.getMemberId()));

        if (req.overrideApprovedAmount() != null) {
            if (req.overrideApprovedAmount().compareTo(member.getRemainingSi()) > 0) {
                throw new BusinessRuleException("BR-5",
                        "Approved amount " + req.overrideApprovedAmount() + " cannot exceed remaining SI " + member.getRemainingSi());
            }
            claim.setApprovedAmount(req.overrideApprovedAmount().setScale(2, RoundingMode.HALF_UP));
        }

        // BR-10: Two-level approval check for high-value claims
        boolean needsSenior = claim.getApprovedAmount().compareTo(seniorApprovalAmountThreshold) > 0;
        claim.setRequiresSeniorApproval(needsSenior);
        if (needsSenior
                && !TenantContext.hasRole("ROLE_SENIOR_CLAIMS_OFFICER")
                && !TenantContext.hasRole("ROLE_INSURER_ADMIN")) {
            claim.setStatus("PENDING_SENIOR_APPROVAL");
            claim.setAdjudicatorId(TenantContext.getUserId());
            claim.setDecisionRemarks(req.remarks());
            claimRepository.save(claim);
            throw new BusinessRuleException("BR-10",
                    "Claim approved amount INR " + claim.getApprovedAmount() + " exceeds single-adjudicator limit INR "
                            + seniorApprovalAmountThreshold + "; requires ROLE_SENIOR_CLAIMS_OFFICER approval");
        }

        claim.setAdjudicatorId(TenantContext.getUserId());
        if (needsSenior) {
            claim.setSeniorApproverId(TenantContext.getUserId());
        }
        claim.setReasonCode(req.reasonCode() != null ? req.reasonCode() : "ADJUDICATED_PAYABLE");
        claim.setDecisionRemarks(req.remarks());
        claim.setDecidedAt(Instant.now());

        // Update Member (and Dependent) Utilized Sum Insured (BR-5)
        member.setUtilizedSi(member.getUtilizedSi().add(claim.getApprovedAmount()));
        memberRepository.save(member);

        if (claim.getDependentId() != null) {
            dependentRepository.findByIdAndTenantId(claim.getDependentId(), tenantId).ifPresent(dep -> {
                dep.setUtilizedSi(dep.getUtilizedSi().add(claim.getApprovedAmount()));
                dependentRepository.save(dep);
            });
        }

        // Create Settlement Payouts (Hospital / Member / Nominees) & Post to Billing Ledger (FR-6.6)
        List<Settlement> settlements = createSettlementsAndLedgerPayouts(claim, member);

        claim.setStatus("SETTLED");
        Claim savedClaim = claimRepository.save(claim);
        recordClaimEvent(savedClaim, prevStatus, "SETTLED", "CLAIM_APPROVED_AND_SETTLED",
                "Approved INR " + savedClaim.getApprovedAmount() + " across " + settlements.size() + " settlement payout(s)");

        auditService.logEvent("Claim", savedClaim.getId(), "CLAIM_SETTLED",
                Map.of("claimNo", savedClaim.getClaimNo(), "approvedAmount", savedClaim.getApprovedAmount(),
                        "settlementCount", settlements.size()));

        return Map.of("claim", savedClaim, "settlements", settlements);
    }

    @Transactional(readOnly = true)
    public Claim getClaim(UUID claimId) {
        return claimRepository.findByIdAndTenantId(claimId, TenantContext.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Claim", claimId));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getClaimDetails(UUID claimId) {
        UUID tenantId = TenantContext.requireTenantId();
        Claim claim = getClaim(claimId);
        List<ClaimDocument> docs = claimDocumentRepository.findByTenantIdAndClaimId(tenantId, claimId);
        List<ClaimEvent> events = claimEventRepository.findByTenantIdAndClaimIdOrderByCreatedAtAsc(tenantId, claimId);
        List<Settlement> settlements = settlementRepository.findByTenantIdAndClaimId(tenantId, claimId);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("claim", claim);
        res.put("documents", docs);
        res.put("events", events);
        res.put("settlements", settlements);
        return res;
    }

    @Transactional(readOnly = true)
    public List<Claim> listClaims(UUID policyId) {
        UUID tenantId = TenantContext.requireTenantId();
        if (policyId != null) {
            return claimRepository.findByTenantIdAndPolicyIdOrderByCreatedAtDesc(tenantId, policyId);
        }
        return claimRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    private void calculateBenefitAndDeductions(Claim claim,
                                               Product product,
                                               Plan plan,
                                               List<Benefit> benefits,
                                               Member member,
                                               BigDecimal remainingSi) {
        String prodType = product.getProductType().toUpperCase();
        BigDecimal claimed = claim.getClaimedAmount();
        BigDecimal nonPayable = claim.getNonPayableAmount();
        BigDecimal admissible = claimed.subtract(nonPayable).max(BigDecimal.ZERO);

        if ("LIFE".equals(prodType)) {
            // Group Term Life: Lump-sum Death Benefit up to Member's Approved Sum Insured
            BigDecimal payable = claimed.min(remainingSi).setScale(2, RoundingMode.HALF_UP);
            claim.setApprovedAmount(payable);
            return;
        }

        if ("PA".equals(prodType)) {
            // Group Personal Accident: Table A (Death 100%), Table B (PTD 100%), Table C (PPD % scale), Table D (TTD weekly)
            String bCode = claim.getBenefitCode();
            BigDecimal payable;
            if ("PPD".equalsIgnoreCase(bCode)) {
                BigDecimal pct = claim.getDisabilityPct() != null ? claim.getDisabilityPct() : new BigDecimal("50.00");
                payable = member.getSumInsured().multiply(pct).divide(HUNDRED, 2, RoundingMode.HALF_UP);
            } else if ("TTD".equalsIgnoreCase(bCode)) {
                int weeks = claim.getDisablementWeeks() != null ? claim.getDisablementWeeks() : 4;
                BigDecimal weeklyCap = benefits.stream()
                        .filter(b -> "TTD".equalsIgnoreCase(b.getBenefitType()) || "TTD".equalsIgnoreCase(b.getCode()))
                        .map(Benefit::getLimitValue)
                        .findFirst()
                        .orElse(new BigDecimal("10000.00"));
                payable = weeklyCap.multiply(BigDecimal.valueOf(weeks)).setScale(2, RoundingMode.HALF_UP);
            } else {
                // DEATH or PTD = 100% of Sum Insured
                payable = member.getSumInsured();
            }
            claim.setApprovedAmount(payable.min(remainingSi).setScale(2, RoundingMode.HALF_UP));
            return;
        }

        // HEALTH (GMC): Apply benefit sub-limit, plan deductible, co-pay %, and remaining SI cap (FR-6.5, BR-5)
        BigDecimal sublimitDisallowed = BigDecimal.ZERO;
        for (Benefit b : benefits) {
            if (b.getCode().equalsIgnoreCase(claim.getBenefitCode()) || b.getBenefitType().equalsIgnoreCase(claim.getBenefitCode())) {
                BigDecimal cap = "PCT_OF_SI".equalsIgnoreCase(b.getLimitType())
                        ? member.getSumInsured().multiply(b.getLimitValue()).divide(HUNDRED, 2, RoundingMode.HALF_UP)
                        : b.getLimitValue();
                if (admissible.compareTo(cap) > 0) {
                    sublimitDisallowed = admissible.subtract(cap).setScale(2, RoundingMode.HALF_UP);
                    admissible = cap;
                }
            }
        }
        claim.setSublimitDisallowed(sublimitDisallowed);

        BigDecimal deductible = plan.getDeductibleAmount().min(admissible).setScale(2, RoundingMode.HALF_UP);
        claim.setDeductibleAmount(deductible);
        BigDecimal afterDeductible = admissible.subtract(deductible);

        BigDecimal copay = afterDeductible.multiply(plan.getCopayPct()).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        claim.setCopayAmount(copay);

        BigDecimal netPayable = afterDeductible.subtract(copay).min(remainingSi).setScale(2, RoundingMode.HALF_UP);
        claim.setApprovedAmount(netPayable);
    }

    private List<Settlement> createSettlementsAndLedgerPayouts(Claim claim, Member member) {
        UUID tenantId = claim.getTenantId();
        List<Settlement> settlements = new ArrayList<>();
        BigDecimal totalPayable = claim.getApprovedAmount();

        if ("DEATH_BENEFIT".equalsIgnoreCase(claim.getClaimCategory())) {
            List<Nominee> nominees = nomineeRepository.findByTenantIdAndMemberId(tenantId, member.getId());
            if (!nominees.isEmpty()) {
                for (Nominee nom : nominees) {
                    BigDecimal nomineeShareAmount = totalPayable.multiply(nom.getSharePct())
                            .divide(HUNDRED, 2, RoundingMode.HALF_UP);
                    Settlement s = buildSettlement(tenantId, claim.getId(), "NOMINEE",
                            nom.getName() + " (" + nom.getRelation() + ")", nom.getSharePct(), nomineeShareAmount);
                    settlements.add(settlementRepository.save(s));
                    billingService.recordClaimSettlementPayout(tenantId, claim.getPolicyId(), claim.getId(),
                            nomineeShareAmount, nom.getName(), s.getUtrNo());
                }
                return settlements;
            }
        }

        if ("CASHLESS".equalsIgnoreCase(claim.getClaimCategory()) && claim.getHospitalId() != null) {
            Hospital hosp = hospitalRepository.findByIdAndTenantId(claim.getHospitalId(), tenantId).orElse(null);
            String payeeName = hosp != null ? hosp.getName() : "Network Hospital";
            Settlement s = buildSettlement(tenantId, claim.getId(), "HOSPITAL", payeeName, HUNDRED, totalPayable);
            settlements.add(settlementRepository.save(s));
            billingService.recordClaimSettlementPayout(tenantId, claim.getPolicyId(), claim.getId(),
                    totalPayable, payeeName, s.getUtrNo());
        } else {
            Settlement s = buildSettlement(tenantId, claim.getId(), "MEMBER", member.getName(), HUNDRED, totalPayable);
            settlements.add(settlementRepository.save(s));
            billingService.recordClaimSettlementPayout(tenantId, claim.getPolicyId(), claim.getId(),
                    totalPayable, member.getName(), s.getUtrNo());
        }
        return settlements;
    }

    private Settlement buildSettlement(UUID tenantId, UUID claimId, String payeeType, String payeeName, BigDecimal sharePct, BigDecimal amount) {
        Settlement s = new Settlement();
        s.setTenantId(tenantId);
        s.setClaimId(claimId);
        s.setPayeeType(payeeType);
        s.setPayeeName(payeeName);
        s.setSharePct(sharePct);
        s.setAmount(amount);
        s.setPaymentMode("NEFT");
        s.setUtrNo("UTR" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        s.setStatus("SETTLED");
        s.setSettledOn(Instant.now());
        return s;
    }

    private void recordClaimEvent(Claim claim, String fromStatus, String toStatus, String eventType, String note) {
        ClaimEvent ev = new ClaimEvent();
        ev.setTenantId(claim.getTenantId());
        ev.setClaimId(claim.getId());
        ev.setFromStatus(fromStatus);
        ev.setToStatus(toStatus);
        ev.setEventType(eventType);
        ev.setActorId(TenantContext.getUserId());
        ev.setNote(note);
        claimEventRepository.save(ev);
    }

    private List<String> getMandatoryChecklistForCategory(String category) {
        return switch (category.toUpperCase()) {
            case "CASHLESS" -> List.of("PRE_AUTH_FORM", "ID_PROOF");
            case "REIMBURSEMENT" -> List.of("DISCHARGE_SUMMARY", "HOSPITAL_BILL");
            case "DEATH_BENEFIT" -> List.of("DEATH_CERTIFICATE", "NOMINEE_KYC");
            case "DISABILITY_BENEFIT" -> List.of("FIR", "DISABILITY_CERT");
            default -> List.of("HOSPITAL_BILL");
        };
    }
}
