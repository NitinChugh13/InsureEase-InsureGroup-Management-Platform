package com.insureease.policy.repository;

import com.insureease.policy.entity.Endorsement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EndorsementRepository extends JpaRepository<Endorsement, UUID> {
    List<Endorsement> findByTenantIdAndPolicyIdOrderByCreatedAtDesc(UUID tenantId, UUID policyId);
    Optional<Endorsement> findByIdAndTenantId(UUID id, UUID tenantId);
}
