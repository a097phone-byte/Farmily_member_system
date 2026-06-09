package com.farmily.user.dto;

import jakarta.validation.constraints.NotBlank;

public class ChangePasswordRequest {

    // OAuth 帳號沒有舊密碼,不加 @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
