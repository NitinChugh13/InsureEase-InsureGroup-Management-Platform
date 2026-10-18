package com.insureease.audit.repository;

import com.insureease.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByTenantIdOrderByTsDesc(UUID tenantId);
    List<AuditLog> findByTenantIdAndEntityNameAndEntityIdOrderByTsDesc(UUID tenantId, String entityName, String entityId);
}
