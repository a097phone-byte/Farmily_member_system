package com.farmily.user.repository;

import com.farmily.user.model.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FarmerRepository extends JpaRepository<Farmer, Integer> {

    Optional<Farmer> findByEmail(String email);
    boolean existsByEmail (String email);       // 檢查 email 全系統唯一

}
