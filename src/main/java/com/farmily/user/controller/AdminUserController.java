package com.farmily.user.controller;

import com.farmily.user.dto.StatusUpdateRequest;
import com.farmily.user.dto.UserProfileResponse;
import com.farmily.user.service.AdminMemberService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/members")
public class AdminUserController {

    private final AdminMemberService adminMemberService;
    public AdminUserController(AdminMemberService adminMemberService) {
        this.adminMemberService = adminMemberService;
    }

    // 複合查詢會員（可依消費級距、會員狀態複選篩選；不帶參數 = 全部）
    @GetMapping
    public ResponseEntity<List<UserProfileResponse>> list(
            @RequestParam(required = false) List<String> tierName,
            @RequestParam(required = false) List<String> status) {
        return ResponseEntity.ok(adminMemberService.list(tierName, status));
    }

    // 查單一會員
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getOne(@PathVariable Integer userId) {
        return ResponseEntity.ok(adminMemberService.getById(userId));
    }

    // 改狀態 body:{"status":"SUSPENDED"}
    @PutMapping("/{userId}/status")
    public ResponseEntity<UserProfileResponse> updateStatus(
            @PathVariable Integer userId,
            @RequestBody @Valid StatusUpdateRequest req) {
        return ResponseEntity.ok(adminMemberService.updateStatus(userId, req.getStatus()));
    }

}
