package com.farmily.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

//會員請求端 dto (/api/users)
public class UserLoginRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

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
}
