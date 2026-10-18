package com.insureease.claims.entity;

import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "settlement")
public class Settlement extends BaseTenantEntity {

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "payee_type", nullable = false)
    private String payeeType; // HOSPITAL, MEMBER, NOMINEE

    @Column(name = "payee_name", nullable = false)
    private String payeeName;

    @Column(name = "share_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal sharePct = new BigDecimal("100.00");

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_mode", nullable = false)
    private String paymentMode = "NEFT";

    @Column(name = "utr_no", nullable = false)
    private String utrNo;

    @Column(name = "status", nullable = false)
    private String status = "SETTLED";

    @Column(name = "settled_on", nullable = false)
    private Instant settledOn;

    public UUID getClaimId() {
        return claimId;
    }

    public void setClaimId(UUID claimId) {
        this.claimId = claimId;
    }

    public String getPayeeType() {
        return payeeType;
    }

    public void setPayeeType(String payeeType) {
        this.payeeType = payeeType;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public BigDecimal getSharePct() {
        return sharePct;
    }

    public void setSharePct(BigDecimal sharePct) {
        this.sharePct = sharePct;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getUtrNo() {
        return utrNo;
    }

    public void setUtrNo(String utrNo) {
        this.utrNo = utrNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getSettledOn() {
        return settledOn;
    }

    public void setSettledOn(Instant settledOn) {
        this.settledOn = settledOn;
    }
}
