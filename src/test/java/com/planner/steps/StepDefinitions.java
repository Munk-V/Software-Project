package com.planner.steps;

import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.FixedActivity;
import com.planner.domain.Project;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.FixedActivityRepository;
import com.planner.repository.ProjectRepository;
import com.planner.service.ActivityService;
import com.planner.service.DeveloperService;
import com.planner.service.FixedActivityService;
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
    private FixedActivityRepository fixedActivityRepository;
    private ProjectService projectService;
    private ActivityService activityService;
    private DeveloperService developerService;
    private TimeRegistrationService timeRegistrationService;
    private FixedActivityService fixedActivityService;

    private Project currentProject;
    private List<Developer> availableDevelopers;

    @Before
    public void setUp() {
        developerRepository = new DeveloperRepository();
        projectRepository = new ProjectRepository();
        fixedActivityRepository = new FixedActivityRepository();
        projectService = new ProjectService(projectRepository, developerRepository);
        activityService = new ActivityService(projectRepository, developerRepository, fixedActivityRepository);
        developerService = new DeveloperService(developerRepository);
        timeRegistrationService = new TimeRegistrationService(projectRepository, developerRepository);
        fixedActivityService = new FixedActivityService(fixedActivityRepository, developerRepository);
    }

    @Given("the system has a developer with initials {string}")
    public void theSystemHasADeveloperWithInitials(String initials) {
        assertTrue(developerService.developerExists(initials));
    }

    @Given("a project with name {string} exists")
    public void aProjectWithNameExists(String name) {
        currentProject = projectService.createProject(name);
    }

    @Given("the project has an activity named {string}")
    public void theProjectHasAnActivityNamed(String activityName) {
        activityService.createActivity(currentProject.getId(), activityName);
    }

    @Given("the project has an activity {string} with a budget of {double} hours")
    public void theProjectHasAnActivityWithBudget(String activityName, double budget) {
        activityService.createActivity(currentProject.getId(), activityName);
        activityService.setActivityDetails(currentProject.getId(), activityName, budget, 0, 0, 0, 0);
    }

    @Given("the activity has weeks {int} {int} to {int} {int}")
    public void theActivityHasWeeks(int startWeek, int startYear, int endWeek, int endYear) {
        Activity activity = currentProject.getActivities().get(currentProject.getActivities().size() - 1);
        activity.setStartWeek(startWeek);
        activity.setStartYear(startYear);
        activity.setEndWeek(endWeek);
        activity.setEndYear(endYear);
    }

    @Given("developer {string} has registered {double} hours on activity {string}")
    public void developerHasRegisteredHoursOnActivity(String initials, double hours, String activityName) {
        timeRegistrationService.registerTime(initials, currentProject.getId(), activityName, LocalDate.now(), hours);
    }

    @When("a developer creates a project with name {string}")
    public void aDeveloperCreatesAProjectWithName(String name) {
        currentProject = projectService.createProject(name);
    }

    @When("a developer creates an activity {string} for the project")
    public void aDeveloperCreatesAnActivity(String activityName) {
        activityService.createActivity(currentProject.getId(), activityName);
    }

    @When("developer {string} is added to the activity {string}")
    public void developerIsAddedToActivity(String initials, String activityName) {
        activityService.addDeveloperToActivity(currentProject.getId(), activityName, initials);
    }

    @When("developer {string} is assigned as project leader")
    public void developerIsAssignedAsProjectLeader(String initials) {
        projectService.assignProjectLeader(currentProject.getId(), initials);
    }

    @When("developer {string} registers {double} hours on activity {string}")
    public void developerRegistersHoursOnActivity(String initials, double hours, String activityName) {
        timeRegistrationService.registerTime(initials, currentProject.getId(), activityName, LocalDate.now(), hours);
    }

    @When("a report is generated for the project")
    public void aReportIsGeneratedForTheProject() {
        // Report is generated via getProject and reading activities
    }

    @Then("a project with name {string} exists in the system")
    public void aProjectWithNameExistsInTheSystem(String name) {
        List<Project> projects = projectService.getAllProjects();
        assertTrue(projects.stream().anyMatch(p -> p.getName().equals(name)));
    }

    @Then("the project {string} has an activity named {string}")
    public void theProjectHasAnActivityNamed(String projectName, String activityName) {
        List<Project> projects = projectService.getAllProjects();
        Project project = projects.stream()
                .filter(p -> p.getName().equals(projectName))
                .findFirst()
                .orElseThrow();
        assertTrue(project.getActivities().stream().anyMatch(a -> a.getName().equals(activityName)));
    }

    @Then("activity {string} has developer {string} assigned")
    public void activityHasDeveloperAssigned(String activityName, String initials) {
        Activity activity = activityService.getActivity(currentProject.getId(), activityName);
        assertTrue(activity.getAssignedDevelopers().stream()
                .anyMatch(d -> d.getInitials().equals(initials)));
    }

    @Then("the project leader of {string} is {string}")
    public void theProjectLeaderIs(String projectName, String initials) {
        Project project = projectService.getAllProjects().stream()
                .filter(p -> p.getName().equals(projectName))
                .findFirst()
                .orElseThrow();
        assertNotNull(project.getProjectLeader());
        assertEquals(initials, project.getProjectLeader().getInitials());
    }

    @Then("activity {string} has {double} registered hours")
    public void activityHasRegisteredHours(String activityName, double expectedHours) {
        Activity activity = activityService.getActivity(currentProject.getId(), activityName);
        assertEquals(expectedHours, activity.getTotalRegisteredHours(), 0.001);
    }

    @Then("the report shows {double} budgeted hours and {double} registered hours for {string}")
    public void theReportShowsBudgetedAndRegisteredHours(double budgeted, double registered, String activityName) {
        Activity activity = activityService.getActivity(currentProject.getId(), activityName);
        assertEquals(budgeted, activity.getBudgetedHours(), 0.001);
        assertEquals(registered, activity.getTotalRegisteredHours(), 0.001);
    }

    // --- Fixed activity steps ---

    @When("developer {string} registers vacation from week {int} {int} to week {int} {int}")
    public void developerRegistersVacation(String initials, int startWeek, int startYear, int endWeek, int endYear) {
        fixedActivityService.registerFixedActivity(initials, FixedActivity.Type.VACATION,
                startWeek, startYear, endWeek, endYear);
    }

    @Then("developer {string} is busy in week {int} {int}")
    public void developerIsBusyInWeek(String initials, int week, int year) {
        assertTrue(fixedActivityService.isDeveloperBusy(initials, week, year));
    }

    @Then("developer {string} is not busy in week {int} {int}")
    public void developerIsNotBusyInWeek(String initials, int week, int year) {
        assertFalse(fixedActivityService.isDeveloperBusy(initials, week, year));
    }

    // --- Available developers steps ---

    @When("available developers in week {int} {int} are requested")
    public void availableDevelopersAreRequested(int week, int year) {
        availableDevelopers = activityService.getAvailableDevelopers(week, year);
    }

    @Then("developer {string} is in the available list")
    public void developerIsInAvailableList(String initials) {
        assertTrue(availableDevelopers.stream().anyMatch(d -> d.getInitials().equals(initials)));
    }

    @Then("developer {string} is not in the available list")
    public void developerIsNotInAvailableList(String initials) {
        assertFalse(availableDevelopers.stream().anyMatch(d -> d.getInitials().equals(initials)));
    }
}
