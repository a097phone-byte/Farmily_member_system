package com.farmily.user.dto;

import com.farmily.user.model.Admin;

// 將 Repository 回傳數據包裝成 dto
public class AdminProfileResponse {

    private Integer adminId;
    private String adminEmail;
    private String adminName;
    private String adminStatus;

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

    // 自訂 from() 方法
    public static AdminProfileResponse from(Admin a){
        AdminProfileResponse dto = new AdminProfileResponse();
        dto.adminId = a.getAdminId();
        dto.adminEmail = a.getAdminEmail();
        dto.adminName = a.getAdminName();
        dto.adminStatus = a.getAdminStatus() != null ? a.getAdminStatus().name() : null;
        return dto;
    }
}
