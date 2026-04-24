package com.planner.steps;
// DELT – se kommentarer over hver step-metode (fordeling pr. service)

import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.Absence;
import com.planner.domain.Project;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.AbsenceRepository;
import com.planner.repository.ProjectRepository;
import com.planner.service.ActivityService;
import com.planner.service.AvailabilityService;
import com.planner.service.DeveloperService;
import com.planner.service.AbsenceService;
import com.planner.service.ProjectService;
import com.planner.service.TimeRegistrationService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StepDefinitions {

    private DeveloperRepository developerRepository;
    private ProjectRepository projectRepository;
    private AbsenceRepository absenceRepository;
    private ProjectService projectService;
    private ActivityService activityService;
    private AvailabilityService availabilityService;
    private DeveloperService developerService;
    private TimeRegistrationService timeRegistrationService;
    private AbsenceService absenceService;

    private Project currentProject;
    private List<Developer> availableDevelopers;
    private Exception thrownException;
    private double calculatedProgress;
    private double todayHours;

    // DELT – bruges af alle scenarier
    @Before
    public void setUp() {
        developerRepository = new DeveloperRepository();
        projectRepository = new ProjectRepository();
        absenceRepository = new AbsenceRepository();
        projectService = new ProjectService(projectRepository, developerRepository);
        activityService = new ActivityService(projectRepository, developerRepository);
        availabilityService = new AvailabilityService(projectRepository, developerRepository, absenceRepository);
        developerService = new DeveloperService(developerRepository);
        timeRegistrationService = new TimeRegistrationService(projectRepository, developerRepository);
        absenceService = new AbsenceService(absenceRepository, developerRepository, projectRepository);
    }

    // Mathias
    @Given("the system has a developer with initials {string}")
    public void theSystemHasADeveloperWithInitials(String initials) {
        assertTrue(developerService.developerExists(initials));
    }

    // Vedanta
    @Given("a project with name {string} exists")
    public void aProjectWithNameExists(String name) {
        currentProject = projectService.createProject(name);
    }

    // Viktor
    @Given("the project has an activity named {string}")
    public void theProjectHasAnActivityNamed(String activityName) {
        activityService.createActivity(currentProject.getId(), activityName);
    }

    // Viktor
    @Given("the project has an activity {string} with a budget of {double} hours")
    public void theProjectHasAnActivityWithBudget(String activityName, double budget) {
        activityService.createActivity(currentProject.getId(), activityName);
        activityService.setActivityDetails(currentProject.getId(), activityName, budget, 0, 0, 0, 0);
    }

    // Viktor
    @Given("the activity has weeks {int} {int} to {int} {int}")
    public void theActivityHasWeeks(int startWeek, int startYear, int endWeek, int endYear) {
        Activity activity = currentProject.getActivities().get(currentProject.getActivities().size() - 1);
        activity.setStartWeek(startWeek);
        activity.setStartYear(startYear);
        activity.setEndWeek(endWeek);
        activity.setEndYear(endYear);
    }

    // Nat
    @Given("developer {string} has registered {double} hours on activity {string}")
    public void developerHasRegisteredHoursOnActivity(String initials, double hours, String activityName) {
        timeRegistrationService.registerTime(initials, currentProject.getId(), activityName, LocalDate.now(), hours);
    }

    // Vedanta
    @When("a developer creates a project with name {string}")
    public void aDeveloperCreatesAProjectWithName(String name) {
        currentProject = projectService.createProject(name);
    }

    // Viktor
    @When("a developer creates an activity {string} for the project")
    public void aDeveloperCreatesAnActivity(String activityName) {
        activityService.createActivity(currentProject.getId(), activityName);
    }

    // Mathias
    @When("developer {string} is added to the activity {string}")
    public void developerIsAddedToActivity(String initials, String activityName) {
        activityService.addDeveloperToActivity(currentProject.getId(), activityName, initials);
    }

    // Vedanta
    @When("developer {string} is assigned as project leader")
    public void developerIsAssignedAsProjectLeader(String initials) {
        projectService.assignProjectLeader(currentProject.getId(), initials);
    }

    // Nat
    @When("developer {string} registers {double} hours on activity {string}")
    public void developerRegistersHoursOnActivity(String initials, double hours, String activityName) {
        timeRegistrationService.registerTime(initials, currentProject.getId(), activityName, LocalDate.now(), hours);
    }

    // Vedanta
    @When("a report is generated for the project")
    public void aReportIsGeneratedForTheProject() {
        // Report is generated via getProject and reading activities
    }

    // Vedanta
    @Then("a project with name {string} exists in the system")
    public void aProjectWithNameExistsInTheSystem(String name) {
        List<Project> projects = projectService.getAllProjects();
        assertTrue(projects.stream().anyMatch(p -> p.getName().equals(name)));
    }

    // Viktor
    @Then("the project {string} has an activity named {string}")
    public void theProjectHasAnActivityNamed(String projectName, String activityName) {
        List<Project> projects = projectService.getAllProjects();
        Project project = projects.stream()
                .filter(p -> p.getName().equals(projectName))
                .findFirst()
                .orElseThrow();
        assertTrue(project.getActivities().stream().anyMatch(a -> a.getName().equals(activityName)));
    }

    // Mathias
    @Then("activity {string} has developer {string} assigned")
    public void activityHasDeveloperAssigned(String activityName, String initials) {
        Activity activity = activityService.getActivity(currentProject.getId(), activityName);
        assertTrue(activity.getAssignedDevelopers().stream()
                .anyMatch(d -> d.getInitials().equals(initials)));
    }

    // Vedanta
    @Then("the project leader of {string} is {string}")
    public void theProjectLeaderIs(String projectName, String initials) {
        Project project = projectService.getAllProjects().stream()
                .filter(p -> p.getName().equals(projectName))
                .findFirst()
                .orElseThrow();
        assertNotNull(project.getProjectLeader());
        assertEquals(initials, project.getProjectLeader().getInitials());
    }

    // Nat
    @Then("activity {string} has {double} registered hours")
    public void activityHasRegisteredHours(String activityName, double expectedHours) {
        Activity activity = activityService.getActivity(currentProject.getId(), activityName);
        assertEquals(expectedHours, activity.getTotalRegisteredHours(), 0.001);
    }

    // Vedanta
    @Then("the report shows {double} budgeted hours and {double} registered hours for {string}")
    public void theReportShowsBudgetedAndRegisteredHours(double budgeted, double registered, String activityName) {
        Activity activity = activityService.getActivity(currentProject.getId(), activityName);
        assertEquals(budgeted, activity.getBudgetedHours(), 0.001);
        assertEquals(registered, activity.getTotalRegisteredHours(), 0.001);
    }

    // --- Fixed activity steps ---

    // Nicolai
    @When("developer {string} registers vacation from week {int} {int} to week {int} {int}")
    public void developerRegistersVacation(String initials, int startWeek, int startYear, int endWeek, int endYear) {
        absenceService.registerAbsence(initials, Absence.Type.VACATION,
                startWeek, startYear, endWeek, endYear);
    }
    
    // Nicolai
    @When("developer {string} tries to register vacation from week {int} {int} to week {int} {int}")
    public void developer_tries_to_register_vacation_from_week_to_week(String initials, Integer startWeek, Integer startYear, Integer endWeek, Integer endYear) {
        try {
            absenceService.registerAbsence(initials, Absence.Type.VACATION,
                startWeek, startYear, endWeek, endYear);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    // Nicolai
    @When("developer {string} tries to register sick leave from week {int} {int} to week {int} {int}")
    public void developerTriesToRegisterSickLeave(String initials, Integer startWeek, Integer startYear, Integer endWeek, Integer endYear) {
        try {
            absenceService.registerAbsence(initials, Absence.Type.SICK_LEAVE,
                startWeek, startYear, endWeek, endYear);
        } catch (Exception e) {
            thrownException = e;
        }
    }
    // Nicolai
    @Then("developer {string} is busy in week {int} {int}")
    public void developerIsBusyInWeek(String initials, int week, int year) {
        assertTrue(absenceService.isDeveloperAbsent(initials, week, year));
    }

    // Nicolai
    @Then("developer {string} is not busy in week {int} {int}")
    public void developerIsNotBusyInWeek(String initials, int week, int year) {
        assertFalse(absenceService.isDeveloperAbsent(initials, week, year));
    }

    // --- Available developers steps ---

    // Mathias
    @When("available developers in week {int} {int} are requested")
    public void availableDevelopersAreRequested(int week, int year) {
        availableDevelopers = availabilityService.getAvailableDevelopers(week, year);
    }

    // Mathias
    @Then("developer {string} is in the available list")
    public void developerIsInAvailableList(String initials) {
        assertTrue(availableDevelopers.stream().anyMatch(d -> d.getInitials().equals(initials)));
    }

    // Mathias
    @Then("developer {string} is not in the available list")
    public void developerIsNotInAvailableList(String initials) {
        assertFalse(availableDevelopers.stream().anyMatch(d -> d.getInitials().equals(initials)));
    }

    // --- Set deadline steps ---

    // Vedanta
    @When("a deadline is set to week {int} year {int} for the project")
    public void aDeadlineIsSetForTheProject(int week, int year) {
        try {
            projectService.setDeadline(currentProject.getId(), week, year);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    // Vedanta
    @Then("the project has deadline week {int} year {int}")
    public void theProjectHasDeadline(int week, int year) {
        Project project = projectService.getProject(currentProject.getId());
        assertEquals(week, project.getDeadlineWeek());
        assertEquals(year, project.getDeadlineYear());
    }

    // --- View project progress steps ---

    // Vedanta
    @When("the project progress is calculated")
    public void theProjectProgressIsCalculated() {
        calculatedProgress = projectService.getProjectProgress(currentProject.getId());
    }

    // Vedanta
    @Then("the progress is {double} percent")
    public void theProgressIs(double expectedPercent) {
        assertEquals(expectedPercent, calculatedProgress, 0.001);
    }

    // --- Error scenario steps ---

    // Vedanta
    @When("a developer tries to create a project with an empty name")
    public void aDeveloperTriesToCreateProjectWithEmptyName() {
        try {
            projectService.createProject("");
        } catch (Exception e) {
            thrownException = e;
        }
    }

    // Viktor
    @When("a developer tries to create an activity {string} for project {string}")
    public void aDeveloperTriesToCreateActivityForNonExistentProject(String activityName, String projectId) {
        try {
            activityService.createActivity(projectId, activityName);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    // Mathias
    @When("a developer tries to add unknown initials {string} to activity {string}")
    public void aDeveloperTriesToAddUnknownDeveloperToActivity(String initials, String activityName) {
        try {
            activityService.addDeveloperToActivity(currentProject.getId(), activityName, initials);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    // Nat
    @When("developer {string} tries to register {double} hours on activity {string}")
    public void developerTriesToRegisterInvalidHours(String initials, double hours, String activityName) {
        try {
            timeRegistrationService.registerTime(initials, currentProject.getId(), activityName, java.time.LocalDate.now(), hours);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    // Vedanta
    @When("a developer tries to assign unknown initials {string} as project leader")
    public void aDeveloperTriesToAssignUnknownProjectLeader(String initials) {
        try {
            projectService.assignProjectLeader(currentProject.getId(), initials);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    // Nat
    @Given("developer {string} registers {double} hours on activity {string} on date {string}")
    public void developerRegistersHoursOnActivityOnDate(String initials, double hours, String activityName, String dateStr) {
        timeRegistrationService.registerTime(initials, currentProject.getId(), activityName,
                LocalDate.parse(dateStr), hours);
    }

    // Nat
    @When("developer {string} edits the registration on {string} for activity {string} to {double} hours")
    public void developerEditsTimeRegistration(String initials, String dateStr, String activityName, double newHours) {
        timeRegistrationService.editTimeRegistration(initials, currentProject.getId(), activityName,
                LocalDate.parse(dateStr), newHours);
    }

    // Nat
    @When("developer {string} tries to edit the registration on {string} for activity {string} to {double} hours")
    public void developerTriesToEditTimeRegistration(String initials, String dateStr, String activityName, double newHours) {
        try {
            timeRegistrationService.editTimeRegistration(initials, currentProject.getId(), activityName,
                    LocalDate.parse(dateStr), newHours);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    // Nat
    @When("the total hours for developer {string} on date {string} are requested")
    public void totalHoursForDeveloperOnDateRequested(String initials, String dateStr) {
        todayHours = timeRegistrationService.getTodayHours(initials, LocalDate.parse(dateStr));
    }

    // Nat
    @Then("the total hours for the day is {double}")
    public void totalHoursForTheDayIs(double expected) {
        assertEquals(expected, todayHours, 0.001);
    }

    // Viktor
    @When("a developer tries to create an activity with an empty name for the project")
    public void aDeveloperTriesToCreateActivityWithEmptyName() {
        try {
            activityService.createActivity(currentProject.getId(), "");
        } catch (Exception e) {
            thrownException = e;
        }
    }

    // Viktor
    @When("a developer tries to set budget {double} on activity {string}")
    public void aDeveloperTriesToSetBudgetOnActivity(double budget, String activityName) {
        try {
            activityService.setActivityDetails(currentProject.getId(), activityName, budget, 0, 0, 0, 0);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    // Viktor
    @When("a developer tries to set activity {string} weeks {int} {int} to {int} {int}")
    public void aDeveloperTriesToSetActivityWeeks(String activityName, int startWeek, int startYear, int endWeek, int endYear) {
        try {
            activityService.setActivityDetails(currentProject.getId(), activityName, 10.0, startWeek, startYear, endWeek, endYear);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    // DELT – generisk fejl-check brugt på tværs af scenarier
    @Then("an error is raised with message {string}")
    public void anErrorIsRaisedWithMessage(String expectedMessage) {
        assertNotNull(thrownException, "Expected an exception but none was thrown");
        assertEquals(expectedMessage, thrownException.getMessage());
    }

    // Vedanta
    @Then("the total budgeted hours are {double} and total registered hours are {double}")
    public void theTotalHoursAre(double budgeted, double registered) {
        assertEquals(budgeted, currentProject.getTotalBudgetedHours(), 0.001);
        assertEquals(registered, currentProject.getTotalRegisteredHours(), 0.001);
    }
}
