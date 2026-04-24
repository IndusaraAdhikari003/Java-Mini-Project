package com.example.javaminiproject.dao;

import com.example.javaminiproject.db.DBConnection;
import com.example.javaminiproject.model.Notice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NoticeDAO {

    public List<Notice> getAll() throws SQLException {
        List<Notice> list = new ArrayList<>();
        String sql = "SELECT n.*, u.full_name FROM notices n " +
                "LEFT JOIN users u ON n.created_by=u.user_id ORDER BY n.created_at DESC";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Notice n = new Notice(rs.getInt("notice_id"), rs.getString("title"),
                        rs.getString("content"), rs.getInt("created_by"),
                        rs.getTimestamp("created_at").toLocalDateTime());
                n.setCreatedByName(rs.getString("full_name"));
                list.add(n);
            }
        }
        return list;
    }

    public void addNotice(Notice n) throws SQLException {
        String sql = "INSERT INTO notices (title,content,created_by) VALUES (?,?,?)";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, n.getTitle());
            ps.setString(2, n.getContent());
            ps.setInt(3,    n.getCreatedBy());
            ps.executeUpdate();
        }
    }

    public void deleteNotice(int noticeId) throws SQLException {
        String sql = "DELETE FROM notices WHERE notice_id = ?";

        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, noticeId);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Notice delete failed.");
            }
        }
    }
    public boolean deleteNoticeByOwner(int noticeId, int ownerId) throws Exception {
        String sql = "DELETE FROM notices WHERE notice_id = ? AND created_by = ?";

        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, noticeId);
            ps.setInt(2, ownerId);

            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }
}