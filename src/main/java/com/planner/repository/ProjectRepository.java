package com.planner.repository;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.planner.domain.Activity;
import com.planner.domain.Project;

public class ProjectRepository implements IProjectRepository{
    //memory storage
    private final List<Project> projects = new ArrayList<>();
    private int projectCounter = 1; // ids generation incrementally

    @Override
    public String generateProjectId() { // the projects need IDs, because we cant just rely on names
        int year = Year.now().getValue() % 100;
        String yearPart; // zero padding 
        if (year < 10) {
            yearPart = "0" + year;
        } else {
            yearPart = "" + year;
        }

        String counterPart; // zero padding 
        if (projectCounter < 10) {
            counterPart = "00" + projectCounter;
        } else if (projectCounter < 100) {
            counterPart = "0" + projectCounter;
        } else {
            counterPart = "" + projectCounter;
        }

        String id = yearPart + counterPart;
        projectCounter++;
        return id;
    }
    @Override
    public void add(Project project) {
        projects.add(project);
    }
    @Override
    public Optional<Project> findById(String id) {
        for (Project p : projects) {
            if (p.getId().equals(id)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }
    @Override
    public List<Project> findAll() { // return a copy
        return new ArrayList<>(projects);
    }
    @Override
	public Optional<Activity> findActivity(String projectId, String activityName) {
		for (Project p : projects) {
			if (p.getId().equals(projectId)) {
				for (Activity a : p.getActivities()) {
					if (a.getName().equals(activityName)) {
						return Optional.of(a);
					}
				}
			}
		}
		return Optional.empty();
	}
    @Override
    public void removeActivity(String projectId, String activityName) {
        for (Project p : projects) {
            if (p.getId().equals(projectId)) {
                p.getActivities().removeIf(a -> a.getName().equals(activityName));
                return;
            }
        }
    }
}
