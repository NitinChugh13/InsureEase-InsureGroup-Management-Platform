package com.insureease.policy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insureease.common.crypto.EncryptedStringConverter;
import com.insureease.common.crypto.PiiCryptoService;
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
@Table(name = "member")
public class Member extends BaseTenantEntity {

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Column(name = "employee_code", nullable = false)
    private String employeeCode;

    @Column(name = "member_no", nullable = false)
    private String memberNo;

    @Column(name = "certificate_no", nullable = false)
    private String certificateNo;

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

    @Column(name = "email")
    private String email;

    @JsonIgnore
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "mobile_encrypted")
    private String mobile;

    @Column(name = "occupation_class", nullable = false)
    private String occupationClass = "CLASS_I";

    @Column(name = "annual_ctc", precision = 19, scale = 2)
    private BigDecimal annualCtc;

    @Column(name = "requested_si", nullable = false, precision = 19, scale = 2)
    private BigDecimal requestedSi;

    @Column(name = "sum_insured", nullable = false, precision = 19, scale = 2)
    private BigDecimal sumInsured;

    @Column(name = "utilized_si", nullable = false, precision = 19, scale = 2)
    private BigDecimal utilizedSi = BigDecimal.ZERO;

    @Column(name = "annual_member_premium", nullable = false, precision = 19, scale = 2)
    private BigDecimal annualMemberPremium = BigDecimal.ZERO;

    @Column(name = "uw_status", nullable = false)
    private String uwStatus = "AUTO_APPROVED";

    @Column(name = "coverage_start_date", nullable = false)
    private LocalDate coverageStartDate;

    @Column(name = "coverage_end_date", nullable = false)
    private LocalDate coverageEndDate;

    @Column(name = "continuous_cover_since", nullable = false)
    private LocalDate continuousCoverSince;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE"; // ACTIVE, DELETED, EXPIRED

    @Transient
    public LocalDate getDob() {
        return dobEncrypted != null ? LocalDate.parse(dobEncrypted) : null;
    }

    public void setDob(LocalDate dob) {
        this.dobEncrypted = dob != null ? dob.toString() : null;
    }

    @Transient
    public String getMaskedMobile() {
        return PiiCryptoService.maskMobile(this.mobile);
    }

    @Transient
    public BigDecimal getRemainingSi() {
        return sumInsured.subtract(utilizedSi).max(BigDecimal.ZERO);
    }

    public UUID getPolicyId() {
        return policyId;
    }

    public void setPolicyId(UUID policyId) {
        this.policyId = policyId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(String memberNo) {
        this.memberNo = memberNo;
    }

    public String getCertificateNo() {
        return certificateNo;
    }

    public void setCertificateNo(String certificateNo) {
        this.certificateNo = certificateNo;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getOccupationClass() {
        return occupationClass;
    }

    public void setOccupationClass(String occupationClass) {
        this.occupationClass = occupationClass;
    }

    public BigDecimal getAnnualCtc() {
        return annualCtc;
    }

    public void setAnnualCtc(BigDecimal annualCtc) {
        this.annualCtc = annualCtc;
    }

    public BigDecimal getRequestedSi() {
        return requestedSi;
    }

    public void setRequestedSi(BigDecimal requestedSi) {
        this.requestedSi = requestedSi;
    }

    public BigDecimal getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(BigDecimal sumInsured) {
        this.sumInsured = sumInsured;
    }

    public BigDecimal getUtilizedSi() {
        return utilizedSi;
    }

    public void setUtilizedSi(BigDecimal utilizedSi) {
        this.utilizedSi = utilizedSi;
    }

    public BigDecimal getAnnualMemberPremium() {
        return annualMemberPremium;
    }

    public void setAnnualMemberPremium(BigDecimal annualMemberPremium) {
        this.annualMemberPremium = annualMemberPremium;
    }

    public String getUwStatus() {
        return uwStatus;
    }

    public void setUwStatus(String uwStatus) {
        this.uwStatus = uwStatus;
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
