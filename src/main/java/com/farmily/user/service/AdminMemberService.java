package com.farmily.user.service;

import com.farmily.user.dto.UserProfileResponse;

import java.util.List;

// 管理員對會員操作
public interface AdminMemberService {

    // 列出所有會員
    List<UserProfileResponse> listAll();

    // 查單一會員
    UserProfileResponse getById(Integer userId);

    // 改會員狀態(警告/停權/恢復)
    UserProfileResponse updateStatus(Integer userId, String status);
}
