package com.farmily.user.controller.view;

import com.farmily.user.security.AdminUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

// 管理後台頁面（Thymeleaf）：登入頁、總覽頁
@Controller
public class AdminViewController {

    // 登入頁（GET）；POST /admin/login 由 Spring Security 的 formLogin 處理，不用自己寫
    @GetMapping("/admin/login")
    public String loginPage() {
        return "back-end/admin/login";
    }

    // 總覽頁（登入後才進得來）
    @GetMapping("/admin/dashboard")
    public String dashboard(@AuthenticationPrincipal AdminUserDetails me, ModelMap model) {
        model.addAttribute("adminName", me.getAdmin().getAdminName());
        return "back-end/admin/dashboard";
    }
}