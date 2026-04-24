package com.planner.service;
// Mathias

import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.repository.IAbsenceRepository;
import com.planner.repository.IDeveloperRepository;
import com.planner.repository.IProjectRepository;

import java.util.List;
import java.util.stream.Collectors;

public class AvailabilityService {

    private final IProjectRepository projectRepository;
    private final IDeveloperRepository developerRepository;
    private final IAbsenceRepository absenceRepository;

    public AvailabilityService(IProjectRepository projectRepository,
                               IDeveloperRepository developerRepository,
                               IAbsenceRepository absenceRepository) {
        this.projectRepository = projectRepository;
        this.developerRepository = developerRepository;
        this.absenceRepository = absenceRepository;
    }

    public List<Developer> getAvailableDevelopers(int week, int year) {
        List<Developer> allDevelopers = developerRepository.findAll();

        List<Developer> busyOnProjects = projectRepository.findAll().stream()
                .flatMap(p -> p.getActivities().stream())
                .filter(a -> isActiveInWeek(a, week, year))
                .flatMap(a -> a.getAssignedDevelopers().stream())
                .collect(Collectors.toList());

        List<Developer> busyOnAbsences = absenceRepository.findAll().stream()
                .filter(fa -> fa.isActiveInWeek(week, year))
                .map(fa -> fa.getDeveloper())
                .collect(Collectors.toList());

        return allDevelopers.stream()
                .filter(d -> !busyOnProjects.contains(d) && !busyOnAbsences.contains(d))
                .collect(Collectors.toList());
    }

    private boolean isActiveInWeek(Activity activity, int week, int year) {
        if (activity.getStartYear() == 0) return false;
        int actStart = activity.getStartYear() * 100 + activity.getStartWeek();
        int actEnd = activity.getEndYear() * 100 + activity.getEndWeek();
        int target = year * 100 + week;
        return target >= actStart && target <= actEnd;
    }
}
