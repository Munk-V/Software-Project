// Viktor
package com.planner.unit;

// White-box unit tests for ActivityService.
// Covers UC2 (Create Activity) and UC3 (Add Developer to Activity) from Report 1.
// setActivityDetails is systematically tested in Report 2, Section 3.3
//   using equivalence partitioning and boundary value analysis (8 test cases, Table 4).
// addDeveloperToActivity is systematically tested in Report 2, Section 3.4 (Table 6).
// Design by Contract for both methods is specified in Report 2, Section 4.2 and 4.3.
// Each test is named: methodName_inputCondition_expectedBehaviour

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

    // Runs before each test — creates fresh repositories and a test project
    @BeforeEach
    public void setUp() {
        ProjectRepository projectRepository = new ProjectRepository();
        DeveloperRepository developerRepository = new DeveloperRepository();
        activityService = new ActivityService(projectRepository, developerRepository);
        projectService = new ProjectService(projectRepository, developerRepository);
        Project project = projectService.createProject("TestProject");
        projectId = project.getId();
    }

    // ── setActivityDetails (UC2) ─────────────────────────────────────────────────

    // TC1: valid budget and future weeks — all five fields should be saved on the activity
    @Test
    public void setActivityDetails_validInput_setsAllFields() {
        activityService.createActivity(projectId, "Design");
        activityService.setActivityDetails(projectId, "Design", 40.0, 20, 2026, 22, 2026);
        Activity a = activityService.getActivity(projectId, "Design");
        assertEquals(40.0, a.getBudgetedHours(), 0.001);
        assertEquals(20, a.getStartWeek());
        assertEquals(2026, a.getStartYear());
        assertEquals(22, a.getEndWeek());
        assertEquals(2026, a.getEndYear());
    }

    // TC2: negative budget is not allowed — should throw
    @Test
    public void setActivityDetails_negativeBudget_throwsIllegalArgumentException() {
        activityService.createActivity(projectId, "Design");
        assertThrows(IllegalArgumentException.class,
                () -> activityService.setActivityDetails(projectId, "Design", -5.0, 20, 2026, 22, 2026));
    }

    // TC3: budget 0 with unset weeks (0/0) is valid — means "planned but not scheduled yet"
    @Test
    public void setActivityDetails_zeroBudget_isAllowed() {
        activityService.createActivity(projectId, "Design");
        assertDoesNotThrow(
                () -> activityService.setActivityDetails(projectId, "Design", 0.0, 0, 0, 0, 0));
    }

    // TC4: same start and end week is a valid period (single-week activity) — boundary test
    @Test
    public void setActivityDetails_sameStartAndEndWeek_isAllowed() {
        activityService.createActivity(projectId, "Design");
        assertDoesNotThrow(
                () -> activityService.setActivityDetails(projectId, "Design", 10.0, 20, 2026, 20, 2026));
    }

    // TC5: week 54 is above the valid range of 1–53 — should throw
    @Test
    public void setActivityDetails_startWeekOutOfRange_throwsIllegalArgumentException() {
        activityService.createActivity(projectId, "Design");
        assertThrows(IllegalArgumentException.class,
                () -> activityService.setActivityDetails(projectId, "Design", 10.0, 54, 2026, 55, 2026));
    }

    // TC6: end week 54 is also out of range — should throw
    @Test
    public void setActivityDetails_endWeekOutOfRange_throwsIllegalArgumentException() {
        activityService.createActivity(projectId, "Design");
        assertThrows(IllegalArgumentException.class,
                () -> activityService.setActivityDetails(projectId, "Design", 10.0, 1, 2026, 54, 2026));
    }

    // TC7: start week after end week in the same year — ordering check should throw
    @Test
    public void setActivityDetails_startWeekAfterEndWeek_throwsIllegalArgumentException() {
        activityService.createActivity(projectId, "Design");
        assertThrows(IllegalArgumentException.class,
                () -> activityService.setActivityDetails(projectId, "Design", 10.0, 15, 2026, 10, 2026));
    }

    // TC8: start in week 50 of 2026 and end in week 5 of 2027 — year must be part of the
    // comparison (encoded as year*100+week) so this cross-year period should be accepted
    @Test
    public void setActivityDetails_startEndAcrossYears_isAllowed() {
        activityService.createActivity(projectId, "Design");
        assertDoesNotThrow(
                () -> activityService.setActivityDetails(projectId, "Design", 10.0, 50, 2026, 5, 2027));
    }

    // ── addDeveloperToActivity (UC3) ─────────────────────────────────────────────

    // TC1: developer "huba" exists and is successfully assigned to the activity
    @Test
    public void addDeveloperToActivity_validInput_developerAssignedToActivity() {
        activityService.createActivity(projectId, "Design");
        activityService.addDeveloperToActivity(projectId, "Design", "huba");
        Activity a = activityService.getActivity(projectId, "Design");
        assertEquals(1, a.getAssignedDevelopers().size());
    }

    // TC2: developer "ZZZ" does not exist — should throw
    @Test
    public void addDeveloperToActivity_unknownDeveloper_throwsIllegalArgumentException() {
        activityService.createActivity(projectId, "Design");
        assertThrows(IllegalArgumentException.class,
                () -> activityService.addDeveloperToActivity(projectId, "Design", "ZZZ"));
    }

    // TC3: activity "NoSuchActivity" does not exist — should throw
    @Test
    public void addDeveloperToActivity_unknownActivity_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> activityService.addDeveloperToActivity(projectId, "NoSuchActivity", "huba"));
    }

    // ── createActivity (UC2) ─────────────────────────────────────────────────────

    // TC1: valid name — activity is added to the project and can be retrieved
    @Test
    public void createActivity_validInput_activityAddedToProject() {
        activityService.createActivity(projectId, "Requirements");
        Activity a = activityService.getActivity(projectId, "Requirements");
        assertNotNull(a);
        assertEquals("Requirements", a.getName());
    }

    // TC2: empty string is not a valid activity name — should throw
    @Test
    public void createActivity_emptyName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> activityService.createActivity(projectId, ""));
    }

    // TC3: project ID "99999" does not exist — should throw
    @Test
    public void createActivity_unknownProject_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> activityService.createActivity("99999", "Design"));
    }
}
