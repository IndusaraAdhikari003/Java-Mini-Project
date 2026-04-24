package com.example.javaminiproject.dao;

import com.example.javaminiproject.db.DBConnection;
import com.example.javaminiproject.model.CourseMaterial;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseMaterialDAO {

    public void addMaterial(CourseMaterial material) throws SQLException {
        String sql = "INSERT INTO course_materials (course_id, title, file_path, uploaded_by) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, material.getCourseId());
            ps.setString(2, material.getTitle());
            ps.setString(3, material.getFilePath());
            ps.setInt(4, material.getUploadedBy());
            ps.executeUpdate();
        }
    }

    public List<CourseMaterial> getMaterialsByCourse(int courseId) throws SQLException {
        List<CourseMaterial> list = new ArrayList<>();
        String sql = "SELECT cm.*, c.course_name " +
                "FROM course_materials cm " +
                "JOIN courses c ON cm.course_id = c.course_id " +
                "WHERE cm.course_id = ? " +
                "ORDER BY cm.uploaded_at DESC";

        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                CourseMaterial m = new CourseMaterial(
                        rs.getInt("material_id"),
                        rs.getInt("course_id"),
                        rs.getString("title"),
                        rs.getString("file_path"),
                        rs.getInt("uploaded_by")
                );
                m.setCourseName(rs.getString("course_name"));
                list.add(m);
            }
        }
        return list;
    }

    public void deleteMaterial(int materialId) throws SQLException {
        String sql = "DELETE FROM course_materials WHERE material_id = ?";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, materialId);
            ps.executeUpdate();
        }
    }
}