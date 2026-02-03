package com.fiap.sus.network.modules.health_unit.repository.spec;

import com.fiap.sus.network.modules.health_unit.dto.HealthUnitFilter;
import com.fiap.sus.network.modules.health_unit.entity.HealthUnit;
import com.fiap.sus.network.modules.user.entity.UserUnit;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import java.util.UUID;

public class HealthUnitSpecification {

    public static Specification<HealthUnit> withFilter(HealthUnitFilter filter, UUID userId, boolean isAdmin) {
        return withFilter(filter, userId, isAdmin, null, null, null, null);
    }

    public static Specification<HealthUnit> withFilter(HealthUnitFilter filter, UUID userId, boolean isAdmin, Double minLat, Double maxLat, Double minLon, Double maxLon) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            predicates.add(cb.equal(root.get("deleted"), false));

            if (!isAdmin && userId != null) {
                Join<HealthUnit, UserUnit> linkJoin = root.join("userUnits", JoinType.INNER);
                predicates.add(cb.equal(linkJoin.get("user").get("id"), userId));
                predicates.add(cb.equal(linkJoin.get("deleted"), false));
                query.distinct(true);
            }

            if (filter.name() != null && !filter.name().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.name().toLowerCase() + "%"));
            }
            
            if (filter.city() != null && !filter.city().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("address").get("city")), filter.city().toLowerCase()));
            }
            
            if (filter.state() != null && !filter.state().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("address").get("state")), filter.state().toLowerCase()));
            }

            if (minLat != null && maxLat != null) {
                predicates.add(cb.between(root.get("address").get("latitude"), minLat, maxLat));
            }

            if (minLon != null && maxLon != null) {
                predicates.add(cb.between(root.get("address").get("longitude"), minLon, maxLon));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
