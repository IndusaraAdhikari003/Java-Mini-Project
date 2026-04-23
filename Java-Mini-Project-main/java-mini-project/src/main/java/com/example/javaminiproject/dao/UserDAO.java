package com.example.javaminiproject.dao;

import com.example.javaminiproject.db.DBConnection;
import com.example.javaminiproject.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.example.javaminiproject.util.PasswordUtil;

public class UserDAO {

    // LOGIN — returns correct subclass based on role (Polymorphism)
    public User login(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username=?";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");

                if (PasswordUtil.verifyPassword(password, storedPassword)) {
                    int userId = rs.getInt("user_id");

                    // If old plain-text password, upgrade to hashed password automatically
                    if (!PasswordUtil.isHashed(storedPassword)) {
                        String newHashedPassword = PasswordUtil.hashPassword(password);
                        updatePasswordHash(userId, newHashedPassword);
                    }

                    return buildUser(rs);
                }
            }
        }
        return null;
    }

    // Build correct User subclass from ResultSet
    private User buildUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String uname = rs.getString("username");
        String pass = rs.getString("password");
        String name = rs.getString("full_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String dept = rs.getString("department");
        String role = rs.getString("role");
        String profilePic = rs.getString("profile_pic");

        User user = switch (role) {
            case "ADMIN" -> new Admin(id, uname, pass, name, email, phone);
            case "LECTURER" -> new Lecturer(id, uname, pass, name, email, phone, dept);
            case "TECH_OFFICER" -> new TechnicalOfficer(id, uname, pass, name, email, phone, dept);
            case "UNDERGRADUATE" -> buildUndergraduate(id, uname, pass, name, email, phone, dept);
            default -> throw new SQLException("Unknown role: " + role);
        };

        user.setProfilePic(profilePic);
        return user;
    }

    // reads department from users table
    private Undergraduate buildUndergraduate(int userId, String uname, String pass,
                                             String name, String email, String phone,
                                             String department) throws SQLException {
        String sql = "SELECT * FROM undergraduates WHERE user_id=?";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Undergraduate(userId, uname, pass, name, email, phone,
                        department,                    // ← added
                        rs.getInt("ug_id"), rs.getString("reg_number"),
                        rs.getString("batch"), rs.getBoolean("is_repeat"),
                        rs.getBoolean("batch_missed"));
            }
        }
        throw new SQLException("Undergraduate profile not found for userId: " + userId);
    }

    // GET ALL USERS by role
    public List<User> getAllByRole(String role) throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role=? ORDER BY full_name";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(buildUser(rs));
            }
        }
        return list;
    }

    // GET ALL UNDERGRADUATES with ug details

    //  — add department to new Undergraduate()
    public List<Undergraduate> getAllUndergraduates() throws SQLException {
        List<Undergraduate> list = new ArrayList<>();
        String sql = "SELECT u.*, ug.ug_id, ug.reg_number, ug.batch, ug.is_repeat, ug.batch_missed " +
                "FROM users u JOIN undergraduates ug ON u.user_id=ug.user_id ORDER BY ug.reg_number";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Undergraduate ug = new Undergraduate(
                        rs.getInt("user_id"), rs.getString("username"),
                        rs.getString("password"), rs.getString("full_name"),
                        rs.getString("email"), rs.getString("phone"),
                        rs.getString("department"),
                        rs.getInt("ug_id"), rs.getString("reg_number"),
                        rs.getString("batch"), rs.getBoolean("is_repeat"),
                        rs.getBoolean("batch_missed"));

                ug.setProfilePic(rs.getString("profile_pic"));
                list.add(ug);
            }
        }
        return list;
    }


    public List<Undergraduate> getAllUndergraduatesByDepartments(List<String> departments) throws SQLException {
        if (departments == null || departments.isEmpty()) return getAllUndergraduates();
        String placeholders = String.join(",",
                java.util.Collections.nCopies(departments.size(), "?"));
        String sql = "SELECT u.*, ug.ug_id, ug.reg_number, ug.batch, ug.is_repeat, ug.batch_missed " +
                "FROM users u JOIN undergraduates ug ON u.user_id=ug.user_id " +
                "WHERE u.department IN (" + placeholders + ") ORDER BY ug.reg_number";
        List<Undergraduate> list = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            for (int i = 0; i < departments.size(); i++) ps.setString(i + 1, departments.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Undergraduate ug = new Undergraduate(
                        rs.getInt("user_id"), rs.getString("username"),
                        rs.getString("password"), rs.getString("full_name"),
                        rs.getString("email"), rs.getString("phone"),
                        rs.getString("department"),
                        rs.getInt("ug_id"), rs.getString("reg_number"),
                        rs.getString("batch"), rs.getBoolean("is_repeat"),
                        rs.getBoolean("batch_missed"));

                ug.setProfilePic(rs.getString("profile_pic"));
                list.add(ug);
            }
        }
        return list;
    }

    // CREATE USER (Admin only)
    public void createUser(User user, String password) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        String sql = "INSERT INTO users (username,password,role,full_name,email,phone,department) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, PasswordUtil.hashPassword(password));
            ps.setString(3, user.getRole());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhone());
            ps.setString(7, user.getDepartment());
            ps.executeUpdate();

            // If undergraduate, also insert into undergraduates table
            if (user instanceof Undergraduate ug) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    int newUserId = keys.getInt(1);
                    String ugSql = "INSERT INTO undergraduates (user_id,reg_number,batch,is_repeat,batch_missed) VALUES (?,?,?,?,?)";
                    try (PreparedStatement ups = conn.prepareStatement(ugSql)) {
                        ups.setInt(1, newUserId);
                        ups.setString(2, ug.getRegNumber());
                        ups.setString(3, ug.getBatch());
                        ups.setBoolean(4, ug.isRepeat());
                        ups.setBoolean(5, ug.isBatchMissed());
                        ups.executeUpdate();
                    }
                }
            }
        }
    }

    // UPDATE profile (email, phone, dept, pic)
    public void updateProfile(User user) throws SQLException {
        String sql = "UPDATE users SET full_name=?, email=?, phone=?, department=?, profile_pic=? WHERE user_id=?";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getDepartment());
            ps.setString(5, user.getProfilePic());
            ps.setInt(6, user.getUserId());
            ps.executeUpdate();
        }
    }

    // DELETE USER
    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id=?";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
    public void updatePasswordHash(int userId, String hashedPassword) throws SQLException {
        String sql = "UPDATE users SET password=? WHERE user_id=?";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
}