package com.farmily.user.controller;

import com.farmily.user.dto.FarmerReviewResponse;
import com.farmily.user.dto.ReviewRejectRequest;
import com.farmily.user.security.AdminUserDetails;
import com.farmily.user.service.AdminReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    // 證明文件一律為圖片（png/jpg），固定回傳 image 類型，瀏覽器即可內嵌顯示
    @GetMapping("/{reviewId}/cert/{type}")
    public ResponseEntity<byte[]> getCert(
            @PathVariable Integer reviewId,
            @PathVariable String type) {
        byte[] bytes = adminReviewService.getCertFile(reviewId, type);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"cert-" + reviewId + "-" + type + ".jpg\"")
                .body(bytes);
    }

}
