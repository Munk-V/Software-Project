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
        // Top bar
        Label title = new Label("Software Project Planner");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        HBox topBar = new HBox(title);
        topBar.setPadding(new Insets(12, 16, 12, 16));
        topBar.setStyle("-fx-background-color: #2c3e50;");
        title.setStyle("-fx-text-fill: white;");
        root.setTop(topBar);

        // Left panel - project list
        root.setLeft(buildProjectPanel());

        // Center - tabs
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().addAll(
                buildActivitiesTab(),
                buildTimeRegistrationTab(),
                buildReportTab(),
                buildProgressTab(),
                buildAvailableDevelopersTab(),
                buildAbsenceTab()
        );
        root.setCenter(tabPane);
    }

    // ── Left panel ──────────────────────────────────────────────────────────────

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
        nameField.setPromptText("Project name...");

        Button createBtn = new Button("+ Create Project");
        createBtn.setMaxWidth(Double.MAX_VALUE);
        createBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        createBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showError("Project name cannot be empty.");
                return;
            }
            try {
                Project p = projectService.createProject(name);
                projectItems.add("[" + p.getId() + "] " + p.getName());
                nameField.clear();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });

        panel.getChildren().addAll(header, new Separator(), nameField, createBtn,
                new Separator(), new Label("Select project:"), projectListView);
        return panel;
    }

    // ── Activities tab ───────────────────────────────────────────────────────────

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
            String s = devs.stream().map(Developer::getInitials).reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
            return new SimpleStringProperty(s.isEmpty() ? "—" : s);
        });
        table.getColumns().addAll(nameCol, budgetCol, weeksCol, devsCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Form
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
        TextField devInitials = new TextField();
        devInitials.setPromptText("Developer initials");

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
                activityName.clear(); budget.clear();
                startWeek.clear(); startYear.clear(); endWeek.clear(); endYear.clear();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        HBox devRow = new HBox(8, new Label("Add developer:"), devInitials);
        devRow.setAlignment(Pos.CENTER_LEFT);
        Button addDev = new Button("Add Developer to Selected Activity");
        addDev.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");
        addDev.setOnAction(e -> {
            String projectId = getSelectedProjectId();
            Activity selected = table.getSelectionModel().getSelectedItem();
            if (projectId == null || selected == null) { showError("Select a project and activity first."); return; }
            try {
                activityService.addDeveloperToActivity(projectId, selected.getName(), devInitials.getText().trim());
                refreshActivityTable(table, projectId);
                devInitials.clear();
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
                projectService.assignProjectLeader(projectId, leaderField.getText().trim());
                showInfo("Project leader set to: " + leaderField.getText().trim());
                leaderField.clear();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
        HBox leaderRow = new HBox(8, new Label("Project leader:"), leaderField, assignLeader);
        leaderRow.setAlignment(Pos.CENTER_LEFT);

        projectListView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            String id = getSelectedProjectId();
            if (id != null) refreshActivityTable(table, id);
        });

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
                showInfo("Deadline set to week " + week + "/" + year);
                deadlineWeekField.clear();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
        HBox deadlineRow = new HBox(8, new Label("Deadline — Week:"), deadlineWeekField,
                new Label("Year:"), deadlineYearField, setDeadlineBtn);
        deadlineRow.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(
                new Label("Activities for selected project:"),
                table, new Separator(),
                form, addActivity, new Separator(),
                devRow, addDev, new Separator(),
                leaderRow, new Separator(),
                deadlineRow
        );
        tab.setContent(new ScrollPane(content));
        return tab;
    }

    // ── Time registration tab ────────────────────────────────────────────────────

    private Tab buildTimeRegistrationTab() {
        Tab tab = new Tab("Register Time");
        VBox content = new VBox(10);
        content.setPadding(new Insets(12));

        TextField initialsField = new TextField();
        initialsField.setPromptText("Your initials (e.g. huba)");
        TextField activityField = new TextField();
        activityField.setPromptText("Activity name");
        TextField hoursField = new TextField();
        hoursField.setPromptText("Hours (e.g. 2.5)");
        DatePicker datePicker = new DatePicker(LocalDate.now());

        Button registerBtn = new Button("Register Time");
        registerBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        registerBtn.setOnAction(e -> {
            String projectId = getSelectedProjectId();
            if (projectId == null) { showError("Select a project first."); return; }
            try {
                double hours = Double.parseDouble(hoursField.getText().trim());
                timeRegistrationService.registerTime(
                        initialsField.getText().trim(), projectId,
                        activityField.getText().trim(), datePicker.getValue(), hours);
                showInfo("Registered " + hours + " hours on " + activityField.getText().trim());
                hoursField.clear();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.addRow(0, new Label("Initials:"), initialsField);
        form.addRow(1, new Label("Activity:"), activityField);
        form.addRow(2, new Label("Hours:"), hoursField);
        form.addRow(3, new Label("Date:"), datePicker);

        content.getChildren().addAll(new Label("Register time on an activity:"), form, registerBtn);
        tab.setContent(content);
        return tab;
    }

    // ── Report tab ───────────────────────────────────────────────────────────────

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

        content.getChildren().addAll(new Label("Project report (select project then click generate):"),
                generateBtn, table, totalLabel);
        tab.setContent(content);
        return tab;
    }

    // ── Project progress tab ─────────────────────────────────────────────────────

    private Tab buildProgressTab() {
        Tab tab = new Tab("Project Progress");
        VBox content = new VBox(12);
        content.setPadding(new Insets(12));

        Label projectInfoLabel = new Label();
        Label deadlineLabel = new Label();
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        Label progressLabel = new Label("0%");
        Label remainingLabel = new Label();

        TableView<Activity> table = new TableView<>();
        TableColumn<Activity, String> nameCol = new TableColumn<>("Activity");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        TableColumn<Activity, Number> budgetCol = new TableColumn<>("Budgeted (h)");
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
        TableColumn<Activity, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> {
            Activity a = d.getValue();
            double remaining = a.getBudgetedHours() - a.getTotalRegisteredHours();
            if (remaining > 0) return new SimpleStringProperty(String.format("%.1f h left", remaining));
            if (remaining == 0) return new SimpleStringProperty("Complete");
            return new SimpleStringProperty(String.format("%.1f h over budget", -remaining));
        });
        table.getColumns().addAll(nameCol, budgetCol, regCol, pctCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button viewBtn = new Button("View Progress");
        viewBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        viewBtn.setOnAction(e -> {
            String projectId = getSelectedProjectId();
            if (projectId == null) { showError("Select a project first."); return; }

            Project project = projectService.getProject(projectId);
            double pct = projectService.getProjectProgress(projectId);

            projectInfoLabel.setText("Project: [" + project.getId() + "] " + project.getName()
                    + (project.getProjectLeader() != null
                    ? "  |  Leader: " + project.getProjectLeader().getInitials() : ""));

            deadlineLabel.setText(project.hasDeadline()
                    ? "Deadline: Week " + project.getDeadlineWeek() + "/" + project.getDeadlineYear()
                    : "Deadline: not set");

            progressBar.setProgress(pct / 100.0);
            progressLabel.setText(String.format("%.0f%%  (%.1f / %.1f hours)",
                    pct, project.getTotalRegisteredHours(), project.getTotalBudgetedHours()));
            remainingLabel.setText(String.format("Remaining: %.1f hours",
                    project.getTotalBudgetedHours() - project.getTotalRegisteredHours()));

            table.setItems(FXCollections.observableArrayList(project.getActivities()));
        });

        HBox barRow = new HBox(10, progressBar, progressLabel);
        barRow.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(
                viewBtn,
                new Separator(),
                projectInfoLabel,
                deadlineLabel,
                new Label("Overall progress:"),
                barRow,
                remainingLabel,
                new Separator(),
                new Label("Per activity:"),
                table
        );
        tab.setContent(new ScrollPane(content));
        return tab;
    }

    // ── Available developers tab ─────────────────────────────────────────────────

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

    // ── Fixed activity tab ───────────────────────────────────────────────────────

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
                absenceService.registerAbsence(
                        initialsField.getText().trim(),
                        Absence.Type.valueOf(typeBox.getValue()),
                        Integer.parseInt(startWeek.getText().trim()),
                        Integer.parseInt(startYear.getText().trim()),
                        Integer.parseInt(endWeek.getText().trim()),
                        Integer.parseInt(endYear.getText().trim()));
                showInfo("Absence registered for " + initialsField.getText().trim());
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

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private void refreshActivityTable(TableView<Activity> table, String projectId) {
        Project p = projectService.getProject(projectId);
        table.setItems(FXCollections.observableArrayList(p.getActivities()));
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
