package com.planner.repository;
// Vedanta

import com.planner.domain.Activity;
import com.planner.domain.Project;

import java.util.List;
import java.util.Optional;

public interface IProjectRepository {
    String generateProjectId();
    void add(Project project);
    Optional<Project> findById(String id);
    List<Project> findAll();
    Optional<Activity> findActivity(String projectId, String activityName);
}
