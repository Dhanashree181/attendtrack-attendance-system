package dao;

import models.User;
import database.DBConnection;
import java.sql.*;

/**
 * Data Access Object for user authentication and credential management.
 * Handles login verification and password changes for all roles.
 */
public class UserDAO {

    public User login(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    if (!rs.getString("password").equals(password)) return null;

                    String role = rs.getString("role");
                    String linkedId = rs.getString("linked_id");
                    
                    if ("STUDENT".equals(role)) {
                        String checkSql = "SELECT is_active FROM students WHERE roll_number = ?";
                        try (PreparedStatement ps2 = conn.prepareStatement(checkSql)) {
                            ps2.setString(1, linkedId);
                            try (ResultSet rs2 = ps2.executeQuery()) {
                                if (rs2.next() && !rs2.getBoolean("is_active")) return null;
                            }
                        }
                    } else if ("FACULTY".equals(role)) {
                        String checkSql = "SELECT is_active FROM faculty WHERE faculty_id = ?";
                        try (PreparedStatement ps2 = conn.prepareStatement(checkSql)) {
                            ps2.setString(1, linkedId);
                            try (ResultSet rs2 = ps2.executeQuery()) {
                                if (rs2.next() && !rs2.getBoolean("is_active")) return null;
                            }
                        }
                    }

                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        role,
                        linkedId
                    );
                }
            }
        }
        return null;
    }
    
    /** Inserts a new user account into the users table. */
    public void addUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, role, linked_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getLinkedId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Changes the password for a user.
     * Verifies the old password first; returns false if it does not match.
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) throws SQLException {
        // Verify old password
        String checkSql = "SELECT password FROM users WHERE user_id = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, oldPassword);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next() || !rs.getString("password").equals(oldPassword)) {
                    return false; // old password incorrect or user not found
                }
            }
        }
        // Update to new password
        String updateSql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
        return true;
    }

    /** Deletes a user account based on role and linked_id. */
    public void deleteUser(String role, String linkedId) throws SQLException {
        String sql = "DELETE FROM users WHERE role = ? AND linked_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role);
            pstmt.setString(2, linkedId);
            pstmt.executeUpdate();
        }
    }
}
