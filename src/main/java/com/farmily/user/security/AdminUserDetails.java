package com.farmily.user.security;

import com.farmily.user.model.Admin;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// 自訂 Admin 身分識別格式
public class AdminUserDetails implements UserDetails {

    private final Admin admin;
    private final List<GrantedAuthority> authorities;

    public AdminUserDetails(Admin admin, List<GrantedAuthority> authorities) {
        this.admin = admin;
        this.authorities = authorities;
    }

    // 給 Controller 取登入者 id
    public Integer getAdminId() {
        return admin.getAdminId();
    }
    public Admin getAdmin() {
        return admin;
    }

    // 管理員的權限清單
    @Override
    public List<GrantedAuthority> getAuthorities() {
        return authorities;
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
