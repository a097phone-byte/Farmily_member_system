package com.farmily.user.service;

import com.farmily.user.dto.FarmerReviewResponse;
import java.util.List;

// 管理員對小農審核操作
public interface AdminReviewService {

    // 待審清單
    List<FarmerReviewResponse> listPending();

    // 某小農所有審核紀錄
    List<FarmerReviewResponse> listByFarmer(Integer farmerId);

    // 核准
    FarmerReviewResponse approve(Integer reviewId, Integer adminId);

    // 退件
    FarmerReviewResponse reject(Integer reviewId, Integer adminId, String rejectReason);

}
