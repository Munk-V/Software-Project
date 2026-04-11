package com.planner.domain;

import java.util.ArrayList;
import java.util.List;

// Activity belongs to a project and keeps track of budget, time period and developers
public class Activity {

    private final String name;
    private double budgetedHours;
    private int startWeek;
    private int startYear;
    private int endWeek;
    private int endYear;
    // list of developers working on this activity
    private List<Developer> assignedDevelopers = new ArrayList<>();
    // list of all time registrations made for this activity
    private List<TimeRegistration> timeRegistrations = new ArrayList<>();

    public Activity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double getBudgetedHours() {
        return budgetedHours;
    }

    public void setBudgetedHours(double hours) {
        this.budgetedHours = hours;
    }

    public int getStartWeek() {
        return startWeek;
    }

    public void setStartWeek(int week) {
        this.startWeek = week;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int year) {
        this.startYear = year;
    }

    public int getEndWeek() {
        return endWeek;
    }

    public void setEndWeek(int week) {
        this.endWeek = week;
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear(int year) {
        this.endYear = year;
    }

    public List<Developer> getAssignedDevelopers() {
        return assignedDevelopers;
    }

    // only add the developer if they are not already on the activity
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

    // adds up all the hours registered on this activity
    public double getTotalRegisteredHours() {
        double total = 0;
        for (TimeRegistration r : timeRegistrations) {
            total += r.getHours();
        }
        return total;
    }
}
