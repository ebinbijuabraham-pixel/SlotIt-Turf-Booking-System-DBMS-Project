package com.slotit.dao;

import com.slotit.models.Court;
import com.slotit.models.TimeSlot;
import com.slotit.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SlotDAO {

    /**
     * Returns all courts for a given venue.
     */
    public List<Court> getCourtsByVenue(int venueId) {
        List<Court> courts = new ArrayList<>();
        String sql = "SELECT court_id, venue_id, court_name, hourly_rate, peak_multiplier, is_active " +
                     "FROM courts WHERE venue_id = ? ORDER BY court_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, venueId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Court c = new Court(
                        rs.getInt("court_id"),
                        rs.getInt("venue_id"),
                        rs.getString("court_name"),
                        rs.getDouble("hourly_rate"),
                        rs.getDouble("peak_multiplier"),
                        rs.getBoolean("is_active")
                    );
                    courts.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courts;
    }

    /**
     * Fetches slots for a venue and date. If no slots exist yet for that venue/date,
     * automatically seeds standard hourly slots (06:00 to 21:00) for all active courts
     * of the venue so the calendar always has real database rows.
     */
    public List<TimeSlot> getSlotsByVenueAndDate(int venueId, String date) {
        List<TimeSlot> slots = querySlots(venueId, date);
        if (slots.isEmpty()) {
            ensureSlotsForVenueDate(venueId, date);
            slots = querySlots(venueId, date);
        }
        return slots;
    }

    private List<TimeSlot> querySlots(int venueId, String date) {
        List<TimeSlot> slots = new ArrayList<>();
        String sql = "SELECT ts.*, c.court_name, c.hourly_rate, c.peak_multiplier " +
                     "FROM time_slots ts JOIN courts c ON ts.court_id = c.court_id " +
                     "WHERE c.venue_id = ? AND ts.slot_date = ? " +
                     "ORDER BY c.court_id, ts.start_time";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, venueId);
            stmt.setString(2, date);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    slots.add(mapResultSetToTimeSlot(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return slots;
    }

    private void ensureSlotsForVenueDate(int venueId, String date) {
        List<Court> courts = getCourtsByVenue(venueId);
        if (courts.isEmpty()) return;

        int[] hours = {6, 7, 8, 9, 10, 16, 17, 18, 19, 20, 21};
        String insertSql = "INSERT IGNORE INTO time_slots (court_id, slot_date, start_time, end_time, status, is_peak) " +
                           "VALUES (?, ?, ?, ?, 'available', ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            for (Court c : courts) {
                if (!c.isActive()) continue;
                for (int h : hours) {
                    String start = String.format("%02d:00:00", h);
                    String end = String.format("%02d:00:00", h + 1);
                    boolean isPeak = (h >= 18 && h <= 20);
                    stmt.setInt(1, c.getCourtId());
                    stmt.setString(2, date);
                    stmt.setString(3, start);
                    stmt.setString(4, end);
                    stmt.setBoolean(5, isPeak);
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public TimeSlot getSlotById(int slotId) {
        String sql = "SELECT ts.*, c.court_name, c.hourly_rate, c.peak_multiplier " +
                     "FROM time_slots ts JOIN courts c ON ts.court_id = c.court_id " +
                     "WHERE ts.slot_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, slotId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTimeSlot(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * KEY METHOD for transaction concurrency.
     * Uses SELECT ... FOR UPDATE to lock the row.
     */
    public TimeSlot lockAndCheckSlot(Connection conn, int slotId) throws SQLException {
        String sql = "SELECT ts.*, c.court_name, c.hourly_rate, c.peak_multiplier " +
                     "FROM time_slots ts JOIN courts c ON ts.court_id = c.court_id " +
                     "WHERE ts.slot_id = ? FOR UPDATE";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, slotId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    TimeSlot slot = mapResultSetToTimeSlot(rs);
                    if ("available".equalsIgnoreCase(slot.getStatus())) {
                        return slot;
                    }
                }
            }
        }
        return null;
    }

    private TimeSlot mapResultSetToTimeSlot(ResultSet rs) throws SQLException {
        TimeSlot slot = new TimeSlot();
        slot.setSlotId(rs.getInt("slot_id"));
        slot.setCourtId(rs.getInt("court_id"));
        slot.setSlotDate(rs.getString("slot_date"));
        slot.setStartTime(rs.getString("start_time"));
        slot.setEndTime(rs.getString("end_time"));
        slot.setStatus(rs.getString("status"));
        slot.setPeak(rs.getBoolean("is_peak"));

        slot.setCourtName(rs.getString("court_name"));
        slot.setHourlyRate(rs.getDouble("hourly_rate"));
        slot.setPeakMultiplier(rs.getDouble("peak_multiplier"));

        double finalPrice = slot.isPeak() ?
            (slot.getHourlyRate() * slot.getPeakMultiplier()) :
            slot.getHourlyRate();
        slot.setFinalPrice(finalPrice);

        return slot;
    }
}
