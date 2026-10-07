package repository;

import db.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Room;

public class RoomRepository extends DBContext {

    // Method cũ - giữ lại để không ảnh hưởng module khác.
    public List<Room> getAllActive() {

        List<Room> rooms = new ArrayList<>();

        String sql = "SELECT * FROM Room WHERE status = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "ACTIVE");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                rooms.add(mapRoom(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rooms;
    }

    // Get all rooms of one cinema.
    public List<Room> findByCinemaId(int cinemaId) {

        List<Room> rooms = new ArrayList<>();

        String sql = "SELECT room_id, cinema_id, room_name, capacity, "
                + "room_type, status "
                + "FROM Room "
                + "WHERE cinema_id = ? "
                + "ORDER BY room_name";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, cinemaId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                rooms.add(mapRoom(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rooms;
    }

    // Find room only when it belongs to the manager's cinema.
    public Room findByIdAndCinema(int roomId, int cinemaId) {

        String sql = "SELECT room_id, cinema_id, room_name, capacity, "
                + "room_type, status "
                + "FROM Room "
                + "WHERE room_id = ? AND cinema_id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, roomId);
            ps.setInt(2, cinemaId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRoom(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean existsRoomName(int cinemaId, String roomName,
            Integer excludeRoomId) {

        String sql;

        if (excludeRoomId == null) {
            sql = "SELECT room_id FROM Room "
                    + "WHERE cinema_id = ? AND room_name = ?";
        } else {
            sql = "SELECT room_id FROM Room "
                    + "WHERE cinema_id = ? AND room_name = ? "
                    + "AND room_id <> ?";
        }

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, cinemaId);
            ps.setString(2, roomName);

            if (excludeRoomId != null) {
                ps.setInt(3, excludeRoomId);
            }

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean insert(Room room) {

        String sql = "INSERT INTO Room "
                + "(cinema_id, room_name, capacity, room_type, status) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, room.getCinemaId());
            ps.setString(2, room.getRoomName());
            ps.setInt(3, room.getCapacity());
            ps.setString(4, room.getRoomType());
            ps.setString(5, room.getStatus());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(Room room) {

        String sql = "UPDATE Room "
                + "SET room_name = ?, capacity = ?, "
                + "room_type = ?, status = ? "
                + "WHERE room_id = ? AND cinema_id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, room.getRoomName());
            ps.setInt(2, room.getCapacity());
            ps.setString(3, room.getRoomType());
            ps.setString(4, room.getStatus());
            ps.setInt(5, room.getRoomId());
            ps.setInt(6, room.getCinemaId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Number of seats already configured for this room.
    public int countSeats(int roomId) {

        String sql = "SELECT COUNT(*) AS total "
                + "FROM Seat "
                + "WHERE room_id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, roomId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    // Check future active showtimes before disabling a room.
    public boolean hasFutureActiveShowtime(int roomId) {

        String sql = "SELECT showtime_id "
                + "FROM Showtime "
                + "WHERE room_id = ? "
                + "AND status = ? "
                + "AND start_time > ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, roomId);
            ps.setString(2, "ACTIVE");
            ps.setTimestamp(
                    3,
                    Timestamp.valueOf(LocalDateTime.now())
            );

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private Room mapRoom(ResultSet rs) throws Exception {

        return new Room(
                rs.getInt("room_id"),
                rs.getInt("cinema_id"),
                rs.getString("room_name"),
                rs.getInt("capacity"),
                rs.getString("room_type"),
                rs.getString("status")
        );
    }
}