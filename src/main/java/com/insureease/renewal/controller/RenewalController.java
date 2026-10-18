package com.insureease.renewal.controller;

import com.insureease.policy.entity.MasterPolicy;
import com.insureease.renewal.entity.RenewalCase;
import com.insureease.renewal.service.RenewalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "05. Renewals & Experience Rating (M5)", description = "ICR / Loss-ratio experience analysis, BR-7 automated renewal quote generation, member rollover, and BR-8 grace-period/lapse transitions")
public class RenewalController {

    private final RenewalService renewalService;

    public RenewalController(RenewalService renewalService) {
        this.renewalService = renewalService;
    }

    @PostMapping("/policies/{policyId}/renewals")
    @Operation(summary = "Analyze policy claims experience (ICR / Loss Ratio) and generate an experience-rated renewal quote (FR-5.2, FR-5.3, BR-7)")
    public Map<String, Object> generateRenewalQuote(@PathVariable UUID policyId) {
        return renewalService.analyzeExperienceAndGenerateRenewalQuote(policyId);
    }

    @PostMapping("/renewals/{renewalCaseId}/rollover")
    @Operation(summary = "Accept renewal quote and roll over active members into a renewed MasterPolicy term preserving waiting-period credit (FR-5.5)")
    public Map<String, Object> rolloverRenewal(@PathVariable UUID renewalCaseId) {
        return renewalService.acceptAndRolloverRenewal(renewalCaseId);
    }

    @PostMapping("/policies/{policyId}/lifecycle")
    @Operation(summary = "Transition policy into GRACE_PERIOD or LAPSED state for overdue dunning/lapse workflows (FR-5.6, BR-4, BR-8)")
    public MasterPolicy transitionLifecycle(@PathVariable UUID policyId,
                                            @RequestParam String targetStatus) {
        return renewalService.updateGraceOrLapseStatus(policyId, targetStatus);
    }

    @GetMapping("/renewals")
    @Operation(summary = "List all renewal cases for active tenant")
    public List<RenewalCase> listRenewals() {
        return renewalService.listRenewalCases();
    }
}
