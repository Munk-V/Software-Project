// Viktor
package com.planner.unit;

import com.planner.domain.TimeRegistration;
import com.planner.domain.Project;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.ProjectRepository;
import com.planner.service.ActivityService;
import com.planner.service.ProjectService;
import com.planner.service.TimeRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// White-box unit tests for TimeRegistrationService.registerTime
public class TimeRegistrationServiceTest {

    private TimeRegistrationService timeService;
    private String projectId;
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);

    // Sets up a project with one activity and assigns "huba" to it
    @BeforeEach
    public void setUp() {
        ProjectRepository projectRepository = new ProjectRepository();
        DeveloperRepository developerRepository = new DeveloperRepository();
        timeService = new TimeRegistrationService(projectRepository, developerRepository);
        ProjectService projectService = new ProjectService(projectRepository, developerRepository);
        ActivityService activityService = new ActivityService(projectRepository, developerRepository);
        Project project = projectService.createProject("TestProject");
        projectId = project.getId();
        activityService.createActivity(projectId, "Design");
        activityService.addDeveloperToActivity(projectId, "Design", "huba");
    }

    // TC1 valid input returns a time registration with the correct hours
    @Test
    @DisplayName("Register time on an activity")
    public void registerTime_validInput_returnsTimeRegistration() {
        TimeRegistration tr = timeService.registerTime("huba", projectId, "Design", DATE, 2.0);
        assertNotNull(tr);
        assertEquals(2.0, tr.getHours(), 0.001);
    }

    // TC2 zero hours is not a positive value and must be rejected
    @Test
    @DisplayName("Cannot register zero hours on an activity")
    public void registerTime_zeroHours_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> timeService.registerTime("huba", projectId, "Design", DATE, 0.0));
    }

    // TC3 negative hours must be rejected
    @Test
    @DisplayName("Cannot register negative hours on an activity")
    public void registerTime_negativeHours_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> timeService.registerTime("huba", projectId, "Design", DATE, -1.0));
    }

    // TC4 hours must be a multiple of 0.5 so 1.3 should throw
    @Test
    @DisplayName("Cannot register hours not a multiple of 0.5")
    public void registerTime_hoursNotMultipleOfHalf_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> timeService.registerTime("huba", projectId, "Design", DATE, 1.3));
    }

    // TC5 developer not in the system should throw
    @Test
    @DisplayName("Cannot register time for a non-existent developer")
    public void registerTime_unknownDeveloper_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> timeService.registerTime("ZZZ", projectId, "Design", DATE, 2.0));
    }

    // TC6 activity does not exist in the project so it should throw
    @Test
    @DisplayName("Cannot register time on a non-existent activity")
    public void registerTime_unknownActivity_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> timeService.registerTime("huba", projectId, "NoSuchActivity", DATE, 2.0));
    }
}
