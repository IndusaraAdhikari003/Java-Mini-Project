package com.example.javaminiproject.dao;

import com.example.javaminiproject.db.DBConnection;
import com.example.javaminiproject.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public List<Course> getAllCourses() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name AS lec_name FROM courses c " +
                "LEFT JOIN users u ON c.lecturer_id=u.user_id ORDER BY c.course_code";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ── FILTERED by department list ──
    // Returns courses only from the given departments
    public List<Course> getCoursesByDepartments(List<String> departments)
            throws SQLException {
        if (departments == null || departments.isEmpty()) return getAllCourses();
        String placeholders = String.join(",",
                java.util.Collections.nCopies(departments.size(), "?"));
        String sql = "SELECT c.*, u.full_name AS lec_name FROM courses c " +
                "LEFT JOIN users u ON c.lecturer_id=u.user_id " +
                "WHERE c.department IN (" + placeholders + ") " +
                "ORDER BY c.course_code";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            for (int i = 0; i < departments.size(); i++) {
                ps.setString(i + 1, departments.get(i));
            }
            ResultSet rs = ps.executeQuery();
            List<Course> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        }
    }

    public void addCourse(Course c) throws SQLException {
        String sql = "INSERT INTO courses " +
                "(course_code,course_name,credits,has_theory," +
                "has_practical,department,lecturer_id) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1,  c.getCourseCode());
            ps.setString(2,  c.getCourseName());
            ps.setInt(3,     c.getCredits());
            ps.setBoolean(4, c.isHasTheory());
            ps.setBoolean(5, c.isHasPractical());
            ps.setString(6,  c.getDepartment());
            ps.setInt(7,     c.getLecturerId());
            ps.executeUpdate();
        }
    }

    public void updateCourse(Course c) throws SQLException {
        String sql = "UPDATE courses SET course_code=?,course_name=?," +
                "credits=?,has_theory=?,has_practical=?,lecturer_id=? " +
                "WHERE course_id=?";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1,  c.getCourseCode());
            ps.setString(2,  c.getCourseName());
            ps.setInt(3,     c.getCredits());
            ps.setBoolean(4, c.isHasTheory());
            ps.setBoolean(5, c.isHasPractical());
            ps.setInt(6,     c.getLecturerId());
            ps.setInt(7,     c.getCourseId());
            ps.executeUpdate();
        }
    }

    public void deleteCourse(int courseId) throws SQLException {
        String sql = "DELETE FROM courses WHERE course_id=?";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.executeUpdate();
        }
    }

    private Course map(ResultSet rs) throws SQLException {
        return new Course(
                rs.getInt("course_id"),      rs.getString("course_code"),
                rs.getString("course_name"), rs.getInt("credits"),
                rs.getBoolean("has_theory"), rs.getBoolean("has_practical"),
                rs.getString("department"),  rs.getInt("lecturer_id"),
                rs.getString("lec_name"));
    }
}