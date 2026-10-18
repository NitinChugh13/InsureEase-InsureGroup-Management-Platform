package com.insureease.quote.service;

import com.insureease.audit.service.AuditService;
import com.insureease.common.exception.BusinessRuleException;
import com.insureease.common.exception.ResourceNotFoundException;
import com.insureease.common.tenant.TenantContext;
import com.insureease.notification.service.NotificationService;
import com.insureease.product.entity.Plan;
import com.insureease.product.entity.Product;
import com.insureease.product.entity.RateRow;
import com.insureease.product.entity.UnderwritingRuleDef;
import com.insureease.product.service.ProductService;
import com.insureease.quote.entity.Policyholder;
import com.insureease.quote.entity.Quote;
import com.insureease.quote.entity.QuoteMember;
import com.insureease.quote.entity.QuoteVersion;
import com.insureease.quote.entity.UnderwritingCase;
import com.insureease.quote.repository.PolicyholderRepository;
import com.insureease.quote.repository.QuoteMemberRepository;
import com.insureease.quote.repository.QuoteRepository;
import com.insureease.quote.repository.QuoteVersionRepository;
import com.insureease.quote.repository.UnderwritingCaseRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class QuoteService {

    private static final AtomicInteger QUOTE_SEQ = new AtomicInteger(1000);

    private final PolicyholderRepository policyholderRepository;
    private final QuoteRepository quoteRepository;
    private final QuoteVersionRepository quoteVersionRepository;
    private final QuoteMemberRepository quoteMemberRepository;
    private final UnderwritingCaseRepository underwritingCaseRepository;
    private final ProductService productService;
    private final PremiumEngine premiumEngine;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final BigDecimal seniorApprovalSiThreshold;
    private final BigDecimal seniorApprovalPremiumThreshold;
    private final BigDecimal maxAutoDiscountPct;

    public QuoteService(PolicyholderRepository policyholderRepository,
                        QuoteRepository quoteRepository,
                        QuoteVersionRepository quoteVersionRepository,
                        QuoteMemberRepository quoteMemberRepository,
                        UnderwritingCaseRepository underwritingCaseRepository,
                        ProductService productService,
                        PremiumEngine premiumEngine,
                        AuditService auditService,
                        NotificationService notificationService,
                        @Value("${insureease.underwriting.senior-approval-si-threshold:50000000.00}") BigDecimal seniorApprovalSiThreshold,
                        @Value("${insureease.underwriting.senior-approval-premium-threshold:1000000.00}") BigDecimal seniorApprovalPremiumThreshold,
                        @Value("${insureease.underwriting.max-auto-discount-pct:10.00}") BigDecimal maxAutoDiscountPct) {
        this.policyholderRepository = policyholderRepository;
        this.quoteRepository = quoteRepository;
        this.quoteVersionRepository = quoteVersionRepository;
        this.quoteMemberRepository = quoteMemberRepository;
        this.underwritingCaseRepository = underwritingCaseRepository;
        this.productService = productService;
        this.premiumEngine = premiumEngine;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.seniorApprovalSiThreshold = seniorApprovalSiThreshold;
        this.seniorApprovalPremiumThreshold = seniorApprovalPremiumThreshold;
        this.maxAutoDiscountPct = maxAutoDiscountPct;
    }

    public record CreatePolicyholderRequest(
            String code,
            String legalName,
            String orgType,
            String industryCode,
            String gstin,
            String pan,
            String stateCode,
            String addressLine,
            String contactName,
            String contactEmail,
            String contactMobile
    ) {
    }

    public record CreateQuoteRequest(
            UUID policyholderId,
            UUID planId,
            BigDecimal loadingPct,
            LocalDate validTill
    ) {
    }

    public record CensusMemberInput(
            String employeeCode,
            String name,
            LocalDate dob,
            String gender,
            String relation,
            String occupationClass,
            BigDecimal annualCtc,
            BigDecimal sumInsured,
            String email,
            String mobile,
            String nomineeName,
            String nomineeRelation,
            LocalDate nomineeDob,
            BigDecimal nomineeSharePct,
            String appointeeName
    ) {
    }

    public record CensusUploadRequest(List<CensusMemberInput> members) {
    }

    public record UnderwriterDecisionRequest(
            String decision, // APPROVE, REJECT, COUNTER_OFFER
            BigDecimal approvedLoadingPct,
            BigDecimal approvedDiscountPct,
            String remarks
    ) {
    }

    public record MemberUwDecisionRequest(
            String decision, // APPROVE, DECLINE
            String remarks
    ) {
    }

    @Transactional
    public Policyholder createPolicyholder(CreatePolicyholderRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        Policyholder ph = new Policyholder();
        ph.setTenantId(tenantId);
        ph.setCode(req.code().toUpperCase());
        ph.setLegalName(req.legalName());
        ph.setOrgType(req.orgType() != null ? req.orgType().toUpperCase() : "CORPORATE");
        ph.setIndustryCode(req.industryCode() != null ? req.industryCode().toUpperCase() : "IT_SERVICES");
        ph.setGstin(req.gstin().toUpperCase());
        ph.setPan(req.pan().toUpperCase());
        ph.setStateCode(req.stateCode() != null ? req.stateCode() : req.gstin().substring(0, 2));
        ph.setAddressLine(req.addressLine());
        ph.setContactName(req.contactName());
        ph.setContactEmail(req.contactEmail());
        ph.setContactMobile(req.contactMobile());
        ph.setKycStatus("VERIFIED");
        Policyholder saved = policyholderRepository.save(ph);

        auditService.logEvent("Policyholder", saved.getId(), "POLICYHOLDER_CREATED",
                Map.of("code", saved.getCode(), "legalName", saved.getLegalName(), "gstin", saved.getGstin()));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Policyholder> listPolicyholders() {
        return policyholderRepository.findByTenantId(TenantContext.requireTenantId());
    }

    @Transactional(readOnly = true)
    public Policyholder getPolicyholder(UUID id) {
        return policyholderRepository.findByIdAndTenantId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Policyholder", id));
    }

    @Transactional
    public Quote createQuote(CreateQuoteRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        Policyholder ph = getPolicyholder(req.policyholderId());
        Plan plan = productService.getPlan(req.planId());
        Product product = productService.getProduct(plan.getProductId());

        Quote quote = new Quote();
        quote.setTenantId(tenantId);
        quote.setQuoteNo("QT-" + LocalDate.now().getYear() + "-" + QUOTE_SEQ.incrementAndGet());
        quote.setPolicyholderId(ph.getId());
        quote.setPlanId(plan.getId());
        quote.setStatus("DRAFT");
        quote.setCurrentVersion(1);
        quote.setLoadingPct(req.loadingPct() != null ? req.loadingPct() : BigDecimal.ZERO);
        quote.setGstPct(product.getGstPct());
        quote.setValidTill(req.validTill() != null ? req.validTill() : LocalDate.now().plusDays(30));
        Quote saved = quoteRepository.save(quote);

        auditService.logEvent("Quote", saved.getId(), "QUOTE_CREATED",
                Map.of("quoteNo", saved.getQuoteNo(), "planCode", plan.getCode(), "productType", product.getProductType()));
        return saved;
    }

    @Transactional(readOnly = true)
    public Quote getQuote(UUID quoteId) {
        return quoteRepository.findByIdAndTenantId(quoteId, TenantContext.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Quote", quoteId));
    }

    @Transactional(readOnly = true)
    public List<Quote> listQuotes() {
        return quoteRepository.findByTenantIdOrderByCreatedAtDesc(TenantContext.requireTenantId());
    }

    @Transactional(readOnly = true)
    public List<QuoteMember> getQuoteMembers(UUID quoteId) {
        getQuote(quoteId);
        return quoteMemberRepository.findByTenantIdAndQuoteIdOrderByRowNoAsc(TenantContext.requireTenantId(), quoteId);
    }

    @Transactional
    public Map<String, Object> uploadCensusJson(UUID quoteId, List<CensusMemberInput> inputs) {
        UUID tenantId = TenantContext.requireTenantId();
        Quote quote = getQuote(quoteId);
        if (List.of("ACCEPTED", "CONVERTED").contains(quote.getStatus())) {
            throw new BusinessRuleException("FR-3.2", "Cannot upload census to an accepted or converted quote");
        }

        Plan plan = productService.getPlan(quote.getPlanId());
        Product product = productService.getProduct(plan.getProductId());

        quoteMemberRepository.deleteByTenantIdAndQuoteId(tenantId, quoteId);

        Set<String> seenSelfEmployeeCodes = new HashSet<>();
        Map<String, Integer> dependentCountsByEmp = new HashMap<>();
        List<QuoteMember> savedMembers = new ArrayList<>();
        List<Map<String, Object>> rowErrors = new ArrayList<>();

        int rowNo = 0;
        int empCount = 0;
        int depCount = 0;

        for (CensusMemberInput in : inputs) {
            rowNo++;
            List<String> errors = new ArrayList<>();

            String empCode = in.employeeCode() != null ? in.employeeCode().trim() : "";
            if (empCode.isBlank()) {
                errors.add("Missing employeeCode");
            }
            if (in.name() == null || in.name().isBlank()) {
                errors.add("Missing member name");
            }
            if (in.dob() == null) {
                errors.add("Missing dob");
            }

            String relation = in.relation() != null ? in.relation().trim().toUpperCase() : "SELF";
            int age = in.dob() != null ? Period.between(in.dob(), LocalDate.now()).getYears() : 0;

            if ("SELF".equals(relation)) {
                if (!seenSelfEmployeeCodes.add(empCode)) {
                    errors.add("Duplicate primary employeeCode: " + empCode);
                }
                if (age < 18 || age > plan.getMaxEntryAge()) {
                    errors.add("Employee age " + age + " outside allowed range [18 - " + plan.getMaxEntryAge() + "]");
                }
                empCount++;
            } else {
                // BR-6: Dependent validation
                if (!"HEALTH".equalsIgnoreCase(product.getProductType())) {
                    errors.add("BR-6: Dependents are only permitted under HEALTH (GMC) plans, not " + product.getProductType());
                } else {
                    if (!List.of("SPOUSE", "CHILD", "PARENT").contains(relation)) {
                        errors.add("BR-6: Invalid dependent relation: " + relation);
                    }
                    if ("CHILD".equals(relation) && age > plan.getMaxChildAge()) {
                        errors.add("BR-6: Child age " + age + " exceeds plan maxChildAge " + plan.getMaxChildAge());
                    }
                    if ("PARENT".equals(relation) && age > plan.getMaxParentAge()) {
                        errors.add("BR-6: Parent age " + age + " exceeds plan maxParentAge " + plan.getMaxParentAge());
                    }
                    int count = dependentCountsByEmp.merge(empCode, 1, Integer::sum);
                    if (count > plan.getMaxDependents()) {
                        errors.add("BR-6: Exceeds maxDependents (" + plan.getMaxDependents() + ") for employee " + empCode);
                    }
                }
                depCount++;
            }

            // BR-6: Nominee validation for LIFE (GTL) and PA (GPA)
            if (List.of("LIFE", "PA").contains(product.getProductType().toUpperCase()) && "SELF".equals(relation)) {
                if (in.nomineeName() == null || in.nomineeName().isBlank()) {
                    errors.add("BR-6: Mandatory nomineeName missing for " + product.getProductType() + " policy");
                }
                BigDecimal share = in.nomineeSharePct() != null ? in.nomineeSharePct() : new BigDecimal("100.00");
                if (share.compareTo(new BigDecimal("100.00")) != 0) {
                    errors.add("BR-6: Nominee share percentage must equal 100.00%");
                }
                if (in.nomineeDob() != null) {
                    int nomineeAge = Period.between(in.nomineeDob(), LocalDate.now()).getYears();
                    if (nomineeAge < 18 && (in.appointeeName() == null || in.appointeeName().isBlank())) {
                        errors.add("BR-6: appointeeName is required when nominee is a minor (< 18 years)");
                    }
                }
            }

            QuoteMember qm = new QuoteMember();
            qm.setTenantId(tenantId);
            qm.setQuoteId(quoteId);
            qm.setRowNo(rowNo);
            qm.setEmployeeCode(empCode.isBlank() ? "INVALID-" + rowNo : empCode);
            qm.setName(in.name() != null ? in.name() : "Unknown");
            qm.setDob(in.dob() != null ? in.dob() : LocalDate.of(1990, 1, 1));
            qm.setAge(Math.max(age, 0));
            qm.setGender(in.gender() != null ? in.gender().toUpperCase() : "M");
            qm.setRelation(relation);
            qm.setOccupationClass(in.occupationClass() != null ? in.occupationClass().toUpperCase() : "CLASS_I");
            qm.setAnnualCtc(in.annualCtc());
            BigDecimal reqSi = in.sumInsured() != null ? in.sumInsured() : plan.getMinSumInsured();
            qm.setRequestedSi(reqSi);
            qm.setApprovedSi(reqSi);
            qm.setEmail(in.email());
            qm.setMobile(in.mobile());
            qm.setNomineeName(in.nomineeName());
            qm.setNomineeRelation(in.nomineeRelation());
            qm.setNomineeDob(in.nomineeDob());
            qm.setNomineeSharePct(in.nomineeSharePct() != null ? in.nomineeSharePct() : new BigDecimal("100.00"));
            qm.setAppointeeName(in.appointeeName());

            if (errors.isEmpty()) {
                qm.setValid(true);
                qm.setValidationErrors(null);
            } else {
                qm.setValid(false);
                qm.setValidationErrors(String.join("; ", errors));
                rowErrors.add(Map.of(
                        "rowNo", rowNo,
                        "employeeCode", empCode,
                        "errors", errors
                ));
            }
            savedMembers.add(quoteMemberRepository.save(qm));
        }

        quote.setTotalLives(savedMembers.size());
        quote.setEmployeeCount(empCount);
        quote.setDependentCount(depCount);
        quote.setInvalidRowCount(rowErrors.size());
        quote.setStatus(rowErrors.isEmpty() ? "CENSUS_VALIDATED" : "DRAFT");
        quoteRepository.save(quote);

        auditService.logEvent("Quote", quoteId, "CENSUS_UPLOADED",
                Map.of("totalRows", savedMembers.size(), "validRows", savedMembers.size() - rowErrors.size(),
                        "invalidRows", rowErrors.size()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("quoteId", quoteId);
        result.put("status", quote.getStatus());
        result.put("totalRows", savedMembers.size());
        result.put("validRows", savedMembers.size() - rowErrors.size());
        result.put("invalidRows", rowErrors.size());
        result.put("errors", rowErrors);
        return result;
    }

    @Transactional
    public Map<String, Object> uploadCensusCsv(UUID quoteId, MultipartFile file) {
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {
            List<CensusMemberInput> list = new ArrayList<>();
            for (CSVRecord rec : parser) {
                list.add(new CensusMemberInput(
                        getCsvCol(rec, "employee_code", null),
                        getCsvCol(rec, "name", null),
                        parseDate(getCsvCol(rec, "dob", null)),
                        getCsvCol(rec, "gender", "M"),
                        getCsvCol(rec, "relation", "SELF"),
                        getCsvCol(rec, "occupation_class", "CLASS_I"),
                        parseDecimal(getCsvCol(rec, "annual_ctc", null)),
                        parseDecimal(getCsvCol(rec, "sum_insured", null)),
                        getCsvCol(rec, "email", null),
                        getCsvCol(rec, "mobile", null),
                        getCsvCol(rec, "nominee_name", null),
                        getCsvCol(rec, "nominee_relation", null),
                        parseDate(getCsvCol(rec, "nominee_dob", null)),
                        parseDecimal(getCsvCol(rec, "nominee_share_pct", "100")),
                        getCsvCol(rec, "appointee_name", null)
                ));
            }
            return uploadCensusJson(quoteId, list);
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessRuleException("FR-3.2", "Failed to parse census CSV file: " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> calculatePremiumAndEvaluateRules(UUID quoteId) {
        UUID tenantId = TenantContext.requireTenantId();
        Quote quote = getQuote(quoteId);
        List<QuoteMember> members = quoteMemberRepository.findByTenantIdAndQuoteIdOrderByRowNoAsc(tenantId, quoteId);

        if (members.isEmpty()) {
            throw new BusinessRuleException("FR-3.3", "Census must be uploaded before calculating premium");
        }
        if (quote.getInvalidRowCount() > 0) {
            throw new BusinessRuleException("FR-3.2",
                    "Census contains " + quote.getInvalidRowCount() + " invalid row(s). Fix census errors before pricing.");
        }

        Plan plan = productService.getPlan(quote.getPlanId());
        Product product = productService.getProduct(plan.getProductId());
        List<RateRow> rateRows = productService.getActiveRateRows(plan.getId());

        BigDecimal totalReqSi = BigDecimal.ZERO;
        BigDecimal totalAppSi = BigDecimal.ZERO;
        BigDecimal sumBasePremium = BigDecimal.ZERO;
        int overFclCount = 0;
        int maxAge = 0;

        for (QuoteMember qm : members) {
            PremiumEngine.MemberPricingResult res = premiumEngine.priceSingleMember(
                    product,
                    plan,
                    rateRows,
                    qm.getAge(),
                    qm.getGender(),
                    qm.getRelation(),
                    qm.getOccupationClass(),
                    qm.getAnnualCtc(),
                    qm.getRequestedSi(),
                    qm.getUwStatus()
            );
            qm.setRequestedSi(res.requestedSi());
            qm.setApprovedSi(res.approvedSi());
            qm.setExcessFclSi(res.excessFclSi());
            qm.setUwStatus(res.uwStatus());
            qm.setBasePremium(res.basePremium());
            quoteMemberRepository.save(qm);

            totalReqSi = totalReqSi.add(res.requestedSi());
            totalAppSi = totalAppSi.add(res.approvedSi());
            sumBasePremium = sumBasePremium.add(res.basePremium());
            if ("MEDICAL_REQUIRED".equals(res.uwStatus())) {
                overFclCount++;
            }
            if (qm.getAge() > maxAge) {
                maxAge = qm.getAge();
            }
        }

        PremiumEngine.AggregatePremiumBreakdown agg = premiumEngine.computeAggregate(
                sumBasePremium,
                quote.getEmployeeCount(),
                quote.getLoadingPct(),
                quote.getUwAdjustmentPct(),
                product.getGstPct()
        );

        quote.setTotalRequestedSi(totalReqSi);
        quote.setTotalApprovedSi(totalAppSi);
        quote.setBasePremium(agg.basePremium());
        quote.setGroupDiscountPct(agg.groupDiscountPct());
        quote.setNetPremium(agg.netPremium());
        quote.setGstPct(agg.gstPct());
        quote.setGstAmount(agg.gstAmount());
        quote.setTotalPremium(agg.totalPremium());

        // Evaluate Underwriting Rules (FR-3.4, BR-9, BR-12)
        List<String> triggeredRules = new ArrayList<>();
        boolean requiresSenior = false;

        if (overFclCount > 0) {
            triggeredRules.add("FCL_EXCEEDED: " + overFclCount + " member(s) exceed Free Cover Limit (" + plan.getFreeCoverLimit() + ")");
        }
        if (maxAge > 55) {
            triggeredRules.add("HIGH_AGE_BAND: Max member age is " + maxAge);
        }
        if (totalAppSi.compareTo(seniorApprovalSiThreshold) > 0) {
            triggeredRules.add("BR-9_HIGH_TOTAL_SI: Total SI " + totalAppSi + " exceeds senior threshold " + seniorApprovalSiThreshold);
            requiresSenior = true;
        }
        if (agg.totalPremium().compareTo(seniorApprovalPremiumThreshold) > 0) {
            triggeredRules.add("BR-9_HIGH_PREMIUM: Total premium " + agg.totalPremium() + " exceeds senior threshold " + seniorApprovalPremiumThreshold);
            requiresSenior = true;
        }

        for (UnderwritingRuleDef rule : productService.getPlanRules(plan.getId())) {
            if ("MAX_MEMBER_AGE".equalsIgnoreCase(rule.getMetric())
                    && BigDecimal.valueOf(maxAge).compareTo(rule.getThresholdValue()) > 0) {
                triggeredRules.add(rule.getRuleCode() + ": " + rule.getDescription());
                if ("SENIOR_UW_REQUIRED".equalsIgnoreCase(rule.getAction())) {
                    requiresSenior = true;
                }
            }
        }

        UnderwritingCase uwCase = underwritingCaseRepository
                .findFirstByTenantIdAndQuoteIdOrderByCreatedAtDesc(tenantId, quoteId)
                .orElseGet(UnderwritingCase::new);
        uwCase.setTenantId(tenantId);
        uwCase.setQuoteId(quoteId);
        uwCase.setTriggeredRules(triggeredRules.isEmpty() ? "NONE" : String.join(" | ", triggeredRules));
        uwCase.setRiskScore(triggeredRules.size() * 25);
        uwCase.setRequiresSeniorApproval(requiresSenior);

        if (triggeredRules.isEmpty()) {
            uwCase.setStatus("AUTO_APPROVED");
            uwCase.setDecision("APPROVED");
            uwCase.setDecidedAt(Instant.now());
            quote.setStatus("APPROVED");
        } else {
            uwCase.setStatus(requiresSenior ? "PENDING_SENIOR_APPROVAL" : "PENDING_REVIEW");
            quote.setStatus("UNDER_REVIEW");
        }
        underwritingCaseRepository.save(uwCase);
        quoteRepository.save(quote);

        saveQuoteVersion(quote, "Pricing calculation completed (" + quote.getStatus() + ")");

        auditService.logEvent("Quote", quoteId, "PREMIUM_CALCULATED",
                Map.of("totalPremium", quote.getTotalPremium(), "status", quote.getStatus(),
                        "overFclCount", overFclCount, "requiresSeniorApproval", requiresSenior));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("quote", quote);
        response.put("underwritingCase", uwCase);
        response.put("overFclMemberCount", overFclCount);
        return response;
    }

    @Transactional
    public Quote recordUnderwriterDecision(UUID quoteId, UnderwriterDecisionRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        Quote quote = getQuote(quoteId);
        Product product = productService.getProduct(productService.getPlan(quote.getPlanId()).getProductId());

        UnderwritingCase uwCase = underwritingCaseRepository
                .findFirstByTenantIdAndQuoteIdOrderByCreatedAtDesc(tenantId, quoteId)
                .orElseGet(() -> {
                    UnderwritingCase c = new UnderwritingCase();
                    c.setTenantId(tenantId);
                    c.setQuoteId(quoteId);
                    return c;
                });

        BigDecimal addLoading = req.approvedLoadingPct() != null ? req.approvedLoadingPct() : BigDecimal.ZERO;
        BigDecimal addDiscount = req.approvedDiscountPct() != null ? req.approvedDiscountPct() : BigDecimal.ZERO;

        // BR-9: Maker-Checker check
        boolean needsSenior = uwCase.isRequiresSeniorApproval()
                || addDiscount.compareTo(maxAutoDiscountPct) > 0
                || quote.getTotalApprovedSi().compareTo(seniorApprovalSiThreshold) > 0;
        if (needsSenior && !TenantContext.hasRole("ROLE_SENIOR_UNDERWRITER") && !TenantContext.hasRole("ROLE_INSURER_ADMIN")) {
            throw new BusinessRuleException("BR-9",
                    "Maker-checker violation: Quote requires ROLE_SENIOR_UNDERWRITER approval (high SI/premium or discount > "
                            + maxAutoDiscountPct + "%)");
        }

        String decision = req.decision().toUpperCase();
        uwCase.setDecision(decision);
        uwCase.setApprovedLoadingPct(addLoading);
        uwCase.setApprovedDiscountPct(addDiscount);
        uwCase.setRemarks(req.remarks());
        uwCase.setUnderwriterId(TenantContext.getUserId());
        if (needsSenior) {
            uwCase.setSeniorUnderwriterId(TenantContext.getUserId());
        }
        uwCase.setDecidedAt(Instant.now());

        if ("REJECT".equals(decision) || "REJECTED".equals(decision)) {
            uwCase.setStatus("REJECTED");
            quote.setStatus("REJECTED");
        } else {
            BigDecimal netUwAdjustment = addLoading.subtract(addDiscount);
            quote.setUwAdjustmentPct(netUwAdjustment);
            PremiumEngine.AggregatePremiumBreakdown agg = premiumEngine.computeAggregate(
                    quote.getBasePremium(),
                    quote.getEmployeeCount(),
                    quote.getLoadingPct(),
                    netUwAdjustment,
                    product.getGstPct()
            );
            quote.setNetPremium(agg.netPremium());
            quote.setGstAmount(agg.gstAmount());
            quote.setTotalPremium(agg.totalPremium());
            quote.setCurrentVersion(quote.getCurrentVersion() + 1);

            if ("COUNTER_OFFER".equals(decision) || "COUNTER_OFFERED".equals(decision)) {
                uwCase.setStatus("COUNTER_OFFERED");
                quote.setStatus("COUNTER_OFFERED");
            } else {
                uwCase.setStatus("APPROVED");
                quote.setStatus("APPROVED");
            }
            saveQuoteVersion(quote, req.remarks() != null ? req.remarks() : "Underwriter decision: " + decision);
        }

        underwritingCaseRepository.save(uwCase);
        Quote saved = quoteRepository.save(quote);

        notificationService.sendNotification(tenantId, "EMAIL", "QUOTE_DECISION",
                "hr@policyholder.com", "Quote " + saved.getQuoteNo() + " - " + saved.getStatus(),
                "Your quote status is now " + saved.getStatus() + ". Total Premium: INR " + saved.getTotalPremium(),
                "Quote", saved.getId());

        auditService.logEvent("Quote", quoteId, "UNDERWRITER_DECISION_" + decision,
                Map.of("status", saved.getStatus(), "totalPremium", saved.getTotalPremium()));
        return saved;
    }

    @Transactional
    public Map<String, Object> decideMemberMedicalUnderwriting(UUID quoteId, UUID quoteMemberId, MemberUwDecisionRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        QuoteMember qm = quoteMemberRepository.findByIdAndTenantId(quoteMemberId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("QuoteMember", quoteMemberId));
        if (!qm.getQuoteId().equals(quoteId)) {
            throw new BusinessRuleException("BR-12", "Member does not belong to quote " + quoteId);
        }

        String dec = req.decision().toUpperCase();
        if ("APPROVE".equals(dec) || "APPROVED".equals(dec)) {
            qm.setUwStatus("APPROVED");
            qm.setApprovedSi(qm.getRequestedSi());
            qm.setExcessFclSi(BigDecimal.ZERO);
        } else {
            qm.setUwStatus("DECLINED");
        }
        quoteMemberRepository.save(qm);

        auditService.logEvent("QuoteMember", quoteMemberId, "MEMBER_MEDICAL_UW_" + dec,
                Map.of("employeeCode", qm.getEmployeeCode(), "approvedSi", qm.getApprovedSi()));

        return calculatePremiumAndEvaluateRules(quoteId);
    }

    @Transactional
    public Quote acceptQuote(UUID quoteId) {
        Quote quote = getQuote(quoteId);
        if (!List.of("APPROVED", "COUNTER_OFFERED").contains(quote.getStatus())) {
            throw new BusinessRuleException("FR-3.7",
                    "Only an APPROVED or COUNTER_OFFERED quote can be accepted (current status: " + quote.getStatus() + ")");
        }
        if (quote.getValidTill().isBefore(LocalDate.now())) {
            quote.setStatus("EXPIRED");
            quoteRepository.save(quote);
            throw new BusinessRuleException("FR-3.6", "Quote has expired on " + quote.getValidTill());
        }
        quote.setStatus("ACCEPTED");
        Quote saved = quoteRepository.save(quote);
        auditService.logEvent("Quote", quoteId, "QUOTE_ACCEPTED", Map.of("quoteNo", saved.getQuoteNo()));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<QuoteVersion> getQuoteVersions(UUID quoteId) {
        getQuote(quoteId);
        return quoteVersionRepository.findByTenantIdAndQuoteIdOrderByVersionNoDesc(TenantContext.requireTenantId(), quoteId);
    }

    private void saveQuoteVersion(Quote quote, String remarks) {
        QuoteVersion v = new QuoteVersion();
        v.setTenantId(quote.getTenantId());
        v.setQuoteId(quote.getId());
        v.setVersionNo(quote.getCurrentVersion());
        v.setBasePremium(quote.getBasePremium());
        v.setLoadingPct(quote.getLoadingPct().add(quote.getUwAdjustmentPct().max(BigDecimal.ZERO)));
        v.setDiscountPct(quote.getGroupDiscountPct().add(quote.getUwAdjustmentPct().min(BigDecimal.ZERO).abs()));
        v.setNetPremium(quote.getNetPremium());
        v.setGstAmount(quote.getGstAmount());
        v.setTotalPremium(quote.getTotalPremium());
        v.setRemarks(remarks);
        v.setActorRole(TenantContext.getRoles().stream().findFirst().orElse("SYSTEM"));
        quoteVersionRepository.save(v);
    }

    private String getCsvCol(CSVRecord rec, String col, String def) {
        if (rec.isMapped(col)) {
            String val = rec.get(col);
            return (val != null && !val.isBlank()) ? val.trim() : def;
        }
        return def;
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return LocalDate.parse(s.trim());
    }

    private BigDecimal parseDecimal(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return new BigDecimal(s.trim());
    }
}
