package com.farmily.user.security.service;

import com.farmily.user.model.Farmer;
import com.farmily.user.repository.FarmerRepository;
import com.farmily.user.security.FarmerUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Security - "How" to find Farmer ?
@Service
public class  FarmerUserDetailsService implements UserDetailsService {

    private final FarmerRepository farmerRepository;

    public FarmerUserDetailsService(FarmerRepository farmerRepository) {
        this.farmerRepository = farmerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // step1: 用 email 去 farmer 表撈帳號，撈不到就丟例外
        Farmer farmer = farmerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("查無此帳號: " + email));

        // step2: 包成自製的 FarmerUserDetails 身分格式回傳
        return new FarmerUserDetails(farmer);
    }
}