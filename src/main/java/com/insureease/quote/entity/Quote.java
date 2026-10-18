package com.insureease.quote.entity;

import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "quote")
public class Quote extends BaseTenantEntity {

    @Column(name = "quote_no", nullable = false)
    private String quoteNo;

    @Column(name = "policyholder_id", nullable = false)
    private UUID policyholderId;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "status", nullable = false)
    private String status = "DRAFT"; // DRAFT, CENSUS_VALIDATED, UNDER_REVIEW, APPROVED, COUNTER_OFFERED, REJECTED, ACCEPTED, CONVERTED, EXPIRED

    @Column(name = "current_version", nullable = false)
    private int currentVersion = 1;

    @Column(name = "total_lives", nullable = false)
    private int totalLives = 0;

    @Column(name = "employee_count", nullable = false)
    private int employeeCount = 0;

    @Column(name = "dependent_count", nullable = false)
    private int dependentCount = 0;

    @Column(name = "invalid_row_count", nullable = false)
    private int invalidRowCount = 0;

    @Column(name = "total_requested_si", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalRequestedSi = BigDecimal.ZERO;

    @Column(name = "total_approved_si", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalApprovedSi = BigDecimal.ZERO;

    @Column(name = "base_premium", nullable = false, precision = 19, scale = 2)
    private BigDecimal basePremium = BigDecimal.ZERO;

    @Column(name = "group_discount_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal groupDiscountPct = BigDecimal.ZERO;

    @Column(name = "loading_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal loadingPct = BigDecimal.ZERO;

    @Column(name = "uw_adjustment_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal uwAdjustmentPct = BigDecimal.ZERO;

    @Column(name = "net_premium", nullable = false, precision = 19, scale = 2)
    private BigDecimal netPremium = BigDecimal.ZERO;

    @Column(name = "gst_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal gstPct = new BigDecimal("18.00");

    @Column(name = "gst_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal gstAmount = BigDecimal.ZERO;

    @Column(name = "total_premium", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPremium = BigDecimal.ZERO;

    @Column(name = "valid_till", nullable = false)
    private LocalDate validTill;

    @Column(name = "parent_policy_id")
    private UUID parentPolicyId;

    public String getQuoteNo() {
        return quoteNo;
    }

    public void setQuoteNo(String quoteNo) {
        this.quoteNo = quoteNo;
    }

    public UUID getPolicyholderId() {
        return policyholderId;
    }

    public void setPolicyholderId(UUID policyholderId) {
        this.policyholderId = policyholderId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(int currentVersion) {
        this.currentVersion = currentVersion;
    }

    public int getTotalLives() {
        return totalLives;
    }

    public void setTotalLives(int totalLives) {
        this.totalLives = totalLives;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }

    public int getDependentCount() {
        return dependentCount;
    }

    public void setDependentCount(int dependentCount) {
        this.dependentCount = dependentCount;
    }

    public int getInvalidRowCount() {
        return invalidRowCount;
    }

    public void setInvalidRowCount(int invalidRowCount) {
        this.invalidRowCount = invalidRowCount;
    }

    public BigDecimal getTotalRequestedSi() {
        return totalRequestedSi;
    }

    public void setTotalRequestedSi(BigDecimal totalRequestedSi) {
        this.totalRequestedSi = totalRequestedSi;
    }

    public BigDecimal getTotalApprovedSi() {
        return totalApprovedSi;
    }

    public void setTotalApprovedSi(BigDecimal totalApprovedSi) {
        this.totalApprovedSi = totalApprovedSi;
    }

    public BigDecimal getBasePremium() {
        return basePremium;
    }

    public void setBasePremium(BigDecimal basePremium) {
        this.basePremium = basePremium;
    }

    public BigDecimal getGroupDiscountPct() {
        return groupDiscountPct;
    }

    public void setGroupDiscountPct(BigDecimal groupDiscountPct) {
        this.groupDiscountPct = groupDiscountPct;
    }

    public BigDecimal getLoadingPct() {
        return loadingPct;
    }

    public void setLoadingPct(BigDecimal loadingPct) {
        this.loadingPct = loadingPct;
    }

    public BigDecimal getUwAdjustmentPct() {
        return uwAdjustmentPct;
    }

    public void setUwAdjustmentPct(BigDecimal uwAdjustmentPct) {
        this.uwAdjustmentPct = uwAdjustmentPct;
    }

    public BigDecimal getNetPremium() {
        return netPremium;
    }

    public void setNetPremium(BigDecimal netPremium) {
        this.netPremium = netPremium;
    }

    public BigDecimal getGstPct() {
        return gstPct;
    }

    public void setGstPct(BigDecimal gstPct) {
        this.gstPct = gstPct;
    }

    public BigDecimal getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(BigDecimal gstAmount) {
        this.gstAmount = gstAmount;
    }

    public BigDecimal getTotalPremium() {
        return totalPremium;
    }

    public void setTotalPremium(BigDecimal totalPremium) {
        this.totalPremium = totalPremium;
    }

    public LocalDate getValidTill() {
        return validTill;
    }

    public void setValidTill(LocalDate validTill) {
        this.validTill = validTill;
    }

    public UUID getParentPolicyId() {
        return parentPolicyId;
    }

    public void setParentPolicyId(UUID parentPolicyId) {
        this.parentPolicyId = parentPolicyId;
    }
}
