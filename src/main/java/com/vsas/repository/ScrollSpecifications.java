package com.vsas.repository;

import com.vsas.entity.Scroll;
import com.vsas.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class ScrollSpecifications {

    private ScrollSpecifications() {}

    public static Specification<Scroll> filtered(
            String uploaderIdKey, String scrollId, String name, Instant uploadedFrom, Instant uploadedTo) {
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            List<Predicate> p = new ArrayList<>();
            Join<Scroll, User> u = root.join("uploader", JoinType.INNER);
            if (uploaderIdKey != null && !uploaderIdKey.isBlank()) {
                p.add(cb.equal(u.get("idKey"), uploaderIdKey.trim()));
            }
            if (scrollId != null && !scrollId.isBlank()) {
                p.add(cb.equal(root.get("scrollId"), scrollId.trim()));
            }
            if (name != null && !name.isBlank()) {
                String q = name.trim().toLowerCase(Locale.ROOT).replace("%", "").replace("_", "");
                if (!q.isEmpty()) {
                    p.add(cb.like(cb.lower(root.get("name")), "%" + q + "%"));
                }
            }
            if (uploadedFrom != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("uploadedAt"), uploadedFrom));
            }
            if (uploadedTo != null) {
                p.add(cb.lessThanOrEqualTo(root.get("uploadedAt"), uploadedTo));
            }
            if (p.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }
}
