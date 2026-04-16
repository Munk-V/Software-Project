package com.planner.unit;

import com.planner.domain.Project;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.ProjectRepository;
import com.planner.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectServiceTest {

    private ProjectService projectService;

    @BeforeEach
    public void setUp() {
        projectService = new ProjectService(new ProjectRepository(), new DeveloperRepository());
    }

    // ── createProject ────────────────────────────────────────────────────────────

    @Test
    public void createProject_validName_returnsProjectWithCorrectName() {
        Project project = projectService.createProject("WebShop");
        assertEquals("WebShop", project.getName());
    }

    @Test
    public void createProject_validName_projectStoredInRepository() {
        Project project = projectService.createProject("WebShop");
        Project found = projectService.getProject(project.getId());
        assertNotNull(found);
        assertEquals("WebShop", found.getName());
    }

    @Test
    public void createProject_validName_idIsNotNull() {
        Project project = projectService.createProject("WebShop");
        assertNotNull(project.getId());
    }

    @Test
    public void createProject_emptyName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(""));
    }

    @Test
    public void createProject_blankName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> projectService.createProject("   "));
    }

    @Test
    public void createProject_nullName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(null));
    }

    @Test
    public void createProject_multipleProjects_eachGetsUniqueId() {
        Project p1 = projectService.createProject("Alpha");
        Project p2 = projectService.createProject("Beta");
        assertNotEquals(p1.getId(), p2.getId());
    }

    // ── setDeadline ──────────────────────────────────────────────────────────────

    @Test
    public void setDeadline_validWeek_deadlineIsSet() {
        Project project = projectService.createProject("WebShop");
        projectService.setDeadline(project.getId(), 20, 2026);
        assertEquals(20, projectService.getProject(project.getId()).getDeadlineWeek());
        assertEquals(2026, projectService.getProject(project.getId()).getDeadlineYear());
    }

    @Test
    public void setDeadline_week0_throwsIllegalArgumentException() {
        Project project = projectService.createProject("WebShop");
        assertThrows(IllegalArgumentException.class, () -> projectService.setDeadline(project.getId(), 0, 2026));
    }

    @Test
    public void setDeadline_week54_throwsIllegalArgumentException() {
        Project project = projectService.createProject("WebShop");
        assertThrows(IllegalArgumentException.class, () -> projectService.setDeadline(project.getId(), 54, 2026));
    }

    @Test
    public void setDeadline_week1_valid() {
        Project project = projectService.createProject("WebShop");
        assertDoesNotThrow(() -> projectService.setDeadline(project.getId(), 1, 2026));
    }

    @Test
    public void setDeadline_week53_valid() {
        Project project = projectService.createProject("WebShop");
        assertDoesNotThrow(() -> projectService.setDeadline(project.getId(), 53, 2026));
    }

    @Test
    public void setDeadline_unknownProjectId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> projectService.setDeadline("99999", 10, 2026));
    }

    // ── getProjectProgress ───────────────────────────────────────────────────────

    @Test
    public void getProjectProgress_noActivities_returnsZero() {
        Project project = projectService.createProject("Empty");
        assertEquals(0.0, projectService.getProjectProgress(project.getId()), 0.001);
    }
}
