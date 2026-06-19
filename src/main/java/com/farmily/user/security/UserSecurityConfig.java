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

                .cors(Customizer.withDefaults())
//                .formLogin(Customizer.withDefaults())    // 關掉預設 form 登入，自定義，避免背景監聽 login
                .httpBasic(Customizer.withDefaults());     // API call: Authorization Basic XXX
    }


    // ===== 管理員 SecurityFilterChain：負責處理 /api/admin/** =====
    @Bean
    @Order(1)
    public SecurityFilterChain adminChain(HttpSecurity http) throws Exception {
        return commonSetup(http)
                .securityMatcher("/api/admin/**")
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/admin/login").permitAll()
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
                .build();
    }

    // ===== 其餘所有 url 需登入 =====
    @Bean
    @Order(4)
    public SecurityFilterChain defaultChain(HttpSecurity http) throws Exception {
        return commonSetup(http)
                .authorizeHttpRequests(request -> request
                        .anyRequest().authenticated()
                )
                .build();
    }

    // CSRF 保護
    private CsrfTokenRequestAttributeHandler createCsrfHandler(){
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null);

        return csrfHandler;
    }
}
