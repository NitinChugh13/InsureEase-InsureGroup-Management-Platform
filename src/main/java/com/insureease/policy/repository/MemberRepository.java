package com.insureease.policy.repository;

import com.insureease.policy.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    List<Member> findByTenantIdAndPolicyIdOrderByEmployeeCodeAsc(UUID tenantId, UUID policyId);
    List<Member> findByTenantIdAndPolicyIdAndStatus(UUID tenantId, UUID policyId, String status);
    Optional<Member> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<Member> findByTenantIdAndPolicyIdAndEmployeeCode(UUID tenantId, UUID policyId, String employeeCode);
}
