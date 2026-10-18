package com.insureease.product.repository;

import com.insureease.product.entity.Benefit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BenefitRepository extends JpaRepository<Benefit, UUID> {
    List<Benefit> findByTenantIdAndPlanId(UUID tenantId, UUID planId);
}
