package com.planner.service;

import com.planner.domain.Developer;
import com.planner.domain.Project;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.ProjectRepository;

import java.util.List;

public class ProjectService {

    private final ProjectRepository projectRepository;
    private final DeveloperRepository developerRepository;

    public ProjectService(ProjectRepository projectRepository, DeveloperRepository developerRepository) {
        this.projectRepository = projectRepository;
        this.developerRepository = developerRepository;
    }

    public Project createProject(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }
        String id = projectRepository.generateProjectId();
        Project project = new Project(id, name);
        projectRepository.add(project);
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
