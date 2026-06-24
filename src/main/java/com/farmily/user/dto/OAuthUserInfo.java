package com.farmily.user.dto;

// phase4. 接住由 GoogleTokenVerifier 回傳 Google 已驗證後的資料 (處理過)，轉交給 Service 處理
public class OAuthUserInfo {

    private String email;
    private String name;
    private String providerId;              // Google 的使用者唯一編號（JWT 的 "sub"）

    public OAuthUserInfo(){}

    public OAuthUserInfo(String email, String name, String providerId) {
        this.email = email;
        this.name = name;
        this.providerId = providerId;
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

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

}
