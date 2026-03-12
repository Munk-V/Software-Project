package com.planner.repository;

import com.planner.domain.Developer;
import com.planner.domain.Absence;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AbsenceRepository {

    private final List<Absence> absences = new ArrayList<>();

    public void add(Absence absence) {
        absences.add(absence);
    }

    public List<Absence> findByDeveloper(Developer developer) {
        return absences.stream()
                .filter(a -> a.getDeveloper().equals(developer))
                .collect(Collectors.toList());
    }

    public List<Absence> findAll() {
        return new ArrayList<>(absences);
    }
}
