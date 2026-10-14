package repository;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.FoodItem;

/** DAO cho bảng Food_Item. */
public class FoodItemRepository {

    public List<FoodItem> findByCinema(int cinemaId) {
        return findByCinema(cinemaId, false);
    }

    public List<FoodItem> findActiveByCinema(int cinemaId) {
        return findByCinema(cinemaId, true);
    }

    private List<FoodItem> findByCinema(int cinemaId, boolean activeOnly) {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT food_item_id, cinema_id, item_name, item_type, "
                + "description, price, image_url, status "
                + "FROM dbo.Food_Item WHERE cinema_id = ? "
                + (activeOnly ? "AND status = 'ACTIVE' " : "")
                + "ORDER BY CASE WHEN status = 'ACTIVE' THEN 0 ELSE 1 END, "
                + "item_type, item_name";

        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                return items;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, cinemaId);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        items.add(mapFoodItem(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public FoodItem findById(int foodItemId, int cinemaId) {
        return findOne(foodItemId, cinemaId, false);
    }

    public FoodItem findActiveById(int foodItemId, int cinemaId) {
        return findOne(foodItemId, cinemaId, true);
    }

    private FoodItem findOne(int foodItemId, int cinemaId, boolean activeOnly) {
        String sql = "SELECT food_item_id, cinema_id, item_name, item_type, "
                + "description, price, image_url, status "
                + "FROM dbo.Food_Item WHERE food_item_id = ? AND cinema_id = ? "
                + (activeOnly ? "AND status = 'ACTIVE' " : "");

        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                return null;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, foodItemId);
                statement.setInt(2, cinemaId);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return mapFoodItem(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(FoodItem item) {
        String sql = "INSERT INTO dbo.Food_Item "
                + "(cinema_id, item_name, item_type, description, price, image_url, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                return false;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                setItemParameters(statement, item);
                return statement.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(FoodItem item) {
        String sql = "UPDATE dbo.Food_Item SET item_name = ?, item_type = ?, "
                + "description = ?, price = ?, image_url = ?, status = ? "
                + "WHERE food_item_id = ? AND cinema_id = ?";

        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                return false;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, item.getItemName());
                statement.setString(2, item.getItemType());
                statement.setString(3, item.getDescription());
                statement.setBigDecimal(4, item.getPrice());
                statement.setString(5, item.getImageUrl());
                statement.setString(6, item.getStatus());
                statement.setInt(7, item.getFoodItemId());
                statement.setInt(8, item.getCinemaId());
                return statement.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(int foodItemId, int cinemaId, String status) {
        String sql = "UPDATE dbo.Food_Item SET status = ? "
                + "WHERE food_item_id = ? AND cinema_id = ?";

        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                return false;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, status);
                statement.setInt(2, foodItemId);
                statement.setInt(3, cinemaId);
                return statement.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setItemParameters(PreparedStatement statement, FoodItem item)
            throws SQLException {
        statement.setInt(1, item.getCinemaId());
        statement.setString(2, item.getItemName());
        statement.setString(3, item.getItemType());
        statement.setString(4, item.getDescription());
        statement.setBigDecimal(5, item.getPrice());
        statement.setString(6, item.getImageUrl());
        statement.setString(7, item.getStatus());
    }

    private FoodItem mapFoodItem(ResultSet rs) throws SQLException {
        return new FoodItem(
                rs.getInt("food_item_id"),
                rs.getInt("cinema_id"),
                rs.getString("item_name"),
                rs.getString("item_type"),
                rs.getString("description"),
                rs.getBigDecimal("price"),
                rs.getString("image_url"),
                rs.getString("status")
        );
    }
}
