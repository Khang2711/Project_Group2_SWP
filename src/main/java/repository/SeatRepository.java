package repository;

import db.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;
import model.Seat;

public class SeatRepository extends DBContext {

    // =========================================================
    // DANH SÁCH GHẾ CỦA PHÒNG
    // =========================================================
    public List<Seat> findByRoomId(
            int roomId) {

        List<Seat> seats =
                new ArrayList<>();

        String sql =
                "SELECT seat_id, room_id, seat_row, "
                + "seat_number, seat_type, status "
                + "FROM Seat "
                + "WHERE room_id = ? "
                + "ORDER BY seat_row, seat_number";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(
                    1,
                    roomId
            );

            ResultSet rs =
                    ps.executeQuery();

            while (rs.next()) {

                seats.add(
                        mapSeat(rs)
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return seats;
    }

    // =========================================================
    // TÌM 1 GHẾ
    // =========================================================
    public Seat findByIdAndRoom(
            int seatId,
            int roomId) {

        String sql =
                "SELECT seat_id, room_id, seat_row, "
                + "seat_number, seat_type, status "
                + "FROM Seat "
                + "WHERE seat_id = ? "
                + "AND room_id = ?";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(
                    1,
                    seatId
            );

            ps.setInt(
                    2,
                    roomId
            );

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

    // =========================================================
    // KIỂM TRA TRÙNG VỊ TRÍ
    // =========================================================
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

            ps.setInt(
                    1,
                    roomId
            );

            ps.setInt(
                    2,
                    seatRow
            );

            ps.setInt(
                    3,
                    seatNumber
            );

            if (excludeSeatId != null) {

                ps.setInt(
                        4,
                        excludeSeatId
                );
            }

            ResultSet rs =
                    ps.executeQuery();

            return rs.next();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    // =========================================================
    // ĐẾM GHẾ
    // =========================================================
    public int countSeats(
            int roomId) {

        String sql =
                "SELECT COUNT(*) AS total "
                + "FROM Seat "
                + "WHERE room_id = ?";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(
                    1,
                    roomId
            );

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {

                return rs.getInt(
                        "total"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;
    }

    // =========================================================
    // KIỂM TRA LỊCH SỬ VÉ
    // =========================================================
    public boolean hasTicketHistory(
            int seatId) {

        String sql =
                "SELECT TOP 1 ticket_id "
                + "FROM Ticket "
                + "WHERE seat_id = ?";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(
                    1,
                    seatId
            );

            ResultSet rs =
                    ps.executeQuery();

            return rs.next();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    // =========================================================
    // THÊM GHẾ
    // Giữ lại để không ảnh hưởng chức năng cũ.
    // =========================================================
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

            ps.setInt(
                    1,
                    seat.getRoomId()
            );

            ps.setInt(
                    2,
                    seat.getSeatRow()
            );

            ps.setInt(
                    3,
                    seat.getSeatNumber()
            );

            ps.setString(
                    4,
                    seat.getSeatType()
            );

            ps.setString(
                    5,
                    seat.getStatus()
            );

            return ps.executeUpdate() > 0;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    // =========================================================
    // UPDATE 1 GHẾ
    // =========================================================
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

            ps.setInt(
                    1,
                    seat.getSeatRow()
            );

            ps.setInt(
                    2,
                    seat.getSeatNumber()
            );

            ps.setString(
                    3,
                    seat.getSeatType()
            );

            ps.setString(
                    4,
                    seat.getStatus()
            );

            ps.setInt(
                    5,
                    seat.getSeatId()
            );

            ps.setInt(
                    6,
                    seat.getRoomId()
            );

            return ps.executeUpdate() > 0;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    // =========================================================
    // UPDATE NHIỀU GHẾ CÙNG LÚC
    //
    // seatType = null -> không đổi loại ghế
    // status   = null -> không đổi status
    // =========================================================
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

        boolean hasPreviousField =
                false;

        if (seatType != null) {

            sql.append(
                    "seat_type = ?"
            );

            hasPreviousField =
                    true;
        }

        if (status != null) {

            if (hasPreviousField) {

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

            for (Integer seatId
                    : seatIds) {

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

    // =========================================================
    // MAP
    // =========================================================
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