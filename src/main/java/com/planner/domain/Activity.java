// Viktor
package com.planner.domain;

import java.util.ArrayList;
import java.util.List;

// Domain class representing an activity within a project.
// Defined in Report 1, Section 5.1.2.
// An activity belongs to a project and keeps track of budget, time period, assigned developers, and registered hours.
public class Activity {

    // The activity's name — cannot be changed after creation 
    private final String name;

    // How many hours the activity is budgeted to take (UC2: Create Activity)
    private double budgetedHours;

    // Start week and year for the activity, 0 means "not yet set"
    private int startWeek;
    private int startYear;

    // End week and year for the activity, 0 means "not yet set"
    private int endWeek;
    private int endYear;

    // Developers assigned to this activity, UC3: Add Developer to Activity.
    private List<Developer> assignedDevelopers = new ArrayList<>();

    // All time registrations logged on this activity, UC4: Register Time on Activity
    private List<TimeRegistration> timeRegistrations = new ArrayList<>();

    // Constructor — creates an activity with a name, UC2: Create Activity
    public Activity(String name) {
        this.name = name;
    }

    // Returns the activity's name
    public String getName() {
        return name;
    }

    // Returns the budgeted number of hours
    public double getBudgetedHours() {
        return budgetedHours;
    }

    // Sets the budgeted number of hours
    public void setBudgetedHours(double hours) {
        this.budgetedHours = hours;
    }

    // Returns the start week number
    public int getStartWeek() {
        return startWeek;
    }

    // Sets the start week number
    public void setStartWeek(int week) {
        this.startWeek = week;
    }

    // Returns the start year
    public int getStartYear() {
        return startYear;
    }

    // Sets the start year
    public void setStartYear(int year) {
        this.startYear = year;
    }

    // Returns the end week number
    public int getEndWeek() {
        return endWeek;
    }

    // Sets the end week number
    public void setEndWeek(int week) {
        this.endWeek = week;
    }

    // Returns the end year
    public int getEndYear() {
        return endYear;
    }

    // Sets the end year
    public void setEndYear(int year) {
        this.endYear = year;
    }

    // Returns the list of assigned developers, UC3: Add Developer to Activity
    public List<Developer> getAssignedDevelopers() {
        return assignedDevelopers;
    }

    // Adds a developer to the activity, only if they are not already assigned.
    // Prevents duplicate entries, as required by UC3, Report 1, Section 4.3.
    public void addDeveloper(Developer developer) {
        if (!assignedDevelopers.contains(developer)) {
            assignedDevelopers.add(developer);
        }
    }

    // Returns all time registrations on this activity, UC4: Register Time on Activity
    public List<TimeRegistration> getTimeRegistrations() {
        return timeRegistrations;
    }

    // Adds a new time registration to this activity, UC4: Register Time on Activity
    public void addTimeRegistration(TimeRegistration registration) {
        timeRegistrations.add(registration);
    }

    // Sums all registered hours on this activity and returns the total.
    // Used by UC5, Generate Report and UC10, View Project Progress to compare
    // registered hours against the budget.
    public double getTotalRegisteredHours() {
        double total = 0;
        for (TimeRegistration r : timeRegistrations) {
            total += r.getHours();
        }
        return total;
    }
}
