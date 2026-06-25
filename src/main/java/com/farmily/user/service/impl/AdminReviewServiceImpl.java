package com.farmily.user.service.impl;

import com.farmily.user.dto.FarmerReviewResponse;
import com.farmily.user.model.Admin;
import com.farmily.user.model.Farmer;
import com.farmily.user.model.FarmerReview;
import com.farmily.user.repository.AdminRepository;
import com.farmily.user.repository.FarmerRepository;
import com.farmily.user.repository.FarmerReviewRepository;
import com.farmily.user.service.AdminReviewService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AdminReviewServiceImpl implements AdminReviewService {

    private final FarmerRepository farmerRepository;
    private final AdminRepository adminRepository;
    private final FarmerReviewRepository farmerReviewRepository;

    public AdminReviewServiceImpl(FarmerRepository farmerRepository, AdminRepository adminRepository, FarmerReviewRepository farmerReviewRepository) {
        this.farmerRepository = farmerRepository;
        this.adminRepository = adminRepository;
        this.farmerReviewRepository = farmerReviewRepository;
    }

    // 待審清單
    @Override
    @Transactional(readOnly = true)
    public List<FarmerReviewResponse> listPending() {
        // 撈出所有待審 (PENDING)
        List<FarmerReview> reviews = farmerReviewRepository.findByReviewStatusOrderBySubmittedAtAsc(FarmerReview.ReviewStatus.PENDING);

        // 準備一個空清單，逐筆把 FarmerReview 轉成回應用的 FarmerReviewResponse
        List<FarmerReviewResponse> result = new ArrayList<>();
        for (FarmerReview review : reviews) {
            result.add(FarmerReviewResponse.from(review)); // 轉成 DTO 後加進結果清單
        }
        return result;
    }

    // 某小農的審核紀錄
    @Override
    public List<FarmerReviewResponse> listByFarmer(Integer farmerId) {
        List<FarmerReview> reviews = farmerReviewRepository.findByFarmer_FarmerIdOrderByReviewRoundDesc(farmerId);

        List<FarmerReviewResponse> result = new ArrayList<>();
        for (FarmerReview review : reviews) {
            result.add(FarmerReviewResponse.from(review));
        }
        return result;
    }

    // 列出審核中清單
    @Override
    public List<FarmerReviewResponse> listReviewing() {
        List<FarmerReview> reviews = farmerReviewRepository.findByReviewStatusOrderBySubmittedAtAsc(FarmerReview.ReviewStatus.REVIEWING);

        List<FarmerReviewResponse> result = new ArrayList<>();
        for(FarmerReview review : reviews){
            result.add(FarmerReviewResponse.from(review));
        }
        return result;
    }

    // 開始審核 PENDING 案件
    @Override
    public FarmerReviewResponse reviewing(Integer reviewId, Integer adminId) {
        FarmerReview review = findReview(reviewId);
        if(review.getReviewStatus() != FarmerReview.ReviewStatus.PENDING){
            throw new IllegalStateException("此案件審核中或已審核");
        }
        review.setReviewStatus(FarmerReview.ReviewStatus.REVIEWING);
        review.setAdmin(findAdmin(adminId));

        return FarmerReviewResponse.from(farmerReviewRepository.save(review));
    }

    // 核准過審：把這一輪 submitted_XXX 寫回 Farmer，並啟用帳號
    @Override
    public FarmerReviewResponse approve(Integer reviewId, Integer adminId) {
        FarmerReview review = findReview(reviewId);
        ensureReviewing(review);       // 若 APPROVED/REJECTED 就擋下

        if (review.getAdmin() == null || !review.getAdmin().getAdminId().equals(adminId)) {
            throw new AccessDeniedException("此案件由其他管理員認領，您無法審核");
        }

        // 更新到 Farmer 核准的資料
        Farmer farmer = review.getFarmer();
        farmer.setFarmName(review.getSubmittedFarmName());
        farmer.setFarmAddress(review.getSubmittedFarmAddress());
        farmer.setCityDistrict(review.getSubmittedDistrict());
        farmer.setLocLat(review.getSubmittedLocLat());
        farmer.setLocLong(review.getSubmittedLocLong());
        farmer.setFarmerStatus(Farmer.FarmerStatus.ACTIVE);   // 啟用之後能登入
        farmerRepository.save(farmer);

        // 同步更新 FarmerReview 核准的資料
        review.setReviewStatus(FarmerReview.ReviewStatus.APPROVED);
        review.setReviewedAt(LocalDateTime.now());
        review.setAdmin(findAdmin(adminId));                  // 記錄是誰審的

        return FarmerReviewResponse.from(farmerReviewRepository.save(review));
    }

    // 退件重審
    @Override
    public FarmerReviewResponse reject(Integer reviewId, Integer adminId, String rejectReason) {
        FarmerReview review = findReview(reviewId);
        ensureReviewing(review);
        if (review.getAdmin() == null || !review.getAdmin().getAdminId().equals(adminId)) {
            throw new AccessDeniedException("此案件由其他管理員認領，您無法審核");
        }

        // 同步更新 FarmerReview 拒絕的資料
        review.setReviewStatus(FarmerReview.ReviewStatus.REJECTED);
        review.setReviewedAt(LocalDateTime.now());
        review.setRejectReason(rejectReason);
        review.setAdmin(findAdmin(adminId));

        return FarmerReviewResponse.from(farmerReviewRepository.save(review));
    }


    // ---- 自訂方法 ----
    private FarmerReview findReview(Integer reviewId) {
        return farmerReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("查無此審核案件"));
    }
    private Admin findAdmin(Integer adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("查無此管理員"));
    }

    // 確保一定要先 REVIEWING (除了 REVIEWING，擋住其他審核狀態)
    private void ensureReviewing(FarmerReview review) {
        FarmerReview.ReviewStatus s = review.getReviewStatus();
        if (s != FarmerReview.ReviewStatus.REVIEWING) {
            throw new IllegalStateException("請先認領案件才能進行審核");
        }
    }
}
