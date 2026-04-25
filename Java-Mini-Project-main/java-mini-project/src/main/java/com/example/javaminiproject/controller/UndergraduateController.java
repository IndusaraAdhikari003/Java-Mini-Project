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

import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.nio.file.Paths;

import java.awt.Desktop;


public class UndergraduateController {

    @FXML private VBox  contentPane;
    @FXML private Label labelUserName;
    @FXML private Label labelReg;

    private Undergraduate ug;

    private final CourseDAO     courseDAO     = new CourseDAO();
    private final MarksDAO      marksDAO      = new MarksDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final MedicalDAO    medicalDAO    = new MedicalDAO();
    private final NoticeDAO     noticeDAO     = new NoticeDAO();
    private final TimetableDAO  timetableDAO  = new TimetableDAO();
    private final UserDAO       userDAO       = new UserDAO();
    private final CourseMaterialDAO courseMaterialDAO = new CourseMaterialDAO();

    public void setUser(User u) { this.ug = (Undergraduate) u; }

    @FXML
    public void initialize() {
        if (ug == null) return;
        if (labelUserName != null) labelUserName.setText(ug.getFullName());
        if (labelReg      != null) labelReg.setText(ug.getRegNumber());
        showDashboard();
    }

    // ── DEPARTMENT FILTER ──────────────────────────────────
    // Student sees own department + Multidisciplinary Studies
    private List<String> getAllowedDepartments() {
        List<String> depts = new ArrayList<>();
        String myDept = ug.getDepartment();
        if (myDept != null) depts.add(myDept);
        if (!"Multidisciplinary Studies".equals(myDept)) {
            depts.add("Multidisciplinary Studies");
        }
        return depts;
    }

    // ── DASHBOARD ──────────────────────────────────────────
    @FXML
    public void showDashboard() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("🏠 Welcome, " + ug.getFullName()));

        VBox profileCard = new VBox(10);
        profileCard.getStyleClass().add("card");
        profileCard.getChildren().add(createProfileImageView(ug.getProfilePic()));
        profileCard.getChildren().addAll(
                sectionLabel("Student Information"),
                infoRow("📋 Reg Number",   ug.getRegNumber()),
                infoRow("🏛 Department",   ug.getDepartment()),
                infoRow("🎓 Batch",        ug.getBatch()),
                infoRow("📧 Email",        ug.getEmail()),
                infoRow("📱 Phone",        ug.getPhone()),
                infoRow("🔁 Repeat",       ug.isRepeat()      ? "Yes" : "No"),
                infoRow("📅 Batch Missed", ug.isBatchMissed() ? "Yes" : "No")
        );

        HBox statsRow = new HBox(16);
        try {
            List<Course> courses = courseDAO.getCoursesByDepartments(
                    getAllowedDepartments());
            List<Mark>   marks   = marksDAO.getForStudent(ug.getUgId());
            long graded = marks.stream()
                    .filter(m -> m.getExamType().equals("FINAL")).count();

            VBox cCourses = clickableCard("📚 Courses",    String.valueOf(courses.size()), "#1a73e8", this::showMyCourses);
            VBox cGraded  = clickableCard("📝 Graded",     String.valueOf(graded),         "#28a745", this::showMarks);
            VBox cBatch   = clickableCard("🎓 Batch",      ug.getBatch(),                  "#fd7e14", null);
            statsRow.getChildren().addAll(cCourses, cGraded, cBatch);
        } catch (Exception e) {
            statsRow.getChildren().add(new Label("Error: " + e.getMessage()));
        }
        contentPane.getChildren().addAll(statsRow, profileCard);

        // Recent notices
        try {
            contentPane.getChildren().add(title("📢 Latest Notices"));
            VBox noticeBox = new VBox(8);
            noticeBox.getStyleClass().add("card");
            List<Notice> notices = noticeDAO.getAll();
            if (notices.isEmpty()) {
                noticeBox.getChildren().add(new Label("No notices available."));
            } else {
                notices.stream().limit(3).forEach(n -> {
                    VBox item = new VBox(4);
                    String t2 = n.getTitle()   != null && !n.getTitle().isBlank()
                            ? n.getTitle()   : "(No Title)";
                    String c2 = n.getContent() != null && !n.getContent().isBlank()
                            ? n.getContent() : "(No content)";
                    Label t = new Label("📌 " + t2);
                    t.setStyle("-fx-font-weight:bold;-fx-font-size:13;");
                    Label body = new Label(c2);
                    body.setWrapText(true);
                    body.setStyle("-fx-font-size:12;-fx-text-fill:#3c4043;");
                    Label d = new Label("📅 " + n.getCreatedAt().toLocalDate()
                            + "  |  By: " + (n.getCreatedByName() != null
                            ? n.getCreatedByName() : "Admin"));
                    d.setStyle("-fx-font-size:11;-fx-text-fill:#6c757d;");
                    item.getChildren().addAll(t, body, d);
                    noticeBox.getChildren().addAll(item, new Separator());
                });
            }
            contentPane.getChildren().add(noticeBox);
        } catch (Exception ignored) {}
    }

    // ── MY COURSES ─────────────────────────────────────────
    public void showMyCourses() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("📚 My Courses"));
        try {
            List<Course> courses = courseDAO.getCoursesByDepartments(
                    getAllowedDepartments());
            if (courses.isEmpty()) {
                contentPane.getChildren().add(new Label("No courses found."));
                return;
            }
            for (Course c : courses) {
                VBox card = new VBox(10);
                card.setStyle(
                        "-fx-background-color:white;-fx-background-radius:10;-fx-padding:16;" +
                                "-fx-border-color:" + (c.isHasPractical() ? "#28a745" : "#1a73e8") + ";" +
                                "-fx-border-width:0 0 0 5;" +
                                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);");

                HBox titleRow = new HBox(12);
                titleRow.setAlignment(Pos.CENTER_LEFT);
                Label codeLabel = new Label(c.getCourseCode());
                codeLabel.setStyle("-fx-background-color:#e8f0fe;-fx-text-fill:#1a73e8;" +
                        "-fx-font-weight:bold;-fx-background-radius:4;-fx-padding:3 8;");
                Label nameLabel = new Label(c.getCourseName());
                nameLabel.setStyle("-fx-font-weight:bold;-fx-font-size:14;");
                Label deptLabel = new Label("🏛 " + c.getDepartment());
                deptLabel.setStyle("-fx-font-size:11;-fx-text-fill:#6c757d;");
                titleRow.getChildren().addAll(codeLabel, nameLabel);

                HBox detailRow = new HBox(20);
                detailRow.getChildren().addAll(
                        detailChip("Credits",    String.valueOf(c.getCredits())),
                        detailChip("Theory",     c.isHasTheory()    ? "Yes" : "No"),
                        detailChip("Practical",  c.isHasPractical() ? "Yes" : "No"),
                        detailChip("Lecturer",   c.getLecturerName() != null
                                ? c.getLecturerName() : "TBA")
                );

                HBox statsRow2 = new HBox(16);
                try {
                    double attPct = attendanceDAO.getPercent(
                            ug.getUgId(), c.getCourseId(), "ALL");
                    double caAvg  = marksDAO.getCAAverage(ug.getUgId(), c.getCourseId());
                    boolean attOk = GradeCalculator.isAttendanceEligible(attPct);
                    boolean caOk  = GradeCalculator.isCAEligible(caAvg);
                    statsRow2.getChildren().addAll(
                            new Label(String.format("📋 Attendance: %.1f%%  %s",
                                    attPct, attOk ? "✅" : "❌")),
                            new Label(String.format("📝 CA Avg: %.1f  %s",
                                    caAvg, caOk ? "✅" : "❌"))
                    );
                } catch (Exception ignored) {}

                card.getChildren().addAll(titleRow, deptLabel, detailRow,
                        new Separator(), statsRow2);
                contentPane.getChildren().add(card);
            }
        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }

    @FXML
    public void showCourseMaterials() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("📚 Course Materials"));

        try {
            List<Course> courses = courseDAO.getCoursesByDepartments(getAllowedDepartments());

            ComboBox<Course> cbCourse = new ComboBox<>(FXCollections.observableArrayList(courses));
            cbCourse.setPromptText("Select Course");

            Button btnLoad = new Button("Load Materials");
            btnLoad.getStyleClass().add("btn-primary");

            TableView<CourseMaterial> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            TableColumn<CourseMaterial, String> colTitle = new TableColumn<>("Title");
            colTitle.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getTitle()));

            TableColumn<CourseMaterial, String> colFile = new TableColumn<>("File");
            colFile.setCellValueFactory(data ->
                    new SimpleStringProperty(new File(data.getValue().getFilePath()).getName()));

            TableColumn<CourseMaterial, String> colPath = new TableColumn<>("Path");
            colPath.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getFilePath()));

            TableColumn<CourseMaterial, String> colAction = new TableColumn<>("Action");
            colAction.setCellFactory(tc -> new TableCell<>() {
                private final Button btnOpen = new Button("Open");

                {
                    btnOpen.setOnAction(e -> {
                        CourseMaterial m = getTableView().getItems().get(getIndex());
                        try {
                            File file = new File(m.getFilePath());
                            if (file.exists()) {
                                Desktop.getDesktop().open(file);
                            } else {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("File Not Found");
                                alert.setHeaderText(null);
                                alert.setContentText("Selected file does not exist.");
                                alert.showAndWait();
                            }
                        } catch (Exception ex) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Open Error");
                            alert.setHeaderText(null);
                            alert.setContentText(ex.getMessage());
                            alert.showAndWait();
                        }
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getIndex() >= getTableView().getItems().size()) {
                        setGraphic(null);
                    } else {
                        setGraphic(btnOpen);
                    }
                }
            });

            table.getColumns().addAll(colTitle, colFile, colPath, colAction);

            Label lblInfo = new Label("Select your subject and load uploaded resources.");
            lblInfo.setStyle("-fx-text-fill:#5f6368; -fx-font-size:13;");

            btnLoad.setOnAction(e -> {
                Course selectedCourse = cbCourse.getValue();
                if (selectedCourse == null) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("No Course Selected");
                    alert.setHeaderText(null);
                    alert.setContentText("Please select a course first.");
                    alert.showAndWait();
                    return;
                }

                try {
                    List<CourseMaterial> materials =
                            courseMaterialDAO.getMaterialsByCourse(selectedCourse.getCourseId());
                    table.setItems(FXCollections.observableArrayList(materials));

                    if (materials.isEmpty()) {
                        lblInfo.setText("No materials uploaded yet for this subject.");
                    } else {
                        lblInfo.setText("Showing materials for: " + selectedCourse.getCourseName());
                    }
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Load Error");
                    alert.setHeaderText(null);
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            });

            HBox topBar = new HBox(10, new Label("Course:"), cbCourse, btnLoad);
            topBar.setAlignment(Pos.CENTER_LEFT);

            VBox box = new VBox(15, lblInfo, topBar, table);
            box.getStyleClass().add("card");

            contentPane.getChildren().add(box);

        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }

    // ── ATTENDANCE ─────────────────────────────────────────
    @FXML
    public void showAttendance() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("📋 My Attendance"));
        try {
            List<Course> courses = courseDAO.getCoursesByDepartments(
                    getAllowedDepartments());
            ComboBox<Course> cbCourse = new ComboBox<>(
                    FXCollections.observableArrayList(courses));
            cbCourse.setPromptText("Select Course");
            ComboBox<String> cbType = new ComboBox<>(
                    FXCollections.observableArrayList("ALL","THEORY","PRACTICAL"));
            cbType.setValue("ALL");
            Button btnLoad = new Button("View");
            btnLoad.getStyleClass().add("btn-primary");

            Label lblPct  = new Label();
            Label lblElig = new Label();
            HBox  sumRow  = new HBox(16);

            TableView<Attendance> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.getColumns().addAll(
                    col("Date",   a -> a.getSessionDate().toString()),
                    col("Type",   a -> a.getSessionType()),
                    col("Status", a -> a.isPresent() ? "✅ Present" : "❌ Absent")
            );

            btnLoad.setOnAction(e -> {
                Course c = cbCourse.getValue();
                if (c == null) return;
                try {
                    String type = cbType.getValue();
                    double pct  = attendanceDAO.getPercent(
                            ug.getUgId(), c.getCourseId(), type);
                    boolean ok  = GradeCalculator.isAttendanceEligible(pct);
                    String color = ok ? "#28a745" : "#dc3545";
                    lblPct.setText(String.format("Attendance: %.1f%%", pct));
                    lblPct.setStyle("-fx-font-size:20;-fx-font-weight:bold;" +
                            "-fx-text-fill:" + color + ";");
                    lblElig.setText(ok ? "✅ Eligible" : "❌ Not eligible (below 80%)");
                    lblElig.setStyle("-fx-font-weight:bold;-fx-text-fill:" + color + ";");

                    sumRow.getChildren().clear();
                    sumRow.getChildren().add(
                            quickCard("Combined", String.format("%.1f%%", pct), "#1a73e8"));
                    if (c.isHasTheory()) {
                        double tp = attendanceDAO.getPercent(
                                ug.getUgId(), c.getCourseId(), "THEORY");
                        sumRow.getChildren().add(
                                quickCard("Theory", String.format("%.1f%%", tp), "#28a745"));
                    }
                    if (c.isHasPractical()) {
                        double pp = attendanceDAO.getPercent(
                                ug.getUgId(), c.getCourseId(), "PRACTICAL");
                        sumRow.getChildren().add(
                                quickCard("Practical", String.format("%.1f%%", pp), "#fd7e14"));
                    }
                    table.setItems(FXCollections.observableArrayList(
                            attendanceDAO.getForStudent(ug.getUgId(), c.getCourseId())));
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            VBox resultBox = new VBox(6, lblPct, lblElig);
            resultBox.getStyleClass().add("card");
            resultBox.setPadding(new Insets(12));

            HBox toolbar = new HBox(12, new Label("Course:"), cbCourse,
                    new Label("Type:"), cbType, btnLoad);
            toolbar.setAlignment(Pos.CENTER_LEFT);
            contentPane.getChildren().addAll(toolbar, resultBox, sumRow, table);
        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }

    // ── MARKS & GPA ────────────────────────────────────────
    @FXML
    public void showMarks() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("🎓 My Grades & GPA"));
        try {
            List<Mark>   allMarks = marksDAO.getForStudent(ug.getUgId());
            List<Course> courses  = courseDAO.getCoursesByDepartments(
                    getAllowedDepartments());

            if (allMarks.isEmpty()) {
                VBox e = new VBox(8); e.getStyleClass().add("card");
                e.getChildren().add(new Label(
                        "No results available yet."));
                contentPane.getChildren().add(e);
                return;
            }

            double[] gps = new double[courses.size()];
            int[]    cr  = new int[courses.size()];

            for (int i = 0; i < courses.size(); i++) {
                Course c = courses.get(i);
                double fm = allMarks.stream()
                        .filter(m -> m.getCourseId() == c.getCourseId()
                                && m.getExamType().equals("FINAL"))
                        .mapToDouble(Mark::getMarksValue).average().orElse(0.0);
                gps[i] = GradeCalculator.getGradePoint(GradeCalculator.getGrade(fm));
                cr[i]  = c.getCredits();
            }
            double sgpa = GradeCalculator.calculateSGPA(gps, cr);

            // SGPA card
            VBox sgpaCard = new VBox(6);
            sgpaCard.setStyle("-fx-background-color:#e8f0fe;-fx-background-radius:12;" +
                    "-fx-padding:20 24;");
            Label sgpaTitle = new Label("📊 Semester GPA (SGPA)");
            sgpaTitle.setStyle("-fx-font-weight:bold;-fx-font-size:14;");
            Label sgpaValue = new Label(String.format("%.2f / 4.00", sgpa));
            sgpaValue.setStyle("-fx-font-size:36;-fx-font-weight:bold;" +
                    "-fx-text-fill:#1a73e8;");
            String cls = sgpa >= 3.7 ? "🏆 First Class"
                    : sgpa >= 3.3 ? "🥈 Second Upper"
                      : sgpa >= 3.0 ? "🥉 Second Lower"
                        : sgpa >= 2.0 ? "✅ Pass" : "❌ Below Pass";
            Label sgpaClass = new Label(cls);
            sgpaClass.setStyle("-fx-font-size:14;-fx-text-fill:#5f6368;-fx-font-weight:bold;");
            sgpaCard.getChildren().addAll(sgpaTitle, sgpaValue, sgpaClass);
            contentPane.getChildren().add(sgpaCard);

            // Grade cards per course
            contentPane.getChildren().add(title("📚 Results by Course"));

            for (Course c : courses) {
                double fm = allMarks.stream()
                        .filter(m -> m.getCourseId() == c.getCourseId()
                                && m.getExamType().equals("FINAL"))
                        .mapToDouble(Mark::getMarksValue).average().orElse(-1);
                double caAvg = marksDAO.getCAAverage(ug.getUgId(), c.getCourseId());
                boolean caOk = GradeCalculator.isCAEligible(caAvg);
                String grade = fm >= 0 ? GradeCalculator.getGrade(fm) : "—";
                double gp    = fm >= 0 ? GradeCalculator.getGradePoint(grade) : 0.0;

                String gradeColor =
                        grade.startsWith("A") ? "#155724" :
                                grade.startsWith("B") ? "#004085" :
                                grade.startsWith("C") ? "#856404" :
                                grade.startsWith("D") ? "#721c24" :
                                grade.equals("E")     ? "#dc3545"  : "#5f6368";

                VBox card = new VBox(10);
                card.setStyle(
                        "-fx-background-color:white;-fx-background-radius:10;-fx-padding:16;" +
                                "-fx-border-color:" + gradeColor + ";" +
                                "-fx-border-width:0 0 0 5;" +
                                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);");

                HBox header = new HBox(12);
                header.setAlignment(Pos.CENTER_LEFT);
                Label codeLabel = new Label(c.getCourseCode());
                codeLabel.setStyle("-fx-background-color:#e8f0fe;-fx-text-fill:#1a73e8;" +
                        "-fx-font-weight:bold;-fx-background-radius:4;-fx-padding:3 8;");
                Label nameLabel = new Label(c.getCourseName());
                nameLabel.setStyle("-fx-font-weight:bold;-fx-font-size:14;");
                HBox.setHgrow(nameLabel, Priority.ALWAYS);

                // Grade badge
                Label gradeBadge = new Label(fm >= 0 ? grade : "Pending");
                gradeBadge.setStyle(
                        "-fx-text-fill:" + gradeColor + ";-fx-font-weight:bold;" +
                                "-fx-font-size:22;-fx-background-radius:8;-fx-padding:6 16;" +
                                "-fx-background-color:rgba(0,0,0,0.05);");
                header.getChildren().addAll(codeLabel, nameLabel, gradeBadge);

                HBox detailRow = new HBox(20);
                detailRow.getChildren().addAll(
                        detailChip("Credits",      String.valueOf(c.getCredits())),
                        detailChip("Grade Points", fm >= 0
                                ? String.format("%.1f", gp) : "—"),
                        detailChip("CA Eligible",  caAvg > 0
                                ? (caOk ? "✅ Yes" : "❌ No") : "—")
                );

                String statusText = fm < 0
                        ? "⏳ Results not yet published"
                        : !caOk
                          ? "❌ Not eligible — CA below 40%"
                          : "✅ Grade: " + grade + "  |  GP: "
                            + String.format("%.1f", gp);
                Label statusLbl = new Label(statusText);
                statusLbl.setStyle("-fx-font-size:12;-fx-text-fill:" + gradeColor + ";" +
                        "-fx-font-weight:bold;");

                card.getChildren().addAll(header, detailRow, new Separator(), statusLbl);
                contentPane.getChildren().add(card);
            }

        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    // ── MEDICALS ───────────────────────────────────────────
    @FXML
    public void showMedicals() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("🏥 My Medical Records"));
        try {
            List<Medical> medList = medicalDAO.getForStudent(ug.getUgId());
            long approved = medList.stream().filter(Medical::isApproved).count();
            long pending  = medList.stream().filter(Medical::isPending).count();
            long rejected = medList.stream().filter(Medical::isRejected).count();

            HBox sumRow = new HBox(16);
            sumRow.getChildren().addAll(
                    quickCard("Total",    String.valueOf(medList.size()), "#1a73e8"),
                    quickCard("✅ Approved", String.valueOf(approved),    "#28a745"),
                    quickCard("⏳ Pending",  String.valueOf(pending),     "#fd7e14"),
                    quickCard("❌ Rejected", String.valueOf(rejected),    "#dc3545")
            );
            contentPane.getChildren().add(sumRow);

            if (medList.isEmpty()) {
                Label l = new Label("No medical records found.");
                l.setStyle("-fx-text-fill:#6c757d;");
                contentPane.getChildren().add(l);
                return;
            }

            for (int i = 0; i < medList.size(); i++) {
                Medical m = medList.get(i);
                boolean isApproved = m.isApproved();
                boolean isRejected = m.isRejected();
                String border = isApproved ? "#28a745"
                        : isRejected ? "#dc3545" : "#fd7e14";
                String badgeText = isApproved ? "✅  APPROVED"
                        : isRejected ? "❌  REJECTED" : "⏳  PENDING";
                String badgeBg   = isApproved ? "rgba(40,167,69,0.15)"
                        : isRejected ? "rgba(220,53,69,0.15)"
                          : "rgba(253,126,20,0.15)";
                String badgeFg   = isApproved ? "#155724"
                        : isRejected ? "#721c24" : "#854d0e";

                VBox card = new VBox(10);
                card.setStyle("-fx-background-color:white;-fx-background-radius:10;" +
                        "-fx-padding:16;-fx-border-color:" + border + ";" +
                        "-fx-border-width:0 0 0 5;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);");

                HBox header = new HBox(12);
                header.setAlignment(Pos.CENTER_LEFT);
                Label recNum = new Label("Medical Record #" + (i + 1));
                recNum.setStyle("-fx-font-weight:bold;-fx-font-size:14;");
                HBox.setHgrow(recNum, Priority.ALWAYS);
                Label badge = new Label(badgeText);
                badge.setStyle("-fx-background-color:" + badgeBg + ";" +
                        "-fx-text-fill:" + badgeFg + ";-fx-font-weight:bold;" +
                        "-fx-background-radius:20;-fx-padding:4 12;-fx-font-size:11;");
                header.getChildren().addAll(recNum, badge);

                long days = java.time.temporal.ChronoUnit.DAYS.between(
                        m.getFromDate(), m.getToDate()) + 1;
                Label info = new Label("📅 " + m.getFromDate() + " → " + m.getToDate()
                        + "   ⏱ " + days + " day(s)"
                        + "   📎 " + (m.getDocPath() != null
                        && !m.getDocPath().isBlank() ? m.getDocPath() : "—"));
                info.setStyle("-fx-font-size:12;-fx-text-fill:#5f6368;");

                Label reason = new Label("📝 " + (m.getReason() != null
                        ? m.getReason() : "—"));
                reason.setWrapText(true);
                reason.setStyle("-fx-font-size:13;-fx-text-fill:#3c4043;");

                card.getChildren().addAll(header, new Separator(), info, reason);
                contentPane.getChildren().add(card);
            }
        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }

    // ── SUBMIT MEDICAL ─────────────────────────────────────
    @FXML
    public void showSubmitMedical() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("📤 Submit Medical"));

        VBox form = new VBox(14);
        form.getStyleClass().add("card");
        form.setMaxWidth(500);

        TextField fMedNum = new TextField();
        fMedNum.setPromptText("e.g MED/2026/001");

        DatePicker dpFrom = new DatePicker(java.time.LocalDate.now());
        DatePicker dpTo   = new DatePicker(java.time.LocalDate.now());

        TextArea fReason = new TextArea();
        fReason.setPromptText("Describe your medical condition...");
        fReason.setPrefRowCount(3);

        TextField fDoc = new TextField();
        fDoc.setPromptText("Select PDF file...");
        fDoc.setEditable(false);

        Button btnBrowse = new Button("Browse PDF");
        final File[] selectedFile = new File[1];

        btnBrowse.setOnAction(ev -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Medical PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );

            File file = fileChooser.showOpenDialog(contentPane.getScene().getWindow());
            if (file != null) {
                selectedFile[0] = file;
                fDoc.setText(file.getName());
            }
        });

        Button btnSubmit = new Button("📤 Submit Medical");
        btnSubmit.getStyleClass().add("btn-primary");

        Label lblStatus = new Label();

        btnSubmit.setOnAction(e -> {
            if (fMedNum.getText().isBlank() || fReason.getText().isBlank()) {
                lblStatus.setText("⚠ Please fill all required fields.");
                lblStatus.setStyle("-fx-text-fill:#fd7e14;-fx-font-weight:bold;");
                return;
            }

            if (dpTo.getValue().isBefore(dpFrom.getValue())) {
                lblStatus.setText("⚠ To date cannot be before From date.");
                lblStatus.setStyle("-fx-text-fill:#dc3545;");
                return;
            }

            if (selectedFile[0] == null) {
                lblStatus.setText("⚠ Please select a PDF document.");
                lblStatus.setStyle("-fx-text-fill:#dc3545;");
                return;
            }

            try {
                Path uploadDir = Path.of("medical_uploads");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                String fileName = System.currentTimeMillis() + "_" + selectedFile[0].getName();
                Path targetPath = uploadDir.resolve(fileName);

                Files.copy(selectedFile[0].toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                String savedPath = targetPath.toString();

                Medical m = new Medical(
                        0,
                        ug.getUgId(),
                        dpFrom.getValue(),
                        dpTo.getValue(),
                        "[" + fMedNum.getText().trim() + "] " + fReason.getText().trim(),
                        savedPath,
                        false
                );

                medicalDAO.addMedical(m);

                lblStatus.setText("✅ Submitted! A Technical Officer will review it.");
                lblStatus.setStyle("-fx-text-fill:#28a745;-fx-font-weight:bold;");

                fMedNum.clear();
                fReason.clear();
                fDoc.clear();
                selectedFile[0] = null;

            } catch (Exception ex) {
                lblStatus.setText("❌ Error: " + ex.getMessage());
                lblStatus.setStyle("-fx-text-fill:#dc3545;");
            }
        });

        form.getChildren().addAll(
                new Label("Medical Reference No:"), fMedNum,
                new Label("From Date:"), dpFrom,
                new Label("To Date:"), dpTo,
                new Label("Reason:"), fReason,
                new Label("PDF Document:"), new HBox(10, fDoc, btnBrowse),
                btnSubmit,
                lblStatus
        );

        contentPane.getChildren().add(form);
    }

    // ── NOTICES ────────────────────────────────────────────
    @FXML
    public void showNotices() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("📢 Notices"));
        try {
            List<Notice> notices = noticeDAO.getAll();
            if (notices.isEmpty()) {
                contentPane.getChildren().add(new Label("No notices available."));
                return;
            }
            for (Notice n : notices) {
                VBox card = new VBox(8);
                card.setStyle("-fx-background-color:white;-fx-background-radius:10;" +
                        "-fx-padding:16;-fx-border-color:#1a73e8;-fx-border-width:0 0 0 4;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);");
                String t2 = n.getTitle()   != null && !n.getTitle().isBlank()
                        ? n.getTitle()   : "(No Title)";
                String c2 = n.getContent() != null && !n.getContent().isBlank()
                        ? n.getContent() : "(No content)";
                Label tl = new Label("📌 " + t2);
                tl.setStyle("-fx-font-size:15;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");
                tl.setWrapText(true);
                Label meta = new Label("👤 " + (n.getCreatedByName() != null
                        ? n.getCreatedByName() : "Admin")
                        + "   📅 " + n.getCreatedAt().toLocalDate());
                meta.setStyle("-fx-font-size:11;-fx-text-fill:#6c757d;");
                Label cl = new Label(c2);
                cl.setWrapText(true);
                cl.setStyle("-fx-font-size:13;-fx-text-fill:#3c4043;");
                card.getChildren().addAll(tl, meta, new Separator(), cl);
                contentPane.getChildren().add(card);
            }
        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }

    // ── TIMETABLE ──────────────────────────────────────────
    @FXML
    public void showTimetable() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("🗓 My Timetable"));
        try {
            List<Timetable> all = timetableDAO.getTimetableByDepartments(
                    getAllowedDepartments());

            if (all.isEmpty()) {
                contentPane.getChildren().add(new Label("No timetable available."));
                return;
            }

            TableView<Timetable> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.setPrefHeight(350);
            table.getColumns().addAll(
                    col("Day",      t -> t.getDayOfWeek()),
                    col("Code",     t -> t.getCourseCode()),
                    col("Course",   t -> t.getCourseName()),
                    col("Type",     t -> t.getSessionType()),
                    col("Start",    t -> t.getStartTime().toString()),
                    col("End",      t -> t.getEndTime().toString()),
                    col("Location", t -> t.getLocation() != null ? t.getLocation() : "TBA")
            );
            table.setItems(FXCollections.observableArrayList(all));
            contentPane.getChildren().add(table);

            // Day cards
            contentPane.getChildren().add(title("📅 Day-by-Day View"));
            String[] days     = {"MON","TUE","WED","THU","FRI","SAT"};
            String[] dayNames = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
            for (int d = 0; d < days.length; d++) {
                final String day = days[d];
                final String dayName = dayNames[d];
                List<Timetable> slots = all.stream()
                        .filter(t -> t.getDayOfWeek().equals(day)).toList();
                if (slots.isEmpty()) continue;

                Label dayLbl = new Label("📅 " + dayName);
                dayLbl.setStyle("-fx-font-weight:bold;-fx-font-size:14;" +
                        "-fx-text-fill:#1a73e8;-fx-padding:8 0 4 0;");
                contentPane.getChildren().add(dayLbl);

                for (Timetable t : slots) {
                    HBox row = new HBox(12);
                    row.setStyle("-fx-background-color:white;-fx-background-radius:8;" +
                            "-fx-padding:12 16;" +
                            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");
                    row.setAlignment(Pos.CENTER_LEFT);

                    VBox timeBadge = new VBox(2);
                    timeBadge.setMinWidth(110);
                    timeBadge.setStyle("-fx-background-color:" +
                            (t.getSessionType().equals("THEORY") ? "#e8f0fe" : "#e6f4ea") +
                            ";-fx-background-radius:6;-fx-padding:6 10;");
                    Label tl = new Label(t.getStartTime() + " - " + t.getEndTime());
                    tl.setStyle("-fx-font-size:12;-fx-font-weight:bold;" +
                            "-fx-text-fill:#1a73e8;");
                    timeBadge.getChildren().add(tl);

                    VBox info = new VBox(3);
                    HBox.setHgrow(info, Priority.ALWAYS);
                    Label cn = new Label(t.getCourseName());
                    cn.setStyle("-fx-font-weight:bold;-fx-font-size:13;");
                    Label cc = new Label(t.getCourseCode() + "  ·  " + t.getSessionType());
                    cc.setStyle("-fx-font-size:11;-fx-text-fill:#6c757d;");
                    info.getChildren().addAll(cn, cc);

                    Label loc = new Label("📍 " + (t.getLocation() != null
                            ? t.getLocation() : "TBA"));
                    loc.setStyle("-fx-font-size:12;-fx-text-fill:#5f6368;");

                    row.getChildren().addAll(timeBadge, info, loc);
                    VBox.setMargin(row, new Insets(0,0,6,0));
                    contentPane.getChildren().add(row);
                }
            }
        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    // ── UPDATE PROFILE ─────────────────────────────────────
    @FXML
    public void showUpdateProfile() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("✏ Update My Profile"));
        VBox form = new VBox(12);
        form.getStyleClass().add("card");
        form.setMaxWidth(450);
        form.getChildren().addAll(
                sectionLabel("Read-only"),
                infoRow("Reg Number", ug.getRegNumber()),
                infoRow("Username",   ug.getUsername()),
                infoRow("Batch",      ug.getBatch()),
                infoRow("Department", ug.getDepartment()),
                new Separator(),
                sectionLabel("Editable")
        );
        TextField fEmail = new TextField(ug.getEmail());
        TextField fPhone = new TextField(ug.getPhone());
        Button btnPhoto = new Button("Choose Photo");
        File[] selectedPhoto = new File[1];

        btnPhoto.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Profile Photo");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            selectedPhoto[0] = fc.showOpenDialog(contentPane.getScene().getWindow());
        });
        Button btnSave = new Button("💾 Save Changes");
        btnSave.getStyleClass().add("btn-primary");
        Label lblStatus = new Label();
        btnSave.setOnAction(e -> {
            try {
                ug.setEmail(fEmail.getText().trim());
                ug.setPhone(fPhone.getText().trim());
                if (selectedPhoto[0] != null) {
                    String savedPath = saveProfilePhoto(selectedPhoto[0]);
                    ug.setProfilePic(savedPath);
                }
                userDAO.updateProfile(ug);
                lblStatus.setText("✅ Profile updated!");
                lblStatus.setStyle("-fx-text-fill:#28a745;-fx-font-weight:bold;");
            } catch (Exception ex) {
                lblStatus.setText("❌ " + ex.getMessage());
                lblStatus.setStyle("-fx-text-fill:#dc3545;");
            }
        });
        form.getChildren().addAll(
                btnPhoto,
                new Label("Email:"), fEmail,
                new Label("Phone:"), fPhone,
                btnSave, lblStatus);
        contentPane.getChildren().add(form);
    }

    // ── LOGOUT ─────────────────────────────────────────────
    @FXML
    public void handleLogout() {
        try { MainApp.showLogin(); } catch (Exception e) { e.printStackTrace(); }
    }

    // ── HELPERS ────────────────────────────────────────────
    private Label title(String t) {
        Label l = new Label(t); l.getStyleClass().add("page-title"); return l; }

    private Label sectionLabel(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-weight:bold;-fx-font-size:13;-fx-text-fill:#5f6368;");
        return l;
    }

    private HBox infoRow(String label, String value) {
        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-font-weight:bold;-fx-min-width:140;-fx-text-fill:#5f6368;");
        Label val = new Label(value != null ? value : "—");
        val.setStyle("-fx-text-fill:#202124;");
        HBox row = new HBox(10, lbl, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox quickCard(String label, String value, String color) {
        VBox card = new VBox(4); card.getStyleClass().add("stat-card");
        Label num = new Label(value);
        num.getStyleClass().add("stat-number");
        num.setStyle("-fx-text-fill:" + color + ";");
        card.getChildren().addAll(num, new Label(label){{getStyleClass().add("stat-label");}});
        return card;
    }

    private VBox clickableCard(String label, String value, String color, Runnable onClick) {
        VBox card = new VBox(4); card.getStyleClass().add("stat-card");
        Label num = new Label(value);
        num.getStyleClass().add("stat-number");
        num.setStyle("-fx-text-fill:" + color + ";");
        card.getChildren().addAll(num,
                new Label(label){{getStyleClass().add("stat-label");}});
        if (onClick != null) {
            card.setStyle("-fx-cursor:hand;");
            card.setOnMouseEntered(e -> card.setStyle("-fx-cursor:hand;" +
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.18),12,0,0,4);"));
            card.setOnMouseExited(e -> card.setStyle("-fx-cursor:hand;" +
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);"));
            card.setOnMouseClicked(e -> onClick.run());
        }
        return card;
    }

    private HBox detailChip(String label, String value) {
        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-font-size:11;-fx-text-fill:#6c757d;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size:12;-fx-font-weight:bold;");
        HBox chip = new HBox(4, lbl, val);
        chip.setAlignment(Pos.CENTER_LEFT);
        return chip;
    }
    // 👇 PASTE HERE
    private String saveProfilePhoto(File selectedFile) throws IOException {
        Path uploadDir = Path.of("profile_pictures");

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String fileName = "user_" + ug.getUserId() + "_" + selectedFile.getName();
        Path targetPath = uploadDir.resolve(fileName);

        Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toString();
    }

    private ImageView createProfileImageView(String path) {
        ImageView img = new ImageView();

        try {
            if (path != null && !path.isBlank()) {
                File file = new File(path);
                if (file.exists()) {
                    img.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception e) {}

        img.setFitWidth(120);
        img.setFitHeight(120);

        return img;
    }

    // 👇 THIS ALREADY EXISTS (DO NOT TOUCH)
    private <T> TableColumn<T,String> col(String h,
                                          java.util.function.Function<T,String> fn) {

        TableColumn<T,String> c = new TableColumn<>(h);
        c.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue() == null ? "" : fn.apply(d.getValue())));
        return c;
    }
}