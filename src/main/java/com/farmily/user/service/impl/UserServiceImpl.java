package com.farmily.user.service.impl;

import com.farmily.user.dto.*;
import com.farmily.user.model.AccountToken;
import com.farmily.user.model.CityDistrict;
import com.farmily.user.model.User;
import com.farmily.user.repository.CityDistrictRepository;
import com.farmily.user.repository.SpendingTierRepository;
import com.farmily.user.repository.UserRepository;

import com.farmily.user.service.EmailUniquenessChecker;
import com.farmily.user.service.EmailVerificationService;
import com.farmily.user.service.UserService;
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
    private final SpendingTierRepository spendingTierRepository;
    private final EmailVerificationService emailVerificationService;

    public UserServiceImpl(UserRepository userRepository,
                           CityDistrictRepository cityDistrictRepository,
                           PasswordEncoder passwordEncoder,
                           EmailUniquenessChecker emailUniquenessChecker,
                           SpendingTierRepository spendingTierRepository,
                           EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.cityDistrictRepository = cityDistrictRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailUniquenessChecker = emailUniquenessChecker;
        this.spendingTierRepository = spendingTierRepository;
        this.emailVerificationService = emailVerificationService;
    }

    // 本地註冊流程
    @Override
    public UserProfileResponse register(UserRegisterRequest reg) {

        // step1: 先檢查是否存在相同會員帳號 (email)
        User existingUser = userRepository.findByEmail(reg.getEmail()).orElse(null);    //回傳 Optional

        // 會員帳號 (email) 存在
        if (existingUser != null) {

            // 狀況 A: 帳號「沒有本地密碼」，代表他只有第三方登入資訊
            if (existingUser.getPassword() == null
                    && existingUser.getAuthProvider() == User.AuthProvider.GOOGLE) {
                throw new IllegalStateException("此帳號已使用 Google 登入，請改用 Google 登入");
            }
            // 狀況 B: 剩餘(已有本地密碼)就是一般重複註冊
            throw new IllegalStateException("帳號已註冊使用");
        }

        // step2: 若 email = null，跨表檢查 email 全域唯一
        emailUniquenessChecker.emailAvailable(reg.getEmail());

        // step3: 會員帳號 (email) 不存在，走本地註冊流程
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

        // 存進 DB
        User savedUser = userRepository.save(newUser);

        // 寄出 Email 驗證信
        emailVerificationService.sendVerification(
                savedUser.getEmail(), AccountToken.AccountType.MEMBER);

        // 包裝會員資料成 dto 給 Controller
        return UserProfileResponse.from(savedUser);
    }

    // 本地登入流程
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse login(LoginRequest log) {
        User user = userRepository.findByEmail(log.getEmail())
                .orElseThrow(() -> new BadCredentialsException("帳號或密碼錯誤"));

        // 純 Google 帳號沒有本地密碼，null 檢查必須在 matches() 之前
        // LoginRequest 有密碼非空值驗證，可移除
//        if (user.getPassword() == null) {
//            throw new IllegalStateException("此帳號為第三方登入，請改用 Google 登入");
//        }

        // 檢查 hash 密碼是否相等
        if (!passwordEncoder.matches(log.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("帳號或密碼錯誤");
        }

        if (user.getUserStatus() == User.UserStatus.SUSPENDED
                || user.getUserStatus() == User.UserStatus.DELETED) {
            throw new IllegalStateException("此帳號已遭停權或終止，有任何疑問請聯繫客服");
        }

        // 本地帳號必須先完成 Email 驗證（點驗證信連結）才能登入；Google OAuth 除外
        if (user.getAuthProvider() == User.AuthProvider.LOCAL
                && (user.getEmailVerified() == null || !user.getEmailVerified())) {
            throw new IllegalStateException("請先完成 Email 驗證後再登入，可至信箱點擊驗證連結");
        }
        return UserProfileResponse.from(user);
    }

    // 查資料
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("查無此用戶"));

        // +消費級距 (不同表)
        Integer amount = user.getMonthlySpending() != null ? user.getMonthlySpending() : 0;
        String tierName = spendingTierRepository.findTierNameByAmount(amount);

        return UserProfileResponse.from(user, tierName);
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
        //  將修改資料存進 DB
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

        // Google 帳號首次設定本地密碼和本地帳號新密碼一樣：本來就沒有 oldPassword，直接 hash 新密碼存入
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
        // step1. 先用 Google 驗證可信 info 的 providerId 找會員
        User user = userRepository.findByProviderId(info.getProviderId()).orElse(null);

        // step2. 用 providerId 找不到，改用 email 找
        if(user == null){
            user = userRepository.findByEmail(info.getEmail()).orElse(null);

            // 若 email 存在
            if(user != null){
                // 則此會員有本地帳號+密碼，多榜定 Google 回傳的 provider_id
                user.setProviderId(info.getProviderId());

                // 有本地帳號一定有密碼
//                if(user.getPassword() == null){
//                    user.setAuthProvider(User.AuthProvider.GOOGLE);
//                }
            }
        }
        // step3. id、email 都找不到，進入註冊會員流程
        if(user == null) {
            user = new User();
            user.setEmail(info.getEmail());
            user.setUserName(info.getName());
            user.setPassword(null);
            user.setAuthProvider(User.AuthProvider.GOOGLE);
            user.setProviderId(info.getProviderId());
            user.setEmailVerified(true);
            user.setUserCreatedAt(LocalDateTime.now());
            user.setMonthlySpending(0);
            user.setUserStatus(User.UserStatus.ACTIVE);
        }

        // step4. 限制被停權、註銷帳號不能登入
        if (user.getUserStatus() == User.UserStatus.SUSPENDED
                || user.getUserStatus() == User.UserStatus.DELETED) {
            throw new IllegalStateException("此帳號已遭停權或終止，有任何疑問請聯繫客服");
        }

        // 包裝會員資料成 dto 給 Controller
        return UserProfileResponse.from(userRepository.save(user));
    }
}

