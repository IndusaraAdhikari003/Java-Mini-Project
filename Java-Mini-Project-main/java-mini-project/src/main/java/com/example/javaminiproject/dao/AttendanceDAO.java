package com.example.javaminiproject.dao;

import com.example.javaminiproject.db.DBConnection;
import com.example.javaminiproject.model.Attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    public void addAttendance(Attendance a) throws SQLException {
        String sql = "INSERT INTO attendance " +
                "(ug_id,course_id,session_date,session_type,is_present) " +
                "VALUES (?,?,?,?,?)";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1,     a.getUgId());
            ps.setInt(2,     a.getCourseId());
            ps.setDate(3,    Date.valueOf(a.getSessionDate()));
            ps.setString(4,  a.getSessionType());
            ps.setBoolean(5, a.isPresent());
            ps.executeUpdate();
        }
    }

    public void deleteAttendance(int attId) throws SQLException {
        String sql = "DELETE FROM attendance WHERE att_id = ?";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, attId);
            ps.executeUpdate();
        }
    }

    public List<Attendance> getForStudent(int ugId, int courseId) throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.*, u.full_name AS student_name " +
                "FROM attendance a " +
                "JOIN undergraduates ug ON a.ug_id = ug.ug_id " +
                "JOIN users u ON ug.user_id = u.user_id " +
                "WHERE a.ug_id=? AND a.course_id=? ORDER BY a.session_date";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, ugId);
            ps.setInt(2, courseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Attendance a = map(rs);
                a.setStudentName(rs.getString("student_name"));
                list.add(a);
            }
        }
        return list;
    }

    public List<Attendance> getAllForCourse(int courseId) throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.att_id, a.ug_id, a.course_id, " +
                "a.session_date, a.session_type, a.is_present, " +
                "u.full_name AS student_name " +
                "FROM attendance a " +
                "JOIN undergraduates ug ON a.ug_id = ug.ug_id " +
                "JOIN users u ON ug.user_id = u.user_id " +
                "WHERE a.course_id = ? " +
                "ORDER BY a.session_date DESC, u.full_name";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Attendance a = new Attendance(
                        rs.getInt("att_id"),
                        rs.getInt("ug_id"),
                        rs.getInt("course_id"),
                        rs.getDate("session_date").toLocalDate(),
                        rs.getString("session_type"),
                        rs.getBoolean("is_present"));
                a.setStudentName(rs.getString("student_name"));
                list.add(a);
            }
        }
        return list;
    }

    public double getPercent(int ugId, int courseId, String type) throws SQLException {
        String sql = "SELECT COUNT(*) AS total, SUM(is_present) AS present " +
                "FROM attendance WHERE ug_id=? AND course_id=?" +
                (type.equals("ALL") ? "" : " AND session_type=?");
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, ugId);
            ps.setInt(2, courseId);
            if (!type.equals("ALL")) ps.setString(3, type);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int total   = rs.getInt("total");
                int present = rs.getInt("present");
                if (total == 0) return 0.0;
                return (double) present / total * 100.0;
            }
        }
        return 0.0;
    }

    public List<Object[]> getBatchSummary(int courseId) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT u.full_name, ug.reg_number, " +
                "COUNT(*) AS total, SUM(a.is_present) AS present " +
                "FROM attendance a " +
                "JOIN undergraduates ug ON a.ug_id=ug.ug_id " +
                "JOIN users u ON ug.user_id=u.user_id " +
                "WHERE a.course_id=? GROUP BY a.ug_id ORDER BY ug.reg_number";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int total   = rs.getInt("total");
                int present = rs.getInt("present");
                double pct  = total == 0 ? 0 : (double) present / total * 100;
                rows.add(new Object[]{
                        rs.getString("reg_number"),
                        rs.getString("full_name"),
                        present, total,
                        String.format("%.1f%%", pct),
                        pct >= 80 ? "✅ Eligible" : "❌ Not Eligible"
                });
            }
        }
        return rows;
    }

    private Attendance map(ResultSet rs) throws SQLException {
        return new Attendance(
                rs.getInt("att_id"),    rs.getInt("ug_id"),
                rs.getInt("course_id"), rs.getDate("session_date").toLocalDate(),
                rs.getString("session_type"), rs.getBoolean("is_present"));
    }
}