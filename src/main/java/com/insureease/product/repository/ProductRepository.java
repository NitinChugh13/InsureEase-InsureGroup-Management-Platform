package com.insureease.product.repository;

import com.insureease.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByTenantId(UUID tenantId);
    Optional<Product> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<Product> findByTenantIdAndCodeIgnoreCase(UUID tenantId, String code);
}
