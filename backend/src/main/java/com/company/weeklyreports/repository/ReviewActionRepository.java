package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.ReviewAction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewActionRepository extends JpaRepository<ReviewAction, Long> {

    /** Backs GET /reports/{id}/reviews — newest first (api-doc.md). */
    List<ReviewAction> findByReportIdOrderByCreatedAtDesc(Long reportId);
}
