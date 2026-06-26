package com.farmily.user.dto;

import com.farmily.user.model.AccountToken;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

// 重寄 Email 驗證信用
public class ResendVerificationRequest {

    @Email
    @NotNull
    private String email;

    // 要重寄給哪一種身分：MEMBER 或 FARMER
    @NotNull
    private AccountToken.AccountType accountType;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AccountToken.AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountToken.AccountType accountType) {
        this.accountType = accountType;
    }
}