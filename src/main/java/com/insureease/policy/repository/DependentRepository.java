package com.insureease.policy.repository;

import com.insureease.policy.entity.Dependent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DependentRepository extends JpaRepository<Dependent, UUID> {
    List<Dependent> findByTenantIdAndMemberId(UUID tenantId, UUID memberId);
    Optional<Dependent> findByIdAndTenantId(UUID id, UUID tenantId);
}
