package com.farmily.user.dto;

// 立即生效、不觸發重審的欄位
public class FarmerProfileUpdateRequest {

    private String farmerPhoneNum;
    private String farmDesc;

    public String getFarmerPhoneNum() {
        return farmerPhoneNum;
    }

    public void setFarmerPhoneNum(String farmerPhoneNum) {
        this.farmerPhoneNum = farmerPhoneNum;
    }

    public String getFarmDesc() {
        return farmDesc;
    }

    public void setFarmDesc(String farmDesc) {
        this.farmDesc = farmDesc;
    }
}
