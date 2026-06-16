package com.farmily.user.service;

import com.farmily.user.dto.*;
import com.farmily.user.model.CityDistrict;
import com.farmily.user.model.User;
import com.farmily.user.repository.CityDistrictRepository;
import com.farmily.user.repository.UserRepository;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CityDistrictRepository cityDistrictRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailUniquenessChecker emailUniquenessChecker;

    public UserServiceImpl(UserRepository userRepository,
                           CityDistrictRepository cityDistrictRepository,
                           PasswordEncoder passwordEncoder,
                           EmailUniquenessChecker emailUniquenessChecker) {
        this.userRepository = userRepository;
        this.cityDistrictRepository = cityDistrictRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailUniquenessChecker = emailUniquenessChecker;
    }

    // 註冊流程
    @Override
    public UserProfileResponse register(UserRegisterRequest reg) {

        // step1: 先檢查是否存在相同會員帳號 (email)
        User existingUser = userRepository.findByEmail(reg.getEmail()).orElse(null);    //回傳 Optional

        // 會員帳號 (email) 存在
        if (existingUser != null) {

            // 狀況 A: 帳號「沒有本地密碼」，代表他只有第三方登入資訊 (需補上跳轉)
            if (existingUser.getPassword() == null
                    && existingUser.getAuthProvider() == User.AuthProvider.GOOGLE) {
                throw new IllegalStateException("此帳號已使用 Google 登入，請改用 Google 登入");
            }
            // 狀況 B: 剩餘(已有本地密碼)就是一般重複註冊
            throw new IllegalStateException("帳號已註冊，請直接登入");
        }

        // step2: 跨表(小農/管理員)檢查 email 全域唯一
        emailUniquenessChecker.emailAvailable(reg.getEmail());

        // step3: 會員帳號(email)不存在，走本地註冊流程
        User newUser = new User();
        newUser.setEmail(reg.getEmail());

        // hash 原始密碼
        String hashedPassword = passwordEncoder.encode(reg.getPassword());
        newUser.setPassword(hashedPassword);

        newUser.setAuthProvider(User.AuthProvider.LOCAL);
        newUser.setUserName(reg.getUserName());
        newUser.setUserNickname(reg.getUserNickname());

        // 抓 city 物件前先判斷
        if (reg.getDistrictId() != null) {
            CityDistrict city = cityDistrictRepository.findById(reg.getDistrictId())
                    .orElseThrow(() -> new IllegalArgumentException("查無此區域 districtId=" + reg.getDistrictId()));
            newUser.setCityDistrict(city);
        }

        newUser.setUserAddress(reg.getUserAddress());
        newUser.setUserPhoneNum(reg.getUserPhoneNum());
        newUser.setBirthday(reg.getBirthday());
        newUser.setUserCreatedAt(LocalDateTime.now());
        newUser.setEmailVerified(false);
        newUser.setMonthlySpending(0);
        newUser.setUserStatus(User.UserStatus.ACTIVE);
        newUser.setFarmerIdentity(false);

        // 包裝會員資料成 dto 給 Controller
        return UserProfileResponse.from(userRepository.save(newUser));
    }

    // 登入流程
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse login(LoginRequest log) {

        User user = userRepository.findByEmail(log.getEmail())
                .orElseThrow(() -> new BadCredentialsException("帳號或密碼錯誤"));

        // 純 Google 帳號沒有本地密碼，null 檢查必須在 matches() 之前
        if (user.getPassword() == null) {
            throw new IllegalStateException("此帳號為第三方登入，請改用 Google 登入");
        }

        // 檢查 hash 密碼是否相等
        if (!passwordEncoder.matches(log.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("帳號或密碼錯誤");
        }

        if (user.getUserStatus() == User.UserStatus.SUSPENDED
                || user.getUserStatus() == User.UserStatus.DELETED) {
            throw new IllegalStateException("此帳號已停用");
        }
        return UserProfileResponse.from(user);
    }

    // 查資料
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("查無此用戶"));
        return UserProfileResponse.from(user);
    }

    // 修改資料
    @Override
    public UserProfileResponse updateMyProfile(Integer userId, UserUpdateRequest update) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("查無此用戶"));

        if (update.getUserName() != null)
            user.setUserName(update.getUserName());
        if (update.getUserNickname() != null)
            user.setUserNickname(update.getUserNickname());
        if (update.getUserPhoneNum() != null)
            user.setUserPhoneNum(update.getUserPhoneNum());
        if (update.getUserAddress() != null)
            user.setUserAddress(update.getUserAddress());
        if (update.getBirthday() != null)
            user.setBirthday(update.getBirthday());
        if (update.getDistrictId() != null) {
            CityDistrict city = cityDistrictRepository.findById(update.getDistrictId())
                    .orElseThrow(() -> new IllegalArgumentException("查無此區域"));
            user.setCityDistrict(city);
        }
        //  repository 將修改資料存進 DB
        return UserProfileResponse.from(userRepository.save(user));
    }

    // 修改密碼
    @Override
    public void changePassword(Integer userId, ChangePasswordRequest pw) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("查無此用戶"));

        // 已有本地密碼，必須先驗證舊密碼正確
        if (user.getPassword() != null) {
            if (pw.getOldPassword() == null
                    || !passwordEncoder.matches(pw.getOldPassword(), user.getPassword())) {
                throw new BadCredentialsException("舊密碼錯誤");
            }
        }

        // Google 帳號首次設定密碼：本來就沒有 oldPassword，直接 hash 新密碼存入
        user.setPassword(passwordEncoder.encode(pw.getNewPassword()));
        userRepository.save(user);
    }


    // 刪除資料
    @Override
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalStateException("查無此用戶");
        }
        userRepository.deleteById(userId);
    }

    // OAuth 2.0 註冊登入
    @Override
    public UserProfileResponse loginOrRegisterOAuth(OAuthUserInfo info) {
        return null;
    }

}

