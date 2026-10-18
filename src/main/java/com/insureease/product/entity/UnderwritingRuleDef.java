package com.insureease.product.entity;

import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "underwriting_rule_def")
public class UnderwritingRuleDef extends BaseTenantEntity {

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "rule_code", nullable = false)
    private String ruleCode;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "metric", nullable = false)
    private String metric; // MAX_MEMBER_AGE, MAX_MEMBER_SI, GROUP_SIZE, TOTAL_SI

    @Column(name = "operator", nullable = false)
    private String operator; // GT, GTE, LT, LTE

    @Column(name = "threshold_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal thresholdValue;

    @Column(name = "action", nullable = false)
    private String action; // REFER_UW, SENIOR_UW_REQUIRED, DECLINE

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public BigDecimal getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(BigDecimal thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
