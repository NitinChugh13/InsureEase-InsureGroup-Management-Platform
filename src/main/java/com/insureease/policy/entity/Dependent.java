package com.insureease.policy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insureease.common.crypto.EncryptedStringConverter;
import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "dependent")
public class Dependent extends BaseTenantEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "dependent_no", nullable = false)
    private String dependentNo;

    @Column(name = "relation", nullable = false)
    private String relation; // SPOUSE, CHILD, PARENT

    @Column(name = "name", nullable = false)
    private String name;

    @JsonIgnore
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "dob_encrypted", nullable = false)
    private String dobEncrypted;

    @Column(name = "age", nullable = false)
    private int age;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "individual_si", precision = 19, scale = 2)
    private BigDecimal individualSi;

    @Column(name = "utilized_si", nullable = false, precision = 19, scale = 2)
    private BigDecimal utilizedSi = BigDecimal.ZERO;

    @Column(name = "annual_dependent_premium", nullable = false, precision = 19, scale = 2)
    private BigDecimal annualDependentPremium = BigDecimal.ZERO;

    @Column(name = "coverage_start_date", nullable = false)
    private LocalDate coverageStartDate;

    @Column(name = "coverage_end_date", nullable = false)
    private LocalDate coverageEndDate;

    @Column(name = "continuous_cover_since", nullable = false)
    private LocalDate continuousCoverSince;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @Transient
    public LocalDate getDob() {
        return dobEncrypted != null ? LocalDate.parse(dobEncrypted) : null;
    }

    public void setDob(LocalDate dob) {
        this.dobEncrypted = dob != null ? dob.toString() : null;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public void setMemberId(UUID memberId) {
        this.memberId = memberId;
    }

    public String getDependentNo() {
        return dependentNo;
    }

    public void setDependentNo(String dependentNo) {
        this.dependentNo = dependentNo;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDobEncrypted() {
        return dobEncrypted;
    }

    public void setDobEncrypted(String dobEncrypted) {
        this.dobEncrypted = dobEncrypted;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public BigDecimal getIndividualSi() {
        return individualSi;
    }

    public void setIndividualSi(BigDecimal individualSi) {
        this.individualSi = individualSi;
    }

    public BigDecimal getUtilizedSi() {
        return utilizedSi;
    }

    public void setUtilizedSi(BigDecimal utilizedSi) {
        this.utilizedSi = utilizedSi;
    }

    public BigDecimal getAnnualDependentPremium() {
        return annualDependentPremium;
    }

    public void setAnnualDependentPremium(BigDecimal annualDependentPremium) {
        this.annualDependentPremium = annualDependentPremium;
    }

    public LocalDate getCoverageStartDate() {
        return coverageStartDate;
    }

    public void setCoverageStartDate(LocalDate coverageStartDate) {
        this.coverageStartDate = coverageStartDate;
    }

    public LocalDate getCoverageEndDate() {
        return coverageEndDate;
    }

    public void setCoverageEndDate(LocalDate coverageEndDate) {
        this.coverageEndDate = coverageEndDate;
    }

    public LocalDate getContinuousCoverSince() {
        return continuousCoverSince;
    }

    public void setContinuousCoverSince(LocalDate continuousCoverSince) {
        this.continuousCoverSince = continuousCoverSince;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
