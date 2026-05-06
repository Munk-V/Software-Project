// Viktor
package com.planner.unit;

import com.planner.domain.Activity;
import com.planner.domain.Project;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.ProjectRepository;
import com.planner.service.ActivityService;
import com.planner.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// White-box unit tests for ActivityService
// Tests are based on equivalence partitioning and boundary value analysis
public class ActivityServiceTest {

    private ActivityService activityService;
    private ProjectService projectService;
    private String projectId;

    // Runs before each test and sets up fresh repositories and a test project
    @BeforeEach
    public void setUp() {
        ProjectRepository projectRepository = new ProjectRepository();
        DeveloperRepository developerRepository = new DeveloperRepository();
        activityService = new ActivityService(projectRepository, developerRepository);
        projectService = new ProjectService(projectRepository, developerRepository);
        Project project = projectService.createProject("TestProject");
        projectId = project.getId();
    }

    // TC1 valid input. All five fields should be saved on the activity
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

    // TC2 negative budget is not allowed
    @Test
    public void setActivityDetails_negativeBudget_throwsIllegalArgumentException() {
        activityService.createActivity(projectId, "Design");
        assertThrows(IllegalArgumentException.class,
                () -> activityService.setActivityDetails(projectId, "Design", -5.0, 10, 2026, 12, 2026));
    }

    // TC3 zero budget with unset weeks is a valid input
    @Test
    public void setActivityDetails_zeroBudget_isAllowed() {
        activityService.createActivity(projectId, "Design");
        assertDoesNotThrow(
                () -> activityService.setActivityDetails(projectId, "Design", 0.0, 0, 0, 0, 0));
    }

    // TC7 start week after end week in the same year should throw an exception
    @Test
    public void setActivityDetails_startWeekAfterEndWeek_throwsIllegalArgumentException() {
        activityService.createActivity(projectId, "Design");
        assertThrows(IllegalArgumentException.class,
                () -> activityService.setActivityDetails(projectId, "Design", 10.0, 15, 2026, 10, 2026));
    }

    // TC5 week 54 is above the valid range of 1 to 53
    @Test
    public void setActivityDetails_startWeekOutOfRange_throwsIllegalArgumentException() {
        activityService.createActivity(projectId, "Design");
        assertThrows(IllegalArgumentException.class,
                () -> activityService.setActivityDetails(projectId, "Design", 10.0, 54, 2026, 55, 2026));
    }

    // TC6 end week 54 is also out of range
    @Test
    public void setActivityDetails_endWeekOutOfRange_throwsIllegalArgumentException() {
        activityService.createActivity(projectId, "Design");
        assertThrows(IllegalArgumentException.class,
                () -> activityService.setActivityDetails(projectId, "Design", 10.0, 1, 2026, 54, 2026));
    }

    // TC4 boundary test. Week 1 and week 53 are both valid boundary values
    @Test
    public void setActivityDetails_sameStartAndEndWeek_isAllowed() {
        activityService.createActivity(projectId, "Design");
        assertDoesNotThrow(
                () -> activityService.setActivityDetails(projectId, "Design", 10.0, 10, 2026, 10, 2026));
    }

    // TC8 start in 2025 and end in 2026. Checks that the year is part of the comparison
    @Test
    public void setActivityDetails_startEndAcrossYears_isAllowed() {
        activityService.createActivity(projectId, "Design");
        assertDoesNotThrow(
                () -> activityService.setActivityDetails(projectId, "Design", 10.0, 50, 2025, 5, 2026));
    }

    // Valid name. The activity should be findable in the project afterwards
    @Test
    public void createActivity_validInput_activityAddedToProject() {
        activityService.createActivity(projectId, "Requirements");
        Activity a = activityService.getActivity(projectId, "Requirements");
        assertNotNull(a);
        assertEquals("Requirements", a.getName());
    }

    // Empty string is not a valid activity name
    @Test
    public void createActivity_emptyName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> activityService.createActivity(projectId, ""));
    }

    // Project id 99999 does not exist so it should throw an exception
    @Test
    public void createActivity_unknownProject_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> activityService.createActivity("99999", "Design"));
    }
}
