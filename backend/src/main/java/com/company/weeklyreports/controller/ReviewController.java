package com.company.weeklyreports.controller;

import com.company.weeklyreports.model.dto.ReportDTO;
import com.company.weeklyreports.model.dto.ReviewActionDTO;
import com.company.weeklyreports.model.dto.ReviewRequest;
import com.company.weeklyreports.security.UserPrincipal;
import com.company.weeklyreports.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports/{id}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ReportDTO review(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ReviewRequest request) {
        return reviewService.review(id, principal, request);
    }

    @GetMapping
    public List<ReviewActionDTO> getReviews(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return reviewService.getReviews(id, principal);
    }
}
