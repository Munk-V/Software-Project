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
		if (name == null) { // check to see if the slot is left empty
			throw new IllegalArgumentException("Project name cannot be empty");
		}
		boolean hasContent = false; // check to see if its just spaces
		for (int i = 0; i < name.length(); i++) {
			if (name.charAt(i) != ' ') {
				hasContent = true;
			}
		}
		if (!hasContent) {
			throw new IllegalArgumentException("Project name cannot be empty");
		}
		String id = projectRepository.generateProjectId(); // call upon the Projet ID maker from the repository
		Project project = new Project(id, name); // Tuple the name and the id together
		projectRepository.add(project);
		return project;       
	}

	public Project getProject(String id) {
		Project found = null;
		for (Project p : projectRepository.findAll()) { //find the project using the ID
			if (p.getId().equals(id)) { // if there is a mathc, then thats the one
				found = p;
			}
		}
		if (found == null) {
			throw new IllegalArgumentException("Project not found: " + id);
		}
		return found;
	}

    public List<Project> getAllProjects() { // just get them all
        return projectRepository.findAll();
    }

    public void setDeadline(String projectId, int week, int year) { // set date
        requireNotInPast(week, year);
        Project project = getProject(projectId);
        if (project.hasStart() && year * 100 + week < project.getStartYear() * 100 + project.getStartWeek()) {  // ensure that the start date isnt after the end date
            throw new IllegalArgumentException("Deadline cannot be before project start");
        }
        project.setDeadline(week, year); // put into tuple
    }

    public void setStart(String projectId, int week, int year) { 
        requireNotInPast(week, year);
        Project project = getProject(projectId);
        if (project.hasDeadline() && year * 100 + week > project.getDeadlineYear() * 100 + project.getDeadlineWeek()) {
            throw new IllegalArgumentException("Start cannot be after project deadline");
        }
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
