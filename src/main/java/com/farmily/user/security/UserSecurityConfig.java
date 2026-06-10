package com.farmily.user.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
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
}
