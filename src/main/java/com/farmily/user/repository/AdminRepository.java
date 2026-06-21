package com.farmily.user.repository;

import com.farmily.user.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Integer> {

    Optional<Admin> findByAdminEmail (String adminEmail);
    boolean existsByAdminEmail (String adminEmail);     // 檢查 email 全系統唯一

    // 從中介表 AdminPermissionRole 合併 AdminRole，查權限代碼 permissionCode
    @Query(value =
            "SELECT admin_role.permission_code " +
                    "FROM admin_permission_role " +
                    "JOIN admin_role ON admin_permission_role.permission_id = admin_role.permission_id " +
                    "WHERE admin_permission_role.admin_id = ?1",
            nativeQuery = true)
    List<String> findPermissionCodesByAdminId(Integer adminId);

    // 用權限代碼查出對應的 permission_id
    @Query(value = "SELECT permission_id FROM admin_role WHERE permission_code = ?1", nativeQuery = true)
    Integer findPermissionIdByCode(String permissionCode);

    // 指派一筆權限給某管理員
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO admin_permission_role (admin_id, permission_id) VALUES (?1, ?2)", nativeQuery = true)
    void addPermission(Integer adminId, Integer permissionId);

    // 清掉某管理員的所有權限（要重新指派前先清空）
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM admin_permission_role WHERE admin_id = ?1", nativeQuery = true)
    void deletePermissionsByAdminId(Integer adminId);
}
