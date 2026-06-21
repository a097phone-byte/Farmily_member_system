package com.farmily.user.dto;

import com.farmily.user.model.Admin;

import java.util.List;

// 將 Repository 回傳數據包裝成 dto
public class AdminProfileResponse {

    private Integer adminId;
    private String adminEmail;
    private String adminName;
    private String adminStatus;
    private List<String> permissionCodes;

    // getter
    public Integer getAdminId() {
        return adminId;
    }
    public String getAdminEmail() {
        return adminEmail;
    }
    public String getAdminName() {
        return adminName;
    }
    public String getAdminStatus() {
        return adminStatus;
    }
    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    // 自訂 from() 方法
    @SafeVarargs
    public static AdminProfileResponse from(Admin a, List<String>...permissionCodes){
        AdminProfileResponse dto = new AdminProfileResponse();
        dto.adminId = a.getAdminId();
        dto.adminEmail = a.getAdminEmail();
        dto.adminName = a.getAdminName();
        dto.adminStatus = a.getAdminStatus() != null ? a.getAdminStatus().name() : null;
        if (permissionCodes.length > 0) {
            dto.permissionCodes = permissionCodes[0];
        }
        return dto;
    }

}
