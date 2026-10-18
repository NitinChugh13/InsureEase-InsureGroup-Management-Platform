package com.insureease.audit.repository;

import com.insureease.audit.entity.ConsentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, UUID> {
    List<ConsentRecord> findByTenantIdAndMemberIdOrderByTsDesc(UUID tenantId, UUID memberId);
}
