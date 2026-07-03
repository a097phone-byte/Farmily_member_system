package com.farmily.user.security;

import com.farmily.user.security.service.AdminUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class UserSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();     // Hash - Bcrypt 加密
    }

    // 記錄「哪個帳號目前有哪些 session」，重設密碼時可據此把該帳號的所有 session 設為過期
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    // session 被銷毀（登出 / 逾時）時發事件，讓 SessionRegistry 自動移除紀錄，避免一直累積
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    // 三個身分共用的 session / csrf / cors / httpBasic 設定
    private HttpSecurity commonSetup(HttpSecurity http) throws Exception {
        return http
                // 設定 Session 的創建機制 (session + cookie)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)

                        // maximumSessions(-1) = 不限制同時登入數，但會掛上 ConcurrentSessionFilter，
                        // 被 expireNow() 標記過期的 session 下次請求就會被擋下 - 重設密碼後踢掉
                        .maximumSessions(-1)
                        .sessionRegistry(sessionRegistry()))

                // 關閉 csrf 保護，可以 call POST/PUT/DELETE API
                .csrf(csrf -> csrf.disable())

                // 設定 CSRF 保護 (前端請求 POST/PUT/DELETE API 需帶上 X-XSRF-TOKEN )
//                .csrf(csrf -> csrf
//                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                        .csrfTokenRequestHandler(createCsrfHandler()))

                // 跨域同源 (需處理前端 Vue.js 和後端不同源情況)
                .cors(Customizer.withDefaults())
//                .formLogin(Customizer.withDefaults())    // 關掉預設 form 登入，改自定義登入，避免背景監聽 login
//                .httpBasic(Customizer.withDefaults());     // API call: Authorization Basic XXX

                .httpBasic(basic -> basic.disable())
                // 未登入時只回 401 狀態碼（不跳原生視窗），讓前端自己導去我們自訂的登入頁
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (request, response, authEx) -> response.sendError(401)));
    }

    // 只認「管理員帳號」的驗證器：用 AdminUserDetailsService 查帳號、用 BCrypt 比對密碼
    // 密碼比對、帳號是否啟用(isEnabled) 都由 Spring Security 自動處理
    @Bean
    public AuthenticationProvider adminAuthenticationProvider(AdminUserDetailsService adminUserDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();       // 「查帳號 + 比對密碼」的標準元件 DaoAuthenticationProvider
        provider.setUserDetailsService(adminUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ===== 管理員「頁面」過濾鏈：負責 /admin/**（Thymeleaf 後台）=====
    // 放在最前面 (Order 0)，且不走 commonSetup（因為它會關 CSRF、未登入回 401，這些是給 Vue 用）
    @Bean
    @Order(0)
    public SecurityFilterChain adminPageChain(HttpSecurity http, AuthenticationProvider adminAuthenticationProvider) throws Exception {
        return http
                .securityMatcher("/admin/**")
                .authenticationProvider(adminAuthenticationProvider)
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/admin/login").permitAll()
                        .requestMatchers("/admin/admins/**").hasAuthority("PERM_ADMIN")
                        .requestMatchers("/admin/farmers/**", "/admin/reviews/**").hasAnyAuthority("PERM_ADMIN", "PERM_FARMER")
                        .requestMatchers("/admin/members/**").hasAnyAuthority("PERM_ADMIN", "PERM_MEMBER")

                        .anyRequest().hasRole("ADMIN"))

                // formLogin：未登入自動導到 /admin/login；POST /admin/login 由 Spring 幫我們處理
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .usernameParameter("email")      // 表單欄位 name="email"
                        .passwordParameter("password")   // 表單欄位 name="password"
                        .defaultSuccessUrl("/admin/dashboard", true)
                        .failureUrl("/admin/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout"))
                .sessionManagement(session -> session
                        .maximumSessions(-1)
                        .sessionRegistry(sessionRegistry()))

                // 注意沒有關閉 CSRF，後台維持開啟 (Thymeleaf 表單會自動帶 token)
                .build();
    }


    // ===== 小農 SecurityFilterChain：負責處理 /api/farmer/** =====
    @Bean
    @Order(1)
    public SecurityFilterChain farmerChain(HttpSecurity http) throws Exception {
        return commonSetup(http)
                .securityMatcher("/api/farmer/**")
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/farmer/register",
                                "/api/farmer/login",
                                "/api/farmer/application/**").permitAll()
                        .anyRequest().hasRole("FARMER")
                )
                .build();
    }

    // ===== 會員 SecurityFilterChain：負責處理 /api/member/** =====
    @Bean
    @Order(2)
    public SecurityFilterChain memberChain(HttpSecurity http) throws Exception {
        return commonSetup(http)
                .securityMatcher("/api/member/**")
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/member/register", "/api/member/login", "/api/member/oauth/**").permitAll()





                        .anyRequest().hasRole("USER")
                )
                // OAuth 2.0 社交登入 - 交給 Spring Security 處理
//                .oauth2Login(oauth2 -> oauth2
//                        .userInfoEndpoint(infoEndpoint -> infoEndpoint
//                                .oidcUserService(myOidcUserService)
//                        )
//                )
                .build();
    }

    // ===== 其餘所有 url 需登入 =====
    @Bean
    @Order(3)
    public SecurityFilterChain defaultChain(HttpSecurity http) throws Exception {
        return commonSetup(http)
                .authorizeHttpRequests(request -> request

                        // 前端靜態檔（Vue 打包輸出在 /farmily-web/ 子路徑）
                        .requestMatchers("/farmily-web/**").permitAll()


                        // 前端靜態檔
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/vendors/**",  "/webjars/**","/favicon.ico").permitAll()
                        .requestMatchers("/oauth-test.html").permitAll()        // OAuth2.0 測試用

                        // 公開：註冊/申請表單的縣市與行政區下拉要用
                        .requestMatchers("/api/city-districts", "/api/city-districts/**").permitAll()

                        // 公開：Email 驗證 / 忘記密碼 (未登入也要能用)
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }

    // CSRF 保護
    private CsrfTokenRequestAttributeHandler createCsrfHandler() {
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null);

        return csrfHandler;
    }
}
