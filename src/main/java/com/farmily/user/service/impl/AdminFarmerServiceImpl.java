package com.farmily.user.service.impl;

import com.farmily.user.dto.FarmerProfileResponse;
import com.farmily.user.model.Farmer;
import com.farmily.user.repository.FarmerRepository;
import com.farmily.user.service.AdminFarmerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AdminFarmerServiceImpl implements AdminFarmerService {

    private final FarmerRepository farmerRepository;

    public AdminFarmerServiceImpl(FarmerRepository farmerRepository) {
        this.farmerRepository = farmerRepository;
    }

    // 列出所有小農
    @Override
    @Transactional(readOnly = true)
    public List<FarmerProfileResponse> listAll() {
        List<Farmer> farmers = farmerRepository.findAll();
        List<FarmerProfileResponse> result = new ArrayList<>();
        for (Farmer f : farmers) {
            result.add(FarmerProfileResponse.from(f, null));
        }
        return result;
    }

    // 查單一小農
    @Override
    @Transactional(readOnly = true)
    public FarmerProfileResponse getById(Integer farmerId) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("查無此小農"));
        return FarmerProfileResponse.from(farmer, null);
    }

    // 停權：只有「啟用中(ACTIVE)」才能停權
    @Override
    public FarmerProfileResponse suspend(Integer farmerId) {
        Farmer farmer = findFarmer(farmerId);
        if (farmer.getFarmerStatus() != Farmer.FarmerStatus.ACTIVE) {
            throw new IllegalStateException("只有啟用中(ACTIVE)的小農才能停權");
        }
        farmer.setFarmerStatus(Farmer.FarmerStatus.SUSPENDED);
        return FarmerProfileResponse.from(farmerRepository.save(farmer), null);
    }

    // 恢復：只有「已停權(SUSPENDED)」才能恢復
    @Override
    public FarmerProfileResponse reinstate(Integer farmerId) {
        Farmer farmer = findFarmer(farmerId);
        if (farmer.getFarmerStatus() != Farmer.FarmerStatus.SUSPENDED) {
            throw new IllegalStateException("只有已停權(SUSPENDED)的小農才能恢復");
        }
        farmer.setFarmerStatus(Farmer.FarmerStatus.ACTIVE);
        return FarmerProfileResponse.from(farmerRepository.save(farmer), null);
    }

    // ====== 自訂方法工具 ======
    private Farmer findFarmer(Integer farmerId) {
        return farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("查無此小農"));
    }
}
