package com.slotit.dao;

import com.slotit.utils.DBConnection;
import com.slotit.utils.JsonUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TournamentDAO {

    /**
     * Returns all tournaments as a JSON array joined with venue name and registered team count.
     */
    public String getAllTournamentsJson() {
        List<String> items = new ArrayList<>();
        String sql = "SELECT t.*, v.name AS venue_name, " +
                     "(SELECT COUNT(*) FROM teams tm WHERE tm.tournament_id = t.tournament_id) AS team_count " +
                     "FROM tournaments t JOIN venues v ON t.venue_id = v.venue_id " +
                     "ORDER BY t.start_date ASC, t.tournament_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String json = String.format(Locale.US,
                    "{\"id\":%d,\"tournamentId\":%d,\"name\":\"%s\",\"venueId\":%d,\"venueName\":\"%s\"," +
                    "\"sport\":\"%s\",\"startDate\":\"%s\",\"endDate\":\"%s\",\"format\":\"%s\"," +
                    "\"maxTeams\":%d,\"status\":\"%s\",\"teamCount\":%d}",
                    rs.getInt("tournament_id"),
                    rs.getInt("tournament_id"),
                    JsonUtil.escapeJson(rs.getString("name")),
                    rs.getInt("venue_id"),
                    JsonUtil.escapeJson(rs.getString("venue_name")),
                    JsonUtil.escapeJson(rs.getString("sport")),
                    JsonUtil.escapeJson(rs.getString("start_date")),
                    JsonUtil.escapeJson(rs.getString("end_date")),
                    JsonUtil.escapeJson(rs.getString("format")),
                    rs.getInt("max_teams"),
                    JsonUtil.escapeJson(rs.getString("status")),
                    rs.getInt("team_count")
                );
                items.add(json);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "[" + String.join(",", items) + "]";
    }

    /**
     * Returns teams for a tournament (or all teams if tournamentId <= 0) as a JSON array.
     */
    public String getTeamsJson(int tournamentId) {
        List<String> items = new ArrayList<>();
        String sql = "SELECT tm.*, t.name AS tournament_name, t.sport, t.start_date, u.name AS captain_name " +
                     "FROM teams tm JOIN tournaments t ON tm.tournament_id = t.tournament_id " +
                     "JOIN users u ON tm.captain_id = u.user_id " +
                     (tournamentId > 0 ? "WHERE tm.tournament_id = ? " : "") +
                     "ORDER BY tm.team_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (tournamentId > 0) {
                stmt.setInt(1, tournamentId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String json = String.format(Locale.US,
                        "{\"teamId\":%d,\"name\":\"%s\",\"tournamentId\":%d,\"tournamentName\":\"%s\"," +
                        "\"sport\":\"%s\",\"startDate\":\"%s\",\"captainId\":%d,\"captainName\":\"%s\",\"playerCount\":%d}",
                        rs.getInt("team_id"),
                        JsonUtil.escapeJson(rs.getString("name")),
                        rs.getInt("tournament_id"),
                        JsonUtil.escapeJson(rs.getString("tournament_name")),
                        JsonUtil.escapeJson(rs.getString("sport")),
                        JsonUtil.escapeJson(rs.getString("start_date")),
                        rs.getInt("captain_id"),
                        JsonUtil.escapeJson(rs.getString("captain_name")),
                        rs.getInt("player_count")
                    );
                    items.add(json);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "[" + String.join(",", items) + "]";
    }

    public boolean createTournament(String name, int venueId, String sport, Date startDate, Date endDate, String format, int createdBy) {
        String query = "INSERT INTO tournaments (name, venue_id, sport, start_date, end_date, format, created_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setInt(2, venueId);
            stmt.setString(3, sport);
            stmt.setDate(4, startDate);
            stmt.setDate(5, endDate);
            stmt.setString(6, format);
            stmt.setInt(7, createdBy);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Registers a team inside a transaction. The tournament row is locked with
     * SELECT ... FOR UPDATE so that two captains registering at the same moment
     * cannot both pass the capacity check and exceed max_teams.
     */
    public boolean registerTeam(int tournamentId, String teamName, int captainId) {
        String lockSql = "SELECT max_teams FROM tournaments WHERE tournament_id = ? FOR UPDATE";
        String countSql = "SELECT COUNT(*) FROM teams WHERE tournament_id = ?";
        String dupSql = "SELECT 1 FROM teams WHERE tournament_id = ? AND (captain_id = ? OR LOWER(name) = LOWER(?))";
        String insertSql = "INSERT INTO teams (tournament_id, name, captain_id, player_count) VALUES (?, ?, ?, 7)";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int max;
            try (PreparedStatement lockStmt = conn.prepareStatement(lockSql)) {
                lockStmt.setInt(1, tournamentId);
                try (ResultSet rs = lockStmt.executeQuery()) {
                    if (!rs.next()) { conn.rollback(); return false; }
                    max = rs.getInt("max_teams");
                }
            }

            try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
                countStmt.setInt(1, tournamentId);
                try (ResultSet rs = countStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) >= max) { conn.rollback(); return false; }
                }
            }

            try (PreparedStatement dupStmt = conn.prepareStatement(dupSql)) {
                dupStmt.setInt(1, tournamentId);
                dupStmt.setInt(2, captainId);
                dupStmt.setString(3, teamName);
                try (ResultSet rs = dupStmt.executeQuery()) {
                    if (rs.next()) { conn.rollback(); return false; }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, tournamentId);
                stmt.setString(2, teamName);
                stmt.setInt(3, captainId);
                stmt.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    /**
     * Generates round-1 fixtures by randomly pairing registered teams.
     * Refuses to run if fixtures already exist or fewer than 2 teams are registered,
     * and schedules matches on a court belonging to the tournament's own venue.
     */
    public boolean generateBracket(int tournamentId) {
        List<Integer> teamIds = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int courtId = -1;
            String lockSql = "SELECT (SELECT MIN(c.court_id) FROM courts c WHERE c.venue_id = t.venue_id) AS court_id " +
                             "FROM tournaments t WHERE t.tournament_id = ? FOR UPDATE";
            try (PreparedStatement lockStmt = conn.prepareStatement(lockSql)) {
                lockStmt.setInt(1, tournamentId);
                try (ResultSet rs = lockStmt.executeQuery()) {
                    if (!rs.next()) { conn.rollback(); return false; }
                    courtId = rs.getInt("court_id");
                    if (rs.wasNull()) { conn.rollback(); return false; }
                }
            }

            try (PreparedStatement existsStmt = conn.prepareStatement("SELECT 1 FROM matches WHERE tournament_id = ? LIMIT 1")) {
                existsStmt.setInt(1, tournamentId);
                try (ResultSet rs = existsStmt.executeQuery()) {
                    if (rs.next()) { conn.rollback(); return false; }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement("SELECT team_id FROM teams WHERE tournament_id = ?")) {
                stmt.setInt(1, tournamentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) teamIds.add(rs.getInt("team_id"));
                }
            }
            if (teamIds.size() < 2) { conn.rollback(); return false; }

            Collections.shuffle(teamIds);

            String insertMatchQuery = "INSERT INTO matches (tournament_id, team1_id, team2_id, court_id, scheduled_at) VALUES (?, ?, ?, ?, NOW())";
            try (PreparedStatement matchStmt = conn.prepareStatement(insertMatchQuery)) {
                for (int i = 0; i < teamIds.size() - 1; i += 2) {
                    matchStmt.setInt(1, tournamentId);
                    matchStmt.setInt(2, teamIds.get(i));
                    matchStmt.setInt(3, teamIds.get(i + 1));
                    matchStmt.setInt(4, courtId);
                    matchStmt.addBatch();
                }
                matchStmt.executeBatch();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}
