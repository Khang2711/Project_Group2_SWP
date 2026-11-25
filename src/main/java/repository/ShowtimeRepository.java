
package repository;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import model.Showtime;

public class ShowtimeRepository extends DBContext {

    // Ghi nguyên nhân lỗi và cung cấp mã SQL khi thao tác tạo lịch thất bại.
    /** Ghi log và trả thông báo khi lưu lịch lỗi. */
    private IllegalArgumentException showtimeWriteError(
            String operation, Exception error) {

        error.printStackTrace();
        Throwable cause = error;

        while (cause != null) {
            if (cause instanceof SQLException) {
                SQLException sql = (SQLException) cause;
                String detail = sql.getMessage();

                if (detail != null && detail.contains("trùng")) {
                    return new IllegalArgumentException(detail, error);
                }

                System.err.println("Showtime SQL error: "
                        + sql.getErrorCode() + ", SQLState: "
                        + sql.getSQLState() + ", " + detail);

                return new IllegalArgumentException(
                        "Không thể " + operation
                        + ". Mã SQL: " + sql.getErrorCode()
                        + ", SQLState: " + sql.getSQLState()
                        + ". Xem log Tomcat.", error);
            }
            cause = cause.getCause();
        }

        return new IllegalArgumentException(
                "Không thể " + operation + ". Xem log Tomcat.", error);
    }

    // =====================================================
    // Các hàm đọc dữ liệu
    // =====================================================

    /** Tìm rạp của một suất chiếu. */
    public int getCinemaIdByShowtimeId(int showtimeId) {
        //Tìm rạp của một suất chiếu.
        //  Showtime s: Bảng suất chiếu.
        //  Room r: Bảng phòng.
        //  JOIN: Ghép suất chiếu với phòng.
        //  r.cinema_id: Lấy ID rạp mà phòng thuộc về.
        //Ví dụ suất chiếu 15 thuộc phòng 3, phòng 3 thuộc rạp Ninh Kiều thì hàm trả về ID của rạp Ninh Kiều.
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

    /** Đọc các lịch chiếu đang hoạt động. 
     *Lấy tất cả suất chiếu ACTIVE, sắp xếp theo giờ bắt đầu.
     */
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

    /** Đọc lịch chiếu theo phim. 
     *Chỉ lấy suất chiếu ACTIVE của một phim cụ thể.
     */
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

    /** Tìm suất chiếu theo Id. 
     *Nếu tìm được thì trả đối tượng Showtime, nếu không thì trả null.
     */
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

    /** Lấy lịch chiếu theo phim và ngày. */
    public List<Showtime> getByMovieIdAndDate(int movieId, LocalDate date) {

        List<Showtime> showtimes = new ArrayList<>();
        //Đây là hàm lấy lịch chiếu của một phim trong một ngày.
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

    /** Lấy lịch chiếu của một rạp. */
    public List<Showtime> getByCinemaId(int cinemaId) {

        List<Showtime> showtimes = new ArrayList<>();
        //Hàm này không lọc theo status, nên có thể lấy cả suất ACTIVE và INACTIVE.
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

    /** Tìm suất chiếu thuộc đúng rạp. 
     *Hàm kiểm tra quyền quản lý theo rạp
     *Hệ thống chỉ trả về suất chiếu khi đúng cả ID suất chiếu và ID rạp.
     */
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
    // MỞ CONNECTION RIÊNG CHO TRANSACTION
    //
    // Không dùng conn được giữ trong Repository cho các giao dịch ghi đồng thời.
    //
    //conn: Connection kế thừa từ DBContext, được các hàm truy vấn thông thường sử dụng
    //connection: Connection riêng được mở để thực hiện transaction tạo/sửa lịch chiếu
    // =====================================================

    /** Mở kết nối cho giao dịch ghi. */
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
    // KHÓA PHÒNG TRONG TRANSACTION
    // Khóa được giải phóng khi commit hoặc rollback.
    // =====================================================

    // Khóa phòng để các transaction ghi lịch cùng phòng thực hiện tuần tự.
    /** Khóa dòng phòng đến khi commit hoặc rollback. */
    private void lockRoom(Connection connection, int roomId)
            throws SQLException {
        //Room: Bảng phòng chiếu.
        //WHERE room_id = ?: Chỉ khóa phòng đang thao tác.
        //UPDLOCK: Yêu cầu khóa cập nhật khi đọc.
        //HOLDLOCK: Giữ khóa theo phạm vi transaction.
        String sql = "SELECT room_id FROM Room WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE room_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Không tìm thấy phòng chiếu: " + roomId);
                }
            }
        }
    }

    // =====================================================
    // KIỂM TRA DỮ LIỆU SUẤT CHIẾU
    // =====================================================

    /** Kiểm tra dữ liệu suất chiếu trước khi lưu. */
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
    // KIỂM TRA TRÙNG GIỜ BẰNG CONNECTION
    // ĐANG THAM GIA TRANSACTION.
    //
    // Khoảng thời gian [start, end).
    //
    // Điều kiện trùng:
    // existing.start < new.end
    // AND existing.end > new.start
    // =====================================================

    /** Kiểm tra trùng giờ trong giao dịch. */
    private boolean hasConflictInTransaction(
            Connection connection,
            int roomId,
            Timestamp startTime,
            Timestamp endTime,
            Integer excludeShowtimeId)
            throws SQLException {
        //SELECT TOP 1: Chỉ cần phát hiện một suất chiếu trùng là đủ.  
        //WITH (UPDLOCK, HOLDLOCK): Sử dụng cơ chế khóa khi kiểm tra.
        //room_id = ?: Chỉ kiểm tra các suất chiếu cùng phòng.
        //status = 'ACTIVE': Chỉ xét suất chiếu ACTIVE.
        //start_time < ?: Suất cũ bắt đầu trước khi suất mới kết thúc.
        //end_time > ?: Suất cũ kết thúc sau khi suất mới bắt đầu.
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
    // TẠO MỘT SUẤT CHIẾU
    //
    // Lock -> Check overlap -> Insert -> Commit
    // =====================================================

    /** Lưu một suất chiếu và rollback nếu lỗi. */
    public boolean insert(Showtime showtime) {
        //Có năm dữ liệu được lưu: phim, phòng, giờ bắt đầu, giờ kết thúc, trạng thái.
        String sql = "INSERT INTO Showtime "
                + "(movie_id, room_id, start_time, "
                + "end_time, status) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            validateShowtime(showtime);//Nếu dữ liệu không hợp lệ thì báo lỗi trước khi mở transaction.
        } catch (SQLException e) {
            throw showtimeWriteError("tạo suất chiếu", e);
        }
        //Tạo kết nối riêng và tắt AutoCommit.
        try (Connection connection =
                openWriteConnection()) {

            connection.setAutoCommit(false);

            try {
                //Khóa phòng đang tạo suất chiếu.
                lockRoom(
                        connection,
                        showtime.getRoomId()
                );
                //Nếu suất mới ACTIVE và đã tồn tại lịch trùng → báo lỗi.
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
                    //setInsertParameters() truyền dữ liệu vào năm dấu ?.
                    setInsertParameters(ps, showtime);
                    updated = ps.executeUpdate();
                }
                //Nếu không thêm đúng một bản ghi thì báo lỗi.
                if (updated != 1) {
                    throw new SQLException(
                            "Không thể thêm suất chiếu."
                    );
                }
                //Nếu thành công, xác nhận giao dịch.
                connection.commit();
                return true;

            } catch (Exception e) {
                //Nếu lỗi, hoàn tác giao dịch.
                connection.rollback();
                throw showtimeWriteError("tạo suất chiếu", e);
            }

        } catch (SQLException e) {
            throw showtimeWriteError("tạo suất chiếu", e);
        }
    }

    // =====================================================
    // THÊM NHIỀU SUẤT CHIẾU
    //
    // 1. Lấy khóa tất cả phòng theo thứ tự tăng.
    // 2. Kiểm tra trùng lịch trong database.
    // 3. Kiểm tra trùng giữa các suất trong batch.
    // 4. Ghi toàn bộ trong cùng transaction.
    // 5. Có lỗi -> rollback toàn bộ.
    // =====================================================

    /** Lưu nhiều suất chiếu trong một transaction. */
    public boolean insertBatch(
            List<Showtime> showtimes) {
        //Không cho danh sách trống.
        if (showtimes == null || showtimes.isEmpty()) {
            return false;
        }

        String sql = "INSERT INTO Showtime "
                + "(movie_id, room_id, start_time, "
                + "end_time, status) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            //Kiểm tra từng suất chiếu.
            for (Showtime showtime : showtimes) {
                validateShowtime(showtime);
            }

        } catch (SQLException e) {
            throw showtimeWriteError("tạo loạt lịch chiếu", e);
        }
        //Sắp xếp các ID phòng
        // -Loại bỏ ID phòng trùng.
        // -Sắp xếp ID phòng tăng dần.
        TreeSet<Integer> roomIds = new TreeSet<>();

        for (Showtime showtime : showtimes) {
            roomIds.add(showtime.getRoomId());
        }

        try (Connection connection =
                openWriteConnection()) {

            connection.setAutoCommit(false);

            try {

                // Khóa toàn bộ phòng liên quan.
                //Nếu tạo lịch cho phòng 1, 3 và 5, Repository lần lượt khóa theo thứ tự tăng dần.
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

                // Kiểm tra trùng trong chính danh sách mới
                //Hai vòng lặp duyệt từng cặp suất chiếu.
                //Nếu khác phòng thì bỏ qua.
                //Nếu cùng phòng, cả hai ACTIVE và giờ giao nhau thì báo lỗi.
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
                    //Thêm tất cả suất chiếu bằng batch
                    for (Showtime showtime : showtimes) {
                        setInsertParameters(ps, showtime);
                        ps.addBatch(); //addBatch(): Thêm mỗi câu INSERT vào batch.
                    }

                    int[] results = ps.executeBatch(); //executeBatch(): Thực thi các câu INSERT.

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
                //Nếu mọi thứ thành công, xác nhận toàn bộ.
                connection.commit();
                return true;

            } catch (Exception e) {
                //Nếu xảy ra lỗi, hoàn tác transaction.
                connection.rollback();
                throw showtimeWriteError("tạo loạt lịch chiếu", e);
            }

        } catch (SQLException e) {
            throw showtimeWriteError("tạo loạt lịch chiếu", e);
        }
    }

    // =====================================================
    // CẬP NHẬT SUẤT CHIẾU
    //
    // Khóa cả phòng cũ và phòng mới.
    // Kiểm tra lại xung đột trước UPDATE.
    // =====================================================

    /** Cập nhật lịch sau khi kiểm tra quyền và giờ. */
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
        //Hệ thống tìm phòng hiện tại của suất chiếu và xác nhận suất đó thuộc đúng rạp.
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
                //Khóa cả phòng cũ và phòng mới
                //Ví dụ Manager chuyển một suất chiếu từ phòng A sang phòng B. Cả dữ liệu liên quan đến phòng A lẫn phòng B đều có thể bị ảnh hưởng.
                //TreeSet giúp khóa theo thứ tự tăng dần và không khóa trùng cùng một phòng.
                //Sau khi lấy khóa, code truy vấn lại phòng cũ. Nếu suất đã bị thao tác khác thay đổi phòng thì báo lỗi.
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
                // Nếu đã có vé, Repository đọc dữ liệu hiện tại của suất chiếu và so sánh với thông tin mới
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
                            //Nếu có thay đổ
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
                //Kiểm tra trùng lịch
                //Ở đây tham số cuối là showtimeId,
                //giúp loại trừ chính suất chiếu đang sửa
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
                //Nếu xảy ra lỗi, transaction rollback và hàm trả false.
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
    // Không trả false khi SQL gặp lỗi.
    // Tránh cho phép ghi lịch khi DB không kiểm tra được.
    // =====================================================

    /** Kiểm tra lịch ACTIVE trùng giờ trong phòng. 
     *Tại sao phải kiểm tra trùng giờ hai lần?
     * Service kiểm tra trước để xác nhận nghiệp vụ. 
     * -Khi lưu, Repository tiếp tục kiểm tra trong transaction sau khi khóa phòng, 
     * vì có thể một Manager khác vừa tạo lịch trong khoảng thời gian đó.
     */
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
    // DB lỗi => không được mặc định chưa có vé.
    // =====================================================

    /** Kiểm tra suất chiếu đã có vé chưa. */
    public boolean hasTickets(int showtimeId) {
        //SELECT TOP 1: Chỉ cần một vé là đủ.
        //FROM Ticket: Kiểm tra bảng vé.
        //WHERE showtime_id = ?: Kiểm tra vé thuộc suất đang xét.
        String sql = "SELECT TOP 1 ticket_id "
                + "FROM Ticket "
                + "WHERE showtime_id = ?";

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);

            try (ResultSet rs = ps.executeQuery()) {
                //true: Suất chiếu đã có ít nhất một vé.
                //false: Không tìm thấy vé.
                return rs.next();
            }

        } catch (Exception e) {
            //Code không trả false khi database lỗi. 
            //Điều này giúp tránh nhầm lỗi truy vấn thành trường hợp chưa có vé.
            throw new IllegalStateException(
                    "Không thể kiểm tra vé của suất chiếu.",
                    e
            );
        }
    }

    // =====================================================
    // CHUYỂN SUẤT CHIẾU SANG INACTIVE
    // =====================================================

    /** Chuyển suất chiếu sang không hoạt động. */
    public boolean setInactive(
            int showtimeId,
            int cinemaId) {
        //Hàm chuyển một suất chiếu thành INACTIVE theo ID suất chiếu và ID rạp.
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

    /** Xóa suất chiếu khi được phép. */
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

    /** Gán dữ liệu cho câu SQL thêm lịch. */
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

    /** Chuyển dòng SQL thành đối tượng suất chiếu. */
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
