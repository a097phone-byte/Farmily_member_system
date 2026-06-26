package com.farmily.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 忘記密碼第二步：帶著信中的 token + 新密碼來重設
public class ResetPasswordRequest {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8)
    private String newPassword;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}