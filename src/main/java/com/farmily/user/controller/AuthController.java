package com.farmily.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

// 認證用 (Authenticator)
@RestController
public class AuthController {

    // 三身分共用登出
    @PostMapping("/api/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {

        // 有才拿 session
        HttpSession session = request.getSession(false);

        // 整個 session 作廢
        if (session != null)
            session.invalidate();

        // 清掉當前執行緒的登入資訊
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok("已登出");
    }

}
