package com.insureease.renewal.entity;

import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "renewal_case")
public class RenewalCase extends BaseTenantEntity {

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "days_to_expiry", nullable = false)
    private int daysToExpiry;

    @Column(name = "trigger_window", nullable = false)
    private String triggerWindow; // T_90, T_60, T_30, MANUAL

    @Column(name = "earned_premium", nullable = false, precision = 19, scale = 2)
    private BigDecimal earnedPremium;

    @Column(name = "paid_claims", nullable = false, precision = 19, scale = 2)
    private BigDecimal paidClaims;

    @Column(name = "reserved_claims", nullable = false, precision = 19, scale = 2)
    private BigDecimal reservedClaims;

    @Column(name = "incurred_claims", nullable = false, precision = 19, scale = 2)
    private BigDecimal incurredClaims;

    @Column(name = "claim_count", nullable = false)
    private int claimCount;

    @Column(name = "loss_ratio_pct", nullable = false, precision = 8, scale = 2)
    private BigDecimal lossRatioPct;

    @Column(name = "recommended_rate_change_pct", nullable = false, precision = 6, scale = 2)
    private BigDecimal recommendedRateChangePct;

    @Column(name = "renewal_quote_id")
    private UUID renewalQuoteId;

    @Column(name = "new_policy_id")
    private UUID newPolicyId;

    @Column(name = "status", nullable = false)
    private String status = "QUOTE_GENERATED"; // QUOTE_GENERATED, UW_REVIEW_REQUIRED, ACCEPTED, RENEWED, LAPSED

    public UUID getPolicyId() {
        return policyId;
    }

    public void setPolicyId(UUID policyId) {
        this.policyId = policyId;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getDaysToExpiry() {
        return daysToExpiry;
    }

    public void setDaysToExpiry(int daysToExpiry) {
        this.daysToExpiry = daysToExpiry;
    }

    public String getTriggerWindow() {
        return triggerWindow;
    }

    public void setTriggerWindow(String triggerWindow) {
        this.triggerWindow = triggerWindow;
    }

    public BigDecimal getEarnedPremium() {
        return earnedPremium;
    }

    public void setEarnedPremium(BigDecimal earnedPremium) {
        this.earnedPremium = earnedPremium;
    }

    public BigDecimal getPaidClaims() {
        return paidClaims;
    }

    public void setPaidClaims(BigDecimal paidClaims) {
        this.paidClaims = paidClaims;
    }

    public BigDecimal getReservedClaims() {
        return reservedClaims;
    }

    public void setReservedClaims(BigDecimal reservedClaims) {
        this.reservedClaims = reservedClaims;
    }

    public BigDecimal getIncurredClaims() {
        return incurredClaims;
    }

    public void setIncurredClaims(BigDecimal incurredClaims) {
        this.incurredClaims = incurredClaims;
    }

    public int getClaimCount() {
        return claimCount;
    }

    public void setClaimCount(int claimCount) {
        this.claimCount = claimCount;
    }

    public BigDecimal getLossRatioPct() {
        return lossRatioPct;
    }

    public void setLossRatioPct(BigDecimal lossRatioPct) {
        this.lossRatioPct = lossRatioPct;
    }

    public BigDecimal getRecommendedRateChangePct() {
        return recommendedRateChangePct;
    }

    public void setRecommendedRateChangePct(BigDecimal recommendedRateChangePct) {
        this.recommendedRateChangePct = recommendedRateChangePct;
    }

    public UUID getRenewalQuoteId() {
        return renewalQuoteId;
    }

    public void setRenewalQuoteId(UUID renewalQuoteId) {
        this.renewalQuoteId = renewalQuoteId;
    }

    public UUID getNewPolicyId() {
        return newPolicyId;
    }

    public void setNewPolicyId(UUID newPolicyId) {
        this.newPolicyId = newPolicyId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
