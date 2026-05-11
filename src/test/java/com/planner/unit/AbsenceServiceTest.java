// Viktor
package com.planner.unit;

// White-box unit tests for AbsenceService.registerAbsence.
// Covers UC6 from Report 1, Section 4.6.
// Systematically tested in Report 2, Section 3.6 using equivalence partitioning.
// idea of sick leave is always allowed even if the developer
// Each test is named: methodName_inputCondition_expectedBehaviour

import com.planner.domain.Absence;
import com.planner.domain.Project;
import com.planner.repository.AbsenceRepository;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.ProjectRepository;
import com.planner.service.AbsenceService;
import com.planner.service.ActivityService;
import com.planner.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AbsenceServiceTest {

    private AbsenceService absenceService;
    private ProjectRepository projectRepository;
    private DeveloperRepository developerRepository;

    // Runs before each test, creates fresh repositories and wires up the absence service
    @BeforeEach
    public void setUp() {
        projectRepository = new ProjectRepository();
        developerRepository = new DeveloperRepository();
        AbsenceRepository absenceRepository = new AbsenceRepository();
        absenceService = new AbsenceService(absenceRepository, developerRepository, projectRepository);
    }

    // TC1: valid vacation in a future free period, absence is stored and returned correctly
    @Test
    public void registerAbsence_validInput_returnsAbsence() {
        Absence absence = absenceService.registerAbsence("huba", Absence.Type.VACATION, 20, 2026, 22, 2026);
        assertNotNull(absence);
        assertEquals(Absence.Type.VACATION, absence.getType());
        assertEquals(20, absence.getStartWeek());
        assertEquals(22, absence.getEndWeek());
    }

    // TC2: null type is not a valid absence type, should throw immediately
    @Test
    public void registerAbsence_nullType_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> absenceService.registerAbsence("huba", null, 10, 2026, 12, 2026));
    }

    // TC3: week 0 is below the valid range of 1–53, should throw
    @Test
    public void registerAbsence_startWeekZero_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> absenceService.registerAbsence("huba", Absence.Type.VACATION, 0, 2026, 12, 2026));
    }

    // TC4: week 54 is above the valid range of 1–53, should throw
    @Test
    public void registerAbsence_startWeekOutOfRange_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> absenceService.registerAbsence("huba", Absence.Type.VACATION, 54, 2026, 55, 2026));
    }

    // TC5: end week 54 is also above the valid range, should throw
    @Test
    public void registerAbsence_endWeekOutOfRange_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> absenceService.registerAbsence("huba", Absence.Type.VACATION, 10, 2026, 54, 2026));
    }

    // TC6: start week 25 is after end week 20 in the same year, ordering check should throw
    @Test
    public void registerAbsence_startAfterEnd_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> absenceService.registerAbsence("huba", Absence.Type.VACATION, 25, 2026, 20, 2026));
    }

    // TC7: developer "ZZZ" does not exist in the system, should throw
    @Test
    public void registerAbsence_unknownDeveloper_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> absenceService.registerAbsence("ZZZ", Absence.Type.VACATION, 20, 2026, 22, 2026));
    }

    // TC8: developer is assigned to an activity in the requested period
    // vacation must be denied because the developer is already booked
    @Test
    public void registerAbsence_developerBusyInPeriod_throwsIllegalArgumentException() {
        ProjectService projectService = new ProjectService(projectRepository, developerRepository);
        ActivityService activityService = new ActivityService(projectRepository, developerRepository);
        Project project = projectService.createProject("BusyProject");
        activityService.createActivity(project.getId(), "BusyTask");
        activityService.setActivityDetails(project.getId(), "BusyTask", 10.0, 20, 2026, 22, 2026);
        activityService.addDeveloperToActivity(project.getId(), "BusyTask", "huba");

        // Vacation in week 21 overlaps with the activity weeks 20–22, must be denied
        assertThrows(IllegalArgumentException.class,
                () -> absenceService.registerAbsence("huba", Absence.Type.VACATION, 21, 2026, 21, 2026));
    }

    // TC9: sick leave always overrides activity conflicts, even if the developer is busy,
    // sick leave is allowed 
    @Test
    public void registerAbsence_sickLeaveWhenDeveloperBusy_returnsAbsence() {
        ProjectService projectService = new ProjectService(projectRepository, developerRepository);
        ActivityService activityService = new ActivityService(projectRepository, developerRepository);

        Project project = projectService.createProject("BusyProject");
        activityService.createActivity(project.getId(), "BusyTask");
        activityService.setActivityDetails(project.getId(), "BusyTask", 10.0, 20, 2026, 22, 2026);
        activityService.addDeveloperToActivity(project.getId(), "BusyTask", "huba");

        // Sick leave in week 21, overlaps with activity but must still be registered
        Absence absence = absenceService.registerAbsence(
                "huba", Absence.Type.SICK_LEAVE, 21, 2026, 21, 2026);

        assertNotNull(absence);
        assertEquals(Absence.Type.SICK_LEAVE, absence.getType());
    }
    // TC10: date in the past should throw
    @Test
    public void registerAbsence_dateInPast_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> absenceService.registerAbsence(
                        "huba", Absence.Type.VACATION, 1, 2000, 2, 2000));
    }
}
