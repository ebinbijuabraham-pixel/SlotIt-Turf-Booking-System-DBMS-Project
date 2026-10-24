package com.slotit.dao;

import com.slotit.models.Venue;
import com.slotit.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Venue entity.
 */
public class VenueDAO {

    private static final String MIN_PRICE_EXPR =
        "COALESCE((SELECT MIN(c.hourly_rate) FROM courts c WHERE c.venue_id = v.venue_id AND c.is_active = TRUE), 500.00) AS min_price";

    /**
     * Returns all venues ordered by rating descending.
     */
    public List<Venue> findAll() {
        List<Venue> venues = new ArrayList<>();
        String sql = "SELECT v.*, " + MIN_PRICE_EXPR + " FROM venues v ORDER BY v.rating DESC, v.venue_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                venues.add(mapRowToVenue(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return venues;
    }

    /**
     * Finds nearby venues using the Haversine formula in SQL.
     */
    public List<Venue> findNearby(double latitude, double longitude, double radiusKm) {
        List<Venue> venues = new ArrayList<>();
        String sql = "SELECT v.*, " + MIN_PRICE_EXPR + ", " +
                "( 6371 * acos( LEAST(1.0, GREATEST(-1.0, cos( radians(?) ) * cos( radians( v.latitude ) ) " +
                "* cos( radians( v.longitude ) - radians(?) ) + sin( radians(?) ) " +
                "* sin( radians( v.latitude ) ) )) ) ) AS distance " +
                "FROM venues v " +
                "HAVING distance <= ? " +
                "ORDER BY distance";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, latitude);
            stmt.setDouble(2, longitude);
            stmt.setDouble(3, latitude);
            stmt.setDouble(4, radiusKm);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Venue venue = mapRowToVenue(rs);
                    venue.setDistance(rs.getDouble("distance"));
                    venues.add(venue);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return venues;
    }

    /**
     * Finds a venue by ID.
     */
    public Venue findById(int venueId) {
        String sql = "SELECT v.*, " + MIN_PRICE_EXPR + " FROM venues v WHERE v.venue_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, venueId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToVenue(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds venues by sport type.
     */
    public List<Venue> findBySport(String sportType) {
        return executeListQuery("SELECT v.*, " + MIN_PRICE_EXPR + " FROM venues v WHERE LOWER(v.sport_type) = LOWER(?)", sportType);
    }

    /**
     * Finds venues by city.
     */
    public List<Venue> findByCity(String city) {
        return executeListQuery("SELECT v.*, " + MIN_PRICE_EXPR + " FROM venues v WHERE LOWER(v.city) = LOWER(?)", city);
    }

    private List<Venue> executeListQuery(String sql, String param) {
        List<Venue> venues = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, param);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    venues.add(mapRowToVenue(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return venues;
    }

    private Venue mapRowToVenue(ResultSet rs) throws SQLException {
        Venue venue = new Venue();
        venue.setVenueId(rs.getInt("venue_id"));
        venue.setOwnerId(rs.getInt("owner_id"));
        venue.setName(rs.getString("name"));
        venue.setAddress(rs.getString("address"));
        venue.setCity(rs.getString("city"));
        venue.setLatitude(rs.getDouble("latitude"));
        venue.setLongitude(rs.getDouble("longitude"));
        venue.setSportType(rs.getString("sport_type"));
        venue.setDescription(rs.getString("description"));
        venue.setRating(rs.getDouble("rating"));
        venue.setTotalRatings(rs.getInt("total_ratings"));
        venue.setImageUrl(rs.getString("image_url"));
        venue.setOutdoor(rs.getBoolean("is_outdoor"));
        venue.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            venue.setMinPrice(rs.getDouble("min_price"));
        } catch (SQLException ignored) {}
        return venue;
    }
}
