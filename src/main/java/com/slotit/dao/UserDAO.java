package com.slotit.dao;

import com.slotit.models.User;
import com.slotit.utils.DBConnection;

import java.sql.*;

/**
 * Data Access Object for User entity.
 */
public class UserDAO {

    /**
     * Registers a new user.
     * @return The generated user ID or -1 if failed.
     */
    public int register(String name, String email, String phone, String passwordHash, String role) {
        String sql = "INSERT INTO users (name, email, phone, password_hash, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, passwordHash);
            stmt.setString(5, role);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Finds a user by email.
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return executeUserQuery(sql, email);
    }

    /**
     * Finds a user by ID.
     */
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        return executeUserQuery(sql, userId);
    }

    /**
     * Helper method to execute query returning a single user.
     */
    private User executeUserQuery(String sql, Object param) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (param instanceof String) {
                stmt.setString(1, (String) param);
            } else if (param instanceof Integer) {
                stmt.setInt(1, (Integer) param);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates a user's wallet balance.
     */
    public boolean updateWallet(int userId, double amount) {
        String sql = "UPDATE users SET wallet_balance = wallet_balance + ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, amount);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Maps a ResultSet row to a User object.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setWalletBalance(rs.getDouble("wallet_balance"));
        user.setStreakCount(rs.getInt("streak_count"));
        user.setTotalBookings(rs.getInt("total_bookings"));
        user.setBadge(rs.getString("badge"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}
