package com.planner.ui;

import com.planner.domain.Absence;
import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.Project;
import com.planner.domain.TimeRegistration;
import com.planner.repository.AbsenceRepository;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.ProjectRepository;
import com.planner.service.AbsenceService;
import com.planner.service.ActivityService;
import com.planner.service.AvailabilityService;
import com.planner.service.DeveloperService;
import com.planner.service.ProjectService;
import com.planner.service.TimeRegistrationService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// Nicolai and Viktor
// The UI as it is now was made late in development.
// An early prototype was used as starting point, which this version in structue/layout is based on. (vbox, hbox, grid etc. )




public class MainWindow {

    // logic layer
    private final ProjectService projectService;
    private final ActivityService activityService;
    private final DeveloperService developerService;
    private final TimeRegistrationService timeRegistrationService;
    private final AbsenceService absenceService;
    private final AvailabilityService availabilityService;

    private final BorderPane root = new BorderPane();
    private String currentUserInitials;


    // shared state, so all selections in the project stays synchronized
    private final ObservableList<String> projectItems = FXCollections.observableArrayList();
    private final ObservableList<String> developerItems = FXCollections.observableArrayList();
    private final ObservableList<String> activityItems = FXCollections.observableArrayList();

    // ui components 
    private final ListView<String> projectList = new ListView<>(projectItems);
    private final TableView<Activity> activityTable = new TableView<>();
    private final TableView<Activity> reportTable = new TableView<>();
    private final Label selectedProjectLabel = new Label("No project selected");
    private final Label projectInfoLabel = new Label("");
    private final Label reportTotalLabel = new Label("");

    public MainWindow(AppContext context) {

        // repos for data storage
        DeveloperRepository developerRepository = new DeveloperRepository();
        ProjectRepository projectRepository = new ProjectRepository();
        AbsenceRepository absenceRepository = new AbsenceRepository();

        projectService = new ProjectService(projectRepository, developerRepository);
        activityService = new ActivityService(projectRepository, developerRepository);
        developerService = new DeveloperService(developerRepository);
        timeRegistrationService = new TimeRegistrationService(projectRepository, developerRepository);
        absenceService = new AbsenceService(absenceRepository, developerRepository, projectRepository);
        availabilityService = new AvailabilityService(projectRepository, developerRepository, absenceRepository);

        currentUserInitials = showLoginDialog();
        if (currentUserInitials == null) {
            Platform.exit();
            return;
        }

        buildUI();
        refreshDevelopers();
    }


    // login implemtation
    private String showLoginDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Login");
        
        // setting textfield
        TextField initialsField = new TextField();
        initialsField.setPromptText("Your initials");
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");
        Button loginButton = new Button("Login");

        
        // login button
        loginButton.setOnAction(e -> {
            String initials = initialsField.getText().trim();
            boolean found = developerService.getAllDevelopers().stream()
                    .anyMatch(d -> d.getInitials().equalsIgnoreCase(initials));
            if (found) {
                dialog.setUserData(initials.toLowerCase());
                dialog.close();
            } else {
                errorLabel.setText("Unknown initials: " + initials);
            }
        });

        initialsField.setOnAction(e -> loginButton.fire());

        VBox box = new VBox(10,
                new Label("Enter your initials to log in:"),
                initialsField,
                loginButton,
                errorLabel
        );
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER_LEFT);

        dialog.setOnCloseRequest(e -> Platform.exit());
        dialog.setScene(new Scene(box, 280, 160));
        dialog.showAndWait();

        return dialog.getUserData() != null ? (String) dialog.getUserData() : null;
    }

    // builds the main layout.
    // project selector to the left and all features in the center
    private void buildUI() {
        root.setPadding(new Insets(10));
        root.setLeft(buildProjectPanel());

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                buildProjectTab(),
                buildActivityTab(),
                buildTimeTab(),
                buildAbsenceAndAvailabilityTab(),
                buildReportTab()
        );
        root.setCenter(tabs);
    }

    private VBox buildProjectPanel() {
        projectList.setPrefWidth(200);
        projectList.setPrefHeight(400);

        // Updates the selected project, when the user wants to cick on a project in the list to the left.
        projectList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> refreshSelectedProject());

        VBox box = new VBox(8,
                new Label("Projects"),
                projectList,
                selectedProjectLabel
        );
        return box;
    }

    // all fields for project tab
    private Tab buildProjectTab() {
        TextField projectName = new TextField();
        TextField startWeek = new TextField();
        TextField startYear = new TextField(String.valueOf(LocalDate.now().getYear()));
        TextField deadlineWeek = new TextField();
        TextField deadlineYear = new TextField(String.valueOf(LocalDate.now().getYear()));
        ComboBox<String> leaderBox = new ComboBox<>(developerItems);
        TextField newDeveloper = new TextField();

        setPrompt(projectName, "Project name");
        setPrompt(startWeek, "Start week");
        setPrompt(deadlineWeek, "Deadline week");
        setPrompt(newDeveloper, "Initials");
        leaderBox.setPromptText("Project leader");

        Button createProject = new Button("Create project");

        // creates the project, THEN optionally adds all deadlines and activities
        createProject.setOnAction(e -> runAction(() -> {
            Project project = projectService.createProject(projectName.getText().trim());

            if (!startWeek.getText().isBlank()) {
                projectService.setStart(project.getId(), parseInt(startWeek), parseInt(startYear));
            }
            if (!deadlineWeek.getText().isBlank()) {
                projectService.setDeadline(project.getId(), parseInt(deadlineWeek), parseInt(deadlineYear));
            }

            projectItems.add(projectText(project));
            projectList.getSelectionModel().selectLast();
            projectName.clear();
            startWeek.clear();
            deadlineWeek.clear();
        }));

        
        Button setLeader = new Button("Set leader");
        setLeader.setOnAction(e -> runAction(() -> {
            String projectId = requireProjectId();
            String initials = requireValue(leaderBox.getValue(), "Choose a project leader.");
            projectService.assignProjectLeader(projectId, initials);
            refreshSelectedProject();
        }));

        Button addDeveloper = new Button("Add developer");
        addDeveloper.setOnAction(e -> runAction(() -> {
            developerService.registerDeveloper(newDeveloper.getText().trim());
            newDeveloper.clear();
            refreshDevelopers();// keeps all oter boxes updated with the new developer
        }));

        VBox content = page(
                new Label("Create project"),
                form(
                        row("Name", projectName),
                        row("Start", startWeek, startYear),
                        row("Deadline", deadlineWeek, deadlineYear)
                ),
                createProject,
                
                new Label("Project leader"),
                row("Leader", leaderBox, setLeader),
                
                new Label("Developers"),
                row("New developer", newDeveloper, addDeveloper),
                projectInfoLabel
                
        );

        return tab("Project", content);
    }

    private Tab buildActivityTab() {

        //all activity fields
        setupActivityTable(activityTable);
        activityTable.setPrefHeight(250);
        
        TextField activityName = new TextField();
        TextField budget = new TextField();
        TextField startWeek = new TextField();
        TextField startYear = new TextField();
        TextField endWeek = new TextField();
        TextField endYear = new TextField();
        ComboBox<String> activityBox = new ComboBox<>(activityItems);
        ComboBox<String> developerBox = new ComboBox<>(developerItems);
        

        setPrompt(activityName, "Activity name");
        setPrompt(budget, "Budget hours");
        setPrompt(startWeek, "Start week");
        setPrompt(startYear, "Start year");
        setPrompt(endWeek, "End week");
        setPrompt(endYear, "End year");
        activityBox.setPromptText("Activity");
        developerBox.setPromptText("Developer");

        Button createActivity = new Button("Create activity");
        createActivity.setOnAction(e -> runAction(() -> {
            String projectId = requireProjectId();
            String name = activityName.getText().trim();
            activityService.createActivity(projectId, name);

            if (!budget.getText().isBlank()) {
                try {
                    activityService.setActivityDetails(
                            projectId, name, parseDouble(budget), optionalInt(startWeek),
                            optionalInt(startYear), optionalInt(endWeek), optionalInt(endYear)
                    );
                } catch (Exception ex) {
                    activityService.deleteActivity(projectId, name);
                    throw ex;
                }
            }

            activityName.clear();
            budget.clear();
            startWeek.clear();
            startYear.clear();
            endWeek.clear();
            endYear.clear();
            refreshSelectedProject();
        }));

        Button assignDeveloper = new Button("Assign developer");
        assignDeveloper.setOnAction(e -> runAction(() -> {
            activityService.addDeveloperToActivity(
                    requireProjectId(), 
                    requireValue(activityBox.getValue(), "Choose an activity."),
                    requireValue(developerBox.getValue(), "Choose a developer.")
            );
            refreshSelectedProject();
        }));

        VBox content = page(
                new Label("Activities for selected project"),
                activityTable,
                new Label("Create activity"),
                form(
                        row("Name", activityName),
                        row("Budget", budget),
                        row("Start", startWeek, startYear),
                        row("End", endWeek, endYear)
                ),
                createActivity,
                new Label("Assign developer to activity"),
                row("Activity", activityBox),
                row("Developer", developerBox),
                assignDeveloper
        );
        return tab("Activities", content);
    }

    // builds tabs for time registration
    private Tab buildTimeTab() {
        // Fields for registering, editing and checking time registrations.
        ComboBox<String> regDeveloper = new ComboBox<>(developerItems);
        ComboBox<String> regActivity = new ComboBox<>(activityItems);
        TextField regHours = new TextField();
        TextField regComment = new TextField();
        DatePicker regDate = new DatePicker(LocalDate.now());

        ComboBox<String> editDeveloper = new ComboBox<>(developerItems);
        ComboBox<String> editActivity = new ComboBox<>(activityItems);
        TextField editHours = new TextField();
        DatePicker editDate = new DatePicker(LocalDate.now());

        ComboBox<String> checkDeveloper = new ComboBox<>(developerItems);
        DatePicker checkDate = new DatePicker(LocalDate.now());
        Label checkResult = new Label("");

        regDeveloper.setValue(currentUserInitials);
        regActivity.setPromptText("Activity");
        editDeveloper.setPromptText("Developer");
        editActivity.setPromptText("Activity");
        checkDeveloper.setPromptText("Developer");
        setPrompt(regHours, "Hours");
        setPrompt(regComment, "Comment (optional)");
        setPrompt(editHours, "New hours");

        // reads the values from UI, sends logic to service layer.
        Button registerTime = new Button("Register time");

        // Runs when user clicks register time button. (event)
        registerTime.setOnAction(e -> runAction(() -> {
            timeRegistrationService.registerTime(
                    requireValue(regDeveloper.getValue(), "Choose a developer."), requireProjectId(),
                    requireValue(regActivity.getValue(), "Choose an activity."), regDate.getValue(),
                    parseDouble(regHours), regComment.getText().trim()
            );
            regHours.clear();
            regComment.clear();
            refreshSelectedProject();
        }));

        Button editTime = new Button("Edit time");
        // same runs when action (event)
        editTime.setOnAction(e -> runAction(() -> {
            timeRegistrationService.editTimeRegistration(
                    requireValue(editDeveloper.getValue(), "Choose a developer."), requireProjectId(),
                    requireValue(editActivity.getValue(), "Choose an activity."), editDate.getValue(),
                    parseDouble(editHours)
            );
            editHours.clear();
            refreshSelectedProject();
        }));

        Button checkHours = new Button("Check hours");
        checkHours.setOnAction(e -> runAction(() -> {
            String initials = requireValue(checkDeveloper.getValue(), "Choose a developer.");
            double hours = timeRegistrationService.getTodayHours(initials, checkDate.getValue());
            checkResult.setText(initials + ": " + hours + " hours on " + checkDate.getValue());
     }));

        VBox content = page(
                new Label("Register time"),
                row("Developer", regDeveloper),
                row("Activity", regActivity),
                row("Date", regDate),
                row("Hours", regHours),
                row("Comment", regComment),
                registerTime,
                new Label("Edit time registration"),
                row("Developer", editDeveloper),
                row("Activity", editActivity),
                row("Date", editDate),
                row("Hours", editHours),
                editTime,
                new Label("Check registered hours for a day"),
                row("Developer", checkDeveloper),
                row("Date", checkDate),
                checkHours,
                checkResult );

        return tab("Time", content);
    }

    // absence and abulity tabs, pretty much the same as time with 
    private Tab buildAbsenceAndAvailabilityTab() {
        ComboBox<String> absenceDeveloper = new ComboBox<>(developerItems);
        ComboBox<String> absenceType = new ComboBox<>(FXCollections.observableArrayList("VACATION", "SICK_LEAVE", "COURSE", "OTHER"));

        TextField absenceStartWeek = new TextField();
        TextField absenceStartYear = new TextField(String.valueOf(LocalDate.now().getYear()));
        TextField absenceEndWeek = new TextField();
        TextField absenceEndYear = new TextField(String.valueOf(LocalDate.now().getYear()));

        TextField availableWeek = new TextField();
        TextField availableYear = new TextField(String.valueOf(LocalDate.now().getYear()));
        ListView<String> availableList = new ListView<>();

        availableList.setPrefHeight(200);

        absenceDeveloper.setValue(currentUserInitials);
        absenceType.setValue("VACATION");
        setPrompt(absenceStartWeek, "Start week");
        setPrompt(absenceEndWeek, "End week");
        setPrompt(availableWeek, "Week");

        // buttonAction = connect to AbcenseService
        Button registerAbsence = new Button("Register absence");

        registerAbsence.setOnAction(e -> runAction(() -> {
            absenceService.registerAbsence(
                    requireValue(absenceDeveloper.getValue(), "Choose a developer."),
                    Absence.Type.valueOf(absenceType.getValue()),
                    parseInt(absenceStartWeek),
                    parseInt(absenceStartYear),
                    parseInt(absenceEndWeek),
                    parseInt(absenceEndYear)
            );
            // clearing and refreshing.
            absenceStartWeek.clear();
            absenceEndWeek.clear();
            refreshSelectedProject();
        }));

        Button checkAvailable = new Button("Check available developers");
        checkAvailable.setOnAction(e -> runAction(() -> {
            int week = parseInt(availableWeek);
            int year = parseInt(availableYear);

            
            List<Developer> developers = availabilityService.getAvailableDevelopers(week, year);
            ObservableList<String> items = FXCollections.observableArrayList();
            if (developers.isEmpty()) {
                items.add("No available developers");
            } else {
                developers.forEach(d -> items.add(d.getInitials()));
            }
            availableList.setItems(items);
        }));

        VBox content = page(
                new Label("Register absence"),
                row("Developer", absenceDeveloper),
                row("Type", absenceType),
                row("Start", absenceStartWeek, absenceStartYear),
                row("End", absenceEndWeek, absenceEndYear),
                registerAbsence,
                new Label("Find available developers"),
                row("Week/year", availableWeek, availableYear),
                checkAvailable,
                availableList
        );
        return tab("Absence / Available", content);
    }

    // AI was used for this function to achieve the wanted functionality.
    private Tab buildReportTab() {

        setupReportTable(reportTable);
        reportTable.setPrefHeight(250);

        Label detailLabel = new Label("");

        //list showing the details of the time reg
        ListView<String> detailList = new ListView<>();
        detailList.setPrefHeight(150);

        // Listener function that runs when a new report in table is selected 
        ///////// AI
        reportTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {

            // if nothing is selected, the ui is cleared
            if (newVal == null) {
                detailLabel.setText("");
                detailList.setItems(FXCollections.observableArrayList());
                return;
            }
            // updates the activity lavbel
            detailLabel.setText("Registrations for: " + newVal.getName());
            ObservableList<String> items = FXCollections.observableArrayList();

            // loop through all time reg for the activity
            for (TimeRegistration tr : newVal.getTimeRegistrations()) {
                String line = tr.getDeveloper().getInitials() + "  |  " + tr.getDate() + "  |  " + tr.getHours() + " h";
                if (!tr.getComment().isBlank()) line += "  |  " + tr.getComment();
                items.add(line);
            }
            detailList.setItems(items);
        });
        /////////// AI

        // generate button. same as others
        Button generateReport = new Button("Generate report for selected project");
        generateReport.setOnAction(e -> runAction(() -> {
            Project project = projectService.getProject(requireProjectId());

            List<Activity> activitiesWithHours = new java.util.ArrayList<>();
            for (Activity activity : project.getActivities()) {
                if (activity.getTotalRegisteredHours() > 0) {
                    activitiesWithHours.add(activity);
                }
            }

            // show activity.
            reportTable.setItems(FXCollections.observableArrayList(activitiesWithHours));
            //clears the old details
            detailLabel.setText("");
            detailList.setItems(FXCollections.observableArrayList());
 
            // calculater for remaining budget( help from ai)
            double remaining = project.getTotalBudgetedHours() - project.getTotalRegisteredHours();
            reportTotalLabel.setText("Budgeted: " + project.getTotalBudgetedHours()
                    + " h, registered: " + project.getTotalRegisteredHours()
                    + " h, remaining: " + remaining + " h");
        }));

        VBox content = page(
                new Label("Report"),
                generateReport,
                reportTable,
                reportTotalLabel,
                detailLabel,
                detailList
        );

        return tab("Report", content);
    }

    private void setupActivityTable(TableView<Activity> table) {

        // in activity row, getValue() = activity name
        TableColumn<Activity, String> name = new TableColumn<>("Activity");
        name.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));

        TableColumn<Activity, Number> budget = new TableColumn<>("Budget");
        budget.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getBudgetedHours()));

        TableColumn<Activity, Number> registered = new TableColumn<>("Registered");
        registered.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotalRegisteredHours()));

        TableColumn<Activity, String> weeks = new TableColumn<>("Weeks");
        weeks.setCellValueFactory(d -> new SimpleStringProperty(weekText(d.getValue())));

        TableColumn<Activity, String> developers = new TableColumn<>("Developers");
        developers.setCellValueFactory(d -> new SimpleStringProperty(developerText(d.getValue())));

        table.getColumns().setAll(name, budget, registered, weeks, developers);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupReportTable(TableView<Activity> table) {
        // same here
        TableColumn<Activity, String> name = new TableColumn<>("Activity");
        name.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));

        TableColumn<Activity, Number> budget = new TableColumn<>("Budgeted hours");
        budget.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getBudgetedHours()));

        TableColumn<Activity, Number> registered = new TableColumn<>("Registered hours");
        registered.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotalRegisteredHours()));

        // defines collumns in the activity table
        table.getColumns().setAll(name, budget, registered);
        //resizing (AI)
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }



    // refreshes 
    private void refreshSelectedProject(){
        String projectId = getSelectedProjectId();

        // if no proejct is seleted clear the activity field
        if (projectId == null) {
            selectedProjectLabel.setText("No project selected");
            activityItems.clear();
            activityTable.setItems(FXCollections.observableArrayList());
            return;
        }

        //loads the project and updates tables/label
        Project project = projectService.getProject(projectId);
        selectedProjectLabel.setText("Selected: " + project.getId() + " " + project.getName());
        projectInfoLabel.setText(projectInfo(project));

        activityItems.setAll(project.getActivities().stream()
                .map(Activity::getName)
                .collect(Collectors.toList()));
        activityTable.setItems(FXCollections.observableArrayList(project.getActivities()));
        activityTable.refresh();
    }

    // refresh function for developers
    private void refreshDevelopers() {
        developerItems.setAll(developerService.getAllDevelopers().stream()
                .map(Developer::getInitials)
                .collect(Collectors.toList()));
    }

    // Makes all project information  readable in ui
    // here ai heloed a bit as well
    private String projectInfo(Project project) {
        String leader = project.getProjectLeader() == null ? "none" : project.getProjectLeader().getInitials();
        String start = project.hasStart() ? project.getStartWeek() + "/" + project.getStartYear() : "not set";
        String deadline = project.hasDeadline() ? project.getDeadlineWeek() + "/" + project.getDeadlineYear() : "not set";
        return "Project: " + project.getId() + " " + project.getName()
                + "\nLeader: " + leader
                + "\nStart: " + start
                + "\nDeadline: " + deadline
                + "\nBudgeted hours: " + project.getTotalBudgetedHours()
                + "\nRegistered hours: " + project.getTotalRegisteredHours();
    }

    // star/end dsiplay
    private String weekText(Activity activity) {
        if (activity.getStartYear() == 0 || activity.getEndYear() == 0) {
            return "-";
        }
        return activity.getStartWeek() + "/" + activity.getStartYear()
                + " - " + activity.getEndWeek() + "/" + activity.getEndYear();
    }

    private String developerText(Activity activity) {
        if (activity.getAssignedDevelopers().isEmpty()) {
            return "-";
        }
        return activity.getAssignedDevelopers().stream()
                .map(Developer::getInitials)
                .collect(Collectors.joining(", "));
    }

    private String projectText(Project project) {
        return "[" + project.getId() + "] " + project.getName();
    }

    private String getSelectedProjectId() {
        String selected = projectList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return null;
        }
        return selected.substring(1, selected.indexOf("]"));
    }

    // proejctID
    private String requireProjectId() {
        String projectId = getSelectedProjectId();
        if (projectId == null) {
            throw new IllegalArgumentException("Select a project first.");
        }
        return projectId;
    }

    private String requireValue(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private int parseInt(TextField field) {
        return Integer.parseInt(field.getText().trim());
    }

    private int optionalInt(TextField field) {
        if (field.getText().isBlank()) {
            return 0;
        }
        return parseInt(field);
    }

    private double parseDouble(TextField field) {
        return Double.parseDouble(field.getText().trim());
    }

    private void runAction(Runnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void setPrompt(TextField field, String prompt) {
        field.setPromptText(prompt);
        field.setPrefWidth(150);
    }

    // horisontal row.
    private HBox row(String label, javafx.scene.Node... nodes) {
        Label l = new Label(label + ":");
        l.setMinWidth(100);
        HBox row = new HBox(8, l);
        row.getChildren().addAll(nodes);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
    // grid for multiple rows
    private GridPane form(HBox... rows) {
        GridPane grid = new GridPane();
        grid.setVgap(6);
        for (int i = 0; i < rows.length; i++) {
            grid.add(rows[i], 0, i);
        }
        return grid;
    }

    private VBox page(javafx.scene.Node... nodes) {
        VBox box = new VBox(10, nodes);
        box.setPadding(new Insets(12));
        box.setFillWidth(true);
        return box;
    }
    // scrollpane for scrolable tab
    private Tab tab(String title, javafx.scene.Node content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        Tab tab = new Tab(title);
        tab.setContent(scrollPane);
        return tab;
    }

    public BorderPane getRoot() {
        return root;
    }
}
