package com.farmily.user.controller.view;

import com.farmily.user.dto.AdminCreateRequest;
import com.farmily.user.dto.AdminUpdateRequest;
import com.farmily.user.dto.PermissionResponse;
import com.farmily.user.security.AdminUserDetails;
import com.farmily.user.service.AdminService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 管理後台－管理員 CRUD 頁（Thymeleaf）；整條 /admin/admins/** 需 PERM_ADMIN
@Controller
@RequestMapping("/admin/admins")
public class AdminAccountViewController {

    private final AdminService adminService;

    public AdminAccountViewController(AdminService adminService) {
        this.adminService = adminService;
    }

    // 每頁都需要的所有可指派權限清單
    @ModelAttribute("permissionListData")
    public List<PermissionResponse> permissionListData() {
        return adminService.listPermissions();
    }

    // 管理員清單（頁面同時含新增表單）
    @GetMapping
    public String listAll(ModelMap model) {
        model.addAttribute("adminListData", adminService.listAll());
        return "back-end/admin/listAllAdmin";
    }

    // 新增管理員（email/password/name/permissionCodes 由表單自動綁進 AdminCreateRequest）
    @PostMapping
    public String create(AdminCreateRequest req, ModelMap model) {
        adminService.createAdmin(req);
        return backToList(model, "（已新增管理員）");
    }

    // 顯示「修改頁」
    @GetMapping("/{adminId}/edit")
    public String editForm(@PathVariable Integer adminId, ModelMap model) {
        model.addAttribute("admin", adminService.getById(adminId));
        return "back-end/admin/updateAdminInput";
    }

    // 送出修改
    @PostMapping("/{adminId}/update")
    public String update(@PathVariable Integer adminId, AdminUpdateRequest req, ModelMap model) {
        adminService.updateAdmin(adminId, req);
        return backToList(model, "（已修改管理員）");
    }

    // 刪除（軟刪除）；帶目前登入者 id 以擋「刪自己」
    @PostMapping("/{adminId}/delete")
    public String delete(@PathVariable Integer adminId,
                         @AuthenticationPrincipal AdminUserDetails me, ModelMap model) {
        adminService.deleteAdmin(adminId, me.getAdminId());
        return backToList(model, "（已刪除管理員）");
    }

    // 私有小工具：重查清單、回列表頁
    private String backToList(ModelMap model, String success) {
        model.addAttribute("adminListData", adminService.listAll());
        model.addAttribute("success", success);
        return "back-end/admin/listAllAdmin";
    }
}