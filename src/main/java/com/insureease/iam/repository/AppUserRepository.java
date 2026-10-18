package com.insureease.iam.repository;

import com.insureease.iam.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);
    Optional<AppUser> findFirstByEmailIgnoreCase(String email);
    List<AppUser> findByTenantId(UUID tenantId);
}
