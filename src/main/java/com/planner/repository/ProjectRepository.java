package com.planner.repository;
// Vedanta

import com.planner.domain.Activity;
import com.planner.domain.Project;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectRepository implements IProjectRepository {

    private final List<Project> projects = new ArrayList<>();
    private int projectCounter = 1;

    public String generateProjectId() {
        int year = Year.now().getValue() % 100;
        String id = String.format("%02d%03d", year, projectCounter);
        projectCounter++;
        return id;
    }

    public void add(Project project) {
        projects.add(project);
    }

    public Optional<Project> findById(String id) {
        return projects.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public List<Project> findAll() {
        return new ArrayList<>(projects);
    }

    public Optional<Activity> findActivity(String projectId, String activityName) {
        return findById(projectId)
                .flatMap(p -> p.getActivities().stream()
                        .filter(a -> a.getName().equals(activityName))
                        .findFirst());
    }
}
