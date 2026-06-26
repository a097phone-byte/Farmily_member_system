package com.farmily.user.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class UserSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();     // Hash - Bcrypt 加密
    }

    // 三個身分共用的 session / csrf / cors / httpBasic 設定
    private HttpSecurity commonSetup(HttpSecurity http) throws Exception {
        return http
                // 設定 Session 的創建機制 (session + cookie)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS))

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


    // ===== 管理員 SecurityFilterChain：負責處理 /api/admin/** =====
    @Bean
    @Order(1)
    public SecurityFilterChain adminChain(HttpSecurity http) throws Exception {
        return commonSetup(http)
                .securityMatcher("/api/admin/**")
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/admin/login").permitAll()

                        // 細部權限管理 (ADMIN_ROLE) 注意有順序                        # 需補上其他組員的 API
                        .requestMatchers("/api/admin/admins/**").hasAuthority("PERM_ADMIN")

                        .requestMatchers("/api/admin/farmers/**", "/api/admin/reviews/**")
                        .hasAnyAuthority("PERM_ADMIN", "PERM_FARMER")

                        .requestMatchers("/api/admin/members/**")
                        .hasAnyAuthority("PERM_ADMIN", "PERM_MEMBER")


                        // 其餘 ("/api/admin/me")，登入的管理員都能用
                        .anyRequest().hasRole("ADMIN")
                )
                .build();
    }

    // ===== 小農 SecurityFilterChain：負責處理 /api/farmer/** =====
    @Bean
    @Order(2)
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
    @Order(3)
    public SecurityFilterChain memberChain(HttpSecurity http) throws Exception {
        return commonSetup(http)
                .securityMatcher("/api/member/**")
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/member/register",
                                "/api/member/login",
                                "/api/member/oauth/**").permitAll()
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
    @Order(4)
    public SecurityFilterChain defaultChain(HttpSecurity http) throws Exception {
        return commonSetup(http)
                .authorizeHttpRequests(request -> request
                        // 前端靜態檔
                        .requestMatchers("/", "/index.html", "/admin.html", "/css/**", "/js/**", "/vendors/**", "/favicon.ico").permitAll()
                        .requestMatchers("/oauth-test.html").permitAll()        // OAuth2.0 測試用

                        // 公開：註冊/申請表單的縣市與行政區下拉要用
                        .requestMatchers("/api/city-districts", "/api/city-districts/**").permitAll()

                        // 公開：Email 驗證 / 重寄驗證信 (未登入也要能用)
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
