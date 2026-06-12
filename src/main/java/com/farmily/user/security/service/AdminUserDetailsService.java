package com.farmily.user.security.service;

import com.farmily.user.model.Admin;
import com.farmily.user.repository.AdminRepository;
import com.farmily.user.security.AdminUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    public AdminUserDetailsService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // step1: 用 email 去 admin 表撈帳號，撈不到就丟例外
        Admin admin = adminRepository.findByAdminEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("查無此帳號: " + email));

        // step2: 包成自製的 AdminUserDetails 回傳
        return new AdminUserDetails(admin);
    }
}