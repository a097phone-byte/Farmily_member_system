package com.farmily.user.controller;

import com.farmily.user.dto.FarmerReviewResponse;
import com.farmily.user.dto.ReviewRejectRequest;
import com.farmily.user.security.AdminUserDetails;
import com.farmily.user.service.AdminReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    public AdminReviewController(AdminReviewService adminReviewService) {
        this.adminReviewService = adminReviewService;
    }

    // 查所有 PENDING 案件清單
    @GetMapping("/pending")
    public ResponseEntity<List<FarmerReviewResponse>> listPending() {
        return ResponseEntity.ok(adminReviewService.listPending());
    }

    // 查所有 Reviewing 案件清單
    @GetMapping("/reviewing")
    public ResponseEntity<List<FarmerReviewResponse>> listReviewing(){
        return ResponseEntity.ok(adminReviewService.listReviewing());
    }

    // 查某小農所有審核紀錄
    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<FarmerReviewResponse>> listByFarmer(
            @PathVariable Integer farmerId) {
        return ResponseEntity.ok(adminReviewService.listByFarmer(farmerId));
    }

    // 修改審核: 審核中
    @PutMapping("/{reviewId}/start")
    public ResponseEntity<FarmerReviewResponse> startReview(
            @PathVariable Integer reviewId,
            @AuthenticationPrincipal AdminUserDetails me){
        return ResponseEntity.ok(adminReviewService.reviewing(reviewId, me.getAdminId()));
    }

    // 修改審核: 核准
    @PutMapping("/{reviewId}/approve")
    public ResponseEntity<FarmerReviewResponse> approve(
            @PathVariable Integer reviewId,
            @AuthenticationPrincipal AdminUserDetails me) {
        return ResponseEntity.ok(adminReviewService.approve(reviewId, me.getAdminId()));
    }

    // 修改審核: 退件
    @PutMapping("/{reviewId}/reject")
    public ResponseEntity<FarmerReviewResponse> reject(
            @PathVariable Integer reviewId,
            @RequestBody @Valid ReviewRejectRequest req,
            @AuthenticationPrincipal AdminUserDetails me) {
        return ResponseEntity.ok(adminReviewService.reject(reviewId, me.getAdminId(), req.getRejectReason()));
    }

}
