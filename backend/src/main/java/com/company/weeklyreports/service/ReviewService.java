package com.company.weeklyreports.service;

import com.company.weeklyreports.model.dto.ReportDTO;
import com.company.weeklyreports.model.dto.ReviewActionDTO;
import com.company.weeklyreports.model.dto.ReviewRequest;
import com.company.weeklyreports.model.entity.User;

import java.util.List;

/**
 * Business logic for a manager reviewing a submitted report (approve or
 * request changes) and for reading a report's review history. Interfaced
 * (Dependency Inversion) alongside ReportService, per the same
 * "most complex, most worth demonstrating this on" design decision.
 */
public interface ReviewService {

    // Applies a manager's review decision to a submitted report, recording
    // an append-only ReviewAction and returning the report's updated state.
    ReportDTO submitReview(Long reportId, ReviewRequest request, User currentManager);

    // Full review history for one report, newest first. Team members may
    // only read their own report's history; managers may read any.
    List<ReviewActionDTO> getReviewHistory(Long reportId, User currentUser);
}
