package com.farmily.user.controller;

import com.farmily.user.dto.AdminProfileResponse;
import com.farmily.user.dto.LoginRequest;
import com.farmily.user.security.AdminUserDetails;
import com.farmily.user.security.service.AdminUserDetailsService;
import com.farmily.user.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AdminUserDetailsService adminUserDetailsService;

    public AdminController(AdminService adminService, AdminUserDetailsService adminUserDetailsService) {
        this.adminService = adminService;
        this.adminUserDetailsService = adminUserDetailsService;
    }

    // 管理員登入
    @PostMapping("/login")
    public ResponseEntity<AdminProfileResponse> login(
            @RequestBody @Valid LoginRequest req,
            HttpServletRequest request){

        // step1: 呼叫 service 判斷帳號狀態
        AdminProfileResponse response = adminService.login(req);

        // step2: 通知 Spring Security 此人已通過驗證
        UserDetails userDetails = adminUserDetailsService.loadUserByUsername(req.getEmail());
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

    // 查自己資料
    @GetMapping("/me")
    public ResponseEntity<AdminProfileResponse> getMe(
            @AuthenticationPrincipal AdminUserDetails me) {
        return ResponseEntity.ok(adminService.getMyProfile(me.getAdminId()));
    }
}
