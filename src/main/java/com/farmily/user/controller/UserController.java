package com.farmily.user.controller;

import com.farmily.user.dto.*;
import com.farmily.user.security.GoogleTokenVerifier;
import com.farmily.user.security.MemberUserDetails;
import com.farmily.user.security.service.MemberUserDetailsService;
import com.farmily.user.service.EmailService;
import com.farmily.user.service.SessionService;
import com.farmily.user.service.UserService;
import com.farmily.user.util.SessionCookieSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final SessionService sessionService;
    private final EmailService emailService;

    public UserController(UserService userService, MemberUserDetailsService memberUserDetailsService, GoogleTokenVerifier googleTokenVerifier, SessionService sessionService, EmailService emailService) {
        this.userService = userService;
        this.memberUserDetailsService = memberUserDetailsService;
        this.googleTokenVerifier = googleTokenVerifier;
        this.sessionService = sessionService;
        this.emailService = emailService;
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
            HttpServletRequest request,
            HttpServletResponse httpResponse) {

        // step1: 呼叫 Service，判斷帳號狀態、比對密碼，回傳 dto
        UserProfileResponse response = userService.login(log);

        // step2: 呼叫 MemberUserDetailsService 查出 + 驗證身分
        UserDetails userDetails = memberUserDetailsService.loadUserByUsername(log.getEmail());
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // step3: 把登入狀態存進 session，並回一個 session cookie 給前端
        HttpSession session = request.getSession(true);         // getSession(true) 若已有 Session 就回傳，沒有就建立一個新的
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        // 把這個 session 登記進來（手動登入不會自動登記），之後改 / 重設密碼才找得到並讓它失效
        sessionService.registerSession(session.getId(), userDetails);

        // step4: 依「記住我」設定 session 多久沒操作就過期 (單位：秒)，
        //        並決定 JSESSIONID cookie 要不要寫進硬碟 (關瀏覽器後是否保留)
        if (log.isRememberMe()) {
            session.setMaxInactiveInterval(60 * 60 * 24 * 14);   // 勾記住我：server session 14 天
            // 在 getSession(true) 之後才寫，這個 Set-Cookie 會排在 Tomcat 預設那個之後，
            // 瀏覽器採用後者，變成帶 Max-Age 的 persistent cookie，關掉瀏覽器仍保留
            SessionCookieSupport.writeRememberMeCookie(session, httpResponse);
        } else {
            session.setMaxInactiveInterval(60 * 30);             // 不勾：30 分鐘，沿用預設 session cookie (關瀏覽器即刪)
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
            @RequestBody @Valid ChangePasswordRequest pw,
            HttpServletRequest request) {
        userService.changePassword(me.getUserId(), pw);

        // 改密碼成功後：踢掉「其他裝置」的 session（保留自己這台），再寄一封通知信
        HttpSession session = request.getSession(false);
        String currentSessionId = null;
        if (session != null) {
            currentSessionId = session.getId();
        }
        sessionService.expireSessions(me.getUsername(), currentSessionId);
        emailService.sendPasswordChangedNotice(me.getUsername());

        return ResponseEntity.ok("密碼修改成功！");
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

        // 同樣登記進來
        sessionService.registerSession(session.getId(), userDetails);

        return ResponseEntity.ok(response);
    }

}
