package com.insureease.policy.repository;

import com.insureease.policy.entity.EndorsementItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EndorsementItemRepository extends JpaRepository<EndorsementItem, UUID> {
    List<EndorsementItem> findByTenantIdAndEndorsementId(UUID tenantId, UUID endorsementId);
}
