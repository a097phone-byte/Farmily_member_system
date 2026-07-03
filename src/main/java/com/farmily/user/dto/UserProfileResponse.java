package com.farmily.user.dto;

import com.farmily.user.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 將 Repository 回傳數據包裝成 dto
public class UserProfileResponse {

    private Integer userId;
    private String email;
    private String userName;
    private String userNickname;
    private String userPhoneNum;
    private Integer districtId;
    private String cityName;
    private String distName;
    private String userAddress;
    private LocalDate birthday;
    private Integer monthlySpending;
    private String spendingTier;
    private Boolean emailVerified;
    private String authProvider;      // 讓前端知道這帳號是 LOCAL/GOOGLE
    private Boolean hasPassword;      // 前端可根據此決定顯示「修改密碼」還是「設定密碼」
    private LocalDateTime userCreatedAt;
    private User.UserStatus userStatus;

    // getter
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
    public Integer getDistrictId() {
        return districtId;
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
    public String getSpendingTier() {
        return spendingTier;
    }
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    public String getAuthProvider() {
        return authProvider;
    }
    public Boolean getHasPassword() {
        return hasPassword;
    }
    public LocalDateTime getUserCreatedAt() {
        return userCreatedAt;
    }
    public User.UserStatus getUserStatus() {
        return userStatus;
    }

    // 自訂 from() 方法: User + Spending Tier 聯表資料回傳
    public static UserProfileResponse from(User u, String...spendingTier) {

        UserProfileResponse dto = new UserProfileResponse();
        dto.userId = u.getUserId();
        dto.email = u.getEmail();
        dto.userName = u.getUserName();
        dto.userNickname = u.getUserNickname();
        dto.userPhoneNum = u.getUserPhoneNum();
        if (u.getCityDistrict() != null) {
            dto.districtId = u.getCityDistrict().getDistrictId();
            dto.cityName = u.getCityDistrict().getCityName();
            dto.distName = u.getCityDistrict().getDistName();
        }
        dto.userAddress = u.getUserAddress();
        dto.birthday = u.getBirthday();
        dto.monthlySpending = u.getMonthlySpending();
        if (spendingTier.length > 0) {
            dto.spendingTier = spendingTier[0];
        }
        dto.emailVerified = u.getEmailVerified();
        dto.authProvider = u.getAuthProvider() != null ? u.getAuthProvider().name() : null;
        dto.hasPassword = u.getPassword() != null;
        dto.userCreatedAt = u.getUserCreatedAt();
        dto.userStatus = u.getUserStatus();

        return dto;
    }

}
