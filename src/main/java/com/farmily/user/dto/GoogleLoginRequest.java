package com.farmily.user.dto;

import jakarta.validation.constraints.NotBlank;

// phase1. 用來接前端送來的 id_token，POST 給後端驗證
public class GoogleLoginRequest {

    // id_token 由 Google 發送給前端
    @NotBlank
    private String idToken;


    public String getIdToken() {
        return idToken;
    }
    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}