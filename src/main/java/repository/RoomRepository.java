package repository;

import db.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Room;

public class RoomRepository extends DBContext {

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

    public List<Room> findByCinemaId(int cinemaId) {

        List<Room> rooms = new ArrayList<>();

        String sql =
                "SELECT room_id, cinema_id, room_name, "
                + "capacity, room_type, status "
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

    public Room findByIdAndCinema(
            int roomId,
            int cinemaId) {

        String sql =
                "SELECT room_id, cinema_id, room_name, "
                + "capacity, room_type, status "
                + "FROM Room "
                + "WHERE room_id = ? "
                + "AND cinema_id = ?";

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

    public boolean existsRoomName(
            int cinemaId,
            String roomName,
            Integer excludeRoomId) {

        String sql;

        if (excludeRoomId == null) {

            sql =
                    "SELECT room_id "
                    + "FROM Room "
                    + "WHERE cinema_id = ? "
                    + "AND room_name = ?";

        } else {

            sql =
                    "SELECT room_id "
                    + "FROM Room "
                    + "WHERE cinema_id = ? "
                    + "AND room_name = ? "
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

    // Giữ lại để không ảnh hưởng code cũ.
    public boolean insert(Room room) {

        String sql =
                "INSERT INTO Room "
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

    // =========================================================
    // TẠO PHÒNG + SƠ ĐỒ GHẾ
    // rowCount và seatsPerRow do Manager nhập.
    // =========================================================
    public boolean insertWithSeats(
            Room room,
            int rowCount,
            int seatsPerRow) {

        boolean oldAutoCommit = true;

        try {

            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            String roomSql =
                    "INSERT INTO Room "
                    + "(cinema_id, room_name, capacity, room_type, status) "
                    + "OUTPUT INSERTED.room_id "
                    + "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement roomPs =
                    conn.prepareStatement(roomSql);

            roomPs.setInt(1, room.getCinemaId());
            roomPs.setString(2, room.getRoomName());
            roomPs.setInt(3, room.getCapacity());
            roomPs.setString(4, room.getRoomType());
            roomPs.setString(5, room.getStatus());

            ResultSet rs = roomPs.executeQuery();

            if (!rs.next()) {

                conn.rollback();
                return false;
            }

            int roomId = rs.getInt("room_id");

            String seatSql =
                    "INSERT INTO Seat "
                    + "(room_id, seat_row, seat_number, "
                    + "seat_type, status) "
                    + "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement seatPs =
                    conn.prepareStatement(seatSql);

            for (int row = 1;
                    row <= rowCount;
                    row++) {

                for (int number = 1;
                        number <= seatsPerRow;
                        number++) {

                    seatPs.setInt(1, roomId);
                    seatPs.setInt(2, row);
                    seatPs.setInt(3, number);
                    seatPs.setString(4, "STANDARD");
                    seatPs.setString(5, "ACTIVE");

                    seatPs.addBatch();
                }
            }

            seatPs.executeBatch();

            conn.commit();

            return true;

        } catch (Exception e) {

            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            e.printStackTrace();
            return false;

        } finally {

            try {
                conn.setAutoCommit(oldAutoCommit);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean update(Room room) {

        String sql =
                "UPDATE Room "
                + "SET room_name = ?, "
                + "capacity = ?, "
                + "room_type = ?, "
                + "status = ? "
                + "WHERE room_id = ? "
                + "AND cinema_id = ?";

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

    public int countSeats(int roomId) {

        String sql =
                "SELECT COUNT(*) AS total "
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

    // =========================================================
    // Đồng bộ Room.capacity với số ghế thực tế.
    // Gọi sau khi thêm hoặc hard delete Seat.
    // =========================================================
    public boolean syncCapacityFromSeats(
            int roomId) {

        int seatCount = countSeats(roomId);

        // Schema hiện tại yêu cầu capacity > 0.
        if (seatCount <= 0) {
            return false;
        }

        String sql =
                "UPDATE Room "
                + "SET capacity = ? "
                + "WHERE room_id = ?";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, seatCount);
            ps.setInt(2, roomId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean hasFutureActiveShowtime(
            int roomId) {

        String sql =
                "SELECT TOP 1 showtime_id "
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
                    Timestamp.valueOf(
                            LocalDateTime.now()
                    )
            );

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean hasAnyShowtime(
            int roomId) {

        String sql =
                "SELECT TOP 1 showtime_id "
                + "FROM Showtime "
                + "WHERE room_id = ?";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, roomId);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean hasSeatTicketHistory(
            int roomId) {

        String sql =
                "SELECT TOP 1 t.ticket_id "
                + "FROM Ticket t "
                + "JOIN Seat s "
                + "ON t.seat_id = s.seat_id "
                + "WHERE s.room_id = ?";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, roomId);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean delete(
            int roomId,
            int cinemaId) {

        String sql =
                "DELETE FROM Room "
                + "WHERE room_id = ? "
                + "AND cinema_id = ?";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, roomId);
            ps.setInt(2, cinemaId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteRoomWithSeats(
            int roomId,
            int cinemaId) {

        boolean oldAutoCommit = true;

        try {

            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            String seatSql =
                    "DELETE FROM Seat "
                    + "WHERE room_id = ?";

            PreparedStatement seatPs =
                    conn.prepareStatement(seatSql);

            seatPs.setInt(1, roomId);
            seatPs.executeUpdate();

            String roomSql =
                    "DELETE FROM Room "
                    + "WHERE room_id = ? "
                    + "AND cinema_id = ?";

            PreparedStatement roomPs =
                    conn.prepareStatement(roomSql);

            roomPs.setInt(1, roomId);
            roomPs.setInt(2, cinemaId);

            int affected =
                    roomPs.executeUpdate();

            if (affected == 0) {

                conn.rollback();
                return false;
            }

            conn.commit();

            return true;

        } catch (Exception e) {

            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            e.printStackTrace();
            return false;

        } finally {

            try {
                conn.setAutoCommit(oldAutoCommit);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Room mapRoom(
            ResultSet rs)
            throws Exception {

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