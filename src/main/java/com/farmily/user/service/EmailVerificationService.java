package com.farmily.user.service;

import com.farmily.user.model.AccountToken;
import com.farmily.user.model.Farmer;
import com.farmily.user.model.User;
import com.farmily.user.repository.FarmerRepository;
import com.farmily.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Email 驗證流程：寄送驗證信、點連結後完成驗證、重寄驗證信
@Service
@Transactional
public class EmailVerificationService {

    private final TokenService tokenService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;

    // 信件連結用的網址前綴
    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    // 驗證 token 的有效時間（分鐘）
    @Value("${app.token.email-verify-ttl-min}")
    private int verifyTtlMinutes;

    public EmailVerificationService(TokenService tokenService,
                                    EmailService emailService,
                                    UserRepository userRepository,
                                    FarmerRepository farmerRepository) {
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.farmerRepository = farmerRepository;
    }

    // 產生驗證 token 並寄出驗證信
    public void sendVerification(String email, AccountToken.AccountType accountType) {

        String token = tokenService.createToken(
                email, accountType, AccountToken.TokenType.EMAIL_VERIFY, verifyTtlMinutes);

        // 指向前端 SPA 的驗證頁（hash 路由），頁面再帶 token 呼叫後端驗證 API、顯示結果並導回首頁
        String verifyLink = frontendBaseUrl + "/#/verify-email?token=" + token;

        emailService.sendVerifyEmail(email, verifyLink);
    }

    // 小農審核通過後：產生驗證 token，並寄出「啟用 + Email 驗證」信。
    // 小農須點此連結完成驗證才能登入，未點連結不得自行登入。
    public void sendFarmerActivation(String email) {

        String token = tokenService.createToken(
                email, AccountToken.AccountType.FARMER, AccountToken.TokenType.EMAIL_VERIFY, verifyTtlMinutes);

        String verifyLink = frontendBaseUrl + "/#/verify-email?token=" + token;

        emailService.sendFarmerVerifyEmail(email, verifyLink);
    }

    // 使用者點連結後，驗證 token 並把帳號設為已驗證 setEmailVerified(true)
    public void verify(String token) {

        AccountToken accountToken =
                tokenService.validateAndConsume(token, AccountToken.TokenType.EMAIL_VERIFY);

        String email = accountToken.getAccountEmail();

        // 依身分別更新對應的表
        if (accountToken.getAccountType() == AccountToken.AccountType.MEMBER) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                throw new IllegalArgumentException("查無此帳號");
            }
            user.setEmailVerified(true);
            userRepository.save(user);
        } else {
            Farmer farmer = farmerRepository.findByEmail(email).orElse(null);
            if (farmer == null) {
                throw new IllegalArgumentException("查無此帳號");
            }
            farmer.setEmailVerified(true);
            farmerRepository.save(farmer);
        }
    }

    // 重寄驗證信：帳號存在且尚未驗證才寄，避免洩漏帳號是否存在
    public void resend(String email, AccountToken.AccountType accountType) {

        boolean exists = false;
        boolean alreadyVerified = false;

        if (accountType == AccountToken.AccountType.MEMBER) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                exists = true;
                // emailVerified 可能是 null，先判斷 null 再判斷 true
                if (user.getEmailVerified() != null && user.getEmailVerified()) {
                    alreadyVerified = true;
                }
            }
        } else {
            Farmer farmer = farmerRepository.findByEmail(email).orElse(null);
            if (farmer != null) {
                exists = true;
                if (farmer.getEmailVerified() != null && farmer.getEmailVerified()) {
                    alreadyVerified = true;
                }
            }
        }

        if (exists && !alreadyVerified) {
            // 小農用「啟用信」文案（與審核通過寄出的內容一致）；會員用一般驗證信
            if (accountType == AccountToken.AccountType.FARMER) {
                sendFarmerActivation(email);
            } else {
                sendVerification(email, accountType);
            }
        }
    }
}