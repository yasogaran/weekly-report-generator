package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.Report;
import com.company.weeklyreports.model.entity.ReportStatus;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * Builds the GET /reports filter query dynamically from whichever of week/projectId/status/
 * memberId were actually supplied (api-doc.md). A Specification composes only the predicates
 * that apply, which fits this better than either a wall of "if param != null" branches
 * inline in the service or a combinatorial explosion of derived-query method names on
 * ReportRepository for every filter combination.
 */
public final class ReportSpecifications {

    private ReportSpecifications() {
    }

    /**
     * @param requesterId the calling user's id
     * @param isManager whether the caller can see other members' reports at all
     * @param memberId manager-only filter; silently ignored for a non-manager caller,
     *                  since their own reports are always scoped to themselves regardless
     *                  (api-doc.md: "silently overridden to the caller's own id")
     */
    public static Specification<Report> build(
            Long requesterId, boolean isManager, Long projectId, ReportStatus status, LocalDate week, Long memberId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!isManager) {
                predicates.add(cb.equal(root.get("user").get("id"), requesterId));
            } else if (memberId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), memberId));
            }

            if (projectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (week != null) {
                predicates.add(cb.equal(root.get("weekStartDate"), week));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
