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

        String yearPart;
        if (year < 10) {
            yearPart = "0" + year;
        } else {
            yearPart = "" + year;
        }

        String counterPart;
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

    public void add(Project project) {
        projects.add(project);
    }

    public Optional<Project> findById(String id) {
        for (Project p : projects) {
            if (p.getId().equals(id)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    public List<Project> findAll() { // return a copy
        return new ArrayList<>(projects);
    }
}
