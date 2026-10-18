package com.insureease.reporting.controller;

import com.insureease.billing.entity.Invoice;
import com.insureease.billing.repository.InvoiceRepository;
import com.insureease.claims.entity.Claim;
import com.insureease.claims.repository.ClaimRepository;
import com.insureease.common.tenant.TenantContext;
import com.insureease.policy.entity.MasterPolicy;
import com.insureease.policy.repository.MasterPolicyRepository;
import com.insureease.renewal.entity.RenewalCase;
import com.insureease.renewal.repository.RenewalCaseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "09. Reports & Executive Dashboards (M9)", description = "Portfolio Loss Ratio (ICR), Claims TAT & IRDAI SLA compliance, Renewals pipeline, and Collections analytics")
public class ReportingController {

    private static final BigDecimal HUNDRED = new BigDecimal("100.00");

    private final MasterPolicyRepository masterPolicyRepository;
    private final ClaimRepository claimRepository;
    private final InvoiceRepository invoiceRepository;
    private final RenewalCaseRepository renewalCaseRepository;

    public ReportingController(MasterPolicyRepository masterPolicyRepository,
                               ClaimRepository claimRepository,
                               InvoiceRepository invoiceRepository,
                               RenewalCaseRepository renewalCaseRepository) {
        this.masterPolicyRepository = masterPolicyRepository;
        this.claimRepository = claimRepository;
        this.invoiceRepository = invoiceRepository;
        this.renewalCaseRepository = renewalCaseRepository;
    }

    @GetMapping("/loss-ratio")
    @Operation(summary = "Compute portfolio and per-policy Incurred Claims Ratio (ICR / Loss Ratio) report (FR-9.1)")
    public Map<String, Object> getLossRatioReport() {
        UUID tenantId = TenantContext.requireTenantId();
        List<MasterPolicy> policies = masterPolicyRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
        List<Claim> allClaims = claimRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);

        BigDecimal portfolioEarnedPremium = BigDecimal.ZERO;
        BigDecimal portfolioIncurredClaims = BigDecimal.ZERO;
        List<Map<String, Object>> policyRows = new ArrayList<>();

        for (MasterPolicy p : policies) {
            BigDecimal earned = p.getNetAnnualPremium();
            BigDecimal incurred = allClaims.stream()
                    .filter(c -> c.getPolicyId().equals(p.getId()))
                    .filter(c -> !"REJECTED".equalsIgnoreCase(c.getStatus()))
                    .map(Claim::getApprovedAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal icr = earned.compareTo(BigDecimal.ZERO) > 0
                    ? incurred.multiply(HUNDRED).divide(earned, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            portfolioEarnedPremium = portfolioEarnedPremium.add(earned);
            portfolioIncurredClaims = portfolioIncurredClaims.add(incurred);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("policyId", p.getId());
            row.put("policyNo", p.getPolicyNo());
            row.put("status", p.getStatus());
            row.put("earnedPremium", earned);
            row.put("incurredClaims", incurred);
            row.put("lossRatioPct", icr);
            policyRows.add(row);
        }

        BigDecimal portfolioIcr = portfolioEarnedPremium.compareTo(BigDecimal.ZERO) > 0
                ? portfolioIncurredClaims.multiply(HUNDRED).divide(portfolioEarnedPremium, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("policyCount", policies.size());
        summary.put("portfolioEarnedPremium", portfolioEarnedPremium);
        summary.put("portfolioIncurredClaims", portfolioIncurredClaims);
        summary.put("portfolioLossRatioPct", portfolioIcr);
        summary.put("policies", policyRows);
        return summary;
    }

    @GetMapping("/claims-tat")
    @Operation(summary = "Compute Claims Turnaround Time (TAT) and IRDAI 2024 SLA compliance metrics (FR-9.1)")
    public Map<String, Object> getClaimsTatReport() {
        UUID tenantId = TenantContext.requireTenantId();
        List<Claim> claims = claimRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);

        int settledCount = 0;
        int slaCompliantCount = 0;
        long totalMinutes = 0;

        for (Claim c : claims) {
            if (c.getDecidedAt() != null && c.getCreatedAt() != null) {
                settledCount++;
                long mins = Math.max(1, Duration.between(c.getCreatedAt(), c.getDecidedAt()).toMinutes());
                totalMinutes += mins;
                if (!c.getDecidedAt().isAfter(c.getSlaDueAt())) {
                    slaCompliantCount++;
                }
            }
        }

        double avgTatMinutes = settledCount > 0 ? (double) totalMinutes / settledCount : 0.0;
        double slaCompliancePct = settledCount > 0 ? (slaCompliantCount * 100.0) / settledCount : 100.0;

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalClaims", claims.size());
        report.put("decidedClaims", settledCount);
        report.put("averageDecisionTatMinutes", Math.round(avgTatMinutes * 100.0) / 100.0);
        report.put("irdaiSlaCompliancePct", Math.round(slaCompliancePct * 100.0) / 100.0);
        return report;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Executive summary across policies, collections, claims, and renewals pipeline (FR-9.1)")
    public Map<String, Object> getExecutiveDashboard() {
        UUID tenantId = TenantContext.requireTenantId();
        List<MasterPolicy> policies = masterPolicyRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
        List<Invoice> invoices = invoiceRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
        List<Claim> claims = claimRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
        List<RenewalCase> renewals = renewalCaseRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);

        BigDecimal totalBilled = invoices.stream()
                .filter(i -> !"CREDIT_NOTE".equals(i.getInvoiceType()))
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCollected = invoices.stream()
                .filter(i -> !"CREDIT_NOTE".equals(i.getInvoiceType()))
                .map(Invoice::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("totalPolicies", policies.size());
        dashboard.put("activePolicies", policies.stream().filter(p -> "ACTIVE".equals(p.getStatus())).count());
        dashboard.put("totalBilledPremiumWithGst", totalBilled);
        dashboard.put("totalCollectedPremium", totalCollected);
        dashboard.put("totalClaimsCount", claims.size());
        dashboard.put("settledClaimsCount", claims.stream().filter(c -> "SETTLED".equals(c.getStatus())).count());
        dashboard.put("activeRenewalPipelineCount", renewals.size());
        return dashboard;
    }
}
