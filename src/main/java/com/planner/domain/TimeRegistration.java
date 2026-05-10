package com.planner.domain;
// Nat

import java.time.LocalDate;

public class TimeRegistration {

    private final Developer developer;
    private final Activity activity;
    private LocalDate date;
    private double hours;
    private final String comment;

    public TimeRegistration(Developer developer, Activity activity, LocalDate date, double hours, String comment) {
        this.developer = developer;
        this.activity = activity;
        this.date = date;
        this.hours = hours;
        this.comment = comment == null ? "" : comment;
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

    public void setDate(LocalDate date) { //Dont think this makes sense
        this.date = date;
    }

    public String getComment() {
        return comment;
    }
}
