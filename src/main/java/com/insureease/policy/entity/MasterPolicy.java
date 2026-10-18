package com.insureease.policy.entity;

import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "master_policy")
public class MasterPolicy extends BaseTenantEntity {

    @Column(name = "policy_no", nullable = false)
    private String policyNo;

    @Column(name = "quote_id", nullable = false)
    private UUID quoteId;

    @Column(name = "policyholder_id", nullable = false)
    private UUID policyholderId;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "tpa_tenant_id")
    private UUID tpaTenantId;

    @Column(name = "parent_policy_id")
    private UUID parentPolicyId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "grace_period_days", nullable = false)
    private int gracePeriodDays = 30;

    @Column(name = "status", nullable = false)
    private String status = "PENDING_PAYMENT"; // DRAFT, PENDING_PAYMENT, ACTIVE, GRACE_PERIOD, LAPSED, CANCELLED, EXPIRED, RENEWED

    @Column(name = "payment_frequency", nullable = false)
    private String paymentFrequency = "ANNUAL"; // ANNUAL, QUARTERLY, MONTHLY

    @Column(name = "total_sum_insured", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalSumInsured = BigDecimal.ZERO;

    @Column(name = "net_annual_premium", nullable = false, precision = 19, scale = 2)
    private BigDecimal netAnnualPremium = BigDecimal.ZERO;

    @Column(name = "gst_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal gstAmount = BigDecimal.ZERO;

    @Column(name = "total_annual_premium", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAnnualPremium = BigDecimal.ZERO;

    public String getPolicyNo() {
        return policyNo;
    }

    public void setPolicyNo(String policyNo) {
        this.policyNo = policyNo;
    }

    public UUID getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(UUID quoteId) {
        this.quoteId = quoteId;
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

    public UUID getTpaTenantId() {
        return tpaTenantId;
    }

    public void setTpaTenantId(UUID tpaTenantId) {
        this.tpaTenantId = tpaTenantId;
    }

    public UUID getParentPolicyId() {
        return parentPolicyId;
    }

    public void setParentPolicyId(UUID parentPolicyId) {
        this.parentPolicyId = parentPolicyId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getGracePeriodDays() {
        return gracePeriodDays;
    }

    public void setGracePeriodDays(int gracePeriodDays) {
        this.gracePeriodDays = gracePeriodDays;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentFrequency() {
        return paymentFrequency;
    }

    public void setPaymentFrequency(String paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
    }

    public BigDecimal getTotalSumInsured() {
        return totalSumInsured;
    }

    public void setTotalSumInsured(BigDecimal totalSumInsured) {
        this.totalSumInsured = totalSumInsured;
    }

    public BigDecimal getNetAnnualPremium() {
        return netAnnualPremium;
    }

    public void setNetAnnualPremium(BigDecimal netAnnualPremium) {
        this.netAnnualPremium = netAnnualPremium;
    }

    public BigDecimal getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(BigDecimal gstAmount) {
        this.gstAmount = gstAmount;
    }

    public BigDecimal getTotalAnnualPremium() {
        return totalAnnualPremium;
    }

    public void setTotalAnnualPremium(BigDecimal totalAnnualPremium) {
        this.totalAnnualPremium = totalAnnualPremium;
    }
}
