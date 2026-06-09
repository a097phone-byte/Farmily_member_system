package com.farmily.user.dto;

public class OAuthUserInfo {

    private String email;
    private String name;
    private String providerId;

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
