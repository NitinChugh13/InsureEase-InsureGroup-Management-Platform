package com.insureease.billing.repository;

import com.insureease.billing.entity.CdTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CdTransactionRepository extends JpaRepository<CdTransaction, UUID> {
    List<CdTransaction> findByTenantIdAndCdAccountIdOrderByCreatedAtDesc(UUID tenantId, UUID cdAccountId);
}
