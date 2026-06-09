package com.farmily.user.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "admin")
public class Admin implements Serializable {

    // Enum 宣告在類別內
    public enum AdminStatus {
        ACTIVE, SUSPENDED, DELETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id", updatable = false)
    private Integer adminId;

    @Column(name = "admin_email", unique = true, nullable = false)
    private String adminEmail;

    @Column(name = "admin_password")
    private String adminPassword;

    @Column(name = "admin_name")
    private String adminName;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_status")
    private AdminStatus adminStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private List<FarmerReview> farmerReviewList;

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private Set<AdminPermissionRole> permissionRoles;


    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public AdminStatus getAdminStatus() {
        return adminStatus;
    }

    public void setAdminStatus(AdminStatus adminStatus) {
        this.adminStatus = adminStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<FarmerReview> getFarmerReviewList() {
        return farmerReviewList;
    }

    public void setFarmerReviewList(List<FarmerReview> farmerReviewList) {
        this.farmerReviewList = farmerReviewList;
    }

    public Set<AdminPermissionRole> getPermissionRoles() {
        return permissionRoles;
    }

    public void setPermissionRoles(Set<AdminPermissionRole> permissionRoles) {
        this.permissionRoles = permissionRoles;
    }
}
