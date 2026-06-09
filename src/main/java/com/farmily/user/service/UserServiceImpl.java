package com.farmily.user.service;

import com.farmily.user.dto.*;
import com.farmily.user.model.CityDistrict;
import com.farmily.user.model.User;
import com.farmily.user.repository.CityDistrictRepository;
import com.farmily.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final CityDistrictRepository cityDistrictRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           CityDistrictRepository cityDistrictRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.cityDistrictRepository = cityDistrictRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 註冊流程
    @Override
    public UserProfileResponse register(UserRegisterRequest reg) {

        // step1: 先檢查是否存在相同會員帳號 (email)
        User existingUser = userRepository.findByEmail(reg.getEmail()).orElse(null);    //回傳 Optional

        // 會員存在
        if (existingUser != null) {

            // 狀況 A: 帳號「沒有本地密碼」，代表他只有第三方登入資訊
            if (existingUser.getPassword() == null) {
                User.AuthProvider provider = existingUser.getAuthProvider();
                if (provider == User.AuthProvider.GOOGLE) {
                    throw new IllegalStateException("此帳號已使用 Google 登入，請改用 Google 登入");
                }
            }
            // 狀況 B: 帳號有本地密碼，或者不屬於上述第三方登入，就是一般重複註冊
            throw new IllegalStateException("帳號已註冊，請直接登入");
        }
        // step2: 會員不存在，走本地註冊流程
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
    public UserProfileResponse login(UserLoginRequest log) {

        User user = userRepository.findByEmail(log.getEmail())
                .orElseThrow(() -> new BadCredentialsException("帳號或密碼錯誤"));

        // 檢查 hash 密碼是否相等
        if (!passwordEncoder.matches(log.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("帳號或密碼錯誤");
        }

        // 純 Google 帳號沒有本地密碼
        if (user.getPassword() == null) {
            throw new IllegalStateException("此帳號為第三方登入,請改用 Google 登入");
        }

        if (user.getUserStatus() == User.UserStatus.SUSPENDED
                || user.getUserStatus() == User.UserStatus.DELETED) {
            throw new IllegalStateException("此帳號已停用");
        }
        return UserProfileResponse.from(user);
    }

    @Override
    public UserProfileResponse loginOrRegisterOAuth(OAuthUserInfo info) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Integer userId) {
        return null;
    }

    @Override
    public UserProfileResponse updateMyProfile(Integer userId, UserUpdateRequest update) {
        return null;
    }

    @Override
    public void changePassword(Integer userId, ChangePasswordRequest pw) {

    }
}
