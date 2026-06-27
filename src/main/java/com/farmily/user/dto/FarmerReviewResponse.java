package com.farmily.user.dto;

import com.farmily.user.model.Admin;
import com.farmily.user.model.Farmer;
import com.farmily.user.model.FarmerReview;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 回傳給管理員看的審核資料包成 dto
public class FarmerReviewResponse {

    private Integer reviewId;
    private Integer farmerId;
    private String farmName;
    private String farmerEmail;
    private Integer reviewRound;
    private String adminName;
    private String adminEmail;
    private String reviewStatus;          // PENDING / REVIEWING / APPROVED / REJECTED
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String rejectReason;
    private String submittedFarmName;     // 本輪提交快照
    private String submittedFarmAddress;
    private String submittedCityName;
    private String submittedDistName;
    private BigDecimal submittedLocLat;
    private BigDecimal submittedLocLong;
    private Boolean hasCertLand;          // 文件只標「有沒有上傳」
    private Boolean hasCertProduct;
    private Boolean hasCertIdentity;
    private Boolean emailVerified;        // 小農是否已完成 Email 驗證（查詢進度頁判斷是否顯示「重寄啟用信」）
    private String farmerStatus;          // PENDING / ACTIVE / SUSPENDED

    // getter
    public Integer getReviewId() {
        return reviewId;
    }
    public Integer getFarmerId() {
        return farmerId;
    }
    public String getFarmName() {
        return farmName;
    }
    public String getFarmerEmail() {
        return farmerEmail;
    }
    public Integer getReviewRound() {
        return reviewRound;
    }
    public String getAdminName() {
        return adminName;
    }
    public String getAdminEmail() {
        return adminEmail;
    }
    public String getReviewStatus() {
        return reviewStatus;
    }

    // 給未啟用小農審核狀態 Reviewing 設定仍為 Pending (前端顯示用)
    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
    public String getRejectReason() {
        return rejectReason;
    }
    public String getSubmittedFarmName() {
        return submittedFarmName;
    }
    public String getSubmittedFarmAddress() {
        return submittedFarmAddress;
    }
    public String getSubmittedCityName() {
        return submittedCityName;
    }
    public String getSubmittedDistName() {
        return submittedDistName;
    }
    public BigDecimal getSubmittedLocLat() {
        return submittedLocLat;
    }
    public BigDecimal getSubmittedLocLong() {
        return submittedLocLong;
    }
    public Boolean getHasCertLand() {
        return hasCertLand;
    }
    public Boolean getHasCertProduct() {
        return hasCertProduct;
    }
    public Boolean getHasCertIdentity() {
        return hasCertIdentity;
    }
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    public String getFarmerStatus() {
        return farmerStatus;
    }
    public void setFarmerStatus(String farmerStatus) {
        this.farmerStatus = farmerStatus;
    }

    public static FarmerReviewResponse from(FarmerReview r){
        FarmerReviewResponse dto = new FarmerReviewResponse();
        dto.reviewId = r.getReviewId();

        Farmer farmer = r.getFarmer();
        if (farmer != null) {
            dto.farmerId = farmer.getFarmerId();
            dto.farmerEmail = farmer.getEmail();
            dto.farmName = farmer.getFarmName();
        }

        dto.reviewRound = r.getReviewRound();

        Admin admin = r.getAdmin();
        if(admin != null){
            dto.adminName = admin.getAdminName();
            dto.adminEmail = admin.getAdminEmail();
        }

        dto.reviewStatus = r.getReviewStatus() != null ? r.getReviewStatus().name() : null;
        dto.submittedAt = r.getSubmittedAt();
        dto.reviewedAt = r.getReviewedAt();
        dto.rejectReason = r.getRejectReason();
        dto.submittedFarmName = r.getSubmittedFarmName();
        dto.submittedFarmAddress = r.getSubmittedFarmAddress();
        if (r.getSubmittedDistrict() != null) {
            dto.submittedCityName = r.getSubmittedDistrict().getCityName();
            dto.submittedDistName = r.getSubmittedDistrict().getDistName();
        }
        dto.submittedLocLat = r.getSubmittedLocLat();
        dto.submittedLocLong = r.getSubmittedLocLong();
        dto.hasCertLand = r.getCertFileLand() != null;
        dto.hasCertProduct = r.getCertFileProduct() != null;
        dto.hasCertIdentity = r.getCertFileIdentity() != null;
        return dto;
    }
}
