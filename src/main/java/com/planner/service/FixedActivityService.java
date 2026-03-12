package com.planner.service;

import com.planner.domain.Developer;
import com.planner.domain.FixedActivity;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.FixedActivityRepository;

import java.util.List;

public class FixedActivityService {

    private final FixedActivityRepository fixedActivityRepository;
    private final DeveloperRepository developerRepository;

    public FixedActivityService(FixedActivityRepository fixedActivityRepository,
                                DeveloperRepository developerRepository) {
        this.fixedActivityRepository = fixedActivityRepository;
        this.developerRepository = developerRepository;
    }

    public FixedActivity registerFixedActivity(String developerInitials, FixedActivity.Type type,
                                               int startWeek, int startYear,
                                               int endWeek, int endYear) {
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));
        FixedActivity fixedActivity = new FixedActivity(developer, type, startWeek, startYear, endWeek, endYear);
        fixedActivityRepository.add(fixedActivity);
        return fixedActivity;
    }

    public boolean isDeveloperBusy(String developerInitials, int week, int year) {
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));
        return fixedActivityRepository.findByDeveloper(developer).stream()
                .anyMatch(fa -> fa.isActiveInWeek(week, year));
    }

    public List<FixedActivity> getFixedActivitiesForDeveloper(String developerInitials) {
        Developer developer = developerRepository.findByInitials(developerInitials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + developerInitials));
        return fixedActivityRepository.findByDeveloper(developer);
    }
}
