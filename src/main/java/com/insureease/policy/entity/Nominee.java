package com.insureease.policy.entity;

import com.insureease.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "nominee")
public class Nominee extends BaseTenantEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "relation", nullable = false)
    private String relation;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "share_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal sharePct = new BigDecimal("100.00");

    @Column(name = "appointee_name")
    private String appointeeName;

    @Column(name = "appointee_relation")
    private String appointeeRelation;

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

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public BigDecimal getSharePct() {
        return sharePct;
    }

    public void setSharePct(BigDecimal sharePct) {
        this.sharePct = sharePct;
    }

    public String getAppointeeName() {
        return appointeeName;
    }

    public void setAppointeeName(String appointeeName) {
        this.appointeeName = appointeeName;
    }

    public String getAppointeeRelation() {
        return appointeeRelation;
    }

    public void setAppointeeRelation(String appointeeRelation) {
        this.appointeeRelation = appointeeRelation;
    }
}
