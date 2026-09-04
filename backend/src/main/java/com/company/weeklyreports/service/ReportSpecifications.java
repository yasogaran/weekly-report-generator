package com.company.weeklyreports.service;

import com.company.weeklyreports.model.dto.ReportFilterParams;
import com.company.weeklyreports.model.entity.Report;
import org.springframework.data.jpa.domain.Specification;

/**
 * Builds a single dynamic Specification<Report> out of whichever
 * ReportFilterParams fields are actually set. Public because its caller,
 * ReportServiceImpl, lives in service.impl (a separate package per
 * system-design.md's layer-based structure) - but this is still only ever
 * meant to be used by the Report service, not a general-purpose utility.
 */
public final class ReportSpecifications {

    private ReportSpecifications() {
    }

    // Combines only the filters that are present into one AND-ed
    // Specification. Starting from Specification.where(null) (matches
    // everything) and conditionally .and()-ing each filter avoids a long
    // if/else chain building raw JPQL by hand.
    public static Specification<Report> fromFilters(ReportFilterParams filters) {
        Specification<Report> spec = Specification.where(null);

        if (filters == null) {
            return spec;
        }

        if (filters.getMemberId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), filters.getMemberId()));
        }
        if (filters.getProjectId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("project").get("id"), filters.getProjectId()));
        }
        if (filters.getStatus() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), filters.getStatus()));
        }
        if (filters.getWeekStart() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("weekStartDate"), filters.getWeekStart()));
        }
        if (filters.getWeekEnd() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("weekEndDate"), filters.getWeekEnd()));
        }

        return spec;
    }
}
