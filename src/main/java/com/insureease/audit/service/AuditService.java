package com.insureease.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insureease.audit.entity.AuditLog;
import com.insureease.audit.entity.ConsentRecord;
import com.insureease.audit.repository.AuditLogRepository;
import com.insureease.audit.repository.ConsentRecordRepository;
import com.insureease.common.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ConsentRecordRepository consentRecordRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository,
                        ConsentRecordRepository consentRecordRepository,
                        ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.consentRecordRepository = consentRecordRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AuditLog logAction(UUID tenantId,
                              String entityName,
                              Object entityId,
                              String action,
                              Object oldValue,
                              Object newValue) {
        UUID resolvedTenant = tenantId != null ? tenantId : TenantContext.getTenantId();
        if (resolvedTenant == null) {
            return null;
        }
        AuditLog entry = new AuditLog();
        entry.setTenantId(resolvedTenant);
        entry.setEntityName(entityName);
        entry.setEntityId(String.valueOf(entityId));
        entry.setAction(action);
        entry.setOldValue(toJsonSafe(oldValue));
        entry.setNewValue(toJsonSafe(newValue));
        entry.setActorId(TenantContext.getUserId());
        entry.setActorEmail(TenantContext.getUserEmail());
        entry.setCorrelationId(TenantContext.getCorrelationId());
        entry.setIpAddress("127.0.0.1");
        entry.setTs(Instant.now());
        return auditLogRepository.save(entry);
    }

    @Transactional
    public AuditLog logEvent(String entityName, Object entityId, String action, Map<String, Object> details) {
        return logAction(TenantContext.getTenantId(), entityName, entityId, action, null, details);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getTenantAuditLogs(String entityName, String entityId) {
        UUID tenantId = TenantContext.requireTenantId();
        if (entityName != null && !entityName.isBlank() && entityId != null && !entityId.isBlank()) {
            return auditLogRepository.findByTenantIdAndEntityNameAndEntityIdOrderByTsDesc(tenantId, entityName, entityId);
        }
        return auditLogRepository.findByTenantIdOrderByTsDesc(tenantId);
    }

    @Transactional
    public ConsentRecord recordConsent(UUID memberId, String purpose, boolean granted, String ipAddress) {
        UUID tenantId = TenantContext.requireTenantId();
        ConsentRecord record = new ConsentRecord();
        record.setTenantId(tenantId);
        record.setMemberId(memberId);
        record.setPurpose(purpose);
        record.setGranted(granted);
        record.setIpAddress(ipAddress != null ? ipAddress : "127.0.0.1");
        record.setTs(Instant.now());
        if (!granted) {
            record.setRevokedAt(Instant.now());
        }
        ConsentRecord saved = consentRecordRepository.save(record);
        logEvent("ConsentRecord", saved.getId(), granted ? "CONSENT_GRANTED" : "CONSENT_REVOKED",
                Map.of("memberId", memberId.toString(), "purpose", purpose));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ConsentRecord> getMemberConsents(UUID memberId) {
        return consentRecordRepository.findByTenantIdAndMemberIdOrderByTsDesc(TenantContext.requireTenantId(), memberId);
    }

    private String toJsonSafe(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String s) {
            return s;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
