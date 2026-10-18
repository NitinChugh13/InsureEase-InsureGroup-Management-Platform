package com.insureease.product.repository;

import com.insureease.product.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanRepository extends JpaRepository<Plan, UUID> {
    List<Plan> findByTenantId(UUID tenantId);
    List<Plan> findByTenantIdAndProductId(UUID tenantId, UUID productId);
    Optional<Plan> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<Plan> findByTenantIdAndCodeIgnoreCase(UUID tenantId, String code);
}
