package com.planner.service;
// Nat
//The service layer code for rgistering time, 
import java.time.LocalDate;
import java.util.List;

import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.Project;
import com.planner.domain.TimeRegistration;
import com.planner.repository.IDeveloperRepository;
import com.planner.repository.IProjectRepository;



public class TimeRegistrationService {
// Creates an instance of the repocitory level classes, for ProjectRepocitory and DeveloperRepocitory.
    private final IProjectRepository projectRepository;
    private final IDeveloperRepository developerRepository;

//Makes instances of the repocitories for later use
    public TimeRegistrationService(IProjectRepository projectRepository, IDeveloperRepository developerRepository) {
        this.projectRepository = projectRepository;
        this.developerRepository = developerRepository;
    }

    public TimeRegistration registerTime(String developerInitials, String projectId, String activityName, LocalDate date, double hours) {
        return registerTime(developerInitials, projectId, activityName, date, hours, "");
    }

    public TimeRegistration registerTime(String developerInitials, String projectId, String activityName, LocalDate date, double hours, String comment) {
        if (hours <= 0 || hours % 0.5 != 0.0) {
            throw new IllegalArgumentException("Hours must be a positive multiple of 0.5");
        }
        //Checks if the time is valid to be addet to the time register

        // Pre-conditions (hold after defensive validation above)
        assert developerInitials != null : "developerInitials must not be null";
        assert projectId != null : "projectId must not be null";
        assert activityName != null : "activityName must not be null";
        assert date != null : "date must not be null";
        assert hours > 0 : "hours must be positive";
        assert hours % 0.5 == 0 : "hours must be a multiple of 0.5";

        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));
                //Checks that the developer excists

        Activity activity = projectRepository.findActivity(projectId, activityName)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found: " + activityName));
                //Cheks thath the activity excists

        TimeRegistration registration = new TimeRegistration(developer, activity, date, hours, comment);
        activity.addTimeRegistration(registration);
        //makes a registration

        // Post-conditions
        assert activity.getTimeRegistrations().contains(registration) : "registration must be stored on activity";
        assert registration.getHours() == hours : "registered hours must match input";

        return registration;
    }

    public void editTimeRegistration(String developerInitials, String projectId,String activityName, LocalDate date, double newHours) {
        //lets you edit time registrations
        if (newHours <= 0 || newHours % 0.5 != 0) {
            throw new IllegalArgumentException("Hours must be a positive multiple of 0.5");
        }

        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));

        Activity activity = projectRepository.findActivity(projectId, activityName)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found: " + activityName));
// Cheks the same things as before, but as it aloso instantites the variebles used it is written in both places

        List<TimeRegistration> registration = activity.getTimeRegistrations();
        for (TimeRegistration timeRegistration : registration) {
                if (timeRegistration.getDeveloper().equals(developer) && timeRegistration.getDate().equals(date)){
                        timeRegistration.setHours(newHours);
                }
                 else{
                    throw new IllegalArgumentException("No time registration found for that date");
                 }
        }//Makes sure that there is actualy a time registration for that developer, activity and date
                

    }


    public double getTodayHours(String developerInitials, LocalDate date) {
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));
        
        Double sum = 0.0; //Also rewriten fully 
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
        //TAkes a developer and date and gors thrue all the activityes to see if they have registeret any houres
    }
}
