package com.insureease.claims.repository;

import com.insureease.claims.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SettlementRepository extends JpaRepository<Settlement, UUID> {
    List<Settlement> findByTenantIdAndClaimId(UUID tenantId, UUID claimId);
}
