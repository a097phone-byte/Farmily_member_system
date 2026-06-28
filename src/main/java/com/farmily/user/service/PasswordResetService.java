package com.farmily.user.service;

import com.farmily.user.model.AccountToken;
import com.farmily.user.model.Farmer;
import com.farmily.user.model.User;
import com.farmily.user.repository.FarmerRepository;
import com.farmily.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 忘記密碼流程：寄重設信、用 token 重設新密碼
@Service
@Transactional
public class PasswordResetService {

    private final TokenService tokenService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    // 信件連結用的網址前綴
    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    // 重設密碼 token 的有效時間（分鐘）
    @Value("${app.token.password-reset-ttl-min}")
    private int resetTtlMinutes;

    public PasswordResetService(TokenService tokenService,
                                EmailService emailService,
                                UserRepository userRepository,
                                FarmerRepository farmerRepository,
                                PasswordEncoder passwordEncoder,
                                SessionService sessionService) {
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.farmerRepository = farmerRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
    }

    // step1. 使用者輸入 email，帳號存在才寄重設信
    // 不論帳號是否存在都不報錯，避免洩漏哪些信箱有註冊
    public void sendResetLink(String email, AccountToken.AccountType accountType) {

        boolean exists = false;

        if (accountType == AccountToken.AccountType.MEMBER) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                exists = true;
            }
        } else {
            Farmer farmer = farmerRepository.findByEmail(email).orElse(null);
            if (farmer != null) {
                exists = true;
            }
        }

        // 帳號不存在就直接結束，什麼都不做
        if (!exists) {
            return;
        }

        // 產生重設密碼 token，並寄出重設信
        String token = tokenService.createToken(
                email, accountType, AccountToken.TokenType.PASSWORD_RESET, resetTtlMinutes);

        // 指向前端 SPA 的重設密碼頁面（hash 路由），頁面再帶 token 與新密碼 POST 回後端
        String resetLink = frontendBaseUrl + "/#/reset-password?token=" + token;
        emailService.sendResetPasswordEmail(email, resetLink);
    }

    // step2. 帶 token + 新密碼來重設
    public void resetPassword(String token, String newPassword) {

        // 驗證 token（過期 / 用過 / 用途不符丟例外）
        AccountToken accountToken =
                tokenService.validateAndConsume(token, AccountToken.TokenType.PASSWORD_RESET);

        String email = accountToken.getAccountEmail();

        // 把新密碼 hash 後存回對應的帳號(忘記密碼不需要驗證舊密碼)
        if (accountToken.getAccountType() == AccountToken.AccountType.MEMBER) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                throw new IllegalArgumentException("查無此帳號");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } else {
            Farmer farmer = farmerRepository.findByEmail(email).orElse(null);
            if (farmer == null) {
                throw new IllegalArgumentException("查無此帳號");
            }
            farmer.setPassword(passwordEncoder.encode(newPassword));
            farmerRepository.save(farmer);
        }

        // 密碼已改，把這個帳號目前所有登入中的 session 設為過期 (null = 全部都踢)
        sessionService.expireSessions(email, null);
    }
}