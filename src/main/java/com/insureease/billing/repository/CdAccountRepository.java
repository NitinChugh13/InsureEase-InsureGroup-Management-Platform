package com.insureease.billing.repository;

import com.insureease.billing.entity.CdAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CdAccountRepository extends JpaRepository<CdAccount, UUID> {
    Optional<CdAccount> findByTenantIdAndPolicyId(UUID tenantId, UUID policyId);
}
