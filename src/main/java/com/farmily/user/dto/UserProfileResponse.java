package com.farmily.user.dto;

import com.farmily.user.model.User;

import java.time.LocalDate;

// 將 Repository 回傳數據包裝成 dto
public class UserProfileResponse {

    private Integer userId;
    private String email;
    private String userName;
    private String userNickname;
    private String userPhoneNum;
    private String userAddress;
    private String cityName;
    private String distName;
    private LocalDate birthday;
    private Integer monthlySpending;
    private Boolean emailVerified;
    private Boolean farmerIdentity;
    private String authProvider;      // 讓前端知道這帳號是 LOCAL/GOOGLE
    private Boolean hasPassword;      // 前端可根據此決定顯示「修改密碼」還是「設定密碼」

    // 自訂 from() 方法
    public static UserProfileResponse from(User u) {

        UserProfileResponse dto = new UserProfileResponse();
        dto.userId = u.getUserId();
        dto.email = u.getEmail();
        dto.userName = u.getUserName();
        dto.userNickname = u.getUserNickname();
        dto.userPhoneNum = u.getUserPhoneNum();
        dto.userAddress = u.getUserAddress();
        if (u.getCityDistrict() != null) {
            dto.cityName = u.getCityDistrict().getCityName();
            dto.distName = u.getCityDistrict().getDistName();
        }
        dto.birthday = u.getBirthday();
        dto.monthlySpending = u.getMonthlySpending();
        dto.emailVerified = u.getEmailVerified();
        dto.farmerIdentity = u.getFarmerIdentity();
        dto.authProvider = u.getAuthProvider() != null ? u.getAuthProvider().name() : null;
        dto.hasPassword = u.getPassword() != null;
        return dto;
    }

    public Integer getUserId() {
        return userId;
    }
    public String getEmail() {
        return email;
    }
    public String getUserName() {
        return userName;
    }
    public String getUserNickname() {
        return userNickname;
    }
    public String getUserPhoneNum() {
        return userPhoneNum;
    }
    public String getUserAddress() {
        return userAddress;
    }
    public String getCityName() {
        return cityName;
    }
    public String getDistName() {
        return distName;
    }
    public LocalDate getBirthday() {
        return birthday;
    }
    public Integer getMonthlySpending() {
        return monthlySpending;
    }
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    public Boolean getFarmerIdentity() {
        return farmerIdentity;
    }
    public String getAuthProvider() {
        return authProvider;
    }
    public Boolean getHasPassword() {
        return hasPassword;
    }


}
