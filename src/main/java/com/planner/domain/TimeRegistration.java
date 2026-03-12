package com.planner.domain;

import java.time.LocalDate;

public class TimeRegistration {

    private final Developer developer;
    private final Activity activity;
    private LocalDate date;
    private double hours;

    public TimeRegistration(Developer developer, Activity activity, LocalDate date, double hours) {
        this.developer = developer;
        this.activity = activity;
        this.date = date;
        this.hours = hours;
    }

    public Developer getDeveloper() {
        return developer;
    }

    public Activity getActivity() {
        return activity;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
