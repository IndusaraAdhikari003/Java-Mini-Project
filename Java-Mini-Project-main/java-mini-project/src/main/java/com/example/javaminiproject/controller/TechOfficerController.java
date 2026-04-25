package com.example.javaminiproject.controller;

import com.example.javaminiproject.MainApp;
import com.example.javaminiproject.dao.*;
import com.example.javaminiproject.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;

import java.awt.Desktop;
import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class TechOfficerController {

    @FXML private VBox  contentPane;
    @FXML private Label labelUserName;

    private User user;

    private final UserDAO       userDAO       = new UserDAO();
    private final CourseDAO     courseDAO     = new CourseDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final MedicalDAO    medicalDAO    = new MedicalDAO();
    private final NoticeDAO     noticeDAO     = new NoticeDAO();
    private final TimetableDAO  timetableDAO  = new TimetableDAO();

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
        contentPane.getChildren().add(title("📊 Technical Officer Dashboard"));

        try {
            List<Medical> allMedicals = medicalDAO.getAllByDepartments(getAllowedDepartments());
            long pending  = allMedicals.stream().filter(Medical::isPending).count();
            long approved = allMedicals.stream().filter(Medical::isApproved).count();
            long rejected = allMedicals.stream().filter(Medical::isRejected).count();
            int  students = userDAO.getAllUndergraduatesByDepartments(getAllowedDepartments()).size();

            HBox statsRow = new HBox(16);
            statsRow.getChildren().addAll(
                    statCard("👥 Students",   String.valueOf(students),          "#1a73e8"),
                    statCard("⏳ Pending",     String.valueOf(pending),           "#fd7e14"),
                    statCard("✅ Approved",    String.valueOf(approved),          "#28a745"),
                    statCard("❌ Rejected",    String.valueOf(rejected),          "#dc3545")
            );
            contentPane.getChildren().add(statsRow);

            contentPane.getChildren().add(sectionTitle("👤 My Profile"));

            VBox profileCard = new VBox(0);
            profileCard.getStyleClass().add("card");
            profileCard.getChildren().add(createProfileImageView(user.getProfilePic()));

            Button btnUpdate = new Button("Update Profile");
            btnUpdate.getStyleClass().add("btn-primary");
            btnUpdate.setOnAction(e -> showUpdateProfile());

            profileCard.getChildren().addAll(
                    btnUpdate,
                    profileRow("👤 Name", user.getFullName()),
                    profileRow("🏛 Department", user.getDepartment()),
                    profileRow("📧 Email", user.getEmail()),
                    profileRow("📱 Phone", user.getPhone()),
                    profileRow("🎓 Role", "Technical Officer")
            );

            contentPane.getChildren().add(profileCard);

            if (pending > 0) {
                VBox alertBox = new VBox(8);
                alertBox.setStyle(
                        "-fx-background-color:#fff3cd;-fx-border-color:#ffc107;" +
                                "-fx-border-width:0 0 0 5;-fx-background-radius:8;-fx-padding:14 16;");
                Label alertTitle = new Label("⚠️  " + pending +
                        " medical record(s) waiting for your review!");
                alertTitle.setStyle("-fx-font-weight:bold;-fx-font-size:14;" +
                        "-fx-text-fill:#856404;");
                Button btnGoReview = new Button("→ Review Now");
                btnGoReview.setStyle(
                        "-fx-background-color:#ffc107;-fx-text-fill:#212529;" +
                                "-fx-font-weight:bold;-fx-background-radius:6;" +
                                "-fx-padding:6 16;-fx-cursor:hand;");
                btnGoReview.setOnAction(e -> showMedicals());
                alertBox.getChildren().addAll(alertTitle, btnGoReview);
                contentPane.getChildren().add(alertBox);
            }

            contentPane.getChildren().add(sectionTitle("📬 Recent Pending Medicals"));
            List<Medical> pendingList = allMedicals.stream()
                    .filter(Medical::isPending).limit(5).toList();

            if (pendingList.isEmpty()) {
                Label noMed = new Label("✅ No pending medicals.");
                noMed.setStyle("-fx-text-fill:#28a745;-fx-font-size:13;");
                contentPane.getChildren().add(noMed);
            } else {
                for (Medical m : pendingList) {
                    HBox row = new HBox(16);
                    row.setStyle(
                            "-fx-background-color:white;-fx-background-radius:8;" +
                                    "-fx-padding:12 16;-fx-border-color:#ffc107;" +
                                    "-fx-border-width:0 0 0 4;" +
                                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");
                    row.setAlignment(Pos.CENTER_LEFT);

                    VBox info = new VBox(3);
                    HBox.setHgrow(info, Priority.ALWAYS);
                    Label sName = new Label("👤 " + m.getStudentName());
                    sName.setStyle("-fx-font-weight:bold;-fx-font-size:13;");
                    Label dates = new Label("📅 " + m.getFromDate() + " → " + m.getToDate());
                    dates.setStyle("-fx-font-size:11;-fx-text-fill:#6c757d;");
                    info.getChildren().addAll(sName, dates);

                    Button btnQA = new Button("✅ Approve");
                    btnQA.setStyle("-fx-background-color:#28a745;-fx-text-fill:white;" +
                            "-fx-background-radius:5;-fx-padding:5 12;" +
                            "-fx-font-size:12;-fx-cursor:hand;");
                    Button btnQR = new Button("❌ Reject");
                    btnQR.setStyle("-fx-background-color:#dc3545;-fx-text-fill:white;" +
                            "-fx-background-radius:5;-fx-padding:5 12;" +
                            "-fx-font-size:12;-fx-cursor:hand;");

                    btnQA.setOnAction(e -> handleApproval(m, true));
                    btnQR.setOnAction(e -> handleApproval(m, false));
                    row.getChildren().addAll(info, btnQA, btnQR);
                    contentPane.getChildren().add(row);
                }
            }

        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private List<String> getAllowedDepartments() {
        List<String> depts = new java.util.ArrayList<>();
        String myDept = user.getDepartment();

        if (myDept != null && !myDept.isBlank()) {
            depts.add(myDept);
        }

        if (!"Multidisciplinary Studies".equals(myDept)) {
            depts.add("Multidisciplinary Studies");
        }

        return depts;
    }
    //---Show update profile ----
    @FXML
    public void showUpdateProfile() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("✏ Update My Profile"));

        VBox form = new VBox(12);
        form.getStyleClass().add("card");
        form.setMaxWidth(450);

        ImageView profileImageView = createProfileImageView(user.getProfilePic());

        Button btnPhoto = new Button("Choose Photo");
        File[] selectedPhoto = new File[1];

        btnPhoto.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Profile Photo");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            selectedPhoto[0] = fc.showOpenDialog(contentPane.getScene().getWindow());

            if (selectedPhoto[0] != null) {
                profileImageView.setImage(new Image(selectedPhoto[0].toURI().toString()));
            }
        });

        TextField fName = new TextField(user.getFullName());
        TextField fEmail = new TextField(user.getEmail());
        TextField fPhone = new TextField(user.getPhone());
        TextField fDept = new TextField(user.getDepartment());

        Button btnSave = new Button("💾 Save Changes");
        btnSave.getStyleClass().add("btn-primary");
        Label lblStatus = new Label();

        btnSave.setOnAction(e -> {
            try {
                user.setFullName(fName.getText().trim());
                user.setEmail(fEmail.getText().trim());
                user.setPhone(fPhone.getText().trim());
                user.setDepartment(fDept.getText().trim());

                if (selectedPhoto[0] != null) {
                    String path = saveProfilePhoto(selectedPhoto[0], user.getUserId());
                    user.setProfilePic(path);
                }

                userDAO.updateProfile(user);

                if (labelUserName != null) {
                    labelUserName.setText(user.getFullName());
                }

                lblStatus.setText("✅ Updated!");
            } catch (Exception ex) {
                lblStatus.setText("❌ Error");
                ex.printStackTrace();
            }
        });

        form.getChildren().addAll(
                profileImageView,
                btnPhoto,
                profileRow("Username", user.getUsername()),
                new Label("Name:"), fName,
                new Label("Email:"), fEmail,
                new Label("Phone:"), fPhone,
                new Label("Department:"), fDept,
                btnSave,
                lblStatus
        );

        contentPane.getChildren().add(form);
    }
    // ── MEDICAL CONTROL ────────────────────────────────────
    @FXML
    public void showMedicals() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("🏥 Medical Control System"));

        try {
            List<Medical> allMedicals = medicalDAO.getAllByDepartments(getAllowedDepartments());
            long pending  = allMedicals.stream().filter(Medical::isPending).count();
            long approved = allMedicals.stream().filter(Medical::isApproved).count();
            long rejected = allMedicals.stream().filter(Medical::isRejected).count();

            // Summary cards
            HBox summaryRow = new HBox(16);
            summaryRow.getChildren().addAll(
                    statCard("📋 Total",    String.valueOf(allMedicals.size()), "#1a73e8"),
                    statCard("⏳ Pending",  String.valueOf(pending),             "#fd7e14"),
                    statCard("✅ Approved", String.valueOf(approved),            "#28a745"),
                    statCard("❌ Rejected", String.valueOf(rejected),            "#dc3545")
            );
            contentPane.getChildren().add(summaryRow);

            // Tab buttons
            HBox tabBar = new HBox(8);
            Button btnAll      = tabBtn("All (" + allMedicals.size() + ")");
            Button btnPending  = tabBtn("⏳ Pending (" + pending + ")");
            Button btnApproved = tabBtn("✅ Approved (" + approved + ")");
            Button btnRejected = tabBtn("❌ Rejected (" + rejected + ")");
            setActiveTab(btnAll);
            tabBar.getChildren().addAll(btnAll, btnPending, btnApproved, btnRejected);
            contentPane.getChildren().add(tabBar);

            // Pending section
            contentPane.getChildren().add(sectionTitle("📬 Pending — Awaiting Decision"));
            VBox pendingSection = new VBox(10);
            buildPendingCards(pendingSection, allMedicals);
            contentPane.getChildren().add(pendingSection);

            // All records table
            contentPane.getChildren().add(sectionTitle("📋 All Medical Records"));
            TableView<Medical> table = buildMedicalTable();
            table.setItems(FXCollections.observableArrayList(allMedicals));
            contentPane.getChildren().add(table);

            // Tab actions
            btnAll.setOnAction(e -> {
                try {
                    List<Medical> all = medicalDAO.getAllByDepartments(getAllowedDepartments());
                    table.setItems(FXCollections.observableArrayList(all));
                    buildPendingCards(pendingSection, all);
                    setActiveTab(btnAll);
                    setInactiveTab(btnPending, btnApproved, btnRejected);
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            btnPending.setOnAction(e -> {
                try {
                    List<Medical> pend = medicalDAO.getAllByDepartments(getAllowedDepartments())
                            .stream()
                            .filter(Medical::isPending)
                            .toList();

                    table.setItems(FXCollections.observableArrayList(pend));
                    buildPendingCards(pendingSection, pend);
                    setActiveTab(btnPending);
                    setInactiveTab(btnAll, btnApproved, btnRejected);
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            btnApproved.setOnAction(e -> {
                try {
                    List<Medical> appr = medicalDAO.getAllByDepartments(getAllowedDepartments())
                            .stream()
                            .filter(Medical::isApproved)
                            .toList();

                    table.setItems(FXCollections.observableArrayList(appr));
                    pendingSection.getChildren().clear();
                    // Show approved cards
                    for (Medical m : appr) {
                        VBox card = buildStatusCard(m, "#28a745", "✅  APPROVED",
                                "rgba(40,167,69,0.15)", "#155724");
                        pendingSection.getChildren().add(card);
                    }
                    if (appr.isEmpty()) {
                        Label l = new Label("No approved records.");
                        l.setStyle("-fx-text-fill:#6c757d;");
                        pendingSection.getChildren().add(l);
                    }
                    setActiveTab(btnApproved);
                    setInactiveTab(btnAll, btnPending, btnRejected);
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            btnRejected.setOnAction(e -> {
                try {
                    List<Medical> rej = medicalDAO.getAllByDepartments(getAllowedDepartments())
                            .stream()
                            .filter(Medical::isRejected)
                            .toList();

                    table.setItems(FXCollections.observableArrayList(rej));
                    pendingSection.getChildren().clear();

                    if (rej.isEmpty()) {
                        Label l = new Label("No rejected records.");
                        l.setStyle("-fx-text-fill:#6c757d;-fx-font-size:13;");
                        pendingSection.getChildren().add(l);
                    } else {
                        for (Medical m : rej) {
                            VBox card = buildStatusCard(m, "#dc3545", "❌  REJECTED",
                                    "rgba(220,53,69,0.15)", "#721c24");

                            // Re-Approve button
                            Button btnReApprove = new Button("↩  Re-Approve");
                            btnReApprove.setStyle(
                                    "-fx-background-color:#28a745;-fx-text-fill:white;" +
                                            "-fx-background-radius:6;-fx-padding:7 16;" +
                                            "-fx-font-size:12;-fx-cursor:hand;");
                            btnReApprove.setOnAction(ev -> {
                                try {
                                    medicalDAO.updateStatus(m.getMedicalId(), "APPROVED");
                                    showMedicals();
                                } catch (Exception ex) { ex.printStackTrace(); }
                            });
                            card.getChildren().add(btnReApprove);
                            pendingSection.getChildren().add(card);
                        }
                    }
                    setActiveTab(btnRejected);
                    setInactiveTab(btnAll, btnPending, btnApproved);
                } catch (Exception ex) { ex.printStackTrace(); }
            });

        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    // Build a status card (reused for approved + rejected)
    private VBox buildStatusCard(Medical m, String borderColor,
                                 String badgeText, String badgeBg, String badgeFg) {
        VBox card = new VBox(8);
        card.setStyle(
                "-fx-background-color:white;-fx-background-radius:10;" +
                        "-fx-padding:16;-fx-border-color:" + borderColor + ";" +
                        "-fx-border-width:0 0 0 5;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label("👤  " + (m.getStudentName() != null
                ? m.getStudentName() : "Unknown"));
        name.setStyle("-fx-font-weight:bold;-fx-font-size:14;-fx-text-fill:#202124;");
        HBox.setHgrow(name, Priority.ALWAYS);
        Label badge = new Label(badgeText);
        badge.setStyle(
                "-fx-background-color:" + badgeBg + ";" +
                        "-fx-text-fill:" + badgeFg + ";-fx-font-weight:bold;" +
                        "-fx-background-radius:20;-fx-padding:4 12;-fx-font-size:11;");
        header.getChildren().addAll(name, badge);

        Label dates = new Label("📅 " + m.getFromDate() + " → " + m.getToDate());
        dates.setStyle("-fx-font-size:12;-fx-text-fill:#5f6368;");

        Button btnOpenPdf = new Button("📄 Open PDF");

        btnOpenPdf.setOnAction(e -> {
            try {
                File file = new File(m.getDocPath());
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                } else {
                    new Alert(Alert.AlertType.ERROR, "PDF not found!").showAndWait();
                }
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Error opening PDF").showAndWait();
            }
        });
        dates.setStyle("-fx-font-size:12;-fx-text-fill:#5f6368;");

        Label reason = new Label("📝 " + (m.getReason() != null
                ? m.getReason() : "—"));
        reason.setWrapText(true);
        reason.setStyle("-fx-font-size:13;-fx-text-fill:#3c4043;");

        card.getChildren().addAll(header, new Separator(), dates, btnOpenPdf, reason);
        return card;
    }

    // Build pending cards
    private void buildPendingCards(VBox container, List<Medical> list) {
        container.getChildren().clear();
        List<Medical> pending = list.stream().filter(Medical::isPending).toList();

        if (pending.isEmpty()) {
            Label none = new Label("✅ No pending medical records.");
            none.setStyle("-fx-text-fill:#28a745;-fx-font-size:13;-fx-padding:8 0;");
            container.getChildren().add(none);
            return;
        }

        for (Medical m : pending) {
            VBox card = new VBox(10);
            card.setStyle(
                    "-fx-background-color:white;-fx-background-radius:10;-fx-padding:16;" +
                            "-fx-border-color:#fd7e14;-fx-border-width:0 0 0 5;" +
                            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");

            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            Label sName = new Label("👤  " + m.getStudentName());
            sName.setStyle("-fx-font-weight:bold;-fx-font-size:14;-fx-text-fill:#202124;");
            HBox.setHgrow(sName, Priority.ALWAYS);
            Label badge = new Label("⏳  PENDING REVIEW");
            badge.setStyle(
                    "-fx-background-color:rgba(253,126,20,0.15);-fx-text-fill:#854d0e;" +
                            "-fx-font-weight:bold;-fx-background-radius:20;" +
                            "-fx-padding:4 12;-fx-font-size:11;");
            header.getChildren().addAll(sName, badge);

            Label dates = new Label("📅 " + m.getFromDate() + " → " + m.getToDate()
                    + "    📎 " + (m.getDocPath() != null
                    && !m.getDocPath().isBlank() ? m.getDocPath() : "No document"));
            dates.setStyle("-fx-font-size:12;-fx-text-fill:#5f6368;");

            Label reason = new Label("📝 " + (m.getReason() != null
                    ? m.getReason() : "No reason"));
            reason.setWrapText(true);
            reason.setStyle("-fx-font-size:13;-fx-text-fill:#3c4043;");

            HBox actions = new HBox(12);
            Button btnApprove = new Button("✅  Approve Medical");
            btnApprove.setStyle(
                    "-fx-background-color:#28a745;-fx-text-fill:white;" +
                            "-fx-font-weight:bold;-fx-background-radius:6;" +
                            "-fx-padding:9 20;-fx-font-size:13;-fx-cursor:hand;");
            Button btnReject = new Button("❌  Reject Medical");
            btnReject.setStyle(
                    "-fx-background-color:#dc3545;-fx-text-fill:white;" +
                            "-fx-font-weight:bold;-fx-background-radius:6;" +
                            "-fx-padding:9 20;-fx-font-size:13;-fx-cursor:hand;");

            btnApprove.setOnAction(e -> handleApproval(m, true));
            btnReject.setOnAction( e -> handleApproval(m, false));
            actions.getChildren().addAll(btnApprove, btnReject);

            card.getChildren().addAll(header, new Separator(), dates, reason,
                    new Separator(), actions);
            container.getChildren().add(card);
        }
    }

    // Handle approve / reject
    private void handleApproval(Medical m, boolean approving) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(approving ? "Approve Medical" : "Reject Medical");
        confirm.setHeaderText(null);
        confirm.setContentText(
                (approving ? "✅ APPROVE" : "❌ REJECT") + " medical for:\n" +
                        "Student: " + m.getStudentName() + "\n" +
                        "Period:  " + m.getFromDate() + " → " + m.getToDate() +
                        "\n\nConfirm?");

        Button okBtn = (Button) confirm.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText(approving ? "✅ Yes, Approve" : "❌ Yes, Reject");
        okBtn.setStyle(approving
                ? "-fx-background-color:#28a745;-fx-text-fill:white;"
                : "-fx-background-color:#dc3545;-fx-text-fill:white;");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    String newStatus = approving ? "APPROVED" : "REJECTED";
                    medicalDAO.updateStatus(m.getMedicalId(), newStatus);
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setHeaderText(null);
                    success.setContentText(approving
                            ? "✅ Medical APPROVED for " + m.getStudentName()
                            : "❌ Medical REJECTED for " + m.getStudentName());
                    success.showAndWait();
                    showMedicals();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR,
                            "Error: " + ex.getMessage()).showAndWait();
                }
            }
        });
    }

    // Build table for all records
    private TableView<Medical> buildMedicalTable() {
        TableView<Medical> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(250);

        TableColumn<Medical,String> colName = new TableColumn<>("Student");
        colName.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStudentName() != null
                        ? d.getValue().getStudentName() : "—"));

        TableColumn<Medical,String> colFrom = new TableColumn<>("From");
        colFrom.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFromDate().toString()));

        TableColumn<Medical,String> colTo = new TableColumn<>("To");
        colTo.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getToDate().toString()));

        TableColumn<Medical,String> colReason = new TableColumn<>("Reason");
        colReason.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getReason() != null ? d.getValue().getReason() : "—"));

        TableColumn<Medical,String> colDoc = new TableColumn<>("Document");
        colDoc.setCellFactory(tc -> new TableCell<>() {
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

        TableColumn<Medical,String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStatus()));
        colStatus.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "APPROVED" ->
                            setStyle("-fx-text-fill:#155724;-fx-font-weight:bold;");
                    case "REJECTED" ->
                            setStyle("-fx-text-fill:#721c24;-fx-font-weight:bold;");
                    default ->
                            setStyle("-fx-text-fill:#854d0e;-fx-font-weight:bold;");
                }
            }
        });

        TableColumn<Medical,String> colAction = new TableColumn<>("Action");
        colAction.setMinWidth(130);
        colAction.setCellFactory(tc -> new TableCell<>() {
            final Button btnA = new Button("✅");
            final Button btnR = new Button("❌");
            final HBox   box  = new HBox(6, btnA, btnR);
            {
                String s = "-fx-background-radius:4;-fx-padding:4 10;" +
                        "-fx-font-size:12;-fx-cursor:hand;-fx-text-fill:white;";
                btnA.setStyle("-fx-background-color:#28a745;" + s);
                btnR.setStyle("-fx-background-color:#dc3545;" + s);
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Medical m = getTableView().getItems().get(getIndex());
                btnA.setDisable("APPROVED".equals(m.getStatus()));
                btnR.setDisable("REJECTED".equals(m.getStatus()));
                btnA.setOnAction(e -> handleApproval(m, true));
                btnR.setOnAction(e -> handleApproval(m, false));
                setGraphic(box);
            }
        });

        table.getColumns().addAll(
                colName, colFrom, colTo, colReason, colDoc, colStatus, colAction);
        return table;
    }

    // ── MARK ATTENDANCE ────────────────────────────────────
    @FXML
    public void showMarkAttendance() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("📋 Mark Attendance"));

        try {
            List<Course>        courses  = courseDAO.getCoursesByDepartments(getAllowedDepartments());
            List<Undergraduate> students = userDAO.getAllUndergraduatesByDepartments(getAllowedDepartments());

            VBox formCard = new VBox(12);
            formCard.getStyleClass().add("card");
            formCard.setMaxWidth(560);
            formCard.getChildren().add(sectionTitle("➕ Add Attendance Record"));

            ComboBox<Course> cbCourse = new ComboBox<>(
                    FXCollections.observableArrayList(courses));
            cbCourse.setPromptText("Select Course");

            ComboBox<Undergraduate> cbStudent = new ComboBox<>(
                    FXCollections.observableArrayList(students));
            cbStudent.setPromptText("Select Student");

            ComboBox<String> cbType = new ComboBox<>(
                    FXCollections.observableArrayList("THEORY", "PRACTICAL"));
            cbType.setValue("THEORY");

            DatePicker dpDate    = new DatePicker(LocalDate.now());
            CheckBox   cbPresent = new CheckBox("Present");
            cbPresent.setSelected(true);

            Button btnSave   = new Button("💾 Save Attendance");
            btnSave.getStyleClass().add("btn-primary");
            Label  lblStatus = new Label();

            btnSave.setOnAction(e -> {
                if (cbCourse.getValue() == null || cbStudent.getValue() == null) {
                    lblStatus.setText("⚠ Please select course and student.");
                    lblStatus.setStyle("-fx-text-fill:#fd7e14;-fx-font-weight:bold;");
                    return;
                }
                try {
                    Attendance a = new Attendance(0,
                            cbStudent.getValue().getUgId(),
                            cbCourse.getValue().getCourseId(),
                            dpDate.getValue(), cbType.getValue(),
                            cbPresent.isSelected());
                    attendanceDAO.addAttendance(a);
                    lblStatus.setText("✅ Saved for " + cbStudent.getValue().getFullName());
                    lblStatus.setStyle("-fx-text-fill:#28a745;-fx-font-weight:bold;");
                } catch (Exception ex) {
                    lblStatus.setText("❌ " + ex.getMessage());
                    lblStatus.setStyle("-fx-text-fill:#dc3545;");
                }
            });

            GridPane form = new GridPane();
            form.setHgap(12); form.setVgap(12);
            form.addRow(0, boldLabel("Course:"),   cbCourse);
            form.addRow(1, boldLabel("Student:"),  cbStudent);
            form.addRow(2, boldLabel("Type:"),     cbType);
            form.addRow(3, boldLabel("Date:"),     dpDate);
            form.addRow(4, boldLabel("Status:"),   cbPresent);
            form.addRow(5, new Label(""),          btnSave);
            form.addRow(6, new Label(""),          lblStatus);
            formCard.getChildren().add(form);
            contentPane.getChildren().add(formCard);

            // View & Delete section
            contentPane.getChildren().add(sectionTitle("🗑 View & Delete Records"));

            HBox filterBar = new HBox(12);
            filterBar.setAlignment(Pos.CENTER_LEFT);
            ComboBox<Course> cbFC = new ComboBox<>(FXCollections.observableArrayList(courses));
            cbFC.setPromptText("Select Course");
            ComboBox<Undergraduate> cbFS = new ComboBox<>(FXCollections.observableArrayList(students));
            cbFS.setPromptText("All Students");
            ComboBox<String> cbFT = new ComboBox<>(
                    FXCollections.observableArrayList("ALL","THEORY","PRACTICAL"));
            cbFT.setValue("ALL");
            Button btnLoad = new Button("🔍 Load Records");
            btnLoad.getStyleClass().add("btn-primary");
            filterBar.getChildren().addAll(
                    boldLabel("Course:"), cbFC,
                    boldLabel("Student:"), cbFS,
                    boldLabel("Type:"), cbFT, btnLoad);

            TableView<Attendance> attTable = new TableView<>();
            attTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            attTable.setPrefHeight(280);

            TableColumn<Attendance,String> cStu = new TableColumn<>("Student");
            cStu.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getStudentName() != null
                            ? d.getValue().getStudentName() : "—"));
            TableColumn<Attendance,String> cDate = new TableColumn<>("Date");
            cDate.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getSessionDate().toString()));
            TableColumn<Attendance,String> cType = new TableColumn<>("Type");
            cType.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getSessionType()));
            TableColumn<Attendance,String> cStat = new TableColumn<>("Status");
            cStat.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().isPresent() ? "✅ Present" : "❌ Absent"));

            TableColumn<Attendance,String> cDel = new TableColumn<>("Action");
            cDel.setMinWidth(110);
            cDel.setCellFactory(tc -> new TableCell<>() {
                final Button del = new Button("🗑 Delete");
                { del.setStyle("-fx-background-color:#dc3545;-fx-text-fill:white;" +
                        "-fx-background-radius:5;-fx-padding:5 12;-fx-cursor:hand;"); }
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) { setGraphic(null); return; }
                    Attendance a = getTableView().getItems().get(getIndex());
                    del.setOnAction(e -> {
                        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
                        c.setHeaderText(null);
                        c.setContentText("Delete attendance record for\n" +
                                a.getStudentName() + "\n" +
                                a.getSessionDate() + " — " + a.getSessionType() +
                                "\n\nCannot be undone.");
                        c.showAndWait().ifPresent(r -> {
                            if (r == ButtonType.OK) {
                                try {
                                    attendanceDAO.deleteAttendance(a.getAttId());
                                    attTable.getItems().remove(a);
                                } catch (Exception ex) { ex.printStackTrace(); }
                            }
                        });
                    });
                    setGraphic(del);
                }
            });

            attTable.getColumns().addAll(cStu, cDate, cType, cStat, cDel);

            Label lblSum = new Label();
            lblSum.setStyle("-fx-font-size:13;-fx-font-weight:bold;");

            btnLoad.setOnAction(e -> {
                Course c = cbFC.getValue();
                if (c == null) { lblSum.setText("⚠ Select a course."); return; }
                try {
                    Undergraduate sel = cbFS.getValue();
                    List<Attendance> records = sel != null
                            ? attendanceDAO.getForStudent(sel.getUgId(), c.getCourseId())
                            : attendanceDAO.getAllForCourse(c.getCourseId());
                    String t = cbFT.getValue();
                    if (!"ALL".equals(t))
                        records = records.stream()
                                .filter(a -> a.getSessionType().equals(t)).toList();
                    attTable.setItems(FXCollections.observableArrayList(records));
                    long pre = records.stream().filter(Attendance::isPresent).count();
                    long abs = records.stream().filter(a -> !a.isPresent()).count();
                    lblSum.setText("Total: " + records.size()
                            + "   ✅ Present: " + pre + "   ❌ Absent: " + abs);
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            contentPane.getChildren().addAll(filterBar, lblSum, attTable);

            // Batch summary
            contentPane.getChildren().add(sectionTitle("📊 Batch Attendance Summary"));
            ComboBox<Course> cbB = new ComboBox<>(FXCollections.observableArrayList(courses));
            cbB.setPromptText("Select course");
            Button btnB = new Button("View Summary"); btnB.getStyleClass().add("btn-outline");
            TableView<Object[]> st = new TableView<>();
            st.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            st.getColumns().addAll(colArr("Reg",0),colArr("Name",1),
                    colArr("Present",2),colArr("Total",3),colArr("%",4),colArr("Status",5));
            btnB.setOnAction(e -> {
                if (cbB.getValue()==null) return;
                try { st.setItems(FXCollections.observableArrayList(
                        attendanceDAO.getBatchSummary(cbB.getValue().getCourseId())));
                } catch (Exception ex) { ex.printStackTrace(); }
            });
            contentPane.getChildren().addAll(new HBox(10,cbB,btnB), st);

        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }

    // ── NOTICES ────────────────────────────────────────────
    @FXML
    public void showNotices() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("📢 Notice Management"));

        try {
            // ── ADD NOTICE FORM ──
            VBox formCard = new VBox(12);
            formCard.getStyleClass().add("card");
            formCard.setMaxWidth(620);
            formCard.getChildren().add(sectionTitle("➕ Post New Notice"));

            TextField fTitle   = new TextField();
            fTitle.setPromptText("Notice title...");

            TextArea  fContent = new TextArea();
            fContent.setPromptText("Notice content / details...");
            fContent.setPrefRowCount(3);
            fContent.setWrapText(true);

            Button btnPost   = new Button("📢 Post Notice");
            btnPost.getStyleClass().add("btn-primary");
            Label  lblStatus = new Label();

            btnPost.setOnAction(e -> {
                if (fTitle.getText().isBlank()) {
                    lblStatus.setText("⚠ Please enter a title.");
                    lblStatus.setStyle("-fx-text-fill:#fd7e14;-fx-font-weight:bold;");
                    return;
                }
                if (fContent.getText().isBlank()) {
                    lblStatus.setText("⚠ Please enter the notice content.");
                    lblStatus.setStyle("-fx-text-fill:#fd7e14;-fx-font-weight:bold;");
                    return;
                }
                try {
                    Notice n = new Notice(0, fTitle.getText().trim(),
                            fContent.getText().trim(),
                            user.getUserId(), null);
                    noticeDAO.addNotice(n);
                    lblStatus.setText("✅ Notice posted successfully!");
                    lblStatus.setStyle("-fx-text-fill:#28a745;-fx-font-weight:bold;");
                    fTitle.clear();
                    fContent.clear();
                    // Refresh page
                    showNotices();
                } catch (Exception ex) {
                    lblStatus.setText("❌ Error: " + ex.getMessage());
                    lblStatus.setStyle("-fx-text-fill:#dc3545;");
                    ex.printStackTrace();
                }
            });

            GridPane form = new GridPane();
            form.setHgap(12); form.setVgap(10);
            form.addRow(0, boldLabel("Title:"),   fTitle);
            form.addRow(1, boldLabel("Content:"), fContent);
            form.addRow(2, new Label(""),         btnPost);
            form.addRow(3, new Label(""),         lblStatus);

            formCard.getChildren().add(form);
            contentPane.getChildren().add(formCard);

            // ── NOTICES LIST ──
            contentPane.getChildren().add(sectionTitle("📋 All Notices"));

            List<Notice> notices = noticeDAO.getAll();

            if (notices.isEmpty()) {
                VBox emptyBox = new VBox(8);
                emptyBox.getStyleClass().add("card");
                Label emptyLbl = new Label("No notices posted yet.");
                emptyLbl.setStyle("-fx-font-size:13;-fx-text-fill:#6c757d;");
                emptyBox.getChildren().add(emptyLbl);
                contentPane.getChildren().add(emptyBox);
                return;
            }

            for (Notice n : notices) {
                VBox card = new VBox(10);
                card.setStyle(
                        "-fx-background-color:white;" +
                                "-fx-background-radius:10;" +
                                "-fx-padding:16;" +
                                "-fx-border-color:#1a73e8;" +
                                "-fx-border-width:0 0 0 4;" +
                                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);");

                // Header row
                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);

                String titleText = (n.getTitle() != null && !n.getTitle().isBlank())
                        ? n.getTitle() : "(No Title)";
                Label titleLbl = new Label("📌 " + titleText);
                titleLbl.setStyle("-fx-font-size:15;-fx-font-weight:bold;" +
                        "-fx-text-fill:#1a1a2e;");
                titleLbl.setWrapText(true);
                HBox.setHgrow(titleLbl, Priority.ALWAYS);

                // Delete button
                if (n.getCreatedBy() == user.getUserId()) {
                    Button btnDel = new Button("🗑 Delete");
                    btnDel.setStyle(
                            "-fx-background-color:#dc3545;-fx-text-fill:white;" +
                                    "-fx-background-radius:6;-fx-padding:5 12;" +
                                    "-fx-font-size:12;-fx-cursor:hand;");

                    btnDel.setOnAction(e -> {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Delete Notice");
                        confirm.setHeaderText(null);
                        confirm.setContentText(
                                "⚠ Delete this notice?\n\n" +
                                        "Title: " + n.getTitle() +
                                        "\n\nThis cannot be undone.");

                        Button okBtn = (Button) confirm.getDialogPane().lookupButton(ButtonType.OK);
                        okBtn.setText("Yes, Delete");
                        okBtn.setStyle("-fx-background-color:#dc3545;-fx-text-fill:white;");

                        confirm.showAndWait().ifPresent(result -> {
                            if (result == ButtonType.OK) {
                                try {
                                    boolean deleted = noticeDAO.deleteNoticeByOwner(
                                            n.getNoticeId(),
                                            user.getUserId()
                                    );

                                    if (!deleted) {
                                        new Alert(Alert.AlertType.ERROR,
                                                "You can delete only your own notices!")
                                                .showAndWait();
                                        return;
                                    }

                                    showNotices();
                                } catch (Exception ex) {
                                    new Alert(Alert.AlertType.ERROR,
                                            "Error: " + ex.getMessage()).showAndWait();
                                }
                            }
                        });
                    });

                    header.getChildren().addAll(titleLbl, btnDel);
                } else {
                    header.getChildren().add(titleLbl);
                }

                // Meta
                String byName = (n.getCreatedByName() != null)
                        ? n.getCreatedByName() : "Unknown";
                Label meta = new Label("👤 " + byName
                        + "   📅 " + n.getCreatedAt().toLocalDate());
                meta.setStyle("-fx-font-size:11;-fx-text-fill:#6c757d;");

                // Content
                String contentText = (n.getContent() != null && !n.getContent().isBlank())
                        ? n.getContent() : "(No content)";
                Label contentLbl = new Label(contentText);
                contentLbl.setWrapText(true);
                contentLbl.setStyle("-fx-font-size:13;-fx-text-fill:#3c4043;");

                card.getChildren().addAll(header, meta, new Separator(), contentLbl);
                contentPane.getChildren().add(card);
            }

        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    // ── TIMETABLE ──────────────────────────────────────────
    @FXML
    public void showTimetable() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(title("🗓 Timetable Management"));

        try {
            List<Course> courses = courseDAO.getCoursesByDepartments(getAllowedDepartments());

            // ── ADD NEW ENTRY FORM ──
            VBox formCard = new VBox(12);
            formCard.getStyleClass().add("card");
            formCard.setMaxWidth(620);
            formCard.getChildren().add(sectionTitle("➕ Add New Timetable Entry"));

            ComboBox<Course> cbCourse = new ComboBox<>(
                    FXCollections.observableArrayList(courses));
            cbCourse.setPromptText("Select Course");
            cbCourse.setPrefWidth(280);

            ComboBox<String> cbDay = new ComboBox<>(FXCollections.observableArrayList(
                    "MON","TUE","WED","THU","FRI","SAT"));
            cbDay.setPromptText("Day");

            ComboBox<String> cbType = new ComboBox<>(FXCollections.observableArrayList(
                    "THEORY","PRACTICAL"));
            cbType.setPromptText("Session Type");
            cbType.setValue("THEORY");

            TextField fStart    = new TextField();
            fStart.setPromptText("Start e.g 08:00");

            TextField fEnd      = new TextField();
            fEnd.setPromptText("End e.g 10:00");

            TextField fLocation = new TextField();
            fLocation.setPromptText("Location e.g Hall A - Block 1");
            fLocation.setPrefWidth(250);

            Button btnAdd    = new Button("➕ Add Entry");
            btnAdd.getStyleClass().add("btn-primary");
            Label  lblStatus = new Label();

            GridPane grid = new GridPane();
            grid.setHgap(12); grid.setVgap(10);
            grid.addRow(0, boldLabel("Course:"),   cbCourse,   boldLabel("Day:"),      cbDay);
            grid.addRow(1, boldLabel("Type:"),     cbType,     boldLabel("Location:"), fLocation);
            grid.addRow(2, boldLabel("Start:"),    fStart,     boldLabel("End:"),      fEnd);
            grid.addRow(3, btnAdd, lblStatus);

            formCard.getChildren().add(grid);
            contentPane.getChildren().add(formCard);

            // ── TIMETABLE TABLE ──
            contentPane.getChildren().add(sectionTitle("📋 Current Timetable"));

            TableView<Timetable> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.setPrefHeight(380);

            TableColumn<Timetable,String> colDay  = new TableColumn<>("Day");
            colDay.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getDayOfWeek()));

            TableColumn<Timetable,String> colCode = new TableColumn<>("Code");
            colCode.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getCourseCode()));

            TableColumn<Timetable,String> colName = new TableColumn<>("Course");
            colName.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getCourseName()));

            TableColumn<Timetable,String> colType = new TableColumn<>("Type");
            colType.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getSessionType()));
            colType.setCellFactory(tc -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setStyle(""); return; }
                    setText(item);
                    setStyle("THEORY".equals(item)
                            ? "-fx-text-fill:#1a73e8;-fx-font-weight:bold;"
                            : "-fx-text-fill:#28a745;-fx-font-weight:bold;");
                }
            });

            TableColumn<Timetable,String> colStart = new TableColumn<>("Start");
            colStart.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getStartTime().toString()));

            TableColumn<Timetable,String> colEnd = new TableColumn<>("End");
            colEnd.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getEndTime().toString()));

            TableColumn<Timetable,String> colLoc = new TableColumn<>("Location");
            colLoc.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getLocation() != null
                            ? d.getValue().getLocation() : "TBA"));

            // ── EDIT + DELETE columns ──
            TableColumn<Timetable,String> colEdit = new TableColumn<>("Edit");
            colEdit.setMinWidth(80);
            colEdit.setCellFactory(tc -> new TableCell<>() {
                final Button btn = new Button("✏ Edit");
                {
                    btn.setStyle(
                            "-fx-background-color:#1a73e8;-fx-text-fill:white;" +
                                    "-fx-background-radius:5;-fx-padding:4 10;" +
                                    "-fx-font-size:11;-fx-cursor:hand;");
                }
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) { setGraphic(null); return; }
                    Timetable tt = getTableView().getItems().get(getIndex());
                    btn.setOnAction(e -> showEditDialog(tt, table, courses));
                    setGraphic(btn);
                }
            });

            TableColumn<Timetable,String> colDel = new TableColumn<>("Delete");
            colDel.setMinWidth(90);
            colDel.setCellFactory(tc -> new TableCell<>() {
                final Button btn = new Button("🗑 Delete");
                {
                    btn.setStyle(
                            "-fx-background-color:#dc3545;-fx-text-fill:white;" +
                                    "-fx-background-radius:5;-fx-padding:4 10;" +
                                    "-fx-font-size:11;-fx-cursor:hand;");
                }
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) { setGraphic(null); return; }
                    Timetable tt = getTableView().getItems().get(getIndex());
                    btn.setOnAction(e -> {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Delete Timetable Entry");
                        confirm.setHeaderText(null);
                        confirm.setContentText(
                                "⚠ Delete this timetable entry?\n\n" +
                                        "Course: " + tt.getCourseName() + "\n" +
                                        "Day:    " + tt.getDayOfWeek() + "\n" +
                                        "Time:   " + tt.getStartTime() + " - " + tt.getEndTime() +
                                        "\n\nThis cannot be undone.");

                        Button okBtn = (Button) confirm.getDialogPane()
                                .lookupButton(ButtonType.OK);
                        okBtn.setText("Yes, Delete");
                        okBtn.setStyle(
                                "-fx-background-color:#dc3545;-fx-text-fill:white;");

                        confirm.showAndWait().ifPresent(result -> {
                            if (result == ButtonType.OK) {
                                try {
                                    timetableDAO.deleteTimetable(tt.getTtId());
                                    table.getItems().remove(tt);
                                    lblStatus.setText("✅ Entry deleted.");
                                    lblStatus.setStyle(
                                            "-fx-text-fill:#28a745;-fx-font-weight:bold;");
                                } catch (Exception ex) {
                                    lblStatus.setText("❌ " + ex.getMessage());
                                    lblStatus.setStyle("-fx-text-fill:#dc3545;");
                                }
                            }
                        });
                    });
                    setGraphic(btn);
                }
            });

            table.getColumns().addAll(
                    colDay, colCode, colName, colType,
                    colStart, colEnd, colLoc, colEdit, colDel);

            // Load timetable
            loadTimetableIntoTable(table);

            // ── WIRE UP ADD BUTTON ──
            btnAdd.setOnAction(e -> {
                if (cbCourse.getValue() == null || cbDay.getValue() == null
                        || fStart.getText().isBlank() || fEnd.getText().isBlank()) {
                    lblStatus.setText("⚠ Please fill all required fields.");
                    lblStatus.setStyle("-fx-text-fill:#fd7e14;-fx-font-weight:bold;");
                    return;
                }
                try {
                    // Validate time format
                    java.time.LocalTime start = java.time.LocalTime.parse(
                            fStart.getText().trim());
                    java.time.LocalTime end   = java.time.LocalTime.parse(
                            fEnd.getText().trim());

                    if (!end.isAfter(start)) {
                        lblStatus.setText("⚠ End time must be after start time.");
                        lblStatus.setStyle("-fx-text-fill:#fd7e14;-fx-font-weight:bold;");
                        return;
                    }

                    Timetable tt = new Timetable(0,
                            cbCourse.getValue().getCourseId(),
                            cbDay.getValue(), start, end,
                            fLocation.getText().trim(),
                            cbType.getValue());
                    timetableDAO.addTimetable(tt);

                    lblStatus.setText("✅ Entry added: " +
                            cbCourse.getValue().getCourseCode() +
                            " — " + cbDay.getValue() +
                            " " + fStart.getText() + "-" + fEnd.getText());
                    lblStatus.setStyle("-fx-text-fill:#28a745;-fx-font-weight:bold;");

                    // Clear fields
                    cbCourse.setValue(null);
                    cbDay.setValue(null);
                    cbType.setValue("THEORY");
                    fStart.clear(); fEnd.clear(); fLocation.clear();

                    // Refresh table
                    loadTimetableIntoTable(table);

                } catch (java.time.format.DateTimeParseException dtpe) {
                    lblStatus.setText("⚠ Invalid time format. Use HH:MM e.g 08:00");
                    lblStatus.setStyle("-fx-text-fill:#dc3545;-fx-font-weight:bold;");
                } catch (Exception ex) {
                    lblStatus.setText("❌ Error: " + ex.getMessage());
                    lblStatus.setStyle("-fx-text-fill:#dc3545;");
                    ex.printStackTrace();
                }
            });

            contentPane.getChildren().add(table);

        } catch (Exception e) {
            contentPane.getChildren().add(new Label("Error: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    // Load timetable into table
    private void loadTimetableIntoTable(TableView<Timetable> table) {
        try {
            table.setItems(FXCollections.observableArrayList(
                    timetableDAO.getTimetableByDepartments(getAllowedDepartments())
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Edit dialog
    private void showEditDialog(Timetable tt, TableView<Timetable> table,
                                List<Course> courses) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Timetable Entry");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(420);

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));

        Label heading = new Label("✏ Edit: " + tt.getCourseName());
        heading.setStyle("-fx-font-weight:bold;-fx-font-size:14;-fx-text-fill:#202124;");

        ComboBox<Course> cbCourse = new ComboBox<>(
                FXCollections.observableArrayList(courses));
        // Select current course
        courses.stream()
                .filter(c -> c.getCourseId() == tt.getCourseId())
                .findFirst().ifPresent(cbCourse::setValue);

        ComboBox<String> cbDay = new ComboBox<>(FXCollections.observableArrayList(
                "MON","TUE","WED","THU","FRI","SAT"));
        cbDay.setValue(tt.getDayOfWeek());

        ComboBox<String> cbType = new ComboBox<>(FXCollections.observableArrayList(
                "THEORY","PRACTICAL"));
        cbType.setValue(tt.getSessionType());

        TextField fStart    = new TextField(tt.getStartTime().toString());
        TextField fEnd      = new TextField(tt.getEndTime().toString());
        TextField fLocation = new TextField(tt.getLocation() != null
                ? tt.getLocation() : "");

        Label lblErr = new Label();
        lblErr.setStyle("-fx-text-fill:#dc3545;");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.addRow(0, boldLabel("Course:"),   cbCourse);
        grid.addRow(1, boldLabel("Day:"),      cbDay);
        grid.addRow(2, boldLabel("Type:"),     cbType);
        grid.addRow(3, boldLabel("Start:"),    fStart);
        grid.addRow(4, boldLabel("End:"),      fEnd);
        grid.addRow(5, boldLabel("Location:"), fLocation);
        grid.addRow(6, new Label(""),          lblErr);

        content.getChildren().addAll(heading, new Separator(), grid);
        dialog.getDialogPane().setContent(content);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("💾 Save Changes");
        okBtn.setStyle("-fx-background-color:#1a73e8;-fx-text-fill:white;");

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    if (cbCourse.getValue() == null) {
                        lblErr.setText("Please select a course.");
                        return;
                    }
                    java.time.LocalTime start = java.time.LocalTime.parse(
                            fStart.getText().trim());
                    java.time.LocalTime end   = java.time.LocalTime.parse(
                            fEnd.getText().trim());

                    if (!end.isAfter(start)) {
                        new Alert(Alert.AlertType.WARNING,
                                "End time must be after start time.").showAndWait();
                        return;
                    }

                    // Update via DAO — delete old, insert new
                    timetableDAO.deleteTimetable(tt.getTtId());
                    Timetable updated = new Timetable(0,
                            cbCourse.getValue().getCourseId(),
                            cbDay.getValue(), start, end,
                            fLocation.getText().trim(),
                            cbType.getValue());
                    timetableDAO.addTimetable(updated);

                    // Refresh table
                    loadTimetableIntoTable(table);

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setHeaderText(null);
                    success.setContentText("✅ Timetable entry updated successfully!");
                    success.showAndWait();

                } catch (java.time.format.DateTimeParseException dtpe) {
                    new Alert(Alert.AlertType.ERROR,
                            "Invalid time format. Use HH:MM e.g 08:00").showAndWait();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR,
                            "Error: " + ex.getMessage()).showAndWait();
                    ex.printStackTrace();
                }
            }
        });
    }

    // ── LOGOUT ─────────────────────────────────────────────
    @FXML
    public void handleLogout() {
        try { MainApp.showLogin(); } catch (Exception e) { e.printStackTrace(); }
    }

    // ── HELPERS ────────────────────────────────────────────


    private String saveProfilePhoto(File selectedFile, int userId) throws IOException {
        Path uploadDir = Path.of("profile_pictures");

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String fileName = "user_" + userId + "_" + selectedFile.getName();
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        img.setFitWidth(120);
        img.setFitHeight(120);
        img.setPreserveRatio(true);

        return img;
    }


    private HBox profileRow(String label, String value) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding:12 16;" +
                "-fx-border-color:#f0f2f5;-fx-border-width:0 0 1 0;");

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight:bold;-fx-text-fill:#5f6368;" +
                "-fx-font-size:13;-fx-min-width:160;");

        Label val = new Label(value != null ? value : "—");
        val.setStyle("-fx-text-fill:#202124;-fx-font-size:13;");

        row.getChildren().addAll(lbl, val);
        return row;
    }

    private Label title(String t) {
        Label l = new Label(t); l.getStyleClass().add("page-title"); return l; }

    private Label sectionTitle(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size:15;-fx-font-weight:bold;" +
                "-fx-text-fill:#202124;-fx-padding:8 0 4 0;");
        return l;
    }

    private Label boldLabel(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-weight:bold;-fx-text-fill:#5f6368;-fx-min-width:80;");
        return l;
    }

    private VBox statCard(String label, String value, String color) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        Label num = new Label(value);
        num.getStyleClass().add("stat-number");
        num.setStyle("-fx-text-fill:" + color + ";");
        card.getChildren().addAll(num, new Label(label){{getStyleClass().add("stat-label");}});
        return card;
    }

    private Button tabBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:#f0f2f5;-fx-text-fill:#5f6368;" +
                "-fx-background-radius:20;-fx-padding:6 16;" +
                "-fx-font-size:12;-fx-cursor:hand;");
        return btn;
    }

    private void setActiveTab(Button btn) {
        btn.setStyle("-fx-background-color:#1a73e8;-fx-text-fill:white;" +
                "-fx-background-radius:20;-fx-padding:6 16;" +
                "-fx-font-size:12;-fx-cursor:hand;");
    }

    private void setInactiveTab(Button... buttons) {
        for (Button b : buttons)
            b.setStyle("-fx-background-color:#f0f2f5;-fx-text-fill:#5f6368;" +
                    "-fx-background-radius:20;-fx-padding:6 16;" +
                    "-fx-font-size:12;-fx-cursor:hand;");
    }

    private TableColumn<Object[],String> colArr(String h, int idx) {
        TableColumn<Object[],String> c = new TableColumn<>(h);
        c.setCellValueFactory(d->new SimpleStringProperty(
                String.valueOf(d.getValue()[idx])));
        return c;
    }
}