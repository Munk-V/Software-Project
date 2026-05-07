package com.planner.service;
// Nat

import com.planner.domain.Absence;
import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.Project;
import com.planner.repository.IAbsenceRepository;
import com.planner.repository.IDeveloperRepository;
import com.planner.repository.IProjectRepository;

import java.util.ArrayList;
import java.util.List;


// everything here needs to be redone
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
        List<Developer> busyDevelopers = new ArrayList<>();
        List<Developer> freeDevelopers = new ArrayList<>();

        List<Project> projects = projectRepository.findAll();
        for (Project project : projects) {
            List<Activity> activities = project.getActivities();
            for (Activity activity : activities) {
                if (isActiveInWeek(activity, week, year)){
                    List<Developer> developers = activity.getAssignedDevelopers();
                    for (Developer developer : developers) {
                        busyDevelopers.add(developer);
                    }
                }
            }
        }

        List<Absence> Absences = absenceRepository.findAll();
        for (Absence absence : Absences) {
            if (absence.isActiveInWeek(week, year)){
                busyDevelopers.add(absence.getDeveloper());
            }
        }

        for (Developer developer : allDevelopers) {
            if (!busyDevelopers.contains(developer)){
                freeDevelopers.add(developer);
            }
        }
        return freeDevelopers;
                
    }

    private boolean isActiveInWeek(Activity activity, int week, int year) {
        if (activity.getStartYear() == 0) return false;
        int actStart = activity.getStartYear() * 100 + activity.getStartWeek();
        int actEnd = activity.getEndYear() * 100 + activity.getEndWeek();
        int target = year * 100 + week;
        return target >= actStart && target <= actEnd;
    }
}
