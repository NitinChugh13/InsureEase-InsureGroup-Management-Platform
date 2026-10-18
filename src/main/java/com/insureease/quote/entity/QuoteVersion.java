package com.insureease.quote.entity;

import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "quote_version")
public class QuoteVersion extends BaseTenantEntity {

    @Column(name = "quote_id", nullable = false)
    private UUID quoteId;

    @Column(name = "version_no", nullable = false)
    private int versionNo;

    @Column(name = "base_premium", nullable = false, precision = 19, scale = 2)
    private BigDecimal basePremium;

    @Column(name = "loading_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal loadingPct;

    @Column(name = "discount_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPct;

    @Column(name = "net_premium", nullable = false, precision = 19, scale = 2)
    private BigDecimal netPremium;

    @Column(name = "gst_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal gstAmount;

    @Column(name = "total_premium", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPremium;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "actor_role")
    private String actorRole;

    public UUID getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(UUID quoteId) {
        this.quoteId = quoteId;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(int versionNo) {
        this.versionNo = versionNo;
    }

    public BigDecimal getBasePremium() {
        return basePremium;
    }

    public void setBasePremium(BigDecimal basePremium) {
        this.basePremium = basePremium;
    }

    public BigDecimal getLoadingPct() {
        return loadingPct;
    }

    public void setLoadingPct(BigDecimal loadingPct) {
        this.loadingPct = loadingPct;
    }

    public BigDecimal getDiscountPct() {
        return discountPct;
    }

    public void setDiscountPct(BigDecimal discountPct) {
        this.discountPct = discountPct;
    }

    public BigDecimal getNetPremium() {
        return netPremium;
    }

    public void setNetPremium(BigDecimal netPremium) {
        this.netPremium = netPremium;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }
}
