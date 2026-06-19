package com.farmily.user.service;

import com.farmily.user.dto.FarmerReviewResponse;
import com.farmily.user.dto.LoginRequest;
import com.farmily.user.dto.PublicFarmerResubmitRequest;

// 小農可查看審核結果(包含初審未通過、通過) - 不存 SecurityContext
public interface FarmerApplicationService {

    // 查自己最新一筆審核結果
    FarmerReviewResponse checkStatus(LoginRequest loginRequest);

    // 免登入：重新送審（僅限尚未通過 PENDING 的申請者）
    FarmerReviewResponse resubmit(PublicFarmerResubmitRequest req);

}
