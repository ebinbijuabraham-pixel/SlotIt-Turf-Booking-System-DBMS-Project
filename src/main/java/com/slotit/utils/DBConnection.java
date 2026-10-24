package com.slotit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection utility class.
 * Returns a new connection every time.
 */
public class DBConnection {

    // Default connection parameters
    private static final String HOST = "localhost";
    private static final int PORT = 3306;
    private static final String DATABASE = "slotit_db";
    private static final String USER = setting("SLOTIT_DB_USER", "root");
    private static final String PASSWORD = setting("SLOTIT_DB_PASSWORD", "root");
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE;

    /** Looks up a setting from a -D system property, then an environment variable, then a default. */
    private static String setting(String key, String fallback) {
        String v = System.getProperty(key);
        if (v == null || v.isEmpty()) v = System.getenv(key);
        return (v == null || v.isEmpty()) ? fallback : v;
    }

    static {
        try {
            // Load MySQL JDBC driver once
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        }
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private DBConnection() {
    }

    /**
     * Returns a new active database connection.
     * @return java.sql.Connection
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            e.printStackTrace();
            return null;
        }
    }
}
