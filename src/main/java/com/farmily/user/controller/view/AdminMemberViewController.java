package com.farmily.user.controller.view;

import com.farmily.user.dto.UserProfileResponse;
import com.farmily.user.service.AdminMemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 管理後台－會員管理頁（Thymeleaf）
@Controller
@RequestMapping("/admin/members")
public class AdminMemberViewController {

    private final AdminMemberService adminMemberService;

    public AdminMemberViewController(AdminMemberService adminMemberService) {
        this.adminMemberService = adminMemberService;
    }

    // 會員清單
    @GetMapping
    public String listAll(ModelMap model) {
        List<UserProfileResponse> list = adminMemberService.listAll();
        model.addAttribute("memberListData", list);
        return "back-end/admin/listAllMember";
    }

    // 變更會員狀態 (POST)
    @PostMapping("/{userId}/status")
    public String updateStatus(@PathVariable Integer userId,
                               @RequestParam("status") String status,
                               ModelMap model) {
        // 1.更新狀態
        adminMemberService.updateStatus(userId, status);
        // 2.重新查全部，回列表頁
        List<UserProfileResponse> list = adminMemberService.listAll();
        model.addAttribute("memberListData", list);
        model.addAttribute("success", "（會員狀態已更新）");
        return "back-end/admin/listAllMember";
    }
}