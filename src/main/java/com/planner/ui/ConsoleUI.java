package com.planner.ui;
// Mathias

import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.Project;
import com.planner.domain.Absence;
import com.planner.service.AbsenceService;
import com.planner.service.ActivityService;
import com.planner.service.AvailabilityService;
import com.planner.service.DeveloperService;
import com.planner.service.ProjectService;
import com.planner.service.TimeRegistrationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {

    private final ProjectService projectService;
    private final ActivityService activityService;
    private final AvailabilityService availabilityService;
    private final DeveloperService developerService;
    private final TimeRegistrationService timeRegistrationService;
    private final AbsenceService absenceService;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleUI(AppContext context) {
        this.projectService = context.projectService;
        this.activityService = context.activityService;
        this.availabilityService = context.availabilityService;
        this.developerService = context.developerService;
        this.timeRegistrationService = context.timeRegistrationService;
        this.absenceService = context.absenceService;
    }

    public void start() {
        System.out.println("=== Software Project Planner ===");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createProject();
                case "2" -> listProjects();
                case "3" -> createActivity();
                case "4" -> addDeveloperToActivity();
                case "5" -> assignProjectLeader();
                case "6" -> registerTime();
                case "7" -> printReport();
                case "8" -> registerAbsence();
                case "9" -> showAvailableDevelopers();
                case "0" -> running = false;
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
        System.out.println("Goodbye!");
    }

    private void printMenu() {
        System.out.println("\n--- Menu ---");
        System.out.println("1. Create project");
        System.out.println("2. List projects");
        System.out.println("3. Create activity");
        System.out.println("4. Add developer to activity");
        System.out.println("5. Assign project leader");
        System.out.println("6. Register time");
        System.out.println("7. Print project report");
        System.out.println("8. Register fixed activity (vacation/sick/course)");
        System.out.println("9. Show available developers in a week");
        System.out.println("0. Exit");
        System.out.print("Choice: ");
    }

    private void createProject() {
        System.out.print("Project name: ");
        String name = scanner.nextLine().trim();
        Project project = projectService.createProject(name);
        System.out.println("Project created with ID: " + project.getId());
    }

    private void listProjects() {
        List<Project> projects = projectService.getAllProjects();
        if (projects.isEmpty()) {
            System.out.println("No projects.");
            return;
        }
        for (Project p : projects) {
            String leader = p.getProjectLeader() != null ? p.getProjectLeader().getInitials() : "none";
            System.out.printf("[%s] %s  (leader: %s, activities: %d)%n",
                    p.getId(), p.getName(), leader, p.getActivities().size());
        }
    }

    private void createActivity() {
        System.out.print("Project ID: ");
        String projectId = scanner.nextLine().trim();
        System.out.print("Activity name: ");
        String activityName = scanner.nextLine().trim();
        activityService.createActivity(projectId, activityName);
        System.out.println("Activity created: " + activityName);
    }

    private void addDeveloperToActivity() {
        System.out.print("Project ID: ");
        String projectId = scanner.nextLine().trim();
        System.out.print("Activity name: ");
        String activityName = scanner.nextLine().trim();
        System.out.print("Developer initials: ");
        String initials = scanner.nextLine().trim();
        activityService.addDeveloperToActivity(projectId, activityName, initials);
        System.out.println("Developer " + initials + " added to activity.");
    }

    private void assignProjectLeader() {
        System.out.print("Project ID: ");
        String projectId = scanner.nextLine().trim();
        System.out.print("Developer initials: ");
        String initials = scanner.nextLine().trim();
        projectService.assignProjectLeader(projectId, initials);
        System.out.println("Project leader set to: " + initials);
    }

    private void registerTime() {
        System.out.print("Your initials: ");
        String initials = scanner.nextLine().trim();
        System.out.print("Project ID: ");
        String projectId = scanner.nextLine().trim();
        System.out.print("Activity name: ");
        String activityName = scanner.nextLine().trim();
        System.out.print("Hours (e.g. 2.5): ");
        double hours = Double.parseDouble(scanner.nextLine().trim());
        timeRegistrationService.registerTime(initials, projectId, activityName, LocalDate.now(), hours);
        System.out.println("Time registered: " + hours + " hours.");
    }

    private void printReport() {
        System.out.print("Project ID: ");
        String projectId = scanner.nextLine().trim();
        Project project = projectService.getProject(projectId);
        System.out.println("\n=== Report: " + project.getName() + " [" + project.getId() + "] ===");
        for (Activity a : project.getActivities()) {
            System.out.printf("  %-25s  budgeted: %6.1f h  registered: %6.1f h%n",
                    a.getName(), a.getBudgetedHours(), a.getTotalRegisteredHours());
        }
        System.out.printf("%-27s  budgeted: %6.1f h  registered: %6.1f h%n",
                "TOTAL", project.getTotalBudgetedHours(), project.getTotalRegisteredHours());
    }

    private void registerAbsence() {
        System.out.print("Your initials: ");
        String initials = scanner.nextLine().trim();
        System.out.println("Type (VACATION / SICK_LEAVE / COURSE / OTHER): ");
        Absence.Type type = Absence.Type.valueOf(scanner.nextLine().trim().toUpperCase());
        System.out.print("Start week (e.g. 10): ");
        int startWeek = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Start year (e.g. 2026): ");
        int startYear = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("End week: ");
        int endWeek = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("End year: ");
        int endYear = Integer.parseInt(scanner.nextLine().trim());
        absenceService.registerAbsence(initials, type, startWeek, startYear, endWeek, endYear);
        System.out.println("Absence registered.");
    }

    private void showAvailableDevelopers() {
        System.out.print("Week number: ");
        int week = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Year: ");
        int year = Integer.parseInt(scanner.nextLine().trim());
        List<Developer> available = availabilityService.getAvailableDevelopers(week, year);
        if (available.isEmpty()) {
            System.out.println("No available developers in week " + week + "/" + year);
        } else {
            System.out.println("Available developers in week " + week + "/" + year + ":");
            available.forEach(d -> System.out.println("  - " + d.getInitials()));
        }
    }

    public static void main(String[] args) {
        new ConsoleUI(new AppContext()).start();
    }
}
