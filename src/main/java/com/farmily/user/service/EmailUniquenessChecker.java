package com.farmily.user.service;

import com.farmily.user.repository.AdminRepository;
import com.farmily.user.repository.FarmerRepository;
import com.farmily.user.repository.UserRepository;
import org.springframework.stereotype.Component;

// 確保 email 在 user / farmer / admin 三張表都沒被用過（全系統唯一）
@Component
public class EmailUniquenessChecker {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final FarmerRepository farmerRepository;

    public EmailUniquenessChecker(UserRepository userRepository,
                                  AdminRepository adminRepository,
                                  FarmerRepository farmerRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.farmerRepository = farmerRepository;
    }

    // 任何一張表有存在 email 就擋下
    public void emailAvailable(String email){

        boolean used = userRepository.existsByEmail(email)
                || farmerRepository.existsByEmail(email)
                || adminRepository.existsByAdminEmail(email);

        if(used){
            throw new IllegalStateException("此 Email 已被註冊，請改用其他 Email");     // 409 資源衝突
        }
    }
}