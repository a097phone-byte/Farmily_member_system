package com.farmily.user.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

// 複合主鍵專用 class
@Embeddable
public class AdminPermissionRoleId implements Serializable {

    //固定版本號，不讓 Java 自動產生，避免因為 class 異動造成意外的版本不符而錯誤
    private static final long serialVersionUID = 1L;

    private Integer adminId;
    private Integer permissionId;

    // 必須覆寫 equals/hashCode
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AdminPermissionRoleId that = (AdminPermissionRoleId) o;
        return Objects.equals(adminId, that.adminId) && Objects.equals(permissionId, that.permissionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adminId, permissionId);
    }


    public AdminPermissionRoleId() {
        super();
    }

    public AdminPermissionRoleId(Integer adminId, Integer permissionId) {
        super();
        this.adminId = adminId;
        this.permissionId = permissionId;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public Integer getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Integer permissionId) {
        this.permissionId = permissionId;
    }
}