package com.farmily.user.controller;

import com.farmily.user.dto.*;
import com.farmily.user.security.AdminUserDetails;
import com.farmily.user.security.service.AdminUserDetailsService;
import com.farmily.user.service.AdminService;
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

import java.util.List;

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

    // 管理員修改自己的資料（只改名字；登入的管理員都能改自己）
    @PutMapping("/me")
    public ResponseEntity<AdminProfileResponse> updateMe(
            @AuthenticationPrincipal AdminUserDetails me,
            @RequestBody @Valid AdminSelfUpdateRequest req) {
        return ResponseEntity.ok(adminService.updateMyProfile(me.getAdminId(), req));
    }

    // 查自己資料
    @GetMapping("/me")
    public ResponseEntity<AdminProfileResponse> getMe(
            @AuthenticationPrincipal AdminUserDetails me) {
        return ResponseEntity.ok(adminService.getMyProfile(me.getAdminId()));
    }

    // ====================== 管理員對其他管理員 CRUD ======================
    // 新增管理員
    @PostMapping("/admins")
    public ResponseEntity<AdminProfileResponse> createAdmin(
            @RequestBody @Valid AdminCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createAdmin(req));
    }

    // 列出所有管理員
    @GetMapping("/admins")
    public ResponseEntity<List<AdminProfileResponse>> listAdmins() {
        return ResponseEntity.ok(adminService.listAll());
    }

    // 查單一管理員（含權限）
    @GetMapping("/admins/{adminId}")
    public ResponseEntity<AdminProfileResponse> getAdmin(@PathVariable Integer adminId) {
        return ResponseEntity.ok(adminService.getById(adminId));
    }

    // 列出系統所有可指派的權限 (前端動態產生勾選清單用)
    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionResponse>> listPermissions() {
        return ResponseEntity.ok(adminService.listPermissions());
    }

    // 修改管理員
    @PutMapping("/admins/{adminId}")
    public ResponseEntity<AdminProfileResponse> updateAdmin(
            @PathVariable Integer adminId,
            @RequestBody @Valid AdminUpdateRequest req) {
        return ResponseEntity.ok(adminService.updateAdmin(adminId, req));
    }

    // 刪除管理員（軟刪除）；me = 目前登入的管理員，用來擋「刪自己」
    @DeleteMapping("/admins/{adminId}")
    public ResponseEntity<String> deleteAdmin(
            @PathVariable Integer adminId,
            @AuthenticationPrincipal AdminUserDetails me) {
        adminService.deleteAdmin(adminId, me.getAdminId());
        return ResponseEntity.ok("已刪除管理員");
    }


}
