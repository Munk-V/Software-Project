package com.planner.repository;
// Nicolai

import com.planner.domain.Absence;
import com.planner.domain.Developer;

import java.util.List;

// IabsenceRepository function was made in a later stage to combat coupling.
// this way the service layer do not directly depend on the Absencerepository (DIP)
public interface IAbsenceRepository {
    void add(Absence absence);
    List<Absence> findByDeveloper(Developer developer);
    List<Absence> findAll();
}
