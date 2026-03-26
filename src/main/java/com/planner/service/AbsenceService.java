package com.planner.service;

import com.planner.domain.Activity;
import com.planner.domain.Developer;
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
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));

        if (type == Absence.Type.VACATION && isDeveloperBusyInPeriod(developer, startWeek, startYear, endWeek, endYear)) {
            throw new IllegalArgumentException("Vacation denied: developer is assigned to an activity in that period");
        }

        Absence absence = new Absence(developer, type, startWeek, startYear, endWeek, endYear);
        absenceRepository.add(absence);
        return absence;
    }

    public boolean isDeveloperAbsent(String developerInitials, int week, int year) {
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));
        return absenceRepository.findByDeveloper(developer).stream()
                .anyMatch(fa -> fa.isActiveInWeek(week, year));
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

        return projectRepository.findAll().stream()
                .flatMap(project -> project.getActivities().stream())
                .filter(activity -> activity.getAssignedDevelopers().contains(developer))
                .anyMatch(activity -> activityOverlaps(activity, requestedStart, requestedEnd));
    }

    private boolean activityOverlaps(Activity activity, int requestedStart, int requestedEnd) {
        if (activity.getStartYear() == 0 || activity.getEndYear() == 0) {
            return false;
        }

        int activityStart = activity.getStartYear() * 100 + activity.getStartWeek();
        int activityEnd = activity.getEndYear() * 100 + activity.getEndWeek();

        return activityStart <= requestedEnd && activityEnd >= requestedStart;
    }
}
