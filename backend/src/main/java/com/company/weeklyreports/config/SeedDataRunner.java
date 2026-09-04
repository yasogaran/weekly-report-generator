package com.company.weeklyreports.config;

import com.company.weeklyreports.model.dto.AchievementRequest;
import com.company.weeklyreports.model.dto.BlockerRequest;
import com.company.weeklyreports.model.dto.CreateReportRequest;
import com.company.weeklyreports.model.dto.HoursByTypeRequest;
import com.company.weeklyreports.model.dto.ReportDTO;
import com.company.weeklyreports.model.dto.ReviewRequest;
import com.company.weeklyreports.model.dto.TaskEntryRequest;
import com.company.weeklyreports.model.dto.UpdateReportRequest;
import com.company.weeklyreports.model.entity.EntryType;
import com.company.weeklyreports.model.entity.Project;
import com.company.weeklyreports.model.entity.ReportStatus;
import com.company.weeklyreports.model.entity.ReviewActionType;
import com.company.weeklyreports.model.entity.Role;
import com.company.weeklyreports.model.entity.User;
import com.company.weeklyreports.repository.ProjectRepository;
import com.company.weeklyreports.repository.UserRepository;
import com.company.weeklyreports.service.ReportService;
import com.company.weeklyreports.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Populates the database with realistic demo data on startup - one manager,
 * five team members, three projects, and a spread of reports across the
 * last four weeks in every status. @Profile("dev") keeps this out of any
 * future prod profile entirely (it's never even a candidate bean there).
 *
 * Idempotency guard: run() checks userRepository.count() == 0 before doing
 * anything else, and returns immediately if the table isn't empty. Without
 * this, restarting the app during development (which happens constantly)
 * would re-seed the same data every time, quickly filling the database
 * with duplicate managers/members/projects/reports. The check is a single
 * cheap COUNT query, not a re-seed-and-dedupe step.
 *
 * Design choice: every report below is created through the REAL
 * ReportService/ReviewService methods (createDraft -> submitReport ->
 * submitReview), not by constructing Report/ReviewAction entities directly
 * and saving them. This is slower to write than hand-building entities,
 * but it means the seed data is only ever in a state the real application
 * could actually produce - in particular, the one report seeded with a
 * full version history is forked by calling the real
 * ReportService.updateReport() on a NEEDS_CORRECTION report, exercising
 * the exact same fork-on-edit code path a real user's correction cycle
 * would - not two hand-built Report rows that merely look related.
 */
@Component
@Profile("dev")
public class SeedDataRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataRunner.class);
    private static final String SEED_PASSWORD = "password123";

    private static final String[] MEMBER_NAMES = {
            "Jordan Lee", "Sam Patel", "Taylor Kim", "Morgan Diaz", "Casey Nguyen"
    };
    private static final String[] PROJECT_NAMES = {"Client A", "Internal Tooling", "R&D"};

    private static final String[] TASK_NAMES = {
            "Implement login flow", "Fix pagination bug", "Write API docs",
            "Refactor report mapper", "Design dashboard charts", "Clear code review backlog",
            "Set up CI pipeline", "Investigate flaky test"
    };
    private static final String[] PRIORITIES = {"HIGH", "MEDIUM", "LOW"};
    private static final String[] DELIVERABLES = {"PR merged", "Doc published", "Demo ready", "Ticket closed"};
    private static final String[] BLOCKER_DESCRIPTIONS = {
            "Waiting on API credentials from the client", "Blocked by an unresolved merge conflict",
            "Need design sign-off before continuing", "Shared test environment is down"
    };
    private static final String[] ACHIEVEMENT_DESCRIPTIONS = {
            "Shipped the new login flow ahead of schedule", "Reduced build time by 30%",
            "Closed out five backlog tickets", "Mentored a new team member"
    };
    private static final String[] TASK_TYPES = {"Development", "Meetings", "Code Review", "Documentation", "Testing"};

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ReportService reportService;
    private final ReviewService reviewService;
    private final PasswordEncoder passwordEncoder;

    public SeedDataRunner(UserRepository userRepository,
                           ProjectRepository projectRepository,
                           ReportService reportService,
                           ReviewService reviewService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.reportService = reportService;
        this.reviewService = reviewService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Seed data skipped - the users table already has data.");
            return;
        }

        User manager = createUser("Alex Manager", "manager@test.com", Role.MANAGER);
        List<User> members = new ArrayList<>();
        for (int i = 0; i < MEMBER_NAMES.length; i++) {
            members.add(createUser(MEMBER_NAMES[i], "member" + (i + 1) + "@test.com", Role.TEAM_MEMBER));
        }

        List<Project> projects = new ArrayList<>();
        for (String name : PROJECT_NAMES) {
            projects.add(projectRepository.save(Project.builder()
                    .name(name)
                    .description(name + " project")
                    .isActive(true)
                    .createdBy(manager)
                    .build()));
        }

        LocalDate currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate[] weekStarts = {
                currentWeekStart,
                currentWeekStart.minusWeeks(1),
                currentWeekStart.minusWeeks(2),
                currentWeekStart.minusWeeks(3)
        };
        // Cycled (not randomized) so the seed data is deterministic and
        // reproducible between runs, while still landing on every status
        // multiple times across the full (member x week) grid below.
        ReportStatus[] statusCycle = {
                ReportStatus.APPROVED, ReportStatus.SUBMITTED, ReportStatus.NEEDS_CORRECTION, ReportStatus.DRAFT
        };

        int seedIndex = 0;
        boolean versionedReportSeeded = false;
        for (int m = 0; m < members.size(); m++) {
            for (int w = 0; w < weekStarts.length; w++) {
                User owner = members.get(m);
                Project project = projects.get((m + w) % projects.size());
                LocalDate weekStart = weekStarts[w];

                // One report - member 0, the second-most-recent week - gets
                // the full NEEDS_CORRECTION -> fork -> resubmit -> APPROVED
                // treatment instead of the plain status cycle, so there's a
                // real multi-version chain with a real review-comment trail
                // to demo.
                if (!versionedReportSeeded && m == 0 && w == 1) {
                    createVersionedReport(owner, project, weekStart, manager, seedIndex);
                    versionedReportSeeded = true;
                } else {
                    ReportStatus targetStatus = statusCycle[(m + w) % statusCycle.length];
                    createReportAtStatus(owner, project, weekStart, targetStatus, manager, seedIndex);
                }
                seedIndex++;
            }
        }

        logSeedCredentials(manager, members);
    }

    private User createUser(String name, String email, Role role) {
        return userRepository.save(User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(SEED_PASSWORD))
                .role(role)
                .build());
    }

    // Drives one report through exactly as much of the real lifecycle as
    // needed to land on targetStatus: createDraft always runs, submitReport
    // runs for anything past DRAFT, and submitReview runs (with the
    // matching decision) for NEEDS_CORRECTION/APPROVED.
    private void createReportAtStatus(User owner, Project project, LocalDate weekStart,
                                       ReportStatus targetStatus, User manager, int seedIndex) {
        ReportDTO report = reportService.createDraft(buildCreateRequest(project, weekStart, seedIndex), owner);

        if (targetStatus == ReportStatus.DRAFT) {
            return;
        }

        reportService.submitReport(report.getId(), owner);

        if (targetStatus == ReportStatus.SUBMITTED) {
            return;
        }

        if (targetStatus == ReportStatus.NEEDS_CORRECTION) {
            reviewService.submitReview(report.getId(),
                    ReviewRequest.builder()
                            .action(ReviewActionType.REQUESTED_CHANGES)
                            .comment("Please add more detail to the in-progress task and re-submit.")
                            .build(),
                    manager);
            return;
        }

        // APPROVED
        reviewService.submitReview(report.getId(),
                ReviewRequest.builder()
                        .action(ReviewActionType.APPROVED)
                        .comment("Looks good - approved.")
                        .build(),
                manager);
    }

    // Builds the one seeded report with a real two-version history:
    // v1 is submitted, sent back with REQUESTED_CHANGES, and then left
    // completely alone - reportService.updateReport() on a
    // NEEDS_CORRECTION report is what forks a brand-new v2 row
    // (versionNumber = 2, parentReport = v1), which is then submitted and
    // approved on its own. v1's id is never reused or written to again
    // after the fork - it stays frozen exactly as ReportService guarantees.
    private void createVersionedReport(User owner, Project project, LocalDate weekStart, User manager, int seedIndex) {
        ReportDTO v1 = reportService.createDraft(buildCreateRequest(project, weekStart, seedIndex), owner);
        reportService.submitReport(v1.getId(), owner);
        reviewService.submitReview(v1.getId(),
                ReviewRequest.builder()
                        .action(ReviewActionType.REQUESTED_CHANGES)
                        .comment("The blocker section needs more context before this can be approved.")
                        .build(),
                manager);

        ReportDTO v2 = reportService.updateReport(v1.getId(), buildUpdateRequest(project, weekStart, seedIndex + 1), owner);
        reportService.submitReport(v2.getId(), owner);
        reviewService.submitReview(v2.getId(),
                ReviewRequest.builder()
                        .action(ReviewActionType.APPROVED)
                        .comment("Thanks for the update - approved.")
                        .build(),
                manager);
    }

    private CreateReportRequest buildCreateRequest(Project project, LocalDate weekStart, int seedIndex) {
        return CreateReportRequest.builder()
                .projectId(project.getId())
                .weekStartDate(weekStart)
                .weekEndDate(weekStart.plusDays(6))
                .notes("Seed data report for demo purposes.")
                .taskEntries(buildTaskEntries(seedIndex))
                .blockers(buildBlockers(seedIndex))
                .achievements(buildAchievements(seedIndex))
                .hoursByType(buildHours(seedIndex))
                .build();
    }

    private UpdateReportRequest buildUpdateRequest(Project project, LocalDate weekStart, int seedIndex) {
        return UpdateReportRequest.builder()
                .projectId(project.getId())
                .weekStartDate(weekStart)
                .weekEndDate(weekStart.plusDays(6))
                .notes("Seed data report for demo purposes (revised after correction).")
                .taskEntries(buildTaskEntries(seedIndex))
                .blockers(buildBlockers(seedIndex))
                .achievements(buildAchievements(seedIndex))
                .hoursByType(buildHours(seedIndex))
                .build();
    }

    private List<TaskEntryRequest> buildTaskEntries(int seedIndex) {
        List<TaskEntryRequest> tasks = new ArrayList<>();
        int taskCount = 2 + (seedIndex % 3); // 2-4
        for (int i = 0; i < taskCount; i++) {
            int idx = (seedIndex + i) % TASK_NAMES.length;
            boolean lastOne = i == taskCount - 1;
            tasks.add(TaskEntryRequest.builder()
                    .taskName(TASK_NAMES[idx])
                    .priority(PRIORITIES[idx % PRIORITIES.length])
                    .plannedPercent(100)
                    .actualPercent(lastOne ? 60 : 100)
                    .status(lastOne ? "In Progress" : "Done")
                    .timePlanned(BigDecimal.valueOf(8))
                    .timeSpent(BigDecimal.valueOf(6 + i))
                    .deliverable(DELIVERABLES[idx % DELIVERABLES.length])
                    .entryType(lastOne ? EntryType.PLANNED_NEXT_WEEK : EntryType.COMPLETED)
                    .build());
        }
        return tasks;
    }

    private List<BlockerRequest> buildBlockers(int seedIndex) {
        List<BlockerRequest> blockers = new ArrayList<>();
        int blockerCount = 1 + (seedIndex % 2); // 1-2
        for (int i = 0; i < blockerCount; i++) {
            blockers.add(BlockerRequest.builder()
                    .description(BLOCKER_DESCRIPTIONS[(seedIndex + i) % BLOCKER_DESCRIPTIONS.length])
                    .isKeyIssue(i == 0)
                    .build());
        }
        return blockers;
    }

    private List<AchievementRequest> buildAchievements(int seedIndex) {
        List<AchievementRequest> achievements = new ArrayList<>();
        int achievementCount = 1 + ((seedIndex + 1) % 2); // 1-2
        for (int i = 0; i < achievementCount; i++) {
            achievements.add(AchievementRequest.builder()
                    .description(ACHIEVEMENT_DESCRIPTIONS[(seedIndex + i) % ACHIEVEMENT_DESCRIPTIONS.length])
                    .isKeyAchievement(i == 0)
                    .build());
        }
        return achievements;
    }

    private List<HoursByTypeRequest> buildHours(int seedIndex) {
        List<HoursByTypeRequest> hours = new ArrayList<>();
        int hoursCount = 2 + (seedIndex % 2); // 2-3
        for (int i = 0; i < hoursCount; i++) {
            hours.add(HoursByTypeRequest.builder()
                    .taskType(TASK_TYPES[(seedIndex + i) % TASK_TYPES.length])
                    .hours(BigDecimal.valueOf(4 + i * 2))
                    .build());
        }
        return hours;
    }

    private void logSeedCredentials(User manager, List<User> members) {
        log.info("=== Seed data created - demo login credentials (password for all: {}) ===", SEED_PASSWORD);
        log.info("Manager:      {}", manager.getEmail());
        members.forEach(member -> log.info("Team member:  {}", member.getEmail()));
    }
}
