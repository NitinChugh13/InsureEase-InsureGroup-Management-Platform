package com.insureease.product.entity;

import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "rate_row")
public class RateRow extends BaseTenantEntity {

    @Column(name = "rate_table_id", nullable = false)
    private UUID rateTableId;

    @Column(name = "age_from", nullable = false)
    private int ageFrom;

    @Column(name = "age_to", nullable = false)
    private int ageTo;

    @Column(name = "gender", nullable = false)
    private String gender = "ANY"; // M, F, ANY

    @Column(name = "relation", nullable = false)
    private String relation = "ANY"; // SELF, SPOUSE, CHILD, PARENT, ANY

    @Column(name = "occupation_class", nullable = false)
    private String occupationClass = "ALL"; // ALL, CLASS_I, CLASS_II, CLASS_III

    @Column(name = "si_slab", precision = 19, scale = 2)
    private BigDecimal siSlab;

    @Column(name = "base_rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal baseRate;

    public UUID getRateTableId() {
        return rateTableId;
    }

    public void setRateTableId(UUID rateTableId) {
        this.rateTableId = rateTableId;
    }

    public int getAgeFrom() {
        return ageFrom;
    }

    public void setAgeFrom(int ageFrom) {
        this.ageFrom = ageFrom;
    }

    public int getAgeTo() {
        return ageTo;
    }

    public void setAgeTo(int ageTo) {
        this.ageTo = ageTo;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getOccupationClass() {
        return occupationClass;
    }

    public void setOccupationClass(String occupationClass) {
        this.occupationClass = occupationClass;
    }

    public BigDecimal getSiSlab() {
        return siSlab;
    }

    public void setSiSlab(BigDecimal siSlab) {
        this.siSlab = siSlab;
    }

    public BigDecimal getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(BigDecimal baseRate) {
        this.baseRate = baseRate;
    }
}
