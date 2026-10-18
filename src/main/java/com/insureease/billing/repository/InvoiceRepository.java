package com.insureease.billing.repository;

import com.insureease.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    List<Invoice> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
    List<Invoice> findByTenantIdAndPolicyIdOrderByCreatedAtDesc(UUID tenantId, UUID policyId);
    Optional<Invoice> findByIdAndTenantId(UUID id, UUID tenantId);
}
