package com.bloodbank.user.domain.repository.specification;

import com.bloodbank.user.application.dto.request.StaffSearchRequest;
import com.bloodbank.user.domain.entity.StaffProfile;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class StaffSpecifications {

    public static Specification<StaffProfile> buildSearchSpecification(StaffSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getSearchTerm())) {
                String pattern = "%" + request.getSearchTerm().toLowerCase() + "%";
                Predicate fullNameMatch = cb.like(cb.lower(root.get("fullName")), pattern);
                Predicate designationMatch = cb.like(cb.lower(root.get("designation")), pattern);
                Predicate phoneMatch = cb.like(cb.lower(root.get("phone")), pattern);
                predicates.add(cb.or(fullNameMatch, designationMatch, phoneMatch));
            }

            if (StringUtils.hasText(request.getDesignation())) {
                predicates.add(cb.equal(cb.lower(root.get("designation")), request.getDesignation().toLowerCase()));
            }

            if (request.getBloodBankId() != null) {
                predicates.add(cb.equal(root.get("bloodBankId"), request.getBloodBankId()));
            }

            if (request.getHospitalId() != null) {
                predicates.add(cb.equal(root.get("hospitalId"), request.getHospitalId()));
            }

            if (request.getReportingManagerId() != null) {
                predicates.add(cb.equal(root.get("reportingManagerId"), request.getReportingManagerId()));
            }

            if (StringUtils.hasText(request.getStatus())) {
                try {
                    predicates.add(cb.equal(root.get("staffStatus").as(String.class), request.getStatus().toUpperCase()));
                } catch (Exception ignored) {
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
