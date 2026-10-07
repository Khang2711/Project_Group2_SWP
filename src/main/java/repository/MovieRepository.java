package repository;

import db.DBContext;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Movie;
import java.sql.Statement;

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

    public List<Movie> getByCinemaId(int cinemaId) {

        List<Movie> movies = new ArrayList<>();

        String sql = "SELECT m.* "
                + "FROM Movie m "
                + "JOIN Cinema_Movie cm "
                + "ON m.movie_id = cm.movie_id "
                + "WHERE cm.cinema_id = ? "
                + "ORDER BY m.movie_id DESC";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, cinemaId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                movies.add(mapMovie(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return movies;
    }

    public Movie getByIdAnyStatus(int movieId) {

        String sql = "SELECT * "
                + "FROM Movie "
                + "WHERE movie_id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, movieId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapMovie(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Integer> getGenreIds(int movieId) {

        List<Integer> genreIds = new ArrayList<>();

        String sql = "SELECT genre_id "
                + "FROM Movie_Genre "
                + "WHERE movie_id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, movieId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                genreIds.add(rs.getInt("genre_id"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return genreIds;
    }

    public boolean belongsToCinema(int movieId, int cinemaId) {

        String sql = "SELECT movie_id "
                + "FROM Cinema_Movie "
                + "WHERE movie_id = ? "
                + "AND cinema_id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, movieId);
            ps.setInt(2, cinemaId);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean insertForCinema(Movie movie,
            List<Integer> genreIds,
            int cinemaId) {

        String movieSql = "INSERT INTO Movie "
                + "(title, description, duration_minutes, age_rating, "
                + "release_date, end_date, poster_url, trailer_url, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String genreSql = "INSERT INTO Movie_Genre "
                + "(movie_id, genre_id) "
                + "VALUES (?, ?)";

        String cinemaSql = "INSERT INTO Cinema_Movie "
                + "(cinema_id, movie_id, status) "
                + "VALUES (?, ?, ?)";

        try {
            conn.setAutoCommit(false);

            PreparedStatement moviePs = conn.prepareStatement(
                    movieSql,
                    Statement.RETURN_GENERATED_KEYS
            );

            setMovieParameters(moviePs, movie);

            moviePs.executeUpdate();

            ResultSet keys = moviePs.getGeneratedKeys();

            if (!keys.next()) {
                conn.rollback();
                conn.setAutoCommit(true);
                return false;
            }

            int movieId = keys.getInt(1);

            for (Integer genreId : genreIds) {

                PreparedStatement genrePs
                        = conn.prepareStatement(genreSql);

                genrePs.setInt(1, movieId);
                genrePs.setInt(2, genreId);

                genrePs.executeUpdate();
            }

            PreparedStatement cinemaPs
                    = conn.prepareStatement(cinemaSql);

            cinemaPs.setInt(1, cinemaId);
            cinemaPs.setInt(2, movieId);
            cinemaPs.setString(3, "ACTIVE");

            cinemaPs.executeUpdate();

            conn.commit();
            conn.setAutoCommit(true);

            return true;

        } catch (Exception e) {

            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (Exception rollbackException) {
                rollbackException.printStackTrace();
            }

            e.printStackTrace();
        }

        return false;
    }

    public boolean updateForCinema(Movie movie,
            List<Integer> genreIds,
            int cinemaId) {

        String movieSql = "UPDATE Movie "
                + "SET title = ?, description = ?, "
                + "duration_minutes = ?, age_rating = ?, "
                + "release_date = ?, end_date = ?, "
                + "poster_url = ?, trailer_url = ?, status = ? "
                + "WHERE movie_id = ?";

        String deleteGenreSql
                = "DELETE FROM Movie_Genre WHERE movie_id = ?";

        String insertGenreSql
                = "INSERT INTO Movie_Genre (movie_id, genre_id) "
                + "VALUES (?, ?)";

        try {
            conn.setAutoCommit(false);

            PreparedStatement moviePs
                    = conn.prepareStatement(movieSql);

            setMovieParameters(moviePs, movie);
            moviePs.setInt(10, movie.getMovieId());

            moviePs.executeUpdate();

            PreparedStatement deletePs
                    = conn.prepareStatement(deleteGenreSql);

            deletePs.setInt(1, movie.getMovieId());
            deletePs.executeUpdate();

            for (Integer genreId : genreIds) {

                PreparedStatement genrePs
                        = conn.prepareStatement(insertGenreSql);

                genrePs.setInt(1, movie.getMovieId());
                genrePs.setInt(2, genreId);

                genrePs.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            return true;

        } catch (Exception e) {

            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (Exception rollbackException) {
                rollbackException.printStackTrace();
            }

            e.printStackTrace();
        }

        return false;
    }

    private void setMovieParameters(
            PreparedStatement ps,
            Movie movie) throws Exception {

        ps.setString(1, movie.getTitle());
        ps.setString(2, movie.getDescription());
        ps.setInt(3, movie.getDurationMinutes());
        ps.setString(4, movie.getAgeRating());
        ps.setDate(5, movie.getReleaseDate());
        ps.setDate(6, movie.getEndDate());
        ps.setString(7, movie.getPosterUrl());
        ps.setString(8, movie.getTrailerUrl());
        ps.setString(9, movie.getStatus());
    }

    private Movie mapMovie(ResultSet rs) throws Exception {

        Movie movie = new Movie();

        movie.setMovieId(rs.getInt("movie_id"));
        movie.setTitle(rs.getString("title"));
        movie.setDescription(rs.getString("description"));
        movie.setDurationMinutes(
                rs.getInt("duration_minutes")
        );
        movie.setAgeRating(rs.getString("age_rating"));
        movie.setReleaseDate(rs.getDate("release_date"));
        movie.setEndDate(rs.getDate("end_date"));
        movie.setPosterUrl(rs.getString("poster_url"));
        movie.setTrailerUrl(rs.getString("trailer_url"));
        movie.setStatus(rs.getString("status"));

        return movie;
    }
}
