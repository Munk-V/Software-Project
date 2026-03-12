package com.planner.service;

import com.planner.domain.Developer;
import com.planner.domain.Absence;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.AbsenceRepository;

import java.util.List;

public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final DeveloperRepository developerRepository;

    public AbsenceService(AbsenceRepository absenceRepository,
                                DeveloperRepository developerRepository) {
        this.absenceRepository = absenceRepository;
        this.developerRepository = developerRepository;
    }

    public Absence registerAbsence(String developerInitials, Absence.Type type,
                                               int startWeek, int startYear,
                                               int endWeek, int endYear) {
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));
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
}
