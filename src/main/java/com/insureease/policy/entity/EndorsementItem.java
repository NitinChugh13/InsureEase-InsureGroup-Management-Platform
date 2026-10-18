package com.insureease.policy.entity;

import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "endorsement_item")
public class EndorsementItem extends BaseTenantEntity {

    @Column(name = "endorsement_id", nullable = false)
    private UUID endorsementId;

    @Column(name = "action", nullable = false)
    private String action; // ADD_MEMBER, DELETE_MEMBER, UPDATE_SI, CORRECT_DATA

    @Column(name = "employee_code", nullable = false)
    private String employeeCode;

    @Column(name = "member_id")
    private UUID memberId;

    @Column(name = "name")
    private String name;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "gender")
    private String gender;

    @Column(name = "relation")
    private String relation = "SELF";

    @Column(name = "occupation_class")
    private String occupationClass = "CLASS_I";

    @Column(name = "annual_ctc", precision = 19, scale = 2)
    private BigDecimal annualCtc;

    @Column(name = "new_sum_insured", precision = 19, scale = 2)
    private BigDecimal newSumInsured;

    @Column(name = "nominee_name")
    private String nomineeName;

    @Column(name = "nominee_relation")
    private String nomineeRelation;

    @Column(name = "nominee_dob")
    private LocalDate nomineeDob;

    @Column(name = "nominee_share_pct", precision = 5, scale = 2)
    private BigDecimal nomineeSharePct;

    @Column(name = "days_remaining", nullable = false)
    private int daysRemaining = 0;

    @Column(name = "annual_premium_base", nullable = false, precision = 19, scale = 2)
    private BigDecimal annualPremiumBase = BigDecimal.ZERO;

    @Column(name = "pro_rata_net_delta", nullable = false, precision = 19, scale = 2)
    private BigDecimal proRataNetDelta = BigDecimal.ZERO;

    @Column(name = "prior_claim_exists", nullable = false)
    private boolean priorClaimExists = false;

    @Column(name = "status", nullable = false)
    private String status = "VALIDATED";

    @Column(name = "validation_message", columnDefinition = "TEXT")
    private String validationMessage;

    public UUID getEndorsementId() {
        return endorsementId;
    }

    public void setEndorsementId(UUID endorsementId) {
        this.endorsementId = endorsementId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public void setMemberId(UUID memberId) {
        this.memberId = memberId;
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

    public BigDecimal getNewSumInsured() {
        return newSumInsured;
    }

    public void setNewSumInsured(BigDecimal newSumInsured) {
        this.newSumInsured = newSumInsured;
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

    public int getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(int daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public BigDecimal getAnnualPremiumBase() {
        return annualPremiumBase;
    }

    public void setAnnualPremiumBase(BigDecimal annualPremiumBase) {
        this.annualPremiumBase = annualPremiumBase;
    }

    public BigDecimal getProRataNetDelta() {
        return proRataNetDelta;
    }

    public void setProRataNetDelta(BigDecimal proRataNetDelta) {
        this.proRataNetDelta = proRataNetDelta;
    }

    public boolean isPriorClaimExists() {
        return priorClaimExists;
    }

    public void setPriorClaimExists(boolean priorClaimExists) {
        this.priorClaimExists = priorClaimExists;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }
}
