package com.farmily.user.service;

import com.farmily.user.dto.CityDistrictResponse;
import java.util.List;

public interface CityDistrictService {
    // 回傳所有縣市/行政區
    List<CityDistrictResponse> listAll();
}