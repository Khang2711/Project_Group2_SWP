package repository;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.ComboItem;

/** DAO cho bảng trung gian Combo_Item. */
public class ComboItemRepository {

    public List<ComboItem> findByComboId(int comboId) {
        List<ComboItem> items = new ArrayList<>();
        String sql = "SELECT combo_id, food_item_id, quantity "
                + "FROM dbo.Combo_Item WHERE combo_id = ? "
                + "ORDER BY food_item_id";

        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                return items;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, comboId);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        items.add(new ComboItem(
                                rs.getInt("combo_id"),
                                rs.getInt("food_item_id"),
                                rs.getInt("quantity")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Replaces a combo's contents inside the caller's transaction.
     * Only active products from the same cinema can be included.
     */
    public boolean replaceItems(Connection connection, int comboId, int cinemaId,
            List<ComboItem> items) throws SQLException {
        if (connection == null || comboId <= 0 || cinemaId <= 0
                || items == null || items.isEmpty()) {
            return false;
        }

        String checkSql = "SELECT COUNT(1) FROM dbo.Food_Item "
                + "WHERE food_item_id = ? AND cinema_id = ? AND status = 'ACTIVE'";

        for (ComboItem item : items) {
            if (item == null || item.getFoodItemId() <= 0 || item.getQuantity() <= 0) {
                return false;
            }
            try (PreparedStatement check = connection.prepareStatement(checkSql)) {
                check.setInt(1, item.getFoodItemId());
                check.setInt(2, cinemaId);
                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) != 1) {
                        return false;
                    }
                }
            }
        }

        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM dbo.Combo_Item WHERE combo_id = ?")) {
            delete.setInt(1, comboId);
            delete.executeUpdate();
        }

        String insertSql = "INSERT INTO dbo.Combo_Item (combo_id, food_item_id, quantity) "
                + "VALUES (?, ?, ?)";
        try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
            for (ComboItem item : items) {
                insert.setInt(1, comboId);
                insert.setInt(2, item.getFoodItemId());
                insert.setInt(3, item.getQuantity());
                insert.addBatch();
            }
            int[] counts = insert.executeBatch();
            for (int count : counts) {
                if (count == 0 || count == PreparedStatement.EXECUTE_FAILED) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Returns true only when the combo has at least one active item and all items are active in this cinema. */
    public boolean hasOnlyActiveItems(int comboId, int cinemaId) {
        String sql = "SELECT CASE WHEN "
                + "EXISTS (SELECT 1 FROM dbo.Combo_Item WHERE combo_id = ?) "
                + "AND NOT EXISTS ("
                + "SELECT 1 FROM dbo.Combo_Item ci "
                + "JOIN dbo.Food_Item fi ON fi.food_item_id = ci.food_item_id "
                + "WHERE ci.combo_id = ? "
                + "AND (fi.cinema_id <> ? OR fi.status <> 'ACTIVE')) "
                + "THEN 1 ELSE 0 END";

        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                return false;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, comboId);
                statement.setInt(2, comboId);
                statement.setInt(3, cinemaId);
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next() && rs.getInt(1) == 1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
