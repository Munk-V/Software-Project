package com.planner.service;
// Nat
//The service layer code for rgistering time, 
import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.Project;
import com.planner.domain.TimeRegistration;
import com.planner.repository.IDeveloperRepository;
import com.planner.repository.IProjectRepository;
import java.time.LocalDate;
import java.util.List;



public class TimeRegistrationService {
// Creates an instance of the repocitory level classes, for ProjectRepocitory and DeveloperRepocitory.
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

        // assert needs to be redone

        assert developerInitials != null : "developerInitials must not be null";
        assert projectId != null : "projectId must not be null";
        assert activityName != null : "activityName must not be null";
        assert date != null : "date must not be null";
        assert hours > 0 : "hours must be positive";
        assert hours % 0.5 == 0 : "hours must be a multiple of 0.5";

        Developer developer =   developerRepository.findByInitials(developerInitials)
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

        List<TimeRegistration> registration = activity.getTimeRegistrations();
        for (TimeRegistration timeRegistration : registration) {
                if (timeRegistration.getDeveloper().equals(developer) && timeRegistration.getDate().equals(date)){
                        timeRegistration.setHours(newHours);
                }
                 else{
                    throw new IllegalArgumentException("No time registration found for that date");
                 }
        }
                

    }

    public double getTodayHours(String developerInitials, LocalDate date) {
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));
        
        Double sum = 0.0;
        List<Project> project = projectRepository.findAll();
        for (Project proj : project) {
        List<Activity> activities = proj.getActivities();
                for (Activity activity : activities) {
                List<TimeRegistration> timeRegistrations = activity.getTimeRegistrations();
                        for (TimeRegistration timeRegistration : timeRegistrations) {
                                if (timeRegistration.getDeveloper().equals(developer) && timeRegistration.getDate().equals(date)){
                                Double time = timeRegistration.getHours();
                                sum = sum + time;
                                }
                        }
                }
        }
        return sum;        
    }
}
