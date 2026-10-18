package com.insureease.billing.repository;

import com.insureease.billing.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    List<LedgerEntry> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
    List<LedgerEntry> findByTenantIdAndPolicyIdOrderByCreatedAtDesc(UUID tenantId, UUID policyId);
}
