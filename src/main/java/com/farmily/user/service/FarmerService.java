package com.farmily.user.service;

import com.farmily.user.dto.*;

public interface FarmerService {

    // 本地註冊
    FarmerProfileResponse register(FarmerRegisterRequest reg);

    // 本地登入
    FarmerProfileResponse login(LoginRequest log);

    // 查個人資料
    FarmerProfileResponse getMyProfile(Integer farmerId);

    // 修改不用重審資料
    FarmerProfileResponse updateContactInfo(Integer farmerId, FarmerProfileUpdateRequest req);

    // 修改需要重審資料
    FarmerProfileResponse updateReviewRequiredInfo(Integer farmerId, FarmerResubmitRequest req);

    // 修改密碼
    void changePassword(Integer farmerId, ChangePasswordRequest pw);

}
