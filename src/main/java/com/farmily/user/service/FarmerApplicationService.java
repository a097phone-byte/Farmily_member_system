package com.farmily.user.service;

import com.farmily.user.dto.FarmerReviewResponse;
import com.farmily.user.dto.LoginRequest;
import com.farmily.user.dto.PublicFarmerResubmitRequest;

// 小農可查看審核結果(包含初審未通過、通過) - 不存 SecurityContext
public interface FarmerApplicationService {

    // 查自己最新審核結果
    FarmerReviewResponse checkStatus(LoginRequest loginRequest);

    // 免登入：重新送審（僅限尚未通過 PENDING 的申請者）
    FarmerReviewResponse resubmit(PublicFarmerResubmitRequest req);

    // 免登入：重寄「啟用信」（給已核准 ACTIVE 但尚未完成 Email 驗證、無法登入的小農）
    void resendActivation(LoginRequest loginRequest);

}
