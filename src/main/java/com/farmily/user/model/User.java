package com.farmily.user.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "`user`")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum UserStatus {
        ACTIVE, WARNED, SUSPENDED, DELETED
    }

    // Oauth 2.0 (LOCAL = 本地註冊, GOOGLE = 第三方)
    public enum AuthProvider { LOCAL, GOOGLE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", updatable = false)
    private Integer userId;

    @Column(name = "email", unique = true)
    private String email;

    @ManyToOne
	@JoinColumn(name = "district_id", referencedColumnName = "district_id")
    private CityDistrict cityDistrict;

    @Column(name = "user_address")
    private String userAddress;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @Column(name = "password", nullable = true)
    private String password;

    @Column(name = "user_created_at")
    private LocalDateTime userCreatedAt;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_nickname")
    private String userNickname;

    @Column
    private LocalDate birthday;

    @Column(name = "user_phone_num")
    private String userPhoneNum;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status")
    private UserStatus userStatus = UserStatus.ACTIVE;

    @Column(name = "monthly_spending")
    private Integer monthlySpending = 0;

    @Column(name = "farmer_identity")
    private Boolean farmerIdentity;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider")
    private AuthProvider authProvider;

    @Column(name = "provider_id")
    private String providerId;


    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public CityDistrict getCityDistrict() {
        return cityDistrict;
    }

    public void setCityDistrict(CityDistrict cityDistrict) {
        this.cityDistrict = cityDistrict;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getUserCreatedAt() {
        return userCreatedAt;
    }

    public void setUserCreatedAt(LocalDateTime userCreatedAt) {
        this.userCreatedAt = userCreatedAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getUserPhoneNum() {
        return userPhoneNum;
    }

    public void setUserPhoneNum(String userPhoneNum) {
        this.userPhoneNum = userPhoneNum;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public Integer getMonthlySpending() {
        return monthlySpending;
    }

    public void setMonthlySpending(Integer monthlySpending) {
        this.monthlySpending = monthlySpending;
    }

    public Boolean getFarmerIdentity() {
        return farmerIdentity;
    }

    public void setFarmerIdentity(Boolean farmerIdentity) {
        this.farmerIdentity = farmerIdentity;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

}
