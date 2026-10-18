package com.insureease.quote.repository;

import com.insureease.quote.entity.QuoteMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteMemberRepository extends JpaRepository<QuoteMember, UUID> {
    List<QuoteMember> findByTenantIdAndQuoteIdOrderByRowNoAsc(UUID tenantId, UUID quoteId);
    Optional<QuoteMember> findByIdAndTenantId(UUID id, UUID tenantId);
    void deleteByTenantIdAndQuoteId(UUID tenantId, UUID quoteId);
}
