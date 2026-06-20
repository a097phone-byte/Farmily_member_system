package com.farmily.user.service;

import com.farmily.user.dto.AdminCreateRequest;
import com.farmily.user.dto.AdminProfileResponse;
import com.farmily.user.dto.AdminUpdateRequest;
import com.farmily.user.dto.LoginRequest;

import java.util.List;

public interface AdminService {

    // 後台登入
    AdminProfileResponse login (LoginRequest log);

    // 查個人資料
    AdminProfileResponse getMyProfile(Integer adminId);

    // CRUD - 新增管理員
    AdminProfileResponse createAdmin(AdminCreateRequest req);

    // CRUD - 查全部管理員
    List<AdminProfileResponse> listAll();

    // CRUD - 查單一管理員
    AdminProfileResponse getById(Integer adminId);

    // CRUD - 修改管理員
    AdminProfileResponse updateAdmin(Integer adminId, AdminUpdateRequest req);

    // CRUD - 刪除管理員
    void deleteAdmin(Integer adminId, Integer currentAdminId);
}
