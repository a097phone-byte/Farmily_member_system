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
}
