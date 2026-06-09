package com.farmily.user.repository;

import com.farmily.user.model.CityDistrict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityDistrictRepository extends JpaRepository<CityDistrict, Integer> {

}
