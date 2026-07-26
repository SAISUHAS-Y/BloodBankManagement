package com.bloodbank.master.domain.repository;

import com.bloodbank.master.domain.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByDistrictIdAndActiveTrueOrderByNameAsc(Long districtId);
    List<City> findByDistrictIdOrderByNameAsc(Long districtId);
    List<City> findByDistrictIdIn(List<Long> districtIds);
}
