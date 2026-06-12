package com.farmily.user.security;

import com.farmily.user.model.User;
import com.farmily.user.repository.AdminRepository;
import com.farmily.user.repository.FarmerRepository;
import com.farmily.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//  Spring Security 驗證身份
@Component
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;
    private final AdminRepository adminRepository;

    @Autowired
    public MyUserDetailsService(UserRepository userRepository,
                                FarmerRepository farmerRepository,
                                AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.farmerRepository = farmerRepository;
        this.adminRepository = adminRepository;
    }

    @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

            // 從資料庫中查詢 User 數據
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("查無此帳號: " + username));

                String userEmail = user.getEmail();
                String userPassword = user.getPassword();

                // 權限部分
                List<GrantedAuthority> authorities = new ArrayList<>();

                // 轉換成 Spring Security 指定的 User 格式: (帳號, 密碼, 權限)
                return new org.springframework.security.core.userdetails.User(
                        userEmail,
                        userPassword,
                        authorities
                );

        }

}
