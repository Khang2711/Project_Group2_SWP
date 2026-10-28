package repository;

import db.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Genre;

public class GenreRepository extends DBContext {

    public List<Genre> getAllActive() {

        List<Genre> genres = new ArrayList<>();

        String sql = "SELECT genre_id, genre_name, description, status "
                + "FROM Genre "
                + "WHERE status = ? "
                + "ORDER BY genre_name";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "ACTIVE");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Genre genre = new Genre();

                genre.setGenreId(rs.getInt("genre_id"));
                genre.setGenreName(rs.getString("genre_name"));
                genre.setDescription(rs.getString("description"));
                genre.setStatus(rs.getString("status"));

                genres.add(genre);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return genres;
    }

    /**
     * Gets all genres, including active and inactive genres.
     *
     * @return a list of all genres
     */
    public List<Genre> getAll() {

        List<Genre> genres = new ArrayList<>();

        String sql = "SELECT genre_id, genre_name, description, status "
                + "FROM Genre "
                + "ORDER BY genre_id ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Genre genre = new Genre();

                genre.setGenreId(rs.getInt("genre_id"));
                genre.setGenreName(rs.getString("genre_name"));
                genre.setDescription(rs.getString("description"));
                genre.setStatus(rs.getString("status"));

                genres.add(genre);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return genres;
    }

    /**
     * Gets a genre by its ID.
     *
     * @param genreId the genre ID
     * @return the genre if found, otherwise null
     */
    public Genre getById(int genreId) {

        String sql = "SELECT * "
                + "FROM Genre "
                + "WHERE genre_id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, genreId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Genre genre = new Genre();

                genre.setGenreId(rs.getInt("genre_id"));
                genre.setGenreName(rs.getString("genre_name"));
                genre.setDescription(rs.getString("description"));
                genre.setStatus(rs.getString("status"));

                return genre;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Checks whether a genre name already exists.
     *
     * @param genreName the genre name to check
     * @return true if the name already exists, otherwise false
     */
    public boolean existsByName(String genreName) {

        String sql = "SELECT genre_id "
                + "FROM Genre "
                + "WHERE LOWER(genre_name) = LOWER(?)";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, genreName);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Inserts a new genre into the database.
     *
     * @param genre the genre to insert
     * @return true if inserted successfully, otherwise false
     */
    public boolean insert(Genre genre) {

        String sql = "INSERT INTO Genre "
                + "(genre_name, description, status) "
                + "VALUES (?, ?, ?)";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, genre.getGenreName());
            ps.setString(2, genre.getDescription());
            ps.setString(3, genre.getStatus());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Updates an existing genre.
     *
     * @param genre the genre with updated information
     * @return true if updated successfully, otherwise false
     */
    public boolean update(Genre genre) {

        String sql = "UPDATE Genre "
                + "SET genre_name = ?, "
                + "description = ?, "
                + "status = ? "
                + "WHERE genre_id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, genre.getGenreName());
            ps.setString(2, genre.getDescription());
            ps.setString(3, genre.getStatus());
            ps.setInt(4, genre.getGenreId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Updates the status of a genre.
     *
     * @param genreId the genre ID
     * @param status the new status (ACTIVE or INACTIVE)
     * @return true if updated successfully, otherwise false
     */
    public boolean updateStatus(int genreId, String status) {

        String sql = "UPDATE Genre "
                + "SET status = ? "
                + "WHERE genre_id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, status);
            ps.setInt(2, genreId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    /**
     * Deletes a genre only when no movie references it.
     * The NOT EXISTS guard and the foreign key both protect against concurrent links.
     */
    public DeleteResult deleteIfUnused(int genreId) {
        if (conn == null) {
            return DeleteResult.ERROR;
        }

        String deleteSql = "DELETE FROM Genre "
                + "WHERE genre_id = ? "
                + "AND NOT EXISTS (SELECT 1 FROM Movie_Genre WHERE genre_id = ?)";

        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setInt(1, genreId);
            ps.setInt(2, genreId);

            if (ps.executeUpdate() > 0) {
                return DeleteResult.DELETED;
            }

            // No row deleted: distinguish an existing, referenced genre from a missing ID.
            String existsSql = "SELECT 1 FROM Genre WHERE genre_id = ?";
            try (PreparedStatement check = conn.prepareStatement(existsSql)) {
                check.setInt(1, genreId);
                try (ResultSet rs = check.executeQuery()) {
                    return rs.next() ? DeleteResult.IN_USE : DeleteResult.NOT_FOUND;
                }
            }
        } catch (SQLException e) {
            // SQL Server error 547: a foreign key blocked deletion (including a race).
            if (e.getErrorCode() == 547) {
                return DeleteResult.IN_USE;
            }
            e.printStackTrace();
            return DeleteResult.ERROR;
        }
    }

    public enum DeleteResult {
        DELETED, IN_USE, NOT_FOUND, ERROR
    }
}
