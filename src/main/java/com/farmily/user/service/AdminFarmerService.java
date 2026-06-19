package com.farmily.user.service;

import com.farmily.user.dto.FarmerProfileResponse;

import java.util.List;

// 管理員對小農操作
public interface AdminFarmerService {

    // 列出所有小農
    List<FarmerProfileResponse> listAll();

    // 查單一小農
    FarmerProfileResponse getById(Integer farmerId);

    // 改小農狀態
    FarmerProfileResponse suspend(Integer farmerId);     // ACTIVE → SUSPENDED

    FarmerProfileResponse reinstate(Integer farmerId);   // SUSPENDED → ACTIVE
}
