package com.planner.repository;

import com.planner.domain.Developer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeveloperRepository {

    private final List<Developer> developers = new ArrayList<>();

    public DeveloperRepository() {
        loadFromFile();
        // Ensure "huba" is always present as per requirements
        if (!exists("huba")) {
            developers.add(0, new Developer("huba"));
        }
    }

    private void loadFromFile() {
        try (InputStream is = getClass().getResourceAsStream("/developers.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .forEach(initials -> developers.add(new Developer(initials)));
        } catch (Exception e) {
            developers.add(new Developer("huba"));
        }
    }

    public void add(Developer developer) {
        developers.add(developer);
    }

    public Optional<Developer> findByInitials(String initials) {
        // For loop through the developers
    for (Developer dev : developers) {
        if (dev.getInitials().equalsIgnoreCase(initials)) {
            // Return the dev
            return Optional.of(dev);
        }
    }
    // safety if initials does not exist
    return Optional.empty();
    }


    public List<Developer> findAll() {
        return new ArrayList<>(developers);
    }

    public boolean exists(String initials) {
        return findByInitials(initials).isPresent();
    }
}
