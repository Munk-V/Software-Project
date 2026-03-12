package com.planner.domain;

import java.util.ArrayList;
import java.util.List;

public class Project {

    private final String id;
    private final String name;
    private Developer projectLeader;
    private final List<Activity> activities = new ArrayList<>();

    public Project(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Developer getProjectLeader() {
        return projectLeader;
    }

    public void setProjectLeader(Developer projectLeader) {
        this.projectLeader = projectLeader;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    public double getTotalBudgetedHours() {
        return activities.stream()
                .mapToDouble(Activity::getBudgetedHours)
                .sum();
    }

    public double getTotalRegisteredHours() {
        return activities.stream()
                .mapToDouble(Activity::getTotalRegisteredHours)
                .sum();
    }
}
