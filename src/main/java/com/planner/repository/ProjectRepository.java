package com.planner.repository;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.planner.domain.Project;

public class ProjectRepository {
    //memory storage
    private final List<Project> projects = new ArrayList<>();
    private int projectCounter = 1; // ids generation incrementally

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

    public List<Project> findAll() { // return a copy
        return new ArrayList<>(projects);
    }
}
