package com.insureease.renewal.repository;

import com.insureease.renewal.entity.RenewalCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RenewalCaseRepository extends JpaRepository<RenewalCase, UUID> {
    List<RenewalCase> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
    Optional<RenewalCase> findFirstByTenantIdAndPolicyIdOrderByCreatedAtDesc(UUID tenantId, UUID policyId);
    Optional<RenewalCase> findByIdAndTenantId(UUID id, UUID tenantId);
}
