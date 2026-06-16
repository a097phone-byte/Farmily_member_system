package com.farmily.user.service;

import com.farmily.user.dto.AdminProfileResponse;
import com.farmily.user.dto.LoginRequest;

public interface AdminService {

    // 後台登入
    AdminProfileResponse login (LoginRequest log);

    // 查個人資料
    AdminProfileResponse getMyProfile(Integer adminId);
}
