package com.insureease.billing.repository;

import com.insureease.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByTenantIdAndInvoiceIdOrderByCreatedAtDesc(UUID tenantId, UUID invoiceId);
    Optional<Payment> findByProviderIgnoreCaseAndGatewayOrderId(String provider, String gatewayOrderId);
    Optional<Payment> findByProviderIgnoreCaseAndGatewayPaymentId(String provider, String gatewayPaymentId);
}
