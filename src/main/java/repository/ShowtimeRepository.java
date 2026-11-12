
package repository;

import db.DBContext;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import model.Showtime;

public class ShowtimeRepository extends DBContext {

    // =====================================================
    // READ OPERATIONS
    // =====================================================

    public int getCinemaIdByShowtimeId(int showtimeId) {

        String sql = "SELECT r.cinema_id "
                + "FROM dbo.Showtime s "
                + "JOIN dbo.Room r ON s.room_id = r.room_id "
                + "WHERE s.showtime_id = ?";

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cinema_id");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public List<Showtime> getAll() {

        List<Showtime> showtimes = new ArrayList<>();

        String sql = "SELECT * FROM Showtime "
                + "WHERE status = ? "
                + "ORDER BY start_time";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, "ACTIVE");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                showtimes.add(mapShowtime(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return showtimes;
    }

    public List<Showtime> getByMovieId(int movieId) {

        List<Showtime> showtimes = new ArrayList<>();

        String sql = "SELECT * FROM Showtime "
                + "WHERE movie_id = ? "
                + "AND status = ? "
                + "ORDER BY start_time";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, movieId);
            ps.setString(2, "ACTIVE");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                showtimes.add(mapShowtime(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return showtimes;
    }

    public Showtime getById(int id) {

        String sql = "SELECT * "
                + "FROM Showtime "
                + "WHERE showtime_id = ?";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapShowtime(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Showtime> getByMovieIdAndDate(int movieId, LocalDate date) {

        List<Showtime> showtimes = new ArrayList<>();

        String sql = "SELECT * "
                + "FROM Showtime "
                + "WHERE movie_id = ? "
                + "AND status = ? "
                + "AND start_time >= ? "
                + "AND start_time < ? "
                + "ORDER BY start_time";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, movieId);
            ps.setString(2, "ACTIVE");
            ps.setTimestamp(3, Timestamp.valueOf(date.atStartOfDay()));
            ps.setTimestamp(4, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                showtimes.add(mapShowtime(rs));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return showtimes;
    }

    public List<Showtime> getByCinemaId(int cinemaId) {

        List<Showtime> showtimes = new ArrayList<>();

        String sql = "SELECT s.* "
                + "FROM Showtime s "
                + "JOIN Room r ON s.room_id = r.room_id "
                + "WHERE r.cinema_id = ? "
                + "ORDER BY s.start_time DESC";

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setInt(1, cinemaId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    showtimes.add(mapShowtime(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return showtimes;
    }

    public Showtime getByIdAndCinema(
            int showtimeId,
            int cinemaId) {

        String sql = "SELECT s.* "
                + "FROM Showtime s "
                + "JOIN Room r ON s.room_id = r.room_id "
                + "WHERE s.showtime_id = ? "
                + "AND r.cinema_id = ?";

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);
            ps.setInt(2, cinemaId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapShowtime(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // =====================================================
    // BUG-08 FIX:
    // MỞ CONNECTION RIÊNG CHO TRANSACTION
    //
    // Không dùng conn được giữ trong Repository
    // cho các giao dịch ghi đồng thời.
    // =====================================================

    private Connection openWriteConnection()
            throws SQLException {

        Connection connection =
                new DBContext().getConnection();

        if (connection == null
                || connection.isClosed()) {

            throw new SQLException(
                    "Không thể kết nối FCinemaDB."
            );
        }

        return connection;
    }

    // =====================================================
    // BUG-08 FIX:
    // KHÓA NGHIỆP VỤ THEO ROOM_ID
    //
    // sp_getapplock yêu cầu SQL Server.
    // LockOwner = Transaction:
    // khóa tự giải phóng khi commit/rollback.
    //
    // Return >= 0: lấy khóa thành công.
    // Return < 0: không lấy được khóa.
    // =====================================================

    private void lockRoom(
            Connection connection,
            int roomId) throws SQLException {

        String sql = "{? = call sys.sp_getapplock(?, ?, ?, ?)}";

        try (CallableStatement cs =
                connection.prepareCall(sql)) {

            cs.registerOutParameter(1, Types.INTEGER);

            cs.setString(
                    2,
                    "FCinema_Showtime_Room_" + roomId
            );

            cs.setString(3, "Exclusive");
            cs.setString(4, "Transaction");
            cs.setInt(5, 10000);

            cs.execute();

            int result = cs.getInt(1);

            if (result < 0) {
                throw new SQLException(
                        "Không thể khóa lịch chiếu "
                        + "của phòng " + roomId
                        + ". Mã lỗi: " + result
                );
            }
        }
    }

    // =====================================================
    // KIỂM TRA DỮ LIỆU SUẤT CHIẾU
    // =====================================================

    private void validateShowtime(
            Showtime showtime) throws SQLException {

        if (showtime == null) {
            throw new SQLException(
                    "Suất chiếu không hợp lệ."
            );
        }

        if (showtime.getMovieId() <= 0
                || showtime.getRoomId() <= 0) {

            throw new SQLException(
                    "Phim hoặc phòng không hợp lệ."
            );
        }

        if (showtime.getStartTime() == null
                || showtime.getEndTime() == null) {

            throw new SQLException(
                    "Thời gian suất chiếu không hợp lệ."
            );
        }

        if (!showtime.getEndTime().after(
                showtime.getStartTime())) {

            throw new SQLException(
                    "Giờ kết thúc phải sau giờ bắt đầu."
            );
        }

        if (!"ACTIVE".equals(showtime.getStatus())
                && !"INACTIVE".equals(
                        showtime.getStatus())) {

            throw new SQLException(
                    "Trạng thái suất chiếu không hợp lệ."
            );
        }
    }

    // =====================================================
    // BUG-08 FIX:
    // KIỂM TRA TRÙNG GIỜ BẰNG CONNECTION
    // ĐANG THAM GIA TRANSACTION.
    //
    // Khoảng thời gian [start, end).
    //
    // Điều kiện trùng:
    // existing.start < new.end
    // AND existing.end > new.start
    // =====================================================

    private boolean hasConflictInTransaction(
            Connection connection,
            int roomId,
            Timestamp startTime,
            Timestamp endTime,
            Integer excludeShowtimeId)
            throws SQLException {

        String sql = "SELECT TOP 1 showtime_id "
                + "FROM Showtime WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE room_id = ? "
                + "AND status = 'ACTIVE' "
                + "AND start_time < ? "
                + "AND end_time > ? ";

        if (excludeShowtimeId != null) {
            sql += "AND showtime_id <> ?";
        }

        try (PreparedStatement ps =
                connection.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ps.setTimestamp(2, endTime);
            ps.setTimestamp(3, startTime);

            if (excludeShowtimeId != null) {
                ps.setInt(4, excludeShowtimeId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // =====================================================
    // BUG-08 FIX:
    // TẠO MỘT SUẤT CHIẾU
    //
    // Lock -> Check overlap -> Insert -> Commit
    // =====================================================

    public boolean insert(Showtime showtime) {

        String sql = "INSERT INTO Showtime "
                + "(movie_id, room_id, start_time, "
                + "end_time, status) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            validateShowtime(showtime);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        try (Connection connection =
                openWriteConnection()) {

            connection.setAutoCommit(false);

            try {

                lockRoom(
                        connection,
                        showtime.getRoomId()
                );

                if ("ACTIVE".equals(
                        showtime.getStatus())) {

                    boolean conflict =
                            hasConflictInTransaction(
                                    connection,
                                    showtime.getRoomId(),
                                    showtime.getStartTime(),
                                    showtime.getEndTime(),
                                    null
                            );

                    if (conflict) {
                        throw new SQLException(
                                "Phòng đã có suất chiếu "
                                + "trùng thời gian."
                        );
                    }
                }

                int updated;

                try (PreparedStatement ps =
                        connection.prepareStatement(sql)) {

                    setInsertParameters(ps, showtime);
                    updated = ps.executeUpdate();
                }

                if (updated != 1) {
                    throw new SQLException(
                            "Không thể thêm suất chiếu."
                    );
                }

                connection.commit();
                return true;

            } catch (Exception e) {

                connection.rollback();
                e.printStackTrace();
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================================================
    // BUG-08 FIX:
    // THÊM NHIỀU SUẤT CHIẾU
    //
    // 1. Lấy khóa tất cả phòng theo thứ tự tăng.
    // 2. Kiểm tra trùng lịch trong database.
    // 3. Kiểm tra trùng giữa các suất trong batch.
    // 4. Ghi toàn bộ trong cùng transaction.
    // 5. Có lỗi -> rollback toàn bộ.
    // =====================================================

    public boolean insertBatch(
            List<Showtime> showtimes) {

        if (showtimes == null || showtimes.isEmpty()) {
            return false;
        }

        String sql = "INSERT INTO Showtime "
                + "(movie_id, room_id, start_time, "
                + "end_time, status) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {

            for (Showtime showtime : showtimes) {
                validateShowtime(showtime);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Thứ tự cố định giúp giảm deadlock
        // giữa các request có nhiều phòng.
        TreeSet<Integer> roomIds = new TreeSet<>();

        for (Showtime showtime : showtimes) {
            roomIds.add(showtime.getRoomId());
        }

        try (Connection connection =
                openWriteConnection()) {

            connection.setAutoCommit(false);

            try {

                // Khóa toàn bộ phòng liên quan.
                for (Integer roomId : roomIds) {
                    lockRoom(connection, roomId);
                }

                // Kiểm tra từng suất với database.
                for (Showtime showtime : showtimes) {

                    if (!"ACTIVE".equals(
                            showtime.getStatus())) {
                        continue;
                    }

                    if (hasConflictInTransaction(
                            connection,
                            showtime.getRoomId(),
                            showtime.getStartTime(),
                            showtime.getEndTime(),
                            null)) {

                        throw new SQLException(
                                "Có suất chiếu trùng "
                                + "với lịch đã tồn tại."
                        );
                    }
                }

                // Kiểm tra các suất trong cùng batch.
                for (int i = 0;
                        i < showtimes.size();
                        i++) {

                    Showtime first = showtimes.get(i);

                    if (!"ACTIVE".equals(
                            first.getStatus())) {
                        continue;
                    }

                    for (int j = i + 1;
                            j < showtimes.size();
                            j++) {

                        Showtime second =
                                showtimes.get(j);

                        if (!"ACTIVE".equals(
                                second.getStatus())) {
                            continue;
                        }

                        if (first.getRoomId()
                                != second.getRoomId()) {
                            continue;
                        }

                        boolean overlap =
                                first.getStartTime().before(
                                        second.getEndTime()
                                )
                                && first.getEndTime().after(
                                        second.getStartTime()
                                );

                        if (overlap) {
                            throw new SQLException(
                                    "Ca " + (i + 1)
                                    + " và ca " + (j + 1)
                                    + " bị trùng thời gian."
                            );
                        }
                    }
                }

                try (PreparedStatement ps =
                        connection.prepareStatement(sql)) {

                    for (Showtime showtime : showtimes) {
                        setInsertParameters(ps, showtime);
                        ps.addBatch();
                    }

                    int[] results = ps.executeBatch();

                    if (results.length != showtimes.size()) {
                        throw new SQLException(
                                "Không thể thêm đầy đủ "
                                + "các suất chiếu."
                        );
                    }

                    for (int result : results) {
                        if (result == 0
                                || result
                                == java.sql.Statement.EXECUTE_FAILED) {

                            throw new SQLException(
                                    "Có suất chiếu thêm thất bại."
                            );
                        }
                    }
                }

                connection.commit();
                return true;

            } catch (Exception e) {

                connection.rollback();
                e.printStackTrace();
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================================================
    // BUG-08 FIX:
    // CẬP NHẬT SUẤT CHIẾU
    //
    // Khóa cả phòng cũ và phòng mới.
    // Kiểm tra lại xung đột trước UPDATE.
    // =====================================================

    public boolean update(
            Showtime showtime,
            int cinemaId) {

        String updateSql = "UPDATE Showtime "
                + "SET movie_id = ?, "
                + "room_id = ?, "
                + "start_time = ?, "
                + "end_time = ?, "
                + "status = ? "
                + "WHERE showtime_id = ? "
                + "AND room_id IN ("
                + "SELECT room_id FROM Room "
                + "WHERE cinema_id = ?)";

        String oldRoomSql = "SELECT s.room_id "
                + "FROM Showtime s "
                + "JOIN Room r ON s.room_id = r.room_id "
                + "WHERE s.showtime_id = ? "
                + "AND r.cinema_id = ?";

        try {
            validateShowtime(showtime);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        try (Connection connection =
                openWriteConnection()) {

            connection.setAutoCommit(false);

            try {

                int oldRoomId = 0;

                try (PreparedStatement ps =
                        connection.prepareStatement(
                                oldRoomSql)) {

                    ps.setInt(
                            1,
                            showtime.getShowtimeId()
                    );
                    ps.setInt(2, cinemaId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            oldRoomId = rs.getInt("room_id");
                        }
                    }
                }

                if (oldRoomId <= 0) {
                    throw new SQLException(
                            "Không tìm thấy suất chiếu "
                            + "thuộc rạp."
                    );
                }

                TreeSet<Integer> roomIds =
                        new TreeSet<>();

                roomIds.add(oldRoomId);
                roomIds.add(showtime.getRoomId());

                for (Integer roomId : roomIds) {
                    lockRoom(connection, roomId);
                }

                // Kiểm tra lại phòng cũ sau khi lấy khóa.
                try (PreparedStatement ps =
                        connection.prepareStatement(
                                oldRoomSql)) {

                    ps.setInt(
                            1,
                            showtime.getShowtimeId()
                    );
                    ps.setInt(2, cinemaId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()
                                || rs.getInt("room_id")
                                != oldRoomId) {

                            throw new SQLException(
                                    "Suất chiếu đã được "
                                    + "thay đổi bởi thao tác khác."
                            );
                        }
                    }
                }

                // Bảo vệ vé đã bán ngay trong transaction.
                // Giữ tương thích với BUG 01 ở Service.
                String ticketSql =
                        "SELECT TOP 1 ticket_id "
                        + "FROM Ticket "
                        + "WHERE showtime_id = ?";

                boolean hasTicket;

                try (PreparedStatement ps =
                        connection.prepareStatement(
                                ticketSql)) {

                    ps.setInt(
                            1,
                            showtime.getShowtimeId()
                    );

                    try (ResultSet rs = ps.executeQuery()) {
                        hasTicket = rs.next();
                    }
                }

                if (hasTicket) {

                    String oldDataSql =
                            "SELECT movie_id, room_id, "
                            + "start_time, end_time, status "
                            + "FROM Showtime "
                            + "WHERE showtime_id = ?";

                    try (PreparedStatement ps =
                            connection.prepareStatement(
                                    oldDataSql)) {

                        ps.setInt(
                                1,
                                showtime.getShowtimeId()
                        );

                        try (ResultSet rs = ps.executeQuery()) {

                            if (!rs.next()) {
                                throw new SQLException(
                                        "Không tìm thấy suất chiếu."
                                );
                            }

                            boolean changed =
                                    rs.getInt("movie_id")
                                    != showtime.getMovieId()
                                    || rs.getInt("room_id")
                                    != showtime.getRoomId()
                                    || !rs.getTimestamp(
                                            "start_time"
                                    ).equals(
                                            showtime.getStartTime()
                                    )
                                    || !rs.getTimestamp(
                                            "end_time"
                                    ).equals(
                                            showtime.getEndTime()
                                    )
                                    || !rs.getString("status")
                                            .equals(
                                                    showtime.getStatus()
                                            );

                            if (changed) {
                                throw new SQLException(
                                        "Không thể thay đổi "
                                        + "suất chiếu đã có vé."
                                );
                            }
                        }
                    }
                }

                if ("ACTIVE".equals(
                        showtime.getStatus())) {

                    if (hasConflictInTransaction(
                            connection,
                            showtime.getRoomId(),
                            showtime.getStartTime(),
                            showtime.getEndTime(),
                            showtime.getShowtimeId())) {

                        throw new SQLException(
                                "Phòng chiếu đã có "
                                + "lịch trùng thời gian."
                        );
                    }
                }

                int updated;

                try (PreparedStatement ps =
                        connection.prepareStatement(
                                updateSql)) {

                    ps.setInt(1, showtime.getMovieId());
                    ps.setInt(2, showtime.getRoomId());
                    ps.setTimestamp(
                            3,
                            showtime.getStartTime()
                    );
                    ps.setTimestamp(
                            4,
                            showtime.getEndTime()
                    );
                    ps.setString(
                            5,
                            showtime.getStatus()
                    );
                    ps.setInt(
                            6,
                            showtime.getShowtimeId()
                    );
                    ps.setInt(7, cinemaId);

                    updated = ps.executeUpdate();
                }

                if (updated != 1) {
                    throw new SQLException(
                            "Không thể cập nhật suất chiếu."
                    );
                }

                connection.commit();
                return true;

            } catch (Exception e) {

                connection.rollback();
                e.printStackTrace();
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================================================
    // KIỂM TRA TRÙNG LỊCH DÙNG TẠI SERVICE
    //
    // BUG FIX:
    // Không trả false khi SQL gặp lỗi.
    // Tránh cho phép ghi lịch khi DB không kiểm tra được.
    // =====================================================

    public boolean hasConflict(
            int roomId,
            Timestamp startTime,
            Timestamp endTime,
            Integer excludeShowtimeId) {

        String sql = "SELECT TOP 1 showtime_id "
                + "FROM Showtime "
                + "WHERE room_id = ? "
                + "AND status = 'ACTIVE' "
                + "AND start_time < ? "
                + "AND end_time > ? ";

        if (excludeShowtimeId != null) {
            sql += "AND showtime_id <> ?";
        }

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ps.setTimestamp(2, endTime);
            ps.setTimestamp(3, startTime);

            if (excludeShowtimeId != null) {
                ps.setInt(4, excludeShowtimeId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Không thể kiểm tra trùng lịch chiếu.",
                    e
            );
        }
    }

    // =====================================================
    // KIỂM TRA LỊCH SỬ VÉ
    //
    // BUG FIX:
    // DB lỗi => không được mặc định chưa có vé.
    // =====================================================

    public boolean hasTickets(int showtimeId) {

        String sql = "SELECT TOP 1 ticket_id "
                + "FROM Ticket "
                + "WHERE showtime_id = ?";

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Không thể kiểm tra vé của suất chiếu.",
                    e
            );
        }
    }

    // =====================================================
    // CHUYỂN SUẤT CHIẾU SANG INACTIVE
    // =====================================================

    public boolean setInactive(
            int showtimeId,
            int cinemaId) {

        String sql = "UPDATE Showtime "
                + "SET status = 'INACTIVE' "
                + "WHERE showtime_id = ? "
                + "AND room_id IN ("
                + "SELECT room_id FROM Room "
                + "WHERE cinema_id = ?)";

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);
            ps.setInt(2, cinemaId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // =====================================================
    // XÓA SUẤT CHIẾU
    // =====================================================

    public boolean delete(
            int showtimeId,
            int cinemaId) {

        String sql = "DELETE FROM Showtime "
                + "WHERE showtime_id = ? "
                + "AND room_id IN ("
                + "SELECT room_id FROM Room "
                + "WHERE cinema_id = ?)";

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);
            ps.setInt(2, cinemaId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // =====================================================
    // SQL PARAMETERS
    // =====================================================

    private void setInsertParameters(
            PreparedStatement ps,
            Showtime showtime) throws SQLException {

        ps.setInt(1, showtime.getMovieId());
        ps.setInt(2, showtime.getRoomId());
        ps.setTimestamp(3, showtime.getStartTime());
        ps.setTimestamp(4, showtime.getEndTime());
        ps.setString(5, showtime.getStatus());
    }

    // =====================================================
    // MAP DATABASE -> SHOWTIME
    // =====================================================

    private Showtime mapShowtime(
            ResultSet rs) throws SQLException {

        Showtime showtime = new Showtime();

        showtime.setShowtimeId(
                rs.getInt("showtime_id")
        );

        showtime.setMovieId(
                rs.getInt("movie_id")
        );

        showtime.setRoomId(
                rs.getInt("room_id")
        );

        showtime.setStartTime(
                rs.getTimestamp("start_time")
        );

        showtime.setEndTime(
                rs.getTimestamp("end_time")
        );

        showtime.setStatus(
                rs.getString("status")
        );

        return showtime;
    }
}
