/* ==========================================================================
   InsureEase — Dynamic Multi-Page B2B Group Insurance Platform
   Pages:
     /               -> Flagship Company Landing Page (Interactive Process Videos + Analyzer)
     /how-it-works   -> Interactive Video Walkthrough & End-to-End User Guide
     /plans          -> Plan Explorer, Benefit Analyzer & Side-by-Side Comparison
     /quote-studio   -> 4-Step Quote-to-Policy & Digital E-Card Configurator
     /login          -> Role-Based Multi-Tenant Enterprise Sign-In
     /app/policies   -> Master Policies, Digital E-Cards & Mid-Term Endorsements
     /app/claims     -> Cashless, Reimbursement, GTL Death & GPA Disability Claims
     /app/billing    -> GST Invoices, Razorpay/Stripe Sandbox & Section 64VB CD Wallet
     /app/renewals   -> Portfolio Analytics, ICR Experience Renewals & Audit Trail
   ========================================================================== */

function resolveInitialPage() {
  const path = window.location.pathname || '/';
  if (path === '/how-it-works') return 'how-it-works';
  if (path === '/plans') return 'plans';
  if (path === '/quote-studio') return 'quote-studio';
  if (path === '/login') return 'login';
  if (path === '/app' || path === '/app/dashboard') return 'app-dashboard';
  if (path === '/app/products') return 'app-products';
  if (path === '/app/quotes' || path === '/app/employers') return 'app-quotes';
  if (path === '/app/policies') return 'app-policies';
  if (path === '/app/claims') return 'app-claims';
  if (path === '/app/billing') return 'app-billing';
  if (path === '/app/renewals' || path === '/app/reports') return 'app-renewals';
  if (path === '/app/audit') return 'app-audit';
  return 'home';
}

const state = {
  theme: localStorage.getItem('ie_theme') || 'dark',
  token: localStorage.getItem('ie_token') || null,
  refreshToken: localStorage.getItem('ie_refresh') || null,
  user: JSON.parse(localStorage.getItem('ie_user') || 'null'),
  currentPage: resolveInitialPage(),
  planCategoryFilter: 'ALL',

  // Interactive Process Video Cinema Player State
  cinemaChapter: 0,
  cinemaPlaying: true,
  cinemaProgress: 0, // 0 to 100 within current chapter

  // Interactive Coverage & Budget Simulator on Landing Page / Plans Page
  simEmployees: 85,
  simIndustryLoad: 1.0, // 1.0 IT, 1.25 Manufacturing, 1.60 Heavy Engineering
  simSumInsured: 500000,

  // 4-Step Quote Studio State
  wizardStep: 1,
  wizardPlanId: null,
  wizardPolicyholderId: null,
  wizardQuoteId: null,
  wizardPolicyId: null,
  wizardInvoiceId: null,
  draftRoster: [],

  // Live Backend Data
  dashboard: null,
  lossRatio: null,
  claimsTat: null,
  products: [],
  plans: [],
  selectedPlanId: null,
  policyholders: [],
  quotes: [],
  selectedQuoteId: null,
  quoteMembers: [],
  policies: [],
  selectedPolicyId: null,
  policyMembers: [],
  policyEndorsements: [],
  policyCdAccount: null,
  hospitals: [],
  claims: [],
  invoices: [],
  ledgerEntries: [],
  renewals: [],
  auditLogs: [],
  selectedProductId: null,
  workspaceNotice: null,
};

document.documentElement.setAttribute('data-theme', state.theme);

/* ==========================================================================
   CINEMA VIDEO PLAYER TIMER (Auto-advances chapters & updates scrubber)
   ========================================================================== */
let cinemaIntervalId = null;

function startCinemaTicker() {
  if (cinemaIntervalId) clearInterval(cinemaIntervalId);
  cinemaIntervalId = setInterval(() => {
    if (!state.cinemaPlaying) return;
    if (state.currentPage !== 'home' && state.currentPage !== 'how-it-works') return;

    state.cinemaProgress += 1.25;
    if (state.cinemaProgress >= 100) {
      state.cinemaProgress = 0;
      const total = (window.CINEMA_CHAPTERS && window.CINEMA_CHAPTERS.length) || 4;
      state.cinemaChapter = (state.cinemaChapter + 1) % total;
      updateCinemaDomOnly();
    } else {
      const fillEl = document.getElementById('cinema-scrubber-fill');
      const timeEl = document.getElementById('cinema-timecode');
      if (fillEl) fillEl.style.width = `${state.cinemaProgress}%`;
      if (timeEl) {
        const sec = Math.floor((state.cinemaProgress / 100) * 18);
        timeEl.textContent = `00:${String(sec).padStart(2, '0')} / 00:18`;
      }
    }
  }, 150);
}

function updateCinemaDomOnly() {
  const container = document.getElementById('cinema-player-mount');
  if (container) {
    container.innerHTML = renderProcessCinemaPlayerInner();
  }
}

window.selectCinemaChapter = function (idx) {
  state.cinemaChapter = idx;
  state.cinemaProgress = 0;
  updateCinemaDomOnly();
};

window.toggleCinemaPlay = function () {
  state.cinemaPlaying = !state.cinemaPlaying;
  updateCinemaDomOnly();
};

window.restartCinemaVideo = function () {
  state.cinemaChapter = 0;
  state.cinemaProgress = 0;
  state.cinemaPlaying = true;
  updateCinemaDomOnly();
};

window.scrubCinemaTimeline = function (event) {
  const rect = event.currentTarget.getBoundingClientRect();
  const pct = Math.max(0, Math.min(100, ((event.clientX - rect.left) / rect.width) * 100));
  state.cinemaProgress = pct;
  updateCinemaDomOnly();
};

/* ==========================================================================
   BRAND LOGO SVG (Tri-Ribbon Shield)
   ========================================================================== */
function renderBrandSvg(size = 30) {
  return `
    <svg width="${size}" height="${size}" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="ie-em-grad" x1="12" y1="8" x2="40" y2="56" gradientUnits="userSpaceOnUse">
          <stop offset="0%" stop-color="#34D399"/>
          <stop offset="100%" stop-color="#059669"/>
        </linearGradient>
        <linearGradient id="ie-cb-grad" x1="26" y1="10" x2="54" y2="54" gradientUnits="userSpaceOnUse">
          <stop offset="0%" stop-color="#38BDF8"/>
          <stop offset="50%" stop-color="#2563EB"/>
          <stop offset="100%" stop-color="#1D4ED8"/>
        </linearGradient>
      </defs>
      <path d="M32 6L10 14.5V31.5C10 45.4 19.3 55.1 32 59C36.6 57.5 40.5 55.1 43.7 51.9L37.3 45.5C35.7 47.1 33.9 48.3 32 49.2C23.7 45.7 19 39.1 19 31.2V20.4L32 15.5L38.8 18L43.5 10.6L32 6Z" fill="url(#ie-em-grad)"/>
      <path d="M32 6L54 14.5V31.5C54 42.1 48.3 50.4 40 55.3L34.9 47.9C40.7 44.2 45 38.4 45 31.2V20.4L32 15.5L25 28.2C23.1 31.6 24.5 35.8 28 37.5C31.4 39.1 35.6 37.7 37.3 34.3L42.1 25.1L49.3 29.1L44.3 38.5C40.3 45.9 31.1 48.8 23.6 45C16.1 41.2 13.3 32.1 17.2 24.6L27.1 7L32 6Z" fill="url(#ie-cb-grad)"/>
      <circle cx="32" cy="31" r="5" fill="#10B981"/>
    </svg>
  `;
}

/* ==========================================================================
   HELPERS, SVG STATUS BADGES & API CLIENT (ZERO EMOJIS)
   ========================================================================== */
function formatInr(amount) {
  const num = Number(amount || 0);
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
    minimumFractionDigits: 0,
  }).format(num);
}

function formatInrExact(amount) {
  const num = Number(amount || 0);
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
    minimumFractionDigits: 2,
  }).format(num);
}

function statusBadge(status) {
  if (!status) return `<span class="badge badge-slate">N/A</span>`;
  const s = String(status).toUpperCase();
  if (['ACTIVE', 'APPROVED', 'AUTO_APPROVED', 'SETTLED', 'PAID', 'VERIFIED', 'CONVERTED', 'ACCEPTED', 'APPLIED', 'ROLLED_OVER', 'CAPTURED'].includes(s)) {
    return `<span class="badge badge-emerald">${icon('check', 12)} ${s.replace(/_/g, ' ')}</span>`;
  }
  if (['PENDING_PAYMENT', 'UNDER_REVIEW', 'REFER_UW', 'SENIOR_UW_REQUIRED', 'MEDICAL_REQUIRED', 'GRACE_PERIOD', 'QUERY_RAISED', 'ON_HOLD_PREMIUM_PENDING', 'GENERATED', 'ISSUED', 'ORDER_CREATED', 'PENDING_SENIOR_APPROVAL', 'COUNTER_OFFERED', 'PRICED', 'QUOTED', 'CENSUS_UPLOADED'].includes(s)) {
    return `<span class="badge badge-amber">${icon('clock', 12)} ${s.replace(/_/g, ' ')}</span>`;
  }
  if (['REJECTED', 'DECLINED', 'LAPSED', 'CANCELLED', 'OVERDUE', 'SLA_BREACHED', 'EXPIRED'].includes(s)) {
    return `<span class="badge badge-crimson">${icon('x', 12)} ${s.replace(/_/g, ' ')}</span>`;
  }
  if (['HEALTH', 'GMC', 'GMC-CORP', 'CASHLESS', 'REIMBURSEMENT'].includes(s)) {
    return `<span class="badge badge-emerald">${icon('health', 12)} ${s}</span>`;
  }
  if (['LIFE', 'GTL', 'GTL-CORP', 'DEATH_BENEFIT'].includes(s)) {
    return `<span class="badge badge-cobalt">${icon('shield', 12)} ${s}</span>`;
  }
  if (['PA', 'GPA', 'GPA-CORP', 'DISABILITY_BENEFIT'].includes(s)) {
    return `<span class="badge badge-violet">${icon('accident', 12)} ${s}</span>`;
  }
  return `<span class="badge badge-slate">${s.replace(/_/g, ' ')}</span>`;
}

function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function showToast(message, type = 'success') {
  const root = document.getElementById('toast-root');
  if (!root) return;
  const el = document.createElement('div');
  el.className = `toast ${type}`;
  el.innerHTML = `
    <div style="display:flex; align-items:flex-start; gap:10px;">
      <span style="margin-top:2px; color:${type === 'error' ? '#FB7185' : type === 'warn' ? '#FBBF24' : '#34D399'};">
        ${type === 'error' ? icon('alert', 16) : type === 'warn' ? icon('clock', 16) : icon('check', 16)}
      </span>
      <div>
        <div style="font-weight:700; margin-bottom:2px;">${type === 'error' ? 'Action Required' : type === 'warn' ? 'Processing' : 'Completed'}</div>
        <div style="color:#CBD5E1; font-size:12.5px;">${escapeHtml(message)}</div>
      </div>
    </div>
    <button class="btn btn-xs btn-ghost" style="color:#94A3B8;" onclick="this.parentElement.remove()">${icon('x', 14)}</button>
  `;
  root.appendChild(el);
  setTimeout(() => {
    if (el.parentElement) el.remove();
  }, 4500);
}

async function api(path, options = {}) {
  const headers = Object.assign({ 'Content-Type': 'application/json' }, options.headers || {});
  if (state.token && !options.public) {
    headers['Authorization'] = `Bearer ${state.token}`;
  }
  const resp = await fetch(`/api/v1${path}`, {
    ...options,
    headers,
  });

  if (resp.status === 401 && !path.includes('/auth/login')) {
    logout();
    throw new Error('Your session expired. Please sign in again.');
  }

  const contentType = resp.headers.get('content-type') || '';
  if (contentType.includes('application/pdf')) {
    return resp.blob();
  }

  const text = await resp.text();
  const data = text ? JSON.parse(text) : null;
  if (!resp.ok) {
    const detail = (data && (data.detail || data.message || data.title || data.error)) || `HTTP ${resp.status}`;
    throw new Error(detail);
  }
  return data;
}

async function downloadAuthenticatedPdf(path, filename) {
  try {
    showToast(`Preparing ${filename}...`, 'warn');
    const blob = await api(path, { method: 'GET' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => URL.revokeObjectURL(url), 5000);
    showToast(`Downloaded ${filename}`);
  } catch (err) {
    showToast(err.message, 'error');
  }
}
window.downloadAuthenticatedPdf = downloadAuthenticatedPdf;

/* ==========================================================================
   MULTI-PAGE ROUTER (HTML5 History API + Instant Page Transitions)
   ========================================================================== */
const PAGE_TO_URL = {
  'home': '/',
  'how-it-works': '/how-it-works',
  'plans': '/plans',
  'quote-studio': '/quote-studio',
  'login': '/login',
  'app-dashboard': '/app/dashboard',
  'app-products': '/app/products',
  'app-quotes': '/app/quotes',
  'app-policies': '/app/policies',
  'app-claims': '/app/claims',
  'app-billing': '/app/billing',
  'app-renewals': '/app/renewals',
  'app-audit': '/app/audit',
};

window.navigatePage = function (pageKey, options = {}) {
  const targetUrl = PAGE_TO_URL[pageKey] || '/';
  state.currentPage = pageKey;
  if (!options.skipPushState && window.location.pathname !== targetUrl) {
    window.history.pushState({ pageKey }, '', targetUrl);
  }
  render();
  if (!options.preserveScroll) {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
};

// Backwards-compatible alias for any internal buttons
window.navigate = function (legacyNav) {
  const map = {
    marketplace: 'quote-studio',
    policies: 'app-policies',
    claims: 'app-claims',
    billing: 'app-billing',
    overview: 'app-renewals',
  };
  window.navigatePage(map[legacyNav] || legacyNav);
};

window.addEventListener('popstate', () => {
  state.currentPage = resolveInitialPage();
  render();
});

/* ==========================================================================
   AUTHENTICATION & INITIAL DATA BOOTSTRAP
   ========================================================================== */
async function ensureDemoSessionAndLoad() {
  if (state.token) {
    await refreshAllData();
    return;
  }
  render();
}

async function handleLogin(tenantCode, email, password) {
  try {
    const res = await api('/auth/login', {
      method: 'POST',
      public: true,
      body: JSON.stringify({
        tenantCode: tenantCode || 'ACME-INSURE',
        email,
        password,
      }),
    });
    state.token = res.accessToken;
    state.refreshToken = res.refreshToken;
    state.user = {
      userId: res.userId,
      tenantId: res.tenantId,
      tenantCode: res.tenantCode || tenantCode || 'ACME-INSURE',
      email: res.email,
      fullName: res.fullName,
      roles: Array.from(res.roles || []),
    };
    localStorage.setItem('ie_token', state.token);
    localStorage.setItem('ie_refresh', state.refreshToken);
    localStorage.setItem('ie_user', JSON.stringify(state.user));
    showToast(`Signed in as ${state.user.fullName} (${state.user.email})`);
    await refreshAllData();
    navigatePage('app-dashboard');
  } catch (err) {
    showToast(err.message || 'Invalid credentials', 'error');
  }
}

function logout() {
  state.token = null;
  state.refreshToken = null;
  state.user = null;
  localStorage.removeItem('ie_token');
  localStorage.removeItem('ie_refresh');
  localStorage.removeItem('ie_user');
  navigatePage('login');
}
window.logout = logout;

function toggleTheme() {
  state.theme = state.theme === 'light' ? 'dark' : 'light';
  document.documentElement.setAttribute('data-theme', state.theme);
  localStorage.setItem('ie_theme', state.theme);
  render();
}
window.toggleTheme = toggleTheme;

async function refreshAllData() {
  if (!state.token) {
    render();
    return;
  }
  try {
    const [
      dashboard,
      lossRatio,
      claimsTat,
      products,
      plans,
      policyholders,
      quotes,
      policies,
      hospitals,
      claims,
      invoices,
      ledgerEntries,
      renewals,
      auditLogs,
    ] = await Promise.all([
      api('/reports/dashboard').catch(() => null),
      api('/reports/loss-ratio').catch(() => null),
      api('/reports/claims-tat').catch(() => null),
      api('/products').catch(() => []),
      api('/plans').catch(() => []),
      api('/policyholders').catch(() => []),
      api('/quotes').catch(() => []),
      api('/policies').catch(() => []),
      api('/hospitals').catch(() => []),
      api('/claims').catch(() => []),
      api('/invoices').catch(() => []),
      api('/ledger-entries').catch(() => []),
      api('/renewals').catch(() => []),
      api('/audit-logs').catch(() => []),
    ]);

    state.dashboard = dashboard;
    state.lossRatio = lossRatio;
    state.claimsTat = claimsTat;
    state.products = products || [];
    state.plans = plans || [];
    state.policyholders = policyholders || [];
    state.quotes = quotes || [];
    state.policies = policies || [];
    state.hospitals = hospitals || [];
    state.claims = claims || [];
    state.invoices = invoices || [];
    state.ledgerEntries = ledgerEntries || [];
    state.renewals = renewals || [];
    state.auditLogs = auditLogs || [];

    if (!state.selectedProductId && state.products.length > 0) {
      state.selectedProductId = state.products[0].id;
    }

    if (!state.wizardPlanId && state.plans.length > 0) {
      state.wizardPlanId = state.plans[0].id;
      loadDefaultSampleRosterForPlan(state.plans[0]);
    }
    if (!state.wizardPolicyholderId && state.policyholders.length > 0) {
      state.wizardPolicyholderId = state.policyholders[0].id;
    }
    if (!state.selectedPolicyId && state.policies.length > 0) {
      await selectPolicy(state.policies[0].id, false);
    } else if (state.selectedPolicyId) {
      await selectPolicy(state.selectedPolicyId, false);
    }
    if (state.wizardQuoteId) {
      state.quoteMembers = await api(`/quotes/${state.wizardQuoteId}/members`).catch(() => []);
    }
  } catch (err) {
    console.error('Refresh error:', err);
  }
  render();
}
window.refreshAllData = refreshAllData;

async function selectPolicy(policyId, shouldRender = true) {
  state.selectedPolicyId = policyId;
  if (policyId) {
    const [members, endorsements, cdAccount] = await Promise.all([
      api(`/policies/${policyId}/members`).catch(() => []),
      api(`/policies/${policyId}/endorsements`).catch(() => []),
      api(`/policies/${policyId}/cd-account`).catch(() => null),
    ]);
    state.policyMembers = members || [];
    state.policyEndorsements = endorsements || [];
    state.policyCdAccount = cdAccount;
  }
  if (shouldRender) render();
}
window.selectPolicy = selectPolicy;

function getProductForPlan(plan) {
  if (!plan) return null;
  return state.products.find(p => p.id === plan.productId) || null;
}

function loadDefaultSampleRosterForPlan(plan) {
  const prod = getProductForPlan(plan);
  const type = prod ? prod.productType : 'HEALTH';
  if (type === 'LIFE') {
    state.draftRoster = [
      {
        employeeCode: 'EMP-101',
        name: 'Vikramaditya Rao (CEO)',
        dob: '1980-05-12',
        gender: 'M',
        relation: 'SELF',
        occupationClass: 'CLASS_I',
        annualCtc: 2500000, // 3x CTC = ₹75L (> ₹50L FCL -> Shows Medical UW option)
        sumInsured: 7500000,
        nomineeName: 'Meera Rao',
        nomineeRelation: 'SPOUSE',
        nomineeDob: '1983-08-19',
        nomineeSharePct: 100,
      },
      {
        employeeCode: 'EMP-102',
        name: 'Ananya Deshmukh (Product Lead)',
        dob: '1993-11-24',
        gender: 'F',
        relation: 'SELF',
        occupationClass: 'CLASS_I',
        annualCtc: 1200000, // 3x CTC = ₹36L (<= ₹50L FCL -> Auto-Approved)
        sumInsured: 3600000,
        nomineeName: 'Suresh Deshmukh',
        nomineeRelation: 'FATHER',
        nomineeDob: '1964-02-10',
        nomineeSharePct: 100,
      },
    ];
  } else if (type === 'PA') {
    state.draftRoster = [
      {
        employeeCode: 'EMP-201',
        name: 'Raghavendra Kulkarni (Field Engineer)',
        dob: '1988-03-15',
        gender: 'M',
        relation: 'SELF',
        occupationClass: 'CLASS_II',
        annualCtc: 1200000,
        sumInsured: 2000000,
        nomineeName: 'Kavita Kulkarni',
        nomineeRelation: 'SPOUSE',
        nomineeDob: '1990-07-20',
        nomineeSharePct: 100,
      },
      {
        employeeCode: 'EMP-202',
        name: 'Sneha Menon (Operations)',
        dob: '1995-07-09',
        gender: 'F',
        relation: 'SELF',
        occupationClass: 'CLASS_I',
        annualCtc: 900000,
        sumInsured: 1500000,
        nomineeName: 'Rajesh Menon',
        nomineeRelation: 'FATHER',
        nomineeDob: '1966-01-18',
        nomineeSharePct: 100,
      },
    ];
  } else {
    state.draftRoster = [
      {
        employeeCode: 'EMP-001',
        name: 'Siddharth Malhotra',
        dob: '1990-04-18',
        gender: 'M',
        relation: 'SELF',
        occupationClass: 'CLASS_I',
        annualCtc: 1650000,
        sumInsured: 500000,
      },
      {
        employeeCode: 'EMP-001',
        name: 'Neha Malhotra (Spouse)',
        dob: '1992-09-10',
        gender: 'F',
        relation: 'SPOUSE',
        occupationClass: 'CLASS_I',
        sumInsured: 500000,
      },
      {
        employeeCode: 'EMP-002',
        name: 'Priya Nair',
        dob: '1994-02-14',
        gender: 'F',
        relation: 'SELF',
        occupationClass: 'CLASS_I',
        annualCtc: 1400000,
        sumInsured: 500000,
      },
    ];
  }
}

/* ==========================================================================
   SHARED MARKETING HEADER & FOOTER (MULTI-PAGE NAVIGATION)
   ========================================================================== */
function renderMarketingNavbar() {
  const p = state.currentPage;
  const u = state.user;
  return `
    <header class="mkt-navbar">
      <div class="mkt-navbar-inner">
        <a class="brand-lockup" style="text-decoration:none; cursor:pointer;" onclick="navigatePage('home')">
          <div class="brand-logo-wrap" style="width:40px; height:40px; border-radius:11px;">
            ${renderBrandSvg(26)}
          </div>
          <div>
            <div class="brand-name" style="font-size:19px;">
              InsureEase <span class="brand-tag">Enterprise</span>
            </div>
            <div style="font-size:11px; color:#94A3B8;">B2B Group Benefits &amp; Actuarial Cloud</div>
          </div>
        </a>

        <nav class="mkt-nav-links" aria-label="Primary navigation">
          <button class="mkt-nav-link ${p === 'home' ? 'active' : ''}" onclick="navigatePage('home')">
            ${icon('globe', 15)} <span>Overview</span>
          </button>
          <button class="mkt-nav-link ${p === 'plans' ? 'active' : ''}" onclick="navigatePage('plans')">
            ${icon('layers', 15)} <span>Products</span>
          </button>
          <button class="mkt-nav-link ${p === 'quote-studio' ? 'active' : ''}" onclick="navigatePage('quote-studio')">
            ${icon('sparkles', 15)} <span>Quote Studio</span>
          </button>
          <button class="mkt-nav-link ${p.startsWith('app-') ? 'active' : ''}" onclick="navigatePage(state.user ? 'app-dashboard' : 'login')">
            ${icon('building', 15)} <span>Operations</span>
          </button>
        </nav>

        <div class="mkt-nav-actions">
          ${u ? `
            <button class="btn btn-sm btn-primary" onclick="navigatePage('app-dashboard')">
              ${icon('building', 15)} Open workspace ${icon('arrowRight', 14)}
            </button>
          ` : `
            <button class="btn btn-sm btn-primary" onclick="navigatePage('login')">
              ${icon('lock', 14)} Sign In
            </button>
          `}
          <button class="btn btn-sm btn-secondary" onclick="toggleTheme()" aria-label="Toggle theme">
            ${state.theme === 'light' ? `${icon('moon', 14)} Dark` : `${icon('sun', 14)} Light`}
          </button>
        </div>
      </div>
    </header>
  `;
}

function renderMarketingFooter() {
  return `
    <footer class="mkt-footer">
      <div class="mkt-footer-inner">
        <div class="mkt-footer-grid">
          <div>
            <div class="brand-lockup" style="margin-bottom:14px;">
              <div class="brand-logo-wrap" style="width:38px; height:38px;">${renderBrandSvg(24)}</div>
              <div>
                <div class="brand-name" style="font-size:18px;">InsureEase <span class="brand-tag">v1.1</span></div>
                <div style="font-size:11.5px; color:#64748B;">IRDAI &amp; DPDP Act 2023 Compliant Platform</div>
              </div>
            </div>
            <p style="font-size:13px; line-height:1.65; color:#94A3B8; max-width:380px;">
              End-to-end corporate group insurance platform covering Group Medical (GMC), Group Term Life (GTL) with ₹50L Free Cover Limit, and Group Personal Accident (GPA) from instant census quoting to 1-hour cashless claims.
            </p>
          </div>

          <div>
            <div class="mkt-footer-col-title">Platform Pages</div>
            <ul class="mkt-footer-links">
              <li><a onclick="navigatePage('home')">Company Home &amp; Overview</a></li>
              <li><a onclick="navigatePage('how-it-works')">Animated Video Process Guide</a></li>
              <li><a onclick="navigatePage('plans')">Explore &amp; Analyze Plans</a></li>
              <li><a onclick="navigatePage('quote-studio')">4-Step Quote &amp; Policy Studio</a></li>
            </ul>
          </div>

          <div>
            <div class="mkt-footer-col-title">Enterprise Portal</div>
            <ul class="mkt-footer-links">
              <li><a onclick="navigatePage('app-policies')">Master Policies &amp; Digital E-Cards</a></li>
              <li><a onclick="navigatePage('app-claims')">Cashless &amp; Reimbursement Claims</a></li>
              <li><a onclick="navigatePage('app-billing')">Section 64VB CD Float Wallet</a></li>
              <li><a onclick="navigatePage('app-renewals')">ICR Experience-Rated Renewals</a></li>
            </ul>
          </div>

          <div>
            <div class="mkt-footer-col-title">Compliance &amp; Architecture</div>
            <ul class="mkt-footer-links">
              <li><a onclick="navigatePage('how-it-works')">Section 64VB Advance Premium</a></li>
              <li><a onclick="navigatePage('how-it-works')">BR-12 Free Cover Limit (₹50L)</a></li>
              <li><a onclick="navigatePage('how-it-works')">HMAC-SHA256 Gateway Webhooks</a></li>
              <li><a onclick="navigatePage('login')">Multi-Tenant Role Switcher</a></li>
            </ul>
          </div>
        </div>

        <div style="padding-top:24px; display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:12px; font-size:12px; color:#64748B;">
          <div>© 2026 InsureEase Technologies Pvt. Ltd. Powered by Spring Boot 3.4 &amp; PostgreSQL 16.</div>
          <div class="mono">GST SAC 997133 · 18% IGST/CGST+SGST · ISO 27001 Ready</div>
        </div>
      </div>
    </footer>
  `;
}

/* ==========================================================================
   INTERACTIVE ANIMATED PROCESS VIDEO CINEMA COMPONENT
   ========================================================================== */
function renderProcessCinemaPlayer() {
  return `
    <div id="cinema-player-mount">
      ${renderProcessCinemaPlayerInner()}
    </div>
  `;
}

function renderProcessCinemaPlayerInner() {
  const chapters = window.CINEMA_CHAPTERS || [];
  const activeIdx = state.cinemaChapter || 0;
  const ch = chapters[activeIdx] || chapters[0];
  const sec = Math.floor((state.cinemaProgress / 100) * 18);
  const timeStr = `00:${String(sec).padStart(2, '0')} / 00:18`;

  const chapterTargetPage = [
    'plans',
    'quote-studio',
    'app-billing',
    'app-claims',
  ][activeIdx] || 'quote-studio';

  return `
    <div class="cinema-shell">
      <!-- Top Window Chrome -->
      <div class="cinema-topbar">
        <div style="display:flex; align-items:center; gap:12px;">
          <div class="cinema-window-dots">
            <span class="cinema-dot" style="background:#EF4444;"></span>
            <span class="cinema-dot" style="background:#F59E0B;"></span>
            <span class="cinema-dot" style="background:#10B981;"></span>
          </div>
          <span class="mono" style="font-size:12px; color:#E2E8F0; font-weight:600; display:inline-flex; align-items:center; gap:6px;">
            ${icon('video', 15)} InsureEase Interactive Process Cinema — Chapter ${escapeHtml(ch.step)}: ${escapeHtml(ch.title)}
          </span>
        </div>
        <div style="display:flex; align-items:center; gap:10px;">
          <span class="badge badge-emerald" style="font-size:10.5px;">
            ${state.cinemaPlaying ? `${icon('activity', 12)} AUTO-PLAYING MOTION VIDEO` : `${icon('pause', 12)} PAUSED`}
          </span>
          <span id="cinema-timecode" class="mono" style="font-size:11.5px; color:#94A3B8;">${timeStr}</span>
        </div>
      </div>

      <!-- 4 Interactive Video Chapter Tabs -->
      <div class="cinema-chapter-tabs">
        ${chapters.map((c, idx) => `
          <button type="button" class="cinema-chapter-tab ${idx === activeIdx ? 'active' : ''}" onclick="selectCinemaChapter(${idx})">
            <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:3px;">
              <span class="mono" style="font-size:10.5px; font-weight:700; color:${idx === activeIdx ? '#38BDF8' : '#64748B'};">
                VIDEO ${escapeHtml(c.step)}
              </span>
              <span class="mono" style="font-size:10px; color:#64748B;">${escapeHtml(c.duration)}</span>
            </div>
            <div style="font-size:13px; font-weight:700; color:${idx === activeIdx ? '#FFFFFF' : '#CBD5E1'};">
              ${escapeHtml(c.title)}
            </div>
          </button>
        `).join('')}
      </div>

      <!-- Animated Vector Motion Viewport -->
      <div class="cinema-viewport">
        ${window.renderCinemaSceneSvg ? window.renderCinemaSceneSvg(activeIdx) : ''}
      </div>

      <!-- Video Scrubber & Playback Controls -->
      <div class="cinema-controls-bar">
        <div class="cinema-scrubber-track" onclick="scrubCinemaTimeline(event)" title="Click anywhere on timeline to scrub">
          <div id="cinema-scrubber-fill" class="cinema-scrubber-fill" style="width:${state.cinemaProgress}%;"></div>
        </div>
        <div style="display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:12px;">
          <div style="display:flex; align-items:center; gap:8px;">
            <button type="button" class="btn btn-xs btn-primary" onclick="toggleCinemaPlay()">
              ${state.cinemaPlaying ? `${icon('pause', 13)} Pause Video` : `${icon('play', 13)} Play Video`}
            </button>
            <button type="button" class="btn btn-xs btn-secondary" style="background:rgba(255,255,255,0.08); color:#F8FAFC; border-color:rgba(255,255,255,0.15);" onclick="restartCinemaVideo()">
              ${icon('refresh', 13)} Restart All 4 Chapters
            </button>
            <button type="button" class="btn btn-xs btn-secondary" style="background:rgba(255,255,255,0.08); color:#F8FAFC; border-color:rgba(255,255,255,0.15);" onclick="selectCinemaChapter(${(activeIdx + 1) % chapters.length})">
              Next Process Video ${icon('arrowRight', 13)}
            </button>
          </div>
          <div style="font-size:12px; color:#94A3B8;">
            Click any chapter tab above to watch how that workflow operates in real time
          </div>
        </div>
      </div>

      <!-- Synchronized Narration & User Action Bar -->
      <div class="cinema-narration-box">
        <div>
          <div style="font-size:11px; font-weight:700; text-transform:uppercase; letter-spacing:0.07em; color:#38BDF8; margin-bottom:4px;">
            What You Are Watching (${escapeHtml(ch.subtitle)})
          </div>
          <p style="font-size:13.5px; color:#E2E8F0; line-height:1.6;">
            ${escapeHtml(ch.narration)}
          </p>
        </div>
        <div style="background:rgba(16, 185, 129, 0.1); border:1px solid rgba(16, 185, 129, 0.28); border-radius:12px; padding:14px 16px; display:flex; flex-direction:column; justify-content:space-between; gap:10px;">
          <div>
            <div style="font-size:11px; font-weight:700; text-transform:uppercase; letter-spacing:0.06em; color:#34D399; margin-bottom:3px;">
              How This Helps You Analyze &amp; Decide
            </div>
            <div style="font-size:12.5px; color:#CBD5E1; line-height:1.5;">
              ${escapeHtml(ch.howItHelps)}
            </div>
          </div>
          <div>
            <button type="button" class="btn btn-xs btn-emerald" onclick="navigatePage('${chapterTargetPage}')">
              Try Step ${escapeHtml(ch.step)} Live Now ${icon('arrowRight', 13)}
            </button>
          </div>
        </div>
      </div>
    </div>
  `;
}

/* ==========================================================================
   PAGE 1: FLAGSHIP COMPANY LANDING PAGE (/)
   ========================================================================== */
function renderCompanyHomePage() {
  // Interactive Simulator Math
  const emp = Number(state.simEmployees || 85);
  const indFactor = Number(state.simIndustryLoad || 1.0);
  const si = Number(state.simSumInsured || 500000);
  const discPct = emp >= 250 ? 15 : emp >= 100 ? 10 : emp >= 50 ? 5 : 0;

  const gmcRawPerEmp = (si / 500000) * 6200 * indFactor;
  const gmcAnnualNet = Math.round(emp * gmcRawPerEmp * (1 - discPct / 100));
  const gtlAnnualNet = Math.round(emp * 4100 * indFactor * (1 - discPct / 100));
  const gpaAnnualNet = Math.round(emp * 1350 * indFactor * (1 - discPct / 100));

  return `
    ${renderMarketingNavbar()}

    <!-- HERO SECTION WITH ANIMATED PROCESS VIDEO CINEMA -->
    <section class="company-hero">
      <div class="company-hero-inner">
        <div class="hero-top-grid">
          <div>
            <div class="hero-eyebrow">
              ${icon('sparkles', 14)}
              <span>Interactive B2B Group Insurance Cloud · IRDAI &amp; Section 64VB Compliant</span>
            </div>
            <h1 class="company-hero-title">
              Analyze Plans, Price Your Census &amp; Issue <span>Group Policies</span> with Live Animated Guidance.
            </h1>
            <p class="company-hero-lead">
              InsureEase guides HR leaders, CFOs, and Benefits Brokers from initial plan discovery and ₹50L Free Cover Limit (FCL) analysis to instant GST quoting, Section 64VB Cash Deposit checkout, Digital Member E-Cards, and 1-hour cashless claims.
            </p>

            <div class="hero-cta-row">
              <button class="btn btn-primary btn-lg" onclick="navigatePage('quote-studio')">
                ${icon('sparkles', 17)} Launch 4-Step Quote &amp; Policy Studio ${icon('arrowRight', 16)}
              </button>
              <button class="btn btn-emerald btn-lg" onclick="navigatePage('plans')">
                ${icon('layers', 17)} Compare Plans &amp; Rate Cards
              </button>
              <button class="btn btn-secondary btn-lg" style="background:rgba(255,255,255,0.08); color:#FFFFFF; border-color:rgba(255,255,255,0.2);" onclick="navigatePage('how-it-works')">
                ${icon('video', 17)} Full Video Walkthrough
              </button>
            </div>

            <div class="hero-trust-strip">
              <div class="trust-metric">
                <div class="trust-metric-val">3 Product Lines</div>
                <div class="trust-metric-lbl">GMC Health · GTL Life · GPA Accident</div>
              </div>
              <div class="trust-metric">
                <div class="trust-metric-val">₹50,00,000</div>
                <div class="trust-metric-lbl">Auto Free Cover Limit (Zero Medicals)</div>
              </div>
              <div class="trust-metric">
                <div class="trust-metric-val">&lt; 60 Mins</div>
                <div class="trust-metric-lbl">Cashless Pre-Auth SLA Turnaround</div>
              </div>
              <div class="trust-metric">
                <div class="trust-metric-val">Section 64VB</div>
                <div class="trust-metric-lbl">Live CD Float Wallet &amp; Pro-Rata Ledger</div>
              </div>
            </div>
          </div>

          <!-- Quick Visual Architecture Card on Right of Hero -->
          <div style="background:rgba(15, 23, 42, 0.78); border:1px solid rgba(56, 189, 248, 0.25); border-radius:20px; padding:24px; box-shadow:0 20px 50px rgba(0,0,0,0.45);">
            <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:14px;">
              <span style="font-size:12px; font-weight:700; text-transform:uppercase; letter-spacing:0.07em; color:#38BDF8; display:inline-flex; align-items:center; gap:6px;">
                ${icon('compass', 15)} End-to-End Platform Navigator
              </span>
              <span class="badge badge-emerald">4 Guided Stages</span>
            </div>
            <p style="font-size:13px; color:#94A3B8; margin-bottom:16px;">
              New to group insurance? Click any stage below to watch its animated video or jump directly into that workflow:
            </p>

            <div style="display:flex; flex-direction:column; gap:10px;">
              <div style="background:rgba(30, 41, 59, 0.7); border:1px solid rgba(255,255,255,0.08); border-radius:12px; padding:12px 14px; display:flex; align-items:center; justify-content:space-between; gap:12px; cursor:pointer;" onclick="selectCinemaChapter(0); document.getElementById('cinema-player-mount').scrollIntoView({behavior:'smooth'});">
                <div style="display:flex; align-items:center; gap:12px;">
                  <div style="width:36px; height:36px; border-radius:10px; background:rgba(16,185,129,0.18); color:#34D399; display:flex; align-items:center; justify-content:center;">
                    ${icon('health', 18)}
                  </div>
                  <div>
                    <div style="font-weight:700; font-size:13.5px; color:#FFFFFF;">Stage 1: See &amp; Compare Plans</div>
                    <div style="font-size:11.5px; color:#94A3B8;">Inspect GMC, GTL &amp; GPA rate cards, room rent &amp; ₹50L FCL</div>
                  </div>
                </div>
                <span style="color:#38BDF8;">${icon('play', 16)}</span>
              </div>

              <div style="background:rgba(30, 41, 59, 0.7); border:1px solid rgba(255,255,255,0.08); border-radius:12px; padding:12px 14px; display:flex; align-items:center; justify-content:space-between; gap:12px; cursor:pointer;" onclick="selectCinemaChapter(1); document.getElementById('cinema-player-mount').scrollIntoView({behavior:'smooth'});">
                <div style="display:flex; align-items:center; gap:12px;">
                  <div style="width:36px; height:36px; border-radius:10px; background:rgba(59,130,246,0.2); color:#60A5FA; display:flex; align-items:center; justify-content:center;">
                    ${icon('calculator', 18)}
                  </div>
                  <div>
                    <div style="font-weight:700; font-size:13.5px; color:#FFFFFF;">Stage 2: Upload Census &amp; Instant Quote</div>
                    <div style="font-size:11.5px; color:#94A3B8;">Auto age-band rating, FCL check &amp; 18% GST proposal PDF</div>
                  </div>
                </div>
                <span style="color:#38BDF8;">${icon('play', 16)}</span>
              </div>

              <div style="background:rgba(30, 41, 59, 0.7); border:1px solid rgba(255,255,255,0.08); border-radius:12px; padding:12px 14px; display:flex; align-items:center; justify-content:space-between; gap:12px; cursor:pointer;" onclick="selectCinemaChapter(2); document.getElementById('cinema-player-mount').scrollIntoView({behavior:'smooth'});">
                <div style="display:flex; align-items:center; gap:12px;">
                  <div style="width:36px; height:36px; border-radius:10px; background:rgba(124,58,237,0.2); color:#C4B5FD; display:flex; align-items:center; justify-content:center;">
                    ${icon('idCard', 18)}
                  </div>
                  <div>
                    <div style="font-weight:700; font-size:13.5px; color:#FFFFFF;">Stage 3: Section 64VB Payment &amp; E-Cards</div>
                    <div style="font-size:11.5px; color:#94A3B8;">Razorpay/Stripe checkout, Master Policy &amp; Member COI PDFs</div>
                  </div>
                </div>
                <span style="color:#38BDF8;">${icon('play', 16)}</span>
              </div>

              <div style="background:rgba(30, 41, 59, 0.7); border:1px solid rgba(255,255,255,0.08); border-radius:12px; padding:12px 14px; display:flex; align-items:center; justify-content:space-between; gap:12px; cursor:pointer;" onclick="selectCinemaChapter(3); document.getElementById('cinema-player-mount').scrollIntoView({behavior:'smooth'});">
                <div style="display:flex; align-items:center; gap:12px;">
                  <div style="width:36px; height:36px; border-radius:10px; background:rgba(245,158,11,0.2); color:#FBBF24; display:flex; align-items:center; justify-content:center;">
                    ${icon('activity', 18)}
                  </div>
                  <div>
                    <div style="font-weight:700; font-size:13.5px; color:#FFFFFF;">Stage 4: Claims &amp; ICR Renewal Pricing</div>
                    <div style="font-size:11.5px; color:#94A3B8;">Automated co-pay/sub-limit cuts &amp; T-60 Loss Ratio renewals</div>
                  </div>
                </div>
                <span style="color:#38BDF8;">${icon('play', 16)}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- FULL-WIDTH INTERACTIVE PROCESS VIDEO CINEMA -->
        ${renderProcessCinemaPlayer()}
      </div>
    </section>

    <!-- SECTION 2: 4 ANIMATED STEP-BY-STEP VIDEO CARDS EXPLAINING HOW TO USE THE APP -->
    <section class="mkt-section">
      <div style="display:flex; align-items:flex-end; justify-content:space-between; flex-wrap:wrap; gap:16px;">
        <div>
          <div class="mkt-eyebrow">${icon('video', 14)} <span>Animated Step-by-Step User Guide</span></div>
          <h2 class="mkt-h2">How to Analyze, Quote &amp; Manage Your Group Policy From Start to Finish</h2>
          <p class="mkt-sub">
            Each animated motion screen below shows exactly how the feature works, what data you need, and how it helps you make smarter employee benefit decisions.
          </p>
        </div>
        <button class="btn btn-primary" onclick="navigatePage('how-it-works')">
          ${icon('bookOpen', 15)} Open Full Interactive Walkthrough Page ${icon('arrowRight', 14)}
        </button>
      </div>

      <div class="step-video-grid">
        <!-- Animated Step 1 -->
        <div class="step-video-card">
          <div class="step-video-screen">
            ${window.renderMiniStepAnimation ? window.renderMiniStepAnimation(1) : ''}
          </div>
          <div class="step-video-body">
            <div>
              <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:8px;">
                <span class="badge badge-emerald">${icon('layers', 12)} STEP 01 · DISCOVER &amp; ANALYZE</span>
                <span class="mono" style="font-size:11.5px; color:var(--text-muted);">Page: /plans</span>
              </div>
              <h3 style="font-family:var(--font-display); font-size:20px; font-weight:800; margin-bottom:8px;">
                How to See &amp; Compare Plans (GMC, GTL &amp; GPA)
              </h3>
              <p style="font-size:13.5px; color:var(--text-secondary);">
                Before buying a policy, HR and Finance teams need clarity on sub-limits, waiting periods, and age-band rates.
              </p>
              <ul class="how-to-checklist">
                <li>
                  <span style="color:var(--accent-emerald); margin-top:2px;">${icon('check', 15)}</span>
                  <span><strong>Filter by Category:</strong> Switch between Group Health (Family Floater), Group Term Life (3x Salary), and Personal Accident (Table A–D).</span>
                </li>
                <li>
                  <span style="color:var(--accent-emerald); margin-top:2px;">${icon('check', 15)}</span>
                  <span><strong>Inspect the Published Rate Card:</strong> Click <em>Inspect Rate Table</em> on any plan to see exact age-band pricing (18–35, 36–45, 46–55, 56–65 yrs) and sub-limits.</span>
                </li>
                <li>
                  <span style="color:var(--accent-emerald); margin-top:2px;">${icon('check', 15)}</span>
                  <span><strong>How It Helps You:</strong> Prevents hidden hospital room-rent deductions and reveals how much cover is approved without medical tests (₹50L FCL).</span>
                </li>
              </ul>
            </div>
            <div style="display:flex; gap:10px; padding-top:14px; border-top:1px solid var(--border-subtle);">
              <button class="btn btn-primary" style="width:100%;" onclick="navigatePage('plans')">
                ${icon('search', 15)} Open Plan Explorer &amp; Rate Analyzer ${icon('arrowRight', 14)}
              </button>
            </div>
          </div>
        </div>

        <!-- Animated Step 2 -->
        <div class="step-video-card">
          <div class="step-video-screen">
            ${window.renderMiniStepAnimation ? window.renderMiniStepAnimation(2) : ''}
          </div>
          <div class="step-video-body">
            <div>
              <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:8px;">
                <span class="badge badge-cobalt">${icon('calculator', 12)} STEP 02 · CENSUS &amp; INSTANT QUOTE</span>
                <span class="mono" style="font-size:11.5px; color:var(--text-muted);">Page: /quote-studio</span>
              </div>
              <h3 style="font-family:var(--font-display); font-size:20px; font-weight:800; margin-bottom:8px;">
                How to Upload Your Team &amp; Calculate an Actuarial Quote
              </h3>
              <p style="font-size:13.5px; color:var(--text-secondary);">
                No waiting days for broker spreadsheets. Our rating engine prices every employee and dependent in milliseconds.
              </p>
              <ul class="how-to-checklist">
                <li>
                  <span style="color:var(--accent-emerald); margin-top:2px;">${icon('check', 15)}</span>
                  <span><strong>Load Sample or Custom Roster:</strong> Use our 1-click sample tech roster or add employees with DOB, gender, relation, and Annual CTC.</span>
                </li>
                <li>
                  <span style="color:var(--accent-emerald); margin-top:2px;">${icon('check', 15)}</span>
                  <span><strong>Automatic ₹50L Free Cover Limit (BR-12):</strong> If an executive's 3x CTC is ₹75L, ₹50L is approved immediately and ₹25L is flagged for 1-click Medical UW approval.</span>
                </li>
                <li>
                  <span style="color:var(--accent-emerald); margin-top:2px;">${icon('check', 15)}</span>
                  <span><strong>How It Helps You:</strong> Gives you a downloadable, board-ready PDF Quote Proposal with exact base premium, group discount, and 18% GST.</span>
                </li>
              </ul>
            </div>
            <div style="display:flex; gap:10px; padding-top:14px; border-top:1px solid var(--border-subtle);">
              <button class="btn btn-primary" style="width:100%;" onclick="navigatePage('quote-studio')">
                ${icon('sparkles', 15)} Launch 4-Step Quote Studio ${icon('arrowRight', 14)}
              </button>
            </div>
          </div>
        </div>

        <!-- Animated Step 3 -->
        <div class="step-video-card">
          <div class="step-video-screen">
            ${window.renderMiniStepAnimation ? window.renderMiniStepAnimation(3) : ''}
          </div>
          <div class="step-video-body">
            <div>
              <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:8px;">
                <span class="badge badge-violet">${icon('idCard', 12)} STEP 03 · 64VB PAYMENT &amp; E-CARDS</span>
                <span class="mono" style="font-size:11.5px; color:var(--text-muted);">Page: /app/policies</span>
              </div>
              <h3 style="font-family:var(--font-display); font-size:20px; font-weight:800; margin-bottom:8px;">
                How Payment Activates Your Master Policy &amp; Member E-Cards
              </h3>
              <p style="font-size:13.5px; color:var(--text-secondary);">
                Under IRDAI Section 64VB, risk cover begins the moment advance premium or Cash Deposit (CD) float is verified.
              </p>
              <ul class="how-to-checklist">
                <li>
                  <span style="color:var(--accent-emerald); margin-top:2px;">${icon('check', 15)}</span>
                  <span><strong>Pay via Sandbox Gateway or NEFT:</strong> Test live HMAC-SHA256 webhook verification with Razorpay or Stripe Sandbox, or record an instant NEFT UTR.</span>
                </li>
                <li>
                  <span style="color:var(--accent-emerald); margin-top:2px;">${icon('check', 15)}</span>
                  <span><strong>Instant Master Policy &amp; Digital E-Cards:</strong> Upon payment, the policy flips to <code>ACTIVE</code> and generates individual Member E-Card PDFs.</span>
                </li>
                <li>
                  <span style="color:var(--accent-emerald); margin-top:2px;">${icon('check', 15)}</span>
                  <span><strong>How It Helps You:</strong> Add new joiners mid-term in 1 click using your Section 64VB CD Float Wallet with automatic pro-rata day calculation.</span>
                </li>
              </ul>
            </div>
            <div style="display:flex; gap:10px; padding-top:14px; border-top:1px solid var(--border-subtle);">
              <button class="btn btn-primary" style="width:100%;" onclick="navigatePage('app-policies')">
                ${icon('shield', 15)} View Active Policies &amp; Digital E-Cards ${icon('arrowRight', 14)}
              </button>
            </div>
          </div>
        </div>

        <!-- Animated Step 4 -->
        <div class="step-video-card">
          <div class="step-video-screen">
            ${window.renderMiniStepAnimation ? window.renderMiniStepAnimation(4) : ''}
          </div>
          <div class="step-video-body">
            <div>
              <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:8px;">
                <span class="badge badge-amber">${icon('activity', 12)} STEP 04 · CLAIMS &amp; ICR RENEWALS</span>
                <span class="mono" style="font-size:11.5px; color:var(--text-muted);">Page: /app/claims</span>
              </div>
              <h3 style="font-family:var(--font-display); font-size:20px; font-weight:800; margin-bottom:8px;">
                How Automated Claims Adjudication &amp; Loss Ratio Renewals Work
              </h3>
              <p style="font-size:13.5px; color:var(--text-secondary);">
                Eliminate manual TPA delays with rule-driven claim deductions and transparent renewal pricing.
              </p>
              <ul class="how-to-checklist">
                <li>
                  <span style="color:var(--accent-emerald); margin-top:2px;">${icon('check', 15)}</span>
                  <span><strong>6-Point Automated Claim Validation:</strong> Checks policy status, waiting periods, benefit sub-limits, non-payable items, and 10% co-pay automatically.</span>
                </li>
                <li>
                  <span style="color:var(--accent-emerald); margin-top:2px;">${icon('check', 15)}</span>
                  <span><strong>Experience-Rated Renewals (ICR %):</strong> Computes <code>Incurred Claims / Earned Premium</code> to apply renewal discounts or loadings fairly.</span>
                </li>
                <li>
                  <span style="color:var(--accent-emerald); margin-top:2px;">${icon('check', 15)}</span>
                  <span><strong>How It Helps You:</strong> Rolls over all active employees to the next policy year while preserving their accumulated waiting-period continuity.</span>
                </li>
              </ul>
            </div>
            <div style="display:flex; gap:10px; padding-top:14px; border-top:1px solid var(--border-subtle);">
              <button class="btn btn-primary" style="width:100%;" onclick="navigatePage('app-claims')">
                ${icon('stethoscope', 15)} Explore Claims &amp; Renewal Engine ${icon('arrowRight', 14)}
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- SECTION 3: INTERACTIVE GROUP COVERAGE & BUDGET SIMULATOR -->
    <section class="mkt-section-alt">
      <div class="mkt-section">
        <div class="grid-2" style="align-items:center; gap:36px;">
          <div>
            <div class="mkt-eyebrow">${icon('Sliders', 14)} <span>Interactive Budget &amp; Coverage Estimator</span></div>
            <h2 class="mkt-h2">Estimate Your Company's Annual Benefits Budget in Seconds</h2>
            <p class="mkt-sub" style="margin-bottom:24px;">
              Adjust the sliders below to see how team headcount, sum insured, and industry risk factor affect your Group Health (GMC), Group Term Life (GTL), and Personal Accident (GPA) premiums — including automatic volume discounts.
            </p>

            <div class="card" style="margin-bottom:0;">
              <div class="card-body">
                <div class="form-group" style="margin-bottom:18px;">
                  <div style="display:flex; justify-content:space-between; margin-bottom:6px;">
                    <label class="form-label" style="margin:0;">1. Total Employee Headcount</label>
                    <span class="mono" style="font-weight:800; color:var(--accent-primary); font-size:15px;">${emp} Employees (${discPct}% Volume Discount)</span>
                  </div>
                  <input type="range" min="10" max="500" step="5" value="${emp}" style="width:100%; accent-color:var(--accent-primary); cursor:pointer;" oninput="updateSim('simEmployees', this.value)" />
                  <div style="display:flex; justify-content:space-between; font-size:11px; color:var(--text-muted); margin-top:4px;">
                    <span>10 Lives (Startup)</span>
                    <span>100 Lives (10% Off)</span>
                    <span>250+ Lives (15% Off)</span>
                    <span>500 Lives</span>
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label class="form-label">2. Health Floater Sum Insured</label>
                    <select class="form-select mono" onchange="updateSim('simSumInsured', this.value)">
                      <option value="300000" ${si === 300000 ? 'selected' : ''}>₹3,00,000 Family Floater</option>
                      <option value="500000" ${si === 500000 ? 'selected' : ''}>₹5,00,000 Family Floater (Standard)</option>
                      <option value="1000000" ${si === 1000000 ? 'selected' : ''}>₹10,00,000 Executive Floater</option>
                    </select>
                  </div>
                  <div class="form-group">
                    <label class="form-label">3. Industry Risk Classification</label>
                    <select class="form-select" onchange="updateSim('simIndustryLoad', this.value)">
                      <option value="1.0" ${indFactor === 1.0 ? 'selected' : ''}>IT / SaaS / Banking (1.00x Base)</option>
                      <option value="1.25" ${indFactor === 1.25 ? 'selected' : ''}>Manufacturing / Logistics (1.25x)</option>
                      <option value="1.6" ${indFactor === 1.6 ? 'selected' : ''}>Infrastructure / Heavy Ind. (1.60x)</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Right Column: Live Simulated Output Cards -->
          <div style="display:flex; flex-direction:column; gap:14px;">
            <div class="card" style="margin:0; border-left:4px solid var(--accent-emerald);">
              <div class="card-body" style="display:flex; align-items:center; justify-content:space-between; gap:16px; flex-wrap:wrap;">
                <div>
                  <span class="badge badge-emerald">${icon('health', 12)} GROUP HEALTH (GMC)</span>
                  <div style="font-weight:800; font-size:16.5px; margin-top:4px;">Family Floater (${formatInr(si)} Cover)</div>
                  <div style="font-size:12.5px; color:var(--text-secondary);">Covers hospitalisation, maternity (₹75k) &amp; daycare</div>
                </div>
                <div style="text-align:right;">
                  <div class="mono" style="font-size:22px; font-weight:800; color:var(--text-primary);">${formatInr(gmcAnnualNet)}<span style="font-size:12px; color:var(--text-muted);">/yr</span></div>
                  <div class="mono" style="font-size:11.5px; color:var(--accent-emerald);">~${formatInr(Math.round(gmcAnnualNet / emp / 12))}/emp/month</div>
                </div>
              </div>
            </div>

            <div class="card" style="margin:0; border-left:4px solid var(--accent-primary);">
              <div class="card-body" style="display:flex; align-items:center; justify-content:space-between; gap:16px; flex-wrap:wrap;">
                <div>
                  <span class="badge badge-cobalt">${icon('shield', 12)} GROUP TERM LIFE (GTL)</span>
                  <div style="font-weight:800; font-size:16.5px; margin-top:4px;">3x Annual CTC Cover (₹50L Free Cover Limit)</div>
                  <div style="font-size:12.5px; color:var(--text-secondary);">Zero medical tests up to ₹50,00,000 per employee</div>
                </div>
                <div style="text-align:right;">
                  <div class="mono" style="font-size:22px; font-weight:800; color:var(--text-primary);">${formatInr(gtlAnnualNet)}<span style="font-size:12px; color:var(--text-muted);">/yr</span></div>
                  <div class="mono" style="font-size:11.5px; color:var(--accent-primary);">~${formatInr(Math.round(gtlAnnualNet / emp / 12))}/emp/month</div>
                </div>
              </div>
            </div>

            <div class="card" style="margin:0; border-left:4px solid var(--accent-violet);">
              <div class="card-body" style="display:flex; align-items:center; justify-content:space-between; gap:16px; flex-wrap:wrap;">
                <div>
                  <span class="badge badge-violet">${icon('accident', 12)} PERSONAL ACCIDENT (GPA)</span>
                  <div style="font-weight:800; font-size:16.5px; margin-top:4px;">24x7 Worldwide Disability &amp; Death Cover</div>
                  <div style="font-size:12.5px; color:var(--text-secondary);">Includes PTD, PPD % scale &amp; weekly hospital indemnity</div>
                </div>
                <div style="text-align:right;">
                  <div class="mono" style="font-size:22px; font-weight:800; color:var(--text-primary);">${formatInr(gpaAnnualNet)}<span style="font-size:12px; color:var(--text-muted);">/yr</span></div>
                  <div class="mono" style="font-size:11.5px; color:var(--accent-violet);">~${formatInr(Math.round(gpaAnnualNet / emp / 12))}/emp/month</div>
                </div>
              </div>
            </div>

            <div style="display:flex; gap:12px; margin-top:4px;">
              <button class="btn btn-primary btn-lg" style="flex:1;" onclick="navigatePage('quote-studio')">
                ${icon('sparkles', 16)} Convert Estimate into Official Quote ${icon('arrowRight', 15)}
              </button>
              <button class="btn btn-secondary btn-lg" onclick="navigatePage('plans')">
                ${icon('layers', 16)} Compare All Plans
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- SECTION 4: LIVE PLAN SHOWCASE CARDS -->
    <section class="mkt-section">
      <div style="display:flex; align-items:flex-end; justify-content:space-between; flex-wrap:wrap; gap:16px; margin-bottom:28px;">
        <div>
          <div class="mkt-eyebrow">${icon('layers', 14)} <span>Live Seeded Actuarial Catalog</span></div>
          <h2 class="mkt-h2">Explore Our Corporate Insurance Plans</h2>
          <p class="mkt-sub">Select any plan below to inspect its published age-band rate table or launch the 4-Step Quote Studio.</p>
        </div>
        <button class="btn btn-secondary" onclick="navigatePage('plans')">
          View Full Side-by-Side Comparison Matrix ${icon('arrowRight', 14)}
        </button>
      </div>

      ${renderPlanCardsGridHtml()}
    </section>

    ${renderMarketingFooter()}
  `;
}

window.updateSim = function (key, val) {
  state[key] = Number(val);
  render();
};

/* ==========================================================================
   PAGE 2: DEDICATED INTERACTIVE VIDEO WALKTHROUGH & USER GUIDE (/how-it-works)
   ========================================================================== */
function renderHowItWorksPage() {
  return `
    ${renderMarketingNavbar()}

    <section class="company-hero" style="padding:48px 28px 56px;">
      <div class="company-hero-inner">
        <div style="max-width:820px; margin-bottom:32px;">
          <div class="hero-eyebrow">${icon('video', 14)} <span>Interactive Process Cinema &amp; Complete User Blueprint</span></div>
          <h1 class="company-hero-title" style="font-size:40px;">
            See Exactly How <span>InsureEase</span> Works From Initial Plan Analysis to Claims Settlement.
          </h1>
          <p class="company-hero-lead" style="margin-bottom:0;">
            Watch the 4 animated process videos below or follow the step-by-step actuarial blueprint to understand how to analyze plans, upload your employee census, handle ₹50L Free Cover Limits, and issue policies.
          </p>
        </div>

        ${renderProcessCinemaPlayer()}
      </div>
    </section>

    <section class="mkt-section">
      <div class="mkt-eyebrow">${icon('compass', 14)} <span>Detailed Step-by-Step User Manual</span></div>
      <h2 class="mkt-h2">Complete 4-Stage Operational Guide for HR, Finance &amp; Brokers</h2>
      <p class="mkt-sub" style="margin-bottom:32px;">
        Every card below pairs a live animated SVG workflow diagram with clear instructions on how to use the module and how its actuarial rules work.
      </p>

      <div class="step-video-grid">
        <div class="step-video-card">
          <div class="step-video-screen">${window.renderMiniStepAnimation ? window.renderMiniStepAnimation(1) : ''}</div>
          <div class="step-video-body">
            <div>
              <span class="badge badge-emerald">STAGE 1 · PLAN SELECTION &amp; SUB-LIMIT ANALYSIS</span>
              <h3 style="font-family:var(--font-display); font-size:19px; font-weight:800; margin:8px 0;">
                1. Analyzing Health (GMC), Life (GTL) &amp; Accident (GPA) Plans
              </h3>
              <ul class="how-to-checklist">
                <li><span>${icon('check', 14)}</span><span><strong>Where to go:</strong> Click <strong>Compare Plans &amp; Rates</strong> in the top navigation bar (<code>/plans</code>).</span></li>
                <li><span>${icon('check', 14)}</span><span><strong>What to check in Health (GMC):</strong> Verify the Family Floater Sum Insured (₹5,00,000), Maternity sub-limit (₹75,000), Co-Pay (10%), and Room Rent limit.</span></li>
                <li><span>${icon('check', 14)}</span><span><strong>What to check in Life (GTL):</strong> Check the Salary Multiple (<code>3x Annual CTC</code>) and the <strong>Free Cover Limit (₹50,00,000)</strong> — employees below ₹50L require zero medical underwriting.</span></li>
              </ul>
            </div>
            <button class="btn btn-primary" onclick="navigatePage('plans')">
              ${icon('layers', 15)} Go to Stage 1: Compare Plans &amp; Rates ${icon('arrowRight', 14)}
            </button>
          </div>
        </div>

        <div class="step-video-card">
          <div class="step-video-screen">${window.renderMiniStepAnimation ? window.renderMiniStepAnimation(2) : ''}</div>
          <div class="step-video-body">
            <div>
              <span class="badge badge-cobalt">STAGE 2 · CENSUS UPLOAD, RATING &amp; UNDERWRITING</span>
              <h3 style="font-family:var(--font-display); font-size:19px; font-weight:800; margin:8px 0;">
                2. Generating an Instant Quote &amp; Handling FCL Medical Referrals
              </h3>
              <ul class="how-to-checklist">
                <li><span>${icon('check', 14)}</span><span><strong>Where to go:</strong> Click <strong>4-Step Quote Studio</strong> (<code>/quote-studio</code>).</span></li>
                <li><span>${icon('check', 14)}</span><span><strong>Actuarial Rating Formula:</strong> Each member is matched to their age band (<code>18-35</code>, <code>36-45</code>, <code>46-55</code>, <code>56-65</code>), multiplied by industry risk load, less volume discount, plus <strong>18% GST</strong>.</span></li>
                <li><span>${icon('check', 14)}</span><span><strong>Approving Above-FCL Cover:</strong> In Step 3 of the Quote Studio, click <em>Approve Full Cover</em> on any executive marked <code>MEDICAL_REQUIRED</code> to unlock their full ₹75L cover.</span></li>
              </ul>
            </div>
            <button class="btn btn-primary" onclick="navigatePage('quote-studio')">
              ${icon('sparkles', 15)} Go to Stage 2: Launch 4-Step Quote Studio ${icon('arrowRight', 14)}
            </button>
          </div>
        </div>

        <div class="step-video-card">
          <div class="step-video-screen">${window.renderMiniStepAnimation ? window.renderMiniStepAnimation(3) : ''}</div>
          <div class="step-video-body">
            <div>
              <span class="badge badge-violet">STAGE 3 · SECTION 64VB PAYMENT, E-CARDS &amp; ENDORSEMENTS</span>
              <h3 style="font-family:var(--font-display); font-size:19px; font-weight:800; margin:8px 0;">
                3. Paying Invoices, Issuing E-Cards &amp; Mid-Term Additions
              </h3>
              <ul class="how-to-checklist">
                <li><span>${icon('check', 14)}</span><span><strong>Online Gateway Checkout:</strong> Pay the generated GST invoice using Razorpay Sandbox, Stripe Sandbox (with real HMAC-SHA256 signature verification), or NEFT.</span></li>
                <li><span>${icon('check', 14)}</span><span><strong>Download Digital E-Cards:</strong> Once paid, download the Master Policy Schedule PDF and individual Member E-Card / COI PDFs.</span></li>
                <li><span>${icon('check', 14)}</span><span><strong>Mid-Term Joiners &amp; Leavers:</strong> In <code>/app/policies</code>, click <em>+ Add New Joiner Mid-Term</em> to calculate exact pro-rata premium and deduct it from your Section 64VB CD Wallet.</span></li>
              </ul>
            </div>
            <button class="btn btn-primary" onclick="navigatePage('app-policies')">
              ${icon('shield', 15)} Go to Stage 3: Policies &amp; Digital E-Cards ${icon('arrowRight', 14)}
            </button>
          </div>
        </div>

        <div class="step-video-card">
          <div class="step-video-screen">${window.renderMiniStepAnimation ? window.renderMiniStepAnimation(4) : ''}</div>
          <div class="step-video-body">
            <div>
              <span class="badge badge-amber">STAGE 4 · CLAIMS ADJUDICATION &amp; ICR RENEWALS</span>
              <h3 style="font-family:var(--font-display); font-size:19px; font-weight:800; margin:8px 0;">
                4. Filing Claims &amp; Running Experience-Rated Renewals
              </h3>
              <ul class="how-to-checklist">
                <li><span>${icon('check', 14)}</span><span><strong>Filing &amp; Adjudicating Claims:</strong> Go to <code>/app/claims</code> and click <em>+ File a New Claim</em>. The engine automatically deducts non-payable items, sub-limit excesses, and 10% co-pay.</span></li>
                <li><span>${icon('check', 14)}</span><span><strong>Experience Rating (ICR %):</strong> Go to <code>/app/renewals</code> to compute <code>Incurred Claims / Earned Premium</code> and generate T-60 renewal terms.</span></li>
                <li><span>${icon('check', 14)}</span><span><strong>1-Click Member Rollover:</strong> Renew the policy and roll over all active employees with continuous waiting-period credit.</span></li>
              </ul>
            </div>
            <button class="btn btn-primary" onclick="navigatePage('app-claims')">
              ${icon('stethoscope', 15)} Go to Stage 4: Claims &amp; Renewals ${icon('arrowRight', 14)}
            </button>
          </div>
        </div>
      </div>
    </section>

    ${renderMarketingFooter()}
  `;
}

/* ==========================================================================
   PAGE 3: PLAN EXPLORER, BENEFIT ANALYZER & COMPARISON MATRIX (/plans)
   ========================================================================== */
function getPlanMarketingMeta(plan) {
  const prod = getProductForPlan(plan);
  const type = prod ? prod.productType : 'HEALTH';

  if (type === 'LIFE') {
    return {
      iconSvg: icon('shield', 24),
      iconColor: 'var(--accent-primary)',
      iconBg: 'var(--accent-primary-soft)',
      badge: 'Group Term Life (GTL)',
      badgeClass: 'badge-cobalt',
      ribbon: '₹50L Free Cover Limit',
      headlinePrice: '₹1.20 – ₹7.50',
      priceUnit: 'per ₹1,000 Sum Assured / year',
      summary: 'Income-linked life protection (3x Annual CTC) with instant cover up to ₹50 Lakhs without medical tests.',
      howToAnalyze: 'Best for protecting employee families against mortality risk. Check the Free Cover Limit (₹50L) so senior executives get immediate cover while medicals are processed.',
      features: [
        'Sum Assured: 3x Employee Annual CTC (up to ₹2 Cr)',
        'Free Cover Limit (FCL): Up to ₹50,00,000 approved instantly with zero medicals',
        '100% Lump-sum Death Benefit paid directly to designated Nominees',
        'Covers natural, illness, and accidental death 24x7 worldwide',
      ],
    };
  }
  if (type === 'PA') {
    return {
      iconSvg: icon('accident', 24),
      iconColor: 'var(--accent-violet)',
      iconBg: 'var(--accent-violet-soft)',
      badge: 'Personal Accident (GPA)',
      badgeClass: 'badge-violet',
      ribbon: 'Table A–D Disability Cover',
      headlinePrice: '₹0.60 – ₹2.25',
      priceUnit: 'per ₹1,000 Sum Insured / year',
      summary: 'Comprehensive 24x7 accidental death, permanent total/partial disability, and weekly hospital cash.',
      howToAnalyze: 'Essential for field engineers, logistics, and operations teams. Compare Occupation Class I (Desk), II (Field), and III (Industrial) rates.',
      features: [
        'Table A & B: 100% payout on Accidental Death or Permanent Total Disability (PTD)',
        'Table C: Proportionate % payout for Permanent Partial Disability (PPD)',
        'Table D: Weekly indemnity cash during temporary total disablement',
        'Custom pricing across Occupation Classes I (Desk), II (Field), and III (Industrial)',
      ],
    };
  }
  return {
    iconSvg: icon('health', 24),
    iconColor: 'var(--accent-emerald)',
    iconBg: 'var(--accent-emerald-soft)',
    badge: 'Group Health (GMC)',
    badgeClass: 'badge-emerald',
    ribbon: 'Most Popular · Family Floater',
    headlinePrice: '₹4,500',
    priceUnit: 'starting / life / year (+18% GST)',
    summary: 'Cashless hospitalisation & maternity cover for employees, spouses, children, and parents across network hospitals.',
    howToAnalyze: 'Evaluate the Family Floater Sum Insured (₹5L), Maternity sub-limit (₹75k), and 10% co-pay to balance comprehensive family care with predictable renewals.',
    features: [
      '₹5,00,000 Family Floater Sum Insured shared across Employee + Family',
      'In-patient hospitalisation covered up to 100% of Sum Insured',
      'Maternity Benefit included up to ₹75,000 (Normal & C-Section)',
      '10% Co-pay · ₹5,000 Deductible · 1-Hour Cashless Pre-Auth SLA',
    ],
  };
}

function renderPlanCardsGridHtml() {
  const filteredPlans = state.plans.filter(pl => {
    if (state.planCategoryFilter === 'ALL') return true;
    const prod = getProductForPlan(pl);
    return prod && prod.productType === state.planCategoryFilter;
  });
  const selectedPlan = state.plans.find(p => p.id === state.wizardPlanId) || state.plans[0];

  return `
    <!-- Category Filter Pills -->
    <div class="category-pills">
      <button class="category-pill ${state.planCategoryFilter === 'ALL' ? 'active' : ''}" onclick="setPlanFilter('ALL')">
        All Insurance Plans (${state.plans.length})
      </button>
      <button class="category-pill ${state.planCategoryFilter === 'HEALTH' ? 'active' : ''}" onclick="setPlanFilter('HEALTH')">
        ${icon('health', 14)} Group Health (GMC)
      </button>
      <button class="category-pill ${state.planCategoryFilter === 'LIFE' ? 'active' : ''}" onclick="setPlanFilter('LIFE')">
        ${icon('shield', 14)} Group Term Life (GTL)
      </button>
      <button class="category-pill ${state.planCategoryFilter === 'PA' ? 'active' : ''}" onclick="setPlanFilter('PA')">
        ${icon('accident', 14)} Personal Accident (GPA)
      </button>
    </div>

    <div class="plan-cards-grid">
      ${filteredPlans.map(pl => {
        const meta = getPlanMarketingMeta(pl);
        const isSelected = selectedPlan && selectedPlan.id === pl.id;
        return `
          <div class="plan-showcase-card ${isSelected ? 'selected' : ''}">
            <div>
              <div class="plan-ribbon">
                <span class="badge ${meta.badgeClass}">${escapeHtml(meta.ribbon)}</span>
              </div>
              <div class="plan-icon-badge" style="background:${meta.iconBg}; color:${meta.iconColor};">
                ${meta.iconSvg}
              </div>
              <div style="margin-bottom:6px;">
                <span class="badge ${meta.badgeClass}">${escapeHtml(meta.badge)}</span>
                <span class="mono" style="font-size:11.5px; color:var(--text-muted); margin-left:6px;">${escapeHtml(pl.code)}</span>
              </div>
              <h3 class="plan-card-title">${escapeHtml(pl.name)}</h3>
              <p class="plan-card-desc">${escapeHtml(meta.summary)}</p>

              <div class="plan-price-box">
                <div style="font-size:11px; font-weight:700; text-transform:uppercase; letter-spacing:0.06em; color:var(--text-muted); margin-bottom:2px;">
                  Published Actuarial Rate
                </div>
                <div class="plan-price-val">${escapeHtml(meta.headlinePrice)}</div>
                <div class="plan-price-unit">${escapeHtml(meta.priceUnit)}</div>
              </div>

              <div style="background:var(--bg-subtle); border-left:3px solid var(--accent-primary); padding:10px 12px; border-radius:6px; margin-bottom:16px; font-size:12px; color:var(--text-secondary);">
                <strong style="color:var(--text-primary); display:block; margin-bottom:2px;">How to Analyze This Plan:</strong>
                ${escapeHtml(meta.howToAnalyze)}
              </div>

              <ul class="plan-feature-list">
                ${meta.features.map(f => `
                  <li class="plan-feature-item">
                    <span class="plan-feature-check">${icon('check', 15)}</span>
                    <span>${escapeHtml(f)}</span>
                  </li>
                `).join('')}
              </ul>
            </div>

            <div style="display:flex; gap:10px; padding-top:12px; border-top:1px solid var(--border-subtle);">
              <button class="btn btn-secondary" style="flex:0.9;" onclick="openPlanDetailsModal('${pl.id}')">
                ${icon('search', 14)} Rate Card
              </button>
              <button class="btn ${isSelected ? 'btn-emerald' : 'btn-primary'}" style="flex:1.4;" onclick="choosePlanForWizard('${pl.id}')">
                ${isSelected ? `${icon('check', 14)} Configure in Studio` : `Select &amp; Quote ${icon('arrowRight', 14)}`}
              </button>
            </div>
          </div>
        `;
      }).join('')}
    </div>
  `;
}

function renderPlansExplorerPage() {
  return `
    ${renderMarketingNavbar()}

    <section class="mkt-section">
      <div class="page-header">
        <div>
          <div class="mkt-eyebrow">${icon('layers', 14)} <span>Plan Explorer &amp; Actuarial Rate Analyzer</span></div>
          <h1 class="mkt-h2">Compare Corporate Group Insurance Plans Side-by-Side</h1>
          <p class="mkt-sub">
            Review coverage structures, sub-limits, Free Cover Limits (FCL), and age-band pricing tables below. When you find the right plan, click <strong>Select &amp; Quote</strong> to open the 4-Step Quote Studio.
          </p>
        </div>
        <div class="page-actions">
          <button class="btn btn-secondary" onclick="navigatePage('how-it-works')">
            ${icon('video', 15)} Watch How Plan Rating Works
          </button>
          <button class="btn btn-primary" onclick="navigatePage('quote-studio')">
            ${icon('sparkles', 15)} Open 4-Step Quote Studio ${icon('arrowRight', 14)}
          </button>
        </div>
      </div>

      ${renderPlanCardsGridHtml()}

      <!-- Side-by-Side Comparison Matrix -->
      <div class="card" style="margin-top:24px;">
        <div class="card-header">
          <div>
            <div class="card-title">${icon('barChart', 18)} Side-by-Side Actuarial &amp; Benefit Comparison Matrix</div>
            <div class="card-subtitle">Compare how Group Health (GMC), Group Term Life (GTL), and Group Personal Accident (GPA) protect your workforce</div>
          </div>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>Evaluation Parameter</th>
                <th>Group Medical Cover (GMC-GOLD-5L)</th>
                <th>Group Term Life (GTL-EXEC-3X)</th>
                <th>Group Personal Accident (GPA-COMP-20L)</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td style="font-weight:700;">Primary Purpose</td>
                <td>Hospitalisation, Daycare &amp; Maternity bills for Employee + Family</td>
                <td>Lump-sum financial security for family upon Employee Death</td>
                <td>24x7 Accidental Death, Permanent/Partial Disability &amp; Weekly Cash</td>
              </tr>
              <tr>
                <td style="font-weight:700;">Coverage Basis</td>
                <td><span class="badge badge-emerald">FAMILY FLOATER (₹5,00,000)</span></td>
                <td><span class="badge badge-cobalt">3x ANNUAL CTC (Up to ₹2 Cr)</span></td>
                <td><span class="badge badge-violet">FLAT / GRADED (Up to ₹20L)</span></td>
              </tr>
              <tr>
                <td style="font-weight:700;">Free Cover Limit (No Medicals)</td>
                <td>100% of ₹5,00,000 Floater (Zero Medicals)</td>
                <td><strong class="mono" style="color:var(--accent-emerald);">₹50,00,000 Free Cover Limit</strong></td>
                <td>100% of ₹20,00,000 (Zero Medicals)</td>
              </tr>
              <tr>
                <td style="font-weight:700;">Rating Basis</td>
                <td class="mono">Flat Annual Rate per Age-Band</td>
                <td class="mono">Per Mille (per ₹1,000 Sum Assured)</td>
                <td class="mono">Per Mille by Occupation Class I/II/III</td>
              </tr>
              <tr>
                <td style="font-weight:700;">Co-Pay &amp; Deductible</td>
                <td class="mono">10% Co-Pay · ₹5,000 Deductible</td>
                <td class="mono">0% Co-Pay · ₹0 Deductible</td>
                <td class="mono">0% Co-Pay · ₹0 Deductible</td>
              </tr>
              <tr>
                <td style="font-weight:700;">Mid-Term Joiner Pro-Rata</td>
                <td>Instant via Section 64VB CD Float Wallet</td>
                <td>Instant via Section 64VB CD Float Wallet</td>
                <td>Instant via Section 64VB CD Float Wallet</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>

    ${renderMarketingFooter()}
  `;
}

/* ==========================================================================
   PAGE 4: 4-STEP QUOTE-TO-POLICY & E-CARD STUDIO (/quote-studio)
   ========================================================================== */
function renderQuoteStudioPage() {
  return `
    ${renderMarketingNavbar()}
    <section class="mkt-section" style="padding-top:36px;">
      ${renderMarketplaceAndWizardView()}
    </section>
    ${renderMarketingFooter()}
  `;
}

function renderMarketplaceAndWizardView() {
  const selectedPlan = state.plans.find(p => p.id === state.wizardPlanId) || state.plans[0];
  const selectedProd = getProductForPlan(selectedPlan);
  const activeQuote = state.quotes.find(q => q.id === state.wizardQuoteId) || null;
  const activePolicy = state.policies.find(p => p.id === state.wizardPolicyId) || null;
  const activeInvoice = state.invoices.find(i => i.id === state.wizardInvoiceId || (activePolicy && i.policyId === activePolicy.id)) || null;

  return `
    <div class="page-header">
      <div>
        <div class="mkt-eyebrow">${icon('sparkles', 14)} <span>Interactive 4-Step Quote-to-Policy Studio</span></div>
        <h1 class="page-title">Configure Your Team, Price Your Census &amp; Issue Your Policy</h1>
        <p class="page-subtitle">Follow the 4 guided steps below to select your plan, upload or customize your employee roster, review the actuarial quote, and activate your Master Policy &amp; Digital E-Cards.</p>
      </div>
      <div class="page-actions">
        <button class="btn btn-secondary" onclick="navigatePage('plans')">
          ${icon('layers', 15)} Compare Plans First
        </button>
        <button class="btn btn-secondary" onclick="openCreatePolicyholderModal()">
          ${icon('plus', 15)} Add Company Profile
        </button>
      </div>
    </div>

    <!-- ====================================================================
         4-STEP INTERACTIVE "GET YOUR POLICY" WIZARD
         ==================================================================== -->
    <div id="policy-wizard-section" class="card" style="border-width:1.5px;">
      <div class="card-header" style="background:var(--bg-subtle);">
        <div>
          <div class="card-title" style="font-size:18px;">
            ${icon('shield', 19)} Step-by-Step Policy Configurator
            ${selectedPlan ? `<span class="badge badge-cobalt">${escapeHtml(selectedPlan.name)}</span>` : ''}
          </div>
          <div class="card-subtitle">Each step runs live against the Spring Boot actuarial rating, underwriting, and Section 64VB billing engine.</div>
        </div>
        <div style="display:flex; gap:8px;">
          <button class="btn btn-xs btn-secondary" onclick="resetPolicyWizard()">
            ${icon('refresh', 13)} Start Fresh Quote
          </button>
        </div>
      </div>

      <div class="card-body">
        <!-- 4-Step Visual Progress Bar -->
        <div class="wizard-stepper">
          <div class="wizard-step ${state.wizardStep === 1 ? 'active' : state.wizardStep > 1 ? 'done' : ''}" onclick="setWizardStep(1)">
            <div class="wizard-step-num">${state.wizardStep > 1 ? icon('check', 14) : '1'}</div>
            <div>
              <div style="font-weight:700; font-size:13px;">1. Plan &amp; Company</div>
              <div style="font-size:11.5px; color:var(--text-muted);">Choose coverage &amp; employer</div>
            </div>
          </div>
          <div class="wizard-step ${state.wizardStep === 2 ? 'active' : state.wizardStep > 2 ? 'done' : ''}" onclick="setWizardStep(2)">
            <div class="wizard-step-num">${state.wizardStep > 2 ? icon('check', 14) : '2'}</div>
            <div>
              <div style="font-weight:700; font-size:13px;">2. Add Employees</div>
              <div style="font-size:11.5px; color:var(--text-muted);">${state.draftRoster.length} members in roster</div>
            </div>
          </div>
          <div class="wizard-step ${state.wizardStep === 3 ? 'active' : state.wizardStep > 3 ? 'done' : ''}" onclick="if(state.wizardQuoteId) setWizardStep(3)">
            <div class="wizard-step-num">${state.wizardStep > 3 ? icon('check', 14) : '3'}</div>
            <div>
              <div style="font-weight:700; font-size:13px;">3. Review Instant Quote</div>
              <div style="font-size:11.5px; color:var(--text-muted);">${activeQuote ? formatInr(activeQuote.totalPremium) : 'Price & FCL check'}</div>
            </div>
          </div>
          <div class="wizard-step ${state.wizardStep === 4 ? 'active' : ''}" onclick="if(state.wizardPolicyId) setWizardStep(4)">
            <div class="wizard-step-num">4</div>
            <div>
              <div style="font-weight:700; font-size:13px;">4. Pay &amp; Get Policy</div>
              <div style="font-size:11.5px; color:var(--text-muted);">${activePolicy ? activePolicy.policyNo : 'Download E-Cards'}</div>
            </div>
          </div>
        </div>

        <div class="wizard-layout">
          <!-- Left Column: Active Step Controls -->
          <div>
            ${state.wizardStep === 1 ? renderWizardStep1(selectedPlan) : ''}
            ${state.wizardStep === 2 ? renderWizardStep2(selectedPlan, selectedProd) : ''}
            ${state.wizardStep === 3 ? renderWizardStep3(activeQuote, selectedPlan, selectedProd) : ''}
            ${state.wizardStep === 4 ? renderWizardStep4(activePolicy, activeInvoice) : ''}
          </div>

          <!-- Right Column: Live Sticky Summary Receipt -->
          <div class="summary-receipt">
            <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:14px;">
              <span style="font-family:var(--font-display); font-weight:800; font-size:15.5px;">Live Policy Summary</span>
              ${activePolicy ? statusBadge(activePolicy.status) : activeQuote ? statusBadge(activeQuote.status) : `<span class="badge badge-cobalt">CONFIGURING</span>`}
            </div>

            <div class="receipt-row">
              <span>Selected Plan</span>
              <strong style="color:var(--text-primary);">${selectedPlan ? escapeHtml(selectedPlan.name) : '—'}</strong>
            </div>
            <div class="receipt-row">
              <span>Coverage Basis</span>
              <span class="mono">${selectedPlan ? escapeHtml(selectedPlan.coverBasis) : '—'}</span>
            </div>
            ${selectedPlan && selectedPlan.freeCoverLimit ? `
              <div class="receipt-row">
                <span>Free Cover Limit (No Medicals)</span>
                <strong class="mono" style="color:var(--accent-emerald);">${formatInr(selectedPlan.freeCoverLimit)}</strong>
              </div>
            ` : ''}
            <div class="receipt-row">
              <span>Insured Lives</span>
              <strong class="mono">${activeQuote ? `${activeQuote.totalLives} (${activeQuote.employeeCount} Emp / ${activeQuote.dependentCount} Dep)` : `${state.draftRoster.length} lives`}</strong>
            </div>

            ${activeQuote ? `
              <div class="receipt-row">
                <span>Total Approved Sum Insured</span>
                <strong class="mono">${formatInr(activeQuote.totalApprovedSi)}</strong>
              </div>
              <div class="receipt-row">
                <span>Base Premium</span>
                <span class="mono">${formatInrExact(activeQuote.basePremium)}</span>
              </div>
              <div class="receipt-row">
                <span>Group Discount (${activeQuote.groupDiscountPct || 0}%)</span>
                <span class="mono" style="color:var(--accent-emerald);">-${formatInrExact(Number(activeQuote.basePremium || 0) - Number(activeQuote.netPremium || 0))}</span>
              </div>
              <div class="receipt-row">
                <span>Net Annual Premium</span>
                <strong class="mono">${formatInrExact(activeQuote.netPremium)}</strong>
              </div>
              <div class="receipt-row">
                <span>GST (${activeQuote.gstPct || 18}%)</span>
                <span class="mono">${formatInrExact(activeQuote.gstAmount)}</span>
              </div>
              <div class="receipt-total">
                <span>Total Payable</span>
                <span class="mono" style="color:var(--accent-primary);">${formatInrExact(activeQuote.totalPremium)}</span>
              </div>
            ` : `
              <div style="margin-top:16px; padding:14px; border-radius:10px; background:var(--bg-subtle); font-size:12.5px; color:var(--text-secondary); display:flex; gap:10px; align-items:flex-start;">
                <span style="color:var(--accent-primary); margin-top:2px;">${icon('sparkles', 16)}</span>
                <span>Complete <strong>Step 1</strong> and <strong>Step 2</strong> to calculate your exact member-by-member premium and 18% GST breakup.</span>
              </div>
            `}

            ${activePolicy ? `
              <div style="margin-top:16px; padding:14px; border-radius:10px; background:var(--accent-emerald-soft); border:1px solid rgba(16,185,129,0.3); font-size:12.5px;">
                <div style="font-weight:700; color:var(--accent-emerald-text); margin-bottom:4px;">
                  Master Policy: <span class="mono">${escapeHtml(activePolicy.policyNo)}</span>
                </div>
                <div style="color:var(--text-secondary);">
                  Term: <span class="mono">${escapeHtml(activePolicy.startDate)}</span> to <span class="mono">${escapeHtml(activePolicy.endDate)}</span>
                </div>
              </div>
            ` : ''}
          </div>
        </div>
      </div>
    </div>
  `;
}

function renderWizardStep1(selectedPlan) {
  return `
    <div style="padding:4px;">
      <h3 style="font-family:var(--font-display); font-size:17px; font-weight:700; margin-bottom:6px;">
        Step 1: Confirm Your Plan &amp; Company Details
      </h3>
      <p style="font-size:13px; color:var(--text-secondary); margin-bottom:18px;">
        Select which insurance plan you want to configure and which corporate employer will hold the Master Policy.
      </p>

      <div class="form-group">
        <label class="form-label">1. Selected Insurance Plan</label>
        <div style="display:flex; gap:10px;">
          <select class="form-select" onchange="choosePlanForWizard(this.value, true)">
            ${state.plans.map(pl => `
              <option value="${pl.id}" ${selectedPlan && selectedPlan.id === pl.id ? 'selected' : ''}>
                ${escapeHtml(pl.name)} (${escapeHtml(pl.code)} · ${escapeHtml(pl.coverBasis)})
              </option>
            `).join('')}
          </select>
          ${selectedPlan ? `
            <button class="btn btn-secondary" onclick="openPlanDetailsModal('${selectedPlan.id}')">
              ${icon('search', 14)} View Rate Card
            </button>
          ` : ''}
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">2. Corporate Employer (Policyholder)</label>
        <div style="display:flex; gap:10px;">
          <select id="wiz-ph-select" class="form-select" onchange="state.wizardPolicyholderId = this.value">
            ${state.policyholders.map(ph => `
              <option value="${ph.id}" ${state.wizardPolicyholderId === ph.id ? 'selected' : ''}>
                ${escapeHtml(ph.legalName)} — GSTIN: ${escapeHtml(ph.gstin)}
              </option>
            `).join('')}
          </select>
          <button class="btn btn-secondary" onclick="openCreatePolicyholderModal()">
            ${icon('plus', 14)} New Company
          </button>
        </div>
      </div>

      <div style="margin-top:24px; display:flex; justify-content:flex-end;">
        <button class="btn btn-primary btn-lg" onclick="setWizardStep(2)">
          Continue to Step 2: Add Employees &amp; Family ${icon('arrowRight', 15)}
        </button>
      </div>
    </div>
  `;
}

function renderWizardStep2(selectedPlan, selectedProd) {
  const isLife = selectedProd && selectedProd.productType === 'LIFE';
  const isPa = selectedProd && selectedProd.productType === 'PA';

  return `
    <div style="padding:4px;">
      <div style="display:flex; align-items:center; justify-content:space-between; gap:12px; flex-wrap:wrap; margin-bottom:14px;">
        <div>
          <h3 style="font-family:var(--font-display); font-size:17px; font-weight:700;">
            Step 2: Add Employees &amp; Family Members
          </h3>
          <p style="font-size:13px; color:var(--text-secondary);">
            Use our 1-click recommended roster below or add your own employees to test age-band rating and Free Cover Limits.
          </p>
        </div>
        <div style="display:flex; gap:8px; flex-wrap:wrap;">
          <button class="btn btn-xs btn-secondary" onclick="loadDefaultSampleRosterForPlan(state.plans.find(p=>p.id===state.wizardPlanId)); render();">
            ${icon('refresh', 13)} Load Recommended Team Roster
          </button>
          <button class="btn btn-xs btn-primary" onclick="openAddDraftMemberModal()">
            ${icon('plus', 13)} Add Person to Roster
          </button>
        </div>
      </div>

      <div class="table-wrap" style="border:1px solid var(--border-subtle); border-radius:12px; margin-bottom:20px;">
        <table class="data-table">
          <thead>
            <tr>
              <th>Emp Code</th>
              <th>Full Name</th>
              <th>DOB</th>
              <th>Sex</th>
              <th>Relation</th>
              <th>${isLife ? 'Annual Salary (CTC)' : 'Sum Insured'}</th>
              ${isLife || isPa ? '<th>Nominee (100%)</th>' : ''}
              <th>Remove</th>
            </tr>
          </thead>
          <tbody>
            ${state.draftRoster.length === 0 ? `
              <tr><td colspan="8" style="text-align:center; padding:24px; color:var(--text-muted);">No members in roster. Click "Add Person to Roster" or "Load Recommended Team Roster".</td></tr>
            ` : state.draftRoster.map((m, idx) => `
              <tr>
                <td class="mono" style="font-weight:600;">${escapeHtml(m.employeeCode)}</td>
                <td style="font-weight:600;">${escapeHtml(m.name)}</td>
                <td class="mono">${escapeHtml(m.dob)}</td>
                <td class="mono">${escapeHtml(m.gender)}</td>
                <td>${statusBadge(m.relation)}</td>
                <td class="mono" style="font-weight:700;">
                  ${isLife ? `${formatInr(m.annualCtc)} (→ ${formatInr((m.annualCtc || 0) * (selectedPlan.salaryMultiple || 3))} Cover)` : formatInr(m.sumInsured)}
                </td>
                ${isLife || isPa ? `<td style="font-size:12.5px;">${escapeHtml(m.nomineeName || 'Spouse')} (${escapeHtml(m.nomineeRelation || 'SPOUSE')})</td>` : ''}
                <td>
                  <button class="btn btn-xs btn-ghost" style="color:var(--accent-crimson);" onclick="removeDraftMember(${idx})">${icon('x', 14)}</button>
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>

      <div style="display:flex; justify-content:space-between; align-items:center;">
        <button class="btn btn-secondary" onclick="setWizardStep(1)">${icon('arrowLeft', 14)} Back to Plan Selection</button>
        <button class="btn btn-primary btn-lg" onclick="submitWizardStep2AndCalculate()">
          ${icon('calculator', 16)} Calculate Instant Price Quote ${icon('arrowRight', 15)}
        </button>
      </div>
    </div>
  `;
}

function renderWizardStep3(activeQuote, selectedPlan, selectedProd) {
  if (!activeQuote) {
    return `<div style="padding:24px; color:var(--text-muted);">Calculating quote...</div>`;
  }
  const hasMedicalRequired = state.quoteMembers.some(m => m.uwStatus === 'MEDICAL_REQUIRED');
  const needsUwDecision = activeQuote.status === 'UNDER_REVIEW';

  return `
    <div style="padding:4px;">
      <div style="display:flex; align-items:center; justify-content:space-between; gap:12px; flex-wrap:wrap; margin-bottom:14px;">
        <div>
          <h3 style="font-family:var(--font-display); font-size:17px; font-weight:700;">
            Step 3: Review Your Official Quote (<span class="mono" style="color:var(--accent-primary);">${escapeHtml(activeQuote.quoteNo)}</span>)
          </h3>
          <p style="font-size:13px; color:var(--text-secondary);">
            Review member-wise coverage and premiums below, download the PDF proposal, or proceed to issue the policy.
          </p>
        </div>
        <button class="btn btn-sm btn-secondary" onclick="downloadAuthenticatedPdf('/quotes/${activeQuote.id}/proposal.pdf', '${activeQuote.quoteNo}.pdf')">
          ${icon('download', 14)} Download Quote Proposal PDF
        </button>
      </div>

      ${hasMedicalRequired ? `
        <div style="background:var(--accent-amber-soft); border:1px solid rgba(217,119,6,0.35); border-radius:12px; padding:14px 16px; margin-bottom:16px; display:flex; align-items:center; justify-content:space-between; gap:12px; flex-wrap:wrap;">
          <div style="font-size:13px; color:var(--accent-amber-text); display:flex; align-items:center; gap:10px;">
            <span>${icon('alert', 18)}</span>
            <span><strong>Free Cover Limit (FCL) Notice:</strong> One or more executives requested cover above the <strong>₹50,00,000 Free Cover Limit</strong>. Their immediate active cover is capped at ₹50,00,000 until medical underwriting is approved.</span>
          </div>
        </div>
      ` : ''}

      ${needsUwDecision ? `
        <div style="background:var(--accent-primary-soft); border:1px solid rgba(37,99,235,0.3); border-radius:12px; padding:14px 16px; margin-bottom:16px; display:flex; align-items:center; justify-content:space-between; gap:12px; flex-wrap:wrap;">
          <div style="font-size:13px;">
            <strong>Underwriter Review Required:</strong> This quote triggered an underwriting referral. Click to approve with a 5% preferred group discount.
          </div>
          <button class="btn btn-sm btn-primary" onclick="quickApproveWizardUw('${activeQuote.id}')">
            ${icon('check', 14)} Approve Underwriting (Apply 5% Discount)
          </button>
        </div>
      ` : ''}

      <div class="table-wrap" style="border:1px solid var(--border-subtle); border-radius:12px; margin-bottom:20px;">
        <table class="data-table">
          <thead>
            <tr>
              <th>Emp Code</th>
              <th>Member Name</th>
              <th>Age / Sex</th>
              <th>Relation</th>
              <th>Requested Cover</th>
              <th>Approved Cover</th>
              <th>Status</th>
              <th>Annual Premium</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            ${state.quoteMembers.map(m => `
              <tr>
                <td class="mono" style="font-weight:600;">${escapeHtml(m.employeeCode)}</td>
                <td style="font-weight:600;">${escapeHtml(m.name)}</td>
                <td class="mono">${m.age} / ${escapeHtml(m.gender)}</td>
                <td>${statusBadge(m.relation)}</td>
                <td class="mono">${formatInr(m.requestedSi)}</td>
                <td class="mono" style="font-weight:700; color:var(--accent-primary);">${formatInr(m.approvedSi)}</td>
                <td>${statusBadge(m.uwStatus)}</td>
                <td class="mono" style="font-weight:700;">${formatInrExact(m.basePremium)}</td>
                <td>
                  ${m.uwStatus === 'MEDICAL_REQUIRED' ? `
                    <button class="btn btn-xs btn-emerald" onclick="approveWizardMemberMedical('${activeQuote.id}', '${m.id}')">
                      ${icon('check', 13)} Approve Full Cover
                    </button>
                  ` : `<span style="font-size:12px; color:var(--text-muted);">Included</span>`}
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>

      <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px;">
        <button class="btn btn-secondary" onclick="setWizardStep(2)">${icon('arrowLeft', 14)} Edit Employee Roster</button>
        <button class="btn btn-emerald btn-lg" onclick="acceptQuoteAndProceedToStep4('${activeQuote.id}')">
          ${icon('check', 16)} Accept Quote &amp; Proceed to Payment (${formatInrExact(activeQuote.totalPremium)}) ${icon('arrowRight', 15)}
        </button>
      </div>
    </div>
  `;
}

function renderWizardStep4(activePolicy, activeInvoice) {
  if (!activePolicy) {
    return `<div style="padding:24px; color:var(--text-muted);">Issuing policy...</div>`;
  }
  const isPaid = activePolicy.status === 'ACTIVE' || (activeInvoice && activeInvoice.status === 'PAID');

  return `
    <div style="padding:4px;">
      ${!isPaid ? `
        <div style="background:var(--accent-amber-soft); border:1.5px solid rgba(217,119,6,0.35); border-radius:14px; padding:22px; margin-bottom:20px;">
          <div style="display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:12px; margin-bottom:12px;">
            <div>
              <span class="badge badge-amber">${icon('clock', 12)} AWAITING PREMIUM PAYMENT (SECTION 64VB)</span>
              <h3 style="font-family:var(--font-display); font-size:19px; font-weight:800; margin-top:6px;">
                Complete Payment to Activate Policy ${escapeHtml(activePolicy.policyNo)}
              </h3>
            </div>
            <div class="mono" style="font-size:22px; font-weight:800; color:var(--text-primary);">
              ${activeInvoice ? formatInrExact(activeInvoice.totalAmount) : formatInrExact(activePolicy.totalAnnualPremium)}
            </div>
          </div>
          <p style="font-size:13.5px; color:var(--text-secondary); margin-bottom:18px;">
            Invoice <strong class="mono">${activeInvoice ? escapeHtml(activeInvoice.invoiceNo) : ''}</strong> has been generated (including 18% GST). Choose your preferred payment method below to activate the Master Policy and generate Member E-Cards immediately:
          </p>
          <div style="display:flex; gap:12px; flex-wrap:wrap;">
            ${activeInvoice ? `
              <button class="btn btn-primary btn-lg" onclick="launchSandboxCheckout('${activeInvoice.id}', 'RAZORPAY')">
                ${icon('creditCard', 16)} Pay Online via Razorpay Sandbox
              </button>
              <button class="btn btn-secondary btn-lg" onclick="launchSandboxCheckout('${activeInvoice.id}', 'STRIPE')">
                ${icon('creditCard', 16)} Pay Online via Stripe Sandbox
              </button>
              <button class="btn btn-emerald btn-lg" onclick="quickManualPayInvoice('${activeInvoice.id}', '${activeInvoice.totalAmount}')">
                ${icon('bank', 16)} Record Instant NEFT / Bank Transfer
              </button>
            ` : ''}
          </div>
        </div>
      ` : `
        <div style="background:var(--accent-emerald-soft); border:1.5px solid rgba(16,185,129,0.4); border-radius:14px; padding:22px; margin-bottom:22px;">
          <div style="display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:12px; margin-bottom:10px;">
            <div>
              <span class="badge badge-emerald">${icon('check', 12)} POLICY ACTIVE &amp; VERIFIED</span>
              <h3 style="font-family:var(--font-display); font-size:20px; font-weight:800; margin-top:6px;">
                Master Policy ${escapeHtml(activePolicy.policyNo)} is Active
              </h3>
            </div>
            <div style="display:flex; gap:10px;">
              <button class="btn btn-primary" onclick="downloadAuthenticatedPdf('/policies/${activePolicy.id}/schedule.pdf', '${activePolicy.policyNo}-schedule.pdf')">
                ${icon('fileText', 15)} Download Master Policy PDF
              </button>
              <button class="btn btn-secondary" onclick="navigatePage('app-policies')">
                Manage Policy &amp; Endorsements ${icon('arrowRight', 14)}
              </button>
            </div>
          </div>
          <p style="font-size:13.5px; color:var(--text-secondary);">
            All ${state.policyMembers.length} insured members are now enrolled and covered from <strong class="mono">${escapeHtml(activePolicy.startDate)}</strong> to <strong class="mono">${escapeHtml(activePolicy.endDate)}</strong>. Download their digital Health E-Cards / Certificates of Insurance below:
          </p>
        </div>

        <h4 style="font-family:var(--font-display); font-size:15.5px; font-weight:700; margin-bottom:12px; display:flex; align-items:center; gap:8px;">
          ${icon('idCard', 18)} Issued Digital Member E-Cards &amp; Certificates of Insurance (${state.policyMembers.length})
        </h4>
        <div class="ecard-grid">
          ${state.policyMembers.map(m => `
            <div class="digital-ecard">
              <div style="display:flex; align-items:center; justify-content:space-between;">
                <div style="display:flex; align-items:center; gap:8px;">
                  ${renderBrandSvg(22)}
                  <span style="font-weight:800; font-size:13.5px;">InsureEase Digital E-Card</span>
                </div>
                <span class="badge badge-emerald">${escapeHtml(m.status)}</span>
              </div>
              <div>
                <div style="font-size:11px; color:#94A3B8; text-transform:uppercase;">Insured Member</div>
                <div style="font-size:16px; font-weight:700;">${escapeHtml(m.name)}</div>
                <div class="mono" style="font-size:11.5px; color:#38BDF8; margin-top:2px;">
                  ID: ${escapeHtml(m.memberNo)} · COI: ${escapeHtml(m.certificateNo)}
                </div>
              </div>
              <div style="display:flex; align-items:center; justify-content:space-between; padding-top:10px; border-top:1px solid rgba(255,255,255,0.12);">
                <div>
                  <div style="font-size:10.5px; color:#94A3B8;">Approved Cover</div>
                  <div class="mono" style="font-size:14px; font-weight:700; color:#34D399;">${formatInr(m.sumInsured)}</div>
                </div>
                <button class="btn btn-xs btn-primary" onclick="downloadAuthenticatedPdf('/members/${m.id}/ecard.pdf', '${m.memberNo}-ecard.pdf')">
                  ${icon('download', 13)} Download E-Card PDF
                </button>
              </div>
            </div>
          `).join('')}
        </div>
      `}
    </div>
  `;
}

/* ==========================================================================
   PAGE 5: LOGIN SCREEN (/login)
   ========================================================================== */
function renderLoginScreen() {
  return `
    <div class="auth-shell">
      <aside class="auth-hero">
        <div style="display:flex; align-items:center; justify-content:space-between;">
          <a class="brand-lockup" style="cursor:pointer; text-decoration:none;" onclick="navigatePage('home')">
            <div class="brand-logo-wrap">${renderBrandSvg(30)}</div>
            <div>
              <div class="brand-name">InsureEase <span class="brand-tag">Cloud</span></div>
              <div style="font-size:11.5px; color:#94A3B8;">Modern Group Insurance Platform</div>
            </div>
          </a>
          <button class="btn btn-xs btn-secondary" style="background:rgba(255,255,255,0.08); color:#F8FAFC; border-color:rgba(255,255,255,0.16);" onclick="navigatePage('home')">
            ${icon('arrowLeft', 13)} Back to Company Website
          </button>
        </div>

        <div class="auth-hero-body">
          <div class="hero-eyebrow">${icon('shield', 14)} <span>Simple · Transparent · Instant Policy Issuance</span></div>
          <h1 class="auth-hero-title">
            Protect Your Team with <span>Health, Term Life &amp; Accident</span> Cover in Minutes.
          </h1>
          <p class="auth-hero-sub">
            Compare group insurance plans side-by-side, add your employees, get an instant GST-compliant quote, pay online via Razorpay or Stripe, and download digital Health E-Cards immediately.
          </p>

          <div class="hero-bento-Showcase">
            <div class="hero-stat-card">
              <div class="hero-stat-label">1. Compare Plans</div>
              <div class="hero-stat-val">Health · Life · Accident</div>
              <div class="hero-stat-meta">${icon('check', 13)} Transparent rates &amp; sub-limits</div>
            </div>
            <div class="hero-stat-card">
              <div class="hero-stat-label">2. Instant Pricing</div>
              <div class="hero-stat-val">Live Quote Engine</div>
              <div class="hero-stat-meta">${icon('check', 13)} Automatic ₹50L Free Cover Limit</div>
            </div>
            <div class="hero-stat-card">
              <div class="hero-stat-label">3. Online Checkout</div>
              <div class="hero-stat-val">Razorpay &amp; Stripe</div>
              <div class="hero-stat-meta">${icon('check', 13)} Section 64VB CD Float Wallet</div>
            </div>
            <div class="hero-stat-card">
              <div class="hero-stat-label">4. Digital E-Cards</div>
              <div class="hero-stat-val">1-Click PDF COI</div>
              <div class="hero-stat-meta">${icon('check', 13)} Fast Cashless &amp; Reimbursement Claims</div>
            </div>
          </div>
        </div>

        <div class="auth-hero-footer">
          <div style="display:flex; align-items:center; gap:10px;">
            <img src="/logo-emblem.jpg" alt="InsureEase" style="width:28px; height:28px; border-radius:6px;" />
            <span>Backed by Spring Boot 3.4 &amp; PostgreSQL 16</span>
          </div>
          <span class="mono">IRDAI &amp; DPDP Compliant</span>
        </div>
      </aside>

      <main class="auth-form-pane">
        <div class="auth-card">
          <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:18px;">
            <div style="display:flex; align-items:center; gap:10px;">
              <img src="/logo-emblem.jpg" alt="InsureEase" style="width:38px; height:38px; border-radius:10px;" />
              <div>
                <div style="font-weight:800; font-size:16px;">InsureEase Portal</div>
                <div style="font-size:11.5px; color:var(--text-muted);">Multi-Tenant Role Sign In</div>
              </div>
            </div>
            <button class="btn btn-xs btn-secondary" onclick="toggleTheme()">
              ${state.theme === 'light' ? `${icon('moon', 13)} Dark` : `${icon('sun', 13)} Light`}
            </button>
          </div>

          <h2 class="auth-card-title">Welcome Back</h2>
          <p class="auth-card-subtitle" style="margin-bottom:18px;">Choose an enterprise role below or sign in to manage policies, claims, and renewals.</p>

          <span class="persona-pills-label">Quick Sign-In Personas</span>
          <div class="persona-grid">
            <button type="button" class="persona-chip active" onclick="fillPersona('ACME-INSURE','admin@acmeinsure.com','Admin@123')">
              <span class="persona-chip-role">Admin / HR</span>
              <span class="persona-chip-email">Full Access</span>
            </button>
            <button type="button" class="persona-chip" onclick="fillPersona('ACME-INSURE','underwriter@acmeinsure.com','Admin@123')">
              <span class="persona-chip-role">Underwriter</span>
              <span class="persona-chip-email">Approvals</span>
            </button>
            <button type="button" class="persona-chip" onclick="fillPersona('ACME-INSURE','adjudicator@acmeinsure.com','Admin@123')">
              <span class="persona-chip-role">Claims Desk</span>
              <span class="persona-chip-email">Payouts</span>
            </button>
          </div>

          <form onsubmit="event.preventDefault(); submitLoginForm();">
            <div class="form-group">
              <label class="form-label">Organization / Tenant Code</label>
              <input id="login-tenant" class="form-control mono" value="ACME-INSURE" required />
            </div>
            <div class="form-group">
              <label class="form-label">Email Address</label>
              <input id="login-email" type="email" class="form-control" value="admin@acmeinsure.com" required />
            </div>
            <div class="form-group" style="margin-bottom:20px;">
              <label class="form-label">Password</label>
              <input id="login-password" type="password" class="form-control" value="Admin@123" required />
            </div>
            <button type="submit" class="btn btn-primary btn-lg" style="width:100%;">
              Sign In to Operations Portal ${icon('arrowRight', 15)}
            </button>
          </form>
        </div>
      </main>
    </div>
  `;
}

window.fillPersona = function (tenant, email, password) {
  document.getElementById('login-tenant').value = tenant;
  document.getElementById('login-email').value = email;
  document.getElementById('login-password').value = password;
};

window.submitLoginForm = function () {
  handleLogin(
    document.getElementById('login-tenant').value.trim(),
    document.getElementById('login-email').value.trim(),
    document.getElementById('login-password').value
  );
};

/* ==========================================================================
   WIZARD ACTIONS & STEP CONTROLLERS
   ========================================================================== */
window.setPlanFilter = function (cat) {
  state.planCategoryFilter = cat;
  render();
};

window.choosePlanForWizard = function (planId, stayOnStep1 = false) {
  state.wizardPlanId = planId;
  const pl = state.plans.find(p => p.id === planId);
  if (pl) {
    loadDefaultSampleRosterForPlan(pl);
  }
  state.wizardStep = stayOnStep1 ? 1 : 2;
  state.wizardQuoteId = null;
  state.wizardPolicyId = null;
  state.wizardInvoiceId = null;
  if (state.currentPage !== 'quote-studio') {
    navigatePage('quote-studio');
  } else {
    render();
  }
  showToast(`Selected ${pl ? pl.name : 'Plan'}. Review your employee roster in Step 2!`);
};

window.setWizardStep = function (step) {
  state.wizardStep = step;
  render();
};

window.resetPolicyWizard = function () {
  state.wizardStep = 1;
  state.wizardQuoteId = null;
  state.wizardPolicyId = null;
  state.wizardInvoiceId = null;
  const pl = state.plans.find(p => p.id === state.wizardPlanId) || state.plans[0];
  if (pl) loadDefaultSampleRosterForPlan(pl);
  render();
};

window.removeDraftMember = function (idx) {
  state.draftRoster.splice(idx, 1);
  render();
};

window.openAddDraftMemberModal = function () {
  const selectedPlan = state.plans.find(p => p.id === state.wizardPlanId) || state.plans[0];
  const selectedProd = getProductForPlan(selectedPlan);
  const isLife = selectedProd && selectedProd.productType === 'LIFE';
  const root = document.getElementById('modal-root');

  root.innerHTML = `
    <div class="modal-backdrop" onclick="if(event.target===this) closeModal()">
      <div class="modal-card" style="max-width:520px;">
        <div class="modal-header">
          <div class="card-title">${icon('users', 18)} Add Employee / Family Member to Roster</div>
          <button class="btn btn-xs btn-ghost" onclick="closeModal()">${icon('x', 15)}</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Employee Code</label>
              <input id="dm-code" class="form-control mono" value="EMP-00${state.draftRoster.length + 1}" />
            </div>
            <div class="form-group">
              <label class="form-label">Full Name</label>
              <input id="dm-name" class="form-control" placeholder="e.g. Rohan Verma" value="Rohan Verma" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Date of Birth</label>
              <input id="dm-dob" type="date" class="form-control mono" value="1991-06-15" />
            </div>
            <div class="form-group">
              <label class="form-label">Gender</label>
              <select id="dm-gender" class="form-select">
                <option value="M">Male (M)</option>
                <option value="F">Female (F)</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Relation</label>
              <select id="dm-rel" class="form-select">
                <option value="SELF">SELF (Employee)</option>
                ${!isLife ? `
                  <option value="SPOUSE">SPOUSE</option>
                  <option value="CHILD">CHILD</option>
                  <option value="PARENT">PARENT</option>
                ` : ''}
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Annual Salary / CTC (INR)</label>
              <input id="dm-ctc" type="number" class="form-control mono" value="1500000" />
            </div>
            <div class="form-group">
              <label class="form-label">Requested Sum Insured (INR)</label>
              <input id="dm-si" type="number" class="form-control mono" value="500000" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Nominee Name (For Life / Accident)</label>
              <input id="dm-nom-name" class="form-control" value="Sunita Verma" />
            </div>
            <div class="form-group">
              <label class="form-label">Nominee Relation</label>
              <input id="dm-nom-rel" class="form-control" value="SPOUSE" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
          <button class="btn btn-primary" onclick="confirmAddDraftMember()">${icon('plus', 14)} Add to Roster</button>
        </div>
      </div>
    </div>
  `;
};

window.confirmAddDraftMember = function () {
  state.draftRoster.push({
    employeeCode: document.getElementById('dm-code').value.trim(),
    name: document.getElementById('dm-name').value.trim(),
    dob: document.getElementById('dm-dob').value,
    gender: document.getElementById('dm-gender').value,
    relation: document.getElementById('dm-rel').value,
    occupationClass: 'CLASS_I',
    annualCtc: Number(document.getElementById('dm-ctc').value || 1200000),
    sumInsured: Number(document.getElementById('dm-si').value || 500000),
    nomineeName: document.getElementById('dm-nom-name').value.trim() || 'Nominee',
    nomineeRelation: document.getElementById('dm-nom-rel').value.trim() || 'SPOUSE',
    nomineeDob: '1988-01-01',
    nomineeSharePct: 100,
  });
  closeModal();
  render();
};

window.submitWizardStep2AndCalculate = async function () {
  if (!state.draftRoster.length) {
    showToast('Please add at least one employee to the roster.', 'error');
    return;
  }
  try {
    showToast('Validating census and calculating group quote...', 'warn');
    const policyholderId = state.wizardPolicyholderId || (state.policyholders[0] && state.policyholders[0].id);
    const planId = state.wizardPlanId || (state.plans[0] && state.plans[0].id);

    const quote = await api('/quotes', {
      method: 'POST',
      body: JSON.stringify({ policyholderId, planId, loadingPct: 0 }),
    });

    await api(`/quotes/${quote.id}/census`, {
      method: 'POST',
      body: JSON.stringify({ members: state.draftRoster }),
    });

    await api(`/quotes/${quote.id}/calculate`, { method: 'POST' });

    state.wizardQuoteId = quote.id;
    state.selectedQuoteId = quote.id;
    state.wizardStep = 3;
    await refreshAllData();
    showToast(`Instant Quote ${quote.quoteNo} calculated!`);
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.quickApproveWizardUw = async function (quoteId) {
  try {
    await api(`/quotes/${quoteId}/decision`, {
      method: 'POST',
      body: JSON.stringify({
        decision: 'APPROVE',
        approvedLoadingPct: 0,
        approvedDiscountPct: 5,
        remarks: 'Approved via Policy Builder with 5% preferred group discount',
      }),
    });
    await refreshAllData();
    showToast('Underwriting approved with 5% discount!');
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.approveWizardMemberMedical = async function (quoteId, memberId) {
  try {
    await api(`/quotes/${quoteId}/members/${memberId}/uw-decision`, {
      method: 'POST',
      body: JSON.stringify({
        decision: 'APPROVE',
        remarks: 'Medical reports verified; full Sum Assured above ₹50L FCL approved',
      }),
    });
    await refreshAllData();
    showToast('Approved full cover above ₹50L Free Cover Limit and updated premium!');
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.acceptQuoteAndProceedToStep4 = async function (quoteId) {
  try {
    let q = state.quotes.find(x => x.id === quoteId);
    if (q && q.status === 'UNDER_REVIEW') {
      await api(`/quotes/${quoteId}/decision`, {
        method: 'POST',
        body: JSON.stringify({
          decision: 'APPROVE',
          approvedLoadingPct: 0,
          approvedDiscountPct: 0,
          remarks: 'Auto-approved on acceptance',
        }),
      });
    }
    q = await api(`/quotes/${quoteId}`);
    if (q.status !== 'ACCEPTED' && q.status !== 'CONVERTED') {
      await api(`/quotes/${quoteId}/accept`, { method: 'POST' });
    }

    const issueRes = await api('/policies', {
      method: 'POST',
      body: JSON.stringify({
        quoteId,
        startDate: new Date(Date.now() - 40 * 86400000).toISOString().slice(0, 10),
        paymentFrequency: 'ANNUAL',
        initialCdFloatDeposit: 500000,
      }),
    });

    const pol = issueRes.policy;
    const inv = issueRes.invoice || issueRes.initialInvoice;
    state.wizardPolicyId = pol.id;
    state.selectedPolicyId = pol.id;
    state.wizardInvoiceId = inv ? inv.id : null;
    state.wizardStep = 4;
    await refreshAllData();
    showToast(`Policy ${pol.policyNo} & GST Invoice generated! Complete payment in Step 4 to activate.`);
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.quickManualPayInvoice = async function (invoiceId, amount) {
  try {
    await api('/payments/manual', {
      method: 'POST',
      body: JSON.stringify({
        invoiceId,
        amount: Number(amount),
        mode: 'NEFT',
        utrRef: `UTR-HDFC-${Math.floor(100000 + Math.random() * 899999)}`,
      }),
    });
    await refreshAllData();
    showToast('Payment reconciled! Master Policy is now ACTIVE and Member E-Cards are ready!');
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.openPlanDetailsModal = async function (planId) {
  try {
    const pl = state.plans.find(p => p.id === planId);
    const [benefits, rates] = await Promise.all([
      api(`/plans/${planId}/benefits`).catch(() => []),
      api(`/plans/${planId}/rate-rows`).catch(() => []),
    ]);
    const root = document.getElementById('modal-root');
    root.innerHTML = `
      <div class="modal-backdrop" onclick="if(event.target===this) closeModal()">
        <div class="modal-card">
          <div class="modal-header">
            <div>
              <div class="card-title">${icon('layers', 18)} ${escapeHtml(pl.name)} — Benefits &amp; Rate Card</div>
              <div class="card-subtitle">Plan Code: <span class="mono">${escapeHtml(pl.code)}</span> · Rate Unit: <span class="mono">${escapeHtml(pl.rateUnit)}</span></div>
            </div>
            <button class="btn btn-xs btn-ghost" onclick="closeModal()">${icon('x', 15)}</button>
          </div>
          <div class="modal-body">
            <h4 style="font-size:13.5px; font-weight:700; margin-bottom:8px;">Included Benefits &amp; Sub-limits</h4>
            <div class="table-wrap" style="border:1px solid var(--border-subtle); border-radius:10px; margin-bottom:18px;">
              <table class="data-table">
                <thead>
                  <tr><th>Benefit</th><th>Description</th><th>Coverage Limit</th></tr>
                </thead>
                <tbody>
                  ${benefits.map(b => `
                    <tr>
                      <td class="mono" style="font-weight:700;">${escapeHtml(b.code)}</td>
                      <td>${escapeHtml(b.name)}</td>
                      <td class="mono" style="font-weight:700; color:var(--accent-emerald);">
                        ${b.limitType === 'AMOUNT' ? formatInr(b.limitValue) : b.limitType === 'WEEKLY_AMOUNT' ? `${formatInr(b.limitValue)} / week` : `${b.limitValue}% of SI`}
                      </td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>

            <h4 style="font-size:13.5px; font-weight:700; margin-bottom:8px;">Published Age-Band / Occupation Rate Table</h4>
            <div class="table-wrap" style="border:1px solid var(--border-subtle); border-radius:10px;">
              <table class="data-table">
                <thead>
                  <tr><th>Age Band</th><th>Occupation Class</th><th>Base Rate</th></tr>
                </thead>
                <tbody>
                  ${rates.map(r => `
                    <tr>
                      <td class="mono">${r.ageFrom} – ${r.ageTo} yrs</td>
                      <td class="mono">${escapeHtml(r.occupationClass)}</td>
                      <td class="mono" style="font-weight:700; color:var(--accent-primary);">
                        ${pl.rateUnit === 'FLAT_AMOUNT' ? `${formatInr(r.baseRate)} / yr` : `₹${r.baseRate} per ₹1,000 SI`}
                      </td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" onclick="closeModal()">Close</button>
            <button class="btn btn-primary" onclick="closeModal(); choosePlanForWizard('${pl.id}')">
              Select This Plan &amp; Open Quote Studio ${icon('arrowRight', 14)}
            </button>
          </div>
        </div>
      </div>
    `;
  } catch (err) {
    showToast(err.message, 'error');
  }
};

/* ==========================================================================
   ENTERPRISE PORTAL VIEWS (/app/policies, /app/claims, /app/billing, /app/renewals)
   ========================================================================== */
function renderPoliciesView() {
  const selectedPolicy = state.policies.find(p => p.id === state.selectedPolicyId) || state.policies[0];
  const cd = state.policyCdAccount && state.policyCdAccount.cdAccount;

  return `
    <div class="page-header">
      <div>
        <h1 class="page-title">My Master Policies, Member E-Cards &amp; Mid-Term Changes</h1>
        <p class="page-subtitle">View issued policies, download Master Policy Schedules and Member Health E-Cards, or add/remove employees mid-term.</p>
      </div>
      <div class="page-actions">
        <button class="btn btn-primary" onclick="navigatePage('quote-studio')">
          ${icon('plus', 15)} Buy / Configure Another Policy
        </button>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <div>
          <div class="card-title">${icon('shield', 18)} Issued Master Policies (${state.policies.length})</div>
          <div class="card-subtitle">Click any policy row below to view its enrolled employees, E-Cards, and mid-term endorsements</div>
        </div>
      </div>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>Policy Number</th>
              <th>Status</th>
              <th>Coverage Period</th>
              <th>Total Sum Insured</th>
              <th>Annual Premium (incl. GST)</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            ${state.policies.length === 0 ? `
              <tr><td colspan="6" style="text-align:center; padding:32px; color:var(--text-muted);">No policies issued yet. Go to <strong>4-Step Quote Studio</strong> to issue your first policy!</td></tr>
            ` : state.policies.map(p => {
              const isSelected = selectedPolicy && selectedPolicy.id === p.id;
              return `
                <tr style="${isSelected ? 'background:var(--accent-primary-soft);' : ''}" onclick="selectPolicy('${p.id}')">
                  <td class="mono" style="font-weight:700; color:var(--accent-primary);">${escapeHtml(p.policyNo)}</td>
                  <td>${statusBadge(p.status)}</td>
                  <td class="mono">${escapeHtml(p.startDate)} → ${escapeHtml(p.endDate)}</td>
                  <td class="mono">${formatInr(p.totalSumInsured)}</td>
                  <td class="mono" style="font-weight:700;">${formatInrExact(p.totalAnnualPremium)}</td>
                  <td>
                    <div style="display:flex; gap:6px;">
                      <button class="btn btn-xs btn-secondary" onclick="event.stopPropagation(); downloadAuthenticatedPdf('/policies/${p.id}/schedule.pdf', '${p.policyNo}-schedule.pdf')">
                        ${icon('fileText', 13)} Schedule PDF
                      </button>
                      <button class="btn btn-xs btn-primary" onclick="event.stopPropagation(); selectPolicy('${p.id}')">
                        ${isSelected ? `${icon('check', 13)} Viewing` : 'View Members'}
                      </button>
                    </div>
                  </td>
                </tr>
              `;
            }).join('')}
          </tbody>
        </table>
      </div>
    </div>

    ${selectedPolicy ? `
      <div class="grid-2">
        <div class="card">
          <div class="card-header">
            <div>
              <div class="card-title">${icon('idCard', 18)} Enrolled Employees &amp; Digital E-Cards (${state.policyMembers.length})</div>
              <div class="card-subtitle">Policy <span class="mono">${escapeHtml(selectedPolicy.policyNo)}</span></div>
            </div>
            ${selectedPolicy.status === 'ACTIVE' ? `
              <button class="btn btn-xs btn-primary" onclick="openEndorsementModal('${selectedPolicy.id}')">
                ${icon('plus', 13)} Add New Joiner Mid-Term
              </button>
            ` : ''}
          </div>
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Member ID / E-Card</th>
                  <th>Employee Name</th>
                  <th>Total Cover</th>
                  <th>Available Balance</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                ${state.policyMembers.length === 0 ? `
                  <tr><td colspan="6" style="text-align:center; padding:24px; color:var(--text-muted);">Policy is awaiting payment. Pay its invoice in <strong>Billing &amp; CD Wallet</strong> to activate and generate E-Cards!</td></tr>
                ` : state.policyMembers.map(m => `
                  <tr>
                    <td class="mono" style="font-weight:600;">${escapeHtml(m.memberNo)}</td>
                    <td>
                      <div style="font-weight:600;">${escapeHtml(m.name)}</div>
                      <div class="mono" style="font-size:11px; color:var(--text-muted);">${escapeHtml(m.employeeCode)}</div>
                    </td>
                    <td class="mono">${formatInr(m.sumInsured)}</td>
                    <td class="mono" style="font-weight:700; color:var(--accent-emerald);">${formatInr(m.remainingSi)}</td>
                    <td>${statusBadge(m.status)}</td>
                    <td>
                      <div style="display:flex; gap:6px;">
                        <button class="btn btn-xs btn-secondary" onclick="downloadAuthenticatedPdf('/members/${m.id}/ecard.pdf', '${m.memberNo}-ecard.pdf')">
                          ${icon('idCard', 13)} E-Card PDF
                        </button>
                        ${m.status === 'ACTIVE' ? `
                          <button class="btn btn-xs btn-crimson" onclick="triggerDeleteMemberEndorsement('${selectedPolicy.id}', '${m.employeeCode}', '${escapeHtml(m.name)}')">Remove</button>
                        ` : ''}
                      </div>
                    </td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <div>
              <div class="card-title">${icon('users', 18)} Mid-Term Endorsements (Add / Remove Employees)</div>
              <div class="card-subtitle">
                CD Float Wallet Balance: <strong class="mono" style="color:var(--accent-emerald);">${cd ? formatInrExact(cd.currentBalance) : '—'}</strong>
              </div>
            </div>
          </div>
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Endorsement No</th>
                  <th>Change Type</th>
                  <th>Pro-Rata Premium (incl. GST)</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                ${state.policyEndorsements.length === 0 ? `
                  <tr><td colspan="5" style="text-align:center; padding:24px; color:var(--text-muted);">No mid-term changes yet. Click "+ Add New Joiner Mid-Term" to test pro-rata additions!</td></tr>
                ` : state.policyEndorsements.map(e => `
                  <tr>
                    <td class="mono" style="font-weight:600;">${escapeHtml(e.endorsementNo)}</td>
                    <td>${statusBadge(e.endorsementType)}</td>
                    <td class="mono" style="font-weight:700;">${formatInrExact(e.totalPremiumDelta)}</td>
                    <td>${statusBadge(e.status)}</td>
                    <td>
                      ${e.status !== 'APPLIED' ? `
                        <button class="btn btn-xs btn-emerald" onclick="approveEndorsement('${e.id}', '${selectedPolicy.id}')">
                          ${icon('check', 13)} Approve &amp; Deduct CD Wallet
                        </button>
                      ` : `<span class="badge badge-emerald">${icon('check', 12)} Applied</span>`}
                    </td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    ` : ''}
  `;
}

function renderClaimsView() {
  return `
    <div class="page-header">
      <div>
        <h1 class="page-title">Claims Center — File &amp; Track Claims</h1>
        <p class="page-subtitle">Submit Cashless Hospitalisation, Reimbursement, Term Life Death, or Accident Disability claims and settle payouts.</p>
      </div>
      <div class="page-actions">
        <button class="btn btn-primary" onclick="openSubmitClaimModal()">
          ${icon('plus', 15)} File a New Claim
        </button>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <div>
          <div class="card-title">${icon('stethoscope', 18)} All Registered Claims (${state.claims.length})</div>
          <div class="card-subtitle">Automated checks verify Policy Status, Waiting Period, Sub-limits, Co-pay %, and Available Cover Balance</div>
        </div>
      </div>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>Claim No</th>
              <th>Claim Type</th>
              <th>Diagnosis / Cause</th>
              <th>Claimed Amount</th>
              <th>Co-Pay &amp; Sub-limit Cut</th>
              <th>Approved Payable</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            ${state.claims.length === 0 ? `
              <tr><td colspan="8" style="text-align:center; padding:28px; color:var(--text-muted);">No claims filed yet. Click "File a New Claim" above to test claim processing!</td></tr>
            ` : state.claims.map(c => `
              <tr>
                <td class="mono" style="font-weight:700; color:var(--accent-primary);">${escapeHtml(c.claimNo)}</td>
                <td>${statusBadge(c.claimCategory)} <span class="mono" style="font-size:11px;">${escapeHtml(c.benefitCode)}</span></td>
                <td>${escapeHtml(c.diagnosis || '—')}</td>
                <td class="mono">${formatInrExact(c.claimedAmount)}</td>
                <td class="mono" style="color:var(--accent-amber);">
                  -${formatInrExact(Number(c.copayAmount || 0) + Number(c.sublimitDisallowed || 0) + Number(c.nonPayableAmount || 0))}
                </td>
                <td class="mono" style="font-weight:700; color:var(--accent-emerald);">${formatInrExact(c.approvedAmount)}</td>
                <td>${statusBadge(c.status)}</td>
                <td>
                  ${!['SETTLED', 'REJECTED'].includes(c.status) ? `
                    <button class="btn btn-xs btn-emerald" onclick="openAdjudicateClaimModal('${c.id}')">
                      ${icon('check', 13)} Review &amp; Settle
                    </button>
                  ` : `<span class="badge badge-emerald">${icon('check', 12)} Paid Out</span>`}
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    </div>
  `;
}

function renderBillingView() {
  const cdDetails = state.policyCdAccount;
  const cd = cdDetails && cdDetails.cdAccount;
  const cdTxns = (cdDetails && cdDetails.transactions) || [];

  return `
    <div class="page-header">
      <div>
        <h1 class="page-title">Billing, GST Invoices &amp; Section 64VB CD Wallet</h1>
        <p class="page-subtitle">Pay open invoices via Razorpay/Stripe Sandbox or Bank Transfer, manage your Cash Deposit (CD) Float Wallet, and view the accounting ledger.</p>
      </div>
      <div class="page-actions">
        ${state.selectedPolicyId ? `
          <button class="btn btn-emerald" onclick="openCdTopupModal('${state.selectedPolicyId}')">
            ${icon('plus', 15)} Top Up CD Float Wallet
          </button>
        ` : ''}
      </div>
    </div>

    <div class="grid-2">
      <div class="card">
        <div class="card-header">
          <div>
            <div class="card-title">${icon('fileText', 18)} GST Tax Invoices &amp; Endorsement Notes</div>
            <div class="card-subtitle">Pay any unpaid invoice below to immediately activate its Master Policy</div>
          </div>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>Invoice No</th>
                <th>Net Premium</th>
                <th>GST (18%)</th>
                <th>Total Payable</th>
                <th>Status</th>
                <th>Pay Now</th>
              </tr>
            </thead>
            <tbody>
              ${state.invoices.length === 0 ? `
                <tr><td colspan="6" style="text-align:center; padding:24px; color:var(--text-muted);">No invoices generated yet.</td></tr>
              ` : state.invoices.map(inv => `
                <tr>
                  <td class="mono" style="font-weight:700;">${escapeHtml(inv.invoiceNo)}</td>
                  <td class="mono">${formatInrExact(inv.netAmount)}</td>
                  <td class="mono">${formatInrExact(inv.gstAmount)}</td>
                  <td class="mono" style="font-weight:700;">${formatInrExact(inv.totalAmount)}</td>
                  <td>${statusBadge(inv.status)}</td>
                  <td>
                    ${inv.status !== 'PAID' ? `
                      <div style="display:flex; gap:5px;">
                        <button class="btn btn-xs btn-primary" onclick="launchSandboxCheckout('${inv.id}', 'RAZORPAY')">
                          ${icon('creditCard', 12)} Razorpay
                        </button>
                        <button class="btn btn-xs btn-secondary" onclick="launchSandboxCheckout('${inv.id}', 'STRIPE')">Stripe</button>
                      </div>
                    ` : `<span class="badge badge-emerald">${icon('check', 12)} Paid</span>`}
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <div>
            <div class="card-title">${icon('wallet', 18)} Section 64VB Cash Deposit (CD) Wallet</div>
            <div class="card-subtitle">
              ${cd ? `Account <span class="mono">${escapeHtml(cd.accountNo)}</span> · Available Balance: <strong class="mono" style="color:var(--accent-emerald);">${formatInrExact(cd.currentBalance)}</strong>` : 'Used for instant mid-term employee additions'}
            </div>
          </div>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>Transaction</th>
                <th>Amount</th>
                <th>Closing Balance</th>
                <th>Details</th>
              </tr>
            </thead>
            <tbody>
              ${cdTxns.length === 0 ? `
                <tr><td colspan="4" style="text-align:center; padding:24px; color:var(--text-muted);">No CD wallet transactions yet.</td></tr>
              ` : cdTxns.map(tx => `
                <tr>
                  <td>${statusBadge(tx.txnType)}</td>
                  <td class="mono" style="font-weight:700;">${formatInrExact(tx.amount)}</td>
                  <td class="mono">${formatInrExact(tx.balanceAfter)}</td>
                  <td style="font-size:12.5px; color:var(--text-secondary);">${escapeHtml(tx.remarks)}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `;
}

function renderOverviewAndRenewalsView() {
  const d = state.dashboard || {};
  const lr = state.lossRatio || {};
  const tat = state.claimsTat || {};
  const icrVal = Number(lr.portfolioLossRatioPct || 0);

  return `
    <div class="page-header">
      <div>
        <h1 class="page-title">Portfolio Analytics, Policy Renewals &amp; Audit Trail</h1>
        <p class="page-subtitle">Real-time KPI summary, Loss Ratio (ICR %) experience-rated renewals, and immutable compliance log.</p>
      </div>
      <div class="page-actions">
        ${state.selectedPolicyId ? `
          <button class="btn btn-primary" onclick="triggerRenewalAnalysis('${state.selectedPolicyId}')">
            ${icon('refresh', 15)} Generate Renewal Quote for Selected Policy
          </button>
        ` : ''}
      </div>
    </div>

    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-card-top">
          <span class="kpi-label">Active Policies</span>
          <div class="kpi-icon-box">${icon('shield', 18)}</div>
        </div>
        <div class="kpi-value">${d.activePolicies ?? 0} <span style="font-size:14px; color:var(--text-muted);">/ ${d.totalPolicies ?? 0} issued</span></div>
        <div class="kpi-foot"><span>Health · Term Life · Accident</span></div>
      </div>

      <div class="kpi-card">
        <div class="kpi-card-top">
          <span class="kpi-label">Collected Premium</span>
          <div class="kpi-icon-box" style="background:var(--accent-emerald-soft); color:var(--accent-emerald);">${icon('wallet', 18)}</div>
        </div>
        <div class="kpi-value">${formatInr(d.totalCollectedPremium || 0)}</div>
        <div class="kpi-foot"><span>Total Billed: <strong class="mono">${formatInr(d.totalBilledPremiumWithGst || 0)}</strong></span></div>
      </div>

      <div class="kpi-card">
        <div class="kpi-card-top">
          <span class="kpi-label">Claims Loss Ratio (ICR)</span>
          <div class="kpi-icon-box" style="background:var(--accent-amber-soft); color:var(--accent-amber);">${icon('barChart', 18)}</div>
        </div>
        <div class="kpi-value">${icrVal.toFixed(2)}%</div>
        <div class="kpi-foot"><span>Incurred Claims: <strong class="mono">${formatInr(lr.portfolioIncurredClaims || 0)}</strong></span></div>
      </div>

      <div class="kpi-card">
        <div class="kpi-card-top">
          <span class="kpi-label">Claims Settled</span>
          <div class="kpi-icon-box" style="background:var(--accent-violet-soft); color:var(--accent-violet);">${icon('activity', 18)}</div>
        </div>
        <div class="kpi-value">${d.settledClaimsCount ?? 0} <span style="font-size:14px; color:var(--text-muted);">/ ${d.totalClaimsCount ?? 0} total</span></div>
        <div class="kpi-foot"><span>IRDAI SLA Compliance: <strong class="mono">${Number(tat.irdaiSlaCompliancePct ?? 100).toFixed(0)}%</strong></span></div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <div>
          <div class="card-title">${icon('refresh', 18)} Policy Renewals &amp; Experience Rating</div>
          <div class="card-subtitle">Calculates policy Loss Ratio (ICR %) and recommends renewal discount/loading automatically</div>
        </div>
      </div>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>Window</th>
              <th>Expiry Date</th>
              <th>Earned Premium</th>
              <th>Incurred Claims</th>
              <th>Loss Ratio (ICR)</th>
              <th>Renewal Rate Change</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            ${state.renewals.length === 0 ? `
              <tr><td colspan="8" style="text-align:center; padding:24px; color:var(--text-muted);">No renewal quotes generated yet. Click "Generate Renewal Quote for Selected Policy" above!</td></tr>
            ` : state.renewals.map(rc => `
              <tr>
                <td><span class="badge badge-cobalt">${escapeHtml(rc.triggerWindow)}</span></td>
                <td class="mono">${escapeHtml(rc.expiryDate)}</td>
                <td class="mono">${formatInrExact(rc.earnedPremium)}</td>
                <td class="mono">${formatInrExact(rc.incurredClaims)}</td>
                <td class="mono" style="font-weight:700;">${Number(rc.lossRatioPct).toFixed(2)}%</td>
                <td class="mono" style="font-weight:700; color:${Number(rc.recommendedRateModPct) > 0 ? 'var(--accent-amber)' : 'var(--accent-emerald)'};">
                  ${Number(rc.recommendedRateModPct) > 0 ? `+${rc.recommendedRateModPct}%` : `${rc.recommendedRateModPct}%`}
                </td>
                <td>${statusBadge(rc.status)}</td>
                <td>
                  ${rc.status !== 'ROLLED_OVER' ? `
                    <button class="btn btn-xs btn-emerald" onclick="triggerRenewalRollover('${rc.id}')">
                      ${icon('refresh', 13)} Renew &amp; Rollover Members
                    </button>
                  ` : `<span class="badge badge-emerald">${icon('check', 12)} Renewed</span>`}
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    </div>
  `;
}

function workspaceRoleLabel() {
  const roles = (state.user && state.user.roles) || [];
  if (roles.some(role => String(role).includes('PLATFORM') || String(role).includes('INSURER'))) return 'Insurer operations';
  if (roles.some(role => String(role).includes('UNDERWRITER'))) return 'Underwriting desk';
  if (roles.some(role => String(role).includes('CLAIMS'))) return 'Claims desk';
  if (roles.some(role => String(role).includes('FINANCE'))) return 'Finance desk';
  if (roles.some(role => String(role).includes('BROKER'))) return 'Broker workspace';
  if (roles.some(role => String(role).includes('EMPLOYER'))) return 'Employer workspace';
  if (roles.some(role => String(role).includes('MEMBER'))) return 'Member workspace';
  return 'Enterprise operations';
}

function renderWorkspaceDashboard() {
  const dashboard = state.dashboard || {};
  const lossRatio = state.lossRatio || {};
  const claimsTat = state.claimsTat || {};
  const pendingQuotes = state.quotes.filter(quote => ['SUBMITTED', 'UNDER_REVIEW', 'REFER_UW', 'COUNTER_OFFERED'].includes(String(quote.status).toUpperCase()));
  const pendingClaims = state.claims.filter(claim => !['SETTLED', 'REJECTED', 'CLOSED'].includes(String(claim.status).toUpperCase()));
  const pendingInvoices = state.invoices.filter(invoice => !['PAID', 'CANCELLED'].includes(String(invoice.status).toUpperCase()));

  return `
    <div class="page-header">
      <div>
        <div class="eyebrow-label">${escapeHtml(workspaceRoleLabel())}</div>
        <h1 class="page-title">Good to see you, ${escapeHtml((state.user && state.user.fullName) || 'Administrator')}</h1>
        <p class="page-subtitle">A live view of your tenant's group insurance operations, from quote intake to claim settlement.</p>
      </div>
      <div class="page-actions">
        <button class="btn btn-primary" onclick="navigatePage('app-quotes')">${icon('sparkles', 15)} Start a quote</button>
        <button class="btn btn-secondary" onclick="refreshAllData()">${icon('refresh', 14)} Refresh data</button>
      </div>
    </div>

    ${state.workspaceNotice ? `<div class="inline-notice ${state.workspaceNotice.type || 'info'}">${escapeHtml(state.workspaceNotice.message)}</div>` : ''}

    <div class="kpi-grid">
      <div class="kpi-card"><div class="kpi-card-top"><span class="kpi-label">Active policies</span><div class="kpi-icon-box">${icon('shield', 18)}</div></div><div class="kpi-value">${dashboard.activePolicies ?? 0}</div><div class="kpi-foot"><span>${dashboard.totalPolicies ?? 0} total issued</span></div></div>
      <div class="kpi-card"><div class="kpi-card-top"><span class="kpi-label">Open quotes</span><div class="kpi-icon-box" style="background:var(--accent-primary-soft); color:var(--accent-primary);">${icon('fileText', 18)}</div></div><div class="kpi-value">${pendingQuotes.length}</div><div class="kpi-foot"><span>${state.quotes.length} total quote records</span></div></div>
      <div class="kpi-card"><div class="kpi-card-top"><span class="kpi-label">Claims in motion</span><div class="kpi-icon-box" style="background:var(--accent-amber-soft); color:var(--accent-amber);">${icon('activity', 18)}</div></div><div class="kpi-value">${pendingClaims.length}</div><div class="kpi-foot"><span>${Number(claimsTat.irdaiSlaCompliancePct ?? 100).toFixed(0)}% SLA compliance</span></div></div>
      <div class="kpi-card"><div class="kpi-card-top"><span class="kpi-label">Portfolio ICR</span><div class="kpi-icon-box" style="background:var(--accent-violet-soft); color:var(--accent-violet);">${icon('barChart', 18)}</div></div><div class="kpi-value">${Number(lossRatio.portfolioLossRatioPct || 0).toFixed(1)}%</div><div class="kpi-foot"><span>${formatInr(lossRatio.portfolioIncurredClaims || 0)} incurred</span></div></div>
    </div>

    <div class="dashboard-grid">
      <section class="card">
        <div class="card-header"><div><div class="card-title">${icon('activity', 18)} Work requiring attention</div><div class="card-subtitle">Prioritised queues across the tenant</div></div></div>
        <div class="attention-list">
          <button class="attention-row" onclick="navigatePage('app-quotes')"><span class="attention-icon attention-blue">${icon('fileText', 16)}</span><span><strong>${pendingQuotes.length} quotes</strong><small>Need underwriting review or customer action</small></span><span class="attention-arrow">${icon('arrowRight', 15)}</span></button>
          <button class="attention-row" onclick="navigatePage('app-claims')"><span class="attention-icon attention-amber">${icon('stethoscope', 16)}</span><span><strong>${pendingClaims.length} claims</strong><small>Awaiting documents, decision, or settlement</small></span><span class="attention-arrow">${icon('arrowRight', 15)}</span></button>
          <button class="attention-row" onclick="navigatePage('app-billing')"><span class="attention-icon attention-green">${icon('wallet', 16)}</span><span><strong>${pendingInvoices.length} invoices</strong><small>Open invoices and payment actions</small></span><span class="attention-arrow">${icon('arrowRight', 15)}</span></button>
          <button class="attention-row" onclick="navigatePage('app-renewals')"><span class="attention-icon attention-violet">${icon('refresh', 16)}</span><span><strong>${state.renewals.length} renewals</strong><small>Experience-rated cases in the pipeline</small></span><span class="attention-arrow">${icon('arrowRight', 15)}</span></button>
        </div>
      </section>
      <section class="card">
        <div class="card-header"><div><div class="card-title">${icon('zap', 18)} Quick actions</div><div class="card-subtitle">Common actions for this workspace</div></div></div>
        <div class="quick-action-grid">
          <button class="quick-action" onclick="navigatePage('app-products')">${icon('layers', 19)}<span>Configure products</span><small>Plans, benefits and rates</small></button>
          <button class="quick-action" onclick="navigatePage('app-quotes')">${icon('users', 19)}<span>Add an employer</span><small>Start a census and quote</small></button>
          <button class="quick-action" onclick="navigatePage('app-policies')">${icon('idCard', 19)}<span>Manage members</span><small>Cards and endorsements</small></button>
          <button class="quick-action" onclick="navigatePage('app-audit')">${icon('lock', 19)}<span>Review compliance</span><small>Audit and consent records</small></button>
        </div>
      </section>
    </div>

    <section class="card">
      <div class="card-header"><div><div class="card-title">${icon('clock', 18)} Recent operational activity</div><div class="card-subtitle">Latest state changes captured for this tenant</div></div><button class="btn btn-xs btn-secondary" onclick="navigatePage('app-audit')">View audit trail ${icon('arrowRight', 13)}</button></div>
      <div class="table-wrap"><table class="data-table"><thead><tr><th>Entity</th><th>Action</th><th>Actor</th><th>Timestamp</th></tr></thead><tbody>
        ${(state.auditLogs || []).slice(0, 6).map(log => `<tr><td class="mono">${escapeHtml(log.entityType || log.entity || '—')}</td><td>${escapeHtml(log.action || '—')}</td><td>${escapeHtml(log.actorEmail || log.actor || 'System')}</td><td class="mono">${escapeHtml(log.createdAt || log.timestamp || '—')}</td></tr>`).join('') || `<tr><td colspan="4" class="table-empty">No activity has been recorded yet.</td></tr>`}
      </tbody></table></div>
    </section>
  `;
}

function renderProductsWorkspace() {
  const selectedProduct = state.products.find(product => product.id === state.selectedProductId) || state.products[0];
  const productPlans = selectedProduct ? state.plans.filter(plan => plan.productId === selectedProduct.id) : [];

  return `
    <div class="page-header"><div><div class="eyebrow-label">Product and rate configuration</div><h1 class="page-title">Configure your insurance catalogue</h1><p class="page-subtitle">Manage products, plans, benefits, rate cards, and underwriting rules from one controlled workspace.</p></div><div class="page-actions"><button class="btn btn-primary" onclick="openCreateProductModal()">${icon('plus', 15)} New product</button></div></div>
    <div class="workspace-split">
      <section class="card product-list-card"><div class="card-header"><div><div class="card-title">${icon('layers', 18)} Products</div><div class="card-subtitle">${state.products.length} configured product lines</div></div></div><div class="stack-list">
        ${state.products.map(product => `<button class="stack-list-item ${selectedProduct && selectedProduct.id === product.id ? 'selected' : ''}" onclick="selectWorkspaceProduct('${product.id}')"><span class="product-mark ${String(product.productType || '').toLowerCase()}">${escapeHtml(product.productType || 'INS')}</span><span><strong>${escapeHtml(product.name || product.code || 'Unnamed product')}</strong><small>${escapeHtml(product.code || product.productType || 'Product')}</small></span><span>${icon('arrowRight', 15)}</span></button>`).join('') || `<div class="table-empty">No products configured for this tenant.</div>`}
      </div></section>
      <section class="card"><div class="card-header"><div><div class="card-title">${icon('layers', 18)} ${escapeHtml(selectedProduct ? (selectedProduct.name || selectedProduct.code) : 'Select a product')}</div><div class="card-subtitle">${selectedProduct ? `${escapeHtml(selectedProduct.productType || 'PRODUCT')} · ${productPlans.length} plans` : 'Choose a product to inspect its plans'}</div></div></div>
        ${selectedProduct ? `<div class="detail-stat-grid"><div><span>Product code</span><strong class="mono">${escapeHtml(selectedProduct.code || '—')}</strong></div><div><span>Status</span><strong>${statusBadge(selectedProduct.status || 'ACTIVE')}</strong></div><div><span>Configured plans</span><strong>${productPlans.length}</strong></div></div><div class="section-divider"></div><div class="subsection-heading"><div><h3>Plans and coverage</h3><p>Plan-level rates and benefits are available from each detail view.</p></div><button class="btn btn-xs btn-secondary" onclick="showToast('Plan creation is available through the configured API contract.', 'warn')">${icon('plus', 13)} Add plan</button></div><div class="table-wrap"><table class="data-table"><thead><tr><th>Plan</th><th>Coverage basis</th><th>SI basis</th><th>FCL</th><th>Co-pay</th><th>Details</th></tr></thead><tbody>${productPlans.map(plan => `<tr><td><strong>${escapeHtml(plan.name || plan.code || 'Unnamed plan')}</strong><div class="mono muted-text">${escapeHtml(plan.code || '')}</div></td><td>${escapeHtml(plan.coverageBasis || '—')}</td><td>${escapeHtml(plan.siBasis || '—')}</td><td class="mono">${formatInr(plan.freeCoverLimit)}</td><td class="mono">${plan.copayPct ?? 0}%</td><td><button class="btn btn-xs btn-secondary" onclick="openPlanDetailsModal('${plan.id}')">View configuration ${icon('arrowRight', 12)}</button></td></tr>`).join('') || `<tr><td colspan="6" class="table-empty">No plans configured for this product.</td></tr>`}</tbody></table></div>` : `<div class="empty-state">Select a product from the list to inspect its plans.</div>`}
      </section>
    </div>
  `;
}

function renderQuotesWorkspace() {
  return `
    <div class="page-header"><div><div class="eyebrow-label">Employer onboarding and underwriting</div><h1 class="page-title">Quotes and employer relationships</h1><p class="page-subtitle">Move an employer from validated census to a priced, reviewed, and accepted group policy.</p></div><div class="page-actions"><button class="btn btn-secondary" onclick="navigatePage('quote-studio')">${icon('sparkles', 15)} Open quote studio</button><button class="btn btn-primary" onclick="openCreatePolicyholderModal()">${icon('plus', 15)} Add employer</button></div></div>
    <div class="kpi-strip"><div><span>Employers</span><strong>${state.policyholders.length}</strong></div><div><span>Total quotes</span><strong>${state.quotes.length}</strong></div><div><span>Under review</span><strong>${state.quotes.filter(quote => ['UNDER_REVIEW', 'REFER_UW', 'SUBMITTED'].includes(String(quote.status).toUpperCase())).length}</strong></div><div><span>Accepted</span><strong>${state.quotes.filter(quote => String(quote.status).toUpperCase() === 'ACCEPTED').length}</strong></div></div>
    <div class="card"><div class="card-header"><div><div class="card-title">${icon('fileText', 18)} Quote work queue</div><div class="card-subtitle">Select a quote to resume its live backend workflow.</div></div><div class="table-tools"><input class="input input-sm" placeholder="Search quote or employer" oninput="filterWorkspaceTable(this, 'quotes-table')" /></div></div><div class="table-wrap"><table class="data-table" id="quotes-table"><thead><tr><th>Quote</th><th>Employer</th><th>Plan</th><th>Lives</th><th>Total premium</th><th>Status</th><th>Action</th></tr></thead><tbody>${state.quotes.map(quote => { const employer = state.policyholders.find(item => item.id === quote.policyholderId); const plan = state.plans.find(item => item.id === quote.planId); return `<tr><td class="mono"><strong>${escapeHtml(quote.quoteNo || '—')}</strong></td><td>${escapeHtml(employer ? employer.legalName : quote.policyholderId || '—')}</td><td>${escapeHtml(plan ? (plan.name || plan.code) : '—')}</td><td class="mono">${quote.totalLives ?? '—'}</td><td class="mono">${formatInrExact(quote.totalPremium)}</td><td>${statusBadge(quote.status)}</td><td><button class="btn btn-xs btn-primary" onclick="openQuoteWorkspace('${quote.id}')">Open quote ${icon('arrowRight', 12)}</button></td></tr>`; }).join('') || `<tr><td colspan="7" class="table-empty">No quotes yet. Add an employer or open Quote Studio to begin.</td></tr>`}</tbody></table></div></div>
    <div class="card"><div class="card-header"><div><div class="card-title">${icon('users', 18)} Employer directory</div><div class="card-subtitle">Policyholder records available to this tenant</div></div></div><div class="table-wrap"><table class="data-table"><thead><tr><th>Legal name</th><th>Code</th><th>GSTIN</th><th>Contact</th><th>Status</th></tr></thead><tbody>${state.policyholders.map(employer => `<tr><td><strong>${escapeHtml(employer.legalName || '—')}</strong></td><td class="mono">${escapeHtml(employer.code || '—')}</td><td class="mono">${escapeHtml(employer.gstin || '—')}</td><td>${escapeHtml(employer.contactEmail || '—')}</td><td>${statusBadge(employer.status || 'ACTIVE')}</td></tr>`).join('') || `<tr><td colspan="5" class="table-empty">No employers have been added.</td></tr>`}</tbody></table></div></div>
  `;
}

function renderAuditWorkspace() {
  return `
    <div class="page-header"><div><div class="eyebrow-label">Audit and DPDP compliance</div><h1 class="page-title">A clear record of every change</h1><p class="page-subtitle">Review immutable state transitions and consent activity for ${escapeHtml((state.user && state.user.tenantCode) || 'this tenant')}.</p></div><div class="page-actions"><button class="btn btn-secondary" onclick="refreshAllData()">${icon('refresh', 14)} Refresh log</button></div></div>
    <div class="compliance-banner"><div class="compliance-icon">${icon('lock', 20)}</div><div><strong>Tenant-scoped audit trail</strong><p>Every privileged mutation is recorded with its actor, correlation context, and before/after values where available.</p></div><span class="badge badge-emerald">${state.auditLogs.length} records loaded</span></div>
    <div class="card"><div class="card-header"><div><div class="card-title">${icon('shield', 18)} Immutable audit events</div><div class="card-subtitle">Read-only compliance view</div></div><div class="table-tools"><input class="input input-sm" placeholder="Filter entity or action" oninput="filterWorkspaceTable(this, 'audit-table')" /></div></div><div class="table-wrap"><table class="data-table" id="audit-table"><thead><tr><th>Timestamp</th><th>Entity</th><th>Action</th><th>Actor</th><th>Correlation ID</th><th>Source IP</th></tr></thead><tbody>${state.auditLogs.map(log => `<tr><td class="mono">${escapeHtml(log.createdAt || log.timestamp || '—')}</td><td class="mono">${escapeHtml(log.entityType || log.entity || '—')}</td><td>${escapeHtml(log.action || '—')}</td><td>${escapeHtml(log.actorEmail || log.actor || 'System')}</td><td class="mono">${escapeHtml(log.correlationId || '—')}</td><td class="mono">${escapeHtml(log.ipAddress || log.ip || '—')}</td></tr>`).join('') || `<tr><td colspan="6" class="table-empty">No audit events have been returned by the API.</td></tr>`}</tbody></table></div></div>
  `;
}

window.selectWorkspaceProduct = function (productId) {
  state.selectedProductId = productId;
  render();
};

window.filterWorkspaceTable = function (input, tableId) {
  const query = String(input.value || '').toLowerCase();
  const table = document.getElementById(tableId);
  if (!table) return;
  Array.from(table.querySelectorAll('tbody tr')).forEach(row => {
    row.style.display = row.textContent.toLowerCase().includes(query) ? '' : 'none';
  });
};

window.openQuoteWorkspace = function (quoteId) {
  state.selectedQuoteId = quoteId;
  state.wizardQuoteId = quoteId;
  navigatePage('quote-studio');
};

window.openCreateProductModal = function () {
  const root = document.getElementById('modal-root');
  root.innerHTML = `
    <div class="modal-backdrop" onclick="if(event.target===this) closeModal()">
      <div class="modal-card" style="max-width:560px;">
        <div class="modal-header"><div class="card-title">${icon('layers', 18)} Create product line</div><button class="btn btn-xs btn-ghost" onclick="closeModal()">${icon('x', 15)}</button></div>
        <div class="modal-body">
          <div class="form-row"><div class="form-group"><label class="form-label">Product code</label><input id="product-code" class="form-control mono" value="GMC-CORP-${Math.floor(10 + Math.random() * 89)}" /></div><div class="form-group"><label class="form-label">Product type</label><select id="product-type" class="form-control"><option value="HEALTH">HEALTH / GMC</option><option value="LIFE">LIFE / GTL</option><option value="PA">PERSONAL ACCIDENT / GPA</option></select></div></div>
          <div class="form-group"><label class="form-label">Product name</label><input id="product-name" class="form-control" value="Corporate Group Health" /></div>
          <div class="form-group"><label class="form-label">Description</label><textarea id="product-description" class="form-control" rows="3">Configurable group insurance product for employer-sponsored cover.</textarea></div>
          <div class="form-group"><label class="form-label">GST percentage</label><input id="product-gst" class="form-control mono" type="number" min="0" max="100" step="0.01" value="18" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-secondary" onclick="closeModal()">Cancel</button><button class="btn btn-primary" onclick="submitCreateProduct()">${icon('check', 14)} Create product</button></div>
      </div>
    </div>
  `;
};

window.submitCreateProduct = async function () {
  try {
    await api('/products', {
      method: 'POST',
      body: JSON.stringify({
        code: document.getElementById('product-code').value.trim(),
        name: document.getElementById('product-name').value.trim(),
        productType: document.getElementById('product-type').value,
        description: document.getElementById('product-description').value.trim(),
        gstPct: Number(document.getElementById('product-gst').value || 18),
      }),
    });
    closeModal();
    await refreshAllData();
    navigatePage('app-products');
    showToast('Product created and added to the catalogue.');
  } catch (err) {
    showToast(err.message, 'error');
  }
};

/* ==========================================================================
   SHARED MODALS (CHECKOUT, ENDORSEMENT, CLAIMS, COMPANY KYC)
   ========================================================================== */
function closeModal() {
  const root = document.getElementById('modal-root');
  if (root) root.innerHTML = '';
}
window.closeModal = closeModal;

window.launchSandboxCheckout = async function (invoiceId, provider = 'RAZORPAY') {
  try {
    const order = await api(`/invoices/${invoiceId}/payment-orders`, {
      method: 'POST',
      body: JSON.stringify({ provider, mode: 'NETBANKING' }),
    });
    const helper = order.sandboxWebhookHelper;
    const root = document.getElementById('modal-root');
    root.innerHTML = `
      <div class="modal-backdrop" onclick="if(event.target===this) closeModal()">
        <div class="modal-card" style="max-width:520px;">
          <div class="gateway-top-strip">
            <div style="display:flex; align-items:center; gap:10px;">
              <div style="width:36px; height:36px; border-radius:8px; background:rgba(255,255,255,0.16); display:flex; align-items:center; justify-content:center; font-weight:800;">
                ${provider === 'RAZORPAY' ? 'RZP' : 'STR'}
              </div>
              <div>
                <div style="font-weight:700; font-size:15px;">${provider} Secure Checkout (Sandbox)</div>
                <div style="font-size:11.5px; opacity:0.85;">Invoice ${escapeHtml(order.invoiceNo)}</div>
              </div>
            </div>
            <div class="mono" style="font-size:18px; font-weight:700;">${formatInrExact(order.amount)}</div>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label class="form-label">Gateway Order ID</label>
              <input class="form-control mono" value="${escapeHtml(order.gatewayOrderId)}" readonly />
            </div>
            <div class="form-group">
              <label class="form-label">Payment Reference ID</label>
              <input class="form-control mono" value="${escapeHtml(helper.sampleGatewayPaymentId)}" readonly />
            </div>
            <div class="form-group">
              <label class="form-label">${escapeHtml(helper.signatureHeaderName)} (HMAC-SHA256 Signature)</label>
              <input id="sim-hmac" class="form-control mono" style="font-size:11.5px;" value="${escapeHtml(helper.computedHmacSignature)}" />
            </div>
            <div style="background:var(--accent-emerald-soft); border:1px solid rgba(16,185,129,0.25); padding:10px 12px; border-radius:8px; font-size:12.5px; color:var(--accent-emerald-text); display:flex; gap:8px; align-items:flex-start;">
              <span style="margin-top:2px;">${icon('check', 15)}</span>
              <span>Clicking <strong>Complete Payment Now</strong> verifies the cryptographic HMAC signature, marks the invoice PAID, activates your Master Policy, and generates Member E-Cards.</span>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
            <button class="btn btn-emerald btn-lg" onclick="confirmSandboxWebhook('${provider}', '${order.gatewayOrderId}', '${helper.sampleGatewayPaymentId}', '${order.amount}', '${helper.signatureHeaderName}')">
              ${icon('check', 15)} Complete Payment Now (${formatInrExact(order.amount)})
            </button>
          </div>
        </div>
      </div>
    `;
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.confirmSandboxWebhook = async function (provider, gatewayOrderId, gatewayPaymentId, amount, sigHeader) {
  try {
    const sig = document.getElementById('sim-hmac').value.trim();
    await api(`/webhooks/payments/${provider.toLowerCase()}`, {
      method: 'POST',
      headers: { [sigHeader]: sig },
      body: JSON.stringify({
        gatewayOrderId,
        gatewayPaymentId,
        event: 'payment.captured',
        amount: Number(amount),
        mode: 'NETBANKING',
      }),
    });
    closeModal();
    await refreshAllData();
    showToast('Payment successful! Your Master Policy is now ACTIVE and Member E-Cards are ready.');
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.openEndorsementModal = function (policyId) {
  const root = document.getElementById('modal-root');
  root.innerHTML = `
    <div class="modal-backdrop" onclick="if(event.target===this) closeModal()">
      <div class="modal-card" style="max-width:500px;">
        <div class="modal-header">
          <div class="card-title">${icon('users', 18)} Add New Joiner Employee Mid-Term</div>
          <button class="btn btn-xs btn-ghost" onclick="closeModal()">${icon('x', 15)}</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Employee Code</label>
              <input id="end-emp-code" class="form-control mono" value="EMP-NEW-${Math.floor(100 + Math.random() * 899)}" />
            </div>
            <div class="form-group">
              <label class="form-label">Employee Name</label>
              <input id="end-emp-name" class="form-control" value="Divya Krishnan" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Date of Birth</label>
              <input id="end-emp-dob" type="date" class="form-control mono" value="1995-06-15" />
            </div>
            <div class="form-group">
              <label class="form-label">Sum Insured (INR)</label>
              <input id="end-emp-si" type="number" class="form-control mono" value="500000" />
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Payment Mode for Pro-Rata Premium</label>
            <select id="end-funding" class="form-select">
              <option value="CD_ACCOUNT">Deduct Automatically from CD Float Wallet (Instant Cover)</option>
              <option value="INVOICE">Generate Separate GST Debit Note</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
          <button class="btn btn-primary" onclick="submitAddMemberEndorsement('${policyId}')">${icon('plus', 14)} Add Employee</button>
        </div>
      </div>
    </div>
  `;
};

window.submitAddMemberEndorsement = async function (policyId) {
  try {
    const empCode = document.getElementById('end-emp-code').value.trim();
    const name = document.getElementById('end-emp-name').value.trim();
    const dob = document.getElementById('end-emp-dob').value;
    const si = Number(document.getElementById('end-emp-si').value || 500000);
    const fundingMode = document.getElementById('end-funding').value;

    const res = await api(`/policies/${policyId}/endorsements`, {
      method: 'POST',
      body: JSON.stringify({
        endorsementType: 'ADD_MEMBER',
        effectiveDate: new Date().toISOString().slice(0, 10),
        fundingMode,
        remarks: `Add new joiner ${name}`,
        items: [
          {
            action: 'ADD_MEMBER',
            employeeCode: empCode,
            name,
            dob,
            gender: 'F',
            relation: 'SELF',
            occupationClass: 'CLASS_I',
            annualCtc: 1000000,
            newSumInsured: si,
            nomineeName: 'Murali Krishnan',
            nomineeRelation: 'FATHER',
            nomineeDob: '1966-04-10',
            nomineeSharePct: 100,
          },
        ],
      }),
    });
    if (res.endorsement && res.endorsement.id && fundingMode === 'CD_ACCOUNT') {
      await api(`/endorsements/${res.endorsement.id}/approve`, { method: 'POST' });
    }
    closeModal();
    await selectPolicy(policyId);
    await refreshAllData();
    showToast(`${name} added to policy and pro-rata premium deducted from CD Wallet!`);
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.triggerDeleteMemberEndorsement = async function (policyId, employeeCode, name) {
  try {
    const res = await api(`/policies/${policyId}/endorsements`, {
      method: 'POST',
      body: JSON.stringify({
        endorsementType: 'DELETE_MEMBER',
        effectiveDate: new Date().toISOString().slice(0, 10),
        fundingMode: 'CD_ACCOUNT',
        remarks: `Remove leaver ${name}`,
        items: [{ action: 'DELETE_MEMBER', employeeCode, name }],
      }),
    });
    if (res.endorsement && res.endorsement.id) {
      await api(`/endorsements/${res.endorsement.id}/approve`, { method: 'POST' });
    }
    await selectPolicy(policyId);
    await refreshAllData();
    showToast(`Removed ${name} and processed pro-rata refund check.`);
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.approveEndorsement = async function (endorsementId, policyId) {
  try {
    await api(`/endorsements/${endorsementId}/approve`, { method: 'POST' });
    await selectPolicy(policyId);
    await refreshAllData();
    showToast('Endorsement approved and applied!');
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.openSubmitClaimModal = async function () {
  const activePolicies = state.policies.filter(p => p.status === 'ACTIVE' || p.status === 'GRACE_PERIOD');
  const pol = activePolicies[0] || state.policies[0];
  if (!pol) {
    showToast('Please get and activate a policy first before filing a claim.', 'warn');
    return;
  }
  const members = await api(`/policies/${pol.id}/members`).catch(() => []);
  if (!members.length) {
    showToast('Please complete payment for your policy first so members are enrolled.', 'warn');
    return;
  }

  const root = document.getElementById('modal-root');
  root.innerHTML = `
    <div class="modal-backdrop" onclick="if(event.target===this) closeModal()">
      <div class="modal-card" style="max-width:520px;">
        <div class="modal-header">
          <div class="card-title">${icon('stethoscope', 18)} File a New Insurance Claim</div>
          <button class="btn btn-xs btn-ghost" onclick="closeModal()">${icon('x', 15)}</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">Select Insured Member (${escapeHtml(pol.policyNo)})</label>
            <select id="clm-member" class="form-select">
              ${members.map(m => `<option value="${m.id}">${escapeHtml(m.name)} (${escapeHtml(m.memberNo)} · Available Cover: ${formatInr(m.remainingSi)})</option>`).join('')}
            </select>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Claim Type</label>
              <select id="clm-cat" class="form-select">
                <option value="CASHLESS">Cashless Hospitalisation</option>
                <option value="REIMBURSEMENT">Reimbursement</option>
                <option value="DEATH_BENEFIT">Term Life Death Benefit</option>
                <option value="DISABILITY_BENEFIT">Accident Disability Benefit</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Benefit Category</label>
              <select id="clm-ben" class="form-select">
                <option value="HOSPITALISATION">In-Patient Hospitalisation</option>
                <option value="MATERNITY">Maternity Cover</option>
                <option value="DEATH">Death Benefit (100%)</option>
                <option value="PPD">Permanent Partial Disability</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Claimed Bill Amount (INR)</label>
              <input id="clm-amt" type="number" class="form-control mono" value="65000" />
            </div>
            <div class="form-group">
              <label class="form-label">Diagnosis / Reason</label>
              <input id="clm-diag" class="form-control" value="Acute Appendicitis Surgery" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
          <button class="btn btn-primary" onclick="submitNewClaim('${pol.id}')">${icon('check', 14)} Submit Claim</button>
        </div>
      </div>
    </div>
  `;
};

window.submitNewClaim = async function (policyId) {
  try {
    const memberId = document.getElementById('clm-member').value;
    const claimCategory = document.getElementById('clm-cat').value;
    const benefitCode = document.getElementById('clm-ben').value;
    const claimedAmount = Number(document.getElementById('clm-amt').value || 65000);
    const diagnosis = document.getElementById('clm-diag').value;
    const today = new Date().toISOString().slice(0, 10);

    const res = await api('/claims', {
      method: 'POST',
      body: JSON.stringify({
        policyId,
        memberId,
        hospitalId: state.hospitals[0] ? state.hospitals[0].id : null,
        claimCategory,
        benefitCode,
        disabilityPct: benefitCode === 'PPD' ? 25 : null,
        incidentDate: today,
        admissionDate: today,
        dischargeDate: today,
        preExistingDisease: false,
        diagnosis,
        claimedAmount,
        nonPayableAmount: 1000,
      }),
    });
    closeModal();
    await refreshAllData();
    showToast(`Claim ${res.claim.claimNo} submitted! Eligible payable amount: ${formatInrExact(res.claim.approvedAmount)}`);
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.openAdjudicateClaimModal = async function (claimId) {
  try {
    const details = await api(`/claims/${claimId}`);
    const c = details.claim;
    const root = document.getElementById('modal-root');
    root.innerHTML = `
      <div class="modal-backdrop" onclick="if(event.target===this) closeModal()">
        <div class="modal-card" style="max-width:500px;">
          <div class="modal-header">
            <div class="card-title">${icon('check', 18)} Review &amp; Settle Claim ${escapeHtml(c.claimNo)}</div>
            <button class="btn btn-xs btn-ghost" onclick="closeModal()">${icon('x', 15)}</button>
          </div>
          <div class="modal-body">
            <div style="background:var(--bg-subtle); padding:14px; border-radius:10px; border:1px solid var(--border-subtle); margin-bottom:14px;">
              <div>Claimed Amount: <strong class="mono">${formatInrExact(c.claimedAmount)}</strong></div>
              <div>Co-pay &amp; Sub-limit Deductions: <strong class="mono">-${formatInrExact(Number(c.copayAmount || 0) + Number(c.sublimitDisallowed || 0) + Number(c.nonPayableAmount || 0))}</strong></div>
              <div style="margin-top:4px; font-size:15px;">Net Approved Payout: <strong class="mono" style="color:var(--accent-emerald);">${formatInrExact(c.approvedAmount)}</strong></div>
            </div>
            <div class="form-group">
              <label class="form-label">Decision</label>
              <select id="adj-dec" class="form-select">
                <option value="APPROVE">Approve &amp; Transfer Settlement Payout</option>
                <option value="QUERY">Raise Query for Additional Documents</option>
                <option value="REJECT">Reject Claim</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Remarks</label>
              <input id="adj-rem" class="form-control" value="Documents verified; approved for instant settlement payout." />
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
            <button class="btn btn-emerald" onclick="submitClaimAdjudication('${c.id}', '${c.claimCategory}')">
              ${icon('check', 14)} Confirm &amp; Settle Claim
            </button>
          </div>
        </div>
      </div>
    `;
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.submitClaimAdjudication = async function (claimId, category) {
  try {
    const decision = document.getElementById('adj-dec').value;
    const remarks = document.getElementById('adj-rem').value;
    if (decision === 'APPROVE') {
      const docs = category === 'DEATH_BENEFIT'
        ? ['DEATH_CERTIFICATE', 'KYC']
        : category === 'DISABILITY_BENEFIT'
        ? ['FIR', 'DISABILITY_CERT']
        : ['DISCHARGE_SUMMARY', 'HOSPITAL_BILL'];
      for (const docType of docs) {
        await api(`/claims/${claimId}/documents`, {
          method: 'POST',
          body: JSON.stringify({
            docType,
            fileName: `${docType.toLowerCase()}.pdf`,
            contentType: 'application/pdf',
            fileSize: 180000,
          }),
        });
      }
    }
    await api(`/claims/${claimId}/decision`, {
      method: 'POST',
      body: JSON.stringify({
        decision,
        reasonCode: decision === 'REJECT' ? 'IRDAI-EXCL-04' : null,
        remarks,
      }),
    });
    closeModal();
    await refreshAllData();
    showToast('Claim settled and payout recorded in ledger!');
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.openCdTopupModal = function (policyId) {
  const root = document.getElementById('modal-root');
  root.innerHTML = `
    <div class="modal-backdrop" onclick="if(event.target===this) closeModal()">
      <div class="modal-card" style="max-width:440px;">
        <div class="modal-header">
          <div class="card-title">${icon('wallet', 18)} Top Up Section 64VB CD Float Wallet</div>
          <button class="btn btn-xs btn-ghost" onclick="closeModal()">${icon('x', 15)}</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">Top-Up Amount (INR)</label>
            <input id="cd-topup-amt" type="number" class="form-control mono" value="250000" />
          </div>
          <div class="form-group">
            <label class="form-label">Bank UTR Reference</label>
            <input id="cd-topup-ref" class="form-control mono" value="UTR-HDFC-${Math.floor(100000 + Math.random() * 899999)}" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
          <button class="btn btn-emerald" onclick="submitCdTopup('${policyId}')">${icon('plus', 14)} Add Funds</button>
        </div>
      </div>
    </div>
  `;
};

window.submitCdTopup = async function (policyId) {
  try {
    const amount = Number(document.getElementById('cd-topup-amt').value || 250000);
    const referenceNo = document.getElementById('cd-topup-ref').value;
    await api(`/policies/${policyId}/cd-account/topup`, {
      method: 'POST',
      body: JSON.stringify({ amount, referenceNo, remarks: 'CD Float Wallet Top-Up' }),
    });
    closeModal();
    await selectPolicy(policyId);
    await refreshAllData();
    showToast(`Added ${formatInrExact(amount)} to CD Wallet!`);
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.triggerRenewalAnalysis = async function (policyId) {
  try {
    const res = await api(`/policies/${policyId}/renewals`, { method: 'POST' });
    await refreshAllData();
    showToast(`Renewal Quote created! Loss Ratio: ${res.renewalCase.lossRatioPct}%`);
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.triggerRenewalRollover = async function (renewalCaseId) {
  try {
    const res = await api(`/renewals/${renewalCaseId}/rollover`, { method: 'POST' });
    await refreshAllData();
    showToast(`Renewed Policy ${res.renewedPolicy.policyNo} issued with continuous waiting-period credit!`);
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.openCreatePolicyholderModal = function () {
  const root = document.getElementById('modal-root');
  root.innerHTML = `
    <div class="modal-backdrop" onclick="if(event.target===this) closeModal()">
      <div class="modal-card" style="max-width:500px;">
        <div class="modal-header">
          <div class="card-title">${icon('building', 18)} Add New Corporate Employer Profile</div>
          <button class="btn btn-xs btn-ghost" onclick="closeModal()">${icon('x', 15)}</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Company Short Code</label>
              <input id="ph-code" class="form-control mono" value="ACME-TECH-${Math.floor(10 + Math.random() * 89)}" />
            </div>
            <div class="form-group">
              <label class="form-label">Company Legal Name</label>
              <input id="ph-name" class="form-control" value="NovaStack Technologies Pvt Ltd" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Company GSTIN</label>
              <input id="ph-gstin" class="form-control mono" value="27AABCN9988K1Z5" />
            </div>
            <div class="form-group">
              <label class="form-label">Company PAN</label>
              <input id="ph-pan" class="form-control mono" value="AABCN9988K" />
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">HR Contact Email</label>
            <input id="ph-email" class="form-control" value="hr@novastack.in" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
          <button class="btn btn-primary" onclick="submitCreatePolicyholder()">${icon('check', 14)} Save Company</button>
        </div>
      </div>
    </div>
  `;
};

window.submitCreatePolicyholder = async function () {
  try {
    const ph = await api('/policyholders', {
      method: 'POST',
      body: JSON.stringify({
        code: document.getElementById('ph-code').value.trim(),
        legalName: document.getElementById('ph-name').value.trim(),
        orgType: 'CORPORATE',
        industryCode: 'IT_SERVICES',
        gstin: document.getElementById('ph-gstin').value.trim(),
        pan: document.getElementById('ph-pan').value.trim(),
        stateCode: '27',
        addressLine: 'Bengaluru / Mumbai',
        contactName: 'HR Admin',
        contactEmail: document.getElementById('ph-email').value.trim(),
        contactMobile: '9820011223',
      }),
    });
    state.wizardPolicyholderId = ph.id;
    closeModal();
    await refreshAllData();
    showToast(`Added company ${ph.legalName}!`);
  } catch (err) {
    showToast(err.message, 'error');
  }
};

/* ==========================================================================
   ENTERPRISE OPERATIONS PORTAL SHELL (/app/*)
   ========================================================================== */
function renderWorkspaceShell() {
  const page = state.currentPage;
  const navMeta = {
    'app-dashboard': { mod: 'Workspace overview', title: 'Operations Dashboard' },
    'app-products': { mod: 'Configuration', title: 'Products, Plans & Rates' },
    'app-quotes': { mod: 'Underwriting', title: 'Employers & Quote Work Queue' },
    'app-policies': { mod: 'Policy Administration', title: 'My Active Policies, E-Cards & Endorsements' },
    'app-claims': { mod: 'Claims Assistance', title: 'Claims Center' },
    'app-billing': { mod: 'Payments & Float', title: 'Billing, GST Invoices & CD Wallet' },
    'app-renewals': { mod: 'Executive Analytics', title: 'Portfolio Overview & Renewals' },
    'app-audit': { mod: 'Compliance', title: 'Audit & Consent Records' },
  }[page] || { mod: 'InsureEase', title: 'Enterprise Operations Portal' };

  const u = state.user || {};
  const initials = (u.fullName || u.email || 'IE').slice(0, 2).toUpperCase();

  return `
    <div class="app-layout">
      <aside class="sidebar">
        <div class="sidebar-header">
          <a class="brand-lockup" style="cursor:pointer; text-decoration:none;" onclick="navigatePage('home')">
            <div class="brand-logo-wrap" style="width:36px; height:36px; border-radius:10px;">
              ${renderBrandSvg(24)}
            </div>
            <div>
              <div class="brand-name" style="font-size:17px;">
                InsureEase
                <span class="brand-tag">Cloud</span>
              </div>
              <div style="font-size:10.5px; color:#64748B;">Group Insurance Portal</div>
            </div>
          </a>
        </div>

        <div class="sidebar-tenant-bar">
          <div style="display:flex; align-items:center; gap:9px;">
            <span class="tenant-badge-dot"></span>
            <div>
              <div style="font-size:10px; color:#94A3B8; text-transform:uppercase;">Organization</div>
              <div class="mono" style="font-size:12.5px; font-weight:700; color:#F8FAFC;">${escapeHtml(u.tenantCode || 'ACME-INSURE')}</div>
            </div>
          </div>
          <span class="badge badge-emerald" style="font-size:9.5px;">LIVE</span>
        </div>

        <nav class="sidebar-nav">
          <div class="nav-section-title">Workspace</div>
          <button class="nav-item ${page === 'app-dashboard' ? 'active' : ''}" onclick="navigatePage('app-dashboard')">
            <span class="nav-item-left">${icon('layers', 16)}<span>Operations Dashboard</span></span>
          </button>
          <button class="nav-item ${page === 'app-products' ? 'active' : ''}" onclick="navigatePage('app-products')">
            <span class="nav-item-left">${icon('layers', 16)}<span>Products &amp; Rates</span></span>
            <span class="nav-pill">${state.products.length}</span>
          </button>
          <button class="nav-item ${page === 'app-quotes' ? 'active' : ''}" onclick="navigatePage('app-quotes')">
            <span class="nav-item-left">${icon('fileText', 16)}<span>Employers &amp; Quotes</span></span>
            <span class="nav-pill">${state.quotes.length}</span>
          </button>
          <button class="nav-item ${page === 'app-policies' ? 'active' : ''}" onclick="navigatePage('app-policies')">
            <span class="nav-item-left">${icon('shield', 16)}<span>My Policies &amp; E-Cards</span></span>
            <span class="nav-pill">${state.policies.length}</span>
          </button>
          <button class="nav-item ${page === 'app-claims' ? 'active' : ''}" onclick="navigatePage('app-claims')">
            <span class="nav-item-left">${icon('stethoscope', 16)}<span>Claims Center</span></span>
            <span class="nav-pill">${state.claims.length}</span>
          </button>
          <button class="nav-item ${page === 'app-billing' ? 'active' : ''}" onclick="navigatePage('app-billing')">
            <span class="nav-item-left">${icon('creditCard', 16)}<span>Billing &amp; CD Wallet</span></span>
            <span class="nav-pill">${state.invoices.length}</span>
          </button>
          <button class="nav-item ${page === 'app-renewals' ? 'active' : ''}" onclick="navigatePage('app-renewals')">
            <span class="nav-item-left">${icon('barChart', 16)}<span>Portfolio &amp; Renewals</span></span>
          </button>
          <button class="nav-item ${page === 'app-audit' ? 'active' : ''}" onclick="navigatePage('app-audit')">
            <span class="nav-item-left">${icon('lock', 16)}<span>Audit &amp; Compliance</span></span>
            <span class="nav-pill">${state.auditLogs.length}</span>
          </button>
        </nav>

        <div class="sidebar-footer">
          <div class="user-mini-card">
            <div style="display:flex; align-items:center; gap:10px; min-width:0;">
              <div class="user-avatar">${escapeHtml(initials)}</div>
              <div style="min-width:0;">
                <div style="font-size:12.5px; font-weight:600; color:#F8FAFC; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">
                  ${escapeHtml(u.fullName || 'Administrator')}
                </div>
                <div class="mono" style="font-size:10.5px; color:#94A3B8; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">
                  ${escapeHtml(u.email || '')}
                </div>
              </div>
            </div>
            <button class="btn btn-xs btn-ghost" style="color:#F87171;" onclick="logout()">Sign Out</button>
          </div>
        </div>
      </aside>

      <div class="workspace-main">
        <header class="topbar">
          <div class="topbar-left">
            <span class="breadcrumb-mod">${escapeHtml(navMeta.mod)}</span>
            <span class="breadcrumb-sep">/</span>
            <span class="breadcrumb-page">${escapeHtml(navMeta.title)}</span>
          </div>
          <div class="topbar-right">
            <span class="api-health-pill">${icon('check', 13)} System Online</span>
            <button class="btn btn-xs btn-secondary" onclick="toggleTheme()">
              ${state.theme === 'light' ? `${icon('moon', 13)} Dark` : `${icon('sun', 13)} Light`}
            </button>
          </div>
        </header>

        <main class="page-container">
          ${page === 'app-dashboard' ? renderWorkspaceDashboard() : ''}
          ${page === 'app-products' ? renderProductsWorkspace() : ''}
          ${page === 'app-quotes' ? renderQuotesWorkspace() : ''}
          ${page === 'app-policies' ? renderPoliciesView() : ''}
          ${page === 'app-claims' ? renderClaimsView() : ''}
          ${page === 'app-billing' ? renderBillingView() : ''}
          ${page === 'app-renewals' ? renderOverviewAndRenewalsView() : ''}
          ${page === 'app-audit' ? renderAuditWorkspace() : ''}
        </main>
      </div>
    </div>
  `;
}

/* ==========================================================================
   MASTER RENDER DISPATCHER
   ========================================================================== */
function render() {
  const app = document.getElementById('app');
  if (!app) return;

  const p = state.currentPage;
  if (p === 'login') {
    app.innerHTML = renderLoginScreen();
    return;
  }
  if (p === 'home') {
    app.innerHTML = renderCompanyHomePage();
    return;
  }
  if (p === 'how-it-works') {
    app.innerHTML = renderHowItWorksPage();
    return;
  }
  if (p === 'plans') {
    app.innerHTML = renderPlansExplorerPage();
    return;
  }
  if (p === 'quote-studio') {
    app.innerHTML = renderQuoteStudioPage();
    return;
  }
  if (!state.token) {
    app.innerHTML = renderLoginScreen();
    return;
  }
  if (p === 'app-dashboard' || p === 'app-products' || p === 'app-quotes' || p === 'app-policies' || p === 'app-claims' || p === 'app-billing' || p === 'app-renewals' || p === 'app-audit') {
    app.innerHTML = renderWorkspaceShell();
    return;
  }
  app.innerHTML = renderWorkspaceShell();
}

startCinemaTicker();
ensureDemoSessionAndLoad();
