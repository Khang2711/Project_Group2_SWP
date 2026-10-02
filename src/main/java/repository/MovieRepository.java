package repository;

import db.DBContext;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Movie;

/**
 * Provides data access operations for Movie.
 *
 * @author Kiet
 */
public class MovieRepository extends DBContext {

    /**
     * Gets all active movies.
     *
     * @return a list of active movies
     */
    public List<Movie> getAll() {

        List<Movie> movies = new ArrayList<>();

        String sql = "SELECT * "
                + "FROM Movie "
                + "WHERE status = ? "
                + "ORDER BY release_date DESC";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, "ACTIVE");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Movie movie = new Movie();

                movie.setMovieId(rs.getInt("movie_id"));
                movie.setTitle(rs.getString("title"));
                movie.setDescription(rs.getString("description"));
                movie.setDurationMinutes(rs.getInt("duration_minutes"));
                movie.setAgeRating(rs.getString("age_rating"));
                movie.setReleaseDate(rs.getDate("release_date"));
                movie.setEndDate(rs.getDate("end_date"));
                movie.setPosterUrl(rs.getString("poster_url"));
                movie.setTrailerUrl(rs.getString("trailer_url"));
                movie.setStatus(rs.getString("status"));

                movies.add(movie);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return movies;
    }

    /**
     * Gets a movie by its ID.
     *
     * @param id the movie ID
     * @return the movie if found, otherwise null
     */
    public Movie getById(int id) {

        String sql = "SELECT * "
                + "FROM Movie "
                + "WHERE movie_id = ? "
                + "AND status = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, id);
            ps.setString(2, "ACTIVE");

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Movie movie = new Movie();

                movie.setMovieId(rs.getInt("movie_id"));
                movie.setTitle(rs.getString("title"));
                movie.setDescription(rs.getString("description"));
                movie.setDurationMinutes(rs.getInt("duration_minutes"));
                movie.setAgeRating(rs.getString("age_rating"));
                movie.setReleaseDate(rs.getDate("release_date"));
                movie.setEndDate(rs.getDate("end_date"));
                movie.setPosterUrl(rs.getString("poster_url"));
                movie.setTrailerUrl(rs.getString("trailer_url"));
                movie.setStatus(rs.getString("status"));

                return movie;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Searches active movies by title.
     *
     * @param keyword movie title keyword
     * @return matching active movies
     */
    public List<Movie> search(String keyword) {

        List<Movie> movies = new ArrayList<>();

        String sql = "SELECT * "
                + "FROM Movie "
                + "WHERE status = ? "
                + "AND title LIKE ? "
                + "ORDER BY release_date DESC";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, "ACTIVE");
            ps.setString(2, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Movie movie = new Movie();

                movie.setMovieId(rs.getInt("movie_id"));
                movie.setTitle(rs.getString("title"));
                movie.setDescription(rs.getString("description"));
                movie.setDurationMinutes(rs.getInt("duration_minutes"));
                movie.setAgeRating(rs.getString("age_rating"));
                movie.setReleaseDate(rs.getDate("release_date"));
                movie.setEndDate(rs.getDate("end_date"));
                movie.setPosterUrl(rs.getString("poster_url"));
                movie.setTrailerUrl(rs.getString("trailer_url"));
                movie.setStatus(rs.getString("status"));

                movies.add(movie);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return movies;
    }
}