package com.bloodbank.master.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.master.application.dto.CityResponse;
import com.bloodbank.master.application.dto.DistrictResponse;
import com.bloodbank.master.application.dto.StateResponse;
import com.bloodbank.master.application.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/master/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/states")
    public ResponseEntity<ApiResponse<List<StateResponse>>> getAllStates(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        
        List<StateResponse> states = locationService.getAllStates(includeInactive);
        return ResponseEntity.ok(ApiResponse.success(states));
    }

    @GetMapping("/states/{stateId}/districts")
    public ResponseEntity<ApiResponse<List<DistrictResponse>>> getDistrictsByState(
            @PathVariable Long stateId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        
        List<DistrictResponse> districts = locationService.getDistrictsByState(stateId, includeInactive);
        return ResponseEntity.ok(ApiResponse.success(districts));
    }

    @GetMapping("/districts/{districtId}/cities")
    public ResponseEntity<ApiResponse<List<CityResponse>>> getCitiesByDistrict(
            @PathVariable Long districtId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        
        List<CityResponse> cities = locationService.getCitiesByDistrict(districtId, includeInactive);
        return ResponseEntity.ok(ApiResponse.success(cities));
    }

    @DeleteMapping("/admin/states/{id}")
    @HasPermission("MASTER_MANAGE")
    public ResponseEntity<ApiResponse<Void>> deactivateState(
            @PathVariable Long id,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "ADMIN";
        locationService.deactivateState(id, username);
        return ResponseEntity.ok(ApiResponse.success("State and all child districts/cities cascade-deactivated successfully", null));
    }
}
