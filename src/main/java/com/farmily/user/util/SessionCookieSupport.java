package com.farmily.user.util;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

/**
 * 「記住我」用的 JSESSIONID cookie 工具。
 *
 * <p>本專案是 session 登入，瀏覽器只靠 JSESSIONID cookie 當鑰匙。Tomcat 預設發的是
 * session cookie（沒有 Max-Age，關掉瀏覽器就被刪）。勾了「記住我」時，這裡再補寫一個
 * 帶 Max-Age 的同名 cookie 去覆蓋它，讓它寫進硬碟、關瀏覽器後仍保留。</p>
 *
 * <p>覆蓋之所以成立：呼叫 {@code request.getSession(true)} 時 Tomcat 已把它那份
 * Set-Cookie 加進回應；之後我們再 addHeader 的這份會排在後面，瀏覽器對同名同 path 的
 * cookie 採用較後者，因此我們這份（帶 Max-Age）生效。</p>
 */
public final class SessionCookieSupport {

    /** persistent cookie 在瀏覽器端保留的天數，與「記住我」的 server session 壽命一致 */
    private static final long REMEMBER_ME_DAYS = 14;

    private SessionCookieSupport() {
    }

    /**
     * 寫一個帶 Max-Age 的 persistent JSESSIONID cookie（關瀏覽器後仍保留）。
     * cookie 屬性與 application.properties 的全域設定一致（HttpOnly / Secure / SameSite=Lax）。
     */
    public static void writeRememberMeCookie(HttpSession session, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("JSESSIONID", session.getId())
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .maxAge(Duration.ofDays(REMEMBER_ME_DAYS))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
