package com.slotit.models;

public class TimeSlot {
    private int slotId;
    private int courtId;
    private String slotDate;
    private String startTime;
    private String endTime;
    private String status;
    private boolean isPeak;

    // Transient fields
    private String courtName;
    private double hourlyRate;
    private double peakMultiplier;
    private double finalPrice;

    public TimeSlot() {}

    public int getSlotId() { return slotId; }
    public void setSlotId(int slotId) { this.slotId = slotId; }

    public int getCourtId() { return courtId; }
    public void setCourtId(int courtId) { this.courtId = courtId; }

    public String getSlotDate() { return slotDate; }
    public void setSlotDate(String slotDate) { this.slotDate = slotDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isPeak() { return isPeak; }
    public void setPeak(boolean isPeak) { this.isPeak = isPeak; }

    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }

    public double getPeakMultiplier() { return peakMultiplier; }
    public void setPeakMultiplier(double peakMultiplier) { this.peakMultiplier = peakMultiplier; }

    public double getFinalPrice() { return finalPrice; }
    public void setFinalPrice(double finalPrice) { this.finalPrice = finalPrice; }

    public String toJson() {
        return String.format(java.util.Locale.US,
            "{\"slotId\":%d,\"courtId\":%d,\"slotDate\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\",\"status\":\"%s\",\"isPeak\":%b,\"courtName\":\"%s\",\"hourlyRate\":%.2f,\"peakMultiplier\":%.2f,\"finalPrice\":%.2f}",
            slotId, courtId, esc(slotDate), esc(startTime), esc(endTime), esc(status), isPeak, esc(courtName), hourlyRate, peakMultiplier, finalPrice
        );
    }

    private static String esc(String s) {
        return com.slotit.utils.JsonUtil.escapeJson(s);
    }
}
