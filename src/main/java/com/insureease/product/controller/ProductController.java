package com.insureease.product.controller;

import com.insureease.product.entity.Benefit;
import com.insureease.product.entity.Plan;
import com.insureease.product.entity.Product;
import com.insureease.product.entity.RateRow;
import com.insureease.product.entity.UnderwritingRuleDef;
import com.insureease.product.service.ProductService;
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
@Tag(name = "02. Product & Rate Configuration (M2)", description = "Configure GMC (Health), GTL (Term Life), and GPA (Personal Accident) products, plans, benefits, rate cards, and underwriting rules")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Define a group insurance product: HEALTH (GMC), LIFE (GTL), or PA (GPA) (FR-2.1)")
    public Product createProduct(@Valid @RequestBody ProductService.CreateProductRequest request) {
        return productService.createProduct(request);
    }

    @GetMapping("/products")
    @Operation(summary = "List all products for active tenant")
    public List<Product> listProducts() {
        return productService.listProducts();
    }

    @PostMapping("/products/{productId}/plans")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a plan under a product with coverage basis, SI basis, FCL, co-pay, and waiting periods (FR-2.1)")
    public Plan createPlan(@PathVariable UUID productId,
                           @Valid @RequestBody ProductService.CreatePlanRequest request) {
        return productService.createPlan(productId, request);
    }

    @GetMapping("/plans")
    @Operation(summary = "List plans (optionally filtered by productId)")
    public List<Plan> listPlans(@RequestParam(required = false) UUID productId) {
        return productService.listPlans(productId);
    }

    @GetMapping("/plans/{planId}")
    @Operation(summary = "Get plan details")
    public Plan getPlan(@PathVariable UUID planId) {
        return productService.getPlan(planId);
    }

    @PostMapping("/plans/{planId}/benefits")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add benefit / sub-limit / GPA disability scale entry to a plan (FR-2.1)")
    public Benefit addBenefit(@PathVariable UUID planId,
                              @Valid @RequestBody ProductService.CreateBenefitRequest request) {
        return productService.addBenefit(planId, request);
    }

    @GetMapping("/plans/{planId}/benefits")
    @Operation(summary = "List benefits configured on a plan")
    public List<Benefit> getBenefits(@PathVariable UUID planId) {
        return productService.getPlanBenefits(planId);
    }

    @PostMapping("/plans/{planId}/rate-tables")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a versioned rate table with age-band overlap validation (FR-2.2)")
    public Map<String, Object> createRateTable(@PathVariable UUID planId,
                                               @Valid @RequestBody ProductService.CreateRateTableRequest request) {
        return productService.createRateTable(planId, request);
    }

    @GetMapping("/plans/{planId}/rate-rows")
    @Operation(summary = "Get active rate rows for a plan")
    public List<RateRow> getActiveRateRows(@PathVariable UUID planId) {
        return productService.getActiveRateRows(planId);
    }

    @PostMapping("/plans/{planId}/underwriting-rules")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add an underwriting referral/decline rule to a plan (FR-2.4)")
    public UnderwritingRuleDef addRule(@PathVariable UUID planId,
                                       @Valid @RequestBody ProductService.CreateUwRuleRequest request) {
        return productService.addUnderwritingRule(planId, request);
    }

    @GetMapping("/plans/{planId}/underwriting-rules")
    @Operation(summary = "List active underwriting rules for a plan")
    public List<UnderwritingRuleDef> getRules(@PathVariable UUID planId) {
        return productService.getPlanRules(planId);
    }
}
