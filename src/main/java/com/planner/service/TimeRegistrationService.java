package com.planner.service;
// Nat

import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.TimeRegistration;
import com.planner.repository.IDeveloperRepository;
import com.planner.repository.IProjectRepository;

import java.time.LocalDate;

public class TimeRegistrationService {

    private final IProjectRepository projectRepository;
    private final IDeveloperRepository developerRepository;

    public TimeRegistrationService(IProjectRepository projectRepository, IDeveloperRepository developerRepository) {
        this.projectRepository = projectRepository;
        this.developerRepository = developerRepository;
    }

    public TimeRegistration registerTime(String developerInitials, String projectId,
                                         String activityName, LocalDate date, double hours) {
        if (hours <= 0 || hours % 0.5 != 0) {
            throw new IllegalArgumentException("Hours must be a positive multiple of 0.5");
        }

        // Pre-conditions (hold after defensive validation above)
        assert developerInitials != null : "developerInitials must not be null";
        assert projectId != null : "projectId must not be null";
        assert activityName != null : "activityName must not be null";
        assert date != null : "date must not be null";
        assert hours > 0 : "hours must be positive";
        assert hours % 0.5 == 0 : "hours must be a multiple of 0.5";

        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));

        Activity activity = projectRepository.findActivity(projectId, activityName)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found: " + activityName));

        TimeRegistration registration = new TimeRegistration(developer, activity, date, hours);
        activity.addTimeRegistration(registration);

        // Post-conditions
        assert activity.getTimeRegistrations().contains(registration) : "registration must be stored on activity";
        assert registration.getHours() == hours : "registered hours must match input";

        return registration;
    }

    public void editTimeRegistration(String developerInitials, String projectId,
                                     String activityName, LocalDate date, double newHours) {
        if (newHours <= 0 || newHours % 0.5 != 0) {
            throw new IllegalArgumentException("Hours must be a positive multiple of 0.5");
        }

        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));

        Activity activity = projectRepository.findActivity(projectId, activityName)
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
