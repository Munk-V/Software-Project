// Viktor
package com.planner.service;

import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.Project;
import com.planner.repository.IDeveloperRepository;
import com.planner.repository.IProjectRepository;

import java.util.List;
import java.util.Optional;

// Service class handling all business logic for activities.
// Corresponds to UC2 and UC3 from Report 1.
// Depends on interfaces, Dependency Inversion Principle, Report 2, Section 6.4.
// White-box tests for this class are in Report 2, Section 3.3 and 3.4.
// Design by Contract specifications are in Report 2, Section 4.2 and 4.3.
public class ActivityService {

    // Reference to the project repository, used to find and update projects.
    // Interface type used to follow the Dependency Inversion Principle.
    private final IProjectRepository projectRepository;

    // Reference to the developer repository, used to look up developers by initials.
    // Interface type used to follow the Dependency Inversion Principle.
    private final IDeveloperRepository developerRepository;

    // Constructor — receives repositories via dependency injection
    public ActivityService(IProjectRepository projectRepository, IDeveloperRepository developerRepository) {
        this.projectRepository = projectRepository;
        this.developerRepository = developerRepository;
    }

    // UC2: Create Activity
    // Creates a new activity inside the given project.
    // Tested in Cucumber feature file, "Create an activity and add it to a project".
    public Activity createActivity(String projectId, String activityName) {
        // Reject empty or blank activity names
        if (activityName == null || activityName.trim().isEmpty()) {
            throw new IllegalArgumentException("Activity name cannot be empty");
        }

        // Look up the project, throw if it does not exist
        Optional<Project> result = projectRepository.findById(projectId);
        if (!result.isPresent()) {
            throw new IllegalArgumentException("Project not found: " + projectId);
        }
        Project project = result.get();

        // Create the activity and add it to the project
        Activity activity = new Activity(activityName);
        project.addActivity(activity);
        return activity;
    }

    // UC2: Set Activity Details 
    // Sets the budget and time period on an existing activity.
    // Week 0 means "not yet planned" and skips range and ordering checks for that value.
    // Systematically tested in Report 2, Section 3.3.
    // Design by Contract specification in Report 2, Section 4.2.
    public void setActivityDetails(String projectId, String activityName,
                                   double budgetedHours, int startWeek, int startYear,
                                   int endWeek, int endYear) {
        // Budget cannot be negative
        if (budgetedHours < 0) {
            throw new IllegalArgumentException("Budgeted hours cannot be negative");
        }
        // Start week must be 1–53 if it is set, 0 = unset
        if (startWeek != 0 && (startWeek < 1 || startWeek > 53)) {
            throw new IllegalArgumentException("Start week must be between 1 and 53");
        }
        // End week must be 1–53 if it is set, 0 = unset
        if (endWeek != 0 && (endWeek < 1 || endWeek > 53)) {
            throw new IllegalArgumentException("End week must be between 1 and 53");
        }
        // Start must not be after end.
        // Encoded as year*100+week so two week-dates can be compared as plain integers.
        if (startWeek != 0 && endWeek != 0) {
            int start = startYear * 100 + startWeek;
            int end = endYear * 100 + endWeek;
            if (start > end) {
                throw new IllegalArgumentException("Start week must be before or equal to end week");
            }
        }
        // Dates cannot be in the past
        if (startWeek != 0) requireNotInPast(startWeek, startYear);
        if (endWeek != 0) requireNotInPast(endWeek, endYear);

        // Pre ponditions, Design by Contract, Report 2 Section 4.2:
        // all validation above must pass before reaching this point
        assert budgetedHours >= 0 : "budgetedHours must be non-negative";
        assert startWeek == 0 || (startWeek >= 1 && startWeek <= 53) : "startWeek must be 0 or between 1 and 53";
        assert endWeek == 0 || (endWeek >= 1 && endWeek <= 53) : "endWeek must be 0 or between 1 and 53";

        // Find the activity and apply the validated values
        Activity activity = getActivity(projectId, activityName);
        activity.setBudgetedHours(budgetedHours);
        activity.setStartWeek(startWeek);
        activity.setStartYear(startYear);
        activity.setEndWeek(endWeek);
        activity.setEndYear(endYear);

        // Post conditions, Design by Contract, Report 2 Section 4.2:
        // verify the activity state matches the input after update
        assert activity.getBudgetedHours() == budgetedHours : "budgeted hours must match input";
        assert activity.getStartWeek() == startWeek : "start week must match input";
        assert activity.getEndWeek() == endWeek : "end week must match input";
    }

    // UC3: Add Developer to Activity 
    // Looks up the developer by initials and assigns them to the activity.
    // Systematically tested in Report 2, Section 3.4.
    // Design by Contract specification in Report 2, Section 4.3.
    public void addDeveloperToActivity(String projectId, String activityName, String developerInitials) {
        // Find the activity first — throws if project or activity does not exist
        Activity activity = getActivity(projectId, activityName);

        // Look up the developer by initials, throw if not found
        Optional<Developer> result = developerRepository.findByInitials(developerInitials);
        if (!result.isPresent()) {
            throw new IllegalArgumentException("Developer not found: " + developerInitials);
        }
        Developer developer = result.get();

        // Add developer to activity, Activity.addDeveloper prevents duplicates internally
        activity.addDeveloper(developer);
    }

    // Helper method, finds a specific activity in a project.
    // Throws if either the project or the activity does not exist.
    // Used internally by setActivityDetails and addDeveloperToActivity.
    public Activity getActivity(String projectId, String activityName) {
        Optional<Activity> result = projectRepository.findActivity(projectId, activityName);
        if (!result.isPresent()) {
            throw new IllegalArgumentException("Activity not found: " + activityName);
        }
        return result.get();
    }

    // Removes an activity from a project
    public void deleteActivity(String projectId, String activityName) {
        projectRepository.removeActivity(projectId, activityName);
    }

    // Returns all activities belonging to the given project
    public List<Activity> getActivitiesForProject(String projectId) {
        Optional<Project> result = projectRepository.findById(projectId);
        if (!result.isPresent()) {
            throw new IllegalArgumentException("Project not found: " + projectId);
        }
        return result.get().getActivities();
    }

    // Throws an exception if the given week/year is in the past.
    // Ensures users cannot set activity periods that have already passed.
    private void requireNotInPast(int week, int year) {
        int currentYear = java.time.LocalDate.now().getYear();
        int currentWeek = java.time.LocalDate.now().get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
        if (year * 100 + week < currentYear * 100 + currentWeek) {
            throw new IllegalArgumentException("Date cannot be in the past");
        }
    }
}
