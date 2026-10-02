package repository;

import db.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Cinema;

/**
 * Provides data access operations for Cinema.
 *
 * @author Kiet
 */
public class CinemaRepository extends DBContext {

    /**
     * Gets all active cinemas.
     *
     * @return a list of active cinemas
     */
    public List<Cinema> getAllActive() {

        List<Cinema> cinemas = new ArrayList<>();

        String sql = "SELECT * "
                + "FROM Cinema "
                + "WHERE status = ? "
                + "ORDER BY cinema_name";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, "ACTIVE");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                int cinemaId = rs.getInt("cinema_id");
                String cinemaName = rs.getString("cinema_name");
                String address = rs.getString("address");
                String phone = rs.getString("phone");
                String status = rs.getString("status");

                cinemas.add(new Cinema(cinemaId, cinemaName, address, phone, status));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cinemas;
    }
}