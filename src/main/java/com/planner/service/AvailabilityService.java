package com.planner.service;
// Nat
//Makes the list of awaileble developeres 
import java.util.ArrayList;
import java.util.List;

import com.planner.domain.Absence;
import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.Project;
import com.planner.repository.IAbsenceRepository;
import com.planner.repository.IDeveloperRepository;
import com.planner.repository.IProjectRepository;

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
        //Makes the list of free developers (fully rewiten without any AI)
        List<Developer> allDevelopers = developerRepository.findAll();
        List<Developer> busyDevelopers = new ArrayList<>();
        List<Developer> freeDevelopers = new ArrayList<>();
        //Makes empty lists for later use

        List<Project> projects = projectRepository.findAll();
        for (Project project : projects) {
            List<Activity> activities = project.getActivities();
            for (Activity activity : activities) {
                if (activityIsActiveInWeek(activity, week, year)){
                    List<Developer> developers = activity.getAssignedDevelopers();
                    for (Developer developer : developers) {
                        busyDevelopers.add(developer);
                    }
                }
            }
        }
        //adds developes, that are already busy in a given week, to the list

        List<Absence> Absences = absenceRepository.findAll();
        for (Absence absence : Absences) {
            if (absence.isActiveInWeek(week, year)){
                busyDevelopers.add(absence.getDeveloper());
            }
        }
        //adds developers, that are abcent in a given week, to the list

        for (Developer developer : allDevelopers) {
            if (!busyDevelopers.contains(developer)){
                freeDevelopers.add(developer);
            }
        }
        //adds all the developers not on the busy list, to the list of free developers
        return freeDevelopers;
        //returns them
                
    }

    private boolean activityIsActiveInWeek(Activity activity, int week, int year) {
        //checs if an activity is active in a given week
        if (activity.getStartYear() == 0) return false;
        int actStart = activity.getStartYear() * 100 + activity.getStartWeek();
        int actEnd = activity.getEndYear() * 100 + activity.getEndWeek();
        int target = year * 100 + week; 
        //Combines the year and week to a singular integer for all the dates 
        return target >= actStart && target <= actEnd;
        // sees if the taget date is insid the period the project is active
    }
}
