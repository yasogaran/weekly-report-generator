package com.company.weeklyreports.config;

import com.company.weeklyreports.model.entity.Achievement;
import com.company.weeklyreports.model.entity.Blocker;
import com.company.weeklyreports.model.entity.HoursByType;
import com.company.weeklyreports.model.entity.Project;
import com.company.weeklyreports.model.entity.Report;
import com.company.weeklyreports.model.entity.ReportStatus;
import com.company.weeklyreports.model.entity.ReviewAction;
import com.company.weeklyreports.model.entity.ReviewActionType;
import com.company.weeklyreports.model.entity.Role;
import com.company.weeklyreports.model.entity.TaskEntry;
import com.company.weeklyreports.model.entity.TaskEntryType;
import com.company.weeklyreports.model.entity.User;
import com.company.weeklyreports.repository.ProjectRepository;
import com.company.weeklyreports.repository.ReportRepository;
import com.company.weeklyreports.repository.ReviewActionRepository;
import com.company.weeklyreports.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a small realistic dataset on first run only (build-plan.md Block 8) — 1 manager, 4
 * team members, 3 projects, and a handful of reports across mixed statuses including one
 * with 2 versions, so there's something real to click through immediately after a fresh
 * `mvn spring-boot:run` against an empty database. Uses each entity's @Builder rather than
 * long constructor calls, per CLAUDE.md's guidance that seed fixtures are exactly what the
 * builder pattern is for.
 * <p>
 * Every seeded account's password is "password123" — local dev only, obviously.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String SEED_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ReportRepository reportRepository;
    private final ReviewActionRepository reviewActionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Already seeded (or real data exists) — never overwrite on restart.
        }

        User manager = userRepository.save(User.builder()
                .name("Morgan Lee")
                .email("manager@example.com")
                .passwordHash(passwordEncoder.encode(SEED_PASSWORD))
                .role(Role.MANAGER)
                .build());

        User alice = userRepository.save(teamMember("Alice Chen", "alice@example.com"));
        User bob = userRepository.save(teamMember("Bob Nguyen", "bob@example.com"));
        User carol = userRepository.save(teamMember("Carol Diaz", "carol@example.com"));
        userRepository.save(teamMember("Dave Patel", "dave@example.com"));

        Project clientPortal = projectRepository.save(
                Project.builder().name("Client Portal").description("External client-facing dashboard").build());
        Project internalTools = projectRepository.save(
                Project.builder().name("Internal Tools").description("Internal tooling and automation").build());
        projectRepository.save(
                Project.builder().name("Mobile App").description("iOS/Android companion app").build());

        LocalDate thisWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate twoWeeksAgoStart = thisWeekStart.minusWeeks(2);

        // Alice: a straightforward APPROVED report from two weeks ago.
        Report aliceApproved = buildReport(alice, clientPortal, twoWeeksAgoStart, ReportStatus.APPROVED, 1, null);
        aliceApproved.setSubmittedAt(twoWeeksAgoStart.plusDays(4).atTime(17, 0));
        reportRepository.save(aliceApproved);
        reviewActionRepository.save(ReviewAction.builder()
                .report(aliceApproved)
                .reviewer(manager)
                .action(ReviewActionType.APPROVED)
                .createdAt(twoWeeksAgoStart.plusDays(5).atTime(10, 0))
                .build());

        // Bob: currently SUBMITTED, awaiting review — this week.
        Report bobSubmitted = buildReport(bob, internalTools, thisWeekStart, ReportStatus.SUBMITTED, 1, null);
        bobSubmitted.setSubmittedAt(LocalDateTime.now().minusHours(3));
        reportRepository.save(bobSubmitted);

        // Carol: the versioning demo — a NEEDS_CORRECTION original (frozen, superseded) plus
        // its version-2 fork, currently back in DRAFT after Carol started addressing the
        // feedback but hasn't resubmitted yet.
        Report carolV1 = buildReport(carol, clientPortal, lastWeekStart, ReportStatus.NEEDS_CORRECTION, 1, null);
        carolV1.setSubmittedAt(lastWeekStart.plusDays(4).atTime(16, 0));
        reportRepository.save(carolV1);
        reviewActionRepository.save(ReviewAction.builder()
                .report(carolV1)
                .reviewer(manager)
                .action(ReviewActionType.REQUESTED_CHANGES)
                .comment("Please add more detail to the blocker section and fill in actual hours spent.")
                .createdAt(lastWeekStart.plusDays(5).atTime(9, 30))
                .build());

        Report carolV2 = buildReport(carol, clientPortal, lastWeekStart, ReportStatus.DRAFT, 2, carolV1);
        reportRepository.save(carolV2);

        // Alice: a DRAFT for the current week, not yet submitted.
        reportRepository.save(buildReport(alice, internalTools, thisWeekStart, ReportStatus.DRAFT, 1, null));
    }

    private User teamMember(String name, String email) {
        return User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(SEED_PASSWORD))
                .role(Role.TEAM_MEMBER)
                .build();
    }

    private Report buildReport(
            User user, Project project, LocalDate weekStart, ReportStatus status, int versionNumber, Report parent) {
        Report report = Report.builder()
                .user(user)
                .project(project)
                .weekStartDate(weekStart)
                .weekEndDate(weekStart.plusDays(6))
                .status(status)
                .versionNumber(versionNumber)
                .parentReport(parent)
                .notes("Seed data report for demo purposes.")
                .build();

        report.addTaskEntry(TaskEntry.builder()
                .taskName("Implement login flow")
                .priority("High")
                .plannedPercent(100)
                .actualPercent(80)
                .status("In progress")
                .timePlanned(8.0)
                .timeSpent(6.5)
                .deliverable("Login page + auth wiring")
                .entryType(TaskEntryType.COMPLETED)
                .build());
        report.addTaskEntry(TaskEntry.builder()
                .taskName("Write API integration tests")
                .priority("Medium")
                .plannedPercent(50)
                .actualPercent(0)
                .status("Not started")
                .timePlanned(4.0)
                .timeSpent(0.0)
                .deliverable("Test suite")
                .entryType(TaskEntryType.PLANNED_NEXT_WEEK)
                .build());

        report.addBlocker(Blocker.builder()
                .description("Waiting on design review for the settings page")
                .isKeyIssue(true)
                .build());

        report.addAchievement(Achievement.builder()
                .description("Shipped the onboarding flow ahead of schedule")
                .isKeyAchievement(true)
                .build());

        report.addHoursByType(HoursByType.builder().taskType("Development").hours(6.5).build());
        report.addHoursByType(HoursByType.builder().taskType("Code review").hours(1.5).build());

        return report;
    }
}
