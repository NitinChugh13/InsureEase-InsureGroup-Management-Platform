package com.insureease.claims.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insureease.common.crypto.EncryptedStringConverter;
import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "claim")
public class Claim extends BaseTenantEntity {

    @Column(name = "claim_no", nullable = false)
    private String claimNo;

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "dependent_id")
    private UUID dependentId;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "claim_category", nullable = false)
    private String claimCategory; // CASHLESS, REIMBURSEMENT, DEATH_BENEFIT, DISABILITY_BENEFIT

    @Column(name = "benefit_code", nullable = false)
    private String benefitCode; // HOSPITALISATION, MATERNITY, DEATH, PTD, PPD, TTD

    @Column(name = "disability_pct", precision = 5, scale = 2)
    private BigDecimal disabilityPct;

    @Column(name = "disablement_weeks")
    private Integer disablementWeeks;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "discharge_date")
    private LocalDate dischargeDate;

    @Column(name = "is_pre_existing_disease", nullable = false)
    private boolean preExistingDisease = false;

    @JsonIgnore
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "diagnosis_encrypted", nullable = false)
    private String diagnosis;

    @Column(name = "claimed_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal claimedAmount;

    @Column(name = "non_payable_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal nonPayableAmount = BigDecimal.ZERO;

    @Column(name = "sublimit_disallowed", nullable = false, precision = 19, scale = 2)
    private BigDecimal sublimitDisallowed = BigDecimal.ZERO;

    @Column(name = "deductible_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal deductibleAmount = BigDecimal.ZERO;

    @Column(name = "copay_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal copayAmount = BigDecimal.ZERO;

    @Column(name = "approved_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal approvedAmount = BigDecimal.ZERO;

    @Column(name = "status", nullable = false)
    private String status = "SUBMITTED";
    // SUBMITTED, ON_HOLD_PREMIUM_PENDING, DOCS_PENDING, UNDER_REVIEW, QUERY_RAISED, PENDING_SENIOR_APPROVAL, APPROVED, PARTIALLY_APPROVED, REJECTED, SETTLED, CLOSED

    @Column(name = "eligibility_notes", columnDefinition = "TEXT")
    private String eligibilityNotes;

    @Column(name = "requires_senior_approval", nullable = false)
    private boolean requiresSeniorApproval = false;

    @Column(name = "adjudicator_id")
    private UUID adjudicatorId;

    @Column(name = "senior_approver_id")
    private UUID seniorApproverId;

    @Column(name = "reason_code")
    private String reasonCode;

    @Column(name = "decision_remarks", columnDefinition = "TEXT")
    private String decisionRemarks;

    @Column(name = "sla_due_at", nullable = false)
    private Instant slaDueAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Transient
    public String getMaskedDiagnosis() {
        if (diagnosis == null || diagnosis.length() <= 3) {
            return "***";
        }
        return diagnosis.substring(0, 3) + "*** (Protected Health Info)";
    }

    public String getClaimNo() {
        return claimNo;
    }

    public void setClaimNo(String claimNo) {
        this.claimNo = claimNo;
    }

    public UUID getPolicyId() {
        return policyId;
    }

    public void setPolicyId(UUID policyId) {
        this.policyId = policyId;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public void setMemberId(UUID memberId) {
        this.memberId = memberId;
    }

    public UUID getDependentId() {
        return dependentId;
    }

    public void setDependentId(UUID dependentId) {
        this.dependentId = dependentId;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(UUID hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getClaimCategory() {
        return claimCategory;
    }

    public void setClaimCategory(String claimCategory) {
        this.claimCategory = claimCategory;
    }

    public String getBenefitCode() {
        return benefitCode;
    }

    public void setBenefitCode(String benefitCode) {
        this.benefitCode = benefitCode;
    }

    public BigDecimal getDisabilityPct() {
        return disabilityPct;
    }

    public void setDisabilityPct(BigDecimal disabilityPct) {
        this.disabilityPct = disabilityPct;
    }

    public Integer getDisablementWeeks() {
        return disablementWeeks;
    }

    public void setDisablementWeeks(Integer disablementWeeks) {
        this.disablementWeeks = disablementWeeks;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public LocalDate getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDate admissionDate) {
        this.admissionDate = admissionDate;
    }

    public LocalDate getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(LocalDate dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public boolean isPreExistingDisease() {
        return preExistingDisease;
    }

    public void setPreExistingDisease(boolean preExistingDisease) {
        this.preExistingDisease = preExistingDisease;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public BigDecimal getClaimedAmount() {
        return claimedAmount;
    }

    public void setClaimedAmount(BigDecimal claimedAmount) {
        this.claimedAmount = claimedAmount;
    }

    public BigDecimal getNonPayableAmount() {
        return nonPayableAmount;
    }

    public void setNonPayableAmount(BigDecimal nonPayableAmount) {
        this.nonPayableAmount = nonPayableAmount;
    }

    public BigDecimal getSublimitDisallowed() {
        return sublimitDisallowed;
    }

    public void setSublimitDisallowed(BigDecimal sublimitDisallowed) {
        this.sublimitDisallowed = sublimitDisallowed;
    }

    public BigDecimal getDeductibleAmount() {
        return deductibleAmount;
    }

    public void setDeductibleAmount(BigDecimal deductibleAmount) {
        this.deductibleAmount = deductibleAmount;
    }

    public BigDecimal getCopayAmount() {
        return copayAmount;
    }

    public void setCopayAmount(BigDecimal copayAmount) {
        this.copayAmount = copayAmount;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEligibilityNotes() {
        return eligibilityNotes;
    }

    public void setEligibilityNotes(String eligibilityNotes) {
        this.eligibilityNotes = eligibilityNotes;
    }

    public boolean isRequiresSeniorApproval() {
        return requiresSeniorApproval;
    }

    public void setRequiresSeniorApproval(boolean requiresSeniorApproval) {
        this.requiresSeniorApproval = requiresSeniorApproval;
    }

    public UUID getAdjudicatorId() {
        return adjudicatorId;
    }

    public void setAdjudicatorId(UUID adjudicatorId) {
        this.adjudicatorId = adjudicatorId;
    }

    public UUID getSeniorApproverId() {
        return seniorApproverId;
    }

    public void setSeniorApproverId(UUID seniorApproverId) {
        this.seniorApproverId = seniorApproverId;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getDecisionRemarks() {
        return decisionRemarks;
    }

    public void setDecisionRemarks(String decisionRemarks) {
        this.decisionRemarks = decisionRemarks;
    }

    public Instant getSlaDueAt() {
        return slaDueAt;
    }

    public void setSlaDueAt(Instant slaDueAt) {
        this.slaDueAt = slaDueAt;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }
}
