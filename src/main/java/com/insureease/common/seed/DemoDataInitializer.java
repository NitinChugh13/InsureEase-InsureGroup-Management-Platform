package com.insureease.common.seed;

import com.insureease.claims.service.ClaimService;
import com.insureease.common.tenant.TenantContext;
import com.insureease.iam.entity.Tenant;
import com.insureease.iam.repository.AppUserRepository;
import com.insureease.iam.repository.TenantRepository;
import com.insureease.iam.service.IamService;
import com.insureease.product.entity.Plan;
import com.insureease.product.entity.Product;
import com.insureease.product.service.ProductService;
import com.insureease.quote.service.QuoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final boolean seedEnabled;
    private final TenantRepository tenantRepository;
        private final AppUserRepository appUserRepository;
    private final IamService iamService;
    private final ProductService productService;
    private final QuoteService quoteService;
    private final ClaimService claimService;

    public DemoDataInitializer(@Value("${insureease.seed.enabled:true}") boolean seedEnabled,
                               TenantRepository tenantRepository,
                               AppUserRepository appUserRepository,
                               IamService iamService,
                               ProductService productService,
                               QuoteService quoteService,
                               ClaimService claimService) {
        this.seedEnabled = seedEnabled;
        this.tenantRepository = tenantRepository;
        this.appUserRepository = appUserRepository;
        this.iamService = iamService;
        this.productService = productService;
        this.quoteService = quoteService;
        this.claimService = claimService;
    }

    @Override
    public void run(String... args) {
                if (!seedEnabled) {
            return;
        }

                Tenant existingTenant = tenantRepository.findByCodeIgnoreCase("ACME-INSURE").orElse(null);
                if (existingTenant != null) {
                        try {
                                TenantContext.setTenantId(existingTenant.getId());
                                TenantContext.setUserEmail("admin@acmeinsure.com");
                                ensureDemoUsers(existingTenant);
                        } finally {
                                TenantContext.clear();
                        }
                        return;
                }

        try {
            Tenant tenant = iamService.createTenant(new IamService.CreateTenantRequest(
                    "ACME-INSURE",
                    "Acme General & Life Insurance Co. Ltd.",
                    "INSURER",
                    "ENTERPRISE",
                    "admin@acmeinsure.com",
                    "Admin@123",
                    "Nitin Chugh (Insurer Admin)"
            ));

            TenantContext.setTenantId(tenant.getId());
            TenantContext.setUserEmail("admin@acmeinsure.com");
            ensureDemoUsers(tenant);

            // 1. Seed Group Health (GMC) Product, Plan, Benefits & Rate Table
            Product gmc = productService.createProduct(new ProductService.CreateProductRequest(
                    "GMC-CORP", "Group Mediclaim (Health)", "HEALTH",
                    "Comprehensive inpatient hospitalisation & maternity cover for employees and family",
                    new BigDecimal("18.00")
            ));
            Plan gmcPlan = productService.createPlan(gmc.getId(), new ProductService.CreatePlanRequest(
                    "GMC-GOLD-FLOATER", "Gold Family Floater 5L", "FAMILY_FLOATER", "FLAT_SLAB",
                    null, new BigDecimal("100000.00"), new BigDecimal("2000000.00"), null,
                    "FLAT_AMOUNT", new BigDecimal("10.00"), new BigDecimal("5000.00"),
                    30, 365, 5, 25, 80, 65, new BigDecimal("15.00")
            ));
            productService.addBenefit(gmcPlan.getId(), new ProductService.CreateBenefitRequest(
                    "MATERNITY", "Maternity Normal & C-Section Sub-limit", "MATERNITY",
                    "AMOUNT", new BigDecimal("75000.00"), 270
            ));
            productService.addBenefit(gmcPlan.getId(), new ProductService.CreateBenefitRequest(
                    "HOSPITALISATION", "In-Patient Hospitalisation up to 100% SI", "HOSPITALISATION",
                    "PCT_OF_SI", new BigDecimal("100.00"), 30
            ));
            productService.createRateTable(gmcPlan.getId(), new ProductService.CreateRateTableRequest(
                    "GMC FY2026 Standard Rate Card", LocalDate.now().minusMonths(1), null,
                    List.of(
                            new ProductService.RateRowInput(0, 25, "ANY", "ANY", "ALL", null, new BigDecimal("4500.00")),
                            new ProductService.RateRowInput(26, 35, "ANY", "ANY", "ALL", null, new BigDecimal("6000.00")),
                            new ProductService.RateRowInput(36, 45, "ANY", "ANY", "ALL", null, new BigDecimal("8500.00")),
                            new ProductService.RateRowInput(46, 60, "ANY", "ANY", "ALL", null, new BigDecimal("14000.00")),
                            new ProductService.RateRowInput(61, 80, "ANY", "ANY", "ALL", null, new BigDecimal("24000.00"))
                    )
            ));

            // 2. Seed Group Term Life (GTL) Product, Plan (Salary Multiple + FCL) & Per-Mille Rate Table
            Product gtl = productService.createProduct(new ProductService.CreateProductRequest(
                    "GTL-CORP", "Group Term Life Assurance", "LIFE",
                    "Salary-multiple employee term life cover with Free Cover Limit (FCL)",
                    new BigDecimal("18.00")
            ));
            Plan gtlPlan = productService.createPlan(gtl.getId(), new ProductService.CreatePlanRequest(
                    "GTL-EXEC-3X", "Executive 3x CTC Term Life (FCL 50L)", "INDIVIDUAL", "SALARY_MULTIPLE",
                    new BigDecimal("3.00"), new BigDecimal("500000.00"), new BigDecimal("20000000.00"),
                    new BigDecimal("5000000.00"), // Free Cover Limit = 50 Lakhs
                    "PER_MILLE", BigDecimal.ZERO, BigDecimal.ZERO,
                    0, 0, 0, 25, 80, 65, BigDecimal.ZERO
            ));
            productService.addBenefit(gtlPlan.getId(), new ProductService.CreateBenefitRequest(
                    "DEATH", "Lump-sum Death Benefit (100% SA)", "DEATH",
                    "PCT_OF_SI", new BigDecimal("100.00"), 0
            ));
            productService.createRateTable(gtlPlan.getId(), new ProductService.CreateRateTableRequest(
                    "GTL Mortality Table (Per Mille)", LocalDate.now().minusMonths(1), null,
                    List.of(
                            new ProductService.RateRowInput(18, 30, "ANY", "SELF", "ALL", null, new BigDecimal("1.20")),
                            new ProductService.RateRowInput(31, 40, "ANY", "SELF", "ALL", null, new BigDecimal("1.80")),
                            new ProductService.RateRowInput(41, 50, "ANY", "SELF", "ALL", null, new BigDecimal("3.50")),
                            new ProductService.RateRowInput(51, 65, "ANY", "SELF", "ALL", null, new BigDecimal("7.50"))
                    )
            ));

            // 3. Seed Group Personal Accident (GPA) Product, Plan & Occupation Class Rate Table
            Product gpa = productService.createProduct(new ProductService.CreateProductRequest(
                    "GPA-CORP", "Group Personal Accident", "PA",
                    "24x7 worldwide accidental death and disability cover across Occupation Classes I, II, III",
                    new BigDecimal("18.00")
            ));
            Plan gpaPlan = productService.createPlan(gpa.getId(), new ProductService.CreatePlanRequest(
                    "GPA-SHIELD", "Comprehensive Accident Shield (Table A-D)", "INDIVIDUAL", "FLAT_SLAB",
                    null, new BigDecimal("500000.00"), new BigDecimal("10000000.00"), null,
                    "PER_MILLE", BigDecimal.ZERO, BigDecimal.ZERO,
                    0, 0, 0, 25, 80, 65, BigDecimal.ZERO
            ));
            productService.addBenefit(gpaPlan.getId(), new ProductService.CreateBenefitRequest(
                    "DEATH", "Table A: Accidental Death (100% SI)", "DEATH", "PCT_OF_SI", new BigDecimal("100.00"), 0
            ));
            productService.addBenefit(gpaPlan.getId(), new ProductService.CreateBenefitRequest(
                    "PTD", "Table B: Permanent Total Disability (100% SI)", "PTD", "PCT_OF_SI", new BigDecimal("100.00"), 0
            ));
            productService.addBenefit(gpaPlan.getId(), new ProductService.CreateBenefitRequest(
                    "PPD", "Table C: Permanent Partial Disability (% Scale)", "PPD", "PCT_OF_SI", new BigDecimal("100.00"), 0
            ));
            productService.addBenefit(gpaPlan.getId(), new ProductService.CreateBenefitRequest(
                    "TTD", "Table D: Temporary Total Disability Weekly Indemnity", "TTD", "WEEKLY_AMOUNT", new BigDecimal("10000.00"), 0
            ));
            productService.createRateTable(gpaPlan.getId(), new ProductService.CreateRateTableRequest(
                    "GPA Occupation Class Rate Card (Per Mille)", LocalDate.now().minusMonths(1), null,
                    List.of(
                            new ProductService.RateRowInput(18, 65, "ANY", "SELF", "CLASS_I", null, new BigDecimal("0.60")),
                            new ProductService.RateRowInput(18, 65, "ANY", "SELF", "CLASS_II", null, new BigDecimal("1.10")),
                            new ProductService.RateRowInput(18, 65, "ANY", "SELF", "CLASS_III", null, new BigDecimal("2.25"))
                    )
            ));

            // 4. Seed Sample Employer & Network Hospital
            quoteService.createPolicyholder(new QuoteService.CreatePolicyholderRequest(
                    "HCL-DEMO", "HCLTech Enterprise Solutions Pvt. Ltd.", "CORPORATE", "IT_SERVICES",
                    "27AAACH1234F1Z5", "AAACH1234F", "27", "Plot 3A, Sector 126, Noida / Mumbai",
                    "Aarti Sharma (HR Head)", "hr.benefits@hcl-demo.com", "9876543210"
            ));
            claimService.createHospital(new ClaimService.CreateHospitalRequest(
                    "HOSP-APOLLO-BLR", "Apollo Speciality Hospital", "Bengaluru", "Karnataka",
                    true, "00112233445566", "HDFC0001234"
            ));

            log.info("InsureEase Demo Data Initialized: Tenant=ACME-INSURE, Admin=admin@acmeinsure.com / Admin@123, Products=[GMC-CORP, GTL-CORP, GPA-CORP]");
        } finally {
            TenantContext.clear();
        }
    }

        private void ensureDemoUsers(Tenant tenant) {
                createDemoUserIfMissing(tenant, "underwriter@acmeinsure.com", "Senior Underwriter",
                                Set.of("ROLE_UNDERWRITER", "ROLE_SENIOR_UNDERWRITER"));
                createDemoUserIfMissing(tenant, "adjudicator@acmeinsure.com", "Senior Claims Officer",
                                Set.of("ROLE_CLAIMS_OFFICER", "ROLE_SENIOR_CLAIMS_OFFICER"));
        }

        private void createDemoUserIfMissing(Tenant tenant, String email, String fullName, Set<String> roles) {
                if (appUserRepository.findByTenantIdAndEmailIgnoreCase(tenant.getId(), email).isPresent()) {
                        return;
                }
                iamService.registerUser(new IamService.RegisterUserRequest(
                                tenant.getCode(), email, "Admin@123", fullName, roles, null, null
                ));
        }
}
