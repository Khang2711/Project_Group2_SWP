package repository;

import db.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Seat;

public class SeatRepository extends DBContext {

    public List<Seat> findByRoomId(
            int roomId) {

        List<Seat> seats =
                new ArrayList<>();

        String sql =
                "SELECT seat_id, room_id, "
                + "seat_row, seat_number, "
                + "seat_type, status "
                + "FROM Seat "
                + "WHERE room_id = ? "
                + "ORDER BY seat_row, seat_number";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(1, roomId);

            ResultSet rs =
                    ps.executeQuery();

            while (rs.next()) {
                seats.add(mapSeat(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return seats;
    }

    public Seat findByIdAndRoom(
            int seatId,
            int roomId) {

        String sql =
                "SELECT seat_id, room_id, "
                + "seat_row, seat_number, "
                + "seat_type, status "
                + "FROM Seat "
                + "WHERE seat_id = ? "
                + "AND room_id = ?";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(1, seatId);
            ps.setInt(2, roomId);

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {
                return mapSeat(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean existsPosition(
            int roomId,
            int seatRow,
            int seatNumber,
            Integer excludeSeatId) {

        String sql;

        if (excludeSeatId == null) {

            sql =
                    "SELECT seat_id "
                    + "FROM Seat "
                    + "WHERE room_id = ? "
                    + "AND seat_row = ? "
                    + "AND seat_number = ?";

        } else {

            sql =
                    "SELECT seat_id "
                    + "FROM Seat "
                    + "WHERE room_id = ? "
                    + "AND seat_row = ? "
                    + "AND seat_number = ? "
                    + "AND seat_id <> ?";
        }

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(1, roomId);
            ps.setInt(2, seatRow);
            ps.setInt(3, seatNumber);

            if (excludeSeatId != null) {
                ps.setInt(4, excludeSeatId);
            }

            ResultSet rs =
                    ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public int countSeats(
            int roomId) {

        String sql =
                "SELECT COUNT(*) AS total "
                + "FROM Seat "
                + "WHERE room_id = ?";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(1, roomId);

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int getMaxRow(
            int roomId) {

        String sql =
                "SELECT COALESCE(MAX(seat_row), 0) AS max_row "
                + "FROM Seat "
                + "WHERE room_id = ?";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(1, roomId);

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("max_row");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean hasTicketHistory(
            int seatId) {

        String sql =
                "SELECT TOP 1 ticket_id "
                + "FROM Ticket "
                + "WHERE seat_id = ?";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(1, seatId);

            ResultSet rs =
                    ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean insert(
            Seat seat) {

        String sql =
                "INSERT INTO Seat "
                + "(room_id, seat_row, seat_number, "
                + "seat_type, status) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(1, seat.getRoomId());
            ps.setInt(2, seat.getSeatRow());
            ps.setInt(3, seat.getSeatNumber());
            ps.setString(4, seat.getSeatType());
            ps.setString(5, seat.getStatus());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // =========================================================
    // Thêm nguyên một hàng mới.
    // =========================================================
    public boolean insertRow(
            int roomId,
            int seatRow,
            int seatCount,
            String seatType) {

        String sql =
                "INSERT INTO Seat "
                + "(room_id, seat_row, seat_number, "
                + "seat_type, status) "
                + "VALUES (?, ?, ?, ?, 'ACTIVE')";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            for (int number = 1;
                    number <= seatCount;
                    number++) {

                ps.setInt(1, roomId);
                ps.setInt(2, seatRow);
                ps.setInt(3, number);
                ps.setString(4, seatType);

                ps.addBatch();
            }

            ps.executeBatch();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(
            Seat seat) {

        String sql =
                "UPDATE Seat "
                + "SET seat_row = ?, "
                + "seat_number = ?, "
                + "seat_type = ?, "
                + "status = ? "
                + "WHERE seat_id = ? "
                + "AND room_id = ?";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(1, seat.getSeatRow());
            ps.setInt(2, seat.getSeatNumber());
            ps.setString(3, seat.getSeatType());
            ps.setString(4, seat.getStatus());
            ps.setInt(5, seat.getSeatId());
            ps.setInt(6, seat.getRoomId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateStatus(
            int seatId,
            int roomId,
            String status) {

        String sql =
                "UPDATE Seat "
                + "SET status = ? "
                + "WHERE seat_id = ? "
                + "AND room_id = ?";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setString(1, status);
            ps.setInt(2, seatId);
            ps.setInt(3, roomId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public int updateBulk(
            List<Integer> seatIds,
            int roomId,
            String seatType,
            String status) {

        if (seatIds == null
                || seatIds.isEmpty()) {

            return 0;
        }

        if (seatType == null
                && status == null) {

            return 0;
        }

        StringBuilder sql =
                new StringBuilder(
                        "UPDATE Seat SET "
                );

        boolean previous =
                false;

        if (seatType != null) {

            sql.append(
                    "seat_type = ?"
            );

            previous = true;
        }

        if (status != null) {

            if (previous) {
                sql.append(", ");
            }

            sql.append(
                    "status = ?"
            );
        }

        sql.append(
                " WHERE room_id = ? "
        );

        sql.append(
                "AND seat_id IN ("
        );

        for (int i = 0;
                i < seatIds.size();
                i++) {

            if (i > 0) {
                sql.append(",");
            }

            sql.append("?");
        }

        sql.append(")");

        try {

            PreparedStatement ps =
                    conn.prepareStatement(
                            sql.toString()
                    );

            int index = 1;

            if (seatType != null) {

                ps.setString(
                        index++,
                        seatType
                );
            }

            if (status != null) {

                ps.setString(
                        index++,
                        status
                );
            }

            ps.setInt(
                    index++,
                    roomId
            );

            for (Integer seatId : seatIds) {

                ps.setInt(
                        index++,
                        seatId
                );
            }

            return ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean delete(
            int seatId,
            int roomId) {

        String sql =
                "DELETE FROM Seat "
                + "WHERE seat_id = ? "
                + "AND room_id = ?";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(1, seatId);
            ps.setInt(2, roomId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private Seat mapSeat(
            ResultSet rs)
            throws Exception {

        return new Seat(
                rs.getInt("seat_id"),
                rs.getInt("room_id"),
                rs.getInt("seat_row"),
                rs.getInt("seat_number"),
                rs.getString("seat_type"),
                rs.getString("status")
        );
    }
}