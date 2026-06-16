package com.farmily.user.controller;

import com.farmily.user.dto.FarmerProfileResponse;
import com.farmily.user.service.AdminFarmerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/farmers")
public class AdminFarmerController {

    private final AdminFarmerService adminFarmerService;

    public AdminFarmerController(AdminFarmerService adminFarmerService) {
        this.adminFarmerService = adminFarmerService;
    }

    // 查所有小農
    @GetMapping
    public ResponseEntity<List<FarmerProfileResponse>> list() {
        return ResponseEntity.ok(adminFarmerService.listAll());
    }

    // 查單一小農
    @GetMapping("/{farmerId}")
    public ResponseEntity<FarmerProfileResponse> getOne(@PathVariable Integer farmerId) {
        return ResponseEntity.ok(adminFarmerService.getById(farmerId));
    }

}
