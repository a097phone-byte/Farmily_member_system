package com.farmily.user.security;

import com.farmily.user.model.Farmer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// Security - 自訂 Farmer 身分識別格式
public class FarmerUserDetails implements UserDetails {

    private final Farmer farmer;

    public FarmerUserDetails(Farmer farmer) {
        this.farmer = farmer;
    }

    // 給 Controller 取登入者 id
    public Integer getFarmerId() {
        return farmer.getFarmerId();
    }

    public Farmer getFarmer() {
        return farmer;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_FARMER"));
    }

    // 帳號 = email
    @Override
    public String getUsername() {
        return farmer.getEmail();
    }

    @Override
    public String getPassword() {
        return farmer.getPassword();
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
        return farmer.getFarmerStatus() == Farmer.FarmerStatus.ACTIVE;
    }

}
