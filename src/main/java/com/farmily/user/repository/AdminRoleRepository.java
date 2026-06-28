package com.farmily.user.repository;

import com.farmily.user.model.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;

// 列出系統所有可指派的權限
public interface AdminRoleRepository extends JpaRepository<AdminRole, Integer> {
}