/* ==========================================================================
   InsureEase — Vector SVG Icon System & Animated Process Video Engine
   Zero Emojis · 100% Pure Vector SVG Graphics & Keyframe Motion Scenes
   ========================================================================== */

window.icon = function (name, size = 18, extraClass = '') {
  const attrs = `width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon ${extraClass}"`;
  const icons = {
    'shield': `<svg ${attrs}><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><path d="m9 12 2 2 4-4"/></svg>`,
    'heart-pulse': `<svg ${attrs}><path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/><path d="M3.22 12H9.5l.5-1 2 4.5 2-7 1.5 3.5h5.27"/></svg>`,
    'umbrella': `<svg ${attrs}><path d="M22 12a10.06 10.06 1 0 0-20 0Z"/><path d="M12 12v8a2 2 0 0 0 4 0"/><path d="M12 2v1"/></svg>`,
    'activity': `<svg ${attrs}><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>`,
    'users': `<svg ${attrs}><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
    'user-plus': `<svg ${attrs}><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><line x1="19" y1="8" x2="19" y2="14"/><line x1="22" y1="11" x2="16" y2="11"/></svg>`,
    'calculator': `<svg ${attrs}><rect width="16" height="20" x="4" y="2" rx="2"/><line x1="8" x2="16" y1="6" y2="6"/><line x1="16" x2="16" y1="14" y2="18"/><path d="M16 10h.01"/><path d="M12 10h.01"/><path d="M8 10h.01"/><path d="M12 14h.01"/><path d="M8 14h.01"/><path d="M12 18h.01"/><path d="M8 18h.01"/></svg>`,
    'credit-card': `<svg ${attrs}><rect width="20" height="14" x="2" y="5" rx="2"/><line x1="2" x2="22" y1="10" y2="10"/></svg>`,
    'wallet': `<svg ${attrs}><path d="M21 12V7H5a2 2 0 0 1 0-4h14v4"/><path d="M3 5v14a2 2 0 0 0 2 2h16v-5"/><path d="M18 12a2 2 0 0 0 0 4h4v-4Z"/></svg>`,
    'file-text': `<svg ${attrs}><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/><line x1="16" x2="8" y1="13" y2="13"/><line x1="16" x2="8" y1="17" y2="17"/><line x1="10" x2="8" y1="9" y2="9"/></svg>`,
    'file-check': `<svg ${attrs}><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/><path d="m9 15 2 2 4-4"/></svg>`,
    'id-card': `<svg ${attrs}><rect width="20" height="14" x="2" y="5" rx="2"/><circle cx="8" cy="12" r="2"/><path d="M14 10h4"/><path d="M14 14h4"/></svg>`,
    'check': `<svg ${attrs}><polyline points="20 6 9 17 4 12"/></svg>`,
    'check-circle': `<svg ${attrs}><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>`,
    'play': `<svg ${attrs}><polygon points="5 3 19 12 5 21 5 3" fill="currentColor"/></svg>`,
    'pause': `<svg ${attrs}><rect width="4" height="16" x="6" y="4" fill="currentColor"/><rect width="4" height="16" x="14" y="4" fill="currentColor"/></svg>`,
    'video': `<svg ${attrs}><path d="m22 8-6 4 6 4V8Z"/><rect width="14" height="12" x="2" y="6" rx="2" ry="2"/></svg>`,
    'arrow-right': `<svg ${attrs}><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>`,
    'arrow-left': `<svg ${attrs}><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>`,
    'external-link': `<svg ${attrs}><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" x2="21" y1="14" y2="3"/></svg>`,
    'download': `<svg ${attrs}><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" x2="12" y1="15" y2="3"/></svg>`,
    'building': `<svg ${attrs}><rect width="16" height="20" x="4" y="2" rx="2" ry="2"/><path d="M9 22v-4h6v4"/><path d="M8 6h.01"/><path d="M16 6h.01"/><path d="M12 6h.01"/><path d="M12 10h.01"/><path d="M12 14h.01"/><path d="M16 10h.01"/><path d="M16 14h.01"/><path d="M8 10h.01"/><path d="M8 14h.01"/></svg>`,
    'lock': `<svg ${attrs}><rect width="18" height="11" x="3" y="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>`,
    'sparkles': `<svg ${attrs}><path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"/></svg>`,
    'refresh': `<svg ${attrs}><path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/><path d="M21 3v5h-5"/><path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/><path d="M8 16H3v5"/></svg>`,
    'sun': `<svg ${attrs}><circle cx="12" cy="12" r="4"/><path d="M12 2v2"/><path d="M12 20v2"/><path d="m4.93 4.93 1.41 1.41"/><path d="m17.66 17.66 1.41 1.41"/><path d="M2 12h2"/><path d="M20 12h2"/><path d="m6.34 17.66-1.41 1.41"/><path d="m19.07 4.93-1.41 1.41"/></svg>`,
    'moon': `<svg ${attrs}><path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/></svg>`,
    'layers': `<svg ${attrs}><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg>`,
    'bar-chart': `<svg ${attrs}><line x1="12" x2="12" y1="20" y2="10"/><line x1="18" x2="18" y1="20" y2="4"/><line x1="6" x2="6" y1="20" y2="16"/></svg>`,
    'close': `<svg ${attrs}><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>`,
    'plus': `<svg ${attrs}><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>`,
    'search': `<svg ${attrs}><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>`,
    'globe': `<svg ${attrs}><circle cx="12" cy="12" r="10"/><line x1="2" x2="22" y1="12" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>`,
    'compass': `<svg ${attrs}><circle cx="12" cy="12" r="10"/><polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76"/></svg>`,
    'zap': `<svg ${attrs}><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>`,
  };
  return icons[name] || icons['shield'];
};

/* ==========================================================================
   INTERACTIVE PROCESS VIDEO CINEMA PLAYER (4 ANIMATED WORKFLOW VIDEOS)
   ========================================================================== */
window.CINEMA_CHAPTERS = [
  {
    id: 0,
    tag: 'PROCESS VIDEO 01 · PLAN ANALYSIS',
    title: 'How to Compare & Choose the Right Group Plan',
    duration: '00:08',
    summary: 'See how HR & Finance teams compare Group Health (GMC), Group Term Life (GTL), and Personal Accident (GPA) coverage, waiting periods, and sub-limits before buying.',
    ctaLabel: 'Open Plan Explorer & Compare Live',
    ctaRoute: '/plans',
  },
  {
    id: 1,
    tag: 'PROCESS VIDEO 02 · INSTANT QUOTING & FCL',
    title: 'How Census Validation & Free Cover Limit Pricing Works',
    duration: '00:08',
    summary: 'Watch how uploading your employee roster automatically calculates age-band & salary-multiple rates, applies 18% GST, and caps Term Life above ₹50L FCL.',
    ctaLabel: 'Launch Interactive Quote Studio',
    ctaRoute: '/quote-studio',
  },
  {
    id: 2,
    tag: 'PROCESS VIDEO 03 · 64VB PAYMENT & E-CARDS',
    title: 'How Online Checkout Issues Your Policy & Digital E-Cards',
    duration: '00:08',
    summary: 'See how paying an invoice via Razorpay/Stripe HMAC webhook reconciles the double-entry ledger, activates the Master Policy, and generates Member E-Cards.',
    ctaLabel: 'Start 4-Step Policy Issuance',
    ctaRoute: '/quote-studio',
  },
  {
    id: 3,
    tag: 'PROCESS VIDEO 04 · CLAIMS & ICR RENEWALS',
    title: 'How 1-Hour Cashless Claims & Experience Renewals Work',
    duration: '00:08',
    summary: 'Understand how our 5-Step Eligibility Chain verifies coverage, applies co-pay & sub-limits, settles hospital payouts, and calculates renewal Loss Ratio (ICR %).',
    ctaLabel: 'Explore Claims & Renewals Console',
    ctaRoute: '/app/claims',
  },
];

window.renderCinemaSceneSvg = function (chapterIndex) {
  if (chapterIndex === 0) {
    // SCENE 1: Animated Plan Comparison & Benefit Radar
    return `
      <svg viewBox="0 0 820 390" class="cinema-svg" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <linearGradient id="sc1-bg" x1="0" y1="0" x2="820" y2="390" gradientUnits="userSpaceOnUse">
            <stop offset="0%" stop-color="#0B1120"/>
            <stop offset="100%" stop-color="#0F172A"/>
          </linearGradient>
          <linearGradient id="sc1-beam" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stop-color="#10B981" stop-opacity="0"/>
            <stop offset="50%" stop-color="#10B981" stop-opacity="0.45"/>
            <stop offset="100%" stop-color="#3B82F6" stop-opacity="0"/>
          </linearGradient>
        </defs>
        <rect width="820" height="390" rx="14" fill="url(#sc1-bg)"/>
        <!-- Animated Grid Lines -->
        <g stroke="rgba(255,255,255,0.04)" stroke-width="1">
          <line x1="0" y1="78" x2="820" y2="78"/><line x1="0" y1="156" x2="820" y2="156"/>
          <line x1="0" y1="234" x2="820" y2="234"/><line x1="0" y1="312" x2="820" y2="312"/>
          <line x1="205" y1="0" x2="205" y2="390"/><line x1="410" y1="0" x2="410" y2="390"/><line x1="615" y1="0" x2="615" y2="390"/>
        </g>

        <!-- Header Banner inside Video -->
        <rect x="28" y="22" width="764" height="44" rx="8" fill="#1E293B" stroke="rgba(255,255,255,0.1)"/>
        <circle cx="50" cy="44" r="6" fill="#10B981" class="pulse-node"/>
        <text x="68" y="49" fill="#F8FAFC" font-family="Plus Jakarta Sans, sans-serif" font-size="13" font-weight="700">STEP 1: ANALYZE &amp; SELECT COVERAGE ARCHITECTURE</text>
        <text x="630" y="48" fill="#38BDF8" font-family="JetBrains Mono, monospace" font-size="11" font-weight="600">3 ACTIVE PRODUCT LINES</text>

        <!-- Card 1: Group Health (GMC) -->
        <g class="anim-float-1">
          <rect x="28" y="86" width="242" height="240" rx="12" fill="#111827" stroke="#10B981" stroke-width="2"/>
          <rect x="44" y="104" width="90" height="22" rx="11" fill="rgba(16,185,129,0.18)"/>
          <text x="56" y="119" fill="#34D399" font-family="JetBrains Mono, monospace" font-size="10" font-weight="700">GMC · HEALTH</text>
          <text x="44" y="150" fill="#FFFFFF" font-family="Plus Jakarta Sans, sans-serif" font-size="15" font-weight="800">Gold Family Floater</text>
          <text x="44" y="170" fill="#94A3B8" font-family="Inter, sans-serif" font-size="11">Employee + Spouse + Kids + Parents</text>
          <rect x="44" y="186" width="210" height="44" rx="8" fill="#1E293B"/>
          <text x="56" y="204" fill="#94A3B8" font-family="Inter, sans-serif" font-size="10">FLOATER SUM INSURED</text>
          <text x="56" y="221" fill="#34D399" font-family="JetBrains Mono, monospace" font-size="15" font-weight="700">INR 5,00,000 / Family</text>
          <text x="44" y="254" fill="#E2E8F0" font-family="Inter, sans-serif" font-size="11.5">✓ INR 75,000 Maternity Cover</text>
          <text x="44" y="274" fill="#E2E8F0" font-family="Inter, sans-serif" font-size="11.5">✓ 1-Hr Cashless Network SLA</text>
          <rect x="44" y="290" width="210" height="24" rx="6" fill="#10B981"/>
          <text x="86" y="306" fill="#062E22" font-family="Inter, sans-serif" font-size="11" font-weight="800">SELECTED FOR QUOTE</text>
        </g>

        <!-- Card 2: Group Term Life (GTL) -->
        <g class="anim-float-2">
          <rect x="289" y="86" width="242" height="240" rx="12" fill="#111827" stroke="#3B82F6" stroke-width="1.5"/>
          <rect x="305" y="104" width="90" height="22" rx="11" fill="rgba(59,130,246,0.2)"/>
          <text x="317" y="119" fill="#60A5FA" font-family="JetBrains Mono, monospace" font-size="10" font-weight="700">GTL · TERM LIFE</text>
          <text x="305" y="150" fill="#FFFFFF" font-family="Plus Jakarta Sans, sans-serif" font-size="15" font-weight="800">Executive 3x CTC Life</text>
          <text x="305" y="170" fill="#94A3B8" font-family="Inter, sans-serif" font-size="11">Salary-Multiple Income Protection</text>
          <rect x="305" y="186" width="210" height="44" rx="8" fill="#1E293B"/>
          <text x="317" y="204" fill="#94A3B8" font-family="Inter, sans-serif" font-size="10">FREE COVER LIMIT (NO MEDICAL)</text>
          <text x="317" y="221" fill="#60A5FA" font-family="JetBrains Mono, monospace" font-size="15" font-weight="700">INR 50,00,000 FCL</text>
          <text x="305" y="254" fill="#E2E8F0" font-family="Inter, sans-serif" font-size="11.5">✓ 100% Lump-Sum Death Payout</text>
          <text x="305" y="274" fill="#E2E8F0" font-family="Inter, sans-serif" font-size="11.5">✓ Per-Mille Mortality Pricing</text>
          <rect x="305" y="290" width="210" height="24" rx="6" fill="rgba(59,130,246,0.2)" stroke="#3B82F6"/>
          <text x="355" y="306" fill="#93C5FD" font-family="Inter, sans-serif" font-size="11" font-weight="700">COMPARE RATES</text>
        </g>

        <!-- Card 3: Group Personal Accident (GPA) -->
        <g class="anim-float-3">
          <rect x="550" y="86" width="242" height="240" rx="12" fill="#111827" stroke="#8B5CF6" stroke-width="1.5"/>
          <rect x="566" y="104" width="104" height="22" rx="11" fill="rgba(139,92,246,0.2)"/>
          <text x="578" y="119" fill="#C4B5FD" font-family="JetBrains Mono, monospace" font-size="10" font-weight="700">GPA · ACCIDENT</text>
          <text x="566" y="150" fill="#FFFFFF" font-family="Plus Jakarta Sans, sans-serif" font-size="15" font-weight="800">24x7 Accident Shield</text>
          <text x="566" y="170" fill="#94A3B8" font-family="Inter, sans-serif" font-size="11">Occupation Class I, II &amp; III</text>
          <rect x="566" y="186" width="210" height="44" rx="8" fill="#1E293B"/>
          <text x="578" y="204" fill="#94A3B8" font-family="Inter, sans-serif" font-size="10">DISABILITY BENEFIT TABLES</text>
          <text x="578" y="221" fill="#C4B5FD" font-family="JetBrains Mono, monospace" font-size="15" font-weight="700">Table A · B · C · D</text>
          <text x="566" y="254" fill="#E2E8F0" font-family="Inter, sans-serif" font-size="11.5">✓ PTD (100%) &amp; PPD (% Scale)</text>
          <text x="566" y="274" fill="#E2E8F0" font-family="Inter, sans-serif" font-size="11.5">✓ Weekly Disablement Cash</text>
          <rect x="566" y="290" width="210" height="24" rx="6" fill="rgba(139,92,246,0.2)" stroke="#8B5CF6"/>
          <text x="615" y="306" fill="#DDD6FE" font-family="Inter, sans-serif" font-size="11" font-weight="700">COMPARE RATES</text>
        </g>

        <!-- Bottom Animated Scanner Bar -->
        <rect x="28" y="342" width="764" height="30" rx="8" fill="#1E293B"/>
        <rect x="28" y="342" width="380" height="30" rx="8" fill="url(#sc1-beam)" class="anim-scan-bar"/>
        <text x="46" y="361" fill="#94A3B8" font-family="JetBrains Mono, monospace" font-size="11">
          LIVE ENGINE: Zero Age-Band Overlap Verified · IRDAI Product Filing Active · Instant Quote Ready
        </text>
      </svg>
    `;
  }

  if (chapterIndex === 1) {
    // SCENE 2: Bulk Census Upload, Validation & Instant Pricing + FCL Cap
    return `
      <svg viewBox="0 0 820 390" class="cinema-svg" xmlns="http://www.w3.org/2000/svg">
        <rect width="820" height="390" rx="14" fill="#0B1120"/>
        <rect x="28" y="22" width="764" height="44" rx="8" fill="#1E293B" stroke="rgba(255,255,255,0.1)"/>
        <circle cx="50" cy="44" r="6" fill="#38BDF8" class="pulse-node"/>
        <text x="68" y="49" fill="#F8FAFC" font-family="Plus Jakarta Sans, sans-serif" font-size="13" font-weight="700">STEP 2: CENSUS VALIDATION, FCL CAP &amp; INSTANT GST PRICING</text>
        <text x="615" y="48" fill="#34D399" font-family="JetBrains Mono, monospace" font-size="11" font-weight="600">BR-1 · BR-6 · BR-12 ACTIVE</text>

        <!-- Left Roster Stream Table -->
        <rect x="28" y="82" width="465" height="284" rx="12" fill="#111827" stroke="rgba(255,255,255,0.1)"/>
        <text x="46" y="110" fill="#94A3B8" font-family="JetBrains Mono, monospace" font-size="11" font-weight="700">VALIDATED EMPLOYEE &amp; DEPENDENT CENSUS</text>

        <!-- Row 1 -->
        <g class="anim-row-1">
          <rect x="44" y="124" width="433" height="58" rx="8" fill="#1E293B" stroke="#10B981" stroke-width="1.2"/>
          <text x="58" y="146" fill="#FFFFFF" font-family="Inter, sans-serif" font-size="12.5" font-weight="700">EMP-101 · Siddharth Malhotra (34/M · SELF)</text>
          <text x="58" y="166" fill="#94A3B8" font-family="JetBrains Mono, monospace" font-size="11">SI: INR 5,00,000  |  Rate: INR 6,000.00</text>
          <rect x="364" y="138" width="98" height="24" rx="12" fill="rgba(16,185,129,0.2)"/>
          <text x="378" y="154" fill="#34D399" font-family="JetBrains Mono, monospace" font-size="10" font-weight="700">✓ VALIDATED</text>
        </g>

        <!-- Row 2 -->
        <g class="anim-row-2">
          <rect x="44" y="194" width="433" height="58" rx="8" fill="#1E293B" stroke="#10B981" stroke-width="1.2"/>
          <text x="58" y="216" fill="#FFFFFF" font-family="Inter, sans-serif" font-size="12.5" font-weight="700">EMP-101 · Neha Malhotra (31/F · SPOUSE)</text>
          <text x="58" y="236" fill="#94A3B8" font-family="JetBrains Mono, monospace" font-size="11">Family Floater Member  |  Rate: INR 6,000.00</text>
          <rect x="364" y="208" width="98" height="24" rx="12" fill="rgba(16,185,129,0.2)"/>
          <text x="378" y="224" fill="#34D399" font-family="JetBrains Mono, monospace" font-size="10" font-weight="700">✓ FLOATER OK</text>
        </g>

        <!-- Row 3 (FCL Capped Executive) -->
        <g class="anim-row-3">
          <rect x="44" y="264" width="433" height="82" rx="8" fill="#1E293B" stroke="#F59E0B" stroke-width="1.5"/>
          <text x="58" y="286" fill="#FFFFFF" font-family="Inter, sans-serif" font-size="12.5" font-weight="700">EMP-102 · Vikramaditya Rao (CEO · 3x CTC = INR 75L)</text>
          <text x="58" y="306" fill="#FBBF24" font-family="JetBrains Mono, monospace" font-size="11">BR-12 FCL Cap Applied: Active Cover = INR 50,00,000</text>
          <text x="58" y="326" fill="#94A3B8" font-family="Inter, sans-serif" font-size="11">Excess INR 25,00,000 routed for 1-Click Medical UW Approval</text>
          <rect x="348" y="276" width="116" height="24" rx="12" fill="rgba(245,158,11,0.2)"/>
          <text x="358" y="292" fill="#FBBF24" font-family="JetBrains Mono, monospace" font-size="10" font-weight="700">FCL CAPPED 50L</text>
        </g>

        <!-- Right Live Receipt Calculation Box -->
        <rect x="512" y="82" width="280" height="284" rx="12" fill="#111827" stroke="#3B82F6" stroke-width="2"/>
        <text x="532" y="112" fill="#60A5FA" font-family="JetBrains Mono, monospace" font-size="11" font-weight="700">INSTANT QUOTE RECEIPT</text>
        <text x="532" y="136" fill="#FFFFFF" font-family="Plus Jakarta Sans, sans-serif" font-size="18" font-weight="800">QT-2026-1001</text>

        <line x1="532" y1="152" x2="772" y2="152" stroke="rgba(255,255,255,0.1)"/>
        <text x="532" y="176" fill="#94A3B8" font-family="Inter, sans-serif" font-size="12">Base Annual Premium</text>
        <text x="690" y="176" fill="#F8FAFC" font-family="JetBrains Mono, monospace" font-size="12" font-weight="700">INR 18,000</text>

        <text x="532" y="204" fill="#94A3B8" font-family="Inter, sans-serif" font-size="12">Group Discount (5%)</text>
        <text x="690" y="204" fill="#34D399" font-family="JetBrains Mono, monospace" font-size="12" font-weight="700">-INR 900</text>

        <text x="532" y="232" fill="#94A3B8" font-family="Inter, sans-serif" font-size="12">Net Taxable Premium</text>
        <text x="690" y="232" fill="#F8FAFC" font-family="JetBrains Mono, monospace" font-size="12" font-weight="700">INR 17,100</text>

        <text x="532" y="260" fill="#94A3B8" font-family="Inter, sans-serif" font-size="12">GST (9% CGST + 9% SGST)</text>
        <text x="690" y="260" fill="#F8FAFC" font-family="JetBrains Mono, monospace" font-size="12" font-weight="700">INR 3,078</text>

        <rect x="532" y="282" width="240" height="62" rx="10" fill="rgba(37,99,235,0.2)" stroke="#3B82F6"/>
        <text x="548" y="304" fill="#93C5FD" font-family="Inter, sans-serif" font-size="11" font-weight="600">TOTAL PAYABLE PREMIUM</text>
        <text x="548" y="330" fill="#FFFFFF" font-family="JetBrains Mono, monospace" font-size="20" font-weight="800">INR 20,178.00</text>
      </svg>
    `;
  }

  if (chapterIndex === 2) {
    // SCENE 3: Section 64VB Payment, Razorpay HMAC & E-Card Generation
    return `
      <svg viewBox="0 0 820 390" class="cinema-svg" xmlns="http://www.w3.org/2000/svg">
        <rect width="820" height="390" rx="14" fill="#0B1120"/>
        <rect x="28" y="22" width="764" height="44" rx="8" fill="#1E293B" stroke="rgba(255,255,255,0.1)"/>
        <circle cx="50" cy="44" r="6" fill="#10B981" class="pulse-node"/>
        <text x="68" y="49" fill="#F8FAFC" font-family="Plus Jakarta Sans, sans-serif" font-size="13" font-weight="700">STEP 3: RAZORPAY HMAC CHECKOUT, 64VB LEDGER &amp; DIGITAL E-CARDS</text>
        <text x="605" y="48" fill="#38BDF8" font-family="JetBrains Mono, monospace" font-size="11" font-weight="600">HMAC-SHA256 VERIFIED</text>

        <!-- Step Box 1: Gateway Webhook -->
        <rect x="28" y="92" width="230" height="260" rx="12" fill="#111827" stroke="#3B82F6" stroke-width="1.5"/>
        <text x="46" y="122" fill="#60A5FA" font-family="JetBrains Mono, monospace" font-size="11" font-weight="700">1. GATEWAY ORDER</text>
        <text x="46" y="148" fill="#FFFFFF" font-family="Plus Jakarta Sans, sans-serif" font-size="15" font-weight="800">Razorpay / Stripe</text>
        <rect x="46" y="166" width="194" height="52" rx="8" fill="#1E293B"/>
        <text x="58" y="186" fill="#94A3B8" font-family="JetBrains Mono, monospace" font-size="10">order_rzp_98f2a1c4b</text>
        <text x="58" y="206" fill="#34D399" font-family="JetBrains Mono, monospace" font-size="11" font-weight="700">X-Razorpay-Signature: OK</text>
        <text x="46" y="246" fill="#CBD5E1" font-family="Inter, sans-serif" font-size="11.5">✓ Double-Entry Ledger:</text>
        <text x="46" y="268" fill="#34D399" font-family="JetBrains Mono, monospace" font-size="10.5">DR CASH_BANK   20,178</text>
        <text x="46" y="286" fill="#60A5FA" font-family="JetBrains Mono, monospace" font-size="10.5">CR AR_POLICY   20,178</text>
        <rect x="46" y="308" width="194" height="26" rx="6" fill="rgba(16,185,129,0.2)"/>
        <text x="72" y="325" fill="#34D399" font-family="JetBrains Mono, monospace" font-size="11" font-weight="700">INVOICE REPAID</text>

        <!-- Animated Connector Arrow 1 -->
        <path d="M264 220 L292 220" stroke="#38BDF8" stroke-width="3" stroke-dasharray="4 4" class="anim-dash"/>

        <!-- Step Box 2: Master Policy & CD Wallet -->
        <rect x="298" y="92" width="224" height="260" rx="12" fill="#111827" stroke="#10B981" stroke-width="1.5"/>
        <text x="316" y="122" fill="#34D399" font-family="JetBrains Mono, monospace" font-size="11" font-weight="700">2. POLICY &amp; 64VB FLOAT</text>
        <text x="316" y="148" fill="#FFFFFF" font-family="Plus Jakarta Sans, sans-serif" font-size="15" font-weight="800">POL-HEALTH-2026-1</text>
        <rect x="316" y="166" width="188" height="52" rx="8" fill="#1E293B"/>
        <text x="328" y="186" fill="#94A3B8" font-family="Inter, sans-serif" font-size="10">SECTION 64VB CD WALLET</text>
        <text x="328" y="206" fill="#34D399" font-family="JetBrains Mono, monospace" font-size="14" font-weight="700">INR 5,00,000.00</text>
        <text x="316" y="246" fill="#CBD5E1" font-family="Inter, sans-serif" font-size="11.5">✓ Instant Joiner Additions</text>
        <text x="316" y="268" fill="#CBD5E1" font-family="Inter, sans-serif" font-size="11.5">✓ Pro-Rata Auto-Debit (BR-2)</text>
        <rect x="316" y="308" width="188" height="26" rx="6" fill="#10B981"/>
        <text x="358" y="325" fill="#062E22" font-family="JetBrains Mono, monospace" font-size="11" font-weight="800">STATUS: ACTIVE</text>

        <!-- Animated Connector Arrow 2 -->
        <path d="M528 220 L556 220" stroke="#10B981" stroke-width="3" stroke-dasharray="4 4" class="anim-dash"/>

        <!-- Step Box 3: Digital Health E-Card -->
        <g class="anim-float-1">
          <rect x="562" y="92" width="230" height="260" rx="12" fill="url(#sc1-bg)" stroke="#38BDF8" stroke-width="2"/>
          <text x="580" y="122" fill="#38BDF8" font-family="JetBrains Mono, monospace" font-size="11" font-weight="700">3. DIGITAL HEALTH E-CARD</text>
          <rect x="580" y="140" width="194" height="128" rx="10" fill="#1E293B" stroke="rgba(255,255,255,0.18)"/>
          <text x="594" y="164" fill="#34D399" font-family="Plus Jakarta Sans, sans-serif" font-size="12" font-weight="800">InsureEase Cashless Card</text>
          <text x="594" y="188" fill="#FFFFFF" font-family="Inter, sans-serif" font-size="13" font-weight="700">Siddharth Malhotra</text>
          <text x="594" y="208" fill="#94A3B8" font-family="JetBrains Mono, monospace" font-size="10.5">MEM-2026-1001 · COI-1001</text>
          <text x="594" y="232" fill="#38BDF8" font-family="JetBrains Mono, monospace" font-size="12" font-weight="700">Cover: INR 5,00,000</text>
          <rect x="580" y="286" width="194" height="44" rx="8" fill="#2563EB"/>
          <text x="612" y="313" fill="#FFFFFF" font-family="Inter, sans-serif" font-size="12" font-weight="700">Download E-Card PDF</text>
        </g>
      </svg>
    `;
  }

  // SCENE 4: 5-Step Claims Eligibility Chain & ICR Experience Renewal
  return `
    <svg viewBox="0 0 820 390" class="cinema-svg" xmlns="http://www.w3.org/2000/svg">
      <rect width="820" height="390" rx="14" fill="#0B1120"/>
      <rect x="28" y="22" width="764" height="44" rx="8" fill="#1E293B" stroke="rgba(255,255,255,0.1)"/>
      <circle cx="50" cy="44" r="6" fill="#A855F7" class="pulse-node"/>
      <text x="68" y="49" fill="#F8FAFC" font-family="Plus Jakarta Sans, sans-serif" font-size="13" font-weight="700">STEP 4: 5-STEP CLAIMS ELIGIBILITY, PAYOUT &amp; ICR RENEWAL ENGINE</text>
      <text x="605" y="48" fill="#34D399" font-family="JetBrains Mono, monospace" font-size="11" font-weight="600">IRDAI 1-HR CASHLESS SLA</text>

      <!-- 5-Step Eligibility Chain Pipeline -->
      <rect x="28" y="84" width="764" height="122" rx="12" fill="#111827" stroke="rgba(255,255,255,0.1)"/>
      <text x="46" y="110" fill="#94A3B8" font-family="JetBrains Mono, monospace" font-size="11" font-weight="700">AUTOMATED 5-STEP CLAIM ELIGIBILITY PIPELINE (BR-3 · BR-4 · BR-5)</text>

      <g transform="translate(46, 126)">
        <rect x="0" y="0" width="132" height="58" rx="8" fill="#1E293B" stroke="#10B981"/>
        <text x="14" y="24" fill="#34D399" font-family="JetBrains Mono, monospace" font-size="10" font-weight="700">1. POLICY STATUS</text>
        <text x="14" y="44" fill="#FFFFFF" font-family="Inter, sans-serif" font-size="11.5" font-weight="600">✓ ACTIVE (BR-4)</text>

        <rect x="146" y="0" width="132" height="58" rx="8" fill="#1E293B" stroke="#10B981"/>
        <text x="160" y="24" fill="#34D399" font-family="JetBrains Mono, monospace" font-size="10" font-weight="700">2. MEMBER COVER</text>
        <text x="160" y="44" fill="#FFFFFF" font-family="Inter, sans-serif" font-size="11.5" font-weight="600">✓ Date Verified</text>

        <rect x="292" y="0" width="132" height="58" rx="8" fill="#1E293B" stroke="#10B981"/>
        <text x="306" y="24" fill="#34D399" font-family="JetBrains Mono, monospace" font-size="10" font-weight="700">3. WAITING PERIOD</text>
        <text x="306" y="44" fill="#FFFFFF" font-family="Inter, sans-serif" font-size="11.5" font-weight="600">✓ 30d Passed (BR-3)</text>

        <rect x="438" y="0" width="132" height="58" rx="8" fill="#1E293B" stroke="#10B981"/>
        <text x="452" y="24" fill="#34D399" font-family="JetBrains Mono, monospace" font-size="10" font-weight="700">4. SUB-LIMIT / COPAY</text>
        <text x="452" y="44" fill="#FFFFFF" font-family="Inter, sans-serif" font-size="11.5" font-weight="600">✓ 10% Co-pay Math</text>

        <rect x="584" y="0" width="144" height="58" rx="8" fill="rgba(16,185,129,0.2)" stroke="#10B981" stroke-width="2"/>
        <text x="598" y="24" fill="#34D399" font-family="JetBrains Mono, monospace" font-size="10" font-weight="700">5. UTR SETTLEMENT</text>
        <text x="598" y="44" fill="#FFFFFF" font-family="JetBrains Mono, monospace" font-size="12" font-weight="800">PAID TO HOSPITAL</text>
      </g>

      <!-- Bottom Split: Claim Math + Renewal ICR Experience -->
      <rect x="28" y="222" width="372" height="144" rx="12" fill="#111827" stroke="rgba(255,255,255,0.1)"/>
      <text x="46" y="250" fill="#60A5FA" font-family="JetBrains Mono, monospace" font-size="11" font-weight="700">AUTOMATED BENEFIT &amp; SETTLEMENT VOUCHER</text>
      <text x="46" y="276" fill="#E2E8F0" font-family="Inter, sans-serif" font-size="12">Claimed Hospital Bill: <tspan font-family="JetBrains Mono, monospace" font-weight="700">INR 85,000</tspan></text>
      <text x="46" y="300" fill="#FBBF24" font-family="Inter, sans-serif" font-size="12">Deductible &amp; 10% Co-Pay: <tspan font-family="JetBrains Mono, monospace" font-weight="700">-INR 15,250</tspan></text>
      <text x="46" y="328" fill="#34D399" font-family="Inter, sans-serif" font-size="14" font-weight="800">Net Settled via UTR: <tspan font-family="JetBrains Mono, monospace">INR 69,750.00</tspan></text>

      <rect x="420" y="222" width="372" height="144" rx="12" fill="#111827" stroke="rgba(255,255,255,0.1)"/>
      <text x="438" y="250" fill="#C4B5FD" font-family="JetBrains Mono, monospace" font-size="11" font-weight="700">EXPERIENCE-RATED RENEWAL ENGINE (BR-7)</text>
      <text x="438" y="276" fill="#E2E8F0" font-family="Inter, sans-serif" font-size="12">Incurred Claims Ratio (ICR): <tspan fill="#34D399" font-family="JetBrains Mono, monospace" font-weight="700">42.50% (Healthy)</tspan></text>
      <text x="438" y="300" fill="#E2E8F0" font-family="Inter, sans-serif" font-size="12">BR-7 Renewal Rate Modifier: <tspan fill="#34D399" font-family="JetBrains Mono, monospace" font-weight="700">-5.00% Discount</tspan></text>
      <text x="438" y="328" fill="#93C5FD" font-family="Inter, sans-serif" font-size="12" font-weight="700">✓ Continuous Waiting-Period Tenure Preserved</text>
    </svg>
  `;
};

/* ==========================================================================
   MINI ANIMATED SVG EXPLAINER CARDS FOR EACH OF THE 4 USER JOURNEY STEPS
   ========================================================================== */
window.renderMiniStepAnimation = function (stepNum) {
  if (stepNum === 1) {
    return `
      <svg viewBox="0 0 360 150" class="mini-step-svg" xmlns="http://www.w3.org/2000/svg">
        <rect width="360" height="150" rx="10" fill="#0F172A"/>
        <rect x="16" y="20" width="98" height="110" rx="8" fill="#1E293B" stroke="#10B981" stroke-width="1.5" class="anim-float-1"/>
        <circle cx="36" cy="40" r="8" fill="rgba(16,185,129,0.25)"/>
        <text x="28" y="68" fill="#FFFFFF" font-family="Inter" font-size="11" font-weight="700">Health GMC</text>
        <text x="28" y="86" fill="#34D399" font-family="JetBrains Mono" font-size="10">INR 5L Floater</text>
        <rect x="28" y="100" width="74" height="16" rx="4" fill="#10B981"/>

        <rect x="131" y="20" width="98" height="110" rx="8" fill="#1E293B" stroke="#3B82F6" stroke-width="1.5" class="anim-float-2"/>
        <circle cx="151" cy="40" r="8" fill="rgba(59,130,246,0.25)"/>
        <text x="143" y="68" fill="#FFFFFF" font-family="Inter" font-size="11" font-weight="700">Term Life GTL</text>
        <text x="143" y="86" fill="#60A5FA" font-family="JetBrains Mono" font-size="10">3x CTC · 50L FCL</text>
        <rect x="143" y="100" width="74" height="16" rx="4" fill="#3B82F6"/>

        <rect x="246" y="20" width="98" height="110" rx="8" fill="#1E293B" stroke="#8B5CF6" stroke-width="1.5" class="anim-float-3"/>
        <circle cx="266" cy="40" r="8" fill="rgba(139,92,246,0.25)"/>
        <text x="258" y="68" fill="#FFFFFF" font-family="Inter" font-size="11" font-weight="700">Accident GPA</text>
        <text x="258" y="86" fill="#C4B5FD" font-family="JetBrains Mono" font-size="10">Table A-D Cover</text>
        <rect x="258" y="100" width="74" height="16" rx="4" fill="#8B5CF6"/>
      </svg>
    `;
  }
  if (stepNum === 2) {
    return `
      <svg viewBox="0 0 360 150" class="mini-step-svg" xmlns="http://www.w3.org/2000/svg">
        <rect width="360" height="150" rx="10" fill="#0F172A"/>
        <rect x="18" y="20" width="195" height="32" rx="6" fill="#1E293B" stroke="#10B981" class="anim-row-1"/>
        <text x="30" y="40" fill="#E2E8F0" font-family="JetBrains Mono" font-size="10">EMP-01 · 32y · SELF · OK</text>
        <rect x="18" y="60" width="195" height="32" rx="6" fill="#1E293B" stroke="#10B981" class="anim-row-2"/>
        <text x="30" y="80" fill="#E2E8F0" font-family="JetBrains Mono" font-size="10">EMP-01 · 29y · SPOUSE · OK</text>
        <rect x="18" y="100" width="195" height="32" rx="6" fill="#1E293B" stroke="#F59E0B" class="anim-row-3"/>
        <text x="30" y="120" fill="#FBBF24" font-family="JetBrains Mono" font-size="10">EMP-02 · CEO · FCL 50L CAP</text>

        <rect x="228" y="20" width="114" height="112" rx="8" fill="rgba(37,99,235,0.18)" stroke="#3B82F6"/>
        <text x="240" y="46" fill="#93C5FD" font-family="JetBrains Mono" font-size="9.5" font-weight="700">INSTANT QUOTE</text>
        <text x="240" y="74" fill="#FFFFFF" font-family="JetBrains Mono" font-size="14" font-weight="800">INR 20,178</text>
        <text x="240" y="94" fill="#34D399" font-family="Inter" font-size="10">incl. 18% GST</text>
        <rect x="240" y="106" width="90" height="16" rx="4" fill="#2563EB"/>
      </svg>
    `;
  }
  if (stepNum === 3) {
    return `
      <svg viewBox="0 0 360 150" class="mini-step-svg" xmlns="http://www.w3.org/2000/svg">
        <rect width="360" height="150" rx="10" fill="#0F172A"/>
        <rect x="18" y="24" width="142" height="102" rx="8" fill="#1E293B" stroke="#3B82F6"/>
        <text x="30" y="48" fill="#60A5FA" font-family="JetBrains Mono" font-size="10" font-weight="700">RAZORPAY / STRIPE</text>
        <text x="30" y="70" fill="#FFFFFF" font-family="Inter" font-size="11.5" font-weight="700">HMAC Verified</text>
        <text x="30" y="92" fill="#34D399" font-family="JetBrains Mono" font-size="10">● INVOICE PAID</text>
        <path d="M166 75 L194 75" stroke="#10B981" stroke-width="2.5" stroke-dasharray="4 4" class="anim-dash"/>
        <rect x="200" y="24" width="142" height="102" rx="8" fill="#1E293B" stroke="#10B981" class="anim-float-1"/>
        <text x="212" y="48" fill="#34D399" font-family="JetBrains Mono" font-size="10" font-weight="700">DIGITAL E-CARD</text>
        <text x="212" y="70" fill="#FFFFFF" font-family="Inter" font-size="11.5" font-weight="700">POL-HEALTH-2026</text>
        <text x="212" y="90" fill="#94A3B8" font-family="JetBrains Mono" font-size="9.5">COI &amp; PDF Ready</text>
        <rect x="212" y="100" width="118" height="16" rx="4" fill="#10B981"/>
      </svg>
    `;
  }
  return `
    <svg viewBox="0 0 360 150" class="mini-step-svg" xmlns="http://www.w3.org/2000/svg">
      <rect width="360" height="150" rx="10" fill="#0F172A"/>
      <rect x="18" y="24" width="324" height="44" rx="8" fill="#1E293B" stroke="#10B981"/>
      <circle cx="38" cy="46" r="6" fill="#10B981" class="pulse-node"/>
      <text x="54" y="50" fill="#FFFFFF" font-family="Inter" font-size="11.5" font-weight="700">5-Step Claim Eligibility → Settled via UTR</text>
      <rect x="18" y="82" width="324" height="44" rx="8" fill="#1E293B" stroke="#38BDF8"/>
      <text x="34" y="108" fill="#93C5FD" font-family="JetBrains Mono" font-size="11" font-weight="700">Renewal ICR: 42.5% → -5% Discount Rollover</text>
    </svg>
  `;
};
