// Vedanta s245010
package com.planner.service;

import java.util.List;

import com.planner.domain.Developer;
import com.planner.domain.Project;
import com.planner.repository.IDeveloperRepository;
import com.planner.repository.IProjectRepository;

public class ProjectService { //LOgic 

    private final IProjectRepository projectRepository;
    private final IDeveloperRepository developerRepository;

    public ProjectService(IProjectRepository projectRepository, IDeveloperRepository developerRepository) {
        this.projectRepository = projectRepository;
        this.developerRepository = developerRepository;
    }

	public Project createProject(String name) { // Only allows strings
		if (name == null) {
			throw new IllegalArgumentException("Project name cannot be empty");
		}
		boolean hasContent = false;
		for (int i = 0; i < name.length(); i++) {
			if (name.charAt(i) != ' ') {
				hasContent = true;
			}
		}
		if (!hasContent) {
			throw new IllegalArgumentException("Project name cannot be empty");
		}
		String id = projectRepository.generateProjectId();
		Project project = new Project(id, name);
		projectRepository.add(project);
		return project;       
	}

	public Project getProject(String id) {
		Project found = null;
		for (Project p : projectRepository.findAll()) {
			if (p.getId().equals(id)) {
				found = p;
			}
		}
		if (found == null) {
			throw new IllegalArgumentException("Project not found: " + id);
		}
		return found;
	}

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public void setDeadline(String projectId, int week, int year) {
        requireNotInPast(week, year);
        Project project = getProject(projectId);
        project.setDeadline(week, year);
    }

    public void setStart(String projectId, int week, int year) {
        requireNotInPast(week, year);
        Project project = getProject(projectId);
        project.setStart(week, year);
    }

    private void requireNotInPast(int week, int year) {
        int currentYear = java.time.LocalDate.now().getYear();
        int currentWeek = java.time.LocalDate.now().get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
        if (year * 100 + week < currentYear * 100 + currentWeek) {
            throw new IllegalArgumentException("Date cannot be in the past");
        }
    }

	public double getProjectProgress(String projectId) {
		Project project = getProject(projectId);
		double budgeted = project.getTotalBudgetedHours();
		if (budgeted == 0) return 0;
		return (project.getTotalRegisteredHours() / budgeted) * 100;
	}

	public void assignProjectLeader(String projectId, String developerInitials) {
		Project project = getProject(projectId);
		Developer developer = null;
		for (Developer d : developerRepository.findAll()) {
			if (d.getInitials().equals(developerInitials)) {
				developer = d;
			}
		}
		if (developer == null) {
			throw new IllegalArgumentException("Developer not found: " + developerInitials);
		}
		project.setProjectLeader(developer);
	}
}
