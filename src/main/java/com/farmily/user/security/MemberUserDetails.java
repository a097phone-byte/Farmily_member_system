package com.farmily.user.security;

import com.farmily.user.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// Security - 自訂 Member 身分識別格式
public class MemberUserDetails implements UserDetails {

    private final User user;

    public MemberUserDetails(User user) {
        this.user = user;
    }

    // 給 Controller 取登入者 id
    public Integer getUserId() {
        return user.getUserId();
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }


    // 帳號 = email
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // true: 帳號沒過期，正常
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // true: 帳號沒被鎖，正常
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 帳號是否啟用: ACTIVE、WARNED 狀態仍可登入
    @Override
    public boolean isEnabled() {
        return user.getUserStatus() == User.UserStatus.ACTIVE
                || user.getUserStatus() == User.UserStatus.WARNED;
    }

    // true: 密碼仍有效
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

}
