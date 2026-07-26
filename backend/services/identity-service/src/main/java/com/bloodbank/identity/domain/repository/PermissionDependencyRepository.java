package com.bloodbank.identity.domain.repository;

import com.bloodbank.identity.domain.entity.PermissionDependency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionDependencyRepository extends JpaRepository<PermissionDependency, Long> {

    List<PermissionDependency> findByPermissionId(Long permissionId);

    List<PermissionDependency> findByPermissionIdIn(List<Long> permissionIds);
}
