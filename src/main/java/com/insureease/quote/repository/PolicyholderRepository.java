package com.insureease.quote.repository;

import com.insureease.quote.entity.Policyholder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyholderRepository extends JpaRepository<Policyholder, UUID> {
    List<Policyholder> findByTenantId(UUID tenantId);
    Optional<Policyholder> findByIdAndTenantId(UUID id, UUID tenantId);
}
