package com.farmily.user.controller;

import com.farmily.user.dto.FarmerReviewResponse;
import com.farmily.user.dto.LoginRequest;
import com.farmily.user.dto.PublicFarmerResubmitRequest;
import com.farmily.user.service.FarmerApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 免登入的小農申請入口（給尚未通過初審、無法登入的申請者）
@RestController
@RequestMapping("/api/farmer/application")
public class FarmerApplicationController {

    private final FarmerApplicationService farmerApplicationService;

    public FarmerApplicationController(FarmerApplicationService farmerApplicationService) {
        this.farmerApplicationService = farmerApplicationService;
    }

    // 查最新審核狀態 + 退件理由
    @PostMapping("/status")
    public ResponseEntity<FarmerReviewResponse> status(@RequestBody @Valid LoginRequest log) {
        return ResponseEntity.ok(farmerApplicationService.checkStatus(log));
    }

    // 重新送審
    @PostMapping("/resubmit")
    public ResponseEntity<FarmerReviewResponse> resubmit(@RequestBody @Valid PublicFarmerResubmitRequest req) {
        return ResponseEntity.ok(farmerApplicationService.resubmit(req));
    }
}
