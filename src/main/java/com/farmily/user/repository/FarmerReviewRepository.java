package com.farmily.user.repository;

import com.farmily.user.model.FarmerReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FarmerReviewRepository extends JpaRepository<FarmerReview, Integer> {

    // 查某小農「最新一筆」審核
    FarmerReview findTopByFarmer_FarmerIdOrderByReviewRoundDesc (Integer farmerId);

    // 查某小農「所有」審核紀錄，（新到舊，管理員瀏覽歷史用）
    List<FarmerReview> findByFarmer_FarmerIdOrderByReviewRoundDesc (Integer farmerId);

    // 依狀態列出（待審清單，submittedAt 舊到新）
    List<FarmerReview> findByReviewStatusOrderBySubmittedAtAsc(FarmerReview.ReviewStatus status);

    // 查某小農「最新一筆」+「通過審核」的資料
//    FarmerReview findTopByFarmer_FarmerIdAndReviewStatusOrderByReviewRoundDesc(
//            Integer farmerId, FarmerReview.ReviewStatus status); // 傳 APPROVED

}
