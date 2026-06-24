package com.farmily.user.dto;

import com.farmily.user.model.Farmer;
import com.farmily.user.model.FarmerReview;

import java.math.BigDecimal;

// 將 Repository 回傳數據包裝成 dto
public class FarmerProfileResponse {

    private Integer farmerId;
    private String email;
    private String farmName;
    private String farmAddress;
    private Integer districtId;
    private String cityName;
    private String distName;
    private String farmerPhoneNum;
    private String farmDesc;
    private BigDecimal locLat;
    private BigDecimal locLong;
    private String farmerStatus;
    private String reviewStatus;
    private Integer reviewRound;
    private Boolean hasPassword;

    // getter
    public Integer getFarmerId() {
        return farmerId;
    }
    public String getEmail() {
        return email;
    }
    public String getFarmName() {
        return farmName;
    }
    public String getFarmAddress() {
        return farmAddress;
    }
    public Integer getDistrictId() {
        return districtId;
    }
    public String getCityName() {
        return cityName;
    }
    public String getDistName() {
        return distName;
    }
    public String getFarmerPhoneNum() {
        return farmerPhoneNum;
    }
    public String getFarmDesc() {
        return farmDesc;
    }
    public BigDecimal getLocLat() {
        return locLat;
    }
    public BigDecimal getLocLong() {
        return locLong;
    }
    public String getFarmerStatus() {
        return farmerStatus;
    }
    public String getReviewStatus() {
        return reviewStatus;
    }
    public Integer getReviewRound() {
        return reviewRound;
    }
    public Boolean getHasPassword() {
        return hasPassword;
    }

    // 自訂 from() 方法: Farmer + FarmerReview 聯表
    public static FarmerProfileResponse from(Farmer f, FarmerReview latestReview) {
        FarmerProfileResponse dto = new FarmerProfileResponse();
        dto.farmerId = f.getFarmerId();
        dto.email = f.getEmail();
        dto.farmName = f.getFarmName();
        dto.farmAddress = f.getFarmAddress();
        if (f.getCityDistrict() != null) {
            dto.districtId = f.getCityDistrict().getDistrictId();
            dto.cityName = f.getCityDistrict().getCityName();
            dto.distName = f.getCityDistrict().getDistName();
        }
        dto.farmerPhoneNum = f.getFarmerPhoneNum();
        dto.farmDesc = f.getFarmDesc();
        dto.locLat = f.getLocLat();
        dto.locLong = f.getLocLong();
        dto.farmerStatus = f.getFarmerStatus() != null ? f.getFarmerStatus().name() : null;
        if (latestReview != null) {
            dto.reviewStatus = latestReview.getReviewStatus() != null ? latestReview.getReviewStatus().name() : null;
            dto.reviewRound = latestReview.getReviewRound() != null ? latestReview.getReviewRound().intValue() : null;
        }
        dto.hasPassword = f.getPassword() != null;
        return dto;
    }

}
