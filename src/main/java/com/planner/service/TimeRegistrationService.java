package com.planner.service;

import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.TimeRegistration;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.ProjectRepository;

import java.time.LocalDate;

public class TimeRegistrationService {

    private final ProjectRepository projectRepository;
    private final DeveloperRepository developerRepository;

    public TimeRegistrationService(ProjectRepository projectRepository, DeveloperRepository developerRepository) {
        this.projectRepository = projectRepository;
        this.developerRepository = developerRepository;
    }

    public TimeRegistration registerTime(String developerInitials, String projectId,
                                         String activityName, LocalDate date, double hours) {
        if (hours <= 0 || hours % 0.5 != 0) {
            throw new IllegalArgumentException("Hours must be a positive multiple of 0.5");
        }

        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));

        Activity activity = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId))
                .getActivities().stream()
                .filter(a -> a.getName().equals(activityName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Activity not found: " + activityName));

        TimeRegistration registration = new TimeRegistration(developer, activity, date, hours);
        activity.addTimeRegistration(registration);
        return registration;
    }
}
