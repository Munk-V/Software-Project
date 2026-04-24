package com.planner.unit;
// Viktor

import com.planner.domain.Activity;
import com.planner.domain.Project;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.ProjectRepository;
import com.planner.service.ActivityService;
import com.planner.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ActivityServiceTest {

    private ActivityService activityService;
    private ProjectService projectService;
    private String projectId;

    @BeforeEach
    public void setUp() {
        ProjectRepository projectRepository = new ProjectRepository();
        DeveloperRepository developerRepository = new DeveloperRepository();
        activityService = new ActivityService(projectRepository, developerRepository);
        projectService = new ProjectService(projectRepository, developerRepository);
        Project project = projectService.createProject("TestProject");
        projectId = project.getId();
    }

    // ── setActivityDetails ───────────────────────────────────────────────────────

    @Test
    public void setActivityDetails_validInput_setsAllFields() {
        activityService.createActivity(projectId, "Design");
        activityService.setActivityDetails(projectId, "Design", 40.0, 10, 2026, 12, 2026);
        Activity a = activityService.getActivity(projectId, "Design");
        assertEquals(40.0, a.getBudgetedHours(), 0.001);
        assertEquals(10, a.getStartWeek());
        assertEquals(2026, a.getStartYear());
        assertEquals(12, a.getEndWeek());
        assertEquals(2026, a.getEndYear());
    }

    @Test
    public void setActivityDetails_negativeBudget_throwsIllegalArgumentException() {
        activityService.createActivity(projectId, "Design");
        assertThrows(IllegalArgumentException.class,
                () -> activityService.setActivityDetails(projectId, "Design", -5.0, 10, 2026, 12, 2026));
    }

    @Test
    public void setActivityDetails_zeroBudget_isAllowed() {
        activityService.createActivity(projectId, "Design");
        assertDoesNotThrow(
                () -> activityService.setActivityDetails(projectId, "Design", 0.0, 0, 0, 0, 0));
    }

    @Test
    public void setActivityDetails_startWeekAfterEndWeek_throwsIllegalArgumentException() {
        activityService.createActivity(projectId, "Design");
        assertThrows(IllegalArgumentException.class,
                () -> activityService.setActivityDetails(projectId, "Design", 10.0, 15, 2026, 10, 2026));
    }

    @Test
    public void setActivityDetails_startWeekOutOfRange_throwsIllegalArgumentException() {
        activityService.createActivity(projectId, "Design");
        assertThrows(IllegalArgumentException.class,
                () -> activityService.setActivityDetails(projectId, "Design", 10.0, 54, 2026, 55, 2026));
    }

    @Test
    public void setActivityDetails_endWeekOutOfRange_throwsIllegalArgumentException() {
        activityService.createActivity(projectId, "Design");
        assertThrows(IllegalArgumentException.class,
                () -> activityService.setActivityDetails(projectId, "Design", 10.0, 1, 2026, 54, 2026));
    }

    @Test
    public void setActivityDetails_sameStartAndEndWeek_isAllowed() {
        activityService.createActivity(projectId, "Design");
        assertDoesNotThrow(
                () -> activityService.setActivityDetails(projectId, "Design", 10.0, 10, 2026, 10, 2026));
    }

    @Test
    public void setActivityDetails_startEndAcrossYears_isAllowed() {
        activityService.createActivity(projectId, "Design");
        assertDoesNotThrow(
                () -> activityService.setActivityDetails(projectId, "Design", 10.0, 50, 2025, 5, 2026));
    }

    // ── createActivity ───────────────────────────────────────────────────────────

    @Test
    public void createActivity_validInput_activityAddedToProject() {
        activityService.createActivity(projectId, "Requirements");
        Activity a = activityService.getActivity(projectId, "Requirements");
        assertNotNull(a);
        assertEquals("Requirements", a.getName());
    }

    @Test
    public void createActivity_emptyName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> activityService.createActivity(projectId, ""));
    }

    @Test
    public void createActivity_unknownProject_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> activityService.createActivity("99999", "Design"));
    }
}
