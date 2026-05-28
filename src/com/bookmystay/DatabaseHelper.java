package com.bookmystay;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper {
    private static final String DB_NAME = "bookings.db";
    private static final String CONNECTION_URL = "jdbc:sqlite:" + DB_NAME;
    private static DatabaseHelper instance;

    private DatabaseHelper() {
        try {
            // Load SQLite driver
            Class.forName("org.sqlite.JDBC");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found in classpath. Database will run in-memory mode.");
        }
    }

    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL);
    }

    private void initializeDatabase() {
        String createRoomsTable = "CREATE TABLE IF NOT EXISTS rooms (" +
                "id INTEGER PRIMARY KEY," +
                "number TEXT UNIQUE NOT NULL," +
                "type TEXT NOT NULL," +
                "price REAL NOT NULL," +
                "status TEXT DEFAULT 'Available'" +
                ");";

        String createReservationsTable = "CREATE TABLE IF NOT EXISTS reservations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "guest_name TEXT NOT NULL," +
                "guest_email TEXT NOT NULL," +
                "guest_phone TEXT NOT NULL," +
                "room_id INTEGER NOT NULL," +
                "check_in TEXT NOT NULL," +
                "check_out TEXT NOT NULL," +
                "total_price REAL NOT NULL," +
                "status TEXT DEFAULT 'Booked'," +
                "FOREIGN KEY (room_id) REFERENCES rooms(id)" +
                ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createRoomsTable);
            stmt.execute(createReservationsTable);
            
            // Seed Rooms if empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM rooms");
            if (rs.next() && rs.getInt(1) == 0) {
                seedRooms(conn);
            }
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    private void seedRooms(Connection conn) throws SQLException {
        String insertRoom = "INSERT INTO rooms (number, type, price, status) VALUES (?, ?, ?, 'Available')";
        try (PreparedStatement pstmt = conn.prepareStatement(insertRoom)) {
            // Standard Rooms ($100)
            String[][] standardRooms = {{"101", "Standard"}, {"102", "Standard"}, {"103", "Standard"}, {"104", "Standard"}, {"105", "Standard"}};
            for (String[] room : standardRooms) {
                pstmt.setString(1, room[0]);
                pstmt.setString(2, room[1]);
                pstmt.setDouble(3, 100.0);
                pstmt.addBatch();
            }

            // Deluxe Rooms ($180)
            String[][] deluxeRooms = {{"201", "Deluxe"}, {"202", "Deluxe"}, {"203", "Deluxe"}, {"204", "Deluxe"}};
            for (String[] room : deluxeRooms) {
                pstmt.setString(1, room[0]);
                pstmt.setString(2, room[1]);
                pstmt.setDouble(3, 180.0);
                pstmt.addBatch();
            }

            // Luxury Suites ($350)
            String[][] suiteRooms = {{"301", "Luxury Suite"}, {"302", "Luxury Suite"}};
            for (String[] room : suiteRooms) {
                pstmt.setString(1, room[0]);
                pstmt.setString(2, room[1]);
                pstmt.setDouble(3, 350.0);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            System.out.println("Default rooms seeded successfully.");
        }
    }

    // Fetch all rooms
    public List<Map<String, Object>> getAllRooms() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY number ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> room = new HashMap<>();
                room.put("id", rs.getInt("id"));
                room.put("number", rs.getString("number"));
                room.put("type", rs.getString("type"));
                room.put("price", rs.getDouble("price"));
                room.put("status", rs.getString("status"));
                list.add(room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Check room availability for a date range (date overlap check)
    public boolean isRoomAvailable(int roomId, String checkIn, String checkOut) {
        String sql = "SELECT COUNT(*) FROM reservations " +
                "WHERE room_id = ? " +
                "AND status IN ('Booked', 'CheckedIn') " +
                "AND (check_in < ? AND check_out > ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomId);
            pstmt.setString(2, checkOut); // existing check_in < new check_out
            pstmt.setString(3, checkIn);  // existing check_out > new check_in
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Create a new booking reservation
    public boolean createReservation(String name, String email, String phone, int roomId, 
                                     String checkIn, String checkOut, double totalPrice) {
        String sql = "INSERT INTO reservations (guest_name, guest_email, guest_phone, room_id, " +
                "check_in, check_out, total_price, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'Booked')";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setInt(4, roomId);
            pstmt.setString(5, checkIn);
            pstmt.setString(6, checkOut);
            pstmt.setDouble(7, totalPrice);
            
            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Retrieve all reservations with room numbers
    public List<Map<String, Object>> getAllReservations() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT r.*, rm.number AS room_number, rm.type AS room_type " +
                "FROM reservations r JOIN rooms rm ON r.room_id = rm.id " +
                "ORDER BY r.id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> res = new HashMap<>();
                res.put("id", rs.getInt("id"));
                res.put("guest_name", rs.getString("guest_name"));
                res.put("guest_email", rs.getString("guest_email"));
                res.put("guest_phone", rs.getString("guest_phone"));
                res.put("room_id", rs.getInt("room_id"));
                res.put("room_number", rs.getString("room_number"));
                res.put("room_type", rs.getString("room_type"));
                res.put("check_in", rs.getString("check_in"));
                res.put("check_out", rs.getString("check_out"));
                res.put("total_price", rs.getDouble("total_price"));
                res.put("status", rs.getString("status"));
                list.add(res);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Update reservation status (CheckedIn, CheckedOut, Cancelled)
    public boolean updateReservationStatus(int resId, String status) {
        String sql = "UPDATE reservations SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, resId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get statistics summary
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBookings", 0);
        stats.put("activeStays", 0);
        stats.put("totalRevenue", 0.0);
        stats.put("occupancyRate", 0);

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Total Bookings
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM reservations WHERE status != 'Cancelled'");
            if (rs.next()) stats.put("totalBookings", rs.getInt(1));
            
            // Active Stays
            rs = stmt.executeQuery("SELECT COUNT(*) FROM reservations WHERE status = 'CheckedIn'");
            if (rs.next()) stats.put("activeStays", rs.getInt(1));
            
            // Total Revenue
            rs = stmt.executeQuery("SELECT SUM(total_price) FROM reservations WHERE status = 'CheckedOut'");
            if (rs.next()) stats.put("totalRevenue", rs.getDouble(1));
            
            // Occupancy Rate calculation: occupied rooms / total rooms
            rs = stmt.executeQuery("SELECT COUNT(*) FROM rooms");
            int totalRooms = rs.next() ? rs.getInt(1) : 11;
            
            rs = stmt.executeQuery("SELECT COUNT(DISTINCT room_id) FROM reservations " +
                    "WHERE status = 'CheckedIn'");
            int occupiedRooms = rs.next() ? rs.getInt(1) : 0;
            
            int occupancy = (int) (((double) occupiedRooms / totalRooms) * 100);
            stats.put("occupancyRate", occupancy);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    // Get distribution of bookings by room type for charts
    public Map<String, Integer> getRoomTypeBookingDistribution() {
        Map<String, Integer> dist = new HashMap<>();
        dist.put("Standard", 0);
        dist.put("Deluxe", 0);
        dist.put("Luxury Suite", 0);

        String sql = "SELECT rm.type, COUNT(*) AS cnt " +
                "FROM reservations r JOIN rooms rm ON r.room_id = rm.id " +
                "WHERE r.status != 'Cancelled' " +
                "GROUP BY rm.type";
                
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                dist.put(rs.getString("type"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dist;
    }
}
