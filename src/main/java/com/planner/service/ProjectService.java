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

    public void assignProjectLeader(String projectId, String developerInitials) {
        Project project = getProject(projectId);
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));
        project.setProjectLeader(developer);
    }
}
