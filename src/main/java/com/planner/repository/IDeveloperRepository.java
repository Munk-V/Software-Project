package com.planner.repository;
// Mathias

import com.planner.domain.Developer;

import java.util.List;
import java.util.Optional;

public interface IDeveloperRepository {
    void add(Developer developer);
    Optional<Developer> findByInitials(String initials);
    List<Developer> findAll();
    boolean exists(String initials);
}
