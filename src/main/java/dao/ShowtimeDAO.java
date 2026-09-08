package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Showtime;
import utils.DBUtils;

public class ShowtimeDAO {

    /**
     * Ghép ngày chiếu và giờ bắt đầu thành một mốc DATETIME của SQL Server.
     */
    private String startDateTimeSql(String alias) {
        return "DATEADD(SECOND, DATEDIFF(SECOND, CAST('00:00:00' AS time), "
                + alias + ".startTime), CAST(" + alias + ".showDate AS datetime))";
    }

    private Showtime mapShowtime(ResultSet rs) throws Exception {
        return new Showtime(
                rs.getString("showtimeID"),
                rs.getString("movieID"),
                rs.getString("title"),
                rs.getString("roomID"),
                rs.getString("roomName"),
                rs.getInt("totalSeats"),
                rs.getString("showDate"),
                rs.getString("startTime"),
                rs.getString("endTime"),
                rs.getBoolean("status"),
                rs.getFloat("basePrice"),
                rs.getBoolean("roomStatus")
        );
    }

    private String baseSql() {
        return "SELECT s.showtimeID, s.movieID, m.title, s.roomID, r.roomName, r.totalSeats, "
                + "CONVERT(varchar(10), s.showDate, 120) AS showDate, "
                + "CONVERT(varchar(5), s.startTime, 108) AS startTime, "
                + "CONVERT(varchar(5), s.endTime, 108) AS endTime, "
                + "s.status, m.basePrice, r.status AS roomStatus "
                + "FROM tblShowtimes s "
                + "JOIN tblMovies m ON s.movieID = m.movieID "
                + "JOIN tblRooms r ON s.roomID = r.roomID ";
    }

    /**
     * Tự động khóa các suất đã đến giờ chiếu và cập nhật trạng thái phim.
     * Phim chỉ ở trạng thái đang chiếu khi còn ít nhất một suất tương lai,
     * suất đang mở bán và phòng không bảo trì.
     */
    public void refreshExpiredStatuses() throws Exception {
        String showtimeDateTime = startDateTimeSql("s");

        String closeExpiredSql = "UPDATE s SET s.status = 0 "
                + "FROM tblShowtimes s "
                + "WHERE s.status = 1 AND " + showtimeDateTime + " <= GETDATE()";

        String refreshMovieSql = "UPDATE m SET m.status = CASE WHEN EXISTS ("
                + "SELECT 1 FROM tblShowtimes s "
                + "JOIN tblRooms r ON s.roomID = r.roomID "
                + "WHERE s.movieID = m.movieID "
                + "AND s.status = 1 AND r.status = 1 "
                + "AND " + showtimeDateTime + " > GETDATE()"
                + ") THEN 1 ELSE 0 END "
                + "FROM tblMovies m";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement closeStm = conn.prepareStatement(closeExpiredSql);
                PreparedStatement movieStm = conn.prepareStatement(refreshMovieSql)) {

            closeStm.executeUpdate();
            movieStm.executeUpdate();
        }
    }

    public List<Showtime> getAllShowtimes() throws Exception {
        refreshExpiredStatuses();
        List<Showtime> list = new ArrayList<>();
        String sql = baseSql() + "ORDER BY s.showDate DESC, s.startTime DESC";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql);
                ResultSet rs = stm.executeQuery()) {

            while (rs.next()) {
                list.add(mapShowtime(rs));
            }
        }
        return list;
    }

    /**
     * Lấy toàn bộ suất chiếu còn có thể đặt, có hỗ trợ tìm theo phim.
     */
    public List<Showtime> getUpcomingShowtimes(String keyword) throws Exception {
        refreshExpiredStatuses();
        List<Showtime> list = new ArrayList<>();
        keyword = keyword == null ? "" : keyword.trim();

        String sql = baseSql()
                + "WHERE s.status = 1 AND m.status = 1 AND r.status = 1 "
                + "AND " + startDateTimeSql("s") + " > GETDATE() "
                + "AND (? = '' OR m.movieID LIKE ? OR m.title LIKE ? OR m.description LIKE ?) "
                + "ORDER BY s.showDate, m.title, s.startTime";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            String likeKeyword = "%" + keyword + "%";
            stm.setString(1, keyword);
            stm.setString(2, likeKeyword);
            stm.setString(3, likeKeyword);
            stm.setString(4, likeKeyword);

            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    list.add(mapShowtime(rs));
                }
            }
        }
        return list;
    }

    public List<Showtime> getActiveShowtimesByMovie(String movieID) throws Exception {
        refreshExpiredStatuses();
        List<Showtime> list = new ArrayList<>();
        String sql = baseSql()
                + "WHERE s.movieID = ? AND s.status = 1 AND m.status = 1 AND r.status = 1 "
                + "AND " + startDateTimeSql("s") + " > GETDATE() "
                + "ORDER BY s.showDate, s.startTime";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, movieID);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    list.add(mapShowtime(rs));
                }
            }
        }
        return list;
    }

    public Showtime getShowtimeByID(String showtimeID) throws Exception {
        refreshExpiredStatuses();
        String sql = baseSql() + "WHERE s.showtimeID = ?";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, showtimeID);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return mapShowtime(rs);
                }
            }
        }
        return null;
    }

    /**
     * Chỉ trả về suất còn ở tương lai. Đây là chốt chặn cho trang chọn ghế
     * và bước thanh toán, nên sửa URL cũng không đặt được suất đã qua.
     */
    public Showtime getBookableShowtimeByID(String showtimeID) throws Exception {
        refreshExpiredStatuses();
        String sql = baseSql()
                + "WHERE s.showtimeID = ? AND s.status = 1 AND m.status = 1 AND r.status = 1 "
                + "AND " + startDateTimeSql("s") + " > GETDATE()";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, showtimeID);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return mapShowtime(rs);
                }
            }
        }
        return null;
    }

    public boolean isRoomTimeConflict(String roomID, String showDate, String startTime,
            String endTime, String exceptShowtimeID) throws Exception {

        String sql = "SELECT showtimeID FROM tblShowtimes "
                + "WHERE roomID = ? "
                + "AND CAST(showDate AS date) = CAST(? AS date) "
                + "AND status = 1 "
                + "AND startTime < CAST(? AS time) "
                + "AND endTime > CAST(? AS time) ";

        if (exceptShowtimeID != null && !exceptShowtimeID.trim().isEmpty()) {
            sql += "AND showtimeID <> ? ";
        }

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, roomID);
            stm.setString(2, showDate);
            stm.setString(3, endTime);
            stm.setString(4, startTime);
            if (exceptShowtimeID != null && !exceptShowtimeID.trim().isEmpty()) {
                stm.setString(5, exceptShowtimeID);
            }

            try (ResultSet rs = stm.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean createShowtime(Showtime showtime) throws Exception {
        String sql = "INSERT INTO tblShowtimes(showtimeID, movieID, roomID, showDate, startTime, endTime, status) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?)";
        boolean result;

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, showtime.getShowtimeID());
            stm.setString(2, showtime.getMovieID());
            stm.setString(3, showtime.getRoomID());
            stm.setString(4, showtime.getShowDate());
            stm.setString(5, showtime.getStartTime());
            stm.setString(6, showtime.getEndTime());
            stm.setBoolean(7, showtime.isStatus());
            result = stm.executeUpdate() > 0;
        }

        refreshExpiredStatuses();
        return result;
    }

    public boolean updateShowtime(Showtime showtime) throws Exception {
        String sql = "UPDATE tblShowtimes SET movieID = ?, roomID = ?, showDate = ?, startTime = ?, endTime = ?, status = ? "
                + "WHERE showtimeID = ?";
        boolean result;

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, showtime.getMovieID());
            stm.setString(2, showtime.getRoomID());
            stm.setString(3, showtime.getShowDate());
            stm.setString(4, showtime.getStartTime());
            stm.setString(5, showtime.getEndTime());
            stm.setBoolean(6, showtime.isStatus());
            stm.setString(7, showtime.getShowtimeID());
            result = stm.executeUpdate() > 0;
        }

        refreshExpiredStatuses();
        return result;
    }

    public boolean deleteShowtime(String showtimeID) throws Exception {
        String sql = "UPDATE tblShowtimes SET status = 0 WHERE showtimeID = ?";
        boolean result;

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, showtimeID);
            result = stm.executeUpdate() > 0;
        }

        refreshExpiredStatuses();
        return result;
    }
}
