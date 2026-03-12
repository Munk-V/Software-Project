package com.planner.service;

import com.planner.domain.Developer;
import com.planner.repository.DeveloperRepository;

import java.util.List;

public class DeveloperService {

    private final DeveloperRepository developerRepository;

    public DeveloperService(DeveloperRepository developerRepository) {
        this.developerRepository = developerRepository;
    }

    public Developer getDeveloper(String initials) {
        return developerRepository.findByInitials(initials)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + initials));
    }

    public List<Developer> getAllDevelopers() {
        return developerRepository.findAll();
    }

    public boolean developerExists(String initials) {
        return developerRepository.exists(initials);
    }
}
