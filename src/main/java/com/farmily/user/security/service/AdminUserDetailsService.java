package com.farmily.user.security.service;

import com.farmily.user.model.Admin;
import com.farmily.user.repository.AdminRepository;
import com.farmily.user.security.AdminUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// "How" to find Admin ?
@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    public AdminUserDetailsService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // step1. 用 email 去 admin 表撈帳號，撈不到就丟例外
        Admin admin = adminRepository.findByAdminEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("查無此帳號: " + email));

        // step2. 查出他擁有的權限代碼
        List<String> codes = adminRepository.findPermissionCodesByAdminId(admin.getAdminId());

        // step3. 轉換成 Spring Security 的 GrantedAuthority，組成 authority 清單
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        for (String code : codes) {
            authorities.add(new SimpleGrantedAuthority("PERM_" + code));
        }

        // step4. 把管理員 + 算好的權限，包成自製 AdminUserDetails 身分格式回傳
        return new AdminUserDetails(admin, authorities);
    }
}