package com.insureease.audit.controller;

import com.insureease.audit.entity.AuditLog;
import com.insureease.audit.entity.ConsentRecord;
import com.insureease.audit.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "10. Audit & DPDP Compliance", description = "Immutable audit logs and DPDP Act 2023 consent records")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Query immutable tenant audit trail (optional filter by entityName and entityId)")
    public List<AuditLog> getAuditLogs(@RequestParam(required = false) String entityName,
                                       @RequestParam(required = false) String entityId) {
        return auditService.getTenantAuditLogs(entityName, entityId);
    }

    public record ConsentRequest(
            @NotNull UUID memberId,
            @NotBlank String purpose,
            boolean granted
    ) {
    }

    @PostMapping("/dpdp/consents")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record DPDP Act 2023 member data consent or revocation")
    public ConsentRecord recordConsent(@Valid @RequestBody ConsentRequest request) {
        return auditService.recordConsent(request.memberId(), request.purpose(), request.granted(), "127.0.0.1");
    }

    @GetMapping("/dpdp/consents/{memberId}")
    @Operation(summary = "List consent records for a member")
    public List<ConsentRecord> getMemberConsents(@PathVariable UUID memberId) {
        return auditService.getMemberConsents(memberId);
    }
}
