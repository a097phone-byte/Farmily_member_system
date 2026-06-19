package com.farmily.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

// 小農免登入重送申請：申請者(PENDING)還不能登入，改用 email + password 驗證身分
public class PublicFarmerResubmitRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String farmName;
    private String farmAddress;
    private Integer districtId;
    private BigDecimal locLat;
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

    public String getFarmAddress() {
        return farmAddress;
    }

    public void setFarmAddress(String farmAddress) {
        this.farmAddress = farmAddress;
    }

    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
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
