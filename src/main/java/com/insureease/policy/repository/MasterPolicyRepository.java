package com.insureease.policy.repository;

import com.insureease.policy.entity.MasterPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MasterPolicyRepository extends JpaRepository<MasterPolicy, UUID> {
    List<MasterPolicy> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
    Optional<MasterPolicy> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<MasterPolicy> findByTenantIdAndQuoteId(UUID tenantId, UUID quoteId);
    List<MasterPolicy> findByTenantIdAndStatusAndEndDateLessThanEqual(UUID tenantId, String status, LocalDate date);
}
