package com.insureease.billing.repository;

import com.insureease.billing.entity.InvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, UUID> {
    List<InvoiceLine> findByTenantIdAndInvoiceId(UUID tenantId, UUID invoiceId);
}
