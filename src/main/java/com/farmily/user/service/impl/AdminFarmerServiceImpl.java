package com.farmily.user.service.impl;

import com.farmily.user.dto.FarmerProfileResponse;
import com.farmily.user.model.Farmer;
import com.farmily.user.model.FarmerReview;
import com.farmily.user.repository.FarmerRepository;
import com.farmily.user.repository.FarmerReviewRepository;
import com.farmily.user.service.AdminFarmerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminFarmerServiceImpl implements AdminFarmerService {

    private final FarmerRepository farmerRepository;
    private final FarmerReviewRepository farmerReviewRepository;

    public AdminFarmerServiceImpl(FarmerRepository farmerRepository,
                                  FarmerReviewRepository farmerReviewRepository) {
        this.farmerRepository = farmerRepository;
        this.farmerReviewRepository = farmerReviewRepository;
    }

    // 列出所有小農（帶上每位小農的最新審核狀態）
    @Override
    @Transactional(readOnly = true)
    public List<FarmerProfileResponse> listAll() {
        // step1. 撈出所有小農
        List<Farmer> farmers = farmerRepository.findAll();

        // step2. findAllReviewStatusRounds() 回傳每一列是 Object[]，固定 3 欄
        List<Object[]> rows = farmerReviewRepository.findAllReviewStatusRounds();

        // step3. 2 個 map 分別紀錄: 最新輪次的「狀態」和「輪次數字」，key 都是 farmerId
        Map<Integer, String> latestStatusByFarmerId = new HashMap<>();
        Map<Integer, Integer> latestRoundByFarmerId = new HashMap<>();
        for (Object[] row : rows) {
            // 取出這一列的 3 個欄位 (從 Object 轉回原本型別)
            Integer farmerId = (Integer) row[0];
            String status = (String) row[1];
            Integer round = (Integer) row[2];

            // 小農目前記錄到的最新輪次是多少
            Integer currentRound = latestRoundByFarmerId.get(farmerId);
            if (currentRound == null || round > currentRound) {
                latestRoundByFarmerId.put(farmerId, round);
                latestStatusByFarmerId.put(farmerId, status);
            }
        }

        // step4. 最新審核狀態/輪次從 step3 map 用記憶體查拿，不再回資料庫 (省下 N 次查詢)
        List<FarmerProfileResponse> result = new ArrayList<>();
        for (Farmer f : farmers) {
            String latestStatus = latestStatusByFarmerId.get(f.getFarmerId());
            Integer latestRound = latestRoundByFarmerId.get(f.getFarmerId());
            result.add(toAdminResponse(f, latestStatus, latestRound));
        }
        return result;
    }



    // 查單一小農
    @Override
    @Transactional(readOnly = true)
    public FarmerProfileResponse getById(Integer farmerId) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("查無此小農"));
        return toAdminResponse(farmer);
    }

    // 停權：只有啟用中(ACTIVE)才能停權
    @Override
    public FarmerProfileResponse suspend(Integer farmerId) {
        Farmer farmer = findFarmer(farmerId);
        if (farmer.getFarmerStatus() != Farmer.FarmerStatus.ACTIVE) {
            throw new IllegalStateException("只有啟用中(ACTIVE)的小農才能停權");
        }
        farmer.setFarmerStatus(Farmer.FarmerStatus.SUSPENDED);
        return toAdminResponse(farmerRepository.save(farmer));
    }

    // 恢復：只有已停權(SUSPENDED)才能恢復
    @Override
    public FarmerProfileResponse reinstate(Integer farmerId) {
        Farmer farmer = findFarmer(farmerId);
        if (farmer.getFarmerStatus() != Farmer.FarmerStatus.SUSPENDED) {
            throw new IllegalStateException("只有已停權(SUSPENDED)的小農才能恢復");
        }
        farmer.setFarmerStatus(Farmer.FarmerStatus.ACTIVE);
        return toAdminResponse(farmerRepository.save(farmer));
    }

    // ====== 自訂方法工具 ======
    private Farmer findFarmer(Integer farmerId) {
        return farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("查無此小農"));
    }

    // 取某小農最新一輪審核（可能為 null：理論上每位小農至少有一筆）
    private FarmerReview latestReview(Integer farmerId) {
        return farmerReviewRepository.findTopByFarmer_FarmerIdOrderByReviewRoundDesc(farmerId);
    }

    // 單筆版：自己查最新審核，把值取出後委派給下面的組裝方法（不重複組裝邏輯）
    // 用於 getById / suspend / reinstate，單筆查 1 次 DB，沒有 N+1 問題
    private FarmerProfileResponse toAdminResponse(Farmer farmer) {
        FarmerReview latest = latestReview(farmer.getFarmerId());
        if (latest == null) {
            return toAdminResponse(farmer, null, null);
        }
        // enum 轉成字串（可能為 null）
        String status = latest.getReviewStatus() != null ? latest.getReviewStatus().name() : null;
        return toAdminResponse(farmer, status, latest.getReviewRound());
    }

    // 唯一的組裝方法：把小農 + 它最新一輪的審核狀態/輪次，組裝成管理員端 DTO
    // 審核狀態/輪次都由呼叫端先準備好再傳進來，這裡不查 DB
    private FarmerProfileResponse toAdminResponse(Farmer farmer, String reviewStatus, Integer reviewRound) {

        // 先用 from(farmer, null) 填好小農基本資料；傳 null 代表審核欄位這裡先不填，等下面手動補
        FarmerProfileResponse dto = FarmerProfileResponse.from(farmer, null);
        if (reviewStatus != null) {
            // 管理員端要看真實狀態 (含 REVIEWING)
            dto.setReviewStatus(reviewStatus);
            dto.setReviewRound(reviewRound);
        }
        return dto;
    }
}
