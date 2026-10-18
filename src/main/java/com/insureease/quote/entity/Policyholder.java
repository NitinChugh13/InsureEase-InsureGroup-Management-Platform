package com.insureease.quote.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insureease.common.crypto.EncryptedStringConverter;
import com.insureease.common.crypto.PiiCryptoService;
import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "policyholder")
public class Policyholder extends BaseTenantEntity {

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @Column(name = "org_type", nullable = false)
    private String orgType = "CORPORATE"; // CORPORATE, SME, ASSOCIATION, BANK

    @Column(name = "industry_code", nullable = false)
    private String industryCode = "IT_SERVICES";

    @Column(name = "gstin", nullable = false)
    private String gstin;

    @JsonIgnore
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "pan_encrypted", nullable = false)
    private String pan;

    @Column(name = "state_code", nullable = false)
    private String stateCode = "27"; // e.g., 27 = Maharashtra, 07 = Delhi, 29 = Karnataka

    @Column(name = "address_line")
    private String addressLine;

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @JsonIgnore
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "contact_mobile_encrypted")
    private String contactMobile;

    @Column(name = "kyc_status", nullable = false)
    private String kycStatus = "VERIFIED";

    @Transient
    public String getMaskedPan() {
        return PiiCryptoService.maskPan(this.pan);
    }

    @Transient
    public String getMaskedContactMobile() {
        return PiiCryptoService.maskMobile(this.contactMobile);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public String getIndustryCode() {
        return industryCode;
    }

    public void setIndustryCode(String industryCode) {
        this.industryCode = industryCode;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactMobile() {
        return contactMobile;
    }

    public void setContactMobile(String contactMobile) {
        this.contactMobile = contactMobile;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }
}
