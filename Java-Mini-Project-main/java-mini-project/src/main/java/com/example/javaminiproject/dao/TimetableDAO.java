package com.example.javaminiproject.dao;

import com.example.javaminiproject.db.DBConnection;
import com.example.javaminiproject.model.Timetable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimetableDAO {

    public List<Timetable> getAll() throws SQLException {
        String sql = "SELECT t.*, c.course_name, c.course_code FROM timetables t " +
                "JOIN courses c ON t.course_id=c.course_id " +
                "ORDER BY FIELD(t.day_of_week,'MON','TUE','WED','THU','FRI','SAT')," +
                "t.start_time";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            return mapAll(ps.executeQuery());
        }
    }

    // ── FILTERED by department list ──
    public List<Timetable> getTimetableByDepartments(List<String> departments)
            throws SQLException {
        if (departments == null || departments.isEmpty()) return getAll();
        String placeholders = String.join(",",
                java.util.Collections.nCopies(departments.size(), "?"));
        String sql = "SELECT t.*, c.course_name, c.course_code FROM timetables t " +
                "JOIN courses c ON t.course_id=c.course_id " +
                "WHERE c.department IN (" + placeholders + ") " +
                "ORDER BY FIELD(t.day_of_week,'MON','TUE','WED','THU','FRI','SAT')," +
                "t.start_time";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            for (int i = 0; i < departments.size(); i++) {
                ps.setString(i + 1, departments.get(i));
            }
            return mapAll(ps.executeQuery());
        }
    }

    public void addTimetable(Timetable t) throws SQLException {
        String sql = "INSERT INTO timetables " +
                "(course_id,day_of_week,start_time,end_time,location,session_type) " +
                "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1,    t.getCourseId());
            ps.setString(2, t.getDayOfWeek());
            ps.setTime(3,   Time.valueOf(t.getStartTime()));
            ps.setTime(4,   Time.valueOf(t.getEndTime()));
            ps.setString(5, t.getLocation());
            ps.setString(6, t.getSessionType());
            ps.executeUpdate();
        }
    }

    public void deleteTimetable(int ttId) throws SQLException {
        String sql = "DELETE FROM timetables WHERE tt_id=?";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, ttId);
            ps.executeUpdate();
        }
    }

    private List<Timetable> mapAll(ResultSet rs) throws SQLException {
        List<Timetable> list = new ArrayList<>();
        while (rs.next()) {
            Timetable tt = new Timetable(
                    rs.getInt("tt_id"),          rs.getInt("course_id"),
                    rs.getString("day_of_week"), rs.getTime("start_time").toLocalTime(),
                    rs.getTime("end_time").toLocalTime(), rs.getString("location"),
                    rs.getString("session_type"));
            tt.setCourseName(rs.getString("course_name"));
            tt.setCourseCode(rs.getString("course_code"));
            list.add(tt);
        }
        return list;
    }
}