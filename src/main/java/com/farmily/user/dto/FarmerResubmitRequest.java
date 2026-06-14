package com.farmily.user.dto;

import java.math.BigDecimal;

// 重新送審用（暫存，通過才生效）
public class FarmerResubmitRequest {

    private String farmName;
    private Integer districtId;
    private String farmAddress;
    private BigDecimal locLat;
    private BigDecimal locLong;
    private byte[] certFileLand;
    private byte[] certFileProduct;
    private byte[] certFileIdentity;

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
