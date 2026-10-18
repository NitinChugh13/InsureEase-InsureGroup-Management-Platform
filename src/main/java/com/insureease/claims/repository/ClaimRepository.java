package com.insureease.claims.repository;

import com.insureease.claims.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {
    List<Claim> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
    List<Claim> findByTenantIdAndPolicyIdOrderByCreatedAtDesc(UUID tenantId, UUID policyId);
    List<Claim> findByTenantIdAndMemberIdOrderByCreatedAtDesc(UUID tenantId, UUID memberId);
    List<Claim> findByTenantIdAndPolicyIdAndStatus(UUID tenantId, UUID policyId, String status);
    Optional<Claim> findByIdAndTenantId(UUID id, UUID tenantId);
}
