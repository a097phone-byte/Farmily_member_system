package com.farmily.user.model;

// 存進 Redis 資料
public class AccountToken {

    // 這個 token 是給哪一種身分用的
    public enum AccountType {
        MEMBER, FARMER
    }

    // 這個 token 的用途
    public enum TokenType {
        EMAIL_VERIFY, PASSWORD_RESET
    }

    private String token;
    private String accountEmail;
    private AccountType accountType;    // 會員或小農
    private TokenType tokenType;        // Email 驗證或重設密碼



    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAccountEmail() {
        return accountEmail;
    }

    public void setAccountEmail(String accountEmail) {
        this.accountEmail = accountEmail;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }
}