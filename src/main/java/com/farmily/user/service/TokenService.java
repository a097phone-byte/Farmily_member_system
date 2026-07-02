package com.farmily.user.service;

import com.farmily.user.model.AccountToken;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.UUID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

// 負責產生與驗證 token（會員/小農共用），過期時間(TTL)自動清除，取出後即刪除
@Service
public class TokenService {

    // key 前綴，避免和其他資料衝突
    private static final String KEY_PREFIX = "farmily:token:";

    private final JedisPool jedisPool;

    public TokenService(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    // 組出 key，例如：farmily:token:PASSWORD_RESET:xxxx-uuid
    // 把 tokenType 設為 key：拿驗證信 token 去重設密碼會找不到
    private String buildKey(AccountToken.TokenType tokenType, String tokenValue) {
        return KEY_PREFIX + tokenType.name() + ":" + tokenValue;
    }

    // 產生一個新的 token 存進 Redis，並回傳 token 字串
    public String createToken(String email,
                              AccountToken.AccountType accountType,
                              AccountToken.TokenType tokenType,
                              int ttlMinutes) {

        String tokenValue = UUID.randomUUID().toString();

        // 組成 JSON 字串
        JSONObject json = new JSONObject();
        json.put("accountEmail", email);
        json.put("accountType", accountType.name());
        json.put("tokenType", tokenType.name());
        String jsonStr = json.toString();

        String key = buildKey(tokenType, tokenValue);
        int ttlSeconds = ttlMinutes * 60;   // Redis 過期單位是秒

        Jedis jedis = jedisPool.getResource();
        try {
            // 先 set 存值，再 expire 設定過期秒數
            jedis.set(key, jsonStr);
            jedis.expire(key, ttlSeconds);
        } finally {
            jedis.close();
        }

        return tokenValue;
    }

    // 驗證 token；有效就「取出並刪除」（一次性），並回傳整理好的 AccountToken
    public AccountToken validateAndConsume(String tokenValue, AccountToken.TokenType tokenType) {

        String key = buildKey(tokenType, tokenValue);

        String jsonStr;
        Jedis jedis = jedisPool.getResource();
        try {
            // 先把資料讀出來
            jsonStr = jedis.get(key);

            // 有讀到才刪掉，用過即失效
            if (jsonStr != null) {
                jedis.del(key);
            }
        } finally {
            jedis.close();
        }

        if (jsonStr == null) {
            throw new IllegalArgumentException("連結無效或已過期，請重新申請");
        }

        // JSON to Object: 把 JSON 字串還原回 AccountToken 物件
        org.json.JSONObject json = new org.json.JSONObject(jsonStr);
        String email = json.getString("accountEmail");
        String accountTypeStr = json.getString("accountType");
        String tokenTypeStr = json.getString("tokenType");

        AccountToken accountToken = new AccountToken();
        accountToken.setToken(tokenValue);
        accountToken.setAccountEmail(email);
        accountToken.setAccountType(AccountToken.AccountType.valueOf(accountTypeStr));
        accountToken.setTokenType(AccountToken.TokenType.valueOf(tokenTypeStr));
        return accountToken;
    }
}