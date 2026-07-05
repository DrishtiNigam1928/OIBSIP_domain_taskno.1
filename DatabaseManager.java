package com.reservation.db;

import com.reservation.model.Booking;

import java.sql.*;

/**
 * Handles all database access for the Reservation System.
 * Uses SQLite (file-based, no server required) and PreparedStatement
 * everywhere to prevent SQL injection.
 */
public class DatabaseManager {

    // The DB file (reservation.db) is created automatically in the working directory
    private static final String DB_URL = "jdbc:sqlite:reservation.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /** Creates tables (if they don't exist yet) and seeds sample data. */
    public static void initializeDatabase() {
        String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "username TEXT PRIMARY KEY, " +
                "password TEXT NOT NULL)";

        String createTrains = "CREATE TABLE IF NOT EXISTS trains (" +
                "train_number TEXT PRIMARY KEY, " +
                "train_name TEXT NOT NULL)";

        String createBookings = "CREATE TABLE IF NOT EXISTS bookings (" +
                "pnr TEXT PRIMARY KEY, " +
                "passenger_name TEXT NOT NULL, " +
                "train_number TEXT NOT NULL, " +
                "train_name TEXT NOT NULL, " +
                "class_type TEXT NOT NULL, " +
                "journey_date TEXT NOT NULL, " +
                "source_station TEXT NOT NULL, " +
                "destination_station TEXT NOT NULL, " +
                "booking_time TEXT NOT NULL)";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createTrains);
            stmt.execute(createBookings);
            seedData(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void seedData(Connection conn) throws SQLException {
        // Seed one default login (admin / admin123) if users table is empty
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users(username, password) VALUES (?, ?)")) {
                    ps.setString(1, "admin");
                    ps.setString(2, "admin123");
                    ps.executeUpdate();
                }
            }
        }

        // Seed a handful of sample trains if trains table is empty
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM trains")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String[][] trains = {
                        {"12301", "Howrah Rajdhani Express"},
                        {"12951", "Mumbai Rajdhani Express"},
                        {"12259", "Sealdah Duronto Express"},
                        {"12626", "Kerala Express"},
                        {"12002", "Bhopal Shatabdi Express"},
                        {"12909", "Garib Rath Express"}
                };
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO trains(train_number, train_name) VALUES (?, ?)")) {
                    for (String[] t : trains) {
                        ps.setString(1, t[0]);
                        ps.setString(2, t[1]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }
    }

    /** Returns true if username+password matches a row in users table. */
    public static boolean validateLogin(String username, String password) {
        String sql = "SELECT 1 FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Looks up a train name from its number. Returns null if not found. */
    public static String getTrainName(String trainNumber) {
        String sql = "SELECT train_name FROM trains WHERE train_number = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trainNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("train_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Generates a random 10-digit PNR that does not already exist in bookings. */
    public static String generateUniquePnr() {
        String pnr;
        do {
            long number = 1000000000L + (long) (Math.random() * 8999999999L);
            pnr = String.valueOf(number);
        } while (pnrExists(pnr));
        return pnr;
    }

    private static boolean pnrExists(String pnr) {
        String sql = "SELECT 1 FROM bookings WHERE pnr = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // fail-safe: force a regeneration attempt
        }
    }

    /** Inserts a new booking row. Returns true on success. */
    public static boolean insertBooking(Booking b) {
        String sql = "INSERT INTO bookings(pnr, passenger_name, train_number, train_name, " +
                "class_type, journey_date, source_station, destination_station, booking_time) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getPnr());
            ps.setString(2, b.getPassengerName());
            ps.setString(3, b.getTrainNumber());
            ps.setString(4, b.getTrainName());
            ps.setString(5, b.getClassType());
            ps.setString(6, b.getJourneyDate());
            ps.setString(7, b.getSourceStation());
            ps.setString(8, b.getDestinationStation());
            ps.setString(9, b.getBookingTime());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Fetches full booking details for a PNR. Returns null if not found. */
    public static Booking getBookingByPnr(String pnr) {
        String sql = "SELECT * FROM bookings WHERE pnr = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Booking(
                            rs.getString("pnr"),
                            rs.getString("passenger_name"),
                            rs.getString("train_number"),
                            rs.getString("train_name"),
                            rs.getString("class_type"),
                            rs.getString("journey_date"),
                            rs.getString("source_station"),
                            rs.getString("destination_station"),
                            rs.getString("booking_time")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Deletes a booking by PNR. Returns true if a row was removed. */
    public static boolean deleteBooking(String pnr) {
        String sql = "DELETE FROM bookings WHERE pnr = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pnr);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
