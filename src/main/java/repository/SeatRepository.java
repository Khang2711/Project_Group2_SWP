
package repository;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import model.Seat;

public class SeatRepository extends DBContext {

    /** Đọc tất cả ghế trong phòng. */
    public List<Seat> findByRoomId(int roomId) {
        List<Seat> seats = new ArrayList<>();

        String sql = "SELECT seat_id, room_id, "
                + "seat_row, seat_number, seat_type, status "
                + "FROM Seat WHERE room_id = ? "
                + "ORDER BY seat_row, seat_number";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            //Duyệt kết quả SQL, chuyển từng dòng thành đối tượng Seat, rồi đưa vào danh sách.
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapSeat(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return seats;
    }

    /** Tìm ghế theo mã ghế và phòng. */
    public Seat findByIdAndRoom(int seatId, int roomId) {
        String sql = "SELECT seat_id, room_id, "
                + "seat_row, seat_number, seat_type, status "
                + "FROM Seat WHERE seat_id = ? AND room_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seatId);
            ps.setInt(2, roomId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSeat(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /** Kiểm tra vị trí ghế có bị trùng. */
    public boolean existsPosition(
            int roomId, int seatRow, int seatNumber,
            Integer excludeSeatId) {

        String sql = "SELECT TOP 1 seat_id FROM Seat "
                + "WHERE room_id = ? AND seat_row = ? "
                + "AND seat_number = ?";
        //Loại trừ chính ghế đang sửa để tránh báo trùng với bản thân nó.
        if (excludeSeatId != null) {
            sql += " AND seat_id <> ?";
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setInt(2, seatRow);
            ps.setInt(3, seatNumber);

            if (excludeSeatId != null) {
                ps.setInt(4, excludeSeatId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Không thể kiểm tra vị trí ghế.", e);
        }
    }

    /** Đếm số ghế trong phòng. */
    public int countSeats(int roomId) {
        String sql = "SELECT COUNT(*) FROM Seat WHERE room_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Không thể đếm số ghế.", e);
        }

        return 0;
    }

    /** Lấy số hàng ghế cuối cùng. */
    public int getMaxRow(int roomId) {
        String sql = "SELECT COALESCE(MAX(seat_row), 0) "
                + "FROM Seat WHERE room_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Không thể xác định hàng ghế.", e);
        }

        return 0;
    }

    /** Kiểm tra ghế từng có vé chưa. 
     * Chặn sửa ghế đã có vé
     * SELECT TOP 1: Chỉ cần tìm thấy một vé.
     * FROM Ticket: Kiểm tra bảng vé.
     * WHERE seat_id = ?: Kiểm tra vé của ghế đang xét.
     */
    public boolean hasTicketHistory(int seatId) {
        String sql = "SELECT TOP 1 ticket_id "
                + "FROM Ticket WHERE seat_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seatId);
            //true: Ghế đã từng có vé.
            //false: Chưa tìm thấy vé.
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Không thể kiểm tra lịch sử vé.", e);
        }
    }

    /** Thêm một ghế vào database. */
    public boolean insert(Seat seat) {
        String sql = "INSERT INTO Seat "
                + "(room_id, seat_row, seat_number, "
                + "seat_type, status) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seat.getRoomId());
            ps.setInt(2, seat.getSeatRow());
            ps.setInt(3, seat.getSeatNumber());
            ps.setString(4, seat.getSeatType());
            ps.setString(5, seat.getStatus());
            //Nếu thêm đúng một bản ghi thì trả true.
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================================================
    // GIỮ HÀM CŨ - THÊM HÀNG GHẾ
    // =====================================================

    /** Thêm các ghế của một hàng mới. */
    public boolean insertRow(
            int roomId, int seatRow,
            int seatCount, String seatType) {

        String sql = "INSERT INTO Seat "
                + "(room_id, seat_row, seat_number, "
                + "seat_type, status) "
                + "VALUES (?, ?, ?, ?, 'ACTIVE')";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int number = 1; number <= seatCount; number++) {
                ps.setInt(1, roomId);
                ps.setInt(2, seatRow);
                ps.setInt(3, number);
                ps.setString(4, seatType);
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            return batchSucceeded(results, seatCount);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================================================
    // THÊM HÀNG GHẾ MỚI
    // Có transaction và đồng bộ capacity.
    // =====================================================

    /** Thêm hàng ghế và cập nhật sức chứa cùng giao dịch. */
    public boolean insertRowAtomic(
            int roomId, int seatRow,
            int seatCount, String seatType) {
        //Kiểm tra ID phòng, hàng ghế, số ghế và loại ghế có hợp lệ không.
        validateInsertInput(roomId, seatRow, seatCount, seatType);
        //Mở kết nối database mới để thực hiện giao dịch.
        //setAutoCommit(false) giúp các thao tác thêm ghế và cập nhật sức chứa nằm trong cùng transaction.
        try (Connection connection = openWriteConnection()) {
            connection.setAutoCommit(false);
            //Khóa phòng
            //Đây là bước rất quan trọng để kiểm soát các thao tác thay đổi ghế đồng thời.
            //Ví dụ: hai Manager cùng thao tác thêm ghế vào một phòng, khóa giúp các transaction sử dụng cơ chế khóa này không cùng sửa phòng đó một cách tùy tiện.
            try {
                lockRoom(connection, roomId);
                //Lấy hàng lớn nhất trong phòng.
                int maxRow = getMaxRowInTransaction(
                        connection, roomId);

                if (seatRow != maxRow + 1 || seatRow > 30) {
                    throw new SQLException(
                            "Hàng mới phải nằm sau hàng cuối.");
                }
                //check 500 seat
                checkRoomCapacity(connection, roomId, seatCount);
                //Tạo các ghế bắt đầu từ số 1 của hàng mới.
                insertSeatRange(
                        connection, roomId, seatRow,
                        1, seatCount, seatType);
                //syncCapacity(): Đếm lại ghế và cập nhật Room.capacity.
                syncCapacity(connection, roomId);

                connection.commit();
                return true;

            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Không thể thêm hàng ghế: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // THÊM NHIỀU GHẾ VÀO HÀNG ĐÃ TỒN TẠI.
    //
    // Ví dụ hàng 2 có ghế 1..8, thêm 5 ghế
    // thì tạo ghế 9..13 trong chính hàng 2.
    //
    // - Không đổi ID ghế cũ.
    // - Không thay đổi vé đã bán.
    // - Không thêm trùng vị trí.
    // - Giới hạn ghế số 1..50.
    // - Rollback nếu bất kỳ ghế nào lỗi.
    // =====================================================

    /** Nối ghế vào hàng và cập nhật sức chứa cùng giao dịch. */
    public boolean appendSeatsToRowAtomic(
            int roomId,
            int seatRow,
            int seatCount,
            String seatType) {

        validateInsertInput(roomId, seatRow, seatCount, seatType);
        //Mở transaction và khóa phòng
        try (Connection connection = openWriteConnection()) {
            connection.setAutoCommit(false);

            try {
                lockRoom(connection, roomId);

                int lastNumber = -1;
                //Tìm số ghế cuối của hàng
                String rowSql =
                        "SELECT MAX(seat_number) AS last_number "
                        + "FROM Seat "
                        + "WHERE room_id = ? AND seat_row = ?";

                try (PreparedStatement ps =
                        connection.prepareStatement(rowSql)) {

                    ps.setInt(1, roomId);
                    ps.setInt(2, seatRow);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            lastNumber = rs.getInt("last_number");

                            if (rs.wasNull()) {
                                lastNumber = -1;
                            }
                        }
                    }
                }

                // Không tự tạo hàng mới nếu hàng chưa có ghế thì không cho dùng chức năng này..
                if (lastNumber < 1) {
                    throw new SQLException(
                            "Hàng ghế chưa tồn tại. "
                            + "Vui lòng dùng chức năng Thêm hàng.");
                }

                if (lastNumber + seatCount > 50) {
                    throw new SQLException(
                            "Hàng ghế chỉ được đánh số tối đa 50.");
                }
                //Kiểm tra tổng ghế của phòng sau khi thêm có vượt 500 không.
                checkRoomCapacity(
                        connection, roomId, seatCount);
                //Xác định ghế đầu tiên cần tạo
                int firstNewNumber = lastNumber + 1;

                insertSeatRange(
                        connection,
                        roomId,
                        seatRow,
                        firstNewNumber,
                        seatCount,
                        seatType
                );
                //Nếu bất kỳ bước nào phát sinh lỗi, giao dịch được rollback.
                syncCapacity(connection, roomId);

                connection.commit();
                return true;

            } catch (Exception e) {
                connection.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Không thể thêm ghế vào hàng: "
                    + e.getMessage(), e);
        }
    }

    // =====================================================
    // CẬP NHẬT MỘT GHẾ
    // =====================================================

    /** Cập nhật thông tin ghế. */
    public boolean update(Seat seat) {
        String sql = "UPDATE Seat SET seat_row = ?, "
                + "seat_number = ?, seat_type = ?, status = ? "
                + "WHERE seat_id = ? AND room_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seat.getSeatRow());
            ps.setInt(2, seat.getSeatNumber());
            ps.setString(3, seat.getSeatType());
            ps.setString(4, seat.getStatus());
            ps.setInt(5, seat.getSeatId());
            ps.setInt(6, seat.getRoomId());

            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Đổi trạng thái ghế. */
    public boolean updateStatus(
            int seatId, int roomId, String status) {

        String sql = "UPDATE Seat SET status = ? "
                + "WHERE seat_id = ? AND room_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, seatId);
            ps.setInt(3, roomId);

            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Sửa nhiều ghế trong database. */
    public int updateBulk(
            List<Integer> seatIds,
            int roomId,
            String seatType,
            String status) {

        if (seatIds == null || seatIds.isEmpty()
                || (seatType == null && status == null)) {
            return 0;
        }

        StringBuilder sql =
                new StringBuilder("UPDATE Seat SET ");

        boolean previous = false;
        //Nếu Manager chọn sửa loại ghế thì thêm phần cập nhật seat_type.
        if (seatType != null) {
            sql.append("seat_type = ?");
            previous = true;
        }
        //Nếu Manager chọn sửa trạng thái thì thêm phần cập nhật status.
        if (status != null) {
            if (previous) {
                sql.append(", ");
            }
            sql.append("status = ?");
        }

        sql.append(" WHERE room_id = ? AND seat_id IN (");
        sql.append(placeholders(seatIds.size()));
        sql.append(")");

        try (PreparedStatement ps =
                conn.prepareStatement(sql.toString())) {

            int index = 1;

            if (seatType != null) {
                ps.setString(index++, seatType);
            }

            if (status != null) {
                ps.setString(index++, status);
            }

            ps.setInt(index++, roomId);

            for (Integer seatId : seatIds) {
                ps.setInt(index++, seatId);
            }

            return ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /** Xóa một ghế. */
    public boolean delete(int seatId, int roomId) {
        String sql = "DELETE FROM Seat "
                + "WHERE seat_id = ? AND room_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seatId);
            ps.setInt(2, roomId);

            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================================================
    // XÓA NHIỀU GHẾ TRONG MỘT TRANSACTION
    // =====================================================

    /** Xóa nhiều ghế và cập nhật sức chứa cùng giao dịch. */
    public boolean deleteSeatsAtomic(
            List<Integer> seatIds, int roomId) {
        //Không cho danh sách rỗng hoặc mã phòng không hợp lệ.
        if (roomId <= 0 || seatIds == null
                || seatIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Danh sách ghế không hợp lệ.");
        }
        //Loại bỏ ID ghế bị trùng.
        Set<Integer> ids = new LinkedHashSet<>(seatIds);

        for (Integer seatId : ids) {
            if (seatId == null || seatId <= 0) {
                throw new IllegalArgumentException(
                        "ID ghế không hợp lệ.");
            }
        }

        String marks = placeholders(ids.size());
        //Tạo câu SQL khóa các ghế
        String lockSeatsSql =
                "SELECT seat_id FROM Seat WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE room_id = ? AND seat_id IN (" + marks + ")";

        String historySql =
                "SELECT TOP 1 t.ticket_id FROM Ticket t "
                + "JOIN Seat s ON t.seat_id = s.seat_id "
                + "WHERE s.room_id = ? "
                + "AND s.seat_id IN (" + marks + ")";

        String deleteSql =
                "DELETE FROM Seat WHERE room_id = ? "
                + "AND seat_id IN (" + marks + ")";
        //Mở transaction và khóa phòng
        try (Connection connection = openWriteConnection()) {
            connection.setAutoCommit(false);

            try {
                lockRoom(connection, roomId);

                try (PreparedStatement ps =
                        connection.prepareStatement(lockSeatsSql)) {

                    bindSeatIds(ps, roomId, ids);

                    int found = 0;

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            found++;
                        }
                    }
                    //Kiểm tra các ghế còn tồn tại.
                    if (found != ids.size()) {
                        throw new SQLException(
                                "Danh sách ghế đã thay đổi.");
                    }
                }

                try (PreparedStatement ps =
                        connection.prepareStatement(historySql)) {

                    bindSeatIds(ps, roomId, ids);
                    //Nếu bất kỳ ghế nào đã có vé, không cho xóa.
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new SQLException(
                                    "Không thể xóa ghế đã có vé.");
                        }
                    }
                }

                int total = countSeatsInTransaction(
                        connection, roomId);
                //Không cho xóa hết ghế
                if (total - ids.size() <= 0) {
                    throw new SQLException(
                            "Phòng phải còn ít nhất một ghế.");
                }

                try (PreparedStatement ps =
                        connection.prepareStatement(deleteSql)) {

                    bindSeatIds(ps, roomId, ids);
                    //Nếu không xóa đủ số lượng thì báo lỗi để rollback.
                    if (ps.executeUpdate() != ids.size()) {
                        throw new SQLException(
                                "Không thể xóa đầy đủ các ghế.");
                    }
                }
                //Đồng bộ sức chứa và commit
                syncCapacity(connection, roomId);
                connection.commit();
                return true;

            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Không thể xóa ghế: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // CÁC HÀM HỖ TRỢ TRANSACTION
    // =====================================================

    /** Mở kết nối để thực hiện giao dịch ghi. */
    private Connection openWriteConnection()
            throws SQLException {

        Connection connection = new DBContext().getConnection();

        if (connection == null || connection.isClosed()) {
            throw new SQLException(
                    "Không thể kết nối FCinemaDB.");
        }

        return connection;
    }

    /** Khóa phòng khi thay đổi ghế. */
    private void lockRoom(
            Connection connection, int roomId)
            throws SQLException {

        String sql = "SELECT room_id FROM Room "
                + "WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE room_id = ?";

        try (PreparedStatement ps =
                connection.prepareStatement(sql)) {

            ps.setInt(1, roomId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "Không tìm thấy phòng.");
                }
            }
        }
    }

    /** Kiểm tra dữ liệu trước khi thêm ghế. */
    private void validateInsertInput(
            int roomId, int seatRow,
            int seatCount, String seatType) {

        if (roomId <= 0 || seatRow <= 0 || seatRow > 30
                || seatCount <= 0 || seatCount > 50) {

            throw new IllegalArgumentException(
                    "Số hàng hoặc số ghế không hợp lệ.");
        }

        if (!"STANDARD".equals(seatType)
                && !"VIP".equals(seatType)
                && !"COUPLE".equals(seatType)) {

            throw new IllegalArgumentException(
                    "Loại ghế không hợp lệ.");
        }
    }

    /** Lấy hàng cuối trong giao dịch. */
    private int getMaxRowInTransaction(
            Connection connection, int roomId)
            throws SQLException {

        String sql = "SELECT COALESCE(MAX(seat_row), 0) "
                + "FROM Seat WHERE room_id = ?";

        try (PreparedStatement ps =
                connection.prepareStatement(sql)) {

            ps.setInt(1, roomId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    /** Đếm ghế trong giao dịch. */
    private int countSeatsInTransaction(
            Connection connection, int roomId)
            throws SQLException {

        String sql = "SELECT COUNT(*) FROM Seat WHERE room_id = ?";

        try (PreparedStatement ps =
                connection.prepareStatement(sql)) {

            ps.setInt(1, roomId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Không thể đếm ghế.");
    }

    /** Kiểm tra giới hạn số ghế phòng. */
    private void checkRoomCapacity(
            Connection connection, int roomId,
            int seatsToAdd) throws SQLException {

        int current = countSeatsInTransaction(
                connection, roomId);

        if (current + seatsToAdd > 500) {
            throw new SQLException(
                    "Một phòng không được vượt quá 500 ghế.");
        }
    }

    /** Thêm dãy ghế theo số thứ tự. */
    private void insertSeatRange(
            Connection connection,
            int roomId,
            int seatRow,
            int firstNumber,
            int seatCount,
            String seatType) throws SQLException {

        String sql = "INSERT INTO Seat "
                + "(room_id, seat_row, seat_number, "
                + "seat_type, status) "
                + "VALUES (?, ?, ?, ?, 'ACTIVE')";

        try (PreparedStatement ps =
                connection.prepareStatement(sql)) {

            for (int i = 0; i < seatCount; i++) {
                ps.setInt(1, roomId);
                ps.setInt(2, seatRow);
                ps.setInt(3, firstNumber + i);
                ps.setString(4, seatType);
                ps.addBatch();
            }

            int[] results = ps.executeBatch();

            if (!batchSucceeded(results, seatCount)) {
                throw new SQLException(
                        "Không thể thêm đầy đủ các ghế.");
            }
        }
    }

    /** Cập nhật sức chứa theo số ghế thực tế. */
    private void syncCapacity(
            Connection connection, int roomId)
            throws SQLException {

        String sql = "UPDATE Room SET capacity = "
                + "(SELECT COUNT(*) FROM Seat WHERE room_id = ?) "
                + "WHERE room_id = ?";

        try (PreparedStatement ps =
                connection.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ps.setInt(2, roomId);

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                        "Không thể đồng bộ sức chứa.");
            }
        }
    }

    /** Kiểm tra tất cả lệnh SQL đã thành công. */
    private boolean batchSucceeded(
            int[] results, int expectedCount) {

        if (results.length != expectedCount) {
            return false;
        }

        for (int result : results) {
            if (result == 0
                    || result == Statement.EXECUTE_FAILED) {
                return false;
            }
        }

        return true;
    }

    /** Tạo dấu ? cho câu SQL nhiều tham số. */
    private String placeholders(int count) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("?");
        }

        return sb.toString();
    }

    /** Gán các mã ghế vào câu SQL. */
    private void bindSeatIds(
            PreparedStatement ps, int roomId,
            Set<Integer> ids) throws SQLException {

        ps.setInt(1, roomId);

        int index = 2;

        for (Integer id : ids) {
            ps.setInt(index++, id);
        }
    }

    /** Chuyển dòng SQL thành đối tượng ghế. */
    private Seat mapSeat(ResultSet rs)
            throws SQLException {

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
