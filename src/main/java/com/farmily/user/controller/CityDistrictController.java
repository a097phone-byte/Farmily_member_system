package com.farmily.user.controller;

import com.farmily.user.dto.CityDistrictResponse;
import com.farmily.user.service.CityDistrictService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 提供前端「縣市/行政區」下拉選單資料（公開，不需登入）
@RestController
@RequestMapping("/api/city-districts")
public class CityDistrictController {

    private final CityDistrictService cityDistrictService;

    public CityDistrictController(CityDistrictService cityDistrictService) {
        this.cityDistrictService = cityDistrictService;
    }

    // GET /api/city-districts → 回傳全部
    @GetMapping
    public ResponseEntity<List<CityDistrictResponse>> listAll() {
        return ResponseEntity.ok(cityDistrictService.listAll());
    }
}