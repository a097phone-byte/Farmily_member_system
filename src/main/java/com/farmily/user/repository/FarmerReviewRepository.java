package com.farmily.user.repository;

import com.farmily.user.model.FarmerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

// 管理員對小農審核永續層操作
public interface FarmerReviewRepository extends JpaRepository<FarmerReview, Integer> {

    // 查某小農「最新一筆」審核
    FarmerReview findTopByFarmer_FarmerIdOrderByReviewRoundDesc (Integer farmerId);

    // 查某小農「所有」審核紀錄
    List<FarmerReview> findByFarmer_FarmerIdOrderByReviewRoundDesc (Integer farmerId);

    // 依狀態列出（待審清單，submittedAt 舊到新）
    List<FarmerReview> findByReviewStatusOrderBySubmittedAtAsc(FarmerReview.ReviewStatus status);

    // 查出所有審核的輕量欄位，用於列出所有小農時避免逐筆查最新審核 (N+1)
    @Query(value =
            "SELECT farmer_id, review_status, review_round FROM farmer_review",
            nativeQuery = true)
    List<Object[]> findAllReviewStatusRounds();

    // 查某小農「最新一筆」+「通過審核」的資料
//    FarmerReview findTopByFarmer_FarmerIdAndReviewStatusOrderByReviewRoundDesc(
//            Integer farmerId, FarmerReview.ReviewStatus status); // 傳 APPROVED

}
