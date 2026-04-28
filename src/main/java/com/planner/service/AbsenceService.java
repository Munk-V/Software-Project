package com.planner.service;
// Nicolai

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

    public AbsenceService(IAbsenceRepository absenceRepository,
                          IDeveloperRepository developerRepository,
                          IProjectRepository projectRepository) {
        this.absenceRepository = absenceRepository;
        this.developerRepository = developerRepository;
        this.projectRepository = projectRepository;
    }

    public Absence registerAbsence(String developerInitials, Absence.Type type,
                                   int startWeek, int startYear,
                                   int endWeek, int endYear) {
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

        // Pre-conditions (hold after defensive validation above)
        assert developerInitials != null : "developerInitials must not be null";
        assert type != null : "type must not be null";
        assert startWeek >= 1 && startWeek <= 53 : "startWeek must be between 1 and 53";
        assert endWeek >= 1 && endWeek <= 53 : "endWeek must be between 1 and 53";

        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));

        if (isDeveloperBusyInPeriod(developer, startWeek, startYear, endWeek, endYear)) {
            throw new IllegalArgumentException("Absence denied: developer is assigned to an activity in that period");
        }

        Absence absence = new Absence(developer, type, startWeek, startYear, endWeek, endYear);
        absenceRepository.add(absence);

        // Post-conditions
        assert absence != null : "created absence must not be null";
        assert absenceRepository.findByDeveloper(developer).contains(absence) : "absence must be stored in repository";

        return absence;
    }

    public boolean isDeveloperAbsent(String developerInitials, int week, int year) {
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));

        return absenceRepository.findByDeveloper(developer).stream()
                .anyMatch(a -> a.isActiveInWeek(week, year));
    }

    public List<Absence> getAbsencesForDeveloper(String developerInitials) {
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));
        return absenceRepository.findByDeveloper(developer);
    }

    private boolean isDeveloperBusyInPeriod(Developer developer,
                                            int startWeek, int startYear,
                                            int endWeek, int endYear) {
        int requestedStart = startYear * 100 + startWeek;
        int requestedEnd = endYear * 100 + endWeek;

        for (Project project : projectRepository.findAll()) {
            for (Activity activity : project.getActivities()) {
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
}
