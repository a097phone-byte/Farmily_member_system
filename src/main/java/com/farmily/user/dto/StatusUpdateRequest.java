package com.farmily.user.dto;

import jakarta.validation.constraints.NotBlank;

// 管理原管理一般會員、小農狀態
public class StatusUpdateRequest {

    @NotBlank
    private String status;   // 例 "SUSPENDED"、"WARNED"、"ACTIVE"

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
