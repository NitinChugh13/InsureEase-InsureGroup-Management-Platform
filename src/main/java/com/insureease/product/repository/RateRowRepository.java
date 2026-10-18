package com.insureease.product.repository;

import com.insureease.product.entity.RateRow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RateRowRepository extends JpaRepository<RateRow, UUID> {
    List<RateRow> findByTenantIdAndRateTableIdOrderByAgeFromAsc(UUID tenantId, UUID rateTableId);
}
