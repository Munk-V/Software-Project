package com.planner.domain;

public class Developer {

    private final String initials;

    public Developer(String initials) {
        this.initials = initials;
    }

    public String getInitials() {
        return initials;
    }

    @Override
    public String toString() {
        return initials;
    }
}
