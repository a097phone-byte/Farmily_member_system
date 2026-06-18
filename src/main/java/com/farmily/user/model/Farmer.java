package com.farmily.user.model;

import com.farmily.user.model.CityDistrict;
import com.farmily.user.model.FarmerReview;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "FARMER")
public class Farmer implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum FarmerStatus {
        PENDING, ACTIVE, SUSPENDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "farmer_id", updatable = false)
    private Integer farmerId;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password", nullable = true)
    private String password;

    @Column(name = "farm_address")
    private String farmAddress;

    @Column(name = "farm_name")
    private String farmName;

    @Column(name = "loc_lat", precision = 10, scale = 8)
    private BigDecimal locLat;

    @Column(name = "loc_long", precision = 11, scale = 8)
    private BigDecimal locLong;

    @Column(name = "farm_desc", columnDefinition = "longtext")
    private String farmDesc;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "farmer_created_at")
    private LocalDateTime farmerCreatedAt;

    @Column(name = "farmer_phone_num", length = 15)
    private String farmerPhoneNum;

    @Enumerated(EnumType.STRING)
    @Column(name = "farmer_status")
    private FarmerStatus farmerStatus;

    @ManyToOne
    @JoinColumn(name = "district_id", referencedColumnName = "district_id")
    private CityDistrict cityDistrict;

    @OneToMany(mappedBy = "farmer", cascade = CascadeType.ALL)
    private List<FarmerReview> reviews;


    public Integer getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Integer farmerId) {
        this.farmerId = farmerId;
    }

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

    public String getFarmAddress() {
        return farmAddress;
    }

    public void setFarmAddress(String farmAddress) {
        this.farmAddress = farmAddress;
    }

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
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

    public String getFarmDesc() {
        return farmDesc;
    }

    public void setFarmDesc(String farmDesc) {
        this.farmDesc = farmDesc;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public LocalDateTime getFarmerCreatedAt() {
        return farmerCreatedAt;
    }

    public void setFarmerCreatedAt(LocalDateTime farmerCreatedAt) {
        this.farmerCreatedAt = farmerCreatedAt;
    }

    public String getFarmerPhoneNum() {
        return farmerPhoneNum;
    }

    public void setFarmerPhoneNum(String farmerPhoneNum) {
        this.farmerPhoneNum = farmerPhoneNum;
    }

    public FarmerStatus getFarmerStatus() {
        return farmerStatus;
    }

    public void setFarmerStatus(FarmerStatus farmerStatus) {
        this.farmerStatus = farmerStatus;
    }

    public CityDistrict getCityDistrict() {
        return cityDistrict;
    }

    public void setCityDistrict(CityDistrict cityDistrict) {
        this.cityDistrict = cityDistrict;
    }

    public List<FarmerReview> getReviews() {
        return reviews;
    }

    public void setReviews(List<FarmerReview> reviews) {
        this.reviews = reviews;
    }
}