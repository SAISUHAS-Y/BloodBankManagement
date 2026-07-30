package com.bloodbank.user.domain.repository.specification;

import com.bloodbank.user.application.dto.request.DonorSearchRequest;
import com.bloodbank.user.domain.entity.DonorProfile;
import com.bloodbank.user.domain.enums.DonorStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class DonorSpecifications {

    public static Specification<DonorProfile> buildSearchSpecification(DonorSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getSearchTerm())) {
                String pattern = "%" + request.getSearchTerm().toLowerCase() + "%";
                Predicate fullNameMatch = cb.like(cb.lower(root.get("fullName")), pattern);
                Predicate emailMatch = cb.like(cb.lower(root.get("email")), pattern);
                Predicate phoneMatch = cb.like(cb.lower(root.get("phone")), pattern);
                Predicate idNumberMatch = cb.like(cb.lower(root.get("idNumber")), pattern);
                predicates.add(cb.or(fullNameMatch, emailMatch, phoneMatch, idNumberMatch));
            }

            if (request.getBloodGroupId() != null) {
                predicates.add(cb.equal(root.get("bloodGroupId"), request.getBloodGroupId()));
            }

            if (request.getStateId() != null) {
                predicates.add(cb.equal(root.get("stateId"), request.getStateId()));
            }

            if (request.getDistrictId() != null) {
                predicates.add(cb.equal(root.get("districtId"), request.getDistrictId()));
            }

            if (request.getCityId() != null) {
                predicates.add(cb.equal(root.get("cityId"), request.getCityId()));
            }

            if (StringUtils.hasText(request.getGenderCode())) {
                predicates.add(cb.equal(root.get("genderCode"), request.getGenderCode()));
            }

            if (StringUtils.hasText(request.getStatus())) {
                try {
                    DonorStatus statusEnum = DonorStatus.valueOf(request.getStatus().toUpperCase());
                    predicates.add(cb.equal(root.get("donorStatus"), statusEnum));
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (request.getLastDonationFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("lastDonationDate"), request.getLastDonationFrom()));
            }

            if (request.getLastDonationTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("lastDonationDate"), request.getLastDonationTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
