package com.example.javaminiproject.controller;

import com.example.javaminiproject.MainApp;
import com.example.javaminiproject.dao.*;
import com.example.javaminiproject.model.*;
import com.example.javaminiproject.util.GradeCalculator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

import java.awt.Desktop;
import java.io.File;

public class LecturerController {

    @FXML private VBox  contentPane;
    @FXML private Label labelUserName;

    private User user;

    private final UserDAO       userDAO       = new UserDAO();
    private final CourseDAO     courseDAO     = new CourseDAO();
    private final MarksDAO      marksDAO      = new MarksDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final MedicalDAO    medicalDAO    = new MedicalDAO();
    private final NoticeDAO     noticeDAO     = new NoticeDAO();

    public void setUser(User u) { this.user = u; }

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
        contentPane.getChildren().add(title("📊 Welcome, " +
                (user != null ? user.getFullName() : "")));

        try {
            int students = userDAO.getAllUndergraduatesByDepartments(getAllowedDepartments()).size();
            int courses = courseDAO.getCoursesByDepartments(getAllowedDepartments()).size();
            int notices  = noticeDAO.getAll().size();

            HBox statsRow = new HBox(16);
            statsRow.getChildren().addAll(
                    statCard("👥 Students", String.valueOf(students), "#1a73e8"),
                    statCard("📚 Courses",  String.valueOf(courses),  "#28a745"),
                    statCard("📢 Notices",  String.valueOf(notices),  "#6f42c1")
            );
            contentPane.getChildren().add(statsRow);

            // ── PROFILE CARD ──
            contentPane.getChildren().add(title("👤 My Profile"));

            VBox profileCard = new VBox(0);
            profileCard.getStyleClass().add("card");
            profileCard.getChildren().addAll(
                    profileRow("👤 Name",       user.getFullName()),
                    profileRow("🏛 Department", user.getDepartment() != null
                            ? user.getDepartment() : "—"),
                    profileRow("📧 Email",      user.getEmail() != null
                            ? user.getEmail() : "—"),
                    profileRow("📱 Phone",      user.getPhone() != null
                            ? user.getPhone() : "—"),
                    profileRow("🎓 Role",       "Lecturer")
            );
            contentPane.getChildren().add(profileCard);

            // ── RECENT NOTICES ──
            contentPane.getChildren().add(title("📢 Recent Notices"));

            List<Notice> allNotices = noticeDAO.getAll();
            if (allNotices.isEmpty()) {
                VBox emptyBox = new VBox();
                emptyBox.getStyleClass().add("card");
                emptyBox.getChildren().add(new Label("No notices yet."));
                contentPane.getChildren().add(emptyBox);
            } else {
                allNotices.stream().limit(3).forEach(n -> {
                    VBox card = new VBox(6);
                    card.setStyle(
                            "-fx-background-color:white;-fx-background-radius:10;" +
                                    "-fx-padding:14 16;-fx-border-color:#1a73e8;" +
                                    "-fx-border-width:0 0 0 4;" +
                                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);");

                    String titleText = (n.getTitle() != null
                            && !n.getTitle().isBlank())
                            ? n.getTitle() : "(No Title)";
                    String bodyText = (n.getContent() != null
                            && !n.getContent().isBlank())
                            ? n.getContent() : "(No content)";
                    String byName = (n.getCreatedByName() != null)
                            ? n.getCreatedByName() : "Admin";

                    Label t = new Label("📌 " + titleText);
                    t.setStyle("-fx-font-weight:bold;-fx-font-size:14;" +
                            "-fx-text-fill:#1a1a2e;");
                    Label body = new Label(bodyText);
                    body.setWrapText(true);
                    body.setStyle("-fx-font-size:13;-fx-text-fill:#3c4043;");
                    Label meta = new Label("👤 " + byName
                            + "   📅 " + n.getCreatedAt().toLocalDate());
                    meta.setStyle("-fx-font-size:11;-fx-text-fill:#6c757d;");

                    card.getChildren().addAll(t, body, meta);
                    contentPane.getChildren().add(card);
                });
            }

        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    // ── STUDENT DETAILS ────────────────────────────────────
    @FXML
    public void showStudentDetails() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("👥 Undergraduate Details"));

        try {
            List<Undergraduate> students = userDAO.getAllUndergraduatesByDepartments(getAllowedDepartments());

            long repeats = students.stream().filter(Undergraduate::isRepeat).count();
            long missed  = students.stream().filter(Undergraduate::isBatchMissed).count();

            HBox summaryRow = new HBox(16);
            summaryRow.getChildren().addAll(
                    statCard("Total Students",  String.valueOf(students.size()), "#1a73e8"),
                    statCard("Repeat Students", String.valueOf(repeats),         "#dc3545"),
                    statCard("Batch Missed",    String.valueOf(missed),          "#fd7e14")
            );
            contentPane.getChildren().add(summaryRow);

            TableView<Undergraduate> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.getColumns().addAll(
                    col("Reg No", u -> u.getRegNumber()),
                    col("Name",   u -> u.getFullName()),
                    col("Batch",  u -> u.getBatch()),
                    col("Email",  u -> u.getEmail()  != null ? u.getEmail()  : "—"),
                    col("Phone",  u -> u.getPhone()  != null ? u.getPhone()  : "—"),
                    col("Repeat", u -> u.isRepeat()       ? "Yes" : "No"),
                    col("Missed", u -> u.isBatchMissed()  ? "Yes" : "No")
            );
            table.setItems(FXCollections.observableArrayList(students));
            contentPane.getChildren().add(table);

        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }

    // ── UPLOAD MARKS ───────────────────────────────────────
    @FXML
    public void showUploadMarks() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("📝 Upload Marks"));

        try {
            List<Course>        courses  = courseDAO.getCoursesByDepartments(getAllowedDepartments());
            List<Undergraduate> students = userDAO.getAllUndergraduatesByDepartments(getAllowedDepartments());

            ComboBox<Course>        cbCourse  = new ComboBox<>(
                    FXCollections.observableArrayList(courses));
            cbCourse.setPromptText("Select Course");

            ComboBox<Undergraduate> cbStudent = new ComboBox<>(
                    FXCollections.observableArrayList(students));
            cbStudent.setPromptText("Select Student");

            ComboBox<String> cbType = new ComboBox<>(FXCollections.observableArrayList(
                    "CA1","CA2","CA3","ASSIGNMENT","FINAL"));
            cbType.setValue("CA1");

            TextField fMarks = new TextField();
            fMarks.setPromptText("Enter marks (0 - 100)");

            Button btnSave   = new Button("💾 Save Mark");
            btnSave.getStyleClass().add("btn-primary");
            Label  lblStatus = new Label();

            btnSave.setOnAction(e -> {
                if (cbCourse.getValue() == null || cbStudent.getValue() == null) {
                    lblStatus.setText("⚠ Please select course and student.");
                    lblStatus.setStyle("-fx-text-fill:#fd7e14;-fx-font-weight:bold;");
                    return;
                }
                try {
                    double val = Double.parseDouble(fMarks.getText().trim());
                    if (val < 0 || val > 100) {
                        lblStatus.setText("⚠ Marks must be between 0 and 100.");
                        lblStatus.setStyle("-fx-text-fill:#fd7e14;-fx-font-weight:bold;");
                        return;
                    }
                    Mark m = new Mark(0,
                            cbStudent.getValue().getUgId(),
                            cbCourse.getValue().getCourseId(),
                            cbType.getValue(), val, user.getUserId());
                    marksDAO.addMark(m);
                    lblStatus.setText("✅ Saved! " + cbStudent.getValue().getFullName()
                            + " — " + cbType.getValue() + " — " + val);
                    lblStatus.setStyle("-fx-text-fill:#28a745;-fx-font-weight:bold;");
                    fMarks.clear();
                } catch (NumberFormatException nfe) {
                    lblStatus.setText("⚠ Please enter a valid number.");
                    lblStatus.setStyle("-fx-text-fill:#fd7e14;");
                } catch (Exception ex) {
                    lblStatus.setText("❌ Error: " + ex.getMessage());
                    lblStatus.setStyle("-fx-text-fill:#dc3545;");
                }
            });

            GridPane form = new GridPane();
            form.setHgap(12); form.setVgap(12);
            form.getStyleClass().add("card");
            form.setMaxWidth(500);
            form.addRow(0, boldLabel("Course:"),    cbCourse);
            form.addRow(1, boldLabel("Student:"),   cbStudent);
            form.addRow(2, boldLabel("Exam Type:"), cbType);
            form.addRow(3, boldLabel("Marks:"),     fMarks);
            form.addRow(4, new Label(""),           btnSave);
            form.addRow(5, new Label(""),           lblStatus);
            contentPane.getChildren().add(form);

            contentPane.getChildren().add(title("📋 Marks Summary"));
            ComboBox<Course> cbSummary = new ComboBox<>(
                    FXCollections.observableArrayList(courses));
            cbSummary.setPromptText("Select course to view marks");
            Button btnView = new Button("View Marks");
            btnView.getStyleClass().add("btn-outline");

            TableView<Object[]> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.getColumns().addAll(
                    colArr("Reg No",0), colArr("Name",1),
                    colArr("Exam Type",2), colArr("Marks",3));

            btnView.setOnAction(e -> {
                Course sel = cbSummary.getValue();
                if (sel == null) return;
                try {
                    table.setItems(FXCollections.observableArrayList(
                            marksDAO.getBatchSummary(sel.getCourseId())));
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            contentPane.getChildren().addAll(
                    new HBox(10, cbSummary, btnView), table);

        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }

    // ── ATTENDANCE VIEW ────────────────────────────────────
    @FXML
    public void showAttendanceView() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("📋 Attendance Records"));

        try {
            List<Course> courses = courseDAO.getCoursesByDepartments(getAllowedDepartments());

            ComboBox<Course> cbCourse = new ComboBox<>(
                    FXCollections.observableArrayList(courses));
            cbCourse.setPromptText("Select Course");

            ComboBox<String> cbType = new ComboBox<>(
                    FXCollections.observableArrayList("ALL","THEORY","PRACTICAL"));
            cbType.setValue("ALL");

            Button btnLoad = new Button("🔍 Load");
            btnLoad.getStyleClass().add("btn-primary");

            Label lblSummary = new Label();
            lblSummary.setStyle("-fx-font-size:13;-fx-font-weight:bold;");

            TableView<Object[]> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.getColumns().addAll(
                    colArr("Reg No",0), colArr("Name",1),
                    colArr("Present",2), colArr("Total",3),
                    colArr("Percent",4), colArr("Eligibility",5));

            btnLoad.setOnAction(e -> {
                Course c = cbCourse.getValue();
                if (c == null) return;
                try {
                    table.setItems(FXCollections.observableArrayList(
                            attendanceDAO.getBatchSummary(c.getCourseId())));
                    lblSummary.setText("Attendance for: " + c.getCourseName());
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            HBox toolbar = new HBox(12,
                    boldLabel("Course:"), cbCourse,
                    boldLabel("Type:"), cbType, btnLoad);
            toolbar.setAlignment(Pos.CENTER_LEFT);
            contentPane.getChildren().addAll(toolbar, lblSummary, table);

        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }

    // ── ELIGIBILITY ────────────────────────────────────────
    @FXML
    public void showEligibility() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("✅ Eligibility Check"));

        try {
            List<Course>        courses  = courseDAO.getCoursesByDepartments(getAllowedDepartments());
            List<Undergraduate> students = userDAO.getAllUndergraduates();

            ComboBox<Course> cbCourse = new ComboBox<>(
                    FXCollections.observableArrayList(courses));
            cbCourse.setPromptText("Select Course");
            Button btnCheck = new Button("Check Eligibility");
            btnCheck.getStyleClass().add("btn-primary");

            TableView<Object[]> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.getColumns().addAll(
                    colArr("Reg No",0), colArr("Name",1),
                    colArr("Attendance%",2), colArr("CA Avg",3),
                    colArr("Att. OK",4), colArr("CA OK",5),
                    colArr("Overall",6));

            TableColumn<Object[],String> colOverall =
                    (TableColumn<Object[],String>) table.getColumns().get(6);
            colOverall.setCellFactory(tc -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setStyle(""); return; }
                    setText(item);
                    setStyle(item.contains("ELIGIBLE") && !item.contains("NOT")
                            ? "-fx-text-fill:#155724;-fx-font-weight:bold;"
                            : "-fx-text-fill:#721c24;-fx-font-weight:bold;");
                }
            });

            btnCheck.setOnAction(e -> {
                Course course = cbCourse.getValue();
                if (course == null) return;
                try {
                    List<Object[]> rows = new java.util.ArrayList<>();
                    for (Undergraduate ug : students) {
                        double attPct = attendanceDAO.getPercent(
                                ug.getUgId(), course.getCourseId(), "ALL");
                        double caAvg  = marksDAO.getCAAverage(
                                ug.getUgId(), course.getCourseId());
                        boolean attOk = GradeCalculator.isAttendanceEligible(attPct);
                        boolean caOk  = GradeCalculator.isCAEligible(caAvg);
                        rows.add(new Object[]{
                                ug.getRegNumber(), ug.getFullName(),
                                String.format("%.1f%%", attPct),
                                String.format("%.1f", caAvg),
                                attOk ? "✅" : "❌",
                                caOk  ? "✅" : "❌",
                                (attOk && caOk) ? "✅ ELIGIBLE" : "❌ NOT ELIGIBLE"
                        });
                    }
                    table.setItems(FXCollections.observableArrayList(rows));
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            contentPane.getChildren().addAll(
                    new HBox(10, boldLabel("Course:"), cbCourse, btnCheck), table);

        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }

    // ── GRADES & GPA ───────────────────────────────────────
    @FXML
    public void showGrades() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("🎓 Grades & GPA"));

        try {
            List<Course>        courses  = courseDAO.getCoursesByDepartments(getAllowedDepartments())
                    ;
            List<Undergraduate> students = userDAO.getAllUndergraduates();

            ComboBox<Course> cbCourse = new ComboBox<>(
                    FXCollections.observableArrayList(courses));
            cbCourse.setPromptText("Select Course");
            Button btnLoad = new Button("Load Grades");
            btnLoad.getStyleClass().add("btn-primary");

            TableView<Object[]> courseTable = new TableView<>();
            courseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            courseTable.getColumns().addAll(
                    colArr("Reg No",0), colArr("Name",1),
                    colArr("Final Mark",2), colArr("Grade",3),
                    colArr("Grade Points",4), colArr("CA Eligible",5));

            btnLoad.setOnAction(e -> {
                Course c = cbCourse.getValue();
                if (c == null) return;
                try {
                    List<Object[]> rows = new java.util.ArrayList<>();
                    for (Undergraduate ug : students) {
                        List<Mark> marks = marksDAO.getForStudent(ug.getUgId());
                        double finalMark = marks.stream()
                                .filter(m -> m.getCourseId() == c.getCourseId()
                                        && m.getExamType().equals("FINAL"))
                                .mapToDouble(Mark::getMarksValue)
                                .average().orElse(0.0);
                        double caAvg = marksDAO.getCAAverage(
                                ug.getUgId(), c.getCourseId());
                        String grade = GradeCalculator.getGrade(finalMark);
                        double gp    = GradeCalculator.getGradePoint(grade);
                        rows.add(new Object[]{
                                ug.getRegNumber(), ug.getFullName(),
                                String.format("%.1f", finalMark),
                                grade, String.format("%.1f", gp),
                                GradeCalculator.isCAEligible(caAvg) ? "✅" : "❌"
                        });
                    }
                    courseTable.setItems(FXCollections.observableArrayList(rows));
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            contentPane.getChildren().add(title("📊 Course Grade Summary"));
            contentPane.getChildren().addAll(
                    new HBox(10, boldLabel("Course:"), cbCourse, btnLoad),
                    courseTable);

            contentPane.getChildren().add(title("📊 SGPA — All Students"));
            Button btnSGPA = new Button("Calculate SGPA for All");
            btnSGPA.getStyleClass().add("btn-primary");

            TableView<Object[]> sgpaTable = new TableView<>();
            sgpaTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            sgpaTable.getColumns().addAll(
                    colArr("Reg No",0), colArr("Name",1),
                    colArr("SGPA",2), colArr("Class",3));

            btnSGPA.setOnAction(e -> {
                try {
                    List<Object[]> rows = new java.util.ArrayList<>();
                    for (Undergraduate ug : students) {
                        double[] gps = new double[courses.size()];
                        int[]    cr  = new int[courses.size()];
                        for (int i = 0; i < courses.size(); i++) {
                            Course c = courses.get(i);
                            List<Mark> marks = marksDAO.getForStudent(ug.getUgId());
                            double fm = marks.stream()
                                    .filter(m -> m.getCourseId() == c.getCourseId()
                                            && m.getExamType().equals("FINAL"))
                                    .mapToDouble(Mark::getMarksValue)
                                    .average().orElse(0.0);
                            gps[i] = GradeCalculator.getGradePoint(
                                    GradeCalculator.getGrade(fm));
                            cr[i]  = c.getCredits();
                        }
                        double sgpa = GradeCalculator.calculateSGPA(gps, cr);
                        String cls  = sgpa >= 3.7 ? "🏆 First Class"
                                : sgpa >= 3.3 ? "Second Upper"
                                  : sgpa >= 3.0 ? "Second Lower"
                                    : sgpa >= 2.0 ? "Pass" : "Below Pass";
                        rows.add(new Object[]{
                                ug.getRegNumber(), ug.getFullName(),
                                String.format("%.2f", sgpa), cls
                        });
                    }
                    sgpaTable.setItems(FXCollections.observableArrayList(rows));
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            contentPane.getChildren().addAll(btnSGPA, sgpaTable);

        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }

    // ── MEDICAL RECORDS ────────────────────────────────────
    @FXML
    public void showMedicalRecords() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("🏥 Medical Records"));

        try {
            List<Medical>       allMedicals = medicalDAO.getAll();
            List<Undergraduate> students    = userDAO.getAllUndergraduates();

            long approved = allMedicals.stream().filter(Medical::isApproved).count();
            long pending  = allMedicals.stream().filter(m -> !m.isApproved()).count();

            HBox summaryRow = new HBox(16);
            summaryRow.getChildren().addAll(
                    statCard("Total",    String.valueOf(allMedicals.size()), "#1a73e8"),
                    statCard("Approved", String.valueOf(approved),           "#28a745"),
                    statCard("Pending",  String.valueOf(pending),            "#fd7e14")
            );
            contentPane.getChildren().add(summaryRow);

            ComboBox<Undergraduate> cbStudent = new ComboBox<>(
                    FXCollections.observableArrayList(students));
            cbStudent.setPromptText("All Students");
            Button btnLoad = new Button("View Records");
            btnLoad.getStyleClass().add("btn-outline");

            TableView<Medical> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            TableColumn<Medical, String> docCol = new TableColumn<>("Document");
            docCol.setCellFactory(tc -> new TableCell<>() {
                private final Button btnOpen = new Button("Open PDF");

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || getIndex() >= getTableView().getItems().size()) {
                        setGraphic(null);
                        return;
                    }

                    Medical m = getTableView().getItems().get(getIndex());

                    if (m.getDocPath() == null || m.getDocPath().isBlank()) {
                        setGraphic(null);
                        return;
                    }

                    btnOpen.setOnAction(e -> {
                        try {
                            File file = new File(m.getDocPath());
                            if (file.exists()) {
                                Desktop.getDesktop().open(file);
                            } else {
                                new Alert(Alert.AlertType.ERROR, "PDF file not found.").showAndWait();
                            }
                        } catch (Exception ex) {
                            new Alert(Alert.AlertType.ERROR, "Cannot open PDF: " + ex.getMessage()).showAndWait();
                        }
                    });

                    setGraphic(btnOpen);
                }
            });
            table.getColumns().addAll(
                    col("Student",  m -> m.getStudentName() != null
                            ? m.getStudentName() : "—"),
                    col("From",     m -> m.getFromDate().toString()),
                    col("To",       m -> m.getToDate().toString()),
                    col("Reason",   m -> m.getReason()  != null ? m.getReason()  : "—"),
                    docCol,
                    col("Status",   m -> m.isApproved()
                            ? "✅ Approved" : "⏳ Pending")
            );
            table.setItems(FXCollections.observableArrayList(allMedicals));

            btnLoad.setOnAction(e -> {
                Undergraduate sel = cbStudent.getValue();
                try {
                    if (sel == null) {
                        table.setItems(FXCollections.observableArrayList(
                                medicalDAO.getAll()));
                    } else {
                        table.setItems(FXCollections.observableArrayList(
                                medicalDAO.getForStudent(sel.getUgId())));
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            contentPane.getChildren().addAll(
                    new HBox(10, boldLabel("Student:"), cbStudent, btnLoad), table);

        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }

    // ── NOTICES ────────────────────────────────────────────
    @FXML
    public void showNotices() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("📢 Notices"));

        TextField fTitle = new TextField();
        fTitle.setPromptText("Notice title");

        TextArea fContent = new TextArea();
        fContent.setPromptText("Notice content");
        fContent.setPrefRowCount(3);

        Button btnAdd = new Button("Post Notice");
        btnAdd.getStyleClass().add("btn-primary");

        btnAdd.setOnAction(e -> {
            try {
                if (fTitle.getText() == null || fTitle.getText().trim().isEmpty()) {
                    showError("Notice title cannot be empty.");
                    fTitle.requestFocus();
                    return;
                }

                if (fContent.getText() == null || fContent.getText().trim().isEmpty()) {
                    showError("Notice content cannot be empty.");
                    fContent.requestFocus();
                    return;
                }

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
                showNotices();

            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        VBox form = new VBox(8, fTitle, fContent, btnAdd);
        form.getStyleClass().add("card");
        contentPane.getChildren().add(form);

        try {
            List<Notice> notices = noticeDAO.getAll();

            if (notices.isEmpty()) {
                Label empty = new Label("No notices available.");
                empty.setStyle("-fx-font-size:14;-fx-text-fill:#6c757d;");
                contentPane.getChildren().add(empty);
                return;
            }

            for (Notice n : notices) {
                VBox card = new VBox(8);
                card.setStyle(
                        "-fx-background-color:white;-fx-background-radius:10;" +
                                "-fx-padding:16;-fx-border-color:#1a73e8;" +
                                "-fx-border-width:0 0 0 4;" +
                                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);"
                );

                String titleText = (n.getTitle() != null && !n.getTitle().isBlank())
                        ? n.getTitle() : "(No Title)";

                String bodyText = (n.getContent() != null && !n.getContent().isBlank())
                        ? n.getContent() : "(No content)";

                String byName = (n.getCreatedByName() != null)
                        ? n.getCreatedByName() : "Unknown";

                String dateText = (n.getCreatedAt() != null)
                        ? n.getCreatedAt().toLocalDate().toString() : "No date";

                Label t = new Label("📌 " + titleText);
                t.setStyle("-fx-font-size:15;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");
                t.setWrapText(true);

                Label meta = new Label("👤 " + byName + "   📅 " + dateText);
                meta.setStyle("-fx-font-size:11;-fx-text-fill:#6c757d;");

                Label body = new Label(bodyText);
                body.setWrapText(true);
                body.setStyle("-fx-font-size:13;-fx-text-fill:#3c4043;");

                card.getChildren().addAll(t, meta, new Separator(), body);

                // show delete button only for own notices
                if (n.getCreatedBy() == user.getUserId()) {
                    Button btnDelete = new Button("Delete My Notice");
                    btnDelete.getStyleClass().add("btn-danger");

                    btnDelete.setOnAction(e -> {
                        try {
                            boolean deleted = noticeDAO.deleteNoticeByOwner(
                                    n.getNoticeId(),
                                    user.getUserId()
                            );

                            if (!deleted) {
                                showError("You can delete only your own notices.");
                                return;
                            }

                            showNotices();

                        } catch (Exception ex) {
                            showError(ex.getMessage());
                        }
                    });

                    card.getChildren().add(btnDelete);
                }

                contentPane.getChildren().add(card);
            }

        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }

    // ── LOGOUT ─────────────────────────────────────────────
    @FXML
    public void handleLogout() {
        try { MainApp.showLogin(); } catch (Exception e) { e.printStackTrace(); }
    }

    // ── HELPERS ────────────────────────────────────────────


    private Label title(String t) {
        Label l = new Label(t);
        l.getStyleClass().add("page-title");
        return l;
    }

    // Profile row — label left, value right
    private HBox profileRow(String label, String value) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding:12 16;" +
                "-fx-border-color:#f0f2f5;-fx-border-width:0 0 1 0;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight:bold;-fx-text-fill:#5f6368;" +
                "-fx-font-size:13;-fx-min-width:160;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill:#202124;-fx-font-size:13;");
        row.getChildren().addAll(lbl, val);
        return row;
    }

    private Label boldLabel(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-weight:bold;-fx-text-fill:#5f6368;-fx-min-width:90;");
        return l;
    }

    private VBox statCard(String label, String value, String color) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        Label num = new Label(value);
        num.getStyleClass().add("stat-number");
        num.setStyle("-fx-text-fill:" + color + ";");
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");
        card.getChildren().addAll(num, lbl);
        return card;
    }

    private <T> TableColumn<T,String> col(String h,
                                          java.util.function.Function<T,String> fn) {
        TableColumn<T,String> c = new TableColumn<>(h);
        c.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue() == null ? "" : fn.apply(d.getValue())));
        return c;
    }

    private TableColumn<Object[],String> colArr(String h, int idx) {
        TableColumn<Object[],String> c = new TableColumn<>(h);
        c.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue()[idx])));
        return c;
    }

    // Returns the departments this lecturer is allowed to see
// Own department + Multidisciplinary Studies
// Exception: Multidisciplinary lecturers see only Multidisciplinary
//    private List<String> getAllowedDepartments() {
//        List<String> depts = new java.util.ArrayList<>();
//        if (user.getDepartment() != null) {
//            depts.add(user.getDepartment());
//        }
//        // Add Multidisciplinary only if lecturer is NOT already in it
//        if (!"Multidisciplinary Studies".equals(user.getDepartment())) {
//            depts.add("Multidisciplinary Studies");
//        }
//        return depts;
//    }
    // Lecturer sees: own department + Multidisciplinary Studies
    private List<String> getAllowedDepartments() {
        List<String> depts = new java.util.ArrayList<>();
        String myDept = user.getDepartment();
        if (myDept != null) depts.add(myDept);
        if (!"Multidisciplinary Studies".equals(myDept)) {
            depts.add("Multidisciplinary Studies");
        }
        return depts;
    }
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.showAndWait();
    }
}