package com.planner.ui;
// DELT – se per-metode kommentarer nedenfor (fordeling pr. tab)

import com.planner.domain.Activity;
import com.planner.domain.Developer;
import com.planner.domain.Absence;
import com.planner.domain.Project;
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
    private final AvailabilityService availabilityService;
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

    public MainWindow(AppContext context) {
        this.projectService = context.projectService;
        this.activityService = context.activityService;
        this.availabilityService = context.availabilityService;
        this.developerService = context.developerService;
        this.timeRegistrationService = context.timeRegistrationService;
        this.absenceService = context.absenceService;
        buildUI();
    }

    private void buildUI() {

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
                buildAvailableDevelopersTab()
        );
        root.setCenter(tabPane);
    }

    // ── Left panel ───────────────────────────────────────────────────────────────

    // Vedanta
    private VBox buildProjectPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(200);

        Label header = new Label("Projects");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        projectListView.setItems(projectItems);
        projectListView.setPrefHeight(400);

        TextField nameField = new TextField();
        nameField.setPromptText("Project name");
        nameField.setPrefWidth(140);
        TextField startWeekField = new TextField();
        startWeekField.setPromptText("Week");
        startWeekField.setPrefWidth(50);
        TextField startYearField = new TextField(String.valueOf(LocalDate.now().getYear()));
        startYearField.setPrefWidth(55);
        TextField deadlineWeekField = new TextField();
        deadlineWeekField.setPromptText("Week");
        deadlineWeekField.setPrefWidth(50);
        TextField deadlineYearField = new TextField(String.valueOf(LocalDate.now().getYear()));
        deadlineYearField.setPrefWidth(55);

        VBox createForm = new VBox(5);
        HBox nameRow = new HBox(6, new Label("Name:"), nameField);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        HBox startRow = new HBox(4, new Label("Start:"), startWeekField, new Label("w /"), startYearField);
        startRow.setAlignment(Pos.CENTER_LEFT);
        HBox deadlineRow = new HBox(4, new Label("Deadline:"), deadlineWeekField, new Label("w /"), deadlineYearField);
        deadlineRow.setAlignment(Pos.CENTER_LEFT);
        createForm.getChildren().addAll(nameRow, startRow, deadlineRow);

        Button createBtn = new Button("+ Create Project");
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

    // Mathias
    private Tab buildOverviewTab() {
        Tab tab = new Tab("Overview");
        VBox content = new VBox(10);
        content.setPadding(new Insets(12));

        overviewProgressBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(overviewProgressBar, Priority.ALWAYS);
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
        overviewTable.setMaxWidth(Double.MAX_VALUE);
        overviewTable.setPrefHeight(200);
        VBox.setVgrow(overviewTable, Priority.ALWAYS);

        // Developer status table
        TableColumn<Developer, String> devCol = new TableColumn<>("Developer");
        devCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getInitials()));
        TableColumn<Developer, String> devActivitiesCol = new TableColumn<>("Assigned Activities");
        devActivitiesCol.setCellValueFactory(d -> new SimpleStringProperty("—")); // filled in refresh
        TableColumn<Developer, String> devStatusCol = new TableColumn<>("Status");
        devStatusCol.setCellValueFactory(d -> new SimpleStringProperty("—")); // filled in refresh
        devStatusTable.getColumns().addAll(devCol, devActivitiesCol, devStatusCol);
        devStatusTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        devStatusTable.setMaxWidth(Double.MAX_VALUE);
        devStatusTable.setPrefHeight(150);
        VBox.setVgrow(devStatusTable, Priority.ALWAYS);

        HBox barRow = new HBox(10, overviewProgressBar, overviewProgressLabel);
        barRow.setAlignment(Pos.CENTER_LEFT);
        barRow.setMaxWidth(Double.MAX_VALUE);

        content.setFillWidth(true);
        content.setMaxWidth(Double.MAX_VALUE);
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
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        tab.setContent(sp);
        return tab;
    }

    // Mathias
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

    // Viktor
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
        table.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Create activity form
        TextField activityName = new TextField();
        activityName.setPromptText("Activity name");
        activityName.setPrefWidth(160);
        TextField budget = new TextField();
        budget.setPromptText("Budget hours");
        budget.setPrefWidth(80);
        TextField startWeek = new TextField();
        startWeek.setPromptText("Week");
        startWeek.setPrefWidth(50);
        TextField startYear = new TextField();
        startYear.setPromptText("Year");
        startYear.setPrefWidth(55);
        TextField endWeek = new TextField();
        endWeek.setPromptText("Week");
        endWeek.setPrefWidth(50);
        TextField endYear = new TextField();
        endYear.setPromptText("Year");
        endYear.setPrefWidth(55);

        VBox form = new VBox(5);
        form.getChildren().addAll(
            new HBox(6, new Label("Activity name:"), activityName),
            new HBox(6, new Label("Budget (h):   "), budget),
            new HBox(4, new Label("Start:"), startWeek, new Label("w /"), startYear),
            new HBox(4, new Label("End:  "), endWeek,   new Label("w /"), endYear)
        );

        Button addActivity = new Button("Add Activity");
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
        devActivityCombo.setPrefWidth(160);
        ComboBox<String> devInitialsCombo = new ComboBox<>(developerInitialsList);
        devInitialsCombo.setPromptText("Select developer...");
        devInitialsCombo.setPrefWidth(130);

        VBox devForm = new VBox(5);
        devForm.getChildren().addAll(
            new HBox(6, new Label("Activity:"), devActivityCombo),
            new HBox(6, new Label("Developer:"), devInitialsCombo)
        );

        Button addDev = new Button("Add Developer to Activity");
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
        leaderField.setPromptText("Initials");
        leaderField.setPrefWidth(90);
        Button assignLeader = new Button("Assign Leader");
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


        projectListView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            String id = getSelectedProjectId();
            if (id != null) refreshActivityTable(table, id);
        });

        content.setFillWidth(true);
        content.setMaxWidth(Double.MAX_VALUE);
        content.getChildren().addAll(
                new Label("Activities for selected project:"),
                table, new Separator(),
                form, addActivity, new Separator(),
                devLabel, devForm, addDev, new Separator(),
                leaderRow
        );
        ScrollPane actSp = new ScrollPane(content);
        actSp.setFitToWidth(true);
        tab.setContent(actSp);
        return tab;
    }

    // ── Time registration tab ─────────────────────────────────────────────────────

    // Nat (UI-helper)
    private Label formLabel(String text) {
        Label l = new Label(text);
        l.setMinWidth(90);
        return l;
    }

    // Nat (tid-sektioner) + Nicolai (absence-sektion – markeret længere nede)
    private Tab buildTimeRegistrationTab() {
        Tab tab = new Tab("Register Time");
        VBox content = new VBox(14);
        content.setPadding(new Insets(16));

        // ── Register time ──
        Label regTitle = new Label("Register time on an activity:");
        regTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        ComboBox<String> initialsCombo = new ComboBox<>(developerInitialsList);
        initialsCombo.setPromptText("Select developer...");
        initialsCombo.setPrefWidth(200);
        ComboBox<String> activityCombo = new ComboBox<>(activityNames);
        activityCombo.setPromptText("Select activity...");
        activityCombo.setPrefWidth(200);
        TextField hoursField = new TextField();
        hoursField.setPromptText("e.g. 2.5");
        hoursField.setPrefWidth(100);
        DatePicker datePicker = new DatePicker(LocalDate.now());

        Button registerBtn = new Button("Register Time");
        registerBtn.setPrefWidth(180);
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

        VBox form = new VBox(8);
        form.getChildren().addAll(
            new HBox(12, formLabel("Developer :"), initialsCombo),
            new HBox(12, formLabel("Activity :"), activityCombo),
            new HBox(12, formLabel("Hours :"), hoursField),
            new HBox(12, formLabel("Date :"), datePicker)
        );

        // ── See today's hours ──
        Label todayTitle = new Label("Today's registered hours:");
        todayTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        ComboBox<String> todayDevCombo = new ComboBox<>(developerInitialsList);
        todayDevCombo.setPromptText("Select developer...");
        todayDevCombo.setPrefWidth(160);
        DatePicker todayPicker = new DatePicker(LocalDate.now());
        Label todayResultLabel = new Label();
        todayResultLabel.setPadding(new Insets(2, 0, 0, 0));
        Button checkTodayBtn = new Button("Check Hours");
        checkTodayBtn.setPrefWidth(130);
        checkTodayBtn.setOnAction(e -> {
            String initials = todayDevCombo.getValue();
            if (initials == null) { showError("Select a developer."); return; }
            double total = timeRegistrationService.getTodayHours(initials, todayPicker.getValue());
            todayResultLabel.setText(initials + " has registered " + total + " hours on " + todayPicker.getValue());
        });
        HBox todayRow = new HBox(12, todayDevCombo, todayPicker, checkTodayBtn);
        todayRow.setAlignment(Pos.CENTER_LEFT);

        // ── Edit time registration ──
        Label editTitle = new Label("Edit time registration:");
        editTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        ComboBox<String> editDevCombo = new ComboBox<>(developerInitialsList);
        editDevCombo.setPromptText("Select developer...");
        editDevCombo.setPrefWidth(200);
        ComboBox<String> editActivityCombo = new ComboBox<>(activityNames);
        editActivityCombo.setPromptText("Select activity...");
        editActivityCombo.setPrefWidth(200);
        DatePicker editDatePicker = new DatePicker(LocalDate.now());
        TextField editHoursField = new TextField();
        editHoursField.setPromptText("e.g. 3.5");
        editHoursField.setPrefWidth(100);
        Button editBtn = new Button("Update Registration");
        editBtn.setPrefWidth(180);
        editBtn.setOnAction(e -> {
            String projectId = getSelectedProjectId();
            if (projectId == null) { showError("Select a project first."); return; }
            String initials = editDevCombo.getValue();
            String actName = editActivityCombo.getValue();
            if (initials == null) { showError("Select a developer."); return; }
            if (actName == null) { showError("Select an activity."); return; }
            try {
                double newHours = Double.parseDouble(editHoursField.getText().trim());
                timeRegistrationService.editTimeRegistration(initials, projectId, actName, editDatePicker.getValue(), newHours);
                refreshOverview(projectId);
                showInfo("Updated registration to " + newHours + " hours.");
                editHoursField.clear();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        VBox editForm = new VBox(8);
        editForm.getChildren().addAll(
            new HBox(12, formLabel("Developer :"), editDevCombo),
            new HBox(12, formLabel("Activity :"), editActivityCombo),
            new HBox(12, formLabel("Date :"), editDatePicker),
            new HBox(12, formLabel("New hours :"), editHoursField)
        );

        // ── Absence section ── (Nicolai)
        Label absenceTitle = new Label("Register absence:");
        absenceTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        ComboBox<String> absInitialsCombo = new ComboBox<>(developerInitialsList);
        absInitialsCombo.setPromptText("Select developer...");
        absInitialsCombo.setPrefWidth(160);
        ComboBox<String> typeBox = new ComboBox<>(
                FXCollections.observableArrayList("VACATION", "SICK_LEAVE", "COURSE", "OTHER"));
        typeBox.setValue("VACATION");
        typeBox.setPrefWidth(160);
        TextField absStartWeek = new TextField(); absStartWeek.setPromptText("Week"); absStartWeek.setPrefWidth(55);
        TextField absStartYear = new TextField(String.valueOf(LocalDate.now().getYear())); absStartYear.setPrefWidth(60);
        TextField absEndWeek = new TextField(); absEndWeek.setPromptText("Week"); absEndWeek.setPrefWidth(55);
        TextField absEndYear = new TextField(String.valueOf(LocalDate.now().getYear())); absEndYear.setPrefWidth(60);

        Button absRegisterBtn = new Button("Register Absence");
        absRegisterBtn.setPrefWidth(180);
        absRegisterBtn.setOnAction(e -> {
            String initials = absInitialsCombo.getValue();
            if (initials == null) { showError("Select a developer."); return; }
            try {
                absenceService.registerAbsence(
                        initials,
                        Absence.Type.valueOf(typeBox.getValue()),
                        Integer.parseInt(absStartWeek.getText().trim()),
                        Integer.parseInt(absStartYear.getText().trim()),
                        Integer.parseInt(absEndWeek.getText().trim()),
                        Integer.parseInt(absEndYear.getText().trim()));
                showInfo("Absence registered for " + initials + ".");
                absStartWeek.clear(); absEndWeek.clear();
                String selectedId = getSelectedProjectId();
                if (selectedId != null) refreshOverview(selectedId);
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        VBox absForm = new VBox(8);
        absForm.getChildren().addAll(
            new HBox(12, formLabel("Developer :"), absInitialsCombo),
            new HBox(12, formLabel("Type :"), typeBox),
            new HBox(8,  formLabel("Start :"), absStartWeek, new Label("w  /"), absStartYear),
            new HBox(8,  formLabel("End :"), absEndWeek,     new Label("w  /"), absEndYear)
        );

        content.setFillWidth(true);
        content.setMaxWidth(Double.MAX_VALUE);
        content.getChildren().addAll(
                regTitle, form, registerBtn,
                new Separator(),
                todayTitle, todayRow, todayResultLabel,
                new Separator(),
                editTitle, editForm, editBtn,
                new Separator(),
                absenceTitle, absForm, absRegisterBtn);
        ScrollPane timeSp = new ScrollPane(content);
        timeSp.setFitToWidth(true);
        tab.setContent(timeSp);
        return tab;
    }

    // ── Report tab ────────────────────────────────────────────────────────────────

    // Vedanta
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
        table.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(table, Priority.ALWAYS);

        Label totalLabel = new Label();

        Button generateBtn = new Button("Generate Report");
        generateBtn.setOnAction(e -> {
            String projectId = getSelectedProjectId();
            if (projectId == null) { showError("Select a project first."); return; }
            Project project = projectService.getProject(projectId);
            table.setItems(FXCollections.observableArrayList(project.getActivities()));
            totalLabel.setText(String.format("Total:  Budgeted: %.1f h   Registered: %.1f h",
                    project.getTotalBudgetedHours(), project.getTotalRegisteredHours()));
        });

        content.setFillWidth(true);
        content.setMaxWidth(Double.MAX_VALUE);
        content.getChildren().addAll(
                new Label("Project report (select project then click generate):"),
                generateBtn, table, totalLabel);
        tab.setContent(content);
        return tab;
    }

    // ── Available developers tab ──────────────────────────────────────────────────

    // Mathias
    private Tab buildAvailableDevelopersTab() {
        Tab tab = new Tab("Available Developers");
        VBox content = new VBox(10);
        content.setPadding(new Insets(12));

        TextField weekField = new TextField();
        weekField.setPromptText("Week (e.g. 11)");
        TextField yearField = new TextField(String.valueOf(LocalDate.now().getYear()));
        ListView<String> resultList = new ListView<>();

        Button checkBtn = new Button("Check Availability");
        checkBtn.setOnAction(e -> {
            try {
                int week = Integer.parseInt(weekField.getText().trim());
                int year = Integer.parseInt(yearField.getText().trim());
                List<Developer> available = availabilityService.getAvailableDevelopers(week, year);
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

        resultList.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(resultList, Priority.ALWAYS);
        content.setFillWidth(true);
        content.setMaxWidth(Double.MAX_VALUE);
        content.getChildren().addAll(new Label("Find available developers for a given week:"), row, resultList);
        tab.setContent(content);
        return tab;
    }


    // ── My Projects tab (project leader view) ────────────────────────────────────

    // Vedanta
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
        leaderCombo.setPrefWidth(160);

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

        // Assign developer form
        Label assignLabel = new Label("Assign developer to activity:");
        assignLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        ObservableList<String> myActNames = FXCollections.observableArrayList();
        ComboBox<String> assignActCombo = new ComboBox<>(myActNames);
        assignActCombo.setPromptText("Select activity...");
        assignActCombo.setPrefWidth(160);
        ComboBox<String> assignDevCombo = new ComboBox<>(developerInitialsList);
        assignDevCombo.setPromptText("Select developer...");
        assignDevCombo.setPrefWidth(130);
        Button assignBtn = new Button("Assign Developer");

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

        VBox assignForm = new VBox(5);
        assignForm.getChildren().addAll(
            new HBox(8, new Label("Activity:  "), assignActCombo),
            new HBox(8, new Label("Developer:"), assignDevCombo)
        );

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

    // Viktor
    private void refreshActivityTable(TableView<Activity> table, String projectId) {
        Project p = projectService.getProject(projectId);
        table.setItems(FXCollections.observableArrayList(p.getActivities()));
        table.refresh();
        refreshActivityNames(projectId);
    }

    // Viktor
    private void refreshActivityNames(String projectId) {
        Project p = projectService.getProject(projectId);
        activityNames.setAll(p.getActivities().stream()
                .map(Activity::getName)
                .collect(java.util.stream.Collectors.toList()));
    }

    // Mathias
    private void refreshDeveloperList() {
        developerInitialsList.setAll(developerService.getAllDevelopers().stream()
                .map(Developer::getInitials)
                .collect(java.util.stream.Collectors.toList()));
    }

    // Nat (UI-helper)
    private String getSelectedProjectId() {
        String selected = projectListView.getSelectionModel().getSelectedItem();
        if (selected == null) return null;
        return selected.substring(1, selected.indexOf("]"));
    }

    // Nat (UI-helper)
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // Nat (UI-helper)
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // Nat (UI-helper)
    public BorderPane getRoot() {
        return root;
    }
}
