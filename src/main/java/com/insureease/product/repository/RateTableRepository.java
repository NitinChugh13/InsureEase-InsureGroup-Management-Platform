package com.insureease.product.repository;

import com.insureease.product.entity.RateTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RateTableRepository extends JpaRepository<RateTable, UUID> {
    List<RateTable> findByTenantIdAndPlanIdOrderByVersionNoDesc(UUID tenantId, UUID planId);
    Optional<RateTable> findFirstByTenantIdAndPlanIdAndStatusOrderByVersionNoDesc(UUID tenantId, UUID planId, String status);
}
