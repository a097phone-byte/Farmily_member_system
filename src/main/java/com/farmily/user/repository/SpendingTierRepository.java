package com.farmily.user.repository;

import com.farmily.user.model.SpendingTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpendingTierRepository extends JpaRepository<SpendingTier, Integer> {

    // 依會員「每月消費金額」找出對應的級距名稱
    @Query(value =
            "SELECT tier_name FROM spending_tier " +
                    "WHERE min_amount <= ?1 AND (max_amount IS NULL OR ?1 <= max_amount) LIMIT 1", nativeQuery = true)
    String findTierNameByAmount(Integer amount);
}
