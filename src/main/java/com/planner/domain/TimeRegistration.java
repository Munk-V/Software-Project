package com.planner.domain;
// Nat
// Sets up the time registration class. 5Instances of this class are used to store timeregistrations and can be edited after the fact.
// THese are used when printing the reports, AI was used to write this code as it is just a class witch has to save a bunch of variables, that we already know
import java.time.LocalDate;

public class TimeRegistration {

    private final Developer developer;
    private final Activity activity;
    private LocalDate date;
    private double hours;
    private final String comment;
//Variabls of the class
    public TimeRegistration(Developer developer, Activity activity, LocalDate date, double hours, String comment) {
        this.developer = developer;
        this.activity = activity;
        this.date = date;
        this.hours = hours;
        this.comment = comment == null ? "" : comment;
    }
//Methods of the class
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

    public String getComment() {
        return comment;
    }
}
