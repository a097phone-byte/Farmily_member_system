package com.farmily.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 共用的本地登入請求 (email + password)
public class LoginRequest  {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    // 記住我：true = 較長時間保持登入；false = 較短時間，沒操作就登出
    private boolean rememberMe;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
