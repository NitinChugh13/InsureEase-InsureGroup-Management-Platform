-- ============================================================================
-- InsureEase: InsureGroup Management Platform (v1.1)
-- Flyway Migration V1: Core Multi-Tenant Schema (33 Tables)
-- Supports Group Health (GMC), Group Term Life (GTL), Group Personal Accident (GPA)
-- ============================================================================

-- 1. TENANT & IAM MODULE (M1)
CREATE TABLE tenant (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    tenant_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    subscription_plan VARCHAR(32) NOT NULL,
    config_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    failed_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    policyholder_id UUID,
    member_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_user_tenant_email UNIQUE (tenant_id, email)
);

CREATE TABLE user_role (
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role_code VARCHAR(64) NOT NULL,
    PRIMARY KEY (user_id, role_code)
);

CREATE TABLE refresh_token (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- 2. PRODUCT & RATE CONFIGURATION MODULE (M2)
CREATE TABLE product (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    product_type VARCHAR(32) NOT NULL,
    description TEXT,
    gst_pct NUMERIC(5, 2) NOT NULL DEFAULT 18.00,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_product_tenant_code UNIQUE (tenant_id, code)
);

CREATE TABLE plan (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    product_id UUID NOT NULL REFERENCES product(id),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    cover_basis VARCHAR(32) NOT NULL,
    si_basis VARCHAR(32) NOT NULL,
    salary_multiple NUMERIC(8, 2),
    min_sum_insured NUMERIC(19, 2) NOT NULL,
    max_sum_insured NUMERIC(19, 2) NOT NULL,
    free_cover_limit NUMERIC(19, 2),
    rate_unit VARCHAR(32) NOT NULL,
    copay_pct NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    deductible_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    initial_waiting_days INTEGER NOT NULL DEFAULT 30,
    ped_waiting_days INTEGER NOT NULL DEFAULT 365,
    max_dependents INTEGER NOT NULL DEFAULT 5,
    max_child_age INTEGER NOT NULL DEFAULT 25,
    max_parent_age INTEGER NOT NULL DEFAULT 80,
    max_entry_age INTEGER NOT NULL DEFAULT 65,
    floater_discount_pct NUMERIC(5, 2) NOT NULL DEFAULT 15.00,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_plan_tenant_code UNIQUE (tenant_id, code)
);

CREATE TABLE benefit (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    plan_id UUID NOT NULL REFERENCES plan(id) ON DELETE CASCADE,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    benefit_type VARCHAR(64) NOT NULL,
    limit_type VARCHAR(32) NOT NULL,
    limit_value NUMERIC(19, 4) NOT NULL,
    waiting_days INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE rate_table (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    plan_id UUID NOT NULL REFERENCES plan(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    version_no INTEGER NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    status VARCHAR(32) NOT NULL,
    loading_config_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE rate_row (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    rate_table_id UUID NOT NULL REFERENCES rate_table(id) ON DELETE CASCADE,
    age_from INTEGER NOT NULL,
    age_to INTEGER NOT NULL,
    gender VARCHAR(16) NOT NULL DEFAULT 'ANY',
    relation VARCHAR(32) NOT NULL DEFAULT 'ANY',
    occupation_class VARCHAR(32) NOT NULL DEFAULT 'ALL',
    si_slab NUMERIC(19, 2),
    base_rate NUMERIC(19, 4) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE underwriting_rule_def (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    plan_id UUID NOT NULL REFERENCES plan(id) ON DELETE CASCADE,
    rule_code VARCHAR(64) NOT NULL,
    description VARCHAR(255) NOT NULL,
    metric VARCHAR(64) NOT NULL,
    operator VARCHAR(16) NOT NULL,
    threshold_value NUMERIC(19, 2) NOT NULL,
    action VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

-- 3. QUOTATION & UNDERWRITING MODULE (M3)
CREATE TABLE policyholder (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    code VARCHAR(64) NOT NULL,
    legal_name VARCHAR(255) NOT NULL,
    org_type VARCHAR(64) NOT NULL,
    industry_code VARCHAR(64) NOT NULL,
    gstin VARCHAR(32) NOT NULL,
    pan_encrypted VARCHAR(512) NOT NULL,
    state_code VARCHAR(16) NOT NULL,
    address_line VARCHAR(512),
    contact_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    contact_mobile_encrypted VARCHAR(512),
    kyc_status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_policyholder_tenant_code UNIQUE (tenant_id, code)
);

CREATE TABLE quote (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    quote_no VARCHAR(64) NOT NULL,
    policyholder_id UUID NOT NULL REFERENCES policyholder(id),
    plan_id UUID NOT NULL REFERENCES plan(id),
    status VARCHAR(32) NOT NULL,
    current_version INTEGER NOT NULL DEFAULT 1,
    total_lives INTEGER NOT NULL DEFAULT 0,
    employee_count INTEGER NOT NULL DEFAULT 0,
    dependent_count INTEGER NOT NULL DEFAULT 0,
    invalid_row_count INTEGER NOT NULL DEFAULT 0,
    total_requested_si NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    total_approved_si NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    base_premium NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    group_discount_pct NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    loading_pct NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    uw_adjustment_pct NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    net_premium NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    gst_pct NUMERIC(5, 2) NOT NULL DEFAULT 18.00,
    gst_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    total_premium NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    valid_till DATE NOT NULL,
    parent_policy_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_quote_tenant_no UNIQUE (tenant_id, quote_no)
);

CREATE TABLE quote_version (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    quote_id UUID NOT NULL REFERENCES quote(id) ON DELETE CASCADE,
    version_no INTEGER NOT NULL,
    base_premium NUMERIC(19, 2) NOT NULL,
    loading_pct NUMERIC(5, 2) NOT NULL,
    discount_pct NUMERIC(5, 2) NOT NULL,
    net_premium NUMERIC(19, 2) NOT NULL,
    gst_amount NUMERIC(19, 2) NOT NULL,
    total_premium NUMERIC(19, 2) NOT NULL,
    remarks TEXT,
    actor_role VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE quote_member (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    quote_id UUID NOT NULL REFERENCES quote(id) ON DELETE CASCADE,
    row_no INTEGER NOT NULL,
    employee_code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    dob DATE NOT NULL,
    age INTEGER NOT NULL,
    gender VARCHAR(16) NOT NULL,
    relation VARCHAR(32) NOT NULL,
    occupation_class VARCHAR(32) NOT NULL DEFAULT 'CLASS_I',
    annual_ctc NUMERIC(19, 2),
    requested_si NUMERIC(19, 2) NOT NULL,
    approved_si NUMERIC(19, 2) NOT NULL,
    excess_fcl_si NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    uw_status VARCHAR(32) NOT NULL,
    base_premium NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    email VARCHAR(255),
    mobile VARCHAR(64),
    nominee_name VARCHAR(255),
    nominee_relation VARCHAR(64),
    nominee_dob DATE,
    nominee_share_pct NUMERIC(5, 2),
    appointee_name VARCHAR(255),
    valid BOOLEAN NOT NULL DEFAULT TRUE,
    validation_errors TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE underwriting_case (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    quote_id UUID NOT NULL REFERENCES quote(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    triggered_rules TEXT,
    risk_score INTEGER NOT NULL DEFAULT 0,
    requires_senior_approval BOOLEAN NOT NULL DEFAULT FALSE,
    decision VARCHAR(32),
    approved_loading_pct NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    approved_discount_pct NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    remarks TEXT,
    underwriter_id UUID,
    senior_underwriter_id UUID,
    decided_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

-- 4. POLICY ADMINISTRATION MODULE (M4)
CREATE TABLE master_policy (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    policy_no VARCHAR(64) NOT NULL,
    quote_id UUID NOT NULL REFERENCES quote(id),
    policyholder_id UUID NOT NULL REFERENCES policyholder(id),
    plan_id UUID NOT NULL REFERENCES plan(id),
    tpa_tenant_id UUID,
    parent_policy_id UUID,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    grace_period_days INTEGER NOT NULL DEFAULT 30,
    status VARCHAR(32) NOT NULL,
    payment_frequency VARCHAR(32) NOT NULL DEFAULT 'ANNUAL',
    total_sum_insured NUMERIC(19, 2) NOT NULL,
    net_annual_premium NUMERIC(19, 2) NOT NULL,
    gst_amount NUMERIC(19, 2) NOT NULL,
    total_annual_premium NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_policy_tenant_no UNIQUE (tenant_id, policy_no)
);

CREATE TABLE member (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    policy_id UUID NOT NULL REFERENCES master_policy(id) ON DELETE CASCADE,
    employee_code VARCHAR(64) NOT NULL,
    member_no VARCHAR(64) NOT NULL,
    certificate_no VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    dob_encrypted VARCHAR(512) NOT NULL,
    age INTEGER NOT NULL,
    gender VARCHAR(16) NOT NULL,
    email VARCHAR(255),
    mobile_encrypted VARCHAR(512),
    occupation_class VARCHAR(32) NOT NULL DEFAULT 'CLASS_I',
    annual_ctc NUMERIC(19, 2),
    requested_si NUMERIC(19, 2) NOT NULL,
    sum_insured NUMERIC(19, 2) NOT NULL,
    utilized_si NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    annual_member_premium NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    uw_status VARCHAR(32) NOT NULL,
    coverage_start_date DATE NOT NULL,
    coverage_end_date DATE NOT NULL,
    continuous_cover_since DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_member_policy_emp UNIQUE (policy_id, employee_code),
    CONSTRAINT uk_member_tenant_no UNIQUE (tenant_id, member_no)
);

CREATE TABLE dependent (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    member_id UUID NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    dependent_no VARCHAR(64) NOT NULL,
    relation VARCHAR(32) NOT NULL,
    name VARCHAR(255) NOT NULL,
    dob_encrypted VARCHAR(512) NOT NULL,
    age INTEGER NOT NULL,
    gender VARCHAR(16) NOT NULL,
    individual_si NUMERIC(19, 2),
    utilized_si NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    annual_dependent_premium NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    coverage_start_date DATE NOT NULL,
    coverage_end_date DATE NOT NULL,
    continuous_cover_since DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE nominee (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    member_id UUID NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    relation VARCHAR(64) NOT NULL,
    dob DATE NOT NULL,
    share_pct NUMERIC(5, 2) NOT NULL,
    appointee_name VARCHAR(255),
    appointee_relation VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE endorsement (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    endorsement_no VARCHAR(64) NOT NULL,
    policy_id UUID NOT NULL REFERENCES master_policy(id),
    endorsement_type VARCHAR(32) NOT NULL,
    effective_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    net_premium_delta NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    gst_delta NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    total_premium_delta NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    funding_mode VARCHAR(32) NOT NULL DEFAULT 'CD_ACCOUNT',
    invoice_id UUID,
    approved_by UUID,
    approved_at TIMESTAMP WITH TIME ZONE,
    remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_endorsement_tenant_no UNIQUE (tenant_id, endorsement_no)
);

CREATE TABLE endorsement_item (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    endorsement_id UUID NOT NULL REFERENCES endorsement(id) ON DELETE CASCADE,
    action VARCHAR(32) NOT NULL,
    employee_code VARCHAR(64) NOT NULL,
    member_id UUID,
    name VARCHAR(255),
    dob DATE,
    gender VARCHAR(16),
    relation VARCHAR(32),
    occupation_class VARCHAR(32),
    annual_ctc NUMERIC(19, 2),
    new_sum_insured NUMERIC(19, 2),
    nominee_name VARCHAR(255),
    nominee_relation VARCHAR(64),
    nominee_dob DATE,
    nominee_share_pct NUMERIC(5, 2),
    days_remaining INTEGER NOT NULL DEFAULT 0,
    annual_premium_base NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    pro_rata_net_delta NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    prior_claim_exists BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(32) NOT NULL,
    validation_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

-- 5. RENEWALS MODULE (M5)
CREATE TABLE renewal_case (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    policy_id UUID NOT NULL REFERENCES master_policy(id),
    expiry_date DATE NOT NULL,
    days_to_expiry INTEGER NOT NULL,
    trigger_window VARCHAR(16) NOT NULL,
    earned_premium NUMERIC(19, 2) NOT NULL,
    paid_claims NUMERIC(19, 2) NOT NULL,
    reserved_claims NUMERIC(19, 2) NOT NULL,
    incurred_claims NUMERIC(19, 2) NOT NULL,
    claim_count INTEGER NOT NULL,
    loss_ratio_pct NUMERIC(8, 2) NOT NULL,
    recommended_rate_change_pct NUMERIC(6, 2) NOT NULL,
    renewal_quote_id UUID,
    new_policy_id UUID,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

-- 6. CLAIMS MODULE (M6)
CREATE TABLE hospital (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(128) NOT NULL,
    state VARCHAR(64) NOT NULL,
    network_flag BOOLEAN NOT NULL DEFAULT TRUE,
    bank_account_encrypted VARCHAR(512),
    ifsc_code VARCHAR(32),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_hospital_tenant_code UNIQUE (tenant_id, code)
);

CREATE TABLE claim (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    claim_no VARCHAR(64) NOT NULL,
    policy_id UUID NOT NULL REFERENCES master_policy(id),
    member_id UUID NOT NULL REFERENCES member(id),
    dependent_id UUID,
    hospital_id UUID REFERENCES hospital(id),
    claim_category VARCHAR(32) NOT NULL,
    benefit_code VARCHAR(64) NOT NULL,
    disability_pct NUMERIC(5, 2),
    disablement_weeks INTEGER,
    incident_date DATE NOT NULL,
    admission_date DATE,
    discharge_date DATE,
    is_pre_existing_disease BOOLEAN NOT NULL DEFAULT FALSE,
    diagnosis_encrypted VARCHAR(512) NOT NULL,
    claimed_amount NUMERIC(19, 2) NOT NULL,
    non_payable_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    sublimit_disallowed NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    deductible_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    copay_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    approved_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(32) NOT NULL,
    eligibility_notes TEXT,
    requires_senior_approval BOOLEAN NOT NULL DEFAULT FALSE,
    adjudicator_id UUID,
    senior_approver_id UUID,
    reason_code VARCHAR(64),
    decision_remarks TEXT,
    sla_due_at TIMESTAMP WITH TIME ZONE NOT NULL,
    decided_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_claim_tenant_no UNIQUE (tenant_id, claim_no)
);

CREATE TABLE claim_document (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    claim_id UUID NOT NULL REFERENCES claim(id) ON DELETE CASCADE,
    doc_type VARCHAR(64) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_ref VARCHAR(512) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE claim_event (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    claim_id UUID NOT NULL REFERENCES claim(id) ON DELETE CASCADE,
    event_type VARCHAR(64) NOT NULL,
    from_status VARCHAR(32),
    to_status VARCHAR(32) NOT NULL,
    actor_id UUID,
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE settlement (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    claim_id UUID NOT NULL REFERENCES claim(id) ON DELETE CASCADE,
    payee_type VARCHAR(32) NOT NULL,
    payee_name VARCHAR(255) NOT NULL,
    share_pct NUMERIC(5, 2) NOT NULL DEFAULT 100.00,
    amount NUMERIC(19, 2) NOT NULL,
    payment_mode VARCHAR(32) NOT NULL,
    utr_no VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    settled_on TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

-- 7. BILLING, CD ACCOUNT & PAYMENTS MODULE (M7)
CREATE TABLE cd_account (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    policy_id UUID NOT NULL UNIQUE REFERENCES master_policy(id),
    account_no VARCHAR(64) NOT NULL,
    current_balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    minimum_threshold NUMERIC(19, 2) NOT NULL DEFAULT 25000.00,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_cd_tenant_no UNIQUE (tenant_id, account_no)
);

CREATE TABLE cd_transaction (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    cd_account_id UUID NOT NULL REFERENCES cd_account(id) ON DELETE CASCADE,
    txn_type VARCHAR(32) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    balance_after NUMERIC(19, 2) NOT NULL,
    reference_type VARCHAR(64) NOT NULL,
    reference_id UUID,
    remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE invoice (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    invoice_no VARCHAR(64) NOT NULL,
    policy_id UUID NOT NULL REFERENCES master_policy(id),
    endorsement_id UUID,
    invoice_type VARCHAR(32) NOT NULL,
    net_amount NUMERIC(19, 2) NOT NULL,
    gst_pct NUMERIC(5, 2) NOT NULL DEFAULT 18.00,
    cgst_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    sgst_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    igst_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    gst_amount NUMERIC(19, 2) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    paid_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    due_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_invoice_tenant_no UNIQUE (tenant_id, invoice_no)
);

CREATE TABLE invoice_line (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    invoice_id UUID NOT NULL REFERENCES invoice(id) ON DELETE CASCADE,
    description VARCHAR(512) NOT NULL,
    sac_code VARCHAR(16) NOT NULL DEFAULT '997133',
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_amount NUMERIC(19, 2) NOT NULL,
    tax_pct NUMERIC(5, 2) NOT NULL DEFAULT 18.00,
    tax_amount NUMERIC(19, 2) NOT NULL,
    line_total NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE payment (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    invoice_id UUID NOT NULL REFERENCES invoice(id),
    provider VARCHAR(32) NOT NULL,
    gateway_order_id VARCHAR(128) NOT NULL,
    gateway_payment_id VARCHAR(128),
    gateway_signature VARCHAR(256),
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(8) NOT NULL DEFAULT 'INR',
    mode VARCHAR(32) NOT NULL,
    txn_ref VARCHAR(128),
    status VARCHAR(32) NOT NULL,
    paid_on TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_payment_gateway_order UNIQUE (provider, gateway_order_id)
);

CREATE TABLE ledger_entry (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    entry_group_id UUID NOT NULL,
    policy_id UUID,
    account_code VARCHAR(64) NOT NULL,
    debit_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    credit_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    ref_type VARCHAR(64) NOT NULL,
    ref_id UUID NOT NULL,
    narration VARCHAR(512),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

-- 8. NOTIFICATIONS, AUDIT & DPDP COMPLIANCE MODULE (M8, M10)
CREATE TABLE notification_log (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    channel VARCHAR(32) NOT NULL,
    template_code VARCHAR(64) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    body TEXT,
    status VARCHAR(32) NOT NULL,
    ref_type VARCHAR(64),
    ref_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    entity_name VARCHAR(64) NOT NULL,
    entity_id VARCHAR(64) NOT NULL,
    action VARCHAR(64) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    actor_id UUID,
    actor_email VARCHAR(255),
    correlation_id VARCHAR(64),
    ip_address VARCHAR(64),
    ts TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE consent_record (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    member_id UUID NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    purpose VARCHAR(64) NOT NULL,
    consent_version VARCHAR(16) NOT NULL DEFAULT 'v1.0',
    granted BOOLEAN NOT NULL,
    ip_address VARCHAR(64),
    ts TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE
);

-- 9. PERFORMANCE INDEXES
CREATE INDEX idx_policy_tenant_status ON master_policy(tenant_id, status);
CREATE INDEX idx_policy_end_date ON master_policy(tenant_id, end_date);
CREATE INDEX idx_member_policy ON member(policy_id, status);
CREATE INDEX idx_claim_policy_status ON claim(policy_id, status);
CREATE INDEX idx_invoice_policy_status ON invoice(policy_id, status);
CREATE INDEX idx_ledger_policy ON ledger_entry(tenant_id, policy_id);
CREATE INDEX idx_audit_tenant_ts ON audit_log(tenant_id, ts DESC);
