package com.farmily.user.dto;

// phase2. 接住 Google 回傳驗證 JWT 的身份資料 (Raw)
public class GoogleTokenInfo {

    // JWT
    private String aud;         // 我們的 client id
    private String sub;         // Google 的使用者固定編號 = providerId
    private String email;
    private String name;


    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}