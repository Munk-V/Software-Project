package com.planner.service;

import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.Project;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.AbsenceRepository;
import com.planner.repository.ProjectRepository;

import java.util.List;
import java.util.stream.Collectors;

public class ActivityService {

    private final ProjectRepository projectRepository;
    private final DeveloperRepository developerRepository;
    private final AbsenceRepository absenceRepository;

    public ActivityService(ProjectRepository projectRepository, DeveloperRepository developerRepository,
                           AbsenceRepository absenceRepository) {
        this.projectRepository = projectRepository;
        this.developerRepository = developerRepository;
        this.absenceRepository = absenceRepository;
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
        Activity activity = getActivity(projectId, activityName);
        activity.setBudgetedHours(budgetedHours);
        activity.setStartWeek(startWeek);
        activity.setStartYear(startYear);
        activity.setEndWeek(endWeek);
        activity.setEndYear(endYear);
    }

    public void addDeveloperToActivity(String projectId, String activityName, String developerInitials) {
        Activity activity = getActivity(projectId, activityName);
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));
        activity.addDeveloper(developer);
    }

    public List<Developer> getAvailableDevelopers(int week, int year) {
        List<Developer> allDevelopers = developerRepository.findAll();

        List<Developer> busyOnProjects = projectRepository.findAll().stream()
                .flatMap(p -> p.getActivities().stream())
                .filter(a -> isActiveInWeek(a, week, year))
                .flatMap(a -> a.getAssignedDevelopers().stream())
                .collect(Collectors.toList());

        List<Developer> busyOnFixedActivities = absenceRepository.findAll().stream()
                .filter(fa -> fa.isActiveInWeek(week, year))
                .map(fa -> fa.getDeveloper())
                .collect(Collectors.toList());

        return allDevelopers.stream()
                .filter(d -> !busyOnProjects.contains(d) && !busyOnFixedActivities.contains(d))
                .collect(Collectors.toList());
    }

    public Activity getActivity(String projectId, String activityName) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        return project.getActivities().stream()
                .filter(a -> a.getName().equals(activityName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Activity not found: " + activityName));
    }

    private boolean isActiveInWeek(Activity activity, int week, int year) {
        if (activity.getStartYear() == 0) return false;
        int actStart = activity.getStartYear() * 100 + activity.getStartWeek();
        int actEnd = activity.getEndYear() * 100 + activity.getEndWeek();
        int target = year * 100 + week;
        return target >= actStart && target <= actEnd;
    }
}
