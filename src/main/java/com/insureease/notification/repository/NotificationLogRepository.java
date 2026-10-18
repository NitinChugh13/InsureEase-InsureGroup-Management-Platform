package com.insureease.notification.repository;

import com.insureease.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {
    List<NotificationLog> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
