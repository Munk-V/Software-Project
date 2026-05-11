// Nicolai, s234146
package com.planner.service;


import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.Project;
import com.planner.domain.Absence;
import com.planner.repository.IDeveloperRepository;
import com.planner.repository.IAbsenceRepository;
import com.planner.repository.IProjectRepository;

import java.util.List;

public class AbsenceService {

    private final IAbsenceRepository absenceRepository;
    private final IDeveloperRepository developerRepository;
    private final IProjectRepository projectRepository;

    public AbsenceService(IAbsenceRepository absenceRepository, IDeveloperRepository developerRepository, IProjectRepository projectRepository) {
        this.absenceRepository = absenceRepository;
        this.developerRepository = developerRepository;
        this.projectRepository = projectRepository;
    }


    // absenceRegistry 
    public Absence registerAbsence(String developerInitials, Absence.Type type, int startWeek, int startYear, int endWeek, int endYear) {
        if (type == null) {
            throw new IllegalArgumentException("Absence type cannot be null");
        }
        // validWeeks
        if (startWeek < 1 || startWeek > 53 || endWeek < 1 || endWeek > 53) {
            throw new IllegalArgumentException("Week must be between 1 and 53");
        }
        // calls function requireNotInPast
        requireNotInPast(startWeek, startYear);
        requireNotInPast(endWeek, endYear);

        // 202643 still AI's idea. But copied to here. 
        int start = startYear * 100 + startWeek;
        int end = endYear * 100 + endWeek;
        if (start > end) {
            throw new IllegalArgumentException("Start week must be before or equal to end week");
        }

        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));

        // Way to work around having a sick leave exception.
        // All absencetypes except, sick leave cannot be made if the developer is active
        if (type != Absence.Type.SICK_LEAVE && isDeveloperBusyInPeriod(developer, startWeek, startYear, endWeek, endYear)) {
            throw new IllegalArgumentException("Absence denied: developer is assigned to an activity in that period");
        }

        // create absence
        Absence absence = new Absence(developer, type, startWeek, startYear, endWeek, endYear);
        absenceRepository.add(absence);

        return absence;
    }

    public boolean isDeveloperAbsent(String developerInitials, int week, int year) {
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));

        // goes through repoositorys, if developer a is active, return true:
        for (Absence a : absenceRepository.findByDeveloper(developer)) {
            if (a.isActiveInWeek(week, year)) {
                return true;
            }
        }
        return false;
    }


    public List<Absence> getAbsencesForDeveloper(String developerInitials) {
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));
        return absenceRepository.findByDeveloper(developer);
    }

    private boolean isDeveloperBusyInPeriod(Developer developer, int startWeek, int startYear, int endWeek, int endYear) {

        int requestedStart = startYear * 100 + startWeek;// eg: 202652
        int requestedEnd = endYear * 100 + endWeek;

        // goes through every project
        for (Project project : projectRepository.findAll()) {
            // every activity
            for (Activity activity : project.getActivities()) {
                // if develepor is busy in the weeek. Return True
                if (activity.getAssignedDevelopers().contains(developer)
                        && activityOverlaps(activity, requestedStart, requestedEnd)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean activityOverlaps(Activity activity, int requestedStart, int requestedEnd) {
        
        if (activity.getStartYear() == 0 || activity.getEndYear() == 0) return false;

        int activityStart = activity.getStartYear() * 100 + activity.getStartWeek();
        int activityEnd = activity.getEndYear() * 100 + activity.getEndWeek();
        return activityStart <= requestedEnd && activityEnd >= requestedStart;
    }

    // Just a java.time import
    private void requireNotInPast(int week, int year) {
        int currentYear = java.time.LocalDate.now().getYear();
        int currentWeek = java.time.LocalDate.now().get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
        // Same logic as rest of code. Commented 100 times
        if (year * 100 + week < currentYear * 100 + currentWeek) {
            throw new IllegalArgumentException("Date cannot be in the past");
        }
    }
}
