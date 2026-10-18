package com.insureease.quote.entity;

import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "quote_member")
public class QuoteMember extends BaseTenantEntity {

    @Column(name = "quote_id", nullable = false)
    private UUID quoteId;

    @Column(name = "row_no", nullable = false)
    private int rowNo;

    @Column(name = "employee_code", nullable = false)
    private String employeeCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "age", nullable = false)
    private int age;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "relation", nullable = false)
    private String relation; // SELF, SPOUSE, CHILD, PARENT

    @Column(name = "occupation_class", nullable = false)
    private String occupationClass = "CLASS_I";

    @Column(name = "annual_ctc", precision = 19, scale = 2)
    private BigDecimal annualCtc;

    @Column(name = "requested_si", nullable = false, precision = 19, scale = 2)
    private BigDecimal requestedSi = BigDecimal.ZERO;

    @Column(name = "approved_si", nullable = false, precision = 19, scale = 2)
    private BigDecimal approvedSi = BigDecimal.ZERO;

    @Column(name = "excess_fcl_si", nullable = false, precision = 19, scale = 2)
    private BigDecimal excessFclSi = BigDecimal.ZERO;

    @Column(name = "uw_status", nullable = false)
    private String uwStatus = "AUTO_APPROVED"; // AUTO_APPROVED, MEDICAL_REQUIRED, APPROVED, DECLINED

    @Column(name = "base_premium", nullable = false, precision = 19, scale = 2)
    private BigDecimal basePremium = BigDecimal.ZERO;

    @Column(name = "email")
    private String email;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "nominee_name")
    private String nomineeName;

    @Column(name = "nominee_relation")
    private String nomineeRelation;

    @Column(name = "nominee_dob")
    private LocalDate nomineeDob;

    @Column(name = "nominee_share_pct", precision = 5, scale = 2)
    private BigDecimal nomineeSharePct;

    @Column(name = "appointee_name")
    private String appointeeName;

    @Column(name = "valid", nullable = false)
    private boolean valid = true;

    @Column(name = "validation_errors", columnDefinition = "TEXT")
    private String validationErrors;

    public UUID getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(UUID quoteId) {
        this.quoteId = quoteId;
    }

    public int getRowNo() {
        return rowNo;
    }

    public void setRowNo(int rowNo) {
        this.rowNo = rowNo;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
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

    public BigDecimal getApprovedSi() {
        return approvedSi;
    }

    public void setApprovedSi(BigDecimal approvedSi) {
        this.approvedSi = approvedSi;
    }

    public BigDecimal getExcessFclSi() {
        return excessFclSi;
    }

    public void setExcessFclSi(BigDecimal excessFclSi) {
        this.excessFclSi = excessFclSi;
    }

    public String getUwStatus() {
        return uwStatus;
    }

    public void setUwStatus(String uwStatus) {
        this.uwStatus = uwStatus;
    }

    public BigDecimal getBasePremium() {
        return basePremium;
    }

    public void setBasePremium(BigDecimal basePremium) {
        this.basePremium = basePremium;
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

    public String getNomineeName() {
        return nomineeName;
    }

    public void setNomineeName(String nomineeName) {
        this.nomineeName = nomineeName;
    }

    public String getNomineeRelation() {
        return nomineeRelation;
    }

    public void setNomineeRelation(String nomineeRelation) {
        this.nomineeRelation = nomineeRelation;
    }

    public LocalDate getNomineeDob() {
        return nomineeDob;
    }

    public void setNomineeDob(LocalDate nomineeDob) {
        this.nomineeDob = nomineeDob;
    }

    public BigDecimal getNomineeSharePct() {
        return nomineeSharePct;
    }

    public void setNomineeSharePct(BigDecimal nomineeSharePct) {
        this.nomineeSharePct = nomineeSharePct;
    }

    public String getAppointeeName() {
        return appointeeName;
    }

    public void setAppointeeName(String appointeeName) {
        this.appointeeName = appointeeName;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(String validationErrors) {
        this.validationErrors = validationErrors;
    }
}
