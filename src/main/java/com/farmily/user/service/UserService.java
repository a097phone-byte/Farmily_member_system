package com.farmily.user.service;

import com.farmily.user.dto.*;

public interface UserService {

    // 本地註冊
    UserProfileResponse register(UserRegisterRequest reg);

    // 本地登入
    UserProfileResponse login (LoginRequest log);

    // OAuth 登入/自動註冊
    UserProfileResponse loginOrRegisterOAuth (OAuthUserInfo info);

    // 查個人資料
    UserProfileResponse getMyProfile(Integer userId);

    // 修改資料
    UserProfileResponse updateMyProfile(Integer userId, UserUpdateRequest update);

    // 修改密碼
    void changePassword(Integer userId, ChangePasswordRequest pw);

    // 移除會員
    void deleteUser(Integer userId);

}
