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
import java.util.List;

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
        List<Farmer> farmers = farmerRepository.findAll();
        List<FarmerProfileResponse> result = new ArrayList<>();
        for (Farmer f : farmers) {
            result.add(toAdminResponse(f));
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

    // 停權：只有「啟用中(ACTIVE)」才能停權
    @Override
    public FarmerProfileResponse suspend(Integer farmerId) {
        Farmer farmer = findFarmer(farmerId);
        if (farmer.getFarmerStatus() != Farmer.FarmerStatus.ACTIVE) {
            throw new IllegalStateException("只有啟用中(ACTIVE)的小農才能停權");
        }
        farmer.setFarmerStatus(Farmer.FarmerStatus.SUSPENDED);
        return toAdminResponse(farmerRepository.save(farmer));
    }

    // 恢復：只有「已停權(SUSPENDED)」才能恢復
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

    // 包成管理員端 DTO：與小農端不同，管理員要看到真實審核狀態，
    // 故覆蓋掉 from() 內「REVIEWING 對外顯示成 PENDING」的遮蔽
    private FarmerProfileResponse toAdminResponse(Farmer farmer) {
        FarmerReview latest = latestReview(farmer.getFarmerId());
        FarmerProfileResponse dto = FarmerProfileResponse.from(farmer, latest);
        if (latest != null && latest.getReviewStatus() != null) {
            dto.setReviewStatus(latest.getReviewStatus().name());
        }
        return dto;
    }
}
