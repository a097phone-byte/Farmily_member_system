package com.farmily.user.dto;

import java.util.List;

// 修改其他管理員時前端可送的資料（欄位都可不填，有填才改）
public class AdminUpdateRequest {

    private String updateName;
    private String updateStatus;
    private List<String> updatePermissionCodes;


    public String getUpdateName() {
        return updateName;
    }

    public void setUpdateName(String updateName) {
        this.updateName = updateName;
    }

    public String getUpdateStatus() {
        return updateStatus;
    }

    public void setUpdateStatus(String updateStatus) {
        this.updateStatus = updateStatus;
    }

    public List<String> getUpdatePermissionCodes() {
        return updatePermissionCodes;
    }

    public void setUpdatePermissionCodes(List<String> updatePermissionCodes) {
        this.updatePermissionCodes = updatePermissionCodes;
    }
}