package com.insureease.product.entity;

import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "rate_table")
public class RateTable extends BaseTenantEntity {

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "version_no", nullable = false)
    private int versionNo = 1;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @Column(name = "loading_config_json", columnDefinition = "TEXT")
    private String loadingConfigJson;

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(int versionNo) {
        this.versionNo = versionNo;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLoadingConfigJson() {
        return loadingConfigJson;
    }

    public void setLoadingConfigJson(String loadingConfigJson) {
        this.loadingConfigJson = loadingConfigJson;
    }
}
