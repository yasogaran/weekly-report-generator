package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.dto.ActivityFeedItemDTO;
import com.company.weeklyreports.model.entity.ReviewAction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Data access for ReviewAction rows - the audit trail of manager decisions
 * on a report (approve / request changes).
 */
public interface ReviewActionRepository extends JpaRepository<ReviewAction, Long> {

    // Full review history for one report, newest first - used on the
    // review page so a manager can see prior rounds of feedback alongside
    // the version currently under review.
    List<ReviewAction> findByReport_IdOrderByCreatedAtDesc(Long reportId);

    // Activity feed, review half: the most recent N approve/request-changes
    // decisions across all reports. The CASE expression turns the
    // ReviewActionType enum into the plain "APPROVED"/"REQUESTED_CHANGES"
    // string ActivityFeedItemDTO.type expects (it also needs to hold
    // "SUBMITTED", which isn't a ReviewActionType value at all, so the DTO
    // field can't just be the enum itself). Paired in DashboardService
    // with ReportRepository.findRecentSubmissions() - see
    // DashboardService.getActivityFeed() for why. The Pageable becomes a
    // real LIMIT/OFFSET at the SQL level, not an in-memory truncation.
    @Query("""
            SELECT NEW com.company.weeklyreports.model.dto.ActivityFeedItemDTO(
                CASE ra.action
                    WHEN com.company.weeklyreports.model.entity.ReviewActionType.APPROVED THEN 'APPROVED'
                    ELSE 'REQUESTED_CHANGES'
                END,
                ra.report.id, ra.reviewer.name, ra.createdAt, ra.comment)
            FROM ReviewAction ra
            ORDER BY ra.createdAt DESC
            """)
    List<ActivityFeedItemDTO> findRecentReviewActivity(Pageable pageable);
}
