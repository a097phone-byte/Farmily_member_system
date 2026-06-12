package com.farmily.user.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                // 設定 Session 的創建機制
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                )

//                .csrf(csrf -> csrf.disable())   // 關閉 csrf 保護，可以 call POST/PUT/DELETE API

                // 設定 CSRF 保護 (前端請求 POST/PUT/DELETE API 需帶上 X-XSRF-TOKEN )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(createCsrfHandler())
                )

                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())        // API call: Authorization Basic XXX
//                .formLogin(Customizer.withDefaults())      // 關掉預設 form 登入，自定義，避免背景監聽 login

                // url 請求設定
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/general/users/register",
                                        "/general/users/login",
                                        "/general/users/oauth/**").permitAll()      // 允許所有人請求
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/farmer/**").hasAnyRole("FARMER", "ADMIN")
                        .anyRequest().authenticated()                                 // 剩餘所有 url 需登入才能請求
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
