package com.planner.service;
// Viktor

import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.Project;
import com.planner.repository.IDeveloperRepository;
import com.planner.repository.IProjectRepository;

import java.util.List;

public class ActivityService {

    private final IProjectRepository projectRepository;
    private final IDeveloperRepository developerRepository;

    public ActivityService(IProjectRepository projectRepository, IDeveloperRepository developerRepository) {
        this.projectRepository = projectRepository;
        this.developerRepository = developerRepository;
    }

    public Activity createActivity(String projectId, String activityName) {
        if (activityName == null || activityName.isBlank()) {
            throw new IllegalArgumentException("Activity name cannot be empty");
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        Activity activity = new Activity(activityName);
        project.addActivity(activity);
        return activity;
    }

    public void setActivityDetails(String projectId, String activityName,
                                   double budgetedHours, int startWeek, int startYear,
                                   int endWeek, int endYear) {
        // Pre-conditions
        assert budgetedHours >= 0 : "budgetedHours must be non-negative";
        assert startWeek == 0 || (startWeek >= 1 && startWeek <= 53) : "startWeek must be 0 (unset) or between 1 and 53";
        assert endWeek == 0 || (endWeek >= 1 && endWeek <= 53) : "endWeek must be 0 (unset) or between 1 and 53";

        if (budgetedHours < 0) {
            throw new IllegalArgumentException("Budgeted hours cannot be negative");
        }
        if (startWeek != 0 && (startWeek < 1 || startWeek > 53)) {
            throw new IllegalArgumentException("Start week must be between 1 and 53");
        }
        if (endWeek != 0 && (endWeek < 1 || endWeek > 53)) {
            throw new IllegalArgumentException("End week must be between 1 and 53");
        }
        if (startWeek != 0 && endWeek != 0) {
            int start = startYear * 100 + startWeek;
            int end = endYear * 100 + endWeek;
            if (start > end) {
                throw new IllegalArgumentException("Start week must be before or equal to end week");
            }
        }

        Activity activity = getActivity(projectId, activityName);
        activity.setBudgetedHours(budgetedHours);
        activity.setStartWeek(startWeek);
        activity.setStartYear(startYear);
        activity.setEndWeek(endWeek);
        activity.setEndYear(endYear);

        // Post-conditions
        assert activity.getBudgetedHours() == budgetedHours : "budgeted hours must match input";
        assert activity.getStartWeek() == startWeek : "start week must match input";
        assert activity.getEndWeek() == endWeek : "end week must match input";
    }

    public void addDeveloperToActivity(String projectId, String activityName, String developerInitials) {
        Activity activity = getActivity(projectId, activityName);
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));
        activity.addDeveloper(developer);
    }

    public Activity getActivity(String projectId, String activityName) {
        return projectRepository.findActivity(projectId, activityName)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found: " + activityName));
    }

    public List<Activity> getActivitiesForProject(String projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId))
                .getActivities();
    }
}
