package com.farmily.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

// 小農註冊申請請求端(api/farmer)
public class FarmerRegisterRequest {

    @Email @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String farmName;
    @NotBlank
    private String farmAddress;

    private Integer districtId;
    private String farmerPhoneNum;
    private String farmDesc;
    private BigDecimal locLat;      // 前端自動抓取後送入
    private BigDecimal locLong;
    private byte[] certFileLand;
    private byte[] certFileProduct;
    private byte[] certFileIdentity;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
    }

    public String getFarmAddress() {
        return farmAddress;
    }

    public void setFarmAddress(String farmAddress) {
        this.farmAddress = farmAddress;
    }

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

    public BigDecimal getLocLat() {
        return locLat;
    }

    public void setLocLat(BigDecimal locLat) {
        this.locLat = locLat;
    }

    public BigDecimal getLocLong() {
        return locLong;
    }

    public void setLocLong(BigDecimal locLong) {
        this.locLong = locLong;
    }

    public byte[] getCertFileLand() {
        return certFileLand;
    }

    public void setCertFileLand(byte[] certFileLand) {
        this.certFileLand = certFileLand;
    }

    public byte[] getCertFileProduct() {
        return certFileProduct;
    }

    public void setCertFileProduct(byte[] certFileProduct) {
        this.certFileProduct = certFileProduct;
    }

    public byte[] getCertFileIdentity() {
        return certFileIdentity;
    }

    public void setCertFileIdentity(byte[] certFileIdentity) {
        this.certFileIdentity = certFileIdentity;
    }
}
