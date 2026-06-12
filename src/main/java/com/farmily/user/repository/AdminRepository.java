package com.farmily.user.repository;

import com.farmily.user.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Integer> {

    Optional<Admin> findByAdminEmail (String adminEmail);
}
