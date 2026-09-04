package com.company.weeklyreports.service;

import com.company.weeklyreports.model.dto.ReportDTO;
import com.company.weeklyreports.model.dto.ReviewActionDTO;
import com.company.weeklyreports.model.dto.ReviewRequest;
import com.company.weeklyreports.security.UserPrincipal;
import java.util.List;

/** Interface + Impl (Dependency Inversion) — same reasoning as ReportService (CLAUDE.md). */
public interface ReviewService {

    /** POST /reports/{id}/reviews — MANAGER only (pure role check, no ownership dimension, api-doc.md). */
    ReportDTO review(Long reportId, UserPrincipal reviewer, ReviewRequest request);

    /** GET /reports/{id}/reviews — any authenticated user; ownership-checked for non-managers. */
    List<ReviewActionDTO> getReviews(Long reportId, UserPrincipal requester);
}
