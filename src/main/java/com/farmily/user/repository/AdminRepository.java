package com.farmily.user.repository;

import com.farmily.user.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Integer> {

    Optional<Admin> findByAdminEmail (String adminEmail);
    boolean existsByAdminEmail (String adminEmail);     // 檢查 email 全系統唯一

    // 從中介表 AdminPermissionRole 合併 AdminRole，取出 permissionCode
    @Query(value =
            "SELECT admin_role.permission_code " +
                    "FROM admin_permission_role " +
                    "JOIN admin_role ON admin_permission_role.permission_id = admin_role.permission_id " +
                    "WHERE admin_permission_role.admin_id = ?1",
            nativeQuery = true)
    List<String> findPermissionCodesByAdminId(Integer adminId);
}
