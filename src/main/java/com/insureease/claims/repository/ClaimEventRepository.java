package com.insureease.claims.repository;

import com.insureease.claims.entity.ClaimEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClaimEventRepository extends JpaRepository<ClaimEvent, UUID> {
    List<ClaimEvent> findByTenantIdAndClaimIdOrderByCreatedAtAsc(UUID tenantId, UUID claimId);
}
