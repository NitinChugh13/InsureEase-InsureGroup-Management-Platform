package com.insureease.billing.controller;

import com.insureease.billing.entity.CdAccount;
import com.insureease.billing.entity.Invoice;
import com.insureease.billing.entity.LedgerEntry;
import com.insureease.billing.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "07. Billing, CD Account & Payments (M7)", description = "GST Invoices, Debit/Credit notes, Section 64VB CD Float Account, Razorpay/Stripe Sandbox orders, HMAC webhooks, and double-entry ledger")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/invoices")
    @Operation(summary = "List invoices and debit/credit notes (optionally filtered by policyId) (FR-7.1)")
    public List<Invoice> listInvoices(@RequestParam(required = false) UUID policyId) {
        return billingService.listInvoices(policyId);
    }

    @GetMapping("/invoices/{invoiceId}")
    @Operation(summary = "Get invoice details including itemized GST lines and payments")
    public Map<String, Object> getInvoiceDetails(@PathVariable UUID invoiceId) {
        return billingService.getInvoiceDetails(invoiceId);
    }

    @PostMapping("/invoices/{invoiceId}/payment-orders")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a Razorpay or Stripe Sandbox Payment Order for an open invoice (FR-7.3)")
    public Map<String, Object> createPaymentOrder(@PathVariable UUID invoiceId,
                                                  @RequestBody BillingService.CreatePaymentOrderRequest request) {
        return billingService.createSandboxPaymentOrder(invoiceId, request);
    }

    @PostMapping("/webhooks/payments/{provider}")
    @Operation(summary = "Public HMAC-SHA256 verified payment webhook for Razorpay / Stripe sandbox reconciliation (FR-7.3, FR-7.4)")
    public Map<String, Object> handleWebhook(
            @PathVariable String provider,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String razorpaySig,
            @RequestHeader(value = "Stripe-Signature", required = false) String stripeSig,
            @RequestBody BillingService.PaymentWebhookPayload payload) {
        String signature = razorpaySig != null ? razorpaySig : stripeSig;
        return billingService.processGatewayWebhook(provider, signature, payload);
    }

    @PostMapping("/payments/manual")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record a manual NEFT/RTGS/UTR payment by Finance Officer and reconcile invoice (FR-7.3, FR-7.4)")
    public Map<String, Object> recordManualPayment(@Valid @RequestBody BillingService.ManualPaymentRequest request) {
        return billingService.recordManualPayment(request);
    }

    @GetMapping("/policies/{policyId}/cd-account")
    @Operation(summary = "Get policy Section 64VB Cash Deposit (CD) Float Account balance and statement (FR-7.2, BR-12)")
    public Map<String, Object> getCdAccount(@PathVariable UUID policyId) {
        return billingService.getCdAccountDetails(policyId);
    }

    @PostMapping("/policies/{policyId}/cd-account/topup")
    @Operation(summary = "Top up policy Section 64VB Cash Deposit (CD) Float Account (FR-7.2, BR-12)")
    public CdAccount topUpCdAccount(@PathVariable UUID policyId,
                                    @Valid @RequestBody BillingService.CdTopUpRequest request) {
        return billingService.topUpCdAccount(policyId, request);
    }

    @GetMapping("/ledger-entries")
    @Operation(summary = "List double-entry accounting ledger entries (optionally filtered by policyId) (FR-7.6)")
    public List<LedgerEntry> listLedgerEntries(@RequestParam(required = false) UUID policyId) {
        return billingService.listLedgerEntries(policyId);
    }
}
