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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // 登入也查權限
        List<String> codes = adminRepository.findPermissionCodesByAdminId(admin.getAdminId());

        return AdminProfileResponse.from(admin, codes);
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

    // 查所有管理員（每位都要附上他擁有的權限代碼）
    @Override
    public List<AdminProfileResponse> listAll() {
        // 步驟 1：撈出所有管理員（1 次查詢）
        List<Admin> admins = adminRepository.findAll();

        // 步驟 2：一次撈出「所有管理員的所有權限」（1 次查詢）。
        //   findAllAdminPermissionCodes() 回傳的每一列是一個 Object[] 陣列，固定 2 格：
        //     row[0] = admin_id（哪位管理員）、row[1] = permission_code（一個權限代碼）
        //   例如資料庫有：1→"ADMIN", 1→"REVIEW", 2→"REVIEW"，就會回傳 3 列。
        //   為什麼用 Object[]？因為這筆查詢一次選了「兩個不同欄位」，
        //   Spring 沒有對應的現成型別，只好用「一個陣列裝一列的多個欄位值」。
        List<Object[]> rows = adminRepository.findAllAdminPermissionCodes();

        // 步驟 3：把上面「平鋪的列」整理成「依管理員分組」的對照表。
        //   key = adminId、value = 該管理員的權限代碼清單。
        //   整理完會像：{ 1 → ["ADMIN","REVIEW"], 2 → ["REVIEW"] }
        Map<Integer, List<String>> codesByAdminId = new HashMap<>();
        for (Object[] row : rows) {
            // 把這一列的兩個欄位取出來。row 裡存的是 Object，要轉回原本的型別才能用。
            Integer adminId = (Integer) row[0];
            String code = (String) row[1];

            // 先看這位管理員在對照表裡有沒有清單了
            List<String> codes = codesByAdminId.get(adminId);
            if (codes == null) {
                // 還沒有: 第一次遇到這位管理員，幫他建一個新的空清單並放進對照表
                codes = new ArrayList<>();
                codesByAdminId.put(adminId, codes);
            }
            // 把這個權限代碼加進該管理員的清單
            codes.add(code);
        }

        // 步驟 4：逐位管理員組裝回應 DTO。
        //   權限直接從步驟 3 的對照表「用記憶體查」拿，不再回資料庫查 → 這就是省下 N 次查詢的關鍵。
        List<AdminProfileResponse> result = new ArrayList<>();
        for (Admin a : admins) {
            List<String> codes = codesByAdminId.get(a.getAdminId());
            if (codes == null) {
                // 這位管理員一個權限都沒有（對照表查不到）→ 給空清單，避免 null
                codes = new ArrayList<>();
            }
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

        // 改名 - 有填才改
        if (req.getUpdateName() != null) {
            admin.setAdminName(req.getUpdateName());
        }
        // 改狀態 - 有填才改
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
