# InsureEase – InsureGroup Management Platform (Backend v1.1)

Multi-tenant SaaS Group Insurance Lifecycle Platform built with **Java 21** and **Spring Boot 3.4**, supporting **Group Health (`GMC`)**, **Group Term Life (`GTL`)**, and **Group Personal Accident (`GPA`)** from Day 1.

## Architecture & Module Map

| Module | Package | Responsibilities |
| :--- | :--- | :--- |
| **M1: Tenant & IAM** | `com.insureease.iam` | Multi-tenant onboarding (`INSURER`, `TPA`, `BROKER`), JWT + rotating Refresh Tokens, RBAC, account lockout |
| **M2: Product & Rates** | `com.insureease.product` | Products (`HEALTH`, `LIFE`, `PA`), Plans, Benefits, Sub-limits, Free Cover Limit (`FCL`), versioned Rate Tables with overlap validation |
| **M3: Quote & Underwriting** | `com.insureease.quote` | Corporate Policyholder KYC, CSV/JSON bulk census validator, Multi-Product `PremiumEngine` (`GMC`, `GTL`, `GPA`), FCL capping & medical UW (`BR-12`), Maker-Checker (`BR-9`) |
| **M4: Policy & Endorsements** | `com.insureease.policy` | Master policy lifecycle, Member/Dependent/Nominee enrolment, COI/E-Card PDFs, mid-term endorsements (`BR-2` pro-rata, `BR-11` zero refund on claimed members, `BR-12` Section 64VB CD auto-debit) |
| **M5: Renewals** | `com.insureease.renewal` | Incurred Claims Ratio (`ICR`) experience analysis, `BR-7` rate adjustment curve, automated renewal quote & member rollover preserving waiting-period credit |
| **M6: Claims** | `com.insureease.claims` | Multi-product FNOL intake (`CASHLESS`, `REIMBURSEMENT`, `DEATH_BENEFIT`, `DISABILITY_BENEFIT`), 5-step `EligibilityChain` (`BR-3`, `BR-4`, `BR-5`), Benefit/Sub-limit/Co-pay/PPD Scale calculator, `BR-10` 2-level adjudication, Hospital/Member/Nominee settlement payouts |
| **M7: Billing, CD & Gateways** | `com.insureease.billing` | GST Invoices, Debit/Credit Notes, Section 64VB Cash Deposit (`CdAccount`) float ledger, **Razorpay & Stripe Sandbox Order + `HMAC-SHA256` Webhook verification**, double-entry `LedgerEntry` |
| **M8: Notifications & PDF** | `com.insureease.notification` | Event notifications & OpenPDF generation (Quote Proposal, Policy Schedule, Member E-Card/COI) |
| **M9: Reports & Dashboards** | `com.insureease.reporting` | Portfolio & Policy Loss Ratio (`ICR`), Claims TAT & IRDAI 2024 SLA compliance, Executive Dashboard |
| **M10: Audit & DPDP** | `com.insureease.audit` | Immutable `audit_log`, DPDP Act 2023 `consent_record`, AES-256-GCM PII encryption & masking |

## Quick Start

### 1. Run Tests
```bash
./mvnw test
```

### 2. Run the Backend Server (PostgreSQL 16 via Docker)
```bash
/Applications/Docker.app/Contents/Resources/bin/docker compose up -d postgres
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

### 3. Run the Enterprise SaaS Frontend (`http://localhost:5173`)
In a second terminal tab, start the frontend dev/proxy server:
```bash
node frontend/server.js
```
Then open **[http://localhost:5173](http://localhost:5173)** in your browser (or `http://localhost:8080` after restarting Spring Boot).

- **Swagger UI / OpenAPI 3:** `http://localhost:8080/swagger-ui.html`
- **Default Demo Credentials (Auto-seeded on startup):**
  - **Tenant Code:** `ACME-INSURE`
  - **Super Admin:** `admin@acmeinsure.com` / `Admin@123`
  - **Senior Underwriter:** `underwriter@acmeinsure.com` / `Admin@123`
  - **Senior Claims Officer:** `adjudicator@acmeinsure.com` / `Admin@123`

