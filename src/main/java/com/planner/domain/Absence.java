package com.planner.domain;

public class Absence {

    public enum Type {
        VACATION, SICK_LEAVE, COURSE, OTHER
    }

    private final Developer developer;
    private final Type type;
    private final int startWeek;
    private final int startYear;
    private final int endWeek;
    private final int endYear;

    public Absence(Developer developer, Type type,
                         int startWeek, int startYear,
                         int endWeek, int endYear) {
        this.developer = developer;
        this.type = type;
        this.startWeek = startWeek;
        this.startYear = startYear;
        this.endWeek = endWeek;
        this.endYear = endYear;
    }

    public Developer getDeveloper() {
        return developer;
    }

    public Type getType() {
        return type;
    }

    public int getStartWeek() {
        return startWeek;
    }

    public int getStartYear() {
        return startYear;
    }

    public int getEndWeek() {
        return endWeek;
    }

    public int getEndYear() {
        return endYear;
    }

    public boolean isActiveInWeek(int week, int year) {
        int actStart = startYear * 100 + startWeek;
        int actEnd = endYear * 100 + endWeek;
        int target = year * 100 + week;
        return target >= actStart && target <= actEnd;
    }
}
