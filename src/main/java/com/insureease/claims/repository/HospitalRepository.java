package com.insureease.claims.repository;

import com.insureease.claims.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HospitalRepository extends JpaRepository<Hospital, UUID> {
    List<Hospital> findByTenantId(UUID tenantId);
    Optional<Hospital> findByIdAndTenantId(UUID id, UUID tenantId);
}
