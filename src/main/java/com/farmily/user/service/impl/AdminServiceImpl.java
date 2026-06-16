package com.farmily.user.service.impl;

import com.farmily.user.dto.AdminProfileResponse;
import com.farmily.user.dto.LoginRequest;
import com.farmily.user.model.Admin;
import com.farmily.user.repository.AdminRepository;
import com.farmily.user.service.AdminService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 管理員登入
    @Override
    public AdminProfileResponse login(LoginRequest log) {
        Admin admin = adminRepository.findByAdminEmail(log.getEmail())
                .orElseThrow(() -> new BadCredentialsException("帳號或密碼錯誤"));

        // 檢查 hash 密碼是否相等
        if(!passwordEncoder.matches(log.getPassword(), admin.getAdminPassword()))
            throw new IllegalStateException("帳號或密碼錯誤");

        if(admin.getAdminStatus() == Admin.AdminStatus.SUSPENDED ||
                admin.getAdminStatus() == Admin.AdminStatus.DELETED){
            throw new IllegalStateException("此帳號已被停權或終止");
        }
        return AdminProfileResponse.from(admin);
    }

    // 管理員查個人資料
    @Override
    @Transactional(readOnly = true)
    public AdminProfileResponse getMyProfile(Integer adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("查無此管理員"));
        return AdminProfileResponse.from(admin);
    }
}
