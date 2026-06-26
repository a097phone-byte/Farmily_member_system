package com.farmily.user.dto;

import jakarta.validation.constraints.Email;

// 會員、小農用
public class ForgotPasswordRequest {

    @Email
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
