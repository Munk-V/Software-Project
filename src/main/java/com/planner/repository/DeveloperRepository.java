package com.planner.repository;

import com.planner.domain.Developer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeveloperRepository {

    private final List<Developer> developers = new ArrayList<>();

    public DeveloperRepository() {
        // System must always contain "huba" as per requirements
        developers.add(new Developer("huba"));
    }

    public void add(Developer developer) {
        developers.add(developer);
    }

    public Optional<Developer> findByInitials(String initials) {
        return developers.stream()
                .filter(d -> d.getInitials().equalsIgnoreCase(initials))
                .findFirst();
    }

    public List<Developer> findAll() {
        return new ArrayList<>(developers);
    }

    public boolean exists(String initials) {
        return findByInitials(initials).isPresent();
    }
}
