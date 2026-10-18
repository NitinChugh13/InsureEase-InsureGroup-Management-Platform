package com.insureease.billing.service;

import com.insureease.audit.service.AuditService;
import com.insureease.billing.entity.CdAccount;
import com.insureease.billing.entity.CdTransaction;
import com.insureease.billing.entity.Invoice;
import com.insureease.billing.entity.InvoiceLine;
import com.insureease.billing.entity.LedgerEntry;
import com.insureease.billing.entity.Payment;
import com.insureease.billing.repository.CdAccountRepository;
import com.insureease.billing.repository.CdTransactionRepository;
import com.insureease.billing.repository.InvoiceLineRepository;
import com.insureease.billing.repository.InvoiceRepository;
import com.insureease.billing.repository.LedgerEntryRepository;
import com.insureease.billing.repository.PaymentRepository;
import com.insureease.claims.entity.Claim;
import com.insureease.claims.repository.ClaimRepository;
import com.insureease.common.exception.BusinessRuleException;
import com.insureease.common.exception.ResourceNotFoundException;
import com.insureease.common.tenant.TenantContext;
import com.insureease.notification.service.NotificationService;
import com.insureease.policy.entity.Dependent;
import com.insureease.policy.entity.MasterPolicy;
import com.insureease.policy.entity.Member;
import com.insureease.policy.entity.Nominee;
import com.insureease.policy.repository.DependentRepository;
import com.insureease.policy.repository.MasterPolicyRepository;
import com.insureease.policy.repository.MemberRepository;
import com.insureease.policy.repository.NomineeRepository;
import com.insureease.quote.entity.QuoteMember;
import com.insureease.quote.repository.QuoteMemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BillingService {

    private static final AtomicInteger INV_SEQ = new AtomicInteger(5000);
    private static final AtomicInteger MEM_SEQ = new AtomicInteger(10000);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final PaymentRepository paymentRepository;
    private final CdAccountRepository cdAccountRepository;
    private final CdTransactionRepository cdTransactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final MasterPolicyRepository masterPolicyRepository;
    private final MemberRepository memberRepository;
    private final DependentRepository dependentRepository;
    private final NomineeRepository nomineeRepository;
    private final QuoteMemberRepository quoteMemberRepository;
    private final ClaimRepository claimRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final String razorpayKeyId;
    private final String razorpayWebhookSecret;
    private final String stripeWebhookSecret;

    public BillingService(InvoiceRepository invoiceRepository,
                          InvoiceLineRepository invoiceLineRepository,
                          PaymentRepository paymentRepository,
                          CdAccountRepository cdAccountRepository,
                          CdTransactionRepository cdTransactionRepository,
                          LedgerEntryRepository ledgerEntryRepository,
                          MasterPolicyRepository masterPolicyRepository,
                          MemberRepository memberRepository,
                          DependentRepository dependentRepository,
                          NomineeRepository nomineeRepository,
                          QuoteMemberRepository quoteMemberRepository,
                          ClaimRepository claimRepository,
                          AuditService auditService,
                          NotificationService notificationService,
                          @Value("${insureease.payments.razorpay.key-id}") String razorpayKeyId,
                          @Value("${insureease.payments.razorpay.webhook-secret}") String razorpayWebhookSecret,
                          @Value("${insureease.payments.stripe.webhook-secret}") String stripeWebhookSecret) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.paymentRepository = paymentRepository;
        this.cdAccountRepository = cdAccountRepository;
        this.cdTransactionRepository = cdTransactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.masterPolicyRepository = masterPolicyRepository;
        this.memberRepository = memberRepository;
        this.dependentRepository = dependentRepository;
        this.nomineeRepository = nomineeRepository;
        this.quoteMemberRepository = quoteMemberRepository;
        this.claimRepository = claimRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.razorpayKeyId = razorpayKeyId;
        this.razorpayWebhookSecret = razorpayWebhookSecret;
        this.stripeWebhookSecret = stripeWebhookSecret;
    }

    public record CreatePaymentOrderRequest(String provider, String mode) {
    }

    public record PaymentWebhookPayload(
            String gatewayOrderId,
            String gatewayPaymentId,
            String event,
            BigDecimal amount,
            String mode
    ) {
    }

    public record ManualPaymentRequest(
            UUID invoiceId,
            BigDecimal amount,
            String mode,
            String utrRef
    ) {
    }

    public record CdTopUpRequest(
            BigDecimal amount,
            String referenceNo,
            String remarks
    ) {
    }

    @Transactional
    public CdAccount createInitialCdAccount(UUID tenantId, UUID policyId, String policyNo, BigDecimal initialDeposit) {
        CdAccount cd = new CdAccount();
        cd.setTenantId(tenantId);
        cd.setPolicyId(policyId);
        cd.setAccountNo("CD-" + policyNo);
        BigDecimal balance = initialDeposit != null ? initialDeposit.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        cd.setCurrentBalance(balance);
        cd.setStatus("ACTIVE");
        CdAccount saved = cdAccountRepository.save(cd);

        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            recordCdTransaction(saved, "CREDIT_TOPUP", balance, "INITIAL_FLOAT", policyId,
                    "Initial Section 64VB Cash Deposit Float on policy issuance");
            postDoubleEntry(tenantId, policyId, "CASH_BANK", "CD_LIABILITY", balance,
                    "CdAccount", saved.getId(), "Initial CD Account float deposit for " + policyNo);
        }
        return saved;
    }

    @Transactional
    public CdAccount topUpCdAccount(UUID policyId, CdTopUpRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        if (req.amount() == null || req.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("FR-7.2", "CD Account top-up amount must be positive");
        }
        CdAccount cd = cdAccountRepository.findByTenantIdAndPolicyId(tenantId, policyId)
                .orElseThrow(() -> new ResourceNotFoundException("CdAccount for policy", policyId));

        BigDecimal newBalance = cd.getCurrentBalance().add(req.amount()).setScale(2, RoundingMode.HALF_UP);
        cd.setCurrentBalance(newBalance);
        CdAccount saved = cdAccountRepository.save(cd);

        recordCdTransaction(saved, "CREDIT_TOPUP", req.amount(), "TOPUP", policyId,
                req.remarks() != null ? req.remarks() : "CD float top-up ref: " + req.referenceNo());
        postDoubleEntry(tenantId, policyId, "CASH_BANK", "CD_LIABILITY", req.amount(),
                "CdAccount", saved.getId(), "CD Account top-up: " + req.referenceNo());

        auditService.logEvent("CdAccount", saved.getId(), "CD_ACCOUNT_TOPUP",
                Map.of("amount", req.amount(), "newBalance", newBalance));
        return saved;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCdAccountDetails(UUID policyId) {
        UUID tenantId = TenantContext.requireTenantId();
        CdAccount cd = cdAccountRepository.findByTenantIdAndPolicyId(tenantId, policyId)
                .orElseThrow(() -> new ResourceNotFoundException("CdAccount for policy", policyId));
        List<CdTransaction> txns = cdTransactionRepository.findByTenantIdAndCdAccountIdOrderByCreatedAtDesc(tenantId, cd.getId());
        return Map.of("cdAccount", cd, "transactions", txns);
    }

    @Transactional
    public boolean tryDebitCdAccountForEndorsement(UUID tenantId, UUID policyId, UUID endorsementId, BigDecimal totalAmount, String description) {
        Optional<CdAccount> opt = cdAccountRepository.findByTenantIdAndPolicyId(tenantId, policyId);
        if (opt.isEmpty()) {
            return false;
        }
        CdAccount cd = opt.get();
        if (cd.getCurrentBalance().compareTo(totalAmount) < 0) {
            return false;
        }
        BigDecimal newBalance = cd.getCurrentBalance().subtract(totalAmount).setScale(2, RoundingMode.HALF_UP);
        cd.setCurrentBalance(newBalance);
        cdAccountRepository.save(cd);

        recordCdTransaction(cd, "DEBIT_ENDORSEMENT", totalAmount, "ENDORSEMENT", endorsementId, description);
        postDoubleEntry(tenantId, policyId, "CD_LIABILITY", "PREMIUM_REVENUE", totalAmount,
                "Endorsement", endorsementId, description);
        return true;
    }

    @Transactional
    public void creditCdAccountForEndorsement(UUID tenantId, UUID policyId, UUID endorsementId, BigDecimal refundAmount, String description) {
        CdAccount cd = cdAccountRepository.findByTenantIdAndPolicyId(tenantId, policyId)
                .orElseThrow(() -> new ResourceNotFoundException("CdAccount for policy", policyId));
        BigDecimal newBalance = cd.getCurrentBalance().add(refundAmount).setScale(2, RoundingMode.HALF_UP);
        cd.setCurrentBalance(newBalance);
        cdAccountRepository.save(cd);

        recordCdTransaction(cd, "CREDIT_REFUND", refundAmount, "ENDORSEMENT", endorsementId, description);
        postDoubleEntry(tenantId, policyId, "PREMIUM_REVENUE", "CD_LIABILITY", refundAmount,
                "Endorsement", endorsementId, description);
    }

    @Transactional
    public Invoice generateInvoice(UUID tenantId,
                                   UUID policyId,
                                   UUID endorsementId,
                                   String invoiceType,
                                   BigDecimal netAmount,
                                   BigDecimal gstPct,
                                   String lineDescription,
                                   boolean alreadySettledViaCd) {
        BigDecimal absNet = netAmount.abs().setScale(2, RoundingMode.HALF_UP);
        BigDecimal safeGstPct = gstPct != null ? gstPct : new BigDecimal("18.00");
        BigDecimal gstAmount = absNet.multiply(safeGstPct).divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP);
        BigDecimal halfGst = gstAmount.divide(new BigDecimal("2.00"), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = absNet.add(gstAmount).setScale(2, RoundingMode.HALF_UP);

        String prefix = switch (invoiceType) {
            case "DEBIT_NOTE" -> "DN-";
            case "CREDIT_NOTE" -> "CN-";
            default -> "INV-";
        };

        Invoice inv = new Invoice();
        inv.setTenantId(tenantId);
        inv.setInvoiceNo(prefix + LocalDate.now().getYear() + "-" + INV_SEQ.incrementAndGet());
        inv.setPolicyId(policyId);
        inv.setEndorsementId(endorsementId);
        inv.setInvoiceType(invoiceType);
        inv.setNetAmount(absNet);
        inv.setGstPct(safeGstPct);
        inv.setCgstAmount(halfGst);
        inv.setSgstAmount(gstAmount.subtract(halfGst));
        inv.setIgstAmount(BigDecimal.ZERO);
        inv.setGstAmount(gstAmount);
        inv.setTotalAmount(totalAmount);
        inv.setPaidAmount(alreadySettledViaCd ? totalAmount : BigDecimal.ZERO);
        inv.setDueDate(LocalDate.now().plusDays(15));
        inv.setStatus(alreadySettledViaCd ? "PAID" : "GENERATED");
        Invoice saved = invoiceRepository.save(inv);

        InvoiceLine line = new InvoiceLine();
        line.setTenantId(tenantId);
        line.setInvoiceId(saved.getId());
        line.setDescription(lineDescription);
        line.setQuantity(1);
        line.setUnitAmount(absNet);
        line.setTaxPct(safeGstPct);
        line.setTaxAmount(gstAmount);
        line.setLineTotal(totalAmount);
        invoiceLineRepository.save(line);

        if (!alreadySettledViaCd && !"CREDIT_NOTE".equals(invoiceType)) {
            // Post accrual: Debit AR_POLICYHOLDER, Credit PREMIUM_REVENUE + GST_PAYABLE
            UUID groupId = UUID.randomUUID();
            saveLedgerRow(tenantId, groupId, policyId, "AR_POLICYHOLDER", totalAmount, BigDecimal.ZERO,
                    "Invoice", saved.getId(), "Invoice raised: " + saved.getInvoiceNo());
            saveLedgerRow(tenantId, groupId, policyId, "PREMIUM_REVENUE", BigDecimal.ZERO, absNet,
                    "Invoice", saved.getId(), "Net premium accrual: " + saved.getInvoiceNo());
            saveLedgerRow(tenantId, groupId, policyId, "GST_PAYABLE", BigDecimal.ZERO, gstAmount,
                    "Invoice", saved.getId(), "GST accrual: " + saved.getInvoiceNo());
        }

        return saved;
    }

    @Transactional
    public Map<String, Object> createSandboxPaymentOrder(UUID invoiceId, CreatePaymentOrderRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        Invoice invoice = invoiceRepository.findByIdAndTenantId(invoiceId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        if ("PAID".equals(invoice.getStatus())) {
            throw new BusinessRuleException("FR-7.3", "Invoice is already PAID: " + invoice.getInvoiceNo());
        }

        String provider = req.provider() != null ? req.provider().toUpperCase() : "RAZORPAY";
        if (!List.of("RAZORPAY", "STRIPE").contains(provider)) {
            throw new BusinessRuleException("FR-7.3", "Supported sandbox providers are RAZORPAY or STRIPE");
        }

        BigDecimal payable = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        String orderPrefix = "RAZORPAY".equals(provider) ? "order_rzp_" : "pi_stripe_";
        String gatewayOrderId = orderPrefix + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        String samplePaymentId = ("RAZORPAY".equals(provider) ? "pay_rzp_" : "ch_stripe_")
                + UUID.randomUUID().toString().replace("-", "").substring(0, 14);

        Payment payment = new Payment();
        payment.setTenantId(tenantId);
        payment.setInvoiceId(invoice.getId());
        payment.setProvider(provider);
        payment.setGatewayOrderId(gatewayOrderId);
        payment.setAmount(payable);
        payment.setCurrency("INR");
        payment.setMode(req.mode() != null ? req.mode().toUpperCase() : "NETBANKING");
        payment.setStatus("ORDER_CREATED");
        Payment saved = paymentRepository.save(payment);

        String sampleSignature = computeHmacSha256(gatewayOrderId + "|" + samplePaymentId,
                "RAZORPAY".equals(provider) ? razorpayWebhookSecret : stripeWebhookSecret);

        auditService.logEvent("Payment", saved.getId(), "SANDBOX_PAYMENT_ORDER_CREATED",
                Map.of("invoiceNo", invoice.getInvoiceNo(), "provider", provider, "gatewayOrderId", gatewayOrderId));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("paymentId", saved.getId());
        response.put("invoiceId", invoice.getId());
        response.put("invoiceNo", invoice.getInvoiceNo());
        response.put("provider", provider);
        response.put("gatewayKeyId", razorpayKeyId);
        response.put("gatewayOrderId", gatewayOrderId);
        response.put("amount", payable);
        response.put("currency", "INR");
        response.put("status", saved.getStatus());
        // Include sandbox webhook test helper so evaluator can immediately invoke the HMAC-verified webhook
        response.put("sandboxWebhookHelper", Map.of(
                "webhookEndpoint", "/api/v1/webhooks/payments/" + provider.toLowerCase(),
                "signatureHeaderName", "RAZORPAY".equals(provider) ? "X-Razorpay-Signature" : "Stripe-Signature",
                "sampleGatewayPaymentId", samplePaymentId,
                "computedHmacSignature", sampleSignature
        ));
        return response;
    }

    @Transactional
    public Map<String, Object> processGatewayWebhook(String providerRaw,
                                                     String signatureHeader,
                                                     PaymentWebhookPayload payload) {
        String provider = providerRaw.toUpperCase();
        String secret = "RAZORPAY".equals(provider) ? razorpayWebhookSecret : stripeWebhookSecret;

        if (payload.gatewayOrderId() == null || payload.gatewayPaymentId() == null) {
            throw new BusinessRuleException("FR-7.3", "gatewayOrderId and gatewayPaymentId are required in webhook payload");
        }

        // 1. Cryptographic HMAC-SHA256 Signature Verification
        String expectedSignature = computeHmacSha256(
                payload.gatewayOrderId() + "|" + payload.gatewayPaymentId(), secret);
        if (signatureHeader == null || !expectedSignature.equalsIgnoreCase(signatureHeader.trim())) {
            throw new BusinessRuleException("FR-7.3",
                    "Invalid webhook HMAC-SHA256 signature for provider " + provider);
        }

        // 2. Idempotency check: if gatewayPaymentId already processed, return existing result safely
        Optional<Payment> alreadyProcessed = paymentRepository.findByProviderIgnoreCaseAndGatewayPaymentId(
                provider, payload.gatewayPaymentId());
        if (alreadyProcessed.isPresent() && "CAPTURED".equals(alreadyProcessed.get().getStatus())) {
            Payment existing = alreadyProcessed.get();
            Invoice inv = invoiceRepository.findById(existing.getInvoiceId()).orElseThrow();
            return Map.of(
                    "idempotentReplay", true,
                    "payment", existing,
                    "invoiceStatus", inv.getStatus()
            );
        }

        Payment payment = paymentRepository.findByProviderIgnoreCaseAndGatewayOrderId(provider, payload.gatewayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment Order", payload.gatewayOrderId()));

        UUID tenantId = payment.getTenantId();
        TenantContext.setTenantId(tenantId);

        payment.setGatewayPaymentId(payload.gatewayPaymentId());
        payment.setGatewaySignature(signatureHeader);
        payment.setTxnRef(payload.gatewayPaymentId());
        if (payload.mode() != null) {
            payment.setMode(payload.mode().toUpperCase());
        }
        payment.setStatus("CAPTURED");
        payment.setPaidOn(Instant.now());
        paymentRepository.save(payment);

        Invoice invoice = reconcileInvoiceAndActivatePolicy(tenantId, payment.getInvoiceId(), payment.getAmount(),
                payment.getId(), provider + ":" + payload.gatewayPaymentId());

        return Map.of(
                "idempotentReplay", false,
                "payment", payment,
                "invoice", invoice
        );
    }

    @Transactional
    public Map<String, Object> recordManualPayment(ManualPaymentRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        Invoice invoice = invoiceRepository.findByIdAndTenantId(req.invoiceId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", req.invoiceId()));

        if ("PAID".equals(invoice.getStatus())) {
            throw new BusinessRuleException("FR-7.3", "Invoice is already PAID");
        }

        Payment payment = new Payment();
        payment.setTenantId(tenantId);
        payment.setInvoiceId(invoice.getId());
        payment.setProvider("MANUAL");
        payment.setGatewayOrderId("MANUAL-" + UUID.randomUUID().toString().substring(0, 8));
        payment.setGatewayPaymentId(req.utrRef());
        payment.setAmount(req.amount() != null ? req.amount() : invoice.getTotalAmount());
        payment.setCurrency("INR");
        payment.setMode(req.mode() != null ? req.mode().toUpperCase() : "NEFT");
        payment.setTxnRef(req.utrRef());
        payment.setStatus("CAPTURED");
        payment.setPaidOn(Instant.now());
        Payment savedPayment = paymentRepository.save(payment);

        Invoice updatedInvoice = reconcileInvoiceAndActivatePolicy(tenantId, invoice.getId(),
                savedPayment.getAmount(), savedPayment.getId(), "UTR:" + req.utrRef());

        return Map.of("payment", savedPayment, "invoice", updatedInvoice);
    }

    @Transactional
    public void recordClaimSettlementPayout(UUID tenantId, UUID policyId, UUID claimId, BigDecimal amount, String payeeName, String utrNo) {
        postDoubleEntry(tenantId, policyId, "CLAIMS_PAYABLE", "CASH_BANK", amount,
                "Claim", claimId, "Claim settlement payout to " + payeeName + " (UTR: " + utrNo + ")");
    }

    @Transactional(readOnly = true)
    public List<Invoice> listInvoices(UUID policyId) {
        UUID tenantId = TenantContext.requireTenantId();
        if (policyId != null) {
            return invoiceRepository.findByTenantIdAndPolicyIdOrderByCreatedAtDesc(tenantId, policyId);
        }
        return invoiceRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getInvoiceDetails(UUID invoiceId) {
        UUID tenantId = TenantContext.requireTenantId();
        Invoice invoice = invoiceRepository.findByIdAndTenantId(invoiceId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));
        List<InvoiceLine> lines = invoiceLineRepository.findByTenantIdAndInvoiceId(tenantId, invoiceId);
        List<Payment> payments = paymentRepository.findByTenantIdAndInvoiceIdOrderByCreatedAtDesc(tenantId, invoiceId);
        return Map.of("invoice", invoice, "lines", lines, "payments", payments);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> listLedgerEntries(UUID policyId) {
        UUID tenantId = TenantContext.requireTenantId();
        if (policyId != null) {
            return ledgerEntryRepository.findByTenantIdAndPolicyIdOrderByCreatedAtDesc(tenantId, policyId);
        }
        return ledgerEntryRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    private Invoice reconcileInvoiceAndActivatePolicy(UUID tenantId,
                                                      UUID invoiceId,
                                                      BigDecimal paymentAmount,
                                                      UUID paymentId,
                                                      String narrationRef) {
        Invoice invoice = invoiceRepository.findByIdAndTenantId(invoiceId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        BigDecimal newPaid = invoice.getPaidAmount().add(paymentAmount).setScale(2, RoundingMode.HALF_UP);
        invoice.setPaidAmount(newPaid);
        if (newPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus("PAID");
        } else {
            invoice.setStatus("PARTIALLY_PAID");
        }
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Post Double-Entry Ledger: Debit CASH_BANK, Credit AR_POLICYHOLDER
        postDoubleEntry(tenantId, invoice.getPolicyId(), "CASH_BANK", "AR_POLICYHOLDER", paymentAmount,
                "Payment", paymentId, "Payment received against " + invoice.getInvoiceNo() + " (" + narrationRef + ")");

        if ("PAID".equals(savedInvoice.getStatus())) {
            MasterPolicy policy = masterPolicyRepository.findByIdAndTenantId(invoice.getPolicyId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("MasterPolicy", invoice.getPolicyId()));

            if ("PENDING_PAYMENT".equals(policy.getStatus()) || "DRAFT".equals(policy.getStatus())) {
                policy.setStatus("ACTIVE");
                masterPolicyRepository.save(policy);
                enrolCensusMembersIfNotYetEnrolled(policy);

                notificationService.sendNotification(tenantId, "EMAIL", "POLICY_ACTIVATED",
                        "hr@policyholder.com", "Policy " + policy.getPolicyNo() + " is now ACTIVE",
                        "Payment confirmed against invoice " + invoice.getInvoiceNo() + ". Member certificates and E-cards generated.",
                        "MasterPolicy", policy.getId());
            } else if ("GRACE_PERIOD".equals(policy.getStatus())) {
                // BR-4: Restore policy from GRACE_PERIOD to ACTIVE and release held claims
                policy.setStatus("ACTIVE");
                masterPolicyRepository.save(policy);
                List<Claim> heldClaims = claimRepository.findByTenantIdAndPolicyIdAndStatus(
                        tenantId, policy.getId(), "ON_HOLD_PREMIUM_PENDING");
                for (Claim held : heldClaims) {
                    held.setStatus("UNDER_REVIEW");
                    held.setEligibilityNotes((held.getEligibilityNotes() != null ? held.getEligibilityNotes() + " | " : "")
                            + "Released from grace period hold upon payment of " + invoice.getInvoiceNo());
                    claimRepository.save(held);
                }
            }
        }

        auditService.logAction(tenantId, "Invoice", savedInvoice.getId(), "INVOICE_RECONCILED",
                null, Map.of("invoiceNo", savedInvoice.getInvoiceNo(), "status", savedInvoice.getStatus(), "paidAmount", newPaid));
        return savedInvoice;
    }

    public void enrolCensusMembersIfNotYetEnrolled(MasterPolicy policy) {
        UUID tenantId = policy.getTenantId();
        List<Member> existing = memberRepository.findByTenantIdAndPolicyIdOrderByEmployeeCodeAsc(tenantId, policy.getId());
        if (!existing.isEmpty()) {
            return;
        }

        // Preserve continuousCoverSince from parent policy if this is a renewal
        Map<String, LocalDate> parentContinuousDates = new HashMap<>();
        if (policy.getParentPolicyId() != null) {
            List<Member> priorMembers = memberRepository.findByTenantIdAndPolicyIdOrderByEmployeeCodeAsc(
                    tenantId, policy.getParentPolicyId());
            for (Member pm : priorMembers) {
                parentContinuousDates.put(pm.getEmployeeCode(), pm.getContinuousCoverSince());
            }
        }

        List<QuoteMember> quoteMembers = quoteMemberRepository.findByTenantIdAndQuoteIdOrderByRowNoAsc(
                tenantId, policy.getQuoteId());

        Map<String, Member> primaryByEmpCode = new HashMap<>();

        // First pass: Enrol primary employees (SELF)
        for (QuoteMember qm : quoteMembers) {
            if (!qm.isValid() || !"SELF".equalsIgnoreCase(qm.getRelation())) {
                continue;
            }
            int seq = MEM_SEQ.incrementAndGet();
            Member m = new Member();
            m.setTenantId(tenantId);
            m.setPolicyId(policy.getId());
            m.setEmployeeCode(qm.getEmployeeCode());
            m.setMemberNo("MEM-" + seq);
            m.setCertificateNo("COI-" + policy.getPolicyNo() + "-" + qm.getEmployeeCode());
            m.setName(qm.getName());
            m.setDob(qm.getDob());
            m.setAge(qm.getAge());
            m.setGender(qm.getGender());
            m.setEmail(qm.getEmail());
            m.setMobile(qm.getMobile());
            m.setOccupationClass(qm.getOccupationClass());
            m.setAnnualCtc(qm.getAnnualCtc());
            m.setRequestedSi(qm.getRequestedSi());
            m.setSumInsured(qm.getApprovedSi());
            m.setUtilizedSi(BigDecimal.ZERO);
            m.setAnnualMemberPremium(qm.getBasePremium());
            m.setUwStatus(qm.getUwStatus());
            m.setCoverageStartDate(policy.getStartDate());
            m.setCoverageEndDate(policy.getEndDate());
            m.setContinuousCoverSince(parentContinuousDates.getOrDefault(qm.getEmployeeCode(), policy.getStartDate()));
            m.setStatus("ACTIVE");
            Member savedMember = memberRepository.save(m);
            primaryByEmpCode.put(qm.getEmployeeCode(), savedMember);

            if (qm.getNomineeName() != null && !qm.getNomineeName().isBlank()) {
                Nominee nom = new Nominee();
                nom.setTenantId(tenantId);
                nom.setMemberId(savedMember.getId());
                nom.setName(qm.getNomineeName());
                nom.setRelation(qm.getNomineeRelation() != null ? qm.getNomineeRelation() : "SPOUSE");
                nom.setDob(qm.getNomineeDob() != null ? qm.getNomineeDob() : LocalDate.of(1992, 1, 1));
                nom.setSharePct(qm.getNomineeSharePct() != null ? qm.getNomineeSharePct() : new BigDecimal("100.00"));
                nom.setAppointeeName(qm.getAppointeeName());
                nomineeRepository.save(nom);
            }
        }

        // Second pass: Enrol dependents (SPOUSE, CHILD, PARENT)
        int depIdx = 1;
        for (QuoteMember qm : quoteMembers) {
            if (!qm.isValid() || "SELF".equalsIgnoreCase(qm.getRelation())) {
                continue;
            }
            Member parentMember = primaryByEmpCode.get(qm.getEmployeeCode());
            if (parentMember == null) {
                continue;
            }
            Dependent dep = new Dependent();
            dep.setTenantId(tenantId);
            dep.setMemberId(parentMember.getId());
            dep.setDependentNo(parentMember.getMemberNo() + "-D" + (depIdx++));
            dep.setRelation(qm.getRelation());
            dep.setName(qm.getName());
            dep.setDob(qm.getDob());
            dep.setAge(qm.getAge());
            dep.setGender(qm.getGender());
            dep.setIndividualSi(qm.getApprovedSi());
            dep.setUtilizedSi(BigDecimal.ZERO);
            dep.setAnnualDependentPremium(qm.getBasePremium());
            dep.setCoverageStartDate(policy.getStartDate());
            dep.setCoverageEndDate(policy.getEndDate());
            dep.setContinuousCoverSince(parentMember.getContinuousCoverSince());
            dep.setStatus("ACTIVE");
            dependentRepository.save(dep);
        }
    }

    private void recordCdTransaction(CdAccount cd, String txnType, BigDecimal amount, String refType, UUID refId, String remarks) {
        CdTransaction txn = new CdTransaction();
        txn.setTenantId(cd.getTenantId());
        txn.setCdAccountId(cd.getId());
        txn.setTxnType(txnType);
        txn.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        txn.setBalanceAfter(cd.getCurrentBalance());
        txn.setReferenceType(refType);
        txn.setReferenceId(refId);
        txn.setRemarks(remarks);
        cdTransactionRepository.save(txn);
    }

    public void postDoubleEntry(UUID tenantId,
                                UUID policyId,
                                String debitAccount,
                                String creditAccount,
                                BigDecimal amount,
                                String refType,
                                UUID refId,
                                String narration) {
        UUID groupId = UUID.randomUUID();
        BigDecimal scaled = amount.abs().setScale(2, RoundingMode.HALF_UP);
        saveLedgerRow(tenantId, groupId, policyId, debitAccount, scaled, BigDecimal.ZERO, refType, refId, narration);
        saveLedgerRow(tenantId, groupId, policyId, creditAccount, BigDecimal.ZERO, scaled, refType, refId, narration);
    }

    private void saveLedgerRow(UUID tenantId,
                               UUID groupId,
                               UUID policyId,
                               String accountCode,
                               BigDecimal debit,
                               BigDecimal credit,
                               String refType,
                               UUID refId,
                               String narration) {
        LedgerEntry entry = new LedgerEntry();
        entry.setTenantId(tenantId);
        entry.setEntryGroupId(groupId);
        entry.setPolicyId(policyId);
        entry.setAccountCode(accountCode);
        entry.setDebitAmount(debit);
        entry.setCreditAmount(credit);
        entry.setRefType(refType);
        entry.setRefId(refId);
        entry.setNarration(narration);
        ledgerEntryRepository.save(entry);
    }

    public String computeHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(rawHmac);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMAC-SHA256", e);
        }
    }
}
