package com.slotit.models;

import com.slotit.utils.JsonUtil;
import java.sql.Timestamp;
import java.util.Locale;

/**
 * Venue model representing the venues table.
 */
public class Venue {
    private int venueId;
    private int ownerId;
    private String name;
    private String address;
    private String city;
    private double latitude;
    private double longitude;
    private String sportType;
    private String description;
    private double rating;
    private int totalRatings;
    private String imageUrl;
    private boolean isOutdoor;
    private Timestamp createdAt;

    // Transient fields (calculated by SQL joins / Haversine)
    private transient double distance;
    private transient double minPrice = 500.0;

    public Venue() {}

    public Venue(int venueId, int ownerId, String name, String address, String city,
                 double latitude, double longitude, String sportType, String description,
                 double rating, int totalRatings, String imageUrl, boolean isOutdoor,
                 Timestamp createdAt) {
        this.venueId = venueId;
        this.ownerId = ownerId;
        this.name = name;
        this.address = address;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sportType = sportType;
        this.description = description;
        this.rating = rating;
        this.totalRatings = totalRatings;
        this.imageUrl = imageUrl;
        this.isOutdoor = isOutdoor;
        this.createdAt = createdAt;
    }

    public int getVenueId() { return venueId; }
    public void setVenueId(int venueId) { this.venueId = venueId; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getSportType() { return sportType; }
    public void setSportType(String sportType) { this.sportType = sportType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getTotalRatings() { return totalRatings; }
    public void setTotalRatings(int totalRatings) { this.totalRatings = totalRatings; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isOutdoor() { return isOutdoor; }
    public void setOutdoor(boolean isOutdoor) { this.isOutdoor = isOutdoor; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public double getMinPrice() { return minPrice; }
    public void setMinPrice(double minPrice) { this.minPrice = minPrice; }

    public String toJson() {
        return String.format(Locale.US,
            "{\"id\":%d,\"venueId\":%d,\"ownerId\":%d,\"name\":\"%s\",\"address\":\"%s\",\"city\":\"%s\"," +
            "\"latitude\":%.8f,\"longitude\":%.8f,\"sportType\":\"%s\",\"description\":\"%s\"," +
            "\"rating\":%.2f,\"totalRatings\":%d,\"imageUrl\":\"%s\",\"isOutdoor\":%b," +
            "\"distance\":%.2f,\"minPrice\":%.2f,\"hourlyRate\":%.2f,\"createdAt\":\"%s\"}",
            venueId, venueId, ownerId,
            JsonUtil.escapeJson(name),
            JsonUtil.escapeJson(address),
            JsonUtil.escapeJson(city),
            latitude, longitude,
            JsonUtil.escapeJson(sportType),
            JsonUtil.escapeJson(description),
            rating, totalRatings,
            JsonUtil.escapeJson(imageUrl),
            isOutdoor, distance, minPrice, minPrice,
            createdAt != null ? JsonUtil.escapeJson(createdAt.toString()) : ""
        );
    }
}
