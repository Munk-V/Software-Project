package com.planner.repository;

import com.planner.domain.Developer;
import com.planner.domain.FixedActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FixedActivityRepository {

    private final List<FixedActivity> fixedActivities = new ArrayList<>();

    public void add(FixedActivity fixedActivity) {
        fixedActivities.add(fixedActivity);
    }

    public List<FixedActivity> findByDeveloper(Developer developer) {
        return fixedActivities.stream()
                .filter(fa -> fa.getDeveloper().equals(developer))
                .collect(Collectors.toList());
    }

    public List<FixedActivity> findAll() {
        return new ArrayList<>(fixedActivities);
    }
}
