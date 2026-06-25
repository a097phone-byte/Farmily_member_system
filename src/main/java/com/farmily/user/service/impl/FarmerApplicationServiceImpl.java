package com.farmily.user.service.impl;

import com.farmily.user.dto.FarmerReviewResponse;
import com.farmily.user.dto.LoginRequest;
import com.farmily.user.dto.PublicFarmerResubmitRequest;
import com.farmily.user.model.CityDistrict;
import com.farmily.user.model.Farmer;
import com.farmily.user.model.FarmerReview;
import com.farmily.user.repository.CityDistrictRepository;
import com.farmily.user.repository.FarmerRepository;
import com.farmily.user.repository.FarmerReviewRepository;
import com.farmily.user.service.FarmerApplicationService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// 讓狀態尚未啟用小農 (狀態 PENDING) 可以查進度
@Service
@Transactional
public class FarmerApplicationServiceImpl implements FarmerApplicationService {

    private final FarmerRepository farmerRepository;
    private final FarmerReviewRepository farmerReviewRepository;
    private final CityDistrictRepository cityDistrictRepository;
    private final PasswordEncoder passwordEncoder;

    public FarmerApplicationServiceImpl(FarmerRepository farmerRepository, FarmerReviewRepository farmerReviewRepository, CityDistrictRepository cityDistrictRepository, PasswordEncoder passwordEncoder) {
        this.farmerRepository = farmerRepository;
        this.farmerReviewRepository = farmerReviewRepository;
        this.cityDistrictRepository = cityDistrictRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 查自己最新一筆審核結果 (狀態 + 退件理由)
    @Override
    @Transactional(readOnly = true)
    public FarmerReviewResponse checkStatus(LoginRequest log) {
        Farmer farmer = authByCredentials(log.getEmail(), log.getPassword());
        FarmerReview latest = farmerReviewRepository.findTopByFarmer_FarmerIdOrderByReviewRoundDesc(farmer.getFarmerId());
        if(latest == null) {
            throw new IllegalArgumentException("查無審核紀錄");
        }
        return FarmerReviewResponse.from(latest);
    }

    // 重新送審（只允許小農狀態 PENDING + 審核狀態為 REJECTED 才能重新送審
    @Override
    public FarmerReviewResponse resubmit(PublicFarmerResubmitRequest req) {

        Farmer farmer = authByCredentials(req.getEmail(), req.getPassword());

        // 查最新審核
        FarmerReview latest = farmerReviewRepository.findTopByFarmer_FarmerIdOrderByReviewRoundDesc(farmer.getFarmerId());

        if (farmer.getFarmerStatus() != Farmer.FarmerStatus.PENDING) {
            throw new IllegalStateException("此帳號已通過審核，請登入後操作");
        }

        // 僅最新一輪審核狀態為 REJECTED 才能重新送審；審核中 PENDING/REVIEWING 一律擋下
        if (latest == null || latest.getReviewStatus() != FarmerReview.ReviewStatus.REJECTED) {
            throw new IllegalStateException("尚在審核中，需待退件後才能重新送審");
        }

        // 計算重審次數
        int nextRound = (latest != null && latest.getReviewRound() != null)
                ? latest.getReviewRound() + 1
                : 1;

        // 重新送審 = 重新寫入 FarmerReview
        FarmerReview review = new FarmerReview();
        review.setFarmer(farmer);
        review.setReviewStatus(FarmerReview.ReviewStatus.PENDING);
        review.setReviewRound(nextRound);
        review.setSubmittedAt(LocalDateTime.now());
        review.setSubmittedFarmName(req.getFarmName());
        review.setSubmittedFarmAddress(req.getFarmAddress());
        review.setSubmittedDistrict(findDistrict(req.getDistrictId()));
        review.setSubmittedLocLat(req.getLocLat());
        review.setSubmittedLocLong(req.getLocLong());
        review.setCertFileLand(req.getCertFileLand());
        review.setCertFileProduct(req.getCertFileProduct());
        review.setCertFileIdentity(req.getCertFileIdentity());

        return FarmerReviewResponse.from(farmerReviewRepository.save(review));
    }


    // 自定義方法: 用 email + password 驗證身分（取代登入入口與 session）
    private Farmer authByCredentials(String email, String password) {
        Farmer farmer = farmerRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("帳號或密碼錯誤"));

        if (farmer.getPassword() == null
                || !passwordEncoder.matches(password, farmer.getPassword())) {
            throw new BadCredentialsException("帳號或密碼錯誤");
        }
        return farmer;
    }

    // 自定義方法: 依 id 撈區域
    private CityDistrict findDistrict(Integer districtId) {
        if (districtId == null) {
            return null;
        }
        return cityDistrictRepository.findById(districtId)
                .orElseThrow(() -> new IllegalArgumentException("查無此區域 districtId=" + districtId));
    }

}
