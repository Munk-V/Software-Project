package com.planner.ui;
// Nicolai

import com.planner.repository.AbsenceRepository;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.IAbsenceRepository;
import com.planner.repository.IDeveloperRepository;
import com.planner.repository.IProjectRepository;
import com.planner.repository.ProjectRepository;
import com.planner.service.AbsenceService;
import com.planner.service.ActivityService;
import com.planner.service.AvailabilityService;
import com.planner.service.DeveloperService;
import com.planner.service.ProjectService;
import com.planner.service.TimeRegistrationService;

public class AppContext {

    //Appcontext acts like a hub for all services to keep them out of the UI.
    //If services an repositorie are not kept seperate, dependencies are directly in the mainwindow themselves. t
    
    // therefore repoes/services are declared here
    public final ProjectService projectService;
    public final ActivityService activityService;
    public final AvailabilityService availabilityService;
    public final DeveloperService developerService;
    public final TimeRegistrationService timeRegistrationService;
    public final AbsenceService absenceService;

    public AppContext() {
        //in-memory storage
        IProjectRepository projectRepository = new ProjectRepository();
        IDeveloperRepository developerRepository = new DeveloperRepository();
        IAbsenceRepository absenceRepository = new AbsenceRepository();

        // initializes all servuces and inject the dependancies. (DIP)
        this.projectService = new ProjectService(projectRepository, developerRepository);
        this.activityService = new ActivityService(projectRepository, developerRepository);
        this.availabilityService = new AvailabilityService(projectRepository, developerRepository, absenceRepository);
        this.developerService = new DeveloperService(developerRepository);
        this.timeRegistrationService = new TimeRegistrationService(projectRepository, developerRepository);
        this.absenceService = new AbsenceService(absenceRepository, developerRepository, projectRepository);
    }
}
