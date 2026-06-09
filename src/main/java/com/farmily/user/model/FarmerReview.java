package com.farmily.user.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "farmer_review")
public class FarmerReview implements Serializable {

    public enum ReviewStatus{
        PENDING, REVIEWING, APPROVED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id", updatable = false)
    private Integer reviewId;

    @ManyToOne
    @JoinColumn(name = "admin_id", referencedColumnName = "admin_id")
    private Admin admin;

    @Column(name = "review_round", nullable = false)
    private Integer reviewRound = 1;        // 預設初始值 = 1

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status")
    private ReviewStatus reviewStatus;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "notes", columnDefinition = "longtext")
    private String notes;

    @Column(name = "cert_file_land", columnDefinition = "longblob")
    private byte[] certFileLand;

    @Column(name = "cert_file_product", columnDefinition = "longblob")
    private byte[] certFileProduct;

    @Column(name = "cert_file_identity", columnDefinition = "longblob")
    private byte[] certFileIdentity;

    @OneToMany(mappedBy = "farmerReview", cascade = CascadeType.ALL)
    private Set<Farmer> farmers;



    public Integer getReviewId() {
        return reviewId;
    }

    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public Integer getReviewRound() {
        return reviewRound;
    }

    public void setReviewRound(Integer reviewRound) {
        this.reviewRound = reviewRound;
    }

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public Set<Farmer> getFarmers() {
        return farmers;
    }

    public void setFarmers(Set<Farmer> farmers) {
        this.farmers = farmers;
    }
}
