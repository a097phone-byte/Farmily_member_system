package com.farmily.user.controller;

import com.farmily.user.dto.*;
import com.farmily.user.security.FarmerUserDetails;
import com.farmily.user.security.service.FarmerUserDetailsService;
import com.farmily.user.service.EmailService;
import com.farmily.user.service.FarmerService;
import com.farmily.user.service.SessionService;
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
@RequestMapping("/api/farmer")
public class FarmerController {

    private final FarmerService farmerService;
    private final FarmerUserDetailsService farmerUserDetailsService;
    private final SessionService sessionService;
    private final EmailService emailService;

    public FarmerController(FarmerService farmerService,
                            FarmerUserDetailsService farmerUserDetailsService,
                            SessionService sessionService,
                            EmailService emailService) {
        this.farmerService = farmerService;
        this.farmerUserDetailsService = farmerUserDetailsService;
        this.sessionService = sessionService;
        this.emailService = emailService;
    }

    // 小農註冊申請
    @PostMapping("/register")
    public ResponseEntity<FarmerProfileResponse> register(
            @RequestBody @Valid FarmerRegisterRequest reg) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(farmerService.register(reg));
    }

    // 小農登入
    @PostMapping("/login")
    public ResponseEntity<FarmerProfileResponse> login(
            @RequestBody @Valid LoginRequest req,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        // step1: 呼叫 Service，判斷帳號狀態、比對密碼，回傳 dto
        FarmerProfileResponse response = farmerService.login(req);

        // step2: 通知 Spring Security 此人已通過驗證
        UserDetails userDetails = farmerUserDetailsService.loadUserByUsername(req.getEmail());
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // step3: 把 SecurityContext 存進 HttpSession，後續請求才能持續認得他
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        // 把這個 session 登記進來（手動登入不會自動登記），
        // 之後改 / 重設密碼才找得到並讓它失效
        sessionService.registerSession(session.getId(), userDetails);

        // step4: 依「記住我」設定 session 多久沒操作就過期 (單位：秒)，
        //        並決定 JSESSIONID cookie 要不要寫進硬碟 (關瀏覽器後是否保留)
        if (req.isRememberMe()) {
            session.setMaxInactiveInterval(60 * 60 * 24 * 14);   // 勾記住我：server session 14 天
            // 在 getSession(true) 之後才寫，這個 Set-Cookie 會排在 Tomcat 預設那個之後，
            // 瀏覽器採用後者 → 變成帶 Max-Age 的 persistent cookie，關掉瀏覽器仍保留
            SessionCookieSupport.writeRememberMeCookie(session, httpResponse);
        } else {
            session.setMaxInactiveInterval(60 * 30);             // 不勾：30 分鐘，沿用預設 session cookie (關瀏覽器即刪)
        }
        return ResponseEntity.ok(response);
    }

    // 查自己資料 - @AuthenticationPrincipal 取出登入者本人
    @GetMapping("/me")
    public ResponseEntity<FarmerProfileResponse> getMe(
            @AuthenticationPrincipal FarmerUserDetails me) {
        return ResponseEntity.ok(farmerService.getMyProfile(me.getFarmerId()));
    }

    // 修改非審核欄位（電話、描述) - 立即生效
    @PutMapping("/me")
    public ResponseEntity<FarmerProfileResponse> updateContactInfo(
            @AuthenticationPrincipal FarmerUserDetails me,
            @RequestBody @Valid FarmerProfileUpdateRequest req) {
        FarmerProfileResponse response = farmerService.updateContactInfo(me.getFarmerId(), req);
        return ResponseEntity.ok(response);
    }

    // 修改審核相關欄位 - 重新送審
    @PutMapping("/me/application")
    public ResponseEntity<FarmerProfileResponse> updateReviewRequiredInfo(
            @AuthenticationPrincipal FarmerUserDetails me,
            @RequestBody @Valid FarmerResubmitRequest req) {
        FarmerProfileResponse response = farmerService.updateReviewRequiredInfo(me.getFarmerId(), req);
        return ResponseEntity.ok(response);
    }

    // 修自己改密碼
    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal FarmerUserDetails me,
            @RequestBody @Valid ChangePasswordRequest pw,
            HttpServletRequest request) {
        farmerService.changePassword(me.getFarmerId(), pw);

        // 改密碼成功後：踢掉「其他裝置」的 session（保留自己這台），再寄一封通知信
        HttpSession session = request.getSession(false);
        String currentSessionId = null;
        if (session != null) {
            currentSessionId = session.getId();
        }
        sessionService.expireSessions(me.getUsername(), currentSessionId);
        emailService.sendPasswordChangedNotice(me.getUsername());

        return ResponseEntity.ok("密碼修改成功！其他裝置已登出");
    }

}
