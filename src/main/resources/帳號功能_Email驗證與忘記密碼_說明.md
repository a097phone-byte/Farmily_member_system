# 帳號功能說明：Email 驗證 + 忘記密碼

> 對象：會員（USER）、小農（FARMER）
> 兩個功能共用同一張 `ACCOUNT_TOKEN` 表，以及 `TokenService`、`EmailService`。
>
> - **階段一：Email 驗證** — 註冊後寄驗證信，點連結把 `email_verified` 設為 `true`。
> - **階段二：忘記密碼** — 輸入 email 寄重設信，點連結帶 token 設定新密碼。

---

## 一、執行前必做（兩件事）

### 1. 先在 MySQL 建立 `ACCOUNT_TOKEN` 表
因為 `application.properties` 設定 `spring.jpa.hibernate.ddl-auto=validate`，
程式啟動時只會「比對」資料表，不會自動建立。
**沒有先建表，啟動就會失敗。**

請執行 `create_table.txt`（重建整個 schema），或單獨執行裡面這段：

```sql
CREATE TABLE ACCOUNT_TOKEN (
    token_id      INT          AUTO_INCREMENT PRIMARY KEY,
    token         VARCHAR(255) NOT NULL UNIQUE,
    account_type  ENUM('MEMBER', 'FARMER') NOT NULL,
    account_email VARCHAR(255) NOT NULL,
    token_type    ENUM('EMAIL_VERIFY', 'PASSWORD_RESET') NOT NULL,
    expires_at    DATETIME     NOT NULL,
    used          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    DATETIME     NOT NULL
);
```

### 2. 填入 Gmail 寄信帳密
打開 `application.properties`，把下面兩行改成自己的：

```properties
spring.mail.username=你的帳號@gmail.com
spring.mail.password=你的應用程式密碼
```

⚠️ `password` 不是 Gmail 登入密碼，而是「應用程式密碼」：
Google 帳戶 → 開啟兩步驟驗證 → 產生 16 碼應用程式密碼，貼上即可。

---

## 二、API 一覽

| Method | 路徑 | 說明 |
|--------|------|------|
| GET | `/api/auth/verify-email?token=xxx` | 【階段一】點驗證信連結，完成 Email 驗證 |
| POST | `/api/auth/resend-verification` | 【階段一】重寄驗證信 |
| POST | `/api/auth/forgot-password` | 【階段二】寄出重設密碼信 |
| POST | `/api/auth/reset-password` | 【階段二】帶 token + 新密碼，完成重設 |

### request body 範例

`resend-verification`：
```json
{ "email": "test@example.com", "accountType": "MEMBER" }
```

`forgot-password`：
```json
{ "email": "test@example.com", "accountType": "FARMER" }
```

`reset-password`：
```json
{ "token": "信中連結帶的 token", "newPassword": "至少8碼新密碼" }
```

`accountType` 只能填 `MEMBER`（會員）或 `FARMER`（小農）。

---

## 三、測試流程

### 階段一：Email 驗證
1. 啟動專案：`mvnw.cmd spring-boot:run`
2. 用 Postman 呼叫會員或小農註冊 API。
3. 到信箱收信（標題「Farmily 帳號 Email 驗證」）。
4. 點信中連結，看到「Email 驗證成功！」。
5. 查 `USER` / `FARMER` 表，`email_verified` 應變成 `1`(true)。

### 階段二：忘記密碼
1. POST `/api/auth/forgot-password`，帶 email + accountType。
2. 到信箱收信（標題「Farmily 重設密碼」）。
3. 信中連結為 `.../reset-password?token=xxx`，**複製其中的 token**。
4. POST `/api/auth/reset-password`，帶 `token` + `newPassword`。
5. 看到「密碼重設成功！」後，用新密碼登入測試。

> 註：重設密碼信的連結是指向「前端頁面」，真正改密碼是前端頁面再 POST
> `/api/auth/reset-password`。目前沒有前端頁面，所以用 Postman 手動複製 token 測試。

---

## 四、程式架構（資料怎麼流動）

### 階段一：Email 驗證
```
註冊 (UserServiceImpl / FarmerServiceImpl)
        │  存好帳號後呼叫
        ▼
EmailVerificationService.sendVerification()
        │  ├─ TokenService.createToken()      → 產生 EMAIL_VERIFY token，存進 ACCOUNT_TOKEN
        │  └─ EmailService.sendVerifyEmail()  → @Async 另開執行緒寄信
        ▼
使用者點連結：GET /api/auth/verify-email?token=xxx (AuthController)
        ▼
EmailVerificationService.verify()
        │  ├─ TokenService.validateAndConsume()  → 檢查 token 並標記已使用
        │  └─ 把 USER / FARMER 的 email_verified 設為 true
```

### 階段二：忘記密碼
```
POST /api/auth/forgot-password (AuthController)
        ▼
PasswordResetService.sendResetLink()
        │  ├─ 帳號存在才繼續（不存在直接結束，不報錯）
        │  ├─ TokenService.createToken()            → 產生 PASSWORD_RESET token
        │  └─ EmailService.sendResetPasswordEmail() → @Async 寄信
        ▼
POST /api/auth/reset-password  (帶 token + newPassword)
        ▼
PasswordResetService.resetPassword()
        │  ├─ TokenService.validateAndConsume()  → 檢查 token 並標記已使用
        │  ├─ 把新密碼 hash 後存回 USER / FARMER（不需驗證舊密碼）
        │  └─ expireAllSessions()                → 把該帳號所有登入中的 session 設為過期
```

### 各檔案職責
| 檔案 | 做什麼 |
|------|--------|
| `model/AccountToken.java` | token 資料表的實體（會員＋小農共用） |
| `repository/AccountTokenRepository.java` | 用 token 字串查紀錄 |
| `service/TokenService.java` | 產生 token、驗證 token（過期 / 用過 / 用途不符） |
| `service/EmailService.java` | 真正用 JavaMailSender 寄信（驗證信 + 重設信） |
| `service/EmailVerificationService.java` | 【階段一】寄驗證信、驗證、重寄 |
| `service/PasswordResetService.java` | 【階段二】寄重設信、重設密碼 |
| `controller/AuthController.java` | 對外的四個端點 |

---

## 五、重要設計重點

1. **token 用 UUID 直接存（沒有加密）**
   新手好懂。日後若要更安全，可改成存 token 的雜湊值（SHA-256）。

2. **token 會過期、且只能用一次**
   - 驗證信有效時間：`app.token.email-verify-ttl-min`（預設 1440 分 = 24 小時）
   - 重設信有效時間：`app.token.password-reset-ttl-min`（預設 30 分鐘）
   驗證 / 重設成功後 `used` 設為 `true`，同一條連結不能再用。

3. **`token_type` 區分用途**
   驗證信是 `EMAIL_VERIFY`、重設信是 `PASSWORD_RESET`。
   拿錯類型的 token（例如拿驗證信 token 去重設密碼）會被擋下。

4. **寄信用 `@Async` 非同步**
   寄信比較慢，另開執行緒做，API 不會被卡住。
   （已在 `UserApplication` 加上 `@EnableAsync`）

5. **不洩漏帳號是否存在**
   `resend-verification` 與 `forgot-password` 不論帳號存不存在，都回相同訊息，
   避免被人拿來測試哪些信箱有註冊。

6. **重設密碼不需要舊密碼**
   因為使用者就是「忘記」了，改用「Email 收信」來證明本人，
   和登入後的「修改密碼」（要驗舊密碼）是不同流程。

7. **改 / 重設密碼後，會讓該帳號登入中的 session 失效**
   避免「密碼被盜改後，舊裝置仍保持登入」的風險。做法：
   - 登入時把 session 登記到 Spring 的 `SessionRegistry`
     （`UserController` / `FarmerController` 手動登入，需自己呼叫 `SessionService.registerSession`）。
   - `UserSecurityConfig` 用 `maximumSessions(-1)` 掛上 `ConcurrentSessionFilter`，
     並註冊 `SessionRegistry` 與 `HttpSessionEventPublisher` 兩個 bean。
   - 統一由 `SessionService.expireSessions(email, keepSessionId)` 處理；被標記的 session
     下次請求就會被擋下、需重新登入。兩個情境差別在「要不要留自己這台」：
     - **忘記密碼**（未登入）：`PasswordResetService` 傳 `keepSessionId = null` → 全部踢掉。
     - **修改密碼**（已登入，需驗舊密碼）：controller 傳「當前 session id」→ 只踢其他裝置，
       保留操作者自己，並另外寄一封「密碼已變更」純通知信（`EmailService.sendPasswordChangedNotice`）。

8. **目前尚未強制「沒驗證就不能登入」**
   為了不卡住現有測試資料，先不擋（之後會綁定業務流程時再驗）。
   若要啟用，在 `UserServiceImpl.login()` / `FarmerServiceImpl.login()`
   加一段檢查 `email_verified` 即可。

---

## 六、相關設定（application.properties）

```properties
# Gmail SMTP
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=你的帳號@gmail.com
spring.mail.password=你的應用程式密碼
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# 驗證 / 重設連結網址前綴
app.frontend-base-url=http://localhost:8080
# 驗證 token 有效時間（分鐘）
app.token.email-verify-ttl-min=1440
# 重設密碼 token 有效時間（分鐘）
app.token.password-reset-ttl-min=30
```
