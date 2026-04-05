package com.planner.service;

import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.Project;
import com.planner.domain.Absence;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.AbsenceRepository;
import com.planner.repository.ProjectRepository;

import java.util.List;

public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final DeveloperRepository developerRepository;
    private final ProjectRepository projectRepository;

    public AbsenceService(AbsenceRepository absenceRepository,
                          DeveloperRepository developerRepository,
                          ProjectRepository projectRepository) {
        this.absenceRepository = absenceRepository;
        this.developerRepository = developerRepository;
        this.projectRepository = projectRepository;
    }

    public Absence registerAbsence(String developerInitials, Absence.Type type,
                                   int startWeek, int startYear,
                                   int endWeek, int endYear) {
                                    
        // Find the developer in developerbase. DeveloperRepository finds the dev or sends back empty string
        Developer developer = developerRepository.findByInitials(developerInitials)

        // orElseThrow() command stolen from the web. Find it more clever than alternative
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));
        


        // If developer wishes for vacation but are busy during the wished weeks, af vacation denied: messege is given. 
        // Havent elaborated further on this feature.
        if (type == Absence.Type.VACATION && isDeveloperBusyInPeriod(developer, startWeek, startYear, endWeek, endYear)) {
            throw new IllegalArgumentException("Vacation denied: developer is assigned to an activity in that period");
        }

        // if tey are free, register absence. Including the enumerate type, week start and end.
        Absence absence = new Absence(developer, type, startWeek, startYear, endWeek, endYear);
        absenceRepository.add(absence);
        return absence;
    }

    // iF the developers absence is granted, their absence is registered as an "activity".
    public boolean isDeveloperAbsent(String developerInitials, int week, int year) {
    Developer developer = developerRepository.findByInitials(developerInitials)
            .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));

    List<Absence> absences = absenceRepository.findByDeveloper(developer);

    for (Absence absence : absences) {
        if (absence.isActiveInWeek(week, year)) {
            return true;
        }
    }

    return false;
    }


    // if a project leader or other developers wants to know if a dev is absent in a given week. this is called through UI.
    public List<Absence> getAbsencesForDeveloper(String developerInitials) {
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));
        return absenceRepository.findByDeveloper(developer);// returns the absence repository, if initials are found.
    }

    // Used un registerAbsence-loop.     
    private boolean isDeveloperBusyInPeriod(Developer developer,
                                            int startWeek, int startYear,
                                            int endWeek, int endYear) {
        // same tie logic used throughout. easier than (year== && day ==) and so on. 
        // this makes one large number e.g 202604 as in year+week
        int requestedStart = startYear * 100 + startWeek;
        int requestedEnd = endYear * 100 + endWeek;
        

        // search through every activity across all projects.
        // only activities assigned to the developer are relevant.
        // if at least one overlaps with the requested period, the developer is considered busy.
         List<Project> projects = projectRepository.findAll();

        for (Project project : projects) {
        List<Activity> activities = project.getActivities();

        for (Activity activity : activities) {
            if (activity.getAssignedDevelopers().contains(developer)) {
                if (activityOverlaps(activity, requestedStart, requestedEnd)) {
                    return true;
                }
            }
        }
    }

    return false;
}

    private boolean activityOverlaps(Activity activity, int requestedStart, int requestedEnd) {
        // if an activity has no valid period yet, it doesnt deny vacation or absence
        if (activity.getStartYear() == 0 || activity.getEndYear() == 0) {
            return false;
        }
        // converts to large number again
        int activityStart = activity.getStartYear() * 100 + activity.getStartWeek();
        int activityEnd = activity.getEndYear() * 100 + activity.getEndWeek();

        // If an overlap exists, acitivty start before other is finished. there is an activity overlap.
        return activityStart <= requestedEnd && activityEnd >= requestedStart;
    }
}
