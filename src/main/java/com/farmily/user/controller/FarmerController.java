package com.farmily.user.controller;

import com.farmily.user.dto.*;
import com.farmily.user.security.FarmerUserDetails;
import com.farmily.user.security.service.FarmerUserDetailsService;
import com.farmily.user.service.FarmerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

    public FarmerController(FarmerService farmerService,
                            FarmerUserDetailsService farmerUserDetailsService) {
        this.farmerService = farmerService;
        this.farmerUserDetailsService = farmerUserDetailsService;
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
            HttpServletRequest request) {

        // step1: 呼叫 Service，判斷帳號狀態、比對密碼，回傳 dto
        FarmerProfileResponse response = farmerService.login(req);

        // step2: 通知 Spring Security 此人已通過驗證
        UserDetails userDetails = farmerUserDetailsService.loadUserByUsername(req.getEmail());
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // step3: 把 SecurityContext 存進 HttpSession，後續請求才能持續認得他
        HttpSession session = request.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());
        return ResponseEntity.ok(response);
    }

    // 查自己資料（含最新審核進度）；@AuthenticationPrincipal 取出登入者本人
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
    public ResponseEntity<FarmerProfileResponse> resubmit(
            @AuthenticationPrincipal FarmerUserDetails me,
            @RequestBody @Valid FarmerResubmitRequest req) {
        FarmerProfileResponse response = farmerService.resubmit(me.getFarmerId(), req);
        return ResponseEntity.ok(response);
    }

    // 修自己改密碼
    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal FarmerUserDetails me,
            @RequestBody @Valid ChangePasswordRequest pw) {
        farmerService.changePassword(me.getFarmerId(), pw);
        return ResponseEntity.ok("密碼修改成功！請使用新密碼登入");
    }

}
