package com.slotit.models;

public class Booking {
    private int bookingId;
    private int userId;
    private int slotId;
    private String bookingTimestamp;
    private double amountPaid;
    private String paymentStatus;

    // Transient fields for display
    private String venueName;
    private String courtName;
    private String slotDate;
    private String startTime;
    private String endTime;
    private String sportType;

    public Booking() {}

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getSlotId() { return slotId; }
    public void setSlotId(int slotId) { this.slotId = slotId; }

    public String getBookingTimestamp() { return bookingTimestamp; }
    public void setBookingTimestamp(String bookingTimestamp) { this.bookingTimestamp = bookingTimestamp; }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }

    public String getSlotDate() { return slotDate; }
    public void setSlotDate(String slotDate) { this.slotDate = slotDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getSportType() { return sportType; }
    public void setSportType(String sportType) { this.sportType = sportType; }

    public String toJson() {
        return String.format(java.util.Locale.US,
            "{\"bookingId\":%d,\"userId\":%d,\"slotId\":%d,\"bookingTimestamp\":\"%s\",\"amountPaid\":%.2f,\"paymentStatus\":\"%s\",\"venueName\":\"%s\",\"courtName\":\"%s\",\"slotDate\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\",\"sportType\":\"%s\"}",
            bookingId, userId, slotId, esc(bookingTimestamp), amountPaid, esc(paymentStatus), esc(venueName), esc(courtName), esc(slotDate), esc(startTime), esc(endTime), esc(sportType)
        );
    }

    private static String esc(String s) {
        return com.slotit.utils.JsonUtil.escapeJson(s);
    }
}
