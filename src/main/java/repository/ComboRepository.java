package repository;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Combo;
import model.ComboItem;

/** DAO cho bảng Combo. */
public class ComboRepository {

    private final ComboItemRepository comboItemRepository = new ComboItemRepository();

    public List<Combo> findByCinema(int cinemaId) {
        List<Combo> combos = new ArrayList<>();
        String sql = "SELECT combo_id, cinema_id, combo_name, description, "
                + "combo_price, image_url, status FROM dbo.Combo "
                + "WHERE cinema_id = ? "
                + "ORDER BY CASE WHEN status = 'ACTIVE' THEN 0 ELSE 1 END, combo_name";

        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                return combos;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, cinemaId);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        combos.add(mapCombo(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return combos;
    }

    public List<Combo> findActiveForSale(int cinemaId) {
        List<Combo> combos = new ArrayList<>();
        String sql = "SELECT c.combo_id, c.cinema_id, c.combo_name, c.description, "
                + "c.combo_price, c.image_url, c.status "
                + "FROM dbo.Combo c WHERE c.cinema_id = ? AND c.status = 'ACTIVE' "
                + "AND EXISTS (SELECT 1 FROM dbo.Combo_Item ci WHERE ci.combo_id = c.combo_id) "
                + "AND NOT EXISTS (SELECT 1 FROM dbo.Combo_Item ci "
                + "JOIN dbo.Food_Item fi ON fi.food_item_id = ci.food_item_id "
                + "WHERE ci.combo_id = c.combo_id "
                + "AND (fi.cinema_id <> c.cinema_id OR fi.status <> 'ACTIVE')) "
                + "ORDER BY c.combo_name";

        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                return combos;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, cinemaId);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        combos.add(mapCombo(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return combos;
    }

    public Combo findById(int comboId, int cinemaId) {
        String sql = "SELECT combo_id, cinema_id, combo_name, description, "
                + "combo_price, image_url, status FROM dbo.Combo "
                + "WHERE combo_id = ? AND cinema_id = ?";

        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                return null;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, comboId);
                statement.setInt(2, cinemaId);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return mapCombo(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Combo findActiveForSaleById(int comboId, int cinemaId) {
        String sql = "SELECT c.combo_id, c.cinema_id, c.combo_name, c.description, "
                + "c.combo_price, c.image_url, c.status "
                + "FROM dbo.Combo c WHERE c.combo_id = ? AND c.cinema_id = ? "
                + "AND c.status = 'ACTIVE' "
                + "AND EXISTS (SELECT 1 FROM dbo.Combo_Item ci WHERE ci.combo_id = c.combo_id) "
                + "AND NOT EXISTS (SELECT 1 FROM dbo.Combo_Item ci "
                + "JOIN dbo.Food_Item fi ON fi.food_item_id = ci.food_item_id "
                + "WHERE ci.combo_id = c.combo_id "
                + "AND (fi.cinema_id <> c.cinema_id OR fi.status <> 'ACTIVE'))";

        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                return null;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, comboId);
                statement.setInt(2, cinemaId);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return mapCombo(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Saves a combo and replaces its composition atomically. */
    public boolean saveWithItems(Combo combo, List<ComboItem> items) {
        if (combo == null || items == null || items.isEmpty()) {
            return false;
        }

        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                return false;
            }

            boolean originalAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);
                int comboId;

                if (combo.getComboId() > 0) {
                    comboId = update(connection, combo) ? combo.getComboId() : -1;
                } else {
                    comboId = insert(connection, combo);
                }

                if (comboId <= 0
                        || !comboItemRepository.replaceItems(
                                connection, comboId, combo.getCinemaId(), items)) {
                    connection.rollback();
                    return false;
                }

                connection.commit();
                combo.setComboId(comboId);
                return true;
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    rollbackError.printStackTrace();
                }
                e.printStackTrace();
                return false;
            } finally {
                try {
                    connection.setAutoCommit(originalAutoCommit);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(int comboId, int cinemaId, String status) {
        String sql = "UPDATE dbo.Combo SET status = ? WHERE combo_id = ? AND cinema_id = ?";
        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                return false;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, status);
                statement.setInt(2, comboId);
                statement.setInt(3, cinemaId);
                return statement.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int insert(Connection connection, Combo combo) throws SQLException {
        String sql = "INSERT INTO dbo.Combo "
                + "(cinema_id, combo_name, description, combo_price, image_url, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, combo.getCinemaId());
            statement.setString(2, combo.getComboName());
            statement.setString(3, combo.getDescription());
            statement.setBigDecimal(4, combo.getComboPrice());
            statement.setString(5, combo.getImageUrl());
            statement.setString(6, combo.getStatus());

            if (statement.executeUpdate() != 1) {
                return -1;
            }
            try (ResultSet rs = statement.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    private boolean update(Connection connection, Combo combo) throws SQLException {
        String sql = "UPDATE dbo.Combo SET combo_name = ?, description = ?, "
                + "combo_price = ?, image_url = ?, status = ? "
                + "WHERE combo_id = ? AND cinema_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, combo.getComboName());
            statement.setString(2, combo.getDescription());
            statement.setBigDecimal(3, combo.getComboPrice());
            statement.setString(4, combo.getImageUrl());
            statement.setString(5, combo.getStatus());
            statement.setInt(6, combo.getComboId());
            statement.setInt(7, combo.getCinemaId());
            return statement.executeUpdate() == 1;
        }
    }

    private Combo mapCombo(ResultSet rs) throws SQLException {
        return new Combo(
                rs.getInt("combo_id"),
                rs.getInt("cinema_id"),
                rs.getString("combo_name"),
                rs.getString("description"),
                rs.getBigDecimal("combo_price"),
                rs.getString("image_url"),
                rs.getString("status")
        );
    }
}
