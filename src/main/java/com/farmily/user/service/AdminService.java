package com.farmily.user.service;

import com.farmily.user.dto.*;

import java.util.List;

public interface AdminService {

    // 管理員後台登入
    AdminProfileResponse login (LoginRequest log);

    // 管理員修改自己的資料 (只改名字)
    AdminProfileResponse updateMyProfile(Integer adminId, AdminSelfUpdateRequest req);

    // 查管理員個人資料
    AdminProfileResponse getMyProfile(Integer adminId);

    // ======== 管理員對其他管理員 CRUD ========
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

    // 列出系統所有可指派的權限 (給前端動態產生勾選清單)
    List<PermissionResponse> listPermissions();
}
