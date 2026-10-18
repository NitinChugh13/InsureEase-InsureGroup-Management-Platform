package com.insureease.notification.service;

import com.insureease.common.tenant.TenantContext;
import com.insureease.notification.entity.NotificationLog;
import com.insureease.notification.repository.NotificationLogRepository;
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;

    public NotificationService(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    @Transactional
    public NotificationLog sendNotification(UUID tenantId,
                                            String channel,
                                            String templateCode,
                                            String recipient,
                                            String subject,
                                            String body,
                                            String refType,
                                            UUID refId) {
        UUID resolvedTenant = tenantId != null ? tenantId : TenantContext.getTenantId();
        if (resolvedTenant == null) {
            return null;
        }
        NotificationLog log = new NotificationLog();
        log.setTenantId(resolvedTenant);
        log.setChannel(channel);
        log.setTemplateCode(templateCode);
        log.setRecipient(recipient != null ? recipient : "notifications@insureease.com");
        log.setSubject(subject);
        log.setBody(body);
        log.setStatus("SENT");
        log.setRefType(refType);
        log.setRefId(refId);
        log.setCreatedAt(Instant.now());
        return notificationLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<NotificationLog> getTenantNotifications() {
        return notificationLogRepository.findByTenantIdOrderByCreatedAtDesc(TenantContext.requireTenantId());
    }

    public byte[] generateDocumentPdf(String title, String subtitle, Map<String, String> keyValueSummary) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("InsureEase – InsureGroup Management Platform",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13)));
            if (subtitle != null) {
                document.add(new Paragraph(subtitle, FontFactory.getFont(FontFactory.HELVETICA, 10)));
            }
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100f);
            for (Map.Entry<String, String> entry : keyValueSummary.entrySet()) {
                table.addCell(entry.getKey());
                table.addCell(entry.getValue() != null ? entry.getValue() : "-");
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate PDF document", e);
        }
    }
}
