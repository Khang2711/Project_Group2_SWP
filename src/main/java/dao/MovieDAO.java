package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Movie;
import utils.DBUtils;

public class MovieDAO {

    private Movie mapMovie(ResultSet rs) throws Exception {
        return new Movie(
                rs.getString("movieID"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("duration"),
                rs.getFloat("basePrice"),
                rs.getBoolean("status"),
                rs.getString("imageURL")
        );
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim();
    }

    private int normalizePage(int page) {
        return page < 1 ? 1 : page;
    }

    private int normalizePageSize(int pageSize) {
        return pageSize < 1 ? 8 : pageSize;
    }

    public List<Movie> getAllMovies() throws Exception {
        return searchMovies("", 1, 1000, false);
    }

    public List<Movie> getActiveMovies() throws Exception {
        return searchMovies("", 1, 1000, true);
    }

    public List<Movie> searchMovies(String keyword, int page, int pageSize, boolean activeOnly) throws Exception {
        List<Movie> list = new ArrayList<>();
        keyword = normalizeKeyword(keyword);
        page = normalizePage(page);
        pageSize = normalizePageSize(pageSize);
        int offset = (page - 1) * pageSize;

        String sql = "SELECT movieID, title, description, duration, basePrice, status, imageURL "
                + "FROM tblMovies "
                + "WHERE (? = '' OR movieID LIKE ? OR title LIKE ? OR description LIKE ?) ";

        if (activeOnly) {
            sql += "AND status = 1 ";
        }

        sql += activeOnly ? "ORDER BY title ASC " : "ORDER BY status DESC, title ASC ";
        sql += "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            String likeKeyword = "%" + keyword + "%";
            stm.setString(1, keyword);
            stm.setString(2, likeKeyword);
            stm.setString(3, likeKeyword);
            stm.setString(4, likeKeyword);
            stm.setInt(5, offset);
            stm.setInt(6, pageSize);

            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    list.add(mapMovie(rs));
                }
            }
        }
        return list;
    }

    public int countMovies(String keyword, boolean activeOnly) throws Exception {
        keyword = normalizeKeyword(keyword);
        String sql = "SELECT COUNT(*) AS total FROM tblMovies "
                + "WHERE (? = '' OR movieID LIKE ? OR title LIKE ? OR description LIKE ?) ";

        if (activeOnly) {
            sql += "AND status = 1 ";
        }

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            String likeKeyword = "%" + keyword + "%";
            stm.setString(1, keyword);
            stm.setString(2, likeKeyword);
            stm.setString(3, likeKeyword);
            stm.setString(4, likeKeyword);

            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    public Movie getMovieByID(String movieID) throws Exception {
        String sql = "SELECT movieID, title, description, duration, basePrice, status, imageURL "
                + "FROM tblMovies WHERE movieID = ?";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, movieID);

            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return mapMovie(rs);
                }
            }
        }
        return null;
    }

    public boolean createMovie(Movie movie) throws Exception {
        String sql = "INSERT INTO tblMovies(movieID, title, description, duration, basePrice, status, imageURL) VALUES(?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, movie.getMovieID());
            stm.setString(2, movie.getTitle());
            stm.setString(3, movie.getDescription());
            stm.setInt(4, movie.getDuration());
            stm.setFloat(5, movie.getBasePrice());
            stm.setBoolean(6, movie.isStatus());
            stm.setString(7, movie.getImageURL());

            return stm.executeUpdate() > 0;
        }
    }

    public boolean updateMovie(Movie movie) throws Exception {
        String movieSql = "UPDATE tblMovies SET title = ?, description = ?, duration = ?, basePrice = ?, status = ?, imageURL = ? WHERE movieID = ?";
        String closeShowtimesSql = "UPDATE tblShowtimes SET status = 0 WHERE movieID = ?";

        try (Connection conn = DBUtils.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement movieStm = conn.prepareStatement(movieSql);
                    PreparedStatement showtimeStm = conn.prepareStatement(closeShowtimesSql)) {

                movieStm.setString(1, movie.getTitle());
                movieStm.setString(2, movie.getDescription());
                movieStm.setInt(3, movie.getDuration());
                movieStm.setFloat(4, movie.getBasePrice());
                movieStm.setBoolean(5, movie.isStatus());
                movieStm.setString(6, movie.getImageURL());
                movieStm.setString(7, movie.getMovieID());
                boolean updated = movieStm.executeUpdate() > 0;

                // Khi admin chọn ngưng chiếu, khóa luôn các suất còn lại của phim.
                if (!movie.isStatus()) {
                    showtimeStm.setString(1, movie.getMovieID());
                    showtimeStm.executeUpdate();
                }

                conn.commit();
                return updated;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean deleteMovie(String movieID) throws Exception {
        String movieSql = "UPDATE tblMovies SET status = 0 WHERE movieID = ?";
        String showtimeSql = "UPDATE tblShowtimes SET status = 0 WHERE movieID = ?";

        try (Connection conn = DBUtils.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement movieStm = conn.prepareStatement(movieSql);
                    PreparedStatement showtimeStm = conn.prepareStatement(showtimeSql)) {

                movieStm.setString(1, movieID);
                boolean updated = movieStm.executeUpdate() > 0;

                showtimeStm.setString(1, movieID);
                showtimeStm.executeUpdate();

                conn.commit();
                return updated;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
