package com.insureease.claims.controller;

import com.insureease.claims.entity.Claim;
import com.insureease.claims.entity.Hospital;
import com.insureease.claims.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "06. Multi-Product Claims Management (M6)", description = "FNOL claim intake (Cashless, Reimbursement, Death, Disability), 5-step EligibilityChain, document checklists, benefit math, two-level adjudication, and settlement payouts")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping("/hospitals")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a network or non-network hospital")
    public Hospital createHospital(@Valid @RequestBody ClaimService.CreateHospitalRequest request) {
        return claimService.createHospital(request);
    }

    @GetMapping("/hospitals")
    @Operation(summary = "List registered hospitals")
    public List<Hospital> listHospitals() {
        return claimService.listHospitals();
    }

    @PostMapping("/claims")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a claim (CASHLESS, REIMBURSEMENT, DEATH_BENEFIT, DISABILITY_BENEFIT) and execute EligibilityChain & BenefitCalculator (FR-6.1, FR-6.3, FR-6.5, BR-3, BR-4, BR-5)")
    public Map<String, Object> submitClaim(@Valid @RequestBody ClaimService.SubmitClaimRequest request) {
        return claimService.submitClaim(request);
    }

    @GetMapping("/claims")
    @Operation(summary = "List claims (optionally filtered by policyId)")
    public List<Claim> listClaims(@RequestParam(required = false) UUID policyId) {
        return claimService.listClaims(policyId);
    }

    @GetMapping("/claims/{claimId}")
    @Operation(summary = "Get full claim details including document checklist, status history, and settlements")
    public Map<String, Object> getClaimDetails(@PathVariable UUID claimId) {
        return claimService.getClaimDetails(claimId);
    }

    @PostMapping("/claims/{claimId}/documents")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload/register a claim document and evaluate mandatory checklist completion (FR-6.2)")
    public Map<String, Object> uploadDocument(@PathVariable UUID claimId,
                                              @Valid @RequestBody ClaimService.UploadDocumentRequest request) {
        return claimService.uploadClaimDocument(claimId, request);
    }

    @PostMapping("/claims/{claimId}/decision")
    @Operation(summary = "Adjudicate claim (APPROVE, PARTIAL_APPROVE, QUERY, REJECT) with BR-10 two-level approval and automatic settlement ledger payout (FR-6.4, FR-6.6, BR-10)")
    public Map<String, Object> adjudicateClaim(@PathVariable UUID claimId,
                                               @Valid @RequestBody ClaimService.AdjudicateClaimRequest request) {
        return claimService.adjudicateClaim(claimId, request);
    }
}
