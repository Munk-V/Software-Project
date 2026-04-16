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
        // Pre-conditions
        assert developerInitials != null : "developerInitials must not be null";
        assert type != null : "type must not be null";
        assert startWeek >= 1 && startWeek <= 53 : "startWeek must be between 1 and 53";
        assert endWeek >= 1 && endWeek <= 53 : "endWeek must be between 1 and 53";

        if (type == null) {
            throw new IllegalArgumentException("Absence type cannot be null");
        }
        if (startWeek < 1 || startWeek > 53 || endWeek < 1 || endWeek > 53) {
            throw new IllegalArgumentException("Week must be between 1 and 53");
        }
        int start = startYear * 100 + startWeek;
        int end = endYear * 100 + endWeek;
        if (start > end) {
            throw new IllegalArgumentException("Start week must be before or equal to end week");
        }

        // Find developer in repository
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));

        // If developer wishes for vacation but are busy during the wished weeks, vacation is denied
        if (type == Absence.Type.VACATION && isDeveloperBusyInPeriod(developer, startWeek, startYear, endWeek, endYear)) {
            throw new IllegalArgumentException("Vacation denied: developer is assigned to an activity in that period");
        }

        // Register absence with type, week start and end
        Absence absence = new Absence(developer, type, startWeek, startYear, endWeek, endYear);
        absenceRepository.add(absence);

        // Post-conditions
        assert absence != null : "created absence must not be null";
        assert absenceRepository.findByDeveloper(developer).contains(absence) : "absence must be stored in repository";

        return absence;
    }

    // If the developers absence is granted, their absence is registered
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

    // If a project leader or other developers want to know if a dev is absent in a given week
    public List<Absence> getAbsencesForDeveloper(String developerInitials) {
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));
        return absenceRepository.findByDeveloper(developer);
    }

    // Used in registerAbsence to check if developer is busy during requested period
    private boolean isDeveloperBusyInPeriod(Developer developer,
                                            int startWeek, int startYear,
                                            int endWeek, int endYear) {
        int requestedStart = startYear * 100 + startWeek;
        int requestedEnd = endYear * 100 + endWeek;

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
        // If an activity has no valid period yet, it doesn't deny vacation or absence
        if (activity.getStartYear() == 0 || activity.getEndYear() == 0) {
            return false;
        }
        int activityStart = activity.getStartYear() * 100 + activity.getStartWeek();
        int activityEnd = activity.getEndYear() * 100 + activity.getEndWeek();

        // Overlap exists if activity starts before requested period ends and ends after it starts
        return activityStart <= requestedEnd && activityEnd >= requestedStart;
    }
}
