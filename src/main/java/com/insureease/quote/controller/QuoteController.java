package com.insureease.quote.controller;

import com.insureease.notification.service.NotificationService;
import com.insureease.quote.entity.Policyholder;
import com.insureease.quote.entity.Quote;
import com.insureease.quote.entity.QuoteMember;
import com.insureease.quote.entity.QuoteVersion;
import com.insureease.quote.service.QuoteService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "03. Quotation & Underwriting (M3)", description = "Employer KYC profiles, bulk census validation, multi-product pricing, FCL medical underwriting, and maker-checker approvals")
public class QuoteController {

    private final QuoteService quoteService;
    private final NotificationService notificationService;

    public QuoteController(QuoteService quoteService, NotificationService notificationService) {
        this.quoteService = quoteService;
        this.notificationService = notificationService;
    }

    @PostMapping("/policyholders")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create corporate Employer/Policyholder profile with GSTIN and encrypted PAN (FR-3.1)")
    public Policyholder createPolicyholder(@Valid @RequestBody QuoteService.CreatePolicyholderRequest request) {
        return quoteService.createPolicyholder(request);
    }

    @GetMapping("/policyholders")
    @Operation(summary = "List employer policyholders for active tenant")
    public List<Policyholder> listPolicyholders() {
        return quoteService.listPolicyholders();
    }

    @PostMapping("/quotes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new group insurance quote for an employer and plan (FR-3.1)")
    public Quote createQuote(@Valid @RequestBody QuoteService.CreateQuoteRequest request) {
        return quoteService.createQuote(request);
    }

    @GetMapping("/quotes")
    @Operation(summary = "List all quotes for active tenant")
    public List<Quote> listQuotes() {
        return quoteService.listQuotes();
    }

    @GetMapping("/quotes/{quoteId}")
    @Operation(summary = "Get quote details")
    public Quote getQuote(@PathVariable UUID quoteId) {
        return quoteService.getQuote(quoteId);
    }

    @PostMapping("/quotes/{quoteId}/census")
    @Operation(summary = "Upload member & dependent census via JSON array with row-level validation (FR-3.2, BR-6)")
    public Map<String, Object> uploadCensusJson(@PathVariable UUID quoteId,
                                               @Valid @RequestBody QuoteService.CensusUploadRequest request) {
        return quoteService.uploadCensusJson(quoteId, request.members());
    }

    @PostMapping(value = "/quotes/{quoteId}/census/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload member & dependent census via CSV file with row-level validation (FR-3.2)")
    public Map<String, Object> uploadCensusCsv(@PathVariable UUID quoteId,
                                              @RequestParam("file") MultipartFile file) {
        return quoteService.uploadCensusCsv(quoteId, file);
    }

    @GetMapping("/quotes/{quoteId}/members")
    @Operation(summary = "Get validated census members and per-member pricing for a quote")
    public List<QuoteMember> getQuoteMembers(@PathVariable UUID quoteId) {
        return quoteService.getQuoteMembers(quoteId);
    }

    @PostMapping("/quotes/{quoteId}/calculate")
    @Operation(summary = "Run multi-product PremiumEngine (GMC/GTL/GPA), apply FCL caps, and evaluate underwriting rules (FR-3.3, FR-3.4, BR-1, BR-12)")
    public Map<String, Object> calculate(@PathVariable UUID quoteId) {
        return quoteService.calculatePremiumAndEvaluateRules(quoteId);
    }

    @PostMapping("/quotes/{quoteId}/decision")
    @Operation(summary = "Underwriter approve, reject, or counter-offer quote with maker-checker enforcement (FR-3.5, BR-9)")
    public Quote recordDecision(@PathVariable UUID quoteId,
                                @Valid @RequestBody QuoteService.UnderwriterDecisionRequest request) {
        return quoteService.recordUnderwriterDecision(quoteId, request);
    }

    @PostMapping("/quotes/{quoteId}/members/{quoteMemberId}/uw-decision")
    @Operation(summary = "Approve or decline medical underwriting for a GTL member exceeding the Free Cover Limit (BR-12)")
    public Map<String, Object> decideMemberUw(@PathVariable UUID quoteId,
                                              @PathVariable UUID quoteMemberId,
                                              @Valid @RequestBody QuoteService.MemberUwDecisionRequest request) {
        return quoteService.decideMemberMedicalUnderwriting(quoteId, quoteMemberId, request);
    }

    @PostMapping("/quotes/{quoteId}/accept")
    @Operation(summary = "Customer/Employer accepts an approved or counter-offered quote (FR-3.7)")
    public Quote acceptQuote(@PathVariable UUID quoteId) {
        return quoteService.acceptQuote(quoteId);
    }

    @GetMapping("/quotes/{quoteId}/versions")
    @Operation(summary = "Get version history for a quote (FR-3.6)")
    public List<QuoteVersion> getVersions(@PathVariable UUID quoteId) {
        return quoteService.getQuoteVersions(quoteId);
    }

    @GetMapping(value = "/quotes/{quoteId}/proposal.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Export Quote Proposal as PDF (FR-3.6)")
    public ResponseEntity<byte[]> exportQuotePdf(@PathVariable UUID quoteId) {
        Quote q = quoteService.getQuote(quoteId);
        Map<String, String> summary = new LinkedHashMap<>();
        summary.put("Quote Number", q.getQuoteNo());
        summary.put("Status", q.getStatus());
        summary.put("Version", String.valueOf(q.getCurrentVersion()));
        summary.put("Total Lives (Employees / Dependents)", q.getTotalLives() + " (" + q.getEmployeeCount() + " / " + q.getDependentCount() + ")");
        summary.put("Total Approved Sum Insured (INR)", q.getTotalApprovedSi().toPlainString());
        summary.put("Base Premium (INR)", q.getBasePremium().toPlainString());
        summary.put("Group Discount (%)", q.getGroupDiscountPct().toPlainString() + "%");
        summary.put("Net Premium (INR)", q.getNetPremium().toPlainString());
        summary.put("GST (" + q.getGstPct() + "%)", q.getGstAmount().toPlainString());
        summary.put("Total Payable Premium (INR)", q.getTotalPremium().toPlainString());
        summary.put("Valid Till", q.getValidTill().toString());

        byte[] pdf = notificationService.generateDocumentPdf(
                "Group Insurance Quote Proposal - " + q.getQuoteNo(),
                "Generated by InsureEase Underwriting Engine",
                summary
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + q.getQuoteNo() + ".pdf")
                .body(pdf);
    }
}
