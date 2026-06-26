package com.farmily.user.repository;

import com.farmily.user.model.AccountToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountTokenRepository extends JpaRepository<AccountToken, Integer> {

    // 用 token 字串找出對應的紀錄
    Optional<AccountToken> findByToken(String token);

}