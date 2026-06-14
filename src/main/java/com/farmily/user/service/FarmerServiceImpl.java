package com.farmily.user.service;

import com.farmily.user.dto.*;
import com.farmily.user.model.CityDistrict;
import com.farmily.user.model.Farmer;
import com.farmily.user.model.FarmerReview;
import com.farmily.user.repository.CityDistrictRepository;
import com.farmily.user.repository.FarmerRepository;
import com.farmily.user.repository.FarmerReviewRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
public class FarmerServiceImpl implements FarmerService{

    private final FarmerRepository farmerRepository;
    private final FarmerReviewRepository farmerReviewRepository;
    private final CityDistrictRepository cityDistrictRepository;
    private final EmailUniquenessChecker emailUniquenessChecker;
    private final PasswordEncoder passwordEncoder;


    public FarmerServiceImpl(FarmerRepository farmerRepository,
                             FarmerReviewRepository farmerReviewRepository,
                             CityDistrictRepository cityDistrictRepository,
                             EmailUniquenessChecker emailUniquenessChecker,
                             PasswordEncoder passwordEncoder) {
        this.farmerRepository = farmerRepository;
        this.farmerReviewRepository = farmerReviewRepository;
        this.cityDistrictRepository = cityDistrictRepository;
        this.emailUniquenessChecker = emailUniquenessChecker;
        this.passwordEncoder = passwordEncoder;
    }

    // 本地註冊
    @Override
    public FarmerProfileResponse register(FarmerRegisterRequest reg) {

        // step1: 跨三張表檢查 email 是否已被使用
        emailUniquenessChecker.emailAvailable(reg.getEmail());

        // step2: email 不存在，走本地註冊流程
        Farmer newFarmer = new Farmer();
        newFarmer.setEmail(reg.getEmail());

        // hash 原始密碼
        String hashedPassword = passwordEncoder.encode(reg.getPassword());
        newFarmer.setPassword(hashedPassword);

        newFarmer.setFarmName(reg.getFarmName());
        newFarmer.setFarmAddress(reg.getFarmAddress());
        newFarmer.setFarmDesc(reg.getFarmDesc());
        newFarmer.setFarmerPhoneNum(reg.getFarmerPhoneNum());
        newFarmer.setLocLat(reg.getLocLat());
        newFarmer.setLocLong(reg.getLocLong());

        // 抓 city 物件前先判斷
        if (reg.getDistrictId() != null) {
            CityDistrict city = cityDistrictRepository.findById(reg.getDistrictId())
                    .orElseThrow(() -> new IllegalArgumentException("查無此區域 districtId=" + reg.getDistrictId()));
            newFarmer.setCityDistrict(city);
        }

        newFarmer.setFarmerStatus(Farmer.FarmerStatus.PENDING);
        newFarmer.setFarmerCreatedAt(LocalDateTime.now());
        newFarmer.setUploadedAt(LocalDateTime.now());

        // round 1 也存提交快照，讓每筆審核都是完整紀錄
        FarmerReview review = newReviewSnapshot(
                newFarmer, (byte) 1,
                reg.getFarmName(), reg.getFarmAddress(), district, reg.getLocLat(), reg.getLocLong(),
                reg.getCertFileLand(), reg.getCertFileProduct(), reg.getCertFileIdentity()
        );

        FarmerReview savedReview = farmerReviewRepository.save(review);

        return FarmerProfileResponse.from(newFarmer, savedReview);
    }




    // 本地登入
    @Override
    @Transactional(readOnly = true)
    public FarmerProfileResponse login(LoginRequest log) {
        Farmer farmer = farmerRepository.findByEmail(log.getEmail())
                .orElseThrow(() -> new BadCredentialsException("帳號或密碼錯誤"));

        // 檢查 hash 密碼是否相等
        if(!passwordEncoder.matches(log.getPassword(), farmer.getPassword())){
            throw new BadCredentialsException("帳號或密碼錯誤");
        }

        if (farmer.getFarmerStatus() == Farmer.FarmerStatus.PENDING) {
            throw new IllegalStateException("您的小農申請審核中，通過後才能登入");

        if(farmer.getFarmerStatus() == Farmer.FarmerStatus.SUSPENDED){
            throw new IllegalStateException("此帳號已停用");
        }

        return FarmerProfileResponse.from(farmer);
    }

    // 查個人資料
    @Override
    public FarmerProfileResponse getMyProfile(Integer farmerId) {
        return null;
    }

    // 修改不用重審資料
    @Override
    public FarmerProfileResponse updateContactInfo(Integer farmerId, FarmerProfileUpdateRequest req) {
        return null;
    }

    // 修改需要重審資料
    @Override
    public FarmerProfileResponse resubmitApplication(Integer farmerId, FarmerResubmitRequest req) {
        return null;
    }

    // 修改密碼
    @Override
    public void changePassword(Integer farmerId, ChangePasswordRequest pw) {

    }
}
