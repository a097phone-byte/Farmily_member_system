package com.farmily.user.dto;

import com.farmily.user.model.CityDistrict;

// 回傳給前端做下拉選單的資料（不直接外露 entity）
public class CityDistrictResponse {

    private Integer districtId;
    private String  cityName;
    private String  distName;
    private Short   zipcode;

    // getter（setter 省略）
    public Integer getDistrictId() { return districtId; }
    public String  getCityName()   { return cityName; }
    public String  getDistName()   { return distName; }
    public Short   getZipcode()    { return zipcode; }

    // 把 entity 轉成 DTO（跟你其他 DTO 一樣用 from()）
    public static CityDistrictResponse from(CityDistrict c) {
        CityDistrictResponse dto = new CityDistrictResponse();
        dto.districtId = c.getDistrictId();
        dto.cityName   = c.getCityName();
        dto.distName   = c.getDistName();
        dto.zipcode    = c.getZipcode();
        return dto;
    }
}