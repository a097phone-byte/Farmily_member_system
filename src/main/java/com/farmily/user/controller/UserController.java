package com.farmily.user.controller;

import com.farmily.user.dto.*;
import com.farmily.user.security.GoogleTokenVerifier;
import com.farmily.user.security.MemberUserDetails;
import com.farmily.user.security.service.MemberUserDetailsService;
import com.farmily.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
public class UserController {

    private final UserService userService;
    private final MemberUserDetailsService memberUserDetailsService;
    private final GoogleTokenVerifier googleTokenVerifier;

    public UserController(UserService userService, MemberUserDetailsService memberUserDetailsService, GoogleTokenVerifier googleTokenVerifier) {
        this.userService = userService;
        this.memberUserDetailsService = memberUserDetailsService;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    // 一般會員註冊
    @PostMapping("/register")
    public ResponseEntity<UserProfileResponse> register(
            @RequestBody @Valid UserRegisterRequest reg){

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(reg));
    }

    // 一般會員登入
    @PostMapping("/login")
    public ResponseEntity<UserProfileResponse> login(
            @RequestBody @Valid LoginRequest  log,
            HttpServletRequest request) {

        // step1: 呼叫 Service，判斷帳號狀態、比對密碼，回傳 dto
        UserProfileResponse response = userService.login(log);

        // step2: 呼叫 MemberUserDetailsService 查出 + 驗證身分
        UserDetails userDetails = memberUserDetailsService.loadUserByUsername(log.getEmail());
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // step3: 把登入狀態存進 session，並回一個 session cookie 給前端
        HttpSession session = request.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        // step4: 依「記住我」設定 session 多久沒操作就過期 (單位：秒)
        if (log.isRememberMe()) {
            session.setMaxInactiveInterval(60 * 60 * 24 * 14);   // 勾記住我：14 天
        } else {
            session.setMaxInactiveInterval(60 * 30);             // 不勾：30 分鐘
        }

        return ResponseEntity.ok(response);
    }

    // 查自己資料
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMe(
            @AuthenticationPrincipal MemberUserDetails me){
        return ResponseEntity.ok(userService.getMyProfile(me.getUserId()));
    }

    // 修改自己資料
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMe(
            @AuthenticationPrincipal MemberUserDetails me,
            @RequestBody @Valid UserUpdateRequest update){
        UserProfileResponse response = userService.updateMyProfile(me.getUserId(), update);
        return ResponseEntity.ok(response);
    }

    // 修改自己密碼
    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal MemberUserDetails me,
            @RequestBody @Valid ChangePasswordRequest pw) {
        userService.changePassword(me.getUserId(), pw);
        return ResponseEntity.ok("密碼修改成功！請使用新密碼登入");
    }


    // 註銷自己帳號
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMe(
            @AuthenticationPrincipal MemberUserDetails me){
        userService.deleteUser(me.getUserId());
        return ResponseEntity.ok("註銷成功!");
    }

    // OAuth2.0 註冊登入 (前端拿到 id_token 後 POST 到這裡進行驗證 - 同比對本地密碼邏輯)
    @PostMapping("/oauth/google")
    public ResponseEntity<UserProfileResponse> googleLogin(
            @RequestBody @Valid GoogleLoginRequest req,
            HttpServletRequest request){

        // Google 驗證 token，拿使用者身分後清洗
        OAuthUserInfo info = googleTokenVerifier.verify(req.getIdToken());

        // 交由 loginOrRegisterOAuth 驗證登入或註冊
        UserProfileResponse response = userService.loginOrRegisterOAuth(info);

        // 建立登入狀態（改用 loadForOAuth）
        UserDetails userDetails = memberUserDetailsService.loadForOAuth(info.getEmail());
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 把登入狀態存進 session，並回一個 session cookie 給前端
        HttpSession session = request.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        return ResponseEntity.ok(response);
    }

}
