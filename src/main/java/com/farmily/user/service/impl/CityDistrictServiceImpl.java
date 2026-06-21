package com.farmily.user.service.impl;

import com.farmily.user.dto.CityDistrictResponse;
import com.farmily.user.model.CityDistrict;
import com.farmily.user.repository.CityDistrictRepository;
import com.farmily.user.service.CityDistrictService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CityDistrictServiceImpl implements CityDistrictService {

    private final CityDistrictRepository cityDistrictRepository;

    public CityDistrictServiceImpl(CityDistrictRepository cityDistrictRepository) {
        this.cityDistrictRepository = cityDistrictRepository;
    }

    @Override
    public List<CityDistrictResponse> listAll() {

        // findAll 撈全部，再一筆筆轉成 DTO
        List<CityDistrict> all = cityDistrictRepository.findAll();
        List<CityDistrictResponse> result = new ArrayList<>();
        for (CityDistrict c : all) {
            result.add(CityDistrictResponse.from(c));
        }
        return result;
    }
}