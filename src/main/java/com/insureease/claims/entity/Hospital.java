package com.insureease.claims.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insureease.common.crypto.EncryptedStringConverter;
import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "hospital")
public class Hospital extends BaseTenantEntity {

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "network_flag", nullable = false)
    private boolean networkFlag = true;

    @JsonIgnore
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "bank_account_encrypted")
    private String bankAccount;

    @Column(name = "ifsc_code")
    private String ifscCode;

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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isNetworkFlag() {
        return networkFlag;
    }

    public void setNetworkFlag(boolean networkFlag) {
        this.networkFlag = networkFlag;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }
}
