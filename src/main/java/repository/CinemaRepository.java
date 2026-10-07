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

    public Cinema findById(int cinemaId) {

        String sql = "SELECT cinema_id, cinema_name, address, phone, status "
                + "FROM Cinema "
                + "WHERE cinema_id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, cinemaId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Cinema(
                        rs.getInt("cinema_id"),
                        rs.getString("cinema_name"),
                        rs.getString("address"),
                        rs.getString("phone"),
                        rs.getString("status")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public int countRooms(int cinemaId) {

        String sql = "SELECT COUNT(*) AS total "
                + "FROM Room "
                + "WHERE cinema_id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, cinemaId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean updateContact(int cinemaId, String address, String phone) {

        String sql = "UPDATE Cinema "
                + "SET address = ?, phone = ? "
                + "WHERE cinema_id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, address);
            ps.setString(2, phone);
            ps.setInt(3, cinemaId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
