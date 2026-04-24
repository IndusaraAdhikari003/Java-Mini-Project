package com.example.javaminiproject.controller;

import com.example.javaminiproject.MainApp;
import com.example.javaminiproject.dao.*;
import com.example.javaminiproject.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import java.util.List;
import javafx.scene.control.TextInputControl;

import com.example.javaminiproject.model.Medical;
import com.example.javaminiproject.dao.MedicalDAO;

public class AdminController {

    @FXML private VBox  contentPane;
    @FXML private Label labelUserName;

    private User user;

    // DAOs
    private final UserDAO       userDAO       = new UserDAO();
    private final CourseDAO     courseDAO     = new CourseDAO();
    private final NoticeDAO     noticeDAO     = new NoticeDAO();
    private final TimetableDAO  timetableDAO  = new TimetableDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final MarksDAO      marksDAO      = new MarksDAO();

    public void setUser(User user) { this.user = user; }

    @FXML
    public void initialize() {
        if (user == null) return;
        if (labelUserName != null) labelUserName.setText(user.getFullName());
        showDashboard();
    }

    // ── DASHBOARD ──────────────────────────────────────────
    @FXML
    public void showDashboard() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(makeTitle("📊 Dashboard"));

        HBox statsRow = new HBox(16);
        try {
            int students  = userDAO.getAllByRole("UNDERGRADUATE").size();
            int lecturers = userDAO.getAllByRole("LECTURER").size();
            int courses   = courseDAO.getAllCourses().size();
            int notices   = noticeDAO.getAll().size();

            // Clickable cards — each navigates to its section
            VBox cardStudents  = clickableStatCard("👥 Students",  String.valueOf(students),  "#1a73e8", () -> showManageUsers());
            VBox cardLecturers = clickableStatCard("🎓 Lecturers", String.valueOf(lecturers), "#28a745", () -> showLecturers());
            VBox cardCourses   = clickableStatCard("📚 Courses",   String.valueOf(courses),   "#fd7e14", () -> showManageCourses());
            VBox cardNotices   = clickableStatCard("📢 Notices",   String.valueOf(notices),   "#6f42c1", () -> showNotices());

            statsRow.getChildren().addAll(cardStudents, cardLecturers, cardCourses, cardNotices);

        } catch (Exception e) {
            statsRow.getChildren().add(new Label("Error: " + e.getMessage()));
        }
        contentPane.getChildren().add(statsRow);

        // Quick recent notices preview
        try {
            contentPane.getChildren().add(makeTitle("📋 Recent Notices"));

            VBox noticeBox = new VBox(8);
            noticeBox.getStyleClass().add("card");
            noticeBox.setPadding(new Insets(12));

            List<Notice> notices = noticeDAO.getAll();

            if (notices.isEmpty()) {
                Label emptyLbl = new Label("No notices yet.");
                emptyLbl.setStyle("-fx-font-size:13; -fx-text-fill:black;");
                noticeBox.getChildren().add(emptyLbl);
            } else {
                notices.stream().limit(3).forEach(n -> {
                    String dateText = (n.getCreatedAt() != null)
                            ? n.getCreatedAt().toLocalDate().toString()
                            : "No date";

                    String titleText = (n.getTitle() != null && !n.getTitle().isBlank())
                            ? n.getTitle()
                            : "Untitled Notice";

                    Label lbl = new Label("• " + titleText + " — " + dateText);
                    lbl.setWrapText(true);
                    lbl.setStyle("-fx-font-size:13; -fx-text-fill:black;");
                    noticeBox.getChildren().add(lbl);
                });
            }

            contentPane.getChildren().add(noticeBox);

        } catch (Exception e) {
            Label errorLbl = new Label("Error loading notices: " + e.getMessage());
            errorLbl.setStyle("-fx-text-fill:red; -fx-font-size:13;");
            contentPane.getChildren().add(errorLbl);
        }
    }
    //---Stat card clickable----
    // Clickable stat card with hover effect
    private VBox clickableStatCard(String label, String value, String color, Runnable onClick) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        card.setStyle("-fx-cursor: hand;");

        Label num = new Label(value);
        num.getStyleClass().add("stat-number");
        num.setStyle("-fx-text-fill:" + color + ";");

        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");

        card.getChildren().addAll(num, lbl);

        // Hover effect
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.18),12,0,0,4);"));
        card.setOnMouseExited(e ->
                card.setStyle("-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);"));

        // Click action
        card.setOnMouseClicked(e -> onClick.run());

        return card;
    }

    // ── LECTURERS (Add / Delete) ───────────────────────────
    public void showLecturers() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(makeTitle("🎓 Lecturers"));

        // --- Table with Delete button column ---
        TableView<User> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        table.getColumns().addAll(
                col("ID",         u -> String.valueOf(u.getUserId())),
                col("Full Name",  u -> u.getFullName()),
                col("Username",   u -> u.getUsername()),
                col("Email",      u -> u.getEmail()),
                col("Phone",      u -> u.getPhone()),
                col("Department", u -> u.getDepartment())
        );

        // Delete action column
        TableColumn<User, String> colDel = new TableColumn<>("Action");
        colDel.setCellFactory(tc -> new TableCell<>() {
            final Button btnDel = new Button("🗑 Delete");
            {
                btnDel.getStyleClass().add("btn-danger");
                btnDel.setStyle("-fx-padding:4 10;-fx-font-size:12;");
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                User u = getTableView().getItems().get(getIndex());
                btnDel.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete lecturer \"" + u.getFullName() + "\"?\nThis cannot be undone.");
                    confirm.setHeaderText("Confirm Delete");
                    confirm.showAndWait().ifPresent(res -> {
                        if (res == ButtonType.OK) {
                            try {
                                userDAO.deleteUser(u.getUserId());
                                loadLecturers(table);
                            } catch (Exception ex) { showError(ex.getMessage()); }
                        }
                    });
                });
                setGraphic(btnDel);
            }
        });
        table.getColumns().add(colDel);
        loadLecturers(table);

        // --- Toolbar ---
        Button btnAdd = new Button("+ Add Lecturer");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> showAddLecturerDialog(table));

        HBox toolbar = new HBox(10, btnAdd);

        contentPane.getChildren().addAll(toolbar, table);
    }

    private void loadLecturers(TableView<User> table) {
        try { table.setItems(FXCollections.observableArrayList(userDAO.getAllByRole("LECTURER"))); }
        catch (Exception e) { showError(e.getMessage()); }
    }

    private void showAddLecturerDialog(TableView<User> table) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Lecturer");
        dialog.setHeaderText("Fill in the lecturer details");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField fName  = new TextField(); fName.setPromptText("e.g. Dr. K. Silva");
        TextField fUser  = new TextField(); fUser.setPromptText("e.g. lec_silva");
        TextField fPass  = new TextField(); fPass.setPromptText("Password");
        TextField fEmail = new TextField(); fEmail.setPromptText("e.g. silva@tech.lk");
        TextField fPhone = new TextField(); fPhone.setPromptText("e.g. 0712000001");
        TextField fDept  = new TextField("Computer Science");

        grid.addRow(0, new Label("Full Name:"),  fName);
        grid.addRow(1, new Label("Username:"),   fUser);
        grid.addRow(2, new Label("Password:"),   fPass);
        grid.addRow(3, new Label("Email:"),      fEmail);
        grid.addRow(4, new Label("Phone:"),      fPhone);
        grid.addRow(5, new Label("Department:"), fDept);

        dialog.getDialogPane().setContent(grid);
        // Focus first field
        javafx.application.Platform.runLater(fName::requestFocus);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            if (isEmpty(fName, "Full Name")) return;
            if (isEmpty(fUser, "Username")) return;
            if (isEmpty(fPass, "Password")) return;
            if (isEmpty(fEmail, "Email")) return;
            if (isEmpty(fPhone, "Phone")) return;
            if (isEmpty(fDept, "Department")) return;

            if (!isValidEmail(fEmail.getText().trim())) {
                showError("Please enter a valid email address.");
                fEmail.requestFocus();
                return;
            }

            if (!isValidPhone(fPhone.getText().trim())) {
                showError("Phone number must contain exactly 10 digits.");
                fPhone.requestFocus();
                return;
            }
            try {
                User newLec = new Lecturer(0, fUser.getText().trim(), fPass.getText().trim(),
                        fName.getText().trim(), fEmail.getText().trim(),
                        fPhone.getText().trim(), fDept.getText().trim());
                userDAO.createUser(newLec, fPass.getText().trim());
                loadLecturers(table);
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
    }
    // ── MANAGE USERS ──────────────────────────────────────
    @FXML
    public void showManageUsers() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(makeTitle("👥 Manage Users"));

        // Role filter
        HBox toolbar = new HBox(10);
        ComboBox<String> roleFilter = new ComboBox<>();
        roleFilter.getItems().addAll("UNDERGRADUATE","LECTURER","TECH_OFFICER");
        roleFilter.setValue("UNDERGRADUATE");

        Button btnAdd = new Button("+ Add User");
        btnAdd.getStyleClass().add("btn-primary");
        toolbar.getChildren().addAll(new Label("Role:"), roleFilter, btnAdd);

        // Table
        TableView<User> table = createUserTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        // Load data on filter change
        roleFilter.setOnAction(e -> loadUsers(table, roleFilter.getValue()));
        loadUsers(table, "UNDERGRADUATE");

        btnAdd.setOnAction(e -> showAddUserDialog(roleFilter.getValue(), table));

        contentPane.getChildren().addAll(toolbar, table);
    }

    private TableView<User> createUserTable() {
        TableView<User> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User,String> colId   = col("ID",       u -> String.valueOf(u.getUserId()));
        TableColumn<User,String> colName = col("Full Name", u -> u.getFullName());
        TableColumn<User,String> colUser = col("Username",  u -> u.getUsername());
        TableColumn<User,String> colEmail= col("Email",     u -> u.getEmail());
        TableColumn<User,String> colDept = col("Department",u -> u.getDepartment());

        TableColumn<User,String> colAction = new TableColumn<>("Action");
        colAction.setCellFactory(tc -> new TableCell<>() {
            final Button del = new Button("Delete");
            { del.getStyleClass().add("btn-danger"); del.setStyle("-fx-padding:4 10;-fx-font-size:12;"); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                User u = getTableView().getItems().get(getIndex());
                del.setOnAction(e -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + u.getFullName() + "?");
                    alert.showAndWait().ifPresent(res -> {
                        if (res == ButtonType.OK) {
                            try { userDAO.deleteUser(u.getUserId()); loadUsers(getTableView(), u.getRole()); }
                            catch (Exception ex) { showError(ex.getMessage()); }
                        }
                    });
                });
                setGraphic(del);
            }
        });

        table.getColumns().addAll(colId, colName, colUser, colEmail, colDept, colAction);
        return table;
    }

    private void loadUsers(TableView<User> table, String role) {
        try {
            List<User> users = userDAO.getAllByRole(role);
            table.setItems(FXCollections.observableArrayList(users));
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void showAddUserDialog(String role, TableView<User> table) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New " + role);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField fName  = new TextField(); fName.setPromptText("Full name");
        TextField fUser  = new TextField(); fUser.setPromptText("Username");
        TextField fPass  = new TextField(); fPass.setPromptText("Password");
        TextField fEmail = new TextField(); fEmail.setPromptText("Email");
        TextField fPhone = new TextField(); fPhone.setPromptText("Phone");
        TextField fDept  = new TextField(); fDept.setPromptText("Department");
        // Only for UG
        TextField fReg   = new TextField(); fReg.setPromptText("Reg Number e.g IT/2023/001");
        TextField fBatch = new TextField(); fBatch.setPromptText("Batch e.g 2023");

        int row = 0;
        grid.addRow(row++, new Label("Full Name:"), fName);
        grid.addRow(row++, new Label("Username:"),  fUser);
        grid.addRow(row++, new Label("Password:"),  fPass);
        grid.addRow(row++, new Label("Email:"),     fEmail);
        grid.addRow(row++, new Label("Phone:"),     fPhone);
        grid.addRow(row++, new Label("Department:"),fDept);
        if (role.equals("UNDERGRADUATE")) {
            grid.addRow(row++, new Label("Reg Number:"), fReg);
            grid.addRow(row++, new Label("Batch:"),      fBatch);
        }
        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (isEmpty(fName, "Full Name")) return;
                if (isEmpty(fUser, "Username")) return;
                if (isEmpty(fPass, "Password")) return;
                if (isEmpty(fEmail, "Email")) return;
                if (isEmpty(fPhone, "Phone")) return;
                if (isEmpty(fDept, "Department")) return;

                if (!isValidEmail(fEmail.getText().trim())) {
                    showError("Please enter a valid email address.");
                    fEmail.requestFocus();
                    return;
                }

                if (!isValidPhone(fPhone.getText().trim())) {
                    showError("Phone number must contain exactly 10 digits.");
                    fPhone.requestFocus();
                    return;
                }

                if (role.equals("UNDERGRADUATE")) {
                    if (isEmpty(fReg, "Reg Number")) return;
                    if (isEmpty(fBatch, "Batch")) return;
                }
                try {
                    User newUser;
                    if (role.equals("UNDERGRADUATE")) {
                        newUser = new Undergraduate(
                                0,
                                fUser.getText(),
                                fPass.getText(),
                                fName.getText(),
                                fEmail.getText(),
                                fPhone.getText(),
                                fDept.getText(),
                                0,
                                fReg.getText(),
                                fBatch.getText(),
                                false,
                                false
                        );
                    } else if (role.equals("LECTURER")) {
                        newUser = new Lecturer(0, fUser.getText(), fPass.getText(),
                                fName.getText(), fEmail.getText(), fPhone.getText(), fDept.getText());
                    } else {
                        newUser = new TechnicalOfficer(0, fUser.getText(), fPass.getText(),
                                fName.getText(), fEmail.getText(), fPhone.getText(), fDept.getText());
                    }
                    userDAO.createUser(newUser, fPass.getText());
                    loadUsers(table, role);
                } catch (Exception e) {
                    showError(e.getMessage());
                }
            }
        });
    }

    // ── MANAGE COURSES ─────────────────────────────────────
    @FXML
    public void showManageCourses() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(makeTitle("📚 Manage Courses"));

        TableView<Course> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getColumns().addAll(
                col("Code",       (Course c) -> c.getCourseCode()),
                col("Name",       c -> c.getCourseName()),
                col("Credits",    c -> String.valueOf(c.getCredits())),
                col("Theory",     c -> c.isHasTheory() ? "Yes" : "No"),
                col("Practical",  c -> c.isHasPractical() ? "Yes" : "No"),
                col("Lecturer",   c -> c.getLecturerName())
        );

        loadCourses(table);

        HBox toolbar = new HBox(10);
        Button btnAdd = new Button("+ Add Course"); btnAdd.getStyleClass().add("btn-primary");
        Button btnDel = new Button("Delete Selected"); btnDel.getStyleClass().add("btn-danger");
        toolbar.getChildren().addAll(btnAdd, btnDel);

        btnAdd.setOnAction(e -> showAddCourseDialog(table));
        btnDel.setOnAction(e -> {
            Course selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            try { courseDAO.deleteCourse(selected.getCourseId()); loadCourses(table); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });

        contentPane.getChildren().addAll(toolbar, table);
    }

    private void loadCourses(TableView<Course> table) {
        try { table.setItems(FXCollections.observableArrayList(courseDAO.getAllCourses())); }
        catch (Exception e) { showError(e.getMessage()); }
    }

    private void showAddCourseDialog(TableView<Course> table) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Course");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField fCode = new TextField(); fCode.setPromptText("e.g IT3101");
        TextField fName = new TextField(); fName.setPromptText("Course name");
        TextField fCred = new TextField(); fCred.setPromptText("Credits (e.g 3)");
        CheckBox  cbT   = new CheckBox("Has Theory");     cbT.setSelected(true);
        CheckBox  cbP   = new CheckBox("Has Practical");
        ComboBox<String> cbLec = new ComboBox<>();

        try {
            userDAO.getAllByRole("LECTURER").forEach(u -> cbLec.getItems().add(u.getUserId() + " - " + u.getFullName()));
        } catch (Exception ignored) {}

        grid.addRow(0, new Label("Code:"),     fCode);
        grid.addRow(1, new Label("Name:"),     fName);
        grid.addRow(2, new Label("Credits:"),  fCred);
        grid.addRow(3, new Label("Type:"),     new HBox(10, cbT, cbP));
        grid.addRow(4, new Label("Lecturer:"), cbLec);
        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    int lecId = Integer.parseInt(cbLec.getValue().split(" - ")[0]);
                    Course c = new Course(0, fCode.getText(), fName.getText(),
                            Integer.parseInt(fCred.getText()),
                            cbT.isSelected(), cbP.isSelected(), "Computer Science", lecId, "");
                    courseDAO.addCourse(c);
                    loadCourses(table);
                } catch (Exception e) { showError(e.getMessage()); }
            }
        });
    }

    // ── NOTICES ────────────────────────────────────────────
    @FXML
    public void showNotices() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(makeTitle("📢 Notices"));

        TableView<Notice> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Notice, String> colTitle = col("Title", (Notice n) -> n.getTitle());
        TableColumn<Notice, String> colBy = col("By", n -> n.getCreatedByName());
        TableColumn<Notice, String> colDate = col("Date",
                n -> n.getCreatedAt() != null ? n.getCreatedAt().toLocalDate().toString() : "No date");
        TableColumn<Notice, String> colContent = col("Content", n -> n.getContent());

        TableColumn<Notice, String> colAction = new TableColumn<>("Action");
        colAction.setMinWidth(120);

        colAction.setCellFactory(tc -> new TableCell<>() {
            final Button btnDelete = new Button("Delete");

            {
                btnDelete.getStyleClass().add("btn-danger");
                btnDelete.setStyle("-fx-padding:4 12;-fx-font-size:11;-fx-cursor:hand;");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                Notice n = getTableView().getItems().get(getIndex());

                btnDelete.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Notice");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Delete this notice?");

                    confirm.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            try {
                                noticeDAO.deleteNotice(n.getNoticeId());
                                loadNotices(table);
                            } catch (Exception ex) {
                                showError(ex.getMessage());
                            }
                        }
                    });
                });

                setGraphic(btnDelete);
            }
        });

        table.getColumns().addAll(colTitle, colBy, colDate, colContent, colAction);

        loadNotices(table);

        table.setRowFactory(tv -> {
            TableRow<Notice> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Notice selectedNotice = row.getItem();
                    showNoticeDetails(selectedNotice);
                }
            });
            return row;
        });

        TextField fTitle = new TextField();
        fTitle.setPromptText("Notice title");

        TextArea fContent = new TextArea();
        fContent.setPromptText("Notice content");
        fContent.setPrefRowCount(3);

        Button btnAdd = new Button("Post Notice");
        btnAdd.getStyleClass().add("btn-primary");

        btnAdd.setOnAction(e -> {
            try {
                if (isEmpty(fTitle, "Notice title")) return;
                if (isEmpty(fContent, "Notice content")) return;

                Notice n = new Notice(
                        0,
                        fTitle.getText().trim(),
                        fContent.getText().trim(),
                        user.getUserId(),
                        null
                );
                noticeDAO.addNotice(n);
                fTitle.clear();
                fContent.clear();
                loadNotices(table);
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        VBox form = new VBox(8, fTitle, fContent, new HBox(10, btnAdd));
        form.getStyleClass().add("card");

        contentPane.getChildren().addAll(form, table);
    }

    private void loadNotices(TableView<Notice> table) {
        try {
            table.setItems(FXCollections.observableArrayList(noticeDAO.getAll()));
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
    // ── TIMETABLE ──────────────────────────────────────────
    @FXML
    public void showTimetable() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(makeTitle("🗓 Timetables"));

        // ── ADD FORM ──
        VBox formCard = new VBox(10);
        formCard.getStyleClass().add("card");
        formCard.getChildren().add(new Label("➕ Add New Timetable Entry") {{
            setStyle("-fx-font-weight:bold;-fx-font-size:13;");
        }});

        try {
            List<Course> courses = courseDAO.getAllCourses();

            ComboBox<Course> cbCourse = new ComboBox<>(FXCollections.observableArrayList(courses));
            cbCourse.setPromptText("Select Course");

            ComboBox<String> cbDay = new ComboBox<>(FXCollections.observableArrayList(
                    "MON","TUE","WED","THU","FRI","SAT"));
            cbDay.setPromptText("Day");

            ComboBox<String> cbType = new ComboBox<>(FXCollections.observableArrayList(
                    "THEORY","PRACTICAL"));
            cbType.setPromptText("Session Type");

            TextField fStart    = new TextField(); fStart.setPromptText("Start time e.g 08:00");
            TextField fEnd      = new TextField(); fEnd.setPromptText("End time e.g 10:00");
            TextField fLocation = new TextField(); fLocation.setPromptText("Location e.g Lab 01 / Hall A");

            Button btnAdd = new Button("Add Entry");
            btnAdd.getStyleClass().add("btn-primary");

            Label lblStatus = new Label();

            GridPane grid = new GridPane();
            grid.setHgap(12); grid.setVgap(10);
            grid.addRow(0, new Label("Course:"),   cbCourse,  new Label("Day:"),      cbDay);
            grid.addRow(1, new Label("Type:"),     cbType,    new Label("Location:"), fLocation);
            grid.addRow(2, new Label("Start:"),    fStart,    new Label("End:"),      fEnd);
            grid.addRow(3, btnAdd, lblStatus);

            // Table
            TableView<Timetable> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.getColumns().addAll(
                    colTT("Day",      t -> t.getDayOfWeek()),
                    colTT("Course",   t -> t.getCourseCode()),
                    colTT("Name",     t -> t.getCourseName()),
                    colTT("Type",     t -> t.getSessionType()),
                    colTT("Start",    t -> t.getStartTime().toString()),
                    colTT("End",      t -> t.getEndTime().toString()),
                    colTT("Location", t -> t.getLocation() != null ? t.getLocation() : "TBA")
            );

            // Delete column
            TableColumn<Timetable, String> colDel = new TableColumn<>("Action");
            colDel.setCellFactory(tc -> new TableCell<>() {
                final Button del = new Button("Delete");
                { del.getStyleClass().add("btn-danger");
                    del.setStyle("-fx-padding:4 10;-fx-font-size:11;"); }
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) { setGraphic(null); return; }
                    Timetable t = getTableView().getItems().get(getIndex());
                    del.setOnAction(e -> {
                        try {
                            timetableDAO.deleteTimetable(t.getTtId());
                            loadTimetable(table);
                            lblStatus.setText("✅ Entry deleted.");
                            lblStatus.setStyle("-fx-text-fill:#28a745;");
                        } catch (Exception ex) {
                            lblStatus.setText("❌ " + ex.getMessage());
                            lblStatus.setStyle("-fx-text-fill:#dc3545;");
                        }
                    });
                    setGraphic(del);
                }
            });
            table.getColumns().add(colDel);
            loadTimetable(table);

            btnAdd.setOnAction(e -> {
                try {
                    if (cbCourse.getValue() == null || cbDay.getValue() == null
                            || cbType.getValue() == null
                            || fStart.getText().isBlank() || fEnd.getText().isBlank()) {
                        lblStatus.setText("⚠ Please fill all fields.");
                        lblStatus.setStyle("-fx-text-fill:#fd7e14;");
                        return;
                    }
                    Timetable tt = new Timetable(0,
                            cbCourse.getValue().getCourseId(),
                            cbDay.getValue(),
                            java.time.LocalTime.parse(fStart.getText().trim()),
                            java.time.LocalTime.parse(fEnd.getText().trim()),
                            fLocation.getText().trim(),
                            cbType.getValue());
                    timetableDAO.addTimetable(tt);
                    lblStatus.setText("✅ Timetable entry added!");
                    lblStatus.setStyle("-fx-text-fill:#28a745;");
                    fStart.clear(); fEnd.clear(); fLocation.clear();
                    cbCourse.setValue(null); cbDay.setValue(null); cbType.setValue(null);
                    loadTimetable(table);
                } catch (Exception ex) {
                    lblStatus.setText("❌ Error: " + ex.getMessage());
                    lblStatus.setStyle("-fx-text-fill:#dc3545;");
                }
            });

            formCard.getChildren().add(grid);
            contentPane.getChildren().addAll(formCard, table);

        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }

    // Helper to load timetable into table
    private void loadTimetable(TableView<Timetable> table) {
        try {
            table.setItems(FXCollections.observableArrayList(timetableDAO.getAll()));
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // Column helper for Timetable
    private TableColumn<Timetable, String> colTT(String h,
                                                 java.util.function.Function<Timetable, String> fn) {
        TableColumn<Timetable, String> c = new TableColumn<>(h);
        c.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue() == null ? "" : fn.apply(d.getValue())));
        return c;
    }
    @FXML
    public void showAllMedicals() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(makeTitle("🏥 All Medical Records"));
        try {
            MedicalDAO medDAO = new MedicalDAO();
            List<Medical> all = medDAO.getAll();

            long approved = all.stream().filter(Medical::isApproved).count();
            long pending  = all.stream().filter(m -> !m.isApproved()).count();

            HBox statsRow = new HBox(16);
            statsRow.getChildren().addAll(
                    statCard("Total",    String.valueOf(all.size()), "#1a73e8"),
                    statCard("Approved", String.valueOf(approved),    "#28a745"),
                    statCard("Pending",  String.valueOf(pending),     "#fd7e14")
            );
            contentPane.getChildren().add(statsRow);

            TableView<Medical> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            table.getColumns().addAll(
                    col("Student",  m -> m.getStudentName() != null ? m.getStudentName() : "—"),
                    col("From",     m -> m.getFromDate().toString()),
                    col("To",       m -> m.getToDate().toString()),
                    col("Reason",   m -> m.getReason() != null ? m.getReason() : "—"),
                    col("Document", m -> m.getDocPath() != null ? m.getDocPath() : "Not attached"),
                    col("Status",   m -> m.isApproved() ? "✅ Approved" : "⏳ Pending")
            );
            table.setItems(FXCollections.observableArrayList(all));
            contentPane.getChildren().add(table);

        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }


    private void showAddTimetableDialog(TableView<Timetable> table) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Timetable Entry");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Course picker
        ComboBox<Course> cbCourse = new ComboBox<>();
        try { cbCourse.setItems(FXCollections.observableArrayList(courseDAO.getAllCourses())); }
        catch (Exception ignored) {}

        ComboBox<String> cbDay  = new ComboBox<>(
                FXCollections.observableArrayList("MON","TUE","WED","THU","FRI","SAT"));
        ComboBox<String> cbType = new ComboBox<>(
                FXCollections.observableArrayList("THEORY","PRACTICAL"));

        // Time fields — format HH:MM
        TextField fStart    = new TextField(); fStart.setPromptText("e.g. 08:00");
        TextField fEnd      = new TextField(); fEnd.setPromptText("e.g. 10:00");
        TextField fLocation = new TextField(); fLocation.setPromptText("e.g. Lab A / LT-01");

        cbDay.setValue("MON");
        cbType.setValue("THEORY");

        grid.addRow(0, new Label("Course:"),   cbCourse);
        grid.addRow(1, new Label("Day:"),      cbDay);
        grid.addRow(2, new Label("Type:"),     cbType);
        grid.addRow(3, new Label("Start (HH:MM):"), fStart);
        grid.addRow(4, new Label("End   (HH:MM):"), fEnd);
        grid.addRow(5, new Label("Location:"), fLocation);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            try {
                if (cbCourse.getValue() == null) { showError("Please select a course."); return; }
                java.time.LocalTime start = java.time.LocalTime.parse(fStart.getText().trim());
                java.time.LocalTime end   = java.time.LocalTime.parse(fEnd.getText().trim());
                if (!end.isAfter(start)) { showError("End time must be after start time."); return; }

                Timetable tt = new Timetable(
                        0, cbCourse.getValue().getCourseId(),
                        cbDay.getValue(), start, end,
                        fLocation.getText().trim(), cbType.getValue());
                timetableDAO.addTimetable(tt);
                loadTimetable(table);
            } catch (java.time.format.DateTimeParseException ex) {
                showError("Invalid time format. Use HH:MM (e.g. 08:30).");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
    }

    // ── LOGOUT ─────────────────────────────────────────────
    @FXML
    public void handleLogout() {
        try { MainApp.showLogin(); }
        catch (Exception e) { e.printStackTrace(); }
    }

    // ── HELPERS ────────────────────────────────────────────
    private Label makeTitle(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("page-title");
        return lbl;
    }

    private VBox statCard(String label, String value, String color) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        Label num = new Label(value); num.getStyleClass().add("stat-number"); num.setStyle("-fx-text-fill:" + color + ";");
        Label lbl = new Label(label); lbl.getStyleClass().add("stat-label");
        card.getChildren().addAll(num, lbl);
        return card;
    }

    private <T> TableColumn<T, String> col(String header, java.util.function.Function<T, String> extractor) {
        TableColumn<T, String> col = new TableColumn<>(header);
        col.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue() == null ? "" : String.valueOf(extractor.apply(data.getValue()))
        ));
        return col;
    }
    private void showNoticeDetails(Notice notice) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Notice Details");
        dialog.setHeaderText(notice.getTitle());

        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        VBox box = new VBox(10);
        box.setPadding(new Insets(15));

        String byText = (notice.getCreatedByName() != null) ? notice.getCreatedByName() : "Unknown";
        String dateText = (notice.getCreatedAt() != null) ? notice.getCreatedAt().toString() : "No date";
        String contentText = (notice.getContent() != null) ? notice.getContent() : "No content";

        Label lblTitle = new Label("Title: " + notice.getTitle());
        Label lblBy = new Label("By: " + byText);
        Label lblDate = new Label("Date: " + dateText);

        TextArea txtContent = new TextArea(contentText);
        txtContent.setWrapText(true);
        txtContent.setEditable(false);
        txtContent.setPrefRowCount(10);

        box.getChildren().addAll(lblTitle, lblBy, lblDate, new Label("Content:"), txtContent);

        dialog.getDialogPane().setContent(box);
        dialog.showAndWait();
    }


    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{10}");
    }

    private boolean isNumeric(String value) {
        return value != null && value.matches("\\d+");
    }
    private boolean isEmpty(TextInputControl field, String fieldName) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            showError(fieldName + " cannot be empty.");
            field.requestFocus();
            return true;
        }
        return false;
    }
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.showAndWait();
    }
}