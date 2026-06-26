package com.farmily.user.controller;

import com.farmily.user.dto.ForgotPasswordRequest;
import com.farmily.user.dto.ResendVerificationRequest;
import com.farmily.user.dto.ResetPasswordRequest;
import com.farmily.user.service.EmailVerificationService;
import com.farmily.user.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 認證用 (Authenticator)
@RestController
public class AuthController {

    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    public AuthController(EmailVerificationService emailVerificationService,
                          PasswordResetService passwordResetService) {
        this.emailVerificationService = emailVerificationService;
        this.passwordResetService = passwordResetService;
    }

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

    // 點驗證信連結後，完成 Email 驗證
    @GetMapping("/api/auth/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        emailVerificationService.verify(token);
        return ResponseEntity.ok("Email 驗證成功！");
    }

    // 重寄驗證信（不論帳號是否存在都回相同訊息，避免洩漏帳號）
    @PostMapping("/api/auth/resend-verification")
    public ResponseEntity<String> resendVerification(
            @RequestBody @Valid ResendVerificationRequest req) {
        emailVerificationService.resend(req.getEmail(), req.getAccountType());
        return ResponseEntity.ok("系統已重新寄出驗證信，請至信箱確認");
    }

    // 忘記密碼第一步：寄重設密碼信（不論帳號是否存在都回相同訊息）
    @PostMapping("/api/auth/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest req) {
        passwordResetService.sendResetLink(req.getEmail(), req.getAccountType());
        return ResponseEntity.ok("系統已寄出重設密碼信，請至信箱確認");
    }

    // 忘記密碼第二步：帶 token + 新密碼來重設
    @PostMapping("/api/auth/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestBody @Valid ResetPasswordRequest req) {
        passwordResetService.resetPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.ok("密碼重設成功！請使用新密碼登入");
    }

}
