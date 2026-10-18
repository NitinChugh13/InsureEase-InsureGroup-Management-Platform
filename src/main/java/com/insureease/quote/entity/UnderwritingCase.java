package com.insureease.quote.entity;

import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "underwriting_case")
public class UnderwritingCase extends BaseTenantEntity {

    @Column(name = "quote_id", nullable = false)
    private UUID quoteId;

    @Column(name = "status", nullable = false)
    private String status = "PENDING_REVIEW"; // AUTO_APPROVED, PENDING_REVIEW, PENDING_SENIOR_APPROVAL, APPROVED, COUNTER_OFFERED, REJECTED

    @Column(name = "triggered_rules", columnDefinition = "TEXT")
    private String triggeredRules;

    @Column(name = "risk_score", nullable = false)
    private int riskScore = 0;

    @Column(name = "requires_senior_approval", nullable = false)
    private boolean requiresSeniorApproval = false;

    @Column(name = "decision")
    private String decision;

    @Column(name = "approved_loading_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal approvedLoadingPct = BigDecimal.ZERO;

    @Column(name = "approved_discount_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal approvedDiscountPct = BigDecimal.ZERO;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "underwriter_id")
    private UUID underwriterId;

    @Column(name = "senior_underwriter_id")
    private UUID seniorUnderwriterId;

    @Column(name = "decided_at")
    private Instant decidedAt;

    public UUID getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(UUID quoteId) {
        this.quoteId = quoteId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTriggeredRules() {
        return triggeredRules;
    }

    public void setTriggeredRules(String triggeredRules) {
        this.triggeredRules = triggeredRules;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public boolean isRequiresSeniorApproval() {
        return requiresSeniorApproval;
    }

    public void setRequiresSeniorApproval(boolean requiresSeniorApproval) {
        this.requiresSeniorApproval = requiresSeniorApproval;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public BigDecimal getApprovedLoadingPct() {
        return approvedLoadingPct;
    }

    public void setApprovedLoadingPct(BigDecimal approvedLoadingPct) {
        this.approvedLoadingPct = approvedLoadingPct;
    }

    public BigDecimal getApprovedDiscountPct() {
        return approvedDiscountPct;
    }

    public void setApprovedDiscountPct(BigDecimal approvedDiscountPct) {
        this.approvedDiscountPct = approvedDiscountPct;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public UUID getUnderwriterId() {
        return underwriterId;
    }

    public void setUnderwriterId(UUID underwriterId) {
        this.underwriterId = underwriterId;
    }

    public UUID getSeniorUnderwriterId() {
        return seniorUnderwriterId;
    }

    public void setSeniorUnderwriterId(UUID seniorUnderwriterId) {
        this.seniorUnderwriterId = seniorUnderwriterId;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }
}
