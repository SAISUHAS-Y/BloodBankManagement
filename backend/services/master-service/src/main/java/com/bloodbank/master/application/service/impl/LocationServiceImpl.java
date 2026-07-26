package com.bloodbank.master.application.service.impl;

import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.master.application.dto.CityResponse;
import com.bloodbank.master.application.dto.DistrictResponse;
import com.bloodbank.master.application.dto.StateResponse;
import com.bloodbank.master.application.service.LocationService;
import com.bloodbank.master.domain.entity.City;
import com.bloodbank.master.domain.entity.District;
import com.bloodbank.master.domain.entity.State;
import com.bloodbank.master.domain.repository.CityRepository;
import com.bloodbank.master.domain.repository.DistrictRepository;
import com.bloodbank.master.domain.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final CityRepository cityRepository;
    private final CacheManager cacheManager;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "states", key = "#includeInactive ? 'all' : 'active'")
    public List<StateResponse> getAllStates(boolean includeInactive) {
        log.info("Fetching states from database (includeInactive={})", includeInactive);
        List<State> states = includeInactive ? stateRepository.findAllByOrderByNameAsc() : stateRepository.findByActiveTrueOrderByNameAsc();
        return states.stream()
                .map(state -> StateResponse.builder()
                        .id(state.getId())
                        .name(state.getName())
                        .code(state.getCode())
                        .active(state.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "districts", key = "#stateId + ':' + #includeInactive")
    public List<DistrictResponse> getDistrictsByState(Long stateId, boolean includeInactive) {
        log.info("Fetching districts from database for stateId={} (includeInactive={})", stateId, includeInactive);
        List<District> districts = includeInactive 
                ? districtRepository.findByStateIdOrderByNameAsc(stateId) 
                : districtRepository.findByStateIdAndActiveTrueOrderByNameAsc(stateId);
        
        return districts.stream()
                .map(district -> DistrictResponse.builder()
                        .id(district.getId())
                        .name(district.getName())
                        .active(district.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "cities", key = "#districtId + ':' + #includeInactive")
    public List<CityResponse> getCitiesByDistrict(Long districtId, boolean includeInactive) {
        log.info("Fetching cities from database for districtId={} (includeInactive={})", districtId, includeInactive);
        List<City> cities = includeInactive 
                ? cityRepository.findByDistrictIdOrderByNameAsc(districtId) 
                : cityRepository.findByDistrictIdAndActiveTrueOrderByNameAsc(districtId);

        return cities.stream()
                .map(city -> CityResponse.builder()
                        .id(city.getId())
                        .name(city.getName())
                        .pincode(city.getPincode())
                        .active(city.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deactivateState(Long stateId, String updatedBy) {
        log.info("Cascade-deactivating State ID: {}", stateId);

        State state = stateRepository.findById(stateId)
                .orElseThrow(() -> new ResourceNotFoundException("State not found with ID: " + stateId));

        // 1. Deactivate State
        state.setActive(false);
        state.setUpdatedBy(updatedBy);
        stateRepository.save(state);

        // 2. Fetch and cascade-deactivate all Districts in this State
        List<District> districts = districtRepository.findByStateId(stateId);
        districts.forEach(d -> {
            d.setActive(false);
            d.setUpdatedBy(updatedBy);
        });
        districtRepository.saveAll(districts);

        // 3. Fetch and cascade-deactivate all Cities under those Districts
        if (!districts.isEmpty()) {
            List<Long> districtIds = districts.stream().map(d -> d.getId()).collect(Collectors.toList());
            List<City> cities = cityRepository.findByDistrictIdIn(districtIds);
            cities.forEach(c -> {
                c.setActive(false);
                c.setUpdatedBy(updatedBy);
            });
            cityRepository.saveAll(cities);
            log.info("Cascade-deactivated {} districts and {} cities under State ID: {}", districts.size(), cities.size(), stateId);
        }

        evictLocationCaches();
    }

    private void evictLocationCaches() {
        if (cacheManager != null) {
            evictCache("states");
            evictCache("districts");
            evictCache("cities");
            evictCache("master_bundle");
        }
    }

    private void evictCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Cleared cache: {}", cacheName);
        }
    }
}
