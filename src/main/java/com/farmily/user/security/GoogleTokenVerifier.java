package com.farmily.user.security;

import com.farmily.user.dto.GoogleTokenInfo;
import com.farmily.user.dto.OAuthUserInfo;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

// phase3. 拿前端給的 Google id_token，向 Google 發出驗證請求，並接住回傳資料
@Component
public class GoogleTokenVerifier {

    private final String GOOGLE_CLIENT_ID = "528090060943-mooi8topfu9g29umm7f67579vvsk553e.apps.googleusercontent.com";
    private final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    // 自訂驗證方法，由前端送來的 id_token，向 Google 驗證，並回傳使用者資訊
    public OAuthUserInfo verify(String idToken){

        // step1. 把 id_token 拼接在 Google 驗證 token 網址後面
        String tokenUrl = GOOGLE_TOKEN_URL + idToken;

        // step2. 用 RestTemplate 連到 Google，將回傳資料裝進 GoogleTokenInfo (Raw)
        RestTemplate restTemplate = new RestTemplate();

        // 向 Google 連線驗證，接住回傳的 JWT 格式資料
        GoogleTokenInfo info;
        try {
            info = restTemplate.getForObject(tokenUrl, GoogleTokenInfo.class);
        } catch (Exception e) {
            throw new BadCredentialsException("Google 登入失敗：token 無效或已過期");      // 錯誤或過期 id_token，Google 會回錯誤
        }

        // 沒拿到資料 = 登入失敗
        if(info == null){
            throw new BadCredentialsException("Google 登入失敗：拿不到資料");
        }

        // step4. 檢查回傳資料是我們的 aud (JWT) = google client id
        if( !GOOGLE_CLIENT_ID.equals(info.getAud()) ){
            throw new BadCredentialsException("Google 登入失敗：此 token 不是給本應用");
        }

        // step5. 驗證通過，把使用者資料整理成 OAuthUserInfo 交出去
        OAuthUserInfo result = new OAuthUserInfo();
        result.setEmail(info.getEmail());
        result.setName(info.getName());
        result.setProviderId(info.getSub());
        return result;
    }
}
