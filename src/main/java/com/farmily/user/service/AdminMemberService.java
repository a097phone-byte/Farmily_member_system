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

    // 篩選查詢會員條件: 消費級距、狀態（皆可複選；null 或空清單 = 不限）
    List<UserProfileResponse> list(List<String> tierNames, List<String> statuses);

}
