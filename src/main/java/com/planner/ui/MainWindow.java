package com.planner.ui;

import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.Absence;
import com.planner.domain.Project;
import com.planner.repository.DeveloperRepository;
import com.planner.repository.AbsenceRepository;
import com.planner.repository.ProjectRepository;
import com.planner.service.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.util.List;

public class MainWindow {

    private final ProjectService projectService;
    private final ActivityService activityService;
    private final DeveloperService developerService;
    private final TimeRegistrationService timeRegistrationService;
    private final AbsenceService absenceService;

    private final BorderPane root = new BorderPane();
    private final ListView<String> projectListView = new ListView<>();
    private final ObservableList<String> projectItems = FXCollections.observableArrayList();

    // Shared dropdown lists updated on project select / activity add
    private final ObservableList<String> activityNames = FXCollections.observableArrayList();
    private final ObservableList<String> developerInitialsList = FXCollections.observableArrayList();

    // Overview fields (updated on project select)
    private final Label overviewInfoLabel = new Label("Select a project to see overview.");
    private final Label overviewLeaderLabel = new Label();
    private final Label overviewDeadlineLabel = new Label();
    private final ProgressBar overviewProgressBar = new ProgressBar(0);
    private final Label overviewProgressLabel = new Label();
    private final Label overviewRemainingLabel = new Label();
    private final TableView<Activity> overviewTable = new TableView<>();
    private final TableView<Developer> devStatusTable = new TableView<>();

    public MainWindow() {
        DeveloperRepository developerRepository = new DeveloperRepository();
        ProjectRepository projectRepository = new ProjectRepository();
        AbsenceRepository absenceRepository = new AbsenceRepository();
        projectService = new ProjectService(projectRepository, developerRepository);
        activityService = new ActivityService(projectRepository, developerRepository, absenceRepository);
        developerService = new DeveloperService(developerRepository);
        timeRegistrationService = new TimeRegistrationService(projectRepository, developerRepository);
        absenceService = new AbsenceService(absenceRepository, developerRepository);

        buildUI();
    }

    private void buildUI() {
        Label title = new Label("Software Project Planner");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        HBox topBar = new HBox(title);
        topBar.setPadding(new Insets(12, 16, 12, 16));
        topBar.setStyle("-fx-background-color: #2c3e50;");
        title.setStyle("-fx-text-fill: white;");
        root.setTop(topBar);

        root.setLeft(buildProjectPanel());
        refreshDeveloperList();

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().addAll(
                buildOverviewTab(),
                buildMyProjectsTab(),
                buildActivitiesTab(),
                buildTimeRegistrationTab(),
                buildReportTab(),
                buildAvailableDevelopersTab(),
                buildAbsenceTab()
        );
        root.setCenter(tabPane);
    }

    // ── Left panel ───────────────────────────────────────────────────────────────

    private VBox buildProjectPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(200);
        panel.setStyle("-fx-background-color: #ecf0f1;");

        Label header = new Label("Projects");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        projectListView.setItems(projectItems);
        projectListView.setPrefHeight(400);

        TextField nameField = new TextField();
        nameField.setPromptText("Project name");
        TextField startWeekField = new TextField();
        startWeekField.setPromptText("Start week");
        TextField startYearField = new TextField(String.valueOf(LocalDate.now().getYear()));
        TextField deadlineWeekField = new TextField();
        deadlineWeekField.setPromptText("Deadline week");
        TextField deadlineYearField = new TextField(String.valueOf(LocalDate.now().getYear()));

        GridPane createForm = new GridPane();
        createForm.setHgap(4);
        createForm.setVgap(4);
        createForm.setMaxWidth(Double.MAX_VALUE);
        createForm.addRow(0, new Label("Name:"), nameField);
        createForm.addRow(1, new Label("Start w/y:"), startWeekField, startYearField);
        createForm.addRow(2, new Label("Deadline:"), deadlineWeekField, deadlineYearField);
        ColumnConstraints col0 = new ColumnConstraints(); col0.setMinWidth(50);
        ColumnConstraints col1 = new ColumnConstraints(); col1.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        createForm.getColumnConstraints().addAll(col0, col1, col2);

        Button createBtn = new Button("+ Create Project");
        createBtn.setMaxWidth(Double.MAX_VALUE);
        createBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        createBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { showError("Project name cannot be empty."); return; }
            try {
                Project p = projectService.createProject(name);
                if (!startWeekField.getText().isBlank()) {
                    projectService.setStart(p.getId(),
                            Integer.parseInt(startWeekField.getText().trim()),
                            Integer.parseInt(startYearField.getText().trim()));
                }
                if (!deadlineWeekField.getText().isBlank()) {
                    projectService.setDeadline(p.getId(),
                            Integer.parseInt(deadlineWeekField.getText().trim()),
                            Integer.parseInt(deadlineYearField.getText().trim()));
                }
                projectItems.add("[" + p.getId() + "] " + p.getName());
                nameField.clear(); startWeekField.clear(); deadlineWeekField.clear();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        projectListView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            String id = getSelectedProjectId();
            if (id != null) refreshOverview(id);
        });

        panel.getChildren().addAll(header, new Separator(), createForm, createBtn,
                new Separator(), new Label("Select project:"), projectListView);
        return panel;
    }

    // ── Overview tab ─────────────────────────────────────────────────────────────

    private Tab buildOverviewTab() {
        Tab tab = new Tab("Overview");
        VBox content = new VBox(10);
        content.setPadding(new Insets(12));

        overviewProgressBar.setPrefWidth(400);
        overviewInfoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        TableColumn<Activity, String> nameCol = new TableColumn<>("Activity");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        TableColumn<Activity, Number> budgetCol = new TableColumn<>("Budget (h)");
        budgetCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getBudgetedHours()));
        TableColumn<Activity, Number> regCol = new TableColumn<>("Registered (h)");
        regCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotalRegisteredHours()));
        TableColumn<Activity, String> pctCol = new TableColumn<>("Progress");
        pctCol.setCellValueFactory(d -> {
            Activity a = d.getValue();
            if (a.getBudgetedHours() == 0) return new SimpleStringProperty("—");
            double pct = (a.getTotalRegisteredHours() / a.getBudgetedHours()) * 100;
            return new SimpleStringProperty(String.format("%.0f%%", pct));
        });
        TableColumn<Activity, String> devsCol = new TableColumn<>("Developers");
        devsCol.setCellValueFactory(d -> {
            List<Developer> devs = d.getValue().getAssignedDevelopers();
            String s = devs.stream().map(Developer::getInitials)
                    .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
            return new SimpleStringProperty(s.isEmpty() ? "—" : s);
        });
        TableColumn<Activity, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> {
            Activity a = d.getValue();
            double rem = a.getBudgetedHours() - a.getTotalRegisteredHours();
            if (rem > 0) return new SimpleStringProperty(String.format("%.1f h left", rem));
            if (rem == 0) return new SimpleStringProperty("Complete");
            return new SimpleStringProperty(String.format("%.1f h over budget", -rem));
        });
        overviewTable.getColumns().addAll(nameCol, budgetCol, regCol, pctCol, devsCol, statusCol);
        overviewTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        overviewTable.setPrefHeight(200);

        // Developer status table
        TableColumn<Developer, String> devCol = new TableColumn<>("Developer");
        devCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getInitials()));
        TableColumn<Developer, String> devActivitiesCol = new TableColumn<>("Assigned Activities");
        devActivitiesCol.setCellValueFactory(d -> new SimpleStringProperty("—")); // filled in refresh
        TableColumn<Developer, String> devStatusCol = new TableColumn<>("Status");
        devStatusCol.setCellValueFactory(d -> new SimpleStringProperty("—")); // filled in refresh
        devStatusTable.getColumns().addAll(devCol, devActivitiesCol, devStatusCol);
        devStatusTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        devStatusTable.setPrefHeight(150);

        HBox barRow = new HBox(10, overviewProgressBar, overviewProgressLabel);
        barRow.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(
                overviewInfoLabel,
                overviewLeaderLabel,
                overviewDeadlineLabel,
                new Separator(),
                new Label("Overall progress:"),
                barRow,
                overviewRemainingLabel,
                new Separator(),
                new Label("Activities:"),
                overviewTable,
                new Separator(),
                new Label("Project developers:"),
                devStatusTable
        );
        tab.setContent(new ScrollPane(content));
        return tab;
    }

    private void refreshOverview(String projectId) {
        Project project = projectService.getProject(projectId);
        double pct = projectService.getProjectProgress(projectId);

        overviewInfoLabel.setText("Project: [" + project.getId() + "]  " + project.getName());
        overviewLeaderLabel.setText("Leader: " + (project.getProjectLeader() != null
                ? project.getProjectLeader().getInitials() : "not assigned"));
        overviewDeadlineLabel.setText(
                (project.hasStart() ? "Start: Week " + project.getStartWeek() + "/" + project.getStartYear() + "   " : "") +
                (project.hasDeadline() ? "Deadline: Week " + project.getDeadlineWeek() + "/" + project.getDeadlineYear() : "Deadline: not set"));
        overviewProgressBar.setProgress(pct / 100.0);
        overviewProgressLabel.setText(String.format("%.0f%%  (%.1f / %.1f h)",
                pct, project.getTotalRegisteredHours(), project.getTotalBudgetedHours()));
        overviewRemainingLabel.setText(String.format("Remaining: %.1f hours",
                project.getTotalBudgetedHours() - project.getTotalRegisteredHours()));
        overviewTable.setItems(FXCollections.observableArrayList(project.getActivities()));
        overviewTable.refresh();
        refreshActivityNames(projectId);

        // Developer status — collect unique developers across all activities
        java.util.LinkedHashMap<Developer, java.util.List<String>> devActivityMap = new java.util.LinkedHashMap<>();
        for (Activity a : project.getActivities()) {
            for (Developer d : a.getAssignedDevelopers()) {
                devActivityMap.computeIfAbsent(d, k -> new java.util.ArrayList<>()).add(a.getName());
            }
        }

        // Current ISO week for absence check
        int currentWeek = LocalDate.now().get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
        int currentYear = LocalDate.now().getYear();

        // Rebuild developer status table columns with live data
        devStatusTable.getColumns().clear();
        TableColumn<Developer, String> devCol = new TableColumn<>("Developer");
        devCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getInitials()));

        TableColumn<Developer, String> actCol = new TableColumn<>("Assigned Activities");
        actCol.setCellValueFactory(d -> {
            java.util.List<String> acts = devActivityMap.getOrDefault(d.getValue(), java.util.List.of());
            return new SimpleStringProperty(acts.isEmpty() ? "—" : String.join(", ", acts));
        });

        TableColumn<Developer, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> {
            Developer dev = d.getValue();
            boolean absent = absenceService.getAbsencesForDeveloper(dev.getInitials()).stream()
                    .anyMatch(a -> a.isActiveInWeek(currentWeek, currentYear));
            if (absent) {
                java.util.List<com.planner.domain.Absence> absences = absenceService.getAbsencesForDeveloper(dev.getInitials());
                String reason = absences.stream()
                        .filter(a -> a.isActiveInWeek(currentWeek, currentYear))
                        .map(a -> a.getType().toString())
                        .findFirst().orElse("Absent");
                return new SimpleStringProperty("Absent (" + reason + ")");
            }
            return new SimpleStringProperty("Active");
        });
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(item.startsWith("Absent")
                            ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;"
                            : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            }
        });

        devStatusTable.getColumns().addAll(devCol, actCol, statusCol);
        devStatusTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        devStatusTable.setItems(FXCollections.observableArrayList(devActivityMap.keySet()));
        devStatusTable.refresh();
    }

    // ── Activities tab ────────────────────────────────────────────────────────────

    private Tab buildActivitiesTab() {
        Tab tab = new Tab("Activities");
        VBox content = new VBox(10);
        content.setPadding(new Insets(12));

        TableView<Activity> table = new TableView<>();
        TableColumn<Activity, String> nameCol = new TableColumn<>("Activity");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        TableColumn<Activity, Number> budgetCol = new TableColumn<>("Budget (h)");
        budgetCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getBudgetedHours()));
        TableColumn<Activity, String> weeksCol = new TableColumn<>("Weeks");
        weeksCol.setCellValueFactory(d -> {
            Activity a = d.getValue();
            String w = a.getStartWeek() == 0 ? "—" :
                    "W" + a.getStartWeek() + "/" + a.getStartYear() + " – W" + a.getEndWeek() + "/" + a.getEndYear();
            return new SimpleStringProperty(w);
        });
        TableColumn<Activity, String> devsCol = new TableColumn<>("Developers");
        devsCol.setCellValueFactory(d -> {
            List<Developer> devs = d.getValue().getAssignedDevelopers();
            String s = devs.stream().map(Developer::getInitials)
                    .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
            return new SimpleStringProperty(s.isEmpty() ? "—" : s);
        });
        table.getColumns().addAll(nameCol, budgetCol, weeksCol, devsCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Create activity form
        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(6);
        TextField activityName = new TextField();
        activityName.setPromptText("Activity name");
        TextField budget = new TextField();
        budget.setPromptText("Budget hours");
        TextField startWeek = new TextField();
        startWeek.setPromptText("Start week");
        TextField startYear = new TextField();
        startYear.setPromptText("Start year");
        TextField endWeek = new TextField();
        endWeek.setPromptText("End week");
        TextField endYear = new TextField();
        endYear.setPromptText("End year");

        form.addRow(0, new Label("Activity name:"), activityName);
        form.addRow(1, new Label("Budget (hours):"), budget);
        form.addRow(2, new Label("Start week / year:"), startWeek, startYear);
        form.addRow(3, new Label("End week / year:"), endWeek, endYear);

        Button addActivity = new Button("Add Activity");
        addActivity.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        addActivity.setOnAction(e -> {
            String projectId = getSelectedProjectId();
            if (projectId == null) { showError("Select a project first."); return; }
            String name = activityName.getText().trim();
            if (name.isEmpty()) { showError("Activity name cannot be empty."); return; }
            try {
                activityService.createActivity(projectId, name);
                if (!budget.getText().isBlank()) {
                    int sw = startWeek.getText().isBlank() ? 0 : Integer.parseInt(startWeek.getText());
                    int sy = startYear.getText().isBlank() ? 0 : Integer.parseInt(startYear.getText());
                    int ew = endWeek.getText().isBlank() ? 0 : Integer.parseInt(endWeek.getText());
                    int ey = endYear.getText().isBlank() ? 0 : Integer.parseInt(endYear.getText());
                    activityService.setActivityDetails(projectId, name, Double.parseDouble(budget.getText()), sw, sy, ew, ey);
                }
                refreshActivityTable(table, projectId);
                refreshOverview(projectId);
                activityName.clear(); budget.clear();
                startWeek.clear(); startYear.clear(); endWeek.clear(); endYear.clear();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        // Add developer section — dropdown for activity, dropdown for developer
        Label devLabel = new Label("Add developer to activity:");
        devLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        ComboBox<String> devActivityCombo = new ComboBox<>(activityNames);
        devActivityCombo.setPromptText("Select activity...");
        devActivityCombo.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> devInitialsCombo = new ComboBox<>(developerInitialsList);
        devInitialsCombo.setPromptText("Select developer...");
        devInitialsCombo.setMaxWidth(Double.MAX_VALUE);

        GridPane devForm = new GridPane();
        devForm.setHgap(8);
        devForm.setVgap(6);
        devForm.addRow(0, new Label("Activity:"), devActivityCombo);
        devForm.addRow(1, new Label("Developer:"), devInitialsCombo);

        Button addDev = new Button("Add Developer to Activity");
        addDev.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");
        addDev.setOnAction(e -> {
            String projectId = getSelectedProjectId();
            if (projectId == null) { showError("Select a project first."); return; }
            String actName = devActivityCombo.getValue();
            String initials = devInitialsCombo.getValue();
            if (actName == null || actName.isEmpty()) { showError("Select an activity."); return; }
            if (initials == null || initials.isEmpty()) { showError("Select a developer."); return; }
            try {
                activityService.addDeveloperToActivity(projectId, actName, initials);
                refreshActivityTable(table, projectId);
                refreshOverview(projectId);
                showInfo("Developer \"" + initials + "\" added to \"" + actName + "\".");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        // Assign project leader
        TextField leaderField = new TextField();
        leaderField.setPromptText("Developer initials");
        Button assignLeader = new Button("Assign Project Leader");
        assignLeader.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
        assignLeader.setOnAction(e -> {
            String projectId = getSelectedProjectId();
            if (projectId == null) { showError("Select a project first."); return; }
            try {
                String initials = leaderField.getText().trim();
                projectService.assignProjectLeader(projectId, initials);
                refreshOverview(projectId);
                leaderField.clear();
                showInfo("Project leader set to: " + initials);
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
        HBox leaderRow = new HBox(8, new Label("Project leader:"), leaderField, assignLeader);
        leaderRow.setAlignment(Pos.CENTER_LEFT);

        // Set deadline
        TextField deadlineWeekField = new TextField();
        deadlineWeekField.setPromptText("Week (e.g. 20)");
        TextField deadlineYearField = new TextField(String.valueOf(LocalDate.now().getYear()));
        Button setDeadlineBtn = new Button("Set Project Deadline");
        setDeadlineBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        setDeadlineBtn.setOnAction(e -> {
            String projectId = getSelectedProjectId();
            if (projectId == null) { showError("Select a project first."); return; }
            try {
                int week = Integer.parseInt(deadlineWeekField.getText().trim());
                int year = Integer.parseInt(deadlineYearField.getText().trim());
                projectService.setDeadline(projectId, week, year);
                refreshOverview(projectId);
                deadlineWeekField.clear();
                showInfo("Deadline set to week " + week + "/" + year);
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
        HBox deadlineRow = new HBox(8, new Label("Deadline — Week:"), deadlineWeekField,
                new Label("Year:"), deadlineYearField, setDeadlineBtn);
        deadlineRow.setAlignment(Pos.CENTER_LEFT);

        projectListView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            String id = getSelectedProjectId();
            if (id != null) refreshActivityTable(table, id);
        });

        content.getChildren().addAll(
                new Label("Activities for selected project:"),
                table, new Separator(),
                form, addActivity, new Separator(),
                devLabel, devForm, addDev, new Separator(),
                leaderRow, new Separator(),
                deadlineRow
        );
        tab.setContent(new ScrollPane(content));
        return tab;
    }

    // ── Time registration tab ─────────────────────────────────────────────────────

    private Tab buildTimeRegistrationTab() {
        Tab tab = new Tab("Register Time");
        VBox content = new VBox(10);
        content.setPadding(new Insets(12));

        ComboBox<String> initialsCombo = new ComboBox<>(developerInitialsList);
        initialsCombo.setPromptText("Select developer...");
        initialsCombo.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> activityCombo = new ComboBox<>(activityNames);
        activityCombo.setPromptText("Select activity...");
        activityCombo.setMaxWidth(Double.MAX_VALUE);
        TextField hoursField = new TextField();
        hoursField.setPromptText("Hours (e.g. 2.5)");
        DatePicker datePicker = new DatePicker(LocalDate.now());

        Button registerBtn = new Button("Register Time");
        registerBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        registerBtn.setOnAction(e -> {
            String projectId = getSelectedProjectId();
            if (projectId == null) { showError("Select a project first."); return; }
            String initials = initialsCombo.getValue();
            String actName = activityCombo.getValue();
            if (initials == null || initials.isEmpty()) { showError("Select a developer."); return; }
            if (actName == null || actName.isEmpty()) { showError("Select an activity."); return; }
            try {
                double hours = Double.parseDouble(hoursField.getText().trim());
                timeRegistrationService.registerTime(initials, projectId, actName, datePicker.getValue(), hours);
                refreshOverview(projectId);
                showInfo("Registered " + hours + " hours on \"" + actName + "\".");
                hoursField.clear();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.addRow(0, new Label("Developer:"), initialsCombo);
        form.addRow(1, new Label("Activity:"), activityCombo);
        form.addRow(2, new Label("Hours:"), hoursField);
        form.addRow(3, new Label("Date:"), datePicker);

        content.getChildren().addAll(new Label("Register time on an activity:"), form, registerBtn);
        tab.setContent(content);
        return tab;
    }

    // ── Report tab ────────────────────────────────────────────────────────────────

    private Tab buildReportTab() {
        Tab tab = new Tab("Report");
        VBox content = new VBox(10);
        content.setPadding(new Insets(12));

        TableView<Activity> table = new TableView<>();
        TableColumn<Activity, String> nameCol = new TableColumn<>("Activity");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        TableColumn<Activity, Number> budgetCol = new TableColumn<>("Budgeted (h)");
        budgetCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getBudgetedHours()));
        TableColumn<Activity, Number> registeredCol = new TableColumn<>("Registered (h)");
        registeredCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotalRegisteredHours()));
        TableColumn<Activity, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> {
            Activity a = d.getValue();
            double diff = a.getBudgetedHours() - a.getTotalRegisteredHours();
            String s = diff >= 0 ? String.format("%.1f h remaining", diff) : String.format("%.1f h over budget", -diff);
            return new SimpleStringProperty(s);
        });
        table.getColumns().addAll(nameCol, budgetCol, registeredCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label totalLabel = new Label();

        Button generateBtn = new Button("Generate Report");
        generateBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        generateBtn.setOnAction(e -> {
            String projectId = getSelectedProjectId();
            if (projectId == null) { showError("Select a project first."); return; }
            Project project = projectService.getProject(projectId);
            table.setItems(FXCollections.observableArrayList(project.getActivities()));
            totalLabel.setText(String.format("Total:  Budgeted: %.1f h   Registered: %.1f h",
                    project.getTotalBudgetedHours(), project.getTotalRegisteredHours()));
        });

        content.getChildren().addAll(
                new Label("Project report (select project then click generate):"),
                generateBtn, table, totalLabel);
        tab.setContent(content);
        return tab;
    }

    // ── Available developers tab ──────────────────────────────────────────────────

    private Tab buildAvailableDevelopersTab() {
        Tab tab = new Tab("Available Developers");
        VBox content = new VBox(10);
        content.setPadding(new Insets(12));

        TextField weekField = new TextField();
        weekField.setPromptText("Week (e.g. 11)");
        TextField yearField = new TextField(String.valueOf(LocalDate.now().getYear()));
        ListView<String> resultList = new ListView<>();

        Button checkBtn = new Button("Check Availability");
        checkBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        checkBtn.setOnAction(e -> {
            try {
                int week = Integer.parseInt(weekField.getText().trim());
                int year = Integer.parseInt(yearField.getText().trim());
                List<Developer> available = activityService.getAvailableDevelopers(week, year);
                ObservableList<String> items = FXCollections.observableArrayList();
                if (available.isEmpty()) {
                    items.add("No available developers in week " + week + "/" + year);
                } else {
                    available.forEach(d -> items.add(d.getInitials()));
                }
                resultList.setItems(items);
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        HBox row = new HBox(8, new Label("Week:"), weekField, new Label("Year:"), yearField, checkBtn);
        row.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(new Label("Find available developers for a given week:"), row, resultList);
        tab.setContent(content);
        return tab;
    }

    // ── Absence tab ───────────────────────────────────────────────────────────────

    private Tab buildAbsenceTab() {
        Tab tab = new Tab("Absences");
        VBox content = new VBox(10);
        content.setPadding(new Insets(12));

        TextField initialsField = new TextField();
        initialsField.setPromptText("Developer initials");
        ComboBox<String> typeBox = new ComboBox<>(
                FXCollections.observableArrayList("VACATION", "SICK_LEAVE", "COURSE", "OTHER"));
        typeBox.setValue("VACATION");
        TextField startWeek = new TextField();
        startWeek.setPromptText("Start week");
        TextField startYear = new TextField(String.valueOf(LocalDate.now().getYear()));
        TextField endWeek = new TextField();
        endWeek.setPromptText("End week");
        TextField endYear = new TextField(String.valueOf(LocalDate.now().getYear()));

        Button registerBtn = new Button("Register Absence");
        registerBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        registerBtn.setOnAction(e -> {
            try {
                String initials = initialsField.getText().trim();
                absenceService.registerAbsence(
                        initials,
                        Absence.Type.valueOf(typeBox.getValue()),
                        Integer.parseInt(startWeek.getText().trim()),
                        Integer.parseInt(startYear.getText().trim()),
                        Integer.parseInt(endWeek.getText().trim()),
                        Integer.parseInt(endYear.getText().trim()));
                showInfo("Absence registered for " + initials + ".");
                initialsField.clear(); startWeek.clear(); endWeek.clear();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.addRow(0, new Label("Initials:"), initialsField);
        form.addRow(1, new Label("Type:"), typeBox);
        form.addRow(2, new Label("Start week / year:"), startWeek, startYear);
        form.addRow(3, new Label("End week / year:"), endWeek, endYear);

        content.getChildren().addAll(
                new Label("Register absence (vacation, sick leave, course):"),
                form, registerBtn);
        tab.setContent(content);
        return tab;
    }

    // ── My Projects tab (project leader view) ────────────────────────────────────

    private Tab buildMyProjectsTab() {
        Tab tab = new Tab("My Projects");
        SplitPane split = new SplitPane();
        split.setDividerPositions(0.35);

        // ── Left: select leader identity + project list ──
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(12));

        Label whoLabel = new Label("Log in as:");
        whoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        ComboBox<String> leaderCombo = new ComboBox<>(developerInitialsList);
        leaderCombo.setPromptText("Select your initials...");
        leaderCombo.setMaxWidth(Double.MAX_VALUE);

        Label myProjectsLabel = new Label("Your projects as leader:");
        ListView<String> myProjectsList = new ListView<>();
        Label noProjectsHint = new Label("No projects where you are leader.");
        noProjectsHint.setStyle("-fx-text-fill: #7f8c8d;");

        leaderCombo.setOnAction(e -> {
            String initials = leaderCombo.getValue();
            if (initials == null) return;
            ObservableList<String> items = FXCollections.observableArrayList();
            for (Project p : projectService.getAllProjects()) {
                if (p.getProjectLeader() != null
                        && p.getProjectLeader().getInitials().equalsIgnoreCase(initials)) {
                    items.add("[" + p.getId() + "] " + p.getName());
                }
            }
            myProjectsList.setItems(items);
        });

        leftPanel.getChildren().addAll(whoLabel, leaderCombo,
                new Separator(), myProjectsLabel, myProjectsList, noProjectsHint);

        // ── Right: activity management for selected project ──
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(12));

        Label rightTitle = new Label("Select a project from the list to manage it.");
        rightTitle.setStyle("-fx-text-fill: #7f8c8d;");
        rightTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        // Activity table
        TableView<Activity> actTable = new TableView<>();
        TableColumn<Activity, String> actNameCol = new TableColumn<>("Activity");
        actNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        TableColumn<Activity, Number> actBudgetCol = new TableColumn<>("Budget (h)");
        actBudgetCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getBudgetedHours()));
        TableColumn<Activity, String> actWeeksCol = new TableColumn<>("Weeks");
        actWeeksCol.setCellValueFactory(d -> {
            Activity a = d.getValue();
            if (a.getStartWeek() == 0) return new SimpleStringProperty("—");
            return new SimpleStringProperty("W" + a.getStartWeek() + "/" + a.getStartYear()
                    + " – W" + a.getEndWeek() + "/" + a.getEndYear());
        });
        TableColumn<Activity, String> actDevsCol = new TableColumn<>("Developers");
        actDevsCol.setCellValueFactory(d -> {
            String s = d.getValue().getAssignedDevelopers().stream()
                    .map(Developer::getInitials)
                    .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
            return new SimpleStringProperty(s.isEmpty() ? "—" : s);
        });
        actTable.getColumns().addAll(actNameCol, actBudgetCol, actWeeksCol, actDevsCol);
        actTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        actTable.setPrefHeight(200);

        // Create activity form
        Label createActLabel = new Label("Create new activity:");
        createActLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        GridPane actForm = new GridPane();
        actForm.setHgap(8); actForm.setVgap(6);
        TextField actNameField = new TextField(); actNameField.setPromptText("Activity name");
        TextField actBudget = new TextField(); actBudget.setPromptText("Budget hours");
        TextField actStartWeek = new TextField(); actStartWeek.setPromptText("Start week");
        TextField actStartYear = new TextField(); actStartYear.setPromptText("Start year");
        TextField actEndWeek = new TextField(); actEndWeek.setPromptText("End week");
        TextField actEndYear = new TextField(); actEndYear.setPromptText("End year");
        actForm.addRow(0, new Label("Name:"), actNameField);
        actForm.addRow(1, new Label("Budget (h):"), actBudget);
        actForm.addRow(2, new Label("Start week/year:"), actStartWeek, actStartYear);
        actForm.addRow(3, new Label("End week/year:"), actEndWeek, actEndYear);

        Button createActBtn = new Button("Create Activity");
        createActBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        // Assign developer form
        Label assignLabel = new Label("Assign developer to activity:");
        assignLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        ObservableList<String> myActNames = FXCollections.observableArrayList();
        ComboBox<String> assignActCombo = new ComboBox<>(myActNames);
        assignActCombo.setPromptText("Select activity...");
        assignActCombo.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> assignDevCombo = new ComboBox<>(developerInitialsList);
        assignDevCombo.setPromptText("Select developer...");
        assignDevCombo.setMaxWidth(Double.MAX_VALUE);
        Button assignBtn = new Button("Assign Developer");
        assignBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");

        // When a project is selected in the left list
        myProjectsList.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val == null) return;
            String projectId = val.substring(1, val.indexOf("]"));
            Project p = projectService.getProject(projectId);
            rightTitle.setText("Managing: [" + p.getId() + "] " + p.getName());
            actTable.setItems(FXCollections.observableArrayList(p.getActivities()));
            actTable.refresh();
            myActNames.setAll(p.getActivities().stream()
                    .map(Activity::getName)
                    .collect(java.util.stream.Collectors.toList()));
        });

        createActBtn.setOnAction(e -> {
            String val = myProjectsList.getSelectionModel().getSelectedItem();
            if (val == null) { showError("Select a project first."); return; }
            String projectId = val.substring(1, val.indexOf("]"));
            String name = actNameField.getText().trim();
            if (name.isEmpty()) { showError("Activity name cannot be empty."); return; }
            try {
                activityService.createActivity(projectId, name);
                if (!actBudget.getText().isBlank()) {
                    int sw = actStartWeek.getText().isBlank() ? 0 : Integer.parseInt(actStartWeek.getText());
                    int sy = actStartYear.getText().isBlank() ? 0 : Integer.parseInt(actStartYear.getText());
                    int ew = actEndWeek.getText().isBlank() ? 0 : Integer.parseInt(actEndWeek.getText());
                    int ey = actEndYear.getText().isBlank() ? 0 : Integer.parseInt(actEndYear.getText());
                    activityService.setActivityDetails(projectId, name, Double.parseDouble(actBudget.getText()), sw, sy, ew, ey);
                }
                Project p = projectService.getProject(projectId);
                actTable.setItems(FXCollections.observableArrayList(p.getActivities()));
                actTable.refresh();
                myActNames.setAll(p.getActivities().stream().map(Activity::getName).collect(java.util.stream.Collectors.toList()));
                refreshActivityNames(projectId);
                refreshOverview(projectId);
                actNameField.clear(); actBudget.clear();
                actStartWeek.clear(); actStartYear.clear(); actEndWeek.clear(); actEndYear.clear();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        assignBtn.setOnAction(e -> {
            String val = myProjectsList.getSelectionModel().getSelectedItem();
            if (val == null) { showError("Select a project first."); return; }
            String projectId = val.substring(1, val.indexOf("]"));
            String actName = assignActCombo.getValue();
            String devInitials = assignDevCombo.getValue();
            if (actName == null) { showError("Select an activity."); return; }
            if (devInitials == null) { showError("Select a developer."); return; }
            try {
                activityService.addDeveloperToActivity(projectId, actName, devInitials);
                Project p = projectService.getProject(projectId);
                actTable.setItems(FXCollections.observableArrayList(p.getActivities()));
                actTable.refresh();
                refreshOverview(projectId);
                showInfo("Developer \"" + devInitials + "\" assigned to \"" + actName + "\".");
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        GridPane assignForm = new GridPane();
        assignForm.setHgap(8); assignForm.setVgap(6);
        assignForm.addRow(0, new Label("Activity:"), assignActCombo);
        assignForm.addRow(1, new Label("Developer:"), assignDevCombo);

        // Project team overview
        Label teamLabel = new Label("Project team (all assigned developers):");
        teamLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        ListView<String> teamList = new ListView<>();
        teamList.setPrefHeight(120);

        // Helper to refresh team list
        Runnable refreshTeam = () -> {
            String sel = myProjectsList.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            String pid = sel.substring(1, sel.indexOf("]"));
            Project p = projectService.getProject(pid);
            java.util.Set<String> seen = new java.util.LinkedHashSet<>();
            for (Activity a : p.getActivities()) {
                for (Developer d : a.getAssignedDevelopers()) {
                    seen.add(d.getInitials());
                }
            }
            ObservableList<String> items = FXCollections.observableArrayList();
            if (seen.isEmpty()) {
                items.add("No developers assigned yet.");
            } else {
                seen.forEach(items::add);
            }
            teamList.setItems(items);
        };

        // Update team list when project selected
        myProjectsList.getSelectionModel().selectedItemProperty().addListener((obs2, old2, val2) -> refreshTeam.run());

        // Also refresh team after assigning
        assignBtn.setOnAction(null);
        assignBtn.setOnAction(e -> {
            String val = myProjectsList.getSelectionModel().getSelectedItem();
            if (val == null) { showError("Select a project first."); return; }
            String projectId = val.substring(1, val.indexOf("]"));
            String actName = assignActCombo.getValue();
            String devInitials = assignDevCombo.getValue();
            if (actName == null) { showError("Select an activity."); return; }
            if (devInitials == null) { showError("Select a developer."); return; }
            try {
                activityService.addDeveloperToActivity(projectId, actName, devInitials);
                Project p = projectService.getProject(projectId);
                actTable.setItems(FXCollections.observableArrayList(p.getActivities()));
                actTable.refresh();
                refreshOverview(projectId);
                refreshTeam.run();
                showInfo("Developer \"" + devInitials + "\" assigned to \"" + actName + "\".");
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        rightPanel.getChildren().addAll(
                rightTitle, new Separator(),
                new Label("Activities:"), actTable, new Separator(),
                createActLabel, actForm, createActBtn, new Separator(),
                assignLabel, assignForm, assignBtn, new Separator(),
                teamLabel, teamList
        );

        ScrollPane rightScroll = new ScrollPane(rightPanel);
        rightScroll.setFitToWidth(true);
        split.getItems().addAll(leftPanel, rightScroll);
        tab.setContent(split);
        return tab;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    private void refreshActivityTable(TableView<Activity> table, String projectId) {
        Project p = projectService.getProject(projectId);
        table.setItems(FXCollections.observableArrayList(p.getActivities()));
        table.refresh();
        refreshActivityNames(projectId);
    }

    private void refreshActivityNames(String projectId) {
        Project p = projectService.getProject(projectId);
        activityNames.setAll(p.getActivities().stream()
                .map(Activity::getName)
                .collect(java.util.stream.Collectors.toList()));
    }

    private void refreshDeveloperList() {
        developerInitialsList.setAll(developerService.getAllDevelopers().stream()
                .map(Developer::getInitials)
                .collect(java.util.stream.Collectors.toList()));
    }

    private String getSelectedProjectId() {
        String selected = projectListView.getSelectionModel().getSelectedItem();
        if (selected == null) return null;
        return selected.substring(1, selected.indexOf("]"));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public BorderPane getRoot() {
        return root;
    }
}
