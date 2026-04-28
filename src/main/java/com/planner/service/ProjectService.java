package com.planner.service;
// Vedanta

import com.planner.domain.Developer;
import com.planner.domain.Project;
import com.planner.repository.IDeveloperRepository;
import com.planner.repository.IProjectRepository;

import java.util.List;

public class ProjectService {

    private final IProjectRepository projectRepository;
    private final IDeveloperRepository developerRepository;

    public ProjectService(IProjectRepository projectRepository, IDeveloperRepository developerRepository) {
        this.projectRepository = projectRepository;
        this.developerRepository = developerRepository;
    }

    public Project createProject(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }
        // Pre-conditions (hold after defensive validation above)
        assert name != null : "name must not be null";
        assert !name.isBlank() : "name must not be blank";

        String id = projectRepository.generateProjectId();
        Project project = new Project(id, name);
        projectRepository.add(project);

        // Post-conditions
        assert project != null : "created project must not be null";
        assert project.getName().equals(name) : "project name must match input";
        assert projectRepository.findById(project.getId()).isPresent() : "project must be stored in repository";

        return project;
    }

    public Project getProject(String id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public void setDeadline(String projectId, int week, int year) {
        Project project = getProject(projectId);
        project.setDeadline(week, year);
    }

    public void setStart(String projectId, int week, int year) {
        Project project = getProject(projectId);
        project.setStart(week, year);
    }

    public double getProjectProgress(String projectId) {
        Project project = getProject(projectId);
        double budgeted = project.getTotalBudgetedHours();
        if (budgeted == 0) return 0;
        return (project.getTotalRegisteredHours() / budgeted) * 100;
    }

    public void assignProjectLeader(String projectId, String developerInitials) {
        Project project = getProject(projectId);
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));
        project.setProjectLeader(developer);
    }
}
