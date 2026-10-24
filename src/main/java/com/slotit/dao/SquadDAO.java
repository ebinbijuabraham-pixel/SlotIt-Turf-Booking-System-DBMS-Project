package com.slotit.dao;

import com.slotit.utils.DBConnection;
import com.slotit.utils.JsonUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SquadDAO {

    /**
     * Lists all squad requests joined with slot, court, venue, and creator info.
     */
    public String getAllSquadsJson() {
        List<String> items = new ArrayList<>();
        String sql = "SELECT sr.*, u.name AS creator_name, ts.slot_date, ts.start_time, ts.end_time, " +
                     "c.court_name, v.name AS venue_name " +
                     "FROM squad_requests sr " +
                     "JOIN users u ON sr.user_id = u.user_id " +
                     "JOIN time_slots ts ON sr.slot_id = ts.slot_id " +
                     "JOIN courts c ON ts.court_id = c.court_id " +
                     "JOIN venues v ON c.venue_id = v.venue_id " +
                     "WHERE sr.status != 'cancelled' " +
                     "ORDER BY sr.status ASC, sr.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String json = String.format(Locale.US,
                    "{\"id\":%d,\"requestId\":%d,\"userId\":%d,\"creatorName\":\"%s\",\"slotId\":%d," +
                    "\"sport\":\"%s\",\"venueName\":\"%s\",\"courtName\":\"%s\",\"slotDate\":\"%s\"," +
                    "\"startTime\":\"%s\",\"endTime\":\"%s\",\"playersNeeded\":%d,\"currentCount\":%d," +
                    "\"message\":\"%s\",\"status\":\"%s\"}",
                    rs.getInt("request_id"),
                    rs.getInt("request_id"),
                    rs.getInt("user_id"),
                    JsonUtil.escapeJson(rs.getString("creator_name")),
                    rs.getInt("slot_id"),
                    JsonUtil.escapeJson(rs.getString("sport")),
                    JsonUtil.escapeJson(rs.getString("venue_name")),
                    JsonUtil.escapeJson(rs.getString("court_name")),
                    JsonUtil.escapeJson(rs.getString("slot_date")),
                    JsonUtil.escapeJson(rs.getString("start_time")),
                    JsonUtil.escapeJson(rs.getString("end_time")),
                    rs.getInt("players_needed"),
                    rs.getInt("current_count"),
                    JsonUtil.escapeJson(rs.getString("message")),
                    JsonUtil.escapeJson(rs.getString("status"))
                );
                items.add(json);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "[" + String.join(",", items) + "]";
    }

    public boolean createRequest(int userId, int slotId, String sport, int playersNeeded) {
        return createRequest(userId, slotId, sport, playersNeeded, "Looking for players to join our game!");
    }

    public boolean createRequest(int userId, int slotId, String sport, int playersNeeded, String message) {
        try (Connection conn = DBConnection.getConnection()) {
            int resolvedSlotId = slotId;
            if (resolvedSlotId <= 0) {
                // Pick the first existing time_slot so user posts without an explicit slot_id still succeed
                try (PreparedStatement ps = conn.prepareStatement("SELECT slot_id FROM time_slots ORDER BY slot_date DESC, slot_id ASC LIMIT 1");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resolvedSlotId = rs.getInt(1);
                    }
                }
            }
            if (resolvedSlotId <= 0) return false;

            String query = "INSERT INTO squad_requests (user_id, slot_id, sport, players_needed, current_count, message, status) VALUES (?, ?, ?, ?, 1, ?, 'open')";
            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, resolvedSlotId);
                stmt.setString(3, sport != null ? sport : "Football");
                stmt.setInt(4, Math.max(2, playersNeeded));
                stmt.setString(5, message != null ? message : "");
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            int requestId = rs.getInt(1);
                            String memberQuery = "INSERT IGNORE INTO squad_members (request_id, user_id) VALUES (?, ?)";
                            try (PreparedStatement memberStmt = conn.prepareStatement(memberQuery)) {
                                memberStmt.setInt(1, requestId);
                                memberStmt.setInt(2, userId);
                                memberStmt.executeUpdate();
                            }
                        }
                    }
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Object> joinSquadWithMessage(int requestId, int userId) {
        Map<String, Object> res = new HashMap<>();
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();

            // 1. START TRANSACTION
            conn.setAutoCommit(false);

            // 2. SELECT ... FOR UPDATE (Row-level lock for concurrency control)
            String selectQuery = "SELECT current_count, players_needed, status FROM squad_requests WHERE request_id = ? FOR UPDATE";
            int currentCount = 0;
            int playersNeeded = 0;
            String status = "open";

            try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                selectStmt.setInt(1, requestId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        currentCount = rs.getInt("current_count");
                        playersNeeded = rs.getInt("players_needed");
                        status = rs.getString("status");
                    } else {
                        conn.rollback();
                        res.put("success", false);
                        res.put("message", "Squad request not found");
                        return res;
                    }
                }
            }

            if (!"open".equalsIgnoreCase(status) || currentCount >= playersNeeded) {
                conn.rollback();
                res.put("success", false);
                res.put("message", "Squad is already full");
                return res;
            }

            // Check if user already joined this squad
            String dupCheck = "SELECT 1 FROM squad_members WHERE request_id = ? AND user_id = ?";
            try (PreparedStatement dupStmt = conn.prepareStatement(dupCheck)) {
                dupStmt.setInt(1, requestId);
                dupStmt.setInt(2, userId);
                try (ResultSet rs = dupStmt.executeQuery()) {
                    if (rs.next()) {
                        conn.rollback();
                        res.put("success", false);
                        res.put("message", "You have already joined this squad");
                        return res;
                    }
                }
            }

            // Increment current_count and mark 'full' if reaching players_needed
            int newCount = currentCount + 1;
            String newStatus = (newCount >= playersNeeded) ? "full" : "open";
            String updateQuery = "UPDATE squad_requests SET current_count = ?, status = ? WHERE request_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, newCount);
                updateStmt.setString(2, newStatus);
                updateStmt.setInt(3, requestId);
                updateStmt.executeUpdate();
            }

            String insertQuery = "INSERT INTO squad_members (request_id, user_id) VALUES (?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, requestId);
                insertStmt.setInt(2, userId);
                insertStmt.executeUpdate();
            }

            conn.commit();
            res.put("success", true);
            res.put("currentCount", newCount);
            res.put("playersNeeded", playersNeeded);
            res.put("status", newStatus);
            res.put("message", "Successfully joined the squad!");
            return res;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            res.put("success", false);
            res.put("message", "Database error: " + e.getMessage());
            return res;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean joinSquad(int requestId, int userId) {
        Map<String, Object> r = joinSquadWithMessage(requestId, userId);
        return Boolean.TRUE.equals(r.get("success"));
    }
}
