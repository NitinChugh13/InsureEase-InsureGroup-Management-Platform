package com.insureease.policy.entity;

import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "endorsement")
public class Endorsement extends BaseTenantEntity {

    @Column(name = "endorsement_no", nullable = false)
    private String endorsementNo;

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Column(name = "endorsement_type", nullable = false)
    private String endorsementType; // ADD_MEMBER, DELETE_MEMBER, UPDATE_SI, CORRECT_DATA, BATCH

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "status", nullable = false)
    private String status = "SUBMITTED"; // SUBMITTED, APPROVED_APPLIED, APPROVED_PENDING_PAYMENT, REJECTED

    @Column(name = "net_premium_delta", nullable = false, precision = 19, scale = 2)
    private BigDecimal netPremiumDelta = BigDecimal.ZERO;

    @Column(name = "gst_delta", nullable = false, precision = 19, scale = 2)
    private BigDecimal gstDelta = BigDecimal.ZERO;

    @Column(name = "total_premium_delta", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPremiumDelta = BigDecimal.ZERO;

    @Column(name = "funding_mode", nullable = false)
    private String fundingMode = "CD_ACCOUNT"; // CD_ACCOUNT, INVOICE

    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    public String getEndorsementNo() {
        return endorsementNo;
    }

    public void setEndorsementNo(String endorsementNo) {
        this.endorsementNo = endorsementNo;
    }

    public UUID getPolicyId() {
        return policyId;
    }

    public void setPolicyId(UUID policyId) {
        this.policyId = policyId;
    }

    public String getEndorsementType() {
        return endorsementType;
    }

    public void setEndorsementType(String endorsementType) {
        this.endorsementType = endorsementType;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getNetPremiumDelta() {
        return netPremiumDelta;
    }

    public void setNetPremiumDelta(BigDecimal netPremiumDelta) {
        this.netPremiumDelta = netPremiumDelta;
    }

    public BigDecimal getGstDelta() {
        return gstDelta;
    }

    public void setGstDelta(BigDecimal gstDelta) {
        this.gstDelta = gstDelta;
    }

    public BigDecimal getTotalPremiumDelta() {
        return totalPremiumDelta;
    }

    public void setTotalPremiumDelta(BigDecimal totalPremiumDelta) {
        this.totalPremiumDelta = totalPremiumDelta;
    }

    public String getFundingMode() {
        return fundingMode;
    }

    public void setFundingMode(String fundingMode) {
        this.fundingMode = fundingMode;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(UUID approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
