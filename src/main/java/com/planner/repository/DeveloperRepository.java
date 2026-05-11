package com.planner.repository;
// Mathias

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.planner.domain.Developer;


public class DeveloperRepository implements IDeveloperRepository {

    private final List<Developer> developers = new ArrayList<>();

public DeveloperRepository() {
    loadFromFile();
    //make sure that huba exists
    if (!exists("huba")) {
        developers.add(0, new Developer("huba"));
    }
}

private void loadFromFile() {
    try {
        InputStream is = getClass().getResourceAsStream("/developers.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        String line = reader.readLine();
        //goes through the lines 1 by 1 and fixes format then adds the developer
        while (line != null) {
            String initials = removeOuterSpaces(line);

            if (initials.length() > 0) {
                developers.add(new Developer(initials));
            }

            line = reader.readLine();
        }

        reader.close();

    } catch (Exception e) {
        developers.add(new Developer("huba"));
    }
}

private String removeOuterSpaces(String text) {
    //makes  sure that we are only getting the letters that we are interested in
    //by creating the right start and end index simply by seeing which symbols are spaces
    int start = 0;
    int end = text.length() - 1;

    while (start <= end && text.charAt(start) == ' ') {
        start++;
    }

    while (end >= start && text.charAt(end) == ' ') {
        end--;
    }

    String result = "";

    for (int i = start; i <= end; i++) {
        result = result + text.charAt(i);
    }

    return result;
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
