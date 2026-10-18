package com.insureease.iam.controller;

import com.insureease.common.tenant.TenantContext;
import com.insureease.iam.entity.AppUser;
import com.insureease.iam.entity.Tenant;
import com.insureease.iam.service.IamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "01. Tenant & IAM (M1)", description = "Multi-tenant onboarding, JWT authentication, refresh token rotation, and RBAC")
public class AuthController {

    private final IamService iamService;

    public AuthController(IamService iamService) {
        this.iamService = iamService;
    }

    @PostMapping("/tenants")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Onboard a new Insurer, TPA, or Broker tenant with an optional initial admin user (FR-1.1)")
    public Tenant createTenant(@Valid @RequestBody IamService.CreateTenantRequest request) {
        return iamService.createTenant(request);
    }

    @GetMapping("/tenants")
    @Operation(summary = "List registered tenants")
    public List<Tenant> listTenants() {
        return iamService.listTenants();
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a user under a tenant with role assignments (FR-1.2, FR-1.3)")
    public AppUser register(@Valid @RequestBody IamService.RegisterUserRequest request) {
        return iamService.registerUser(request);
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Authenticate user and issue JWT access + rotating refresh token (FR-1.2, FR-1.4)")
    public IamService.AuthTokensResponse login(@Valid @RequestBody IamService.LoginRequest request) {
        return iamService.login(request);
    }

    public record RefreshTokenRequest(@NotBlank String refreshToken) {
    }

    @PostMapping("/auth/refresh")
    @Operation(summary = "Rotate refresh token and issue a new JWT access token (FR-1.2)")
    public IamService.AuthTokensResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return iamService.refresh(request.refreshToken());
    }

    @PostMapping("/auth/logout")
    @Operation(summary = "Revoke refresh token on logout (FR-1.2)")
    public Map<String, Object> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null) {
            iamService.logout(request.refreshToken());
        }
        return Map.of("status", "LOGGED_OUT");
    }

    @PostMapping("/auth/password-reset")
    @Operation(summary = "Reset password with complexity policy enforcement and account unlock (FR-1.4)")
    public Map<String, Object> resetPassword(@Valid @RequestBody IamService.PasswordResetRequest request) {
        return iamService.resetPassword(request);
    }

    @GetMapping("/users")
    @Operation(summary = "List users belonging to the active tenant")
    public List<AppUser> listUsers() {
        return iamService.listTenantUsers();
    }

    @GetMapping("/users/me")
    @Operation(summary = "Get active authenticated user and tenant context")
    public Map<String, Object> me() {
        return Map.of(
                "userId", String.valueOf(TenantContext.getUserId()),
                "tenantId", String.valueOf(TenantContext.getTenantId()),
                "email", String.valueOf(TenantContext.getUserEmail()),
                "roles", TenantContext.getRoles()
        );
    }
}
