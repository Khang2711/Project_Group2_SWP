package repository;

import db.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
}