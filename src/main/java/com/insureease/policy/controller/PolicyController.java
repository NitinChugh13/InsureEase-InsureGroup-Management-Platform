package com.insureease.policy.controller;

import com.insureease.notification.service.NotificationService;
import com.insureease.policy.entity.Endorsement;
import com.insureease.policy.entity.MasterPolicy;
import com.insureease.policy.entity.Member;
import com.insureease.policy.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "04. Policy Administration & Endorsements (M4)", description = "Master policy issuance, member/dependent/nominee roster, COI/E-Card PDFs, and Section 64VB mid-term endorsements")
public class PolicyController {

    private final PolicyService policyService;
    private final NotificationService notificationService;

    public PolicyController(PolicyService policyService, NotificationService notificationService) {
        this.policyService = policyService;
        this.notificationService = notificationService;
    }

    @PostMapping("/policies")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Issue a MasterPolicy from an ACCEPTED quote, initialize Section 64VB CD Account, and generate initial GST Invoice (FR-4.1, FR-7.1)")
    public Map<String, Object> issuePolicy(@Valid @RequestBody PolicyService.IssuePolicyRequest request) {
        return policyService.issuePolicyFromQuote(request);
    }

    @GetMapping("/policies")
    @Operation(summary = "List all master policies for active tenant")
    public List<MasterPolicy> listPolicies() {
        return policyService.listPolicies();
    }

    @GetMapping("/policies/{policyId}")
    @Operation(summary = "Get master policy details")
    public MasterPolicy getPolicy(@PathVariable UUID policyId) {
        return policyService.getPolicy(policyId);
    }

    @GetMapping("/policies/{policyId}/members")
    @Operation(summary = "List enrolled members under a master policy (FR-4.2)")
    public List<Member> getPolicyMembers(@PathVariable UUID policyId) {
        return policyService.getPolicyMembers(policyId);
    }

    @GetMapping("/members/{memberId}")
    @Operation(summary = "Get member self-service profile including covered dependents and GTL/GPA nominees (FR-4.6)")
    public Map<String, Object> getMemberProfile(@PathVariable UUID memberId) {
        return policyService.getMemberProfile(memberId);
    }

    @PostMapping("/policies/{policyId}/endorsements")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a mid-term endorsement (ADD_MEMBER, DELETE_MEMBER, UPDATE_SI, CORRECT_DATA) with pro-rata math & BR-11 check (FR-4.3, BR-2, BR-11)")
    public Map<String, Object> submitEndorsement(@PathVariable UUID policyId,
                                                 @Valid @RequestBody PolicyService.SubmitEndorsementRequest request) {
        return policyService.submitEndorsement(policyId, request);
    }

    @PostMapping("/endorsements/{endorsementId}/approve")
    @Operation(summary = "Approve mid-term endorsement, auto-debit Section 64VB CD Account (or raise Debit/Credit Note), and apply member changes (FR-4.3, BR-12)")
    public Map<String, Object> approveEndorsement(@PathVariable UUID endorsementId) {
        return policyService.approveAndApplyEndorsement(endorsementId);
    }

    @GetMapping("/policies/{policyId}/endorsements")
    @Operation(summary = "List endorsements for a policy")
    public List<Endorsement> listEndorsements(@PathVariable UUID policyId) {
        return policyService.listEndorsements(policyId);
    }

    @GetMapping(value = "/policies/{policyId}/schedule.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Download Master Policy Schedule PDF (FR-4.7)")
    public ResponseEntity<byte[]> downloadPolicySchedule(@PathVariable UUID policyId) {
        MasterPolicy p = policyService.getPolicy(policyId);
        Map<String, String> summary = new LinkedHashMap<>();
        summary.put("Policy Number", p.getPolicyNo());
        summary.put("Policy Status", p.getStatus());
        summary.put("Policy Term", p.getStartDate() + " to " + p.getEndDate());
        summary.put("Grace Period", p.getGracePeriodDays() + " days");
        summary.put("Payment Frequency", p.getPaymentFrequency());
        summary.put("Total Sum Insured (INR)", p.getTotalSumInsured().toPlainString());
        summary.put("Net Annual Premium (INR)", p.getNetAnnualPremium().toPlainString());
        summary.put("GST Amount (INR)", p.getGstAmount().toPlainString());
        summary.put("Total Annual Premium (INR)", p.getTotalAnnualPremium().toPlainString());

        byte[] pdf = notificationService.generateDocumentPdf(
                "Master Policy Schedule - " + p.getPolicyNo(),
                "IRDAI Compliant Group Insurance Master Policy Contract",
                summary
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + p.getPolicyNo() + "-schedule.pdf")
                .body(pdf);
    }

    @GetMapping(value = "/members/{memberId}/ecard.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Download Member E-Card / Certificate of Insurance (COI) PDF (FR-4.6, FR-4.7)")
    @SuppressWarnings("unchecked")
    public ResponseEntity<byte[]> downloadMemberECard(@PathVariable UUID memberId) {
        Map<String, Object> profile = policyService.getMemberProfile(memberId);
        Member m = (Member) profile.get("member");
        Map<String, String> summary = new LinkedHashMap<>();
        summary.put("Member Number (E-Card ID)", m.getMemberNo());
        summary.put("Certificate of Insurance (COI)", m.getCertificateNo());
        summary.put("Employee Code", m.getEmployeeCode());
        summary.put("Insured Name", m.getName());
        summary.put("Age / Gender", m.getAge() + " / " + m.getGender());
        summary.put("Occupation Class", m.getOccupationClass());
        summary.put("Approved Sum Insured (INR)", m.getSumInsured().toPlainString());
        summary.put("Remaining Sum Insured Balance (INR)", m.getRemainingSi().toPlainString());
        summary.put("Coverage Period", m.getCoverageStartDate() + " to " + m.getCoverageEndDate());
        summary.put("Continuous Cover Since", m.getContinuousCoverSince().toString());
        summary.put("Member Status", m.getStatus());

        byte[] pdf = notificationService.generateDocumentPdf(
                "Certificate of Insurance & Health E-Card - " + m.getMemberNo(),
                "InsureEase Digital Member Cover Certificate",
                summary
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + m.getMemberNo() + "-ecard.pdf")
                .body(pdf);
    }
}
