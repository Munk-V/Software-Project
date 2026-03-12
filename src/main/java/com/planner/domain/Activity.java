package com.planner.domain;

import java.util.ArrayList;
import java.util.List;

public class Activity {

    private final String name;
    private double budgetedHours;
    private int startWeek;
    private int startYear;
    private int endWeek;
    private int endYear;
    private final List<Developer> assignedDevelopers = new ArrayList<>();
    private final List<TimeRegistration> timeRegistrations = new ArrayList<>();

    public Activity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double getBudgetedHours() {
        return budgetedHours;
    }

    public void setBudgetedHours(double budgetedHours) {
        this.budgetedHours = budgetedHours;
    }

    public int getStartWeek() {
        return startWeek;
    }

    public void setStartWeek(int startWeek) {
        this.startWeek = startWeek;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getEndWeek() {
        return endWeek;
    }

    public void setEndWeek(int endWeek) {
        this.endWeek = endWeek;
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }

    public List<Developer> getAssignedDevelopers() {
        return assignedDevelopers;
    }

    public void addDeveloper(Developer developer) {
        if (!assignedDevelopers.contains(developer)) {
            assignedDevelopers.add(developer);
        }
    }

    public List<TimeRegistration> getTimeRegistrations() {
        return timeRegistrations;
    }

    public void addTimeRegistration(TimeRegistration registration) {
        timeRegistrations.add(registration);
    }

    public double getTotalRegisteredHours() {
        return timeRegistrations.stream()
                .mapToDouble(TimeRegistration::getHours)
                .sum();
    }
}
