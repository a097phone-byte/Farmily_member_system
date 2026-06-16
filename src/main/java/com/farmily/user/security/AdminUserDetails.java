package com.farmily.user.security;

import com.farmily.user.model.Admin;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// Security - 自訂 AdminUserDetailsService 回傳 User 格式
public class AdminUserDetails implements UserDetails {

    private final Admin admin;

    public AdminUserDetails(Admin admin) {
        this.admin = admin;
    }

    // 給 Controller 取登入者 id
    public Integer getAdminId() {
        return admin.getAdminId();
    }
    public Admin getAdmin() {
        return admin;
    }

    // 之後要做細粒度權限控管，可在這裡把 AdminPermissionRole 轉成多個 authority
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    // 帳號 = email
    @Override
    public String getUsername() {
        return admin.getAdminEmail();
    }

    @Override
    public String getPassword() {
        return admin.getAdminPassword();
    }

    // true: 沒過期，正常
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // true: 沒被鎖，正常
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // true: 密碼仍有效
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 帳號是否啟用
    @Override
    public boolean isEnabled() {
        return admin.getAdminStatus() == Admin.AdminStatus.ACTIVE;
    }
}
