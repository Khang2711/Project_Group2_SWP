package repository;

import db.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Showtime;

public class ShowtimeRepository extends DBContext {

    // Lấy cinema_id của suất chiếu để kiểm tra khuyến mãi theo rạp.
    public int getCinemaIdByShowtimeId(int showtimeId) {

        String sql = "SELECT r.cinema_id "
                + "FROM dbo.Showtime s "
                + "JOIN dbo.Room r ON s.room_id = r.room_id "
                + "WHERE s.showtime_id = ?";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, showtimeId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("cinema_id");
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

    public List<Showtime> getByMovieIdAndDate(
            int movieId,
            LocalDate date) {

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

            ps.setTimestamp(
                    3,
                    Timestamp.valueOf(date.atStartOfDay())
            );

            ps.setTimestamp(
                    4,
                    Timestamp.valueOf(
                            date.plusDays(1).atStartOfDay()
                    )
            );

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

        try {

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, cinemaId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                showtimes.add(mapShowtime(rs));
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

        try {

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, showtimeId);
            ps.setInt(2, cinemaId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapShowtime(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean insert(Showtime showtime) {

        String sql = "INSERT INTO Showtime "
                + "(movie_id, room_id, start_time, end_time, status) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);

            setInsertParameters(ps, showtime);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /*
     * Thêm nhiều lịch chiếu trong cùng transaction.
     *
     * Nếu bất kỳ dòng nào lỗi thì rollback toàn bộ,
     * tránh trường hợp chỉ thêm được một nửa danh sách.
     */
    public boolean insertBatch(List<Showtime> showtimes) {

        if (showtimes == null || showtimes.isEmpty()) {
            return false;
        }

        String sql = "INSERT INTO Showtime "
                + "(movie_id, room_id, start_time, end_time, status) "
                + "VALUES (?, ?, ?, ?, ?)";

        boolean oldAutoCommit = true;

        try {

            oldAutoCommit = conn.getAutoCommit();

            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement(sql);

            for (Showtime showtime : showtimes) {

                setInsertParameters(ps, showtime);

                ps.addBatch();
            }

            ps.executeBatch();

            conn.commit();

            return true;

        } catch (Exception e) {

            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
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

    public boolean update(
            Showtime showtime,
            int cinemaId) {

        String sql = "UPDATE Showtime "
                + "SET movie_id = ?, "
                + "room_id = ?, "
                + "start_time = ?, "
                + "end_time = ?, "
                + "status = ? "
                + "WHERE showtime_id = ? "
                + "AND room_id IN ("
                + "SELECT room_id "
                + "FROM Room "
                + "WHERE cinema_id = ?)";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, showtime.getMovieId());
            ps.setInt(2, showtime.getRoomId());
            ps.setTimestamp(3, showtime.getStartTime());
            ps.setTimestamp(4, showtime.getEndTime());
            ps.setString(5, showtime.getStatus());
            ps.setInt(6, showtime.getShowtimeId());
            ps.setInt(7, cinemaId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

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

        try {

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, roomId);
            ps.setTimestamp(2, endTime);
            ps.setTimestamp(3, startTime);

            if (excludeShowtimeId != null) {
                ps.setInt(4, excludeShowtimeId);
            }

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean hasTickets(int showtimeId) {

        String sql = "SELECT TOP 1 ticket_id "
                + "FROM Ticket "
                + "WHERE showtime_id = ?";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, showtimeId);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean setInactive(
            int showtimeId,
            int cinemaId) {

        String sql = "UPDATE Showtime "
                + "SET status = 'INACTIVE' "
                + "WHERE showtime_id = ? "
                + "AND room_id IN ("
                + "SELECT room_id "
                + "FROM Room "
                + "WHERE cinema_id = ?)";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, showtimeId);
            ps.setInt(2, cinemaId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean delete(
            int showtimeId,
            int cinemaId) {

        String sql = "DELETE FROM Showtime "
                + "WHERE showtime_id = ? "
                + "AND room_id IN ("
                + "SELECT room_id "
                + "FROM Room "
                + "WHERE cinema_id = ?)";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, showtimeId);
            ps.setInt(2, cinemaId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void setInsertParameters(
            PreparedStatement ps,
            Showtime showtime)
            throws Exception {

        ps.setInt(1, showtime.getMovieId());
        ps.setInt(2, showtime.getRoomId());
        ps.setTimestamp(3, showtime.getStartTime());
        ps.setTimestamp(4, showtime.getEndTime());
        ps.setString(5, showtime.getStatus());
    }

    private Showtime mapShowtime(ResultSet rs) throws Exception {

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
