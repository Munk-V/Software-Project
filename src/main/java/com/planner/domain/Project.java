package com.planner.domain;
//

import java.util.ArrayList; // for the activities for the project
import java.util.List;

public class Project { // Needs to be accessed by other modules; hence pulic class

// Within Project; hence private
    private final String id;
    private final String name;
    private Developer projectLeader;
    private final List<Activity> activities = new ArrayList<>();
    private int startWeek;
    private int startYear;
    private int deadlineWeek;
    private int deadlineYear;
    // 
    public Project(String id, String name) { //Disecting the input; and setting the valiables 
        this.id = id;
        this.name = name;
    }
    // Now Public so if it is "asked for" by other modules
    // Get the ID and Name 
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    // Project leader
    public Developer getProjectLeader() {
        return projectLeader;
    }
    public void setProjectLeader(Developer projectLeader) { // Setting a developer to be the project Leader 
        this.projectLeader = projectLeader;
    }

    public List<Activity> getActivities() { // List of activities for the project
        return activities;
    }
    
    // Time / deadline related stuff
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
    public boolean hasStart() { // True or Fale if the project has started 
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
            throw new IllegalArgumentException("Deadline week must be between 1 and 53 weeks");
        }
        // disecting the input
        this.deadlineWeek = week;
        this.deadlineYear = year;
    }

    public boolean hasDeadline() { // ensuring that things have a deadline 
        return deadlineYear > 0;
    }

    public void addActivity(Activity activity) { // acitivities for the project
        activities.add(activity);
    }
    public double getTotalBudgetedHours() { // get from the 'activity'
        return activities.stream()
                .mapToDouble(Activity::getBudgetedHours)
                .sum();
    }
    public double getTotalRegisteredHours() {  // get from the 'registeration'
        return activities.stream()
                .mapToDouble(Activity::getTotalRegisteredHours)
                .sum();
    }
}
