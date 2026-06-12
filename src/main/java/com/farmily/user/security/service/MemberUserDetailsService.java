package com.farmily.user.security.service;

import com.farmily.user.model.User;
import com.farmily.user.repository.UserRepository;
import com.farmily.user.security.MemberUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MemberUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MemberUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // step1: 用 email 去 user 表撈帳號，撈不到就丟例外
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("查無此帳號: " + email));

        // step2: 純第三方登入帳號沒有本地密碼，不能走帳密登入
        if (user.getPassword() == null) {
            throw new UsernameNotFoundException("此帳號為第三方登入，請改用第三方登入");
        }

        // step3: 包成自製的 MemberUserDetails 回傳
        return new MemberUserDetails(user);
    }
}