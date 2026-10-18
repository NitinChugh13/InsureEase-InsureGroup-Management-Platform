package com.insureease.product.entity;

import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "plan")
public class Plan extends BaseTenantEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cover_basis", nullable = false)
    private String coverBasis = "INDIVIDUAL"; // INDIVIDUAL, FAMILY_FLOATER

    @Column(name = "si_basis", nullable = false)
    private String siBasis = "FLAT_SLAB"; // FLAT_SLAB, SALARY_MULTIPLE

    @Column(name = "salary_multiple", precision = 8, scale = 2)
    private BigDecimal salaryMultiple;

    @Column(name = "min_sum_insured", nullable = false, precision = 19, scale = 2)
    private BigDecimal minSumInsured = new BigDecimal("100000.00");

    @Column(name = "max_sum_insured", nullable = false, precision = 19, scale = 2)
    private BigDecimal maxSumInsured = new BigDecimal("5000000.00");

    @Column(name = "free_cover_limit", precision = 19, scale = 2)
    private BigDecimal freeCoverLimit; // For GTL (Group Term Life)

    @Column(name = "rate_unit", nullable = false)
    private String rateUnit = "FLAT_AMOUNT"; // FLAT_AMOUNT, PER_MILLE, PCT_OF_SI

    @Column(name = "copay_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal copayPct = BigDecimal.ZERO;

    @Column(name = "deductible_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal deductibleAmount = BigDecimal.ZERO;

    @Column(name = "initial_waiting_days", nullable = false)
    private int initialWaitingDays = 30;

    @Column(name = "ped_waiting_days", nullable = false)
    private int pedWaitingDays = 365;

    @Column(name = "max_dependents", nullable = false)
    private int maxDependents = 5;

    @Column(name = "max_child_age", nullable = false)
    private int maxChildAge = 25;

    @Column(name = "max_parent_age", nullable = false)
    private int maxParentAge = 80;

    @Column(name = "max_entry_age", nullable = false)
    private int maxEntryAge = 65;

    @Column(name = "floater_discount_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal floaterDiscountPct = new BigDecimal("15.00");

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverBasis() {
        return coverBasis;
    }

    public void setCoverBasis(String coverBasis) {
        this.coverBasis = coverBasis;
    }

    public String getSiBasis() {
        return siBasis;
    }

    public void setSiBasis(String siBasis) {
        this.siBasis = siBasis;
    }

    public BigDecimal getSalaryMultiple() {
        return salaryMultiple;
    }

    public void setSalaryMultiple(BigDecimal salaryMultiple) {
        this.salaryMultiple = salaryMultiple;
    }

    public BigDecimal getMinSumInsured() {
        return minSumInsured;
    }

    public void setMinSumInsured(BigDecimal minSumInsured) {
        this.minSumInsured = minSumInsured;
    }

    public BigDecimal getMaxSumInsured() {
        return maxSumInsured;
    }

    public void setMaxSumInsured(BigDecimal maxSumInsured) {
        this.maxSumInsured = maxSumInsured;
    }

    public BigDecimal getFreeCoverLimit() {
        return freeCoverLimit;
    }

    public void setFreeCoverLimit(BigDecimal freeCoverLimit) {
        this.freeCoverLimit = freeCoverLimit;
    }

    public String getRateUnit() {
        return rateUnit;
    }

    public void setRateUnit(String rateUnit) {
        this.rateUnit = rateUnit;
    }

    public BigDecimal getCopayPct() {
        return copayPct;
    }

    public void setCopayPct(BigDecimal copayPct) {
        this.copayPct = copayPct;
    }

    public BigDecimal getDeductibleAmount() {
        return deductibleAmount;
    }

    public void setDeductibleAmount(BigDecimal deductibleAmount) {
        this.deductibleAmount = deductibleAmount;
    }

    public int getInitialWaitingDays() {
        return initialWaitingDays;
    }

    public void setInitialWaitingDays(int initialWaitingDays) {
        this.initialWaitingDays = initialWaitingDays;
    }

    public int getPedWaitingDays() {
        return pedWaitingDays;
    }

    public void setPedWaitingDays(int pedWaitingDays) {
        this.pedWaitingDays = pedWaitingDays;
    }

    public int getMaxDependents() {
        return maxDependents;
    }

    public void setMaxDependents(int maxDependents) {
        this.maxDependents = maxDependents;
    }

    public int getMaxChildAge() {
        return maxChildAge;
    }

    public void setMaxChildAge(int maxChildAge) {
        this.maxChildAge = maxChildAge;
    }

    public int getMaxParentAge() {
        return maxParentAge;
    }

    public void setMaxParentAge(int maxParentAge) {
        this.maxParentAge = maxParentAge;
    }

    public int getMaxEntryAge() {
        return maxEntryAge;
    }

    public void setMaxEntryAge(int maxEntryAge) {
        this.maxEntryAge = maxEntryAge;
    }

    public BigDecimal getFloaterDiscountPct() {
        return floaterDiscountPct;
    }

    public void setFloaterDiscountPct(BigDecimal floaterDiscountPct) {
        this.floaterDiscountPct = floaterDiscountPct;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
