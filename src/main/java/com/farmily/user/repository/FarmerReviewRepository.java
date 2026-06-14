package com.farmily.user.repository;

import com.farmily.user.model.FarmerReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FarmerReviewRepository extends JpaRepository<FarmerReview, Integer> {

    // 某小農所有審核紀錄（新到舊，管理員瀏覽歷史用）
    List<FarmerReview> findByFarmerReviewRoundList (Integer farmerId);

    // 某小農最新一筆審核（顯示目前審核狀態用）
    FarmerReview findLatestByFarmerReviewRound (Integer farmerId);
}
