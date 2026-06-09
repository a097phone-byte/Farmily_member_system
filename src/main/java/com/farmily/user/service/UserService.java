package com.farmily.user.service;

import com.farmily.user.dto.*;

public interface UserService {

    // 本地註冊
    UserProfileResponse register(UserRegisterRequest reg);

    // 本地登入
    UserProfileResponse login (UserLoginRequest log);

    // OAuth 登入/自動註冊
    UserProfileResponse loginOrRegisterOAuth (OAuthUserInfo info);

    UserProfileResponse getMyProfile(Integer userId);

    UserProfileResponse updateMyProfile(Integer userId, UserUpdateRequest update);

    void changePassword(Integer userId, ChangePasswordRequest pw);

}
