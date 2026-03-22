package com.planner.domain;

import java.util.ArrayList;
import java.util.List;

public class Project {

    private final String id;
    private final String name;
    private Developer projectLeader;
    private final List<Activity> activities = new ArrayList<>();
    private int startWeek;
    private int startYear;
    private int deadlineWeek;
    private int deadlineYear;

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

    public int getStartWeek() {
        return startWeek;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStart(int week, int year) {
        this.startWeek = week;
        this.startYear = year;
    }

    public boolean hasStart() {
        return startYear > 0;
    }

    public int getDeadlineWeek() {
        return deadlineWeek;
    }

    public int getDeadlineYear() {
        return deadlineYear;
    }

    public void setDeadline(int week, int year) {
        if (week < 1 || week > 53) {
            throw new IllegalArgumentException("Deadline week must be between 1 and 53");
        }
        this.deadlineWeek = week;
        this.deadlineYear = year;
    }

    public boolean hasDeadline() {
        return deadlineYear > 0;
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
