// Viktor
package com.planner.unit;

import com.planner.domain.Absence;
import com.planner.domain.Project;
import com.planner.repository.AbsenceRepository;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.ProjectRepository;
import com.planner.service.AbsenceService;
import com.planner.service.ActivityService;
import com.planner.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// White-box unit tests for AbsenceService.registerAbsence
public class AbsenceServiceTest {

    private AbsenceService absenceService;
    private ProjectRepository projectRepository;
    private DeveloperRepository developerRepository;

    @BeforeEach
    public void setUp() {
        projectRepository = new ProjectRepository();
        developerRepository = new DeveloperRepository();
        AbsenceRepository absenceRepository = new AbsenceRepository();
        absenceService = new AbsenceService(absenceRepository, developerRepository, projectRepository);
    }

    // TC1 valid absence is stored and returned
    @Test
    @DisplayName("Register absence with valid input")
    public void registerAbsence_validInput_returnsAbsence() {
        Absence absence = absenceService.registerAbsence("huba", Absence.Type.VACATION, 10, 2026, 12, 2026);
        assertNotNull(absence);
        assertEquals(Absence.Type.VACATION, absence.getType());
        assertEquals(10, absence.getStartWeek());
        assertEquals(12, absence.getEndWeek());
    }

    // TC2 null type must be rejected
    @Test
    @DisplayName("Cannot register absence with null type")
    public void registerAbsence_nullType_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> absenceService.registerAbsence("huba", null, 10, 2026, 12, 2026));
    }

    // TC3 week 0 is below the valid range of 1 to 53
    @Test
    @DisplayName("Cannot register absence with start week zero")
    public void registerAbsence_startWeekZero_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> absenceService.registerAbsence("huba", Absence.Type.VACATION, 0, 2026, 12, 2026));
    }

    // TC4 start week 54 is above the valid range
    @Test
    @DisplayName("Cannot register absence with start week out of range")
    public void registerAbsence_startWeekOutOfRange_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> absenceService.registerAbsence("huba", Absence.Type.VACATION, 54, 2026, 55, 2026));
    }

    // TC5 end week 54 is also above the valid range
    @Test
    @DisplayName("Cannot register absence with end week out of range")
    public void registerAbsence_endWeekOutOfRange_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> absenceService.registerAbsence("huba", Absence.Type.VACATION, 10, 2026, 54, 2026));
    }

    // TC6 start week after end week in the same year should throw
    @Test
    @DisplayName("Cannot register absence when start week is after end week")
    public void registerAbsence_startAfterEnd_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> absenceService.registerAbsence("huba", Absence.Type.VACATION, 15, 2026, 10, 2026));
    }

    // TC7 developer not in the system should throw
    @Test
    @DisplayName("Cannot register absence for a non-existent developer")
    public void registerAbsence_unknownDeveloper_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> absenceService.registerAbsence("ZZZ", Absence.Type.VACATION, 10, 2026, 12, 2026));
    }

    // TC8 developer assigned to an activity in that period, absence is denied
    @Test
    @DisplayName("Absence is denied if developer is busy with an activity")
    public void registerAbsence_developerBusyInPeriod_throwsIllegalArgumentException() {
        ProjectService projectService = new ProjectService(projectRepository, developerRepository);
        ActivityService activityService = new ActivityService(projectRepository, developerRepository);
        Project project = projectService.createProject("BusyProject");
        activityService.createActivity(project.getId(), "BusyTask");
        activityService.setActivityDetails(project.getId(), "BusyTask", 10.0, 10, 2026, 12, 2026);
        activityService.addDeveloperToActivity(project.getId(), "BusyTask", "huba");

        assertThrows(IllegalArgumentException.class,
                () -> absenceService.registerAbsence("huba", Absence.Type.VACATION, 11, 2026, 11, 2026));
    }
}
