package com.slotit.dao;

import com.slotit.models.Booking;
import com.slotit.models.TimeSlot;
import com.slotit.utils.DBConnection;
import com.slotit.utils.JsonUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingDAO {

    private SlotDAO slotDAO = new SlotDAO();

    /**
     * THE CORE TRANSACTIONAL METHOD
     * Implements manual transaction control (ACID) and pessimistic row-level locking (SELECT ... FOR UPDATE).
     */
    public Map<String, Object> createBooking(int userId, int slotId) {
        Map<String, Object> result = new HashMap<>();
        Connection conn = null;

        try {
            // 1. Get a connection
            conn = DBConnection.getConnection();

            // ATOMICITY & ISOLATION: Start transaction
            // 2. Set auto-commit to FALSE
            conn.setAutoCommit(false);

            // 3. Call slotDAO.lockAndCheckSlot() to lock the time_slot row
            // Uses SELECT ... FOR UPDATE internally (Pessimistic Locking)
            TimeSlot slot = slotDAO.lockAndCheckSlot(conn, slotId);

            // 4. Check availability
            if (slot == null) {
                conn.rollback();
                result.put("success", false);
                result.put("message", "This slot was just booked by someone else or is unavailable!");
                return result;
            }

            // 5. Get final price
            double finalPrice = slot.getFinalPrice();

            // 6. Check user's wallet_balance with FOR UPDATE lock
            String checkWalletSql = "SELECT wallet_balance, total_bookings FROM users WHERE user_id = ? FOR UPDATE";
            double currentBalance = 0;
            int totalBookings = 0;
            try (PreparedStatement checkWalletStmt = conn.prepareStatement(checkWalletSql)) {
                checkWalletStmt.setInt(1, userId);
                try (ResultSet rs = checkWalletStmt.executeQuery()) {
                    if (rs.next()) {
                        currentBalance = rs.getDouble("wallet_balance");
                        totalBookings = rs.getInt("total_bookings");
                    } else {
                        conn.rollback();
                        result.put("success", false);
                        result.put("message", "User not found");
                        return result;
                    }
                }
            }

            // 7. Check if sufficient balance
            if (currentBalance < finalPrice) {
                conn.rollback();
                result.put("success", false);
                result.put("message", String.format(Locale.US, "Insufficient wallet balance (Have ₹%.0f, Need ₹%.0f)", currentBalance, finalPrice));
                return result;
            }

            // 8. Deduct from wallet and update total_bookings + badge
            int newTotal = totalBookings + 1;
            String newBadge = newTotal >= 25 ? "platinum" : (newTotal >= 10 ? "gold" : (newTotal >= 5 ? "silver" : "none"));
            String deductWalletSql = "UPDATE users SET wallet_balance = wallet_balance - ?, total_bookings = ?, badge = ? WHERE user_id = ?";
            try (PreparedStatement deductStmt = conn.prepareStatement(deductWalletSql)) {
                deductStmt.setDouble(1, finalPrice);
                deductStmt.setInt(2, newTotal);
                deductStmt.setString(3, newBadge);
                deductStmt.setInt(4, userId);
                deductStmt.executeUpdate();
            }

            // 9. Mark slot as booked
            String updateSlotSql = "UPDATE time_slots SET status = 'booked' WHERE slot_id = ?";
            try (PreparedStatement updateSlotStmt = conn.prepareStatement(updateSlotSql)) {
                updateSlotStmt.setInt(1, slotId);
                updateSlotStmt.executeUpdate();
            }

            // 10. Insert booking
            String insertBookingSql = "INSERT INTO bookings (user_id, slot_id, amount_paid, payment_status) VALUES (?, ?, ?, 'completed')";
            int newBookingId = -1;
            try (PreparedStatement insertBookingStmt = conn.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS)) {
                insertBookingStmt.setInt(1, userId);
                insertBookingStmt.setInt(2, slotId);
                insertBookingStmt.setDouble(3, finalPrice);
                insertBookingStmt.executeUpdate();
                try (ResultSet generatedKeys = insertBookingStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newBookingId = generatedKeys.getInt(1);
                    }
                }
            }

            // 11. COMMIT (Atomicity - All operations succeed together)
            conn.commit();

            result.put("success", true);
            result.put("bookingId", newBookingId);
            result.put("newWalletBalance", currentBalance - finalPrice);
            result.put("message", "Booking confirmed!");

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            result.put("success", false);
            result.put("message", "Database transaction error: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public List<Booking> getUserBookings(int userId, String type) {
        List<Booking> bookings = new ArrayList<>();
        boolean isPast = "past".equalsIgnoreCase(type);
        String condition = isPast
            ? "(ts.slot_date < CURDATE() OR b.payment_status = 'refunded')"
            : "(ts.slot_date >= CURDATE() AND b.payment_status != 'refunded')";
        String sql = "SELECT b.*, ts.slot_date, ts.start_time, ts.end_time, c.court_name, v.name as venue_name, v.sport_type " +
                     "FROM bookings b " +
                     "JOIN time_slots ts ON b.slot_id = ts.slot_id " +
                     "JOIN courts c ON ts.court_id = c.court_id " +
                     "JOIN venues v ON c.venue_id = v.venue_id " +
                     "WHERE b.user_id = ? AND " + condition +
                     " ORDER BY ts.slot_date " + (isPast ? "DESC" : "ASC") + ", ts.start_time";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Booking b = new Booking();
                    b.setBookingId(rs.getInt("booking_id"));
                    b.setUserId(rs.getInt("user_id"));
                    b.setSlotId(rs.getInt("slot_id"));
                    b.setBookingTimestamp(rs.getString("booking_timestamp"));
                    b.setAmountPaid(rs.getDouble("amount_paid"));
                    b.setPaymentStatus(rs.getString("payment_status"));
                    b.setVenueName(rs.getString("venue_name"));
                    b.setCourtName(rs.getString("court_name"));
                    b.setSlotDate(rs.getString("slot_date"));
                    b.setStartTime(rs.getString("start_time"));
                    b.setEndTime(rs.getString("end_time"));
                    b.setSportType(rs.getString("sport_type"));
                    bookings.add(b);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public boolean cancelBooking(int bookingId, int userId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Verify booking belongs to user, is not already refunded, and slot is not in the past
            String getBookingSql = "SELECT b.slot_id, b.amount_paid, b.payment_status, ts.slot_date " +
                                   "FROM bookings b JOIN time_slots ts ON b.slot_id = ts.slot_id " +
                                   "WHERE b.booking_id = ? AND b.user_id = ? AND ts.slot_date >= CURDATE() FOR UPDATE";
            int slotId = -1;
            double amountPaid = 0;
            String paymentStatus = "";

            try (PreparedStatement stmt = conn.prepareStatement(getBookingSql)) {
                stmt.setInt(1, bookingId);
                stmt.setInt(2, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        slotId = rs.getInt("slot_id");
                        amountPaid = rs.getDouble("amount_paid");
                        paymentStatus = rs.getString("payment_status");
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            if ("refunded".equalsIgnoreCase(paymentStatus)) {
                conn.rollback();
                return false;
            }

            // Update payment status to refunded
            String refundBookingSql = "UPDATE bookings SET payment_status = 'refunded' WHERE booking_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(refundBookingSql)) {
                stmt.setInt(1, bookingId);
                stmt.executeUpdate();
            }

            // Update slot back to available (triggers trg_slot_drop_notify if waitlist exists)
            String freeSlotSql = "UPDATE time_slots SET status = 'available' WHERE slot_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(freeSlotSql)) {
                stmt.setInt(1, slotId);
                stmt.executeUpdate();
            }

            // Refund wallet
            String refundWalletSql = "UPDATE users SET wallet_balance = wallet_balance + ? WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(refundWalletSql)) {
                stmt.setDouble(1, amountPaid);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public String getOwnerDashboard(int ownerId) {
        int todayBookingCount = 0;
        double weekRevenue = 0.0;
        List<String> recentBookings = new ArrayList<>();
        Map<String, Double> dailyRevenue = new LinkedHashMap<>();

        try (Connection conn = DBConnection.getConnection()) {
            // todayBookingCount
            String countSql = "SELECT COUNT(*) FROM bookings b JOIN time_slots ts ON b.slot_id = ts.slot_id JOIN courts c ON ts.court_id = c.court_id JOIN venues v ON c.venue_id = v.venue_id WHERE v.owner_id = ? AND ts.slot_date = CURDATE() AND b.payment_status = 'completed'";
            try (PreparedStatement stmt = conn.prepareStatement(countSql)) {
                stmt.setInt(1, ownerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) todayBookingCount = rs.getInt(1);
                }
            }

            // weekRevenue
            String revSql = "SELECT COALESCE(SUM(b.amount_paid), 0) FROM bookings b JOIN time_slots ts ON b.slot_id = ts.slot_id JOIN courts c ON ts.court_id = c.court_id JOIN venues v ON c.venue_id = v.venue_id WHERE v.owner_id = ? AND b.payment_status = 'completed' AND ts.slot_date BETWEEN DATE_SUB(CURDATE(), INTERVAL 7 DAY) AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)";
            try (PreparedStatement stmt = conn.prepareStatement(revSql)) {
                stmt.setInt(1, ownerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) weekRevenue = rs.getDouble(1);
                }
            }

            // recentBookings (last 10)
            String recentSql = "SELECT b.booking_id, u.name, ts.slot_date, ts.start_time, c.court_name, v.name as venue_name, b.amount_paid, b.payment_status " +
                               "FROM bookings b JOIN users u ON b.user_id = u.user_id " +
                               "JOIN time_slots ts ON b.slot_id = ts.slot_id " +
                               "JOIN courts c ON ts.court_id = c.court_id " +
                               "JOIN venues v ON c.venue_id = v.venue_id " +
                               "WHERE v.owner_id = ? ORDER BY b.booking_timestamp DESC LIMIT 10";
            try (PreparedStatement stmt = conn.prepareStatement(recentSql)) {
                stmt.setInt(1, ownerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String rb = String.format(Locale.US,
                            "{\"bookingId\":%d,\"username\":\"%s\",\"date\":\"%s\",\"time\":\"%s\",\"court\":\"%s\",\"venue\":\"%s\",\"amount\":%.2f,\"status\":\"%s\"}",
                            rs.getInt("booking_id"),
                            JsonUtil.escapeJson(rs.getString("name")),
                            JsonUtil.escapeJson(rs.getString("slot_date")),
                            JsonUtil.escapeJson(rs.getString("start_time")),
                            JsonUtil.escapeJson(rs.getString("court_name")),
                            JsonUtil.escapeJson(rs.getString("venue_name")),
                            rs.getDouble("amount_paid"),
                            JsonUtil.escapeJson(rs.getString("payment_status"))
                        );
                        recentBookings.add(rb);
                    }
                }
            }

            // dailyRevenue
            String dailySql = "SELECT ts.slot_date, SUM(b.amount_paid) as total FROM bookings b JOIN time_slots ts ON b.slot_id = ts.slot_id JOIN courts c ON ts.court_id = c.court_id JOIN venues v ON c.venue_id = v.venue_id WHERE v.owner_id = ? AND b.payment_status = 'completed' GROUP BY ts.slot_date ORDER BY ts.slot_date LIMIT 7";
            try (PreparedStatement stmt = conn.prepareStatement(dailySql)) {
                stmt.setInt(1, ownerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        dailyRevenue.put(rs.getString("slot_date"), rs.getDouble("total"));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        StringBuilder dailyRevJson = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Double> entry : dailyRevenue.entrySet()) {
            if (!first) dailyRevJson.append(",");
            dailyRevJson.append("\"").append(JsonUtil.escapeJson(entry.getKey())).append("\":")
                        .append(String.format(Locale.US, "%.2f", entry.getValue()));
            first = false;
        }
        dailyRevJson.append("}");

        return String.format(Locale.US,
            "{\"todayBookingCount\":%d,\"weekRevenue\":%.2f,\"recentBookings\":[%s],\"dailyRevenue\":%s}",
            todayBookingCount, weekRevenue, String.join(",", recentBookings), dailyRevJson.toString()
        );
    }
}
