package com.farmily.user.service;

import com.farmily.user.model.AccountToken;
import com.farmily.user.repository.AccountTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

// 負責產生與驗證 token（會員 / 小農共用）
@Service
@Transactional
public class TokenService {

    private final AccountTokenRepository accountTokenRepository;

    public TokenService(AccountTokenRepository accountTokenRepository) {
        this.accountTokenRepository = accountTokenRepository;
    }

    // 產生一個新的 token 存進 DB，並回傳 token 字串
    public String createToken(String email,
                              AccountToken.AccountType accountType,
                              AccountToken.TokenType tokenType,
                              int ttlMinutes) {

        // 用 UUID 當作隨機 token
        String tokenValue = UUID.randomUUID().toString();

        AccountToken token = new AccountToken();
        token.setToken(tokenValue);
        token.setAccountEmail(email);
        token.setAccountType(accountType);
        token.setTokenType(tokenType);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(ttlMinutes));
        token.setUsed(false);
        token.setCreatedAt(LocalDateTime.now());

        accountTokenRepository.save(token);
        return tokenValue;
    }

    // 驗證 token 是否有效，有效就標記為已使用並回傳該筆紀錄
    public AccountToken validateAndConsume(String tokenValue, AccountToken.TokenType tokenType) {

        // 找不到 token 會回傳 null
        AccountToken token = accountTokenRepository.findByToken(tokenValue).orElse(null);

        // 找不到 token
        if (token == null) {
            throw new IllegalArgumentException("驗證連結無效");
        }

        // 用途不符（例如拿驗證信的 token 來重設密碼）
        if (token.getTokenType() != tokenType) {
            throw new IllegalArgumentException("驗證連結無效");
        }

        // 已經用過
        if (token.getUsed() != null && token.getUsed()) {
            throw new IllegalStateException("此連結已使用過");
        }

        // 已過期
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("驗證連結已過期，請重新申請");
        }

        // 通過驗證，標記為已使用
        token.setUsed(true);
        accountTokenRepository.save(token);
        return token;
    }
}