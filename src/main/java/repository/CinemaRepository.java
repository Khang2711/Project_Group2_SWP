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

    /**
     * Gets all cinemas, including inactive branches.
     *
     * @return all cinemas
     */
    public List<Cinema> getAll() {

        List<Cinema> cinemas = new ArrayList<>();

        String sql = "SELECT cinema_id, cinema_name, address, phone, status "
                + "FROM dbo.Cinema "
                + "ORDER BY cinema_id";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                cinemas.add(new Cinema(
                        rs.getInt("cinema_id"),
                        rs.getString("cinema_name"),
                        rs.getString("address"),
                        rs.getString("phone"),
                        rs.getString("status")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cinemas;
    }

    /**
     * Checks whether another branch already uses the same name.
     */
    public boolean existsByName(String cinemaName, int excludeCinemaId) {

        String sql = "SELECT cinema_id "
                + "FROM dbo.Cinema "
                + "WHERE LOWER(cinema_name) = LOWER(?) "
                + "AND cinema_id <> ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, cinemaName);
            ps.setInt(2, excludeCinemaId);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(
                    "Không thể kiểm tra tên chi nhánh."
            );
        }
    }

    public boolean insert(Cinema cinema) {

        String sql = "INSERT INTO dbo.Cinema "
                + "(cinema_name, address, phone, status) "
                + "VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, cinema.getCinemaName());
            ps.setString(2, cinema.getAddress());
            ps.setString(3, cinema.getPhone());
            ps.setString(4, cinema.getStatus());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(Cinema cinema) {

        String sql = "UPDATE dbo.Cinema "
                + "SET cinema_name = ?, address = ?, phone = ?, status = ? "
                + "WHERE cinema_id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, cinema.getCinemaName());
            ps.setString(2, cinema.getAddress());
            ps.setString(3, cinema.getPhone());
            ps.setString(4, cinema.getStatus());
            ps.setInt(5, cinema.getCinemaId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateStatus(int cinemaId, String status) {

        String sql = "UPDATE dbo.Cinema "
                + "SET status = ? "
                + "WHERE cinema_id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, cinemaId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Returns a message when a branch still has related records.
     * A null result means the branch can be deleted.
     */
    public String getDeleteBlockReason(int cinemaId) {

        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM dbo.Staff WHERE cinema_id = ?) AS staff_count, "
                + "(SELECT COUNT(*) FROM dbo.Room WHERE cinema_id = ?) AS room_count, "
                + "(SELECT COUNT(*) FROM dbo.Pricing WHERE cinema_id = ?) AS pricing_count, "
                + "(SELECT COUNT(*) FROM dbo.Cinema_Movie WHERE cinema_id = ?) AS movie_count, "
                + "(SELECT COUNT(*) FROM dbo.Food_Item WHERE cinema_id = ?) AS food_count, "
                + "(SELECT COUNT(*) FROM dbo.[Combo] WHERE cinema_id = ?) AS combo_count, "
                + "(SELECT COUNT(*) FROM dbo.Food_Order WHERE cinema_id = ?) AS order_count";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            for (int i = 1; i <= 7; i++) {
                ps.setInt(i, cinemaId);
            }

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new IllegalStateException(
                        "Không thể kiểm tra dữ liệu liên kết của chi nhánh."
                );
            }

            List<String> relatedData = new ArrayList<>();

            int staffCount = rs.getInt("staff_count");
            int roomCount = rs.getInt("room_count");
            int pricingCount = rs.getInt("pricing_count");
            int movieCount = rs.getInt("movie_count");
            int foodCount = rs.getInt("food_count");
            int comboCount = rs.getInt("combo_count");
            int orderCount = rs.getInt("order_count");

            if (staffCount > 0) {
                relatedData.add("Manager/Staff (" + staffCount + ")");
            }
            if (roomCount > 0) {
                relatedData.add("phòng chiếu (" + roomCount + ")");
            }
            if (pricingCount > 0) {
                relatedData.add("bảng giá vé (" + pricingCount + ")");
            }
            if (movieCount > 0) {
                relatedData.add("phim được gán (" + movieCount + ")");
            }
            if (foodCount > 0) {
                relatedData.add("đồ ăn/thức uống (" + foodCount + ")");
            }
            if (comboCount > 0) {
                relatedData.add("combo (" + comboCount + ")");
            }
            if (orderCount > 0) {
                relatedData.add("đơn bán tại quầy (" + orderCount + ")");
            }

            if (relatedData.isEmpty()) {
                return null;
            }

            return "Không thể xóa chi nhánh vì đang có dữ liệu liên kết: "
                    + String.join(", ", relatedData)
                    + ". Hãy xử lý dữ liệu liên quan trước.";

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(
                    "Không thể kiểm tra dữ liệu liên kết của chi nhánh."
            );
        }
    }

    public boolean delete(int cinemaId) {

        String sql = "DELETE FROM dbo.Cinema "
                + "WHERE cinema_id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, cinemaId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}
