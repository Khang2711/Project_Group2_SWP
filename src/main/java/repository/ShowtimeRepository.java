package repository;

import db.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.Showtime;

/**
 * Provides data access operations for showtimes.
 *
 * @author Kiet
 */
public class ShowtimeRepository extends DBContext {

    /**
     * Gets all active showtimes.
     *
     * @return a list of active showtimes
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

                Showtime showtime = new Showtime();

                showtime.setShowtimeId(rs.getInt("showtime_id"));
                showtime.setMovieId(rs.getInt("movie_id"));
                showtime.setRoomId(rs.getInt("room_id"));
                showtime.setStartTime(rs.getTimestamp("start_time"));
                showtime.setEndTime(rs.getTimestamp("end_time"));
                showtime.setStatus(rs.getString("status"));

                showtimes.add(showtime);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return showtimes;
    }

    /**
     * Gets showtimes by movie ID.
     *
     * @param movieId the movie ID
     * @return a list of showtimes
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

                Showtime showtime = new Showtime();

                showtime.setShowtimeId(rs.getInt("showtime_id"));
                showtime.setMovieId(rs.getInt("movie_id"));
                showtime.setRoomId(rs.getInt("room_id"));
                showtime.setStartTime(rs.getTimestamp("start_time"));
                showtime.setEndTime(rs.getTimestamp("end_time"));
                showtime.setStatus(rs.getString("status"));

                showtimes.add(showtime);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return showtimes;
    }

    /**
     * Gets a showtime by its ID.
     *
     * @param id the showtime ID
     * @return the showtime if found, otherwise null
     */
    public Showtime getById(int id) {

        String sql = "SELECT * FROM Showtime "
                + "WHERE showtime_id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Showtime showtime = new Showtime();

                showtime.setShowtimeId(rs.getInt("showtime_id"));
                showtime.setMovieId(rs.getInt("movie_id"));
                showtime.setRoomId(rs.getInt("room_id"));
                showtime.setStartTime(rs.getTimestamp("start_time"));
                showtime.setEndTime(rs.getTimestamp("end_time"));
                showtime.setStatus(rs.getString("status"));

                return showtime;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
