package com.planner.repository;
// Nicolai

import com.planner.domain.Absence;
import com.planner.domain.Developer;

import java.util.List;

public interface IAbsenceRepository {
    void add(Absence absence);
    List<Absence> findByDeveloper(Developer developer);
    List<Absence> findAll();
}
