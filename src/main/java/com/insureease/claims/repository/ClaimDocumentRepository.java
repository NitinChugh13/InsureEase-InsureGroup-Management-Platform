package com.insureease.claims.repository;

import com.insureease.claims.entity.ClaimDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, UUID> {
    List<ClaimDocument> findByTenantIdAndClaimId(UUID tenantId, UUID claimId);
}
