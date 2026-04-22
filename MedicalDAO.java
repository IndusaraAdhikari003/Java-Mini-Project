package com.example.javaminiproject.dao;

import com.example.javaminiproject.db.DBConnection;
import com.example.javaminiproject.model.Medical;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicalDAO {

    public void addMedical(Medical m) throws SQLException {
        String sql = "INSERT INTO medicals " +
                "(ug_id,from_date,to_date,reason,doc_path,is_approved,status) " +
                "VALUES (?,?,?,?,?,0,'PENDING')";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1,    m.getUgId());
            ps.setDate(2,   Date.valueOf(m.getFromDate()));
            ps.setDate(3,   Date.valueOf(m.getToDate()));
            ps.setString(4, m.getReason());
            ps.setString(5, m.getDocPath());
            ps.executeUpdate();
        }
    }

    public void updateStatus(int medicalId, String status) throws SQLException {
        boolean approved = "APPROVED".equals(status);
        String sql = "UPDATE medicals SET is_approved=?, status=? WHERE medical_id=?";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, approved);
            ps.setString(2,  status);
            ps.setInt(3,     medicalId);
            ps.executeUpdate();
        }
    }

    public void approveMedical(int medicalId, boolean approved) throws SQLException {
        updateStatus(medicalId, approved ? "APPROVED" : "REJECTED");
    }

    public List<Medical> getForStudent(int ugId) throws SQLException {
        List<Medical> list = new ArrayList<>();
        String sql = "SELECT * FROM medicals WHERE ug_id=? ORDER BY submitted_at DESC";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, ugId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Medical> getAll() throws SQLException {
        List<Medical> list = new ArrayList<>();
        String sql = "SELECT m.*, u.full_name " +
                "FROM medicals m " +
                "JOIN undergraduates ug ON m.ug_id = ug.ug_id " +
                "JOIN users u ON ug.user_id = u.user_id " +
                "ORDER BY m.submitted_at DESC";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Medical med = map(rs);
                med.setStudentName(rs.getString("full_name"));
                list.add(med);
            }
        }
        return list;
    }

    public List<Medical> getByStatus(String status) throws SQLException {
        List<Medical> list = new ArrayList<>();
        String sql = "SELECT m.*, u.full_name " +
                "FROM medicals m " +
                "JOIN undergraduates ug ON m.ug_id = ug.ug_id " +
                "JOIN users u ON ug.user_id = u.user_id " +
                "WHERE m.status = ? " +
                "ORDER BY m.submitted_at DESC";
        try (PreparedStatement ps =
                     DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Medical med = map(rs);
                med.setStudentName(rs.getString("full_name"));
                list.add(med);
            }
        }
        return list;
    }

    private Medical map(ResultSet rs) throws SQLException {
        Medical m = new Medical(
                rs.getInt("medical_id"),
                rs.getInt("ug_id"),
                rs.getDate("from_date").toLocalDate(),
                rs.getDate("to_date").toLocalDate(),
                rs.getString("reason"),
                rs.getString("doc_path"),
                rs.getBoolean("is_approved"));
        try {
            String s = rs.getString("status");
            if (s != null) m.setStatus(s);
        } catch (Exception ignored) {}
        return m;
    }
}