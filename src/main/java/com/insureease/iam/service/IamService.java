package com.insureease.iam.service;

import com.insureease.audit.service.AuditService;
import com.insureease.common.exception.BusinessRuleException;
import com.insureease.common.exception.ResourceNotFoundException;
import com.insureease.common.tenant.TenantContext;
import com.insureease.iam.entity.AppUser;
import com.insureease.iam.entity.RefreshToken;
import com.insureease.iam.entity.Tenant;
import com.insureease.iam.repository.AppUserRepository;
import com.insureease.iam.repository.RefreshTokenRepository;
import com.insureease.iam.repository.TenantRepository;
import com.insureease.iam.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class IamService {

    private final TenantRepository tenantRepository;
    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final long refreshTokenDays;

    public IamService(TenantRepository tenantRepository,
                      AppUserRepository appUserRepository,
                      RefreshTokenRepository refreshTokenRepository,
                      PasswordEncoder passwordEncoder,
                      JwtService jwtService,
                      AuditService auditService,
                      @Value("${insureease.security.jwt.refresh-token-expiration-days:7}") long refreshTokenDays) {
        this.tenantRepository = tenantRepository;
        this.appUserRepository = appUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.refreshTokenDays = refreshTokenDays;
    }

    public record CreateTenantRequest(
            String code,
            String name,
            String tenantType,
            String subscriptionPlan,
            String adminEmail,
            String adminPassword,
            String adminName
    ) {
    }

    public record RegisterUserRequest(
            String tenantCode,
            String email,
            String password,
            String fullName,
            Set<String> roles,
            UUID policyholderId,
            UUID memberId
    ) {
    }

    public record LoginRequest(String tenantCode, String email, String password) {
    }

    public record PasswordResetRequest(String tenantCode, String email, String newPassword) {
    }

    public record AuthTokensResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresInSeconds,
            UUID userId,
            UUID tenantId,
            String tenantCode,
            String email,
            String fullName,
            Set<String> roles
    ) {
    }

    @Transactional
    public Tenant createTenant(CreateTenantRequest req) {
        tenantRepository.findByCodeIgnoreCase(req.code()).ifPresent(existing -> {
            throw new BusinessRuleException("FR-1.1", "Tenant code already exists: " + req.code());
        });

        Tenant tenant = new Tenant();
        tenant.setCode(req.code().toUpperCase());
        tenant.setName(req.name());
        tenant.setTenantType(req.tenantType() != null ? req.tenantType().toUpperCase() : "INSURER");
        tenant.setSubscriptionPlan(req.subscriptionPlan() != null ? req.subscriptionPlan().toUpperCase() : "ENTERPRISE");
        tenant.setStatus("ACTIVE");
        tenant = tenantRepository.save(tenant);

        if (req.adminEmail() != null && !req.adminEmail().isBlank()) {
            String rawPwd = req.adminPassword() != null ? req.adminPassword() : "Admin@123";
            validatePasswordComplexity(rawPwd);
            AppUser admin = new AppUser();
            admin.setTenantId(tenant.getId());
            admin.setEmail(req.adminEmail().toLowerCase());
            admin.setPasswordHash(passwordEncoder.encode(rawPwd));
            admin.setFullName(req.adminName() != null ? req.adminName() : "Tenant Administrator");
            admin.setStatus("ACTIVE");
            admin.setRoles(Set.of("ROLE_INSURER_ADMIN", "ROLE_UNDERWRITER", "ROLE_SENIOR_UNDERWRITER",
                    "ROLE_POLICY_ADMIN", "ROLE_CLAIMS_OFFICER", "ROLE_SENIOR_CLAIMS_OFFICER", "ROLE_FINANCE_OFFICER"));
            appUserRepository.save(admin);
        }

        auditService.logAction(tenant.getId(), "Tenant", tenant.getId(), "TENANT_CREATED", null,
                Map.of("code", tenant.getCode(), "name", tenant.getName(), "type", tenant.getTenantType()));
        return tenant;
    }

    @Transactional(readOnly = true)
    public List<Tenant> listTenants() {
        return tenantRepository.findAll();
    }

    @Transactional
    public AppUser registerUser(RegisterUserRequest req) {
        Tenant tenant = resolveTenant(req.tenantCode());
        validatePasswordComplexity(req.password());
        appUserRepository.findByTenantIdAndEmailIgnoreCase(tenant.getId(), req.email()).ifPresent(u -> {
            throw new BusinessRuleException("FR-1.2", "User email already exists in tenant: " + req.email());
        });

        AppUser user = new AppUser();
        user.setTenantId(tenant.getId());
        user.setEmail(req.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setFullName(req.fullName());
        user.setStatus("ACTIVE");
        user.setRoles(req.roles() != null && !req.roles().isEmpty() ? req.roles() : Set.of("ROLE_MEMBER"));
        user.setPolicyholderId(req.policyholderId());
        user.setMemberId(req.memberId());
        AppUser saved = appUserRepository.save(user);

        auditService.logAction(tenant.getId(), "AppUser", saved.getId(), "USER_REGISTERED", null,
                Map.of("email", saved.getEmail(), "roles", saved.getRoles()));
        return saved;
    }

    @Transactional(noRollbackFor = BadCredentialsException.class)
    public AuthTokensResponse login(LoginRequest req) {
        AppUser user;
        Tenant tenant;
        if (req.tenantCode() != null && !req.tenantCode().isBlank()) {
            tenant = resolveTenant(req.tenantCode());
            user = appUserRepository.findByTenantIdAndEmailIgnoreCase(tenant.getId(), req.email())
                    .orElseThrow(() -> new BadCredentialsException("Invalid tenant code, email, or password"));
        } else {
            user = appUserRepository.findFirstByEmailIgnoreCase(req.email())
                    .orElseThrow(() -> new BadCredentialsException("Invalid tenant code, email, or password"));
            tenant = tenantRepository.findById(user.getTenantId())
                    .orElseThrow(() -> new BadCredentialsException("Tenant not found"));
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new BadCredentialsException("Account is temporarily locked due to failed login attempts");
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= 5) {
                user.setLockedUntil(Instant.now().plus(15, ChronoUnit.MINUTES));
            }
            appUserRepository.save(user);
            auditService.logAction(tenant.getId(), "AppUser", user.getId(), "USER_LOGIN_FAILED", null,
                    Map.of("email", user.getEmail(), "failedAttempts", attempts));
            throw new BadCredentialsException("Invalid tenant code, email, or password");
        }

        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        appUserRepository.save(user);

        auditService.logAction(tenant.getId(), "AppUser", user.getId(), "USER_LOGIN_SUCCESS", null,
                Map.of("email", user.getEmail(), "roles", user.getRoles()));
        return issueTokens(tenant, user);
    }

    @Transactional
    public AuthTokensResponse refresh(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new BadCredentialsException("Invalid or revoked refresh token"));

        if (existing.getExpiresAt().isBefore(Instant.now())) {
            existing.setRevoked(true);
            refreshTokenRepository.save(existing);
            throw new BadCredentialsException("Refresh token has expired");
        }

        // Rotate refresh token
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        AppUser user = appUserRepository.findById(existing.getUserId())
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        Tenant tenant = tenantRepository.findById(existing.getTenantId())
                .orElseThrow(() -> new BadCredentialsException("Tenant not found"));

        return issueTokens(tenant, user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        String hash = sha256(rawRefreshToken);
        refreshTokenRepository.findByTokenHashAndRevokedFalse(hash).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            auditService.logAction(rt.getTenantId(), "AppUser", rt.getUserId(), "USER_LOGOUT", null,
                    Map.of("tokenRevoked", true));
        });
    }

    @Transactional
    public Map<String, Object> resetPassword(PasswordResetRequest req) {
        validatePasswordComplexity(req.newPassword());
        AppUser user;
        if (req.tenantCode() != null && !req.tenantCode().isBlank()) {
            Tenant tenant = resolveTenant(req.tenantCode());
            user = appUserRepository.findByTenantIdAndEmailIgnoreCase(tenant.getId(), req.email())
                    .orElseThrow(() -> new ResourceNotFoundException("AppUser", req.email()));
        } else {
            user = appUserRepository.findFirstByEmailIgnoreCase(req.email())
                    .orElseThrow(() -> new ResourceNotFoundException("AppUser", req.email()));
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        appUserRepository.save(user);
        auditService.logAction(user.getTenantId(), "AppUser", user.getId(), "PASSWORD_RESET", null,
                Map.of("email", user.getEmail()));
        return Map.of("email", user.getEmail(), "status", "PASSWORD_UPDATED");
    }

    private void validatePasswordComplexity(String password) {
        if (password == null || password.length() < 8
                || !password.matches(".*[A-Z].*")
                || !password.matches(".*[a-z].*")
                || !password.matches(".*[0-9].*")) {
            throw new BusinessRuleException("FR-1.4",
                    "Password must be at least 8 characters and include uppercase, lowercase, and numeric characters");
        }
    }

    @Transactional(readOnly = true)
    public List<AppUser> listTenantUsers() {
        return appUserRepository.findByTenantId(TenantContext.requireTenantId());
    }

    private AuthTokensResponse issueTokens(Tenant tenant, AppUser user) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefresh = UUID.randomUUID() + "-" + UUID.randomUUID();
        RefreshToken rt = new RefreshToken();
        rt.setTenantId(tenant.getId());
        rt.setUserId(user.getId());
        rt.setTokenHash(sha256(rawRefresh));
        rt.setExpiresAt(Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS));
        refreshTokenRepository.save(rt);

        return new AuthTokensResponse(
                accessToken,
                rawRefresh,
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds(),
                user.getId(),
                tenant.getId(),
                tenant.getCode(),
                user.getEmail(),
                user.getFullName(),
                user.getRoles()
        );
    }

    private Tenant resolveTenant(String tenantCode) {
        if (tenantCode != null && !tenantCode.isBlank()) {
            return tenantRepository.findByCodeIgnoreCase(tenantCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantCode));
        }
        UUID currentTid = TenantContext.getTenantId();
        if (currentTid != null) {
            return tenantRepository.findById(currentTid)
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant", currentTid));
        }
        throw new IllegalArgumentException("tenantCode is required");
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
