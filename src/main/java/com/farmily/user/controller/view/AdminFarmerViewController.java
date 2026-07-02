package com.farmily.user.controller.view;


import com.farmily.user.service.AdminFarmerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

// 管理後台－小農管理頁（Thymeleaf）
@Controller
@RequestMapping("/admin/farmers")
public class AdminFarmerViewController {

    private final AdminFarmerService adminFarmerService;

    public AdminFarmerViewController(AdminFarmerService adminFarmerService) {
        this.adminFarmerService = adminFarmerService;
    }

    // 小農清單
    @GetMapping
    public String listAll(ModelMap model) {
        model.addAttribute("farmerListData", adminFarmerService.listAll());
        return "back-end/admin/listAllFarmer";
    }

    // 停權（ACTIVE -> SUSPENDED）
    @PostMapping("/{farmerId}/suspend")
    public String suspend(@PathVariable Integer farmerId, ModelMap model) {
        adminFarmerService.suspend(farmerId);
        model.addAttribute("farmerListData", adminFarmerService.listAll());
        model.addAttribute("success", "（已停權）");
        return "back-end/admin/listAllFarmer";
    }

    // 恢復（SUSPENDED -> ACTIVE）
    @PostMapping("/{farmerId}/reinstate")
    public String reinstate(@PathVariable Integer farmerId, ModelMap model) {
        adminFarmerService.reinstate(farmerId);
        model.addAttribute("farmerListData", adminFarmerService.listAll());
        model.addAttribute("success", "（已恢復）");
        return "back-end/admin/listAllFarmer";
    }
}