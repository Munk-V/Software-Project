// Vedanta s245010
package com.planner.unit;

// White-box unit tests for ProjectService.
// Covers UC1, UC9, and UC10.
// Test design follows equivalence partitioning and boundary value analysis (Report 2, Section 3.2).
// Each test is named: methodName_inputCondition_expectedBehaviour

import com.planner.domain.Project;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.ProjectRepository;
import com.planner.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectServiceTest {

    private ProjectService projectService;

    // Runs before each test, creates a fresh service with empty in-memory repositories
    @BeforeEach
    public void setUp() {
        projectService = new ProjectService(new ProjectRepository(), new DeveloperRepository());
    }

    // TC1: valid name, the returned project should have exactly the name that was given
    @Test
    public void createProject_validName_returnsProjectWithCorrectName() {
        Project project = projectService.createProject("WebShop");
        assertEquals("WebShop", project.getName());
    }

    // TC2: valid name, the project should be retrievable from the repository after creation
    @Test
    public void createProject_validName_projectStoredInRepository() {
        Project project = projectService.createProject("WebShop");
        Project found = projectService.getProject(project.getId());
        assertNotNull(found);
        assertEquals("WebShop", found.getName());
    }

    // TC3: valid name, a non-null ID must be automatically generated for the project
    @Test
    public void createProject_validName_idIsNotNull() {
        Project project = projectService.createProject("WebShop");
        assertNotNull(project.getId());
    }

    // TC4: empty string is not a valid project name, should throw
    @Test
    public void createProject_emptyName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(""));
    }

    // TC5: a name with only spaces is blank and should be rejected
    @Test
    public void createProject_blankName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> projectService.createProject("   "));
    }

    // TC6: null is not a valid name, should throw
    @Test
    public void createProject_nullName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(null));
    }

    // TC7: each project must get a unique ID even if created in the same session
    @Test
    public void createProject_multipleProjects_eachGetsUniqueId() {
        Project p1 = projectService.createProject("Alpha");
        Project p2 = projectService.createProject("Beta");
        assertNotEquals(p1.getId(), p2.getId());
    }


    // TC1: a valid future week should be stored as the project deadline
    @Test
    public void setDeadline_validWeek_deadlineIsSet() {
        Project project = projectService.createProject("WebShop");
        projectService.setDeadline(project.getId(), 20, 2026);
        assertEquals(20, projectService.getProject(project.getId()).getDeadlineWeek());
        assertEquals(2026, projectService.getProject(project.getId()).getDeadlineYear());
    }

    // TC2: week 0 is below the valid range of 1–53 and must be rejected
    @Test
    public void setDeadline_week0_throwsIllegalArgumentException() {
        Project project = projectService.createProject("WebShop");
        assertThrows(IllegalArgumentException.class, () -> projectService.setDeadline(project.getId(), 0, 2026));
    }

    // TC3: week 54 is above the valid range of 1–53 and must be rejected
    @Test
    public void setDeadline_week54_throwsIllegalArgumentException() {
        Project project = projectService.createProject("WebShop");
        assertThrows(IllegalArgumentException.class, () -> projectService.setDeadline(project.getId(), 54, 2026));
    }

    // TC4: week 1 is the lower boundary of the valid range and should be accepted
    @Test
    public void setDeadline_week1_valid() {
        Project project = projectService.createProject("WebShop");
        assertDoesNotThrow(() -> projectService.setDeadline(project.getId(), 1, 2027));
    }

    // TC5: week 53 is the upper boundary of the valid range and should be accepted
    @Test
    public void setDeadline_week53_valid() {
        Project project = projectService.createProject("WebShop");
        assertDoesNotThrow(() -> projectService.setDeadline(project.getId(), 53, 2026));
    }

    // TC6: setting a deadline on a project that does not exist should throw
    @Test
    public void setDeadline_unknownProjectId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> projectService.setDeadline("99999", 10, 2026));
    }


    // TC1: a project with no activities has no budgeted hours so progress must be 0%
    @Test
    public void getProjectProgress_noActivities_returnsZero() {
        Project project = projectService.createProject("Empty");
        assertEquals(0.0, projectService.getProjectProgress(project.getId()), 0.001);
    }
}
