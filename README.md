# Farmily_member_system# Farmily 會員系統（Member System）

「你儂我農」農產平台的會員子系統，負責三種身分的帳號、認證與權限管理。

## 一、技術棧

- Java 17、Spring Boot 3.5
- Spring MVC（REST API）、Spring Data JPA、Hibernate
- Spring Security（Session 認證、BCrypt 密碼雜湊）
- MySQL 8
- Google OAuth 2.0（OpenID Connect，ID token 驗證）

## 二、系統架構

三種身分各自獨立的資料表、登入線與權限：

| 身分 | 資料表 | 路徑前綴 | 角色 |
|------|--------|----------|------|
| 一般會員 Member | user   | /api/member | ROLE_USER |
| 小農 Farmer     | farmer | /api/farmer | ROLE_FARMER |
| 管理員 Admin    | admin  | /api/admin  | ROLE_ADMIN（+ 細粒度 PERM_xxx）|

- Email 全系統唯一（跨 user / farmer / admin 三表）。
- 認證採 Session + Cookie（JSESSIONID）；登入時將 SecurityContext 寫入 Session。
- 密碼一律以 BCrypt 雜湊儲存。

## 三、環境需求與啟動

1. 安裝 JDK 17、MySQL 8。
2. 建立資料庫並匯入結構與種子資料：
    - 建立資料庫 `myproject`
    - 執行 `src/main/resources/create_table.txt`（含 DROP/CREATE 與種子資料）
3. 設定 `src/main/resources/application.properties` 的資料庫連線：

       spring.datasource.url=jdbc:mysql://localhost:3306/myproject
       spring.datasource.username=root
       spring.datasource.password=你的密碼

4. 設定 Google OAuth Client ID（見第六節）。
5. 啟動：

       ./mvnw spring-boot:run

   或用 IDE 執行 `UserApplication`。

## 四、API 一覽

### 一般會員 /api/member
| Method | Path | 說明 |
|---|---|---|
| POST | /register | 註冊 |
| POST | /login | 帳密登入 |
| POST | /oauth/google | Google 登入／自動註冊 |
| GET | /me | 查自己（含消費級距）|
| PUT | /me | 改自己資料 |
| PUT | /me/password | 改密碼 |
| DELETE | /me | 註銷帳號 |

### 小農 /api/farmer
| Method | Path | 說明 |
|---|---|---|
| POST | /register | 申請註冊（送審）|
| POST | /login | 登入（須通過審核）|
| GET | /me | 查自己 |
| PUT | /me | 改非審核欄位 |
| PUT | /me/application | 重新送審 |
| PUT | /me/password | 改密碼 |
| POST | /application/status | 免登入查審核狀態 |
| POST | /application/resubmit | 免登入重新送審 |

### 管理員 /api/admin
| Method | Path | 說明 | 需要權限 |
|---|---|---|---|
| POST | /login | 登入 | 公開 |
| GET | /me | 查自己（含權限）| 登入管理員 |
| PUT | /me | 改自己（僅名字）| 登入管理員 |
| POST | /admins | 新增管理員（含權限指派）| PERM_ADMIN |
| GET | /admins | 列出所有管理員 | PERM_ADMIN |
| GET | /admins/{id} | 查單一（含權限）| PERM_ADMIN |
| PUT | /admins/{id} | 修改（名字/狀態/權限）| PERM_ADMIN |
| DELETE | /admins/{id} | 刪除（軟刪除）| PERM_ADMIN |

### 管理員後台
| Method | Path | 說明 | 需要權限 |
|---|---|---|---|
| GET | /api/admin/members | 會員列表（含級距）| PERM_ADMIN 或 PERM_MEMBER |
| GET | /api/admin/members/{id} | 查會員 | PERM_ADMIN 或 PERM_MEMBER |
| PUT | /api/admin/members/{id}/status | 改會員狀態 | PERM_ADMIN 或 PERM_MEMBER |
| GET | /api/admin/farmers | 小農列表 | PERM_ADMIN 或 PERM_FARMER |
| GET | /api/admin/farmers/{id} | 查小農 | PERM_ADMIN 或 PERM_FARMER |
| PUT | /api/admin/farmers/{id}/suspend | 停權小農 | PERM_ADMIN 或 PERM_FARMER |
| PUT | /api/admin/farmers/{id}/reinstate | 復權小農 | PERM_ADMIN 或 PERM_FARMER |
| GET | /api/admin/reviews/pending | 待審清單 | PERM_ADMIN 或 PERM_FARMER |
| GET | /api/admin/reviews/farmer/{id} | 某小農審核紀錄 | PERM_ADMIN 或 PERM_FARMER |
| PUT | /api/admin/reviews/{id}/approve | 核准 | PERM_ADMIN 或 PERM_FARMER |
| PUT | /api/admin/reviews/{id}/reject | 退件 | PERM_ADMIN 或 PERM_FARMER |

### 共用
| Method | Path | 說明 |
|---|---|---|
| POST | /api/logout | 三身分共用登出 |

## 五、權限設計（管理員）

- 權限以 `admin_role.permission_code` 定義，登入時轉成 `PERM_xxx` authority。
- 後台各區由 SecurityConfig 以 URL 規則控管（見上表）。
- **同階互相保護**：擁有 `PERM_ADMIN` 的「超級管理員」彼此不能修改／刪除對方；且任何人不能刪除自己。

## 六、Google OAuth 登入

採「前端取得 ID token → 後端驗證」流程：

1. 前端用 Google 登入按鈕取得 `id_token`。
2. `POST /api/member/oauth/google`，body：`{ "idToken": "..." }`。
3. 後端向 Google `tokeninfo` 驗證，確認 `aud` 後取出 email/姓名，登入或自動註冊。

設定：於 `GoogleTokenVerifier` 與前端按鈕填入**同一組** Google Client ID（Google Cloud Console → OAuth 用戶端，已授權的 JavaScript 來源加 `http://localhost:8080`）。

可用 `src/main/resources/static/oauth-test.html` 進行測試。

## 七、消費級距（依每月消費自動分級）

| 級距 | 每月消費 |
|---|---|
| 一般會員 | 0 |
| 銅級會員 | 1 – 1,000 |
| 銀級會員 | 1,001 – 3,000 |
| 金級會員 | 3,001 以上 |

查會員資料時自動回傳對應級距名稱。

## 八、測試帳號

管理員（密碼皆為 `admin1234`）：

| 帳號 | 權限 | 狀態 |
|---|---|---|
| admin01@farm.com | 全部（含 ADMIN/MEMBER/FARMER）| ACTIVE |
| admin02@farm.com | NEWS, BLOG, MEMBER | ACTIVE |
| admin03@farm.com | MEMBER, FARMER, SHOP, GROUP_BUY | ACTIVE |
| admin04@farm.com | EVENT, MARKET_DATA | SUSPENDED（無法登入）|
| admin05@farm.com | ANALYTICS | DELETED（無法登入）|

會員／小農：請參考 `create_table.txt` 種子資料，或自行以 `/register` 建立。

## 九、認證機制

- Session 認證（`SessionCreationPolicy.ALWAYS`），登入後以 Cookie 維持。
- 錯誤碼：401（未認證/憑證錯誤）、403（權限不足）、404（查無資料）、400（欄位驗證）、405（方法不允許）、409（衝突，如 Email 重複）。

## 十、已知限制（後續可強化）

- CSRF 於開發階段關閉，正式環境建議啟用（CookieCsrf + X-XSRF-TOKEN）。