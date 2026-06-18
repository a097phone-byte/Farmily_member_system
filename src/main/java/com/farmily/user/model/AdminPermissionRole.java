package com.farmily.user.model;

import jakarta.persistence.*;

import java.io.Serializable;

// 管理員與權限中介表
@Entity
@Table(name = "admin_permission_role")
public class AdminPermissionRole implements Serializable {
    private static final long serialVersionUID = 1L;

    // 複合主鍵
    @EmbeddedId
    private AdminPermissionRoleId ids;

    @ManyToOne
    @MapsId("adminId")  // 對應 Java 屬性名稱
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @ManyToOne
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id")
    private AdminRole adminRole;

    public AdminPermissionRole() {
        super();
    }

    public AdminPermissionRole(Admin admin, AdminRole adminRole) {
        // 1. 初始化複合主鍵 (這一步很重要，否則 ID 會是 null)
        this.ids = new AdminPermissionRoleId(admin.getAdminId(), adminRole.getPermissionId());
        // 2. 設定關聯物件
        this.admin = admin;
        this.adminRole = adminRole;
    }


    public AdminPermissionRoleId getIds() {
        return ids;
    }

    public void setIds(AdminPermissionRoleId ids) {
        this.ids = ids;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public AdminRole getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(AdminRole adminRole) {
        this.adminRole = adminRole;
    }
}