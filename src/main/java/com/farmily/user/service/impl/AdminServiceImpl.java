package com.farmily.user.service.impl;

import com.farmily.user.dto.*;
import com.farmily.user.model.Admin;
import com.farmily.user.repository.AdminRepository;
import com.farmily.user.service.AdminService;
import com.farmily.user.service.EmailUniquenessChecker;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailUniquenessChecker emailUniquenessChecker;

    public AdminServiceImpl(AdminRepository adminRepository, PasswordEncoder passwordEncoder, EmailUniquenessChecker emailUniquenessChecker) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailUniquenessChecker = emailUniquenessChecker;
    }

    // 管理員登入
    @Override
    public AdminProfileResponse login(LoginRequest log) {
        Admin admin = adminRepository.findByAdminEmail(log.getEmail())
                .orElseThrow(() -> new BadCredentialsException("帳號或密碼錯誤"));

        // 檢查 hash 密碼是否相等
        if (!passwordEncoder.matches(log.getPassword(), admin.getAdminPassword()))
            throw new IllegalStateException("帳號或密碼錯誤");

        if (admin.getAdminStatus() == Admin.AdminStatus.SUSPENDED ||
                admin.getAdminStatus() == Admin.AdminStatus.DELETED) {
            throw new IllegalStateException("此帳號已被停權或終止");
        }
        return AdminProfileResponse.from(admin);
    }

    // 管理員修改自己的資料（只能改名字，不能改狀態或權限）
    @Override
    public AdminProfileResponse updateMyProfile(Integer adminId, AdminSelfUpdateRequest req) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("查無此管理員"));

        if (req.getName() != null) {
            admin.setAdminName(req.getName());
        }
        admin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(admin);

        return AdminProfileResponse.from(admin);
    }


    // 管理員查自己資料 + 權限
    @Override
    @Transactional(readOnly = true)
    public AdminProfileResponse getMyProfile(Integer adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("查無此管理員"));

        // 查權限代碼
        List<String> codes = adminRepository.findPermissionCodesByAdminId(adminId);

        return AdminProfileResponse.from(admin, codes);
    }

    // ================================= 管理員對其他管理員 CRUD =================================
    // 新增管理員（含權限指派）
    @Override
    public AdminProfileResponse createAdmin(AdminCreateRequest req) {

        // step1: email 必須全系統唯一
        emailUniquenessChecker.emailAvailable(req.getEmail());

        // step2: 建立管理員帳號
        Admin admin = new Admin();
        admin.setAdminEmail(req.getEmail());
        admin.setAdminPassword(passwordEncoder.encode(req.getPassword()));
        admin.setAdminName(req.getName());
        admin.setAdminStatus(Admin.AdminStatus.ACTIVE);
        admin.setCreatedAt(LocalDateTime.now());
        Admin saved = adminRepository.save(admin);   // 存進 DB 後才會有 adminId

        // step3: 指派權限
        assignPermissions(saved.getAdminId(), req.getPermissionCodes());

        // step4: 回傳（含剛指派的權限）
        return AdminProfileResponse.from(saved, req.getPermissionCodes());
    }

    // 查所有管理員
    @Override
    public List<AdminProfileResponse> listAll() {
        List<Admin> admins = adminRepository.findAll();
        List<AdminProfileResponse> result = new ArrayList<>();
        for (Admin a : admins) {
            // 查權限代碼
            List<String> codes = adminRepository.findPermissionCodesByAdminId(a.getAdminId());
            result.add(AdminProfileResponse.from(a, codes));
        }
        return result;
    }

    // 查單一管理員（含他的權限）
    @Override
    public AdminProfileResponse getById(Integer adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("查無此管理員"));

        List<String> codes = adminRepository.findPermissionCodesByAdminId(adminId);
        return AdminProfileResponse.from(admin, codes);
    }

    // 修改其他管理員（名字、狀態、權限）
    @Override
    public AdminProfileResponse updateAdmin(Integer adminId, AdminUpdateRequest req) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("查無此管理員"));

        // 同階保護：超級管理員不能相互修改
        if (isSuperAdmin(adminId)) {
            throw new AccessDeniedException("不能修改其他超級管理員!");
        }

        // 有填才改
        if (req.getUpdateName() != null) {
            admin.setAdminName(req.getUpdateName());
        }
        if (req.getUpdateStatus() != null) {
            admin.setAdminStatus(Admin.AdminStatus.valueOf(req.getUpdateStatus()));   // 字串轉 enum
        }

        admin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(admin);

        // 有送權限才重新指派：先清空，再加新的
        if (req.getUpdatePermissionCodes() != null) {
            adminRepository.deletePermissionsByAdminId(adminId);
            assignPermissions(adminId, req.getUpdatePermissionCodes());
        }

        // + 權限
        List<String> codes = adminRepository.findPermissionCodesByAdminId(adminId);
        return AdminProfileResponse.from(admin, codes);
    }

    // 刪除管理員（軟刪除：改成 DELETED，不真的移除）
    @Override
    public void deleteAdmin(Integer adminId, Integer currentAdminId) {
        // 不能刪除自己
        if (adminId.equals(currentAdminId)) {
            throw new IllegalStateException("不能刪除自己的帳號!");
        }

        // 同階保護：超級管理員不能相互刪除
        if (isSuperAdmin(adminId)) {
            throw new AccessDeniedException("不能刪除其他超級管理員!");
        }

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("查無此管理員"));

        admin.setAdminStatus(Admin.AdminStatus.DELETED);
        admin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(admin);
    }


    // 自訂方法：把一串權限代碼指派給某管理員
    private void assignPermissions(Integer adminId, List<String> codes) {
        if (codes == null)
            return;

        for (String code : codes) {
            Integer permissionId = adminRepository.findPermissionIdByCode(code);
            if (permissionId == null) {
                throw new IllegalArgumentException("查無此權限代碼: " + code);
            }
            adminRepository.addPermission(adminId, permissionId);
        }
    }

    // 自訂判斷: 管理員是不是有 PERM_ADMIN 權限
    private boolean isSuperAdmin(Integer adminId) {
        List<String> codes = adminRepository.findPermissionCodesByAdminId(adminId);
        return codes.contains("ADMIN");
    }
}
