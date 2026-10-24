package com.slotit.models;

import java.sql.Timestamp;

/**
 * User model representing the users table.
 */
public class User {
    private int userId;
    private String name;
    private String email;
    private String phone;
    private String passwordHash;
    private String role;
    private double walletBalance;
    private int streakCount;
    private int totalBookings;
    private String badge;
    private Timestamp createdAt;

    // Constructors
    public User() {}

    public User(int userId, String name, String email, String phone, String passwordHash, 
                String role, double walletBalance, int streakCount, int totalBookings, 
                String badge, Timestamp createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
        this.walletBalance = walletBalance;
        this.streakCount = streakCount;
        this.totalBookings = totalBookings;
        this.badge = badge;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(double walletBalance) { this.walletBalance = walletBalance; }

    public int getStreakCount() { return streakCount; }
    public void setStreakCount(int streakCount) { this.streakCount = streakCount; }

    public int getTotalBookings() { return totalBookings; }
    public void setTotalBookings(int totalBookings) { this.totalBookings = totalBookings; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    /**
     * Simple JSON representation of the User.
     * Escaping quotes might be necessary for proper JSON strings.
     * @return JSON string
     */
    public String toJson() {
        return "{" +
                "\"userId\":" + userId + "," +
                "\"name\":\"" + escapeJson(name) + "\"," +
                "\"email\":\"" + escapeJson(email) + "\"," +
                "\"phone\":\"" + escapeJson(phone) + "\"," +
                "\"role\":\"" + escapeJson(role) + "\"," +
                "\"walletBalance\":" + String.format(java.util.Locale.US, "%.2f", walletBalance) + "," +
                "\"streakCount\":" + streakCount + "," +
                "\"totalBookings\":" + totalBookings + "," +
                "\"badge\":\"" + escapeJson(badge) + "\"," +
                "\"createdAt\":\"" + (createdAt != null ? createdAt.toString() : "") + "\"" +
                "}";
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
