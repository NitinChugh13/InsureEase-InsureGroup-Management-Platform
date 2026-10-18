package com.insureease.policy.repository;

import com.insureease.policy.entity.Nominee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NomineeRepository extends JpaRepository<Nominee, UUID> {
    List<Nominee> findByTenantIdAndMemberId(UUID tenantId, UUID memberId);
}
