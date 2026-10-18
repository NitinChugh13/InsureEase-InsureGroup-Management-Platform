package com.insureease.product.repository;

import com.insureease.product.entity.UnderwritingRuleDef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UnderwritingRuleDefRepository extends JpaRepository<UnderwritingRuleDef, UUID> {
    List<UnderwritingRuleDef> findByTenantIdAndPlanIdAndActiveTrue(UUID tenantId, UUID planId);
}
