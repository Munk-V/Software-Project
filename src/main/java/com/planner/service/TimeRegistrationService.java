package com.planner.service;
//Nat
import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.TimeRegistration;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.ProjectRepository;

import java.time.LocalDate;
import java.util.List;

public class TimeRegistrationService {

    private final ProjectRepository projectRepository;
    private final DeveloperRepository developerRepository;

    public TimeRegistrationService(ProjectRepository projectRepository, DeveloperRepository developerRepository) {
        this.projectRepository = projectRepository;
        this.developerRepository = developerRepository;
    }

    public TimeRegistration registerTime(String developerInitials, String projectId, //Houres ar e registerd in half houre intervals
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

    public void editTimeRegistration(String developerInitials, String projectId,
                                     String activityName, LocalDate date, double newHours) {
        if (newHours <= 0 || newHours % 0.5 != 0) {
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

        TimeRegistration registration = activity.getTimeRegistrations().stream()
                .filter(r -> r.getDeveloper().equals(developer) && r.getDate().equals(date))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No time registration found for that date"));

        registration.setHours(newHours);
    }

    public double getTodayHours(String developerInitials, LocalDate date) {
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));

        return projectRepository.findAll().stream()
                .flatMap(p -> p.getActivities().stream())
                .flatMap(a -> a.getTimeRegistrations().stream())
                .filter(r -> r.getDeveloper().equals(developer) && r.getDate().equals(date))
                .mapToDouble(TimeRegistration::getHours)
                .sum();
    }
}
