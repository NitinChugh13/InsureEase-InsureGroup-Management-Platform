package com.insureease.product.entity;

import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "benefit")
public class Benefit extends BaseTenantEntity {

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "benefit_type", nullable = false)
    private String benefitType; // ROOM_RENT, ICU, MATERNITY, HOSPITALISATION, DEATH, PTD, PPD, TTD

    @Column(name = "limit_type", nullable = false)
    private String limitType; // AMOUNT, PCT_OF_SI, WEEKLY_AMOUNT

    @Column(name = "limit_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal limitValue;

    @Column(name = "waiting_days", nullable = false)
    private int waitingDays = 0;

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
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

    public String getBenefitType() {
        return benefitType;
    }

    public void setBenefitType(String benefitType) {
        this.benefitType = benefitType;
    }

    public String getLimitType() {
        return limitType;
    }

    public void setLimitType(String limitType) {
        this.limitType = limitType;
    }

    public BigDecimal getLimitValue() {
        return limitValue;
    }

    public void setLimitValue(BigDecimal limitValue) {
        this.limitValue = limitValue;
    }

    public int getWaitingDays() {
        return waitingDays;
    }

    public void setWaitingDays(int waitingDays) {
        this.waitingDays = waitingDays;
    }
}
