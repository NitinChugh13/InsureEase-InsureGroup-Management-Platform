package com.insureease.quote.repository;

import com.insureease.quote.entity.UnderwritingCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnderwritingCaseRepository extends JpaRepository<UnderwritingCase, UUID> {
    Optional<UnderwritingCase> findFirstByTenantIdAndQuoteIdOrderByCreatedAtDesc(UUID tenantId, UUID quoteId);
    List<UnderwritingCase> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
