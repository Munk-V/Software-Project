package com.planner.repository;

import com.planner.domain.Developer;
import com.planner.domain.Absence;

import java.util.ArrayList;
import java.util.List;


public class AbsenceRepository implements IAbsenceRepository {

    private final List<Absence> absences = new ArrayList<>();

    // basic absence add to the arraylist.
    public void add(Absence absence) {
        absences.add(absence);
    }

    // very essential as it is called multiple times through absenceservice.
    // streams through developer initials 
    public List<Absence> findByDeveloper(Developer developer) {
    List<Absence> result = new ArrayList<>();

    for (Absence absence : absences) { // for loop through the list
        if (absence.getDeveloper().equals(developer)) {
            result.add(absence);// adds absence if any
        }
    }

    return result;
    }

    // if list should be called
    public List<Absence> findAll() {
        return new ArrayList<>(absences);
    }
}
