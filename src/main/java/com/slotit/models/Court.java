package com.slotit.models;

public class Court {
    private int courtId;
    private int venueId;
    private String courtName;
    private double hourlyRate;
    private double peakMultiplier;
    private boolean isActive;

    public Court() {}

    public Court(int courtId, int venueId, String courtName, double hourlyRate, double peakMultiplier, boolean isActive) {
        this.courtId = courtId;
        this.venueId = venueId;
        this.courtName = courtName;
        this.hourlyRate = hourlyRate;
        this.peakMultiplier = peakMultiplier;
        this.isActive = isActive;
    }

    public int getCourtId() { return courtId; }
    public void setCourtId(int courtId) { this.courtId = courtId; }

    public int getVenueId() { return venueId; }
    public void setVenueId(int venueId) { this.venueId = venueId; }

    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }

    public double getPeakMultiplier() { return peakMultiplier; }
    public void setPeakMultiplier(double peakMultiplier) { this.peakMultiplier = peakMultiplier; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }

    public String toJson() {
        return String.format(java.util.Locale.US,
            "{\"courtId\":%d,\"venueId\":%d,\"courtName\":\"%s\",\"hourlyRate\":%.2f,\"peakMultiplier\":%.2f,\"isActive\":%b}",
            courtId, venueId, com.slotit.utils.JsonUtil.escapeJson(courtName), hourlyRate, peakMultiplier, isActive
        );
    }
}
