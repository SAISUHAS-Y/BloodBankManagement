package com.bloodbank.identity.domain.repository;

import com.bloodbank.identity.domain.entity.Role;
import com.bloodbank.identity.domain.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRole(Role role);
    List<RolePermission> findByRoleId(Long roleId);
    List<RolePermission> findByRoleIdIn(Collection<Long> roleIds);
    void deleteByRoleId(Long roleId);
}
