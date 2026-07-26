package com.bloodbank.identity.domain.repository;

import com.bloodbank.identity.domain.entity.User;
import com.bloodbank.identity.domain.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUser(User user);
    List<UserRole> findByUserId(Long userId);
    void deleteByUserId(Long userId);
    boolean existsByRoleId(Long roleId);

    @Query("SELECT COUNT(DISTINCT ur.user.id) FROM UserRole ur JOIN RolePermission rp ON ur.role.id = rp.role.id WHERE rp.permission.code = :permissionCode AND ur.user.isDeleted = false AND ur.user.active = true")
    long countActiveUsersWithPermission(@Param("permissionCode") String permissionCode);
}
