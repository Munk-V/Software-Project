// Viktor
package com.planner.unit;

// White-box unit tests for TimeRegistrationService.registerTime.
// Covers UC4 from Report 1, Section 4.4.
// Systematically tested in Report 2, Section 3.5 using equivalence partitioning.
// Each test is named: methodName_inputCondition_expectedBehaviour

import com.planner.domain.TimeRegistration;
import com.planner.domain.Project;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.ProjectRepository;
import com.planner.service.ActivityService;
import com.planner.service.ProjectService;
import com.planner.service.TimeRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class TimeRegistrationServiceTest {

    private TimeRegistrationService timeService;
    private String projectId;

    // A fixed test date, avoids dependency on the current date in assertions
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);

    // Runs before each test, sets up a project with one activity and assigns "huba" to it
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

    // TC1: valid input, registration is stored and the correct hours are returned
    @Test
    public void registerTime_validInput_returnsTimeRegistration() {
        TimeRegistration tr = timeService.registerTime("huba", projectId, "Design", DATE, 2.0);
        assertNotNull(tr);
        assertEquals(2.0, tr.getHours(), 0.001);
    }

    // TC2: zero hours is not positive and must be rejected
    @Test
    public void registerTime_zeroHours_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> timeService.registerTime("huba", projectId, "Design", DATE, 0.0));
    }

    // TC3: negative hours must be rejected
    @Test
    public void registerTime_negativeHours_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> timeService.registerTime("huba", projectId, "Design", DATE, -1.0));
    }

    // TC4: hours must be a multiple of 0.5 — 1.3 is not valid and should throw
    @Test
    public void registerTime_hoursNotMultipleOfHalf_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> timeService.registerTime("huba", projectId, "Design", DATE, 1.3));
    }

    // TC5: developer "ZZZ" does not exist in the system and should throw
    @Test
    public void registerTime_unknownDeveloper_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> timeService.registerTime("ZZZ", projectId, "Design", DATE, 2.0));
    }

    // TC6: the activity "NoSuchActivity" does not exist in the project and should throw
    @Test
    public void registerTime_unknownActivity_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> timeService.registerTime("huba", projectId, "NoSuchActivity", DATE, 2.0));
    }
}
