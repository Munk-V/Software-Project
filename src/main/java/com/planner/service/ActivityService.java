// Viktor
package com.planner.service;

import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.Project;
import com.planner.repository.IDeveloperRepository;
import com.planner.repository.IProjectRepository;

import java.util.List;
import java.util.Optional;

// Handles all business logic for activities in a project
public class ActivityService {

    private final IProjectRepository projectRepository;
    private final IDeveloperRepository developerRepository;

    public ActivityService(IProjectRepository projectRepository, IDeveloperRepository developerRepository) {
        this.projectRepository = projectRepository;
        this.developerRepository = developerRepository;
    }

    // Creates a new activity inside the given project
    public Activity createActivity(String projectId, String activityName) {
        if (activityName == null || activityName.trim().isEmpty()) {
            throw new IllegalArgumentException("Activity name cannot be empty");
        }

        Optional<Project> result = projectRepository.findById(projectId);
        if (!result.isPresent()) {
            throw new IllegalArgumentException("Project not found: " + projectId);
        }
        Project project = result.get();

        Activity activity = new Activity(activityName);
        project.addActivity(activity);
        return activity;
    }

    // Sets budget and time period on an activity. Week 0 means not yet planned
    public void setActivityDetails(String projectId, String activityName,
                                   double budgetedHours, int startWeek, int startYear,
                                   int endWeek, int endYear) {
        if (budgetedHours < 0) {
            throw new IllegalArgumentException("Budgeted hours cannot be negative");
        }
        if (startWeek != 0 && (startWeek < 1 || startWeek > 53)) {
            throw new IllegalArgumentException("Start week must be between 1 and 53");
        }
        if (endWeek != 0 && (endWeek < 1 || endWeek > 53)) {
            throw new IllegalArgumentException("End week must be between 1 and 53");
        }
        // check that start is not after end. We multiply year by 100 and add week to compare two dates easily
        if (startWeek != 0 && endWeek != 0) {
            int start = startYear * 100 + startWeek;
            int end = endYear * 100 + endWeek;
            if (start > end) {
                throw new IllegalArgumentException("Start week must be before or equal to end week");
            }
        }

        assert budgetedHours >= 0 : "budgetedHours must be non-negative";
        assert startWeek == 0 || (startWeek >= 1 && startWeek <= 53) : "startWeek must be 0 or between 1 and 53";
        assert endWeek == 0 || (endWeek >= 1 && endWeek <= 53) : "endWeek must be 0 or between 1 and 53";

        Activity activity = getActivity(projectId, activityName);
        activity.setBudgetedHours(budgetedHours);
        activity.setStartWeek(startWeek);
        activity.setStartYear(startYear);
        activity.setEndWeek(endWeek);
        activity.setEndYear(endYear);

        assert activity.getBudgetedHours() == budgetedHours : "budgeted hours must match input";
        assert activity.getStartWeek() == startWeek : "start week must match input";
        assert activity.getEndWeek() == endWeek : "end week must match input";
    }

    // Looks up the developer by initials and adds them to the activity
    public void addDeveloperToActivity(String projectId, String activityName, String developerInitials) {
        Activity activity = getActivity(projectId, activityName);

        Optional<Developer> result = developerRepository.findByInitials(developerInitials);
        if (!result.isPresent()) {
            throw new IllegalArgumentException("Developer not found: " + developerInitials);
        }
        Developer developer = result.get();

        activity.addDeveloper(developer);
    }

    // Finds a specific activity in a project and throws an exception if it does not exist
    public Activity getActivity(String projectId, String activityName) {
        Optional<Activity> result = projectRepository.findActivity(projectId, activityName);
        if (!result.isPresent()) {
            throw new IllegalArgumentException("Activity not found: " + activityName);
        }
        return result.get();
    }

    // Returns all activities belonging to the given project
    public List<Activity> getActivitiesForProject(String projectId) {
        Optional<Project> result = projectRepository.findById(projectId);
        if (!result.isPresent()) {
            throw new IllegalArgumentException("Project not found: " + projectId);
        }
        return result.get().getActivities();
    }
}
