package com.insureease.product.service;

import com.insureease.audit.service.AuditService;
import com.insureease.common.exception.BusinessRuleException;
import com.insureease.common.exception.ResourceNotFoundException;
import com.insureease.common.tenant.TenantContext;
import com.insureease.product.entity.Benefit;
import com.insureease.product.entity.Plan;
import com.insureease.product.entity.Product;
import com.insureease.product.entity.RateRow;
import com.insureease.product.entity.RateTable;
import com.insureease.product.entity.UnderwritingRuleDef;
import com.insureease.product.repository.BenefitRepository;
import com.insureease.product.repository.PlanRepository;
import com.insureease.product.repository.ProductRepository;
import com.insureease.product.repository.RateRowRepository;
import com.insureease.product.repository.RateTableRepository;
import com.insureease.product.repository.UnderwritingRuleDefRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final PlanRepository planRepository;
    private final BenefitRepository benefitRepository;
    private final RateTableRepository rateTableRepository;
    private final RateRowRepository rateRowRepository;
    private final UnderwritingRuleDefRepository ruleDefRepository;
    private final AuditService auditService;

    public ProductService(ProductRepository productRepository,
                          PlanRepository planRepository,
                          BenefitRepository benefitRepository,
                          RateTableRepository rateTableRepository,
                          RateRowRepository rateRowRepository,
                          UnderwritingRuleDefRepository ruleDefRepository,
                          AuditService auditService) {
        this.productRepository = productRepository;
        this.planRepository = planRepository;
        this.benefitRepository = benefitRepository;
        this.rateTableRepository = rateTableRepository;
        this.rateRowRepository = rateRowRepository;
        this.ruleDefRepository = ruleDefRepository;
        this.auditService = auditService;
    }

    public record CreateProductRequest(
            String code,
            String name,
            String productType,
            String description,
            BigDecimal gstPct
    ) {
    }

    public record CreatePlanRequest(
            String code,
            String name,
            String coverBasis,
            String siBasis,
            BigDecimal salaryMultiple,
            BigDecimal minSumInsured,
            BigDecimal maxSumInsured,
            BigDecimal freeCoverLimit,
            String rateUnit,
            BigDecimal copayPct,
            BigDecimal deductibleAmount,
            Integer initialWaitingDays,
            Integer pedWaitingDays,
            Integer maxDependents,
            Integer maxChildAge,
            Integer maxParentAge,
            Integer maxEntryAge,
            BigDecimal floaterDiscountPct
    ) {
    }

    public record CreateBenefitRequest(
            String code,
            String name,
            String benefitType,
            String limitType,
            BigDecimal limitValue,
            Integer waitingDays
    ) {
    }

    public record RateRowInput(
            int ageFrom,
            int ageTo,
            String gender,
            String relation,
            String occupationClass,
            BigDecimal siSlab,
            BigDecimal baseRate
    ) {
    }

    public record CreateRateTableRequest(
            String name,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            List<RateRowInput> rows
    ) {
    }

    public record CreateUwRuleRequest(
            String ruleCode,
            String description,
            String metric,
            String operator,
            BigDecimal thresholdValue,
            String action
    ) {
    }

    @Transactional
    public Product createProduct(CreateProductRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        productRepository.findByTenantIdAndCodeIgnoreCase(tenantId, req.code()).ifPresent(p -> {
            throw new BusinessRuleException("FR-2.1", "Product code already exists: " + req.code());
        });

        String type = req.productType().toUpperCase();
        if (!List.of("HEALTH", "LIFE", "PA").contains(type)) {
            throw new BusinessRuleException("FR-2.1", "productType must be one of HEALTH (GMC), LIFE (GTL), or PA (GPA)");
        }

        Product product = new Product();
        product.setTenantId(tenantId);
        product.setCode(req.code().toUpperCase());
        product.setName(req.name());
        product.setProductType(type);
        product.setDescription(req.description());
        product.setGstPct(req.gstPct() != null ? req.gstPct() : new BigDecimal("18.00"));
        product.setStatus("ACTIVE");
        Product saved = productRepository.save(product);

        auditService.logEvent("Product", saved.getId(), "PRODUCT_CREATED",
                Map.of("code", saved.getCode(), "productType", saved.getProductType()));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Product> listProducts() {
        return productRepository.findByTenantId(TenantContext.requireTenantId());
    }

    @Transactional(readOnly = true)
    public Product getProduct(UUID productId) {
        return productRepository.findByIdAndTenantId(productId, TenantContext.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
    }

    @Transactional
    public Plan createPlan(UUID productId, CreatePlanRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        Product product = getProduct(productId);
        planRepository.findByTenantIdAndCodeIgnoreCase(tenantId, req.code()).ifPresent(p -> {
            throw new BusinessRuleException("FR-2.1", "Plan code already exists: " + req.code());
        });

        Plan plan = new Plan();
        plan.setTenantId(tenantId);
        plan.setProductId(product.getId());
        plan.setCode(req.code().toUpperCase());
        plan.setName(req.name());
        plan.setCoverBasis(req.coverBasis() != null ? req.coverBasis().toUpperCase() : "INDIVIDUAL");
        plan.setSiBasis(req.siBasis() != null ? req.siBasis().toUpperCase() : "FLAT_SLAB");
        plan.setSalaryMultiple(req.salaryMultiple());
        plan.setMinSumInsured(req.minSumInsured() != null ? req.minSumInsured() : new BigDecimal("100000.00"));
        plan.setMaxSumInsured(req.maxSumInsured() != null ? req.maxSumInsured() : new BigDecimal("5000000.00"));
        plan.setFreeCoverLimit(req.freeCoverLimit());
        plan.setRateUnit(req.rateUnit() != null ? req.rateUnit().toUpperCase() : "FLAT_AMOUNT");
        plan.setCopayPct(req.copayPct() != null ? req.copayPct() : BigDecimal.ZERO);
        plan.setDeductibleAmount(req.deductibleAmount() != null ? req.deductibleAmount() : BigDecimal.ZERO);
        plan.setInitialWaitingDays(req.initialWaitingDays() != null ? req.initialWaitingDays() : 30);
        plan.setPedWaitingDays(req.pedWaitingDays() != null ? req.pedWaitingDays() : 365);
        plan.setMaxDependents(req.maxDependents() != null ? req.maxDependents() : 5);
        plan.setMaxChildAge(req.maxChildAge() != null ? req.maxChildAge() : 25);
        plan.setMaxParentAge(req.maxParentAge() != null ? req.maxParentAge() : 80);
        plan.setMaxEntryAge(req.maxEntryAge() != null ? req.maxEntryAge() : 65);
        plan.setFloaterDiscountPct(req.floaterDiscountPct() != null ? req.floaterDiscountPct() : new BigDecimal("15.00"));
        plan.setStatus("ACTIVE");
        Plan saved = planRepository.save(plan);

        auditService.logEvent("Plan", saved.getId(), "PLAN_CREATED",
                Map.of("code", saved.getCode(), "productId", productId.toString(), "rateUnit", saved.getRateUnit()));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Plan> listPlans(UUID productId) {
        UUID tenantId = TenantContext.requireTenantId();
        if (productId != null) {
            return planRepository.findByTenantIdAndProductId(tenantId, productId);
        }
        return planRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public Plan getPlan(UUID planId) {
        return planRepository.findByIdAndTenantId(planId, TenantContext.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", planId));
    }

    @Transactional
    public Benefit addBenefit(UUID planId, CreateBenefitRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        Plan plan = getPlan(planId);
        Benefit benefit = new Benefit();
        benefit.setTenantId(tenantId);
        benefit.setPlanId(plan.getId());
        benefit.setCode(req.code().toUpperCase());
        benefit.setName(req.name());
        benefit.setBenefitType(req.benefitType().toUpperCase());
        benefit.setLimitType(req.limitType().toUpperCase());
        benefit.setLimitValue(req.limitValue());
        benefit.setWaitingDays(req.waitingDays() != null ? req.waitingDays() : 0);
        return benefitRepository.save(benefit);
    }

    @Transactional(readOnly = true)
    public List<Benefit> getPlanBenefits(UUID planId) {
        return benefitRepository.findByTenantIdAndPlanId(TenantContext.requireTenantId(), planId);
    }

    @Transactional
    public Map<String, Object> createRateTable(UUID planId, CreateRateTableRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        Plan plan = getPlan(planId);

        if (req.rows() == null || req.rows().isEmpty()) {
            throw new BusinessRuleException("FR-2.2", "Rate table must contain at least one rate row");
        }

        validateRateRowsNoOverlap(req.rows());

        List<RateTable> existing = rateTableRepository.findByTenantIdAndPlanIdOrderByVersionNoDesc(tenantId, planId);
        int nextVersion = existing.isEmpty() ? 1 : existing.getFirst().getVersionNo() + 1;

        RateTable table = new RateTable();
        table.setTenantId(tenantId);
        table.setPlanId(plan.getId());
        table.setName(req.name());
        table.setVersionNo(nextVersion);
        table.setEffectiveFrom(req.effectiveFrom() != null ? req.effectiveFrom() : LocalDate.now());
        table.setEffectiveTo(req.effectiveTo());
        table.setStatus("ACTIVE");
        RateTable savedTable = rateTableRepository.save(table);

        List<RateRow> savedRows = new ArrayList<>();
        for (RateRowInput input : req.rows()) {
            RateRow row = new RateRow();
            row.setTenantId(tenantId);
            row.setRateTableId(savedTable.getId());
            row.setAgeFrom(input.ageFrom());
            row.setAgeTo(input.ageTo());
            row.setGender(input.gender() != null ? input.gender().toUpperCase() : "ANY");
            row.setRelation(input.relation() != null ? input.relation().toUpperCase() : "ANY");
            row.setOccupationClass(input.occupationClass() != null ? input.occupationClass().toUpperCase() : "ALL");
            row.setSiSlab(input.siSlab());
            row.setBaseRate(input.baseRate());
            savedRows.add(rateRowRepository.save(row));
        }

        auditService.logEvent("RateTable", savedTable.getId(), "RATE_TABLE_CREATED",
                Map.of("planId", planId.toString(), "versionNo", nextVersion, "rowCount", savedRows.size()));

        return Map.of("rateTable", savedTable, "rows", savedRows);
    }

    @Transactional(readOnly = true)
    public List<RateRow> getActiveRateRows(UUID planId) {
        UUID tenantId = TenantContext.requireTenantId();
        RateTable active = rateTableRepository.findFirstByTenantIdAndPlanIdAndStatusOrderByVersionNoDesc(
                        tenantId, planId, "ACTIVE")
                .orElseThrow(() -> new BusinessRuleException("FR-2.2", "No active rate table configured for plan: " + planId));
        return rateRowRepository.findByTenantIdAndRateTableIdOrderByAgeFromAsc(tenantId, active.getId());
    }

    @Transactional
    public UnderwritingRuleDef addUnderwritingRule(UUID planId, CreateUwRuleRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        Plan plan = getPlan(planId);
        UnderwritingRuleDef rule = new UnderwritingRuleDef();
        rule.setTenantId(tenantId);
        rule.setPlanId(plan.getId());
        rule.setRuleCode(req.ruleCode().toUpperCase());
        rule.setDescription(req.description());
        rule.setMetric(req.metric().toUpperCase());
        rule.setOperator(req.operator().toUpperCase());
        rule.setThresholdValue(req.thresholdValue());
        rule.setAction(req.action().toUpperCase());
        rule.setActive(true);
        return ruleDefRepository.save(rule);
    }

    @Transactional(readOnly = true)
    public List<UnderwritingRuleDef> getPlanRules(UUID planId) {
        return ruleDefRepository.findByTenantIdAndPlanIdAndActiveTrue(TenantContext.requireTenantId(), planId);
    }

    private void validateRateRowsNoOverlap(List<RateRowInput> rows) {
        for (RateRowInput r : rows) {
            if (r.ageFrom() < 0 || r.ageTo() < r.ageFrom()) {
                throw new BusinessRuleException("FR-2.2",
                        "Invalid age band [" + r.ageFrom() + " - " + r.ageTo() + "]");
            }
            if (r.baseRate() == null || r.baseRate().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException("FR-2.2", "Base rate must be positive");
            }
        }
        // Check overlapping age bands within identical (gender, relation, occupationClass, siSlab) group
        for (int i = 0; i < rows.size(); i++) {
            for (int j = i + 1; j < rows.size(); j++) {
                RateRowInput a = rows.get(i);
                RateRowInput b = rows.get(j);
                boolean sameDimension = eqDimension(a.gender(), b.gender(), "ANY")
                        && eqDimension(a.relation(), b.relation(), "ANY")
                        && eqDimension(a.occupationClass(), b.occupationClass(), "ALL")
                        && sameSlab(a.siSlab(), b.siSlab());
                if (sameDimension && a.ageFrom() <= b.ageTo() && b.ageFrom() <= a.ageTo()) {
                    throw new BusinessRuleException("FR-2.2",
                            "Overlapping age bands detected: [" + a.ageFrom() + "-" + a.ageTo()
                                    + "] and [" + b.ageFrom() + "-" + b.ageTo() + "]");
                }
            }
        }
    }

    private boolean eqDimension(String x, String y, String def) {
        String nx = x != null ? x.toUpperCase() : def;
        String ny = y != null ? y.toUpperCase() : def;
        return nx.equals(ny);
    }

    private boolean sameSlab(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.compareTo(b) == 0;
    }
}
