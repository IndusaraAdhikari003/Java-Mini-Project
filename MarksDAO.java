package com.example.javaminiproject.dao;

import com.example.javaminiproject.db.DBConnection;
import com.example.javaminiproject.model.Mark;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MarksDAO {

    public void addMark(Mark m) throws SQLException {
        String sql = "INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1,    m.getUgId());
            ps.setInt(2,    m.getCourseId());
            ps.setString(3, m.getExamType());
            ps.setDouble(4, m.getMarksValue());
            ps.setInt(5,    m.getUploadedBy());
            ps.executeUpdate();
        }
    }

    public void updateMark(Mark m) throws SQLException {
        String sql = "UPDATE marks SET marks_value=? WHERE mark_id=?";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setDouble(1, m.getMarksValue());
            ps.setInt(2,    m.getMarkId());
            ps.executeUpdate();
        }
    }

    public void deleteMark(int ugId, int courseId, String examType) throws SQLException {
        String sql = "DELETE FROM marks WHERE ug_id=? AND course_id=? AND exam_type=?";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, ugId);
            ps.setInt(2, courseId);
            ps.setString(3, examType);
            ps.executeUpdate();
        }
    }

    // All marks for one student across all courses
    public List<Mark> getForStudent(int ugId) throws SQLException {
        List<Mark> list = new ArrayList<>();
        String sql = "SELECT m.*, c.course_name FROM marks m " +
                "JOIN courses c ON m.course_id=c.course_id " +
                "WHERE m.ug_id=? ORDER BY c.course_code, m.exam_type";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, ugId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Mark mk = new Mark(rs.getInt("mark_id"), rs.getInt("ug_id"),
                        rs.getInt("course_id"), rs.getString("exam_type"),
                        rs.getDouble("marks_value"), rs.getInt("uploaded_by"));
                mk.setCourseName(rs.getString("course_name"));
                list.add(mk);
            }
        }
        return list;
    }

    // CA average for one student + course
    public double getCAAverage(int ugId, int courseId) throws SQLException {
        String sql = "SELECT AVG(marks_value) AS avg FROM marks " +
                "WHERE ug_id=? AND course_id=? AND exam_type IN ('CA1','CA2','CA3','ASSIGNMENT')";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, ugId);
            ps.setInt(2, courseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("avg");
        }
        return 0.0;
    }

    // Batch marks summary
    public List<Object[]> getBatchSummary(int courseId) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT ug.ug_id, u.full_name, ug.reg_number, m.exam_type, AVG(m.marks_value) AS avg " +
                "FROM marks m " +
                "JOIN undergraduates ug ON m.ug_id=ug.ug_id " +
                "JOIN users u ON ug.user_id=u.user_id " +
                "WHERE m.course_id=? " +
                "GROUP BY ug.ug_id, u.full_name, ug.reg_number, m.exam_type " +
                "ORDER BY ug.reg_number, m.exam_type";

        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getString("reg_number"),          // index 0
                        rs.getString("full_name"),           // index 1
                        rs.getString("exam_type"),           // index 2
                        String.format("%.1f", rs.getDouble("avg")), // index 3
                        rs.getInt("ug_id")                  // index 4 (hidden use)
                });
            }
        }
        return rows;
    }
}