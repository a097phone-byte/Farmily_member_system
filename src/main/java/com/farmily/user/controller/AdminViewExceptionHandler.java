package com.farmily.user.controller;

import com.farmily.user.controller.view.AdminAccountViewController;
import com.farmily.user.controller.view.AdminFarmerViewController;
import com.farmily.user.controller.view.AdminMemberViewController;
import com.farmily.user.controller.view.AdminReviewViewController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// 後台頁面（Thymeleaf）專用錯誤處理：把業務規則擋下的例外顯示成友善頁面，
// 而不是走全域 @RestControllerAdvice 的純文字。
// 只套用在後台這 4 個 @Controller（assignableTypes），並用最高優先權蓋過全域處理。
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(assignableTypes = {
        AdminAccountViewController.class,
        AdminMemberViewController.class,
        AdminFarmerViewController.class,
        AdminReviewViewController.class
})
public class AdminViewExceptionHandler {

    // 權限不足（例：同階保護－不能修改/刪除其他超級管理員）
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "back-end/admin/opError";
    }

    // 狀態衝突（例：不能刪除自己、帳號已存在）
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "back-end/admin/opError";
    }

    // 查無資料 / 參數錯誤
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "back-end/admin/opError";
    }
}