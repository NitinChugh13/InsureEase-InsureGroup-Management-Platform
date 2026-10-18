package com.insureease.quote.repository;

import com.insureease.quote.entity.QuoteVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuoteVersionRepository extends JpaRepository<QuoteVersion, UUID> {
    List<QuoteVersion> findByTenantIdAndQuoteIdOrderByVersionNoDesc(UUID tenantId, UUID quoteId);
}
