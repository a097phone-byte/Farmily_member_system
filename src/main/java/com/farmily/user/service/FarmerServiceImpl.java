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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class FarmerServiceImpl implements FarmerService{

    private final FarmerRepository farmerRepository;
    private final FarmerReviewRepository farmerReviewRepository;
    private final CityDistrictRepository cityDistrictRepository;
    private final EmailUniquenessChecker emailUniquenessChecker;
    private final PasswordEncoder passwordEncoder;


    public FarmerServiceImpl(FarmerRepository farmerRepository, FarmerReviewRepository farmerReviewRepository,
                             CityDistrictRepository cityDistrictRepository, EmailUniquenessChecker emailUniquenessChecker,
                             PasswordEncoder passwordEncoder) {
        this.farmerRepository = farmerRepository;
        this.farmerReviewRepository = farmerReviewRepository;
        this.cityDistrictRepository = cityDistrictRepository;
        this.emailUniquenessChecker = emailUniquenessChecker;
        this.passwordEncoder = passwordEncoder;
    }

    // 小農註冊申請
    @Override
    public FarmerProfileResponse register(FarmerRegisterRequest reg) {

        CityDistrict district = findDistrict(reg.getDistrictId());

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
        newFarmer.setCityDistrict(district);
        newFarmer.setFarmerStatus(Farmer.FarmerStatus.PENDING);
        newFarmer.setFarmerCreatedAt(LocalDateTime.now());
        newFarmer.setUploadedAt(LocalDateTime.now());

        // 必須先存 farmer 拿到 id，review 的 farmer_id 外鍵才有對象可指
        Farmer savedFarmer = farmerRepository.save(newFarmer);

        // round 1 也存快取
        FarmerReview review = newReviewSnapshot(
                savedFarmer, 1,
                reg.getFarmName(), reg.getFarmAddress(),
                district, reg.getLocLat(), reg.getLocLong(),
                reg.getCertFileLand(), reg.getCertFileProduct(), reg.getCertFileIdentity());
        FarmerReview savedReview = farmerReviewRepository.save(review);

        return FarmerProfileResponse.from(savedFarmer, savedReview);
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
        }
        if(farmer.getFarmerStatus() == Farmer.FarmerStatus.SUSPENDED){
            throw new IllegalStateException("此帳號已停用");
        }
        // 內含查最新 review
        return toResponse(farmer);
    }

    // 查資料
    @Override
    @Transactional(readOnly = true)
    public FarmerProfileResponse getMyProfile(Integer farmerId) {
        return toResponse(findFarmer(farmerId));
    }

    // 修改資料 (非重審)
    @Override
    public FarmerProfileResponse updateContactInfo(Integer farmerId, FarmerProfileUpdateRequest req) {
        Farmer farmer = findFarmer(farmerId);

        if(req.getFarmerPhoneNum() != null)
            farmer.setFarmerPhoneNum(req.getFarmerPhoneNum());
        if(req.getFarmDesc() != null)
            farmer.setFarmDesc(req.getFarmDesc());

        // repository 將修改資料存進 DB
        return toResponse(farmerRepository.save(farmer));
    }

    // 修改資料 (需重審)
    @Override
    public FarmerProfileResponse resubmit(Integer farmerId, FarmerResubmitRequest req) {
        // 撈出小農和審核物件
        Farmer farmer = findFarmer(farmerId);
        FarmerReview latest = farmerReviewRepository.findTopByFarmer_FarmerIdOrderByReviewRoundDesc(farmerId);

        // 重審次數
        int nextRound = (latest != null && latest.getReviewRound() != null)
                        ? latest.getReviewRound() + 1
                        : 1;

        // 呼叫工廠方法 newReviewSnapshot()，做一個 FarmerReview 物件
        FarmerReview review = newReviewSnapshot(
                farmer, nextRound,
                req.getFarmName(), req.getFarmAddress(),
                findDistrict(req.getDistrictId()),
                req.getLocLat(), req.getLocLong(),
                req.getCertFileLand(), req.getCertFileProduct(),
                req.getCertFileIdentity()
        );

        // 把 FarmerReview 物件存進 DB，回傳帶上 farmerId 物件
        FarmerReview savedReview = farmerReviewRepository.save(review);
        return FarmerProfileResponse.from(farmer, savedReview);
    }


    // 修改密碼
    @Override
    public void changePassword(Integer farmerId, ChangePasswordRequest pw) {
        Farmer farmer = findFarmer(farmerId);

        if (farmer.getPassword() != null) {
            if (pw.getOldPassword() == null
                    || !passwordEncoder.matches(pw.getOldPassword(), farmer.getPassword())) {
                throw new BadCredentialsException("舊密碼錯誤");
            }
        }
        farmer.setPassword(passwordEncoder.encode(pw.getNewPassword()));
        farmerRepository.save(farmer);
    }

    // 自訂工廠方法：組裝每次審核 (n+1) 的資料，回傳一個物件（register/resubmit 共用）
    private FarmerReview newReviewSnapshot(
            Farmer farmer, int round,
            String farmName, String farmAddress, CityDistrict district,
            BigDecimal locLat, BigDecimal locLong,
            byte[] certLand, byte[] certProduct, byte[] certIdentity) {

        FarmerReview review = new FarmerReview();
        review.setFarmer(farmer);
        review.setReviewStatus(FarmerReview.ReviewStatus.PENDING);
        review.setReviewRound(round);
        review.setSubmittedAt(LocalDateTime.now());
        review.setSubmittedFarmName(farmName);
        review.setSubmittedFarmAddress(farmAddress);
        review.setSubmittedDistrict(district);
        review.setSubmittedLocLat(locLat);
        review.setSubmittedLocLong(locLong);
        review.setCertFileLand(certLand);
        review.setCertFileProduct(certProduct);
        review.setCertFileIdentity(certIdentity);
        return review;
    }

    // 查最新 review，連同 farmer 包成回應 DTO
    private FarmerProfileResponse toResponse(Farmer farmer) {
        FarmerReview latest = farmerReviewRepository.findTopByFarmer_FarmerIdOrderByReviewRoundDesc(farmer.getFarmerId());
        return FarmerProfileResponse.from(farmer, latest);
    }

    // 依 id 撈小農
    private Farmer findFarmer(Integer farmerId) {
        return farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("查無此小農"));
    }

    // 依 id 撈區域
    private CityDistrict findDistrict(Integer districtId) {
        if (districtId == null) {
            return null;
        }
        return cityDistrictRepository.findById(districtId)
                .orElseThrow(() -> new IllegalArgumentException("查無此區域 districtId=" + districtId));
    }
}
