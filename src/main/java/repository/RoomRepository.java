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

    /** Đọc các phòng đang hoạt động. */
    public List<Room> getAllActive() {

        List<Room> rooms = new ArrayList<>();

        String sql = "SELECT * "
                + "FROM Room "
                + "WHERE status = ?";

        try {
            //Chuẩn bị SQL.
            PreparedStatement ps = conn.prepareStatement(sql);
            //Gán ACTIVE vào dấu ? đầu tiên.
            ps.setString(1, "ACTIVE");
            //Chạy truy vấn.
            ResultSet rs = ps.executeQuery();
            //Duyệt từng dòng kết quả.
            //Chuyển dòng dữ liệu SQL thành đối tượng Room.
            //Thêm phòng vào danh sách.
            while (rs.next()) {
                rooms.add(mapRoom(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rooms;
    }

    /** Đọc các phòng của rạp. */
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

    /** Tìm phòng theo mã phòng và mã rạp. */
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

    /** Kiểm tra tên phòng trùng trong cùng rạp. */
    public boolean existsRoomName(
            int cinemaId,
            String roomName,
            Integer excludeRoomId) {

        String sql;
        //Khi tạo phòng, excludeRoomId là null, nên SQL tìm bất kỳ phòng nào có cùng tên trong cùng rạp.
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

    
    /** Thêm bản ghi phòng. */
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
            //Nếu INSERT thành công và có dòng bị ảnh hưởng thì trả true.
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
    /** Tạo phòng và ghế trong cùng một transaction. */
    public boolean insertWithSeats(
            Room room,
            int rowCount,
            int seatsPerRow) {
        //Nếu tạo phòng thành công nhưng tạo ghế lỗi, có thể rollback cả phòng vừa tạo.
        //Biến giữ cấu hình commit trước đó của connection.
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
            //Vì SQL có OUTPUT INSERTED.room_id, câu INSERT trả về dữ liệu ID để code đọc bằng ResultSet.
            ResultSet rs = roomPs.executeQuery();
            //Nếu không lấy được dòng kết quả chứa ID phòng, rollback rồi báo thất bại.
            if (!rs.next()) {

                conn.rollback();
                return false;
            }
            //Lấy ID phòng vừa được tạo.
            //Ví dụ database tạo phòng mới với roomId = 15. Các ghế tiếp theo phải được gán room_id = 15.
            int roomId = rs.getInt("room_id");

            String seatSql =
                    "INSERT INTO Seat "
                    + "(room_id, seat_row, seat_number, "
                    + "seat_type, status) "
                    + "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement seatPs =
                    conn.prepareStatement(seatSql);
            //Duyệt từng hàng ghế.
            for (int row = 1;
                    row <= rowCount;
                    row++) {
                //Duyệt từng ghế trong mỗi hàng.
                for (int number = 1;
                        number <= seatsPerRow;
                        number++) {

                    seatPs.setInt(1, roomId);
                    seatPs.setInt(2, row);
                    seatPs.setInt(3, number);
                    seatPs.setString(4, "STANDARD");
                    seatPs.setString(5, "ACTIVE");
                    //Thêm câu INSERT ghế hiện tại vào danh sách thao tác chờ thực thi.
                    seatPs.addBatch();
                }
            }
            //Thực thi các câu INSERT ghế đã thêm bằng addBatch().
            //Cách này giúp gửi các lệnh INSERT theo batch, thay vì gọi thực thi riêng cho từng ghế.
            seatPs.executeBatch();
            //Xác nhận transaction tạo phòng và ghế.
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
            //Sau khi xử lý, code cố gắng khôi phục chế độ AutoCommit ban đầu.
            try {
                conn.setAutoCommit(oldAutoCommit);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /** Cập nhật thông tin phòng. */
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

    /** Đếm số ghế của phòng. */
    public int countSeats(int roomId) {

        String sql =
                "SELECT COUNT(*) AS total "
                + "FROM Seat "
                + "WHERE room_id = ?";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);
            //Trả tổng số ghế.
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
    /** Đồng bộ sức chứa theo số ghế thực tế. */
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

    /** Kiểm tra phòng có lịch chiếu tương lai. */
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
            //Nếu tồn tại ít nhất một suất chiếu thỏa điều kiện thì trả true.
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /** Kiểm tra phòng đã từng có lịch chiếu. 
     * Khác với hàm phía trên, câu SQL này không kiểm tra trạng thái hay ngày.
     * Chỉ cần phòng từng có bất kỳ suất chiếu nào thì rs.next() trả true.
     * 
     * Hàm này được dùng để chặn:
     *      - Đổi loại phòng đã có lịch sử chiếu.
     *      - Xóa phòng đã có lịch sử chiếu.
     */
    public boolean hasAnyShowtime(
            int roomId) {
        
        String sql =
                "SELECT TOP 1 showtime_id "//Chỉ cần tìm thấy một suất chiếu là đủ.
                + "FROM Showtime "
                + "WHERE room_id = ?";//Chỉ tìm các suất chiếu thuộc phòng cần kiểm tra.

        try {
            //Dùng kết nối database conn để chuẩn bị thực hiện SQL.
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, roomId);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /** Kiểm tra ghế trong phòng có lịch sử vé. */
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

    /** Xóa bản ghi phòng. 
     *Đây là hàm xóa bản ghi Room đơn lẻ.
     */
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

    /** Xóa ghế và phòng trong cùng giao dịch. */
    public boolean deleteRoomWithSeats(
            int roomId,
            int cinemaId) {

        boolean oldAutoCommit = true;

        try {
            //Lưu cấu hình cũ và tắt AutoCommit.
            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            //Xóa những ghế có room_id bằng phòng cần xóa.
            String seatSql =
                    "DELETE FROM Seat "
                    + "WHERE room_id = ?";
            
            PreparedStatement seatPs =
                    conn.prepareStatement(seatSql);
            //Gắn ID và chạy DELETE.
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
            //Lấy số dòng Room bị xóa.
            int affected =
                    roomPs.executeUpdate();
            //Nếu không xóa được phòng, rollback cả thao tác xóa ghế vừa thực hiện.
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

    /** Chuyển dòng SQL thành đối tượng phòng. 
     *
     *Nhờ mapRoom(), các hàm như getAllActive(), findByCinemaId() và findByIdAndCinema() không phải lặp lại đoạn chuyển dữ liệu.
     */
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