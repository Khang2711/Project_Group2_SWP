package repository;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.FoodOrderItem;

public class FoodOrderItemRepository {

    public int insert(FoodOrderItem item) {

        try (
                Connection connection
                = new DBContext().getConnection()) {

            return insert(
                    connection,
                    item
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int insert(
            Connection connection,
            FoodOrderItem item)
            throws SQLException {

        String sql
                = "INSERT INTO Food_Order_Item "
                + "(food_order_id, food_item_id, combo_id, "
                + "quantity, unit_price, line_total) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (
                PreparedStatement statement
                = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )) {

            statement.setInt(
                    1,
                    item.getFoodOrderId()
            );

            if (item.getFoodItemId() == null) {

                statement.setNull(
                        2,
                        Types.INTEGER
                );

            } else {

                statement.setInt(
                        2,
                        item.getFoodItemId()
                );
            }

            if (item.getComboId() == null) {

                statement.setNull(
                        3,
                        Types.INTEGER
                );

            } else {

                statement.setInt(
                        3,
                        item.getComboId()
                );
            }

            statement.setInt(
                    4,
                    item.getQuantity()
            );

            statement.setBigDecimal(
                    5,
                    item.getUnitPrice()
            );

            statement.setBigDecimal(
                    6,
                    item.getLineTotal()
            );

            int affectedRows
                    = statement.executeUpdate();

            if (affectedRows == 0) {
                return -1;
            }

            try (
                    ResultSet resultSet
                    = statement.getGeneratedKeys()) {

                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }

        return -1;
    }

    public List<FoodOrderItem> getByFoodOrderId(
            int foodOrderId) {

        List<FoodOrderItem> items
                = new ArrayList<>();

        String sql
                = "SELECT food_order_item_id, food_order_id, "
                + "food_item_id, combo_id, quantity, "
                + "unit_price, line_total "
                + "FROM Food_Order_Item "
                + "WHERE food_order_id = ? "
                + "ORDER BY food_order_item_id";

        try (
                Connection connection
                = new DBContext().getConnection();
                PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    foodOrderId
            );

            try (
                    ResultSet resultSet
                    = statement.executeQuery()) {

                while (resultSet.next()) {

                    items.add(
                            mapFoodOrderItem(
                                    resultSet
                            )
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    private FoodOrderItem mapFoodOrderItem(
            ResultSet resultSet)
            throws SQLException {

        FoodOrderItem item
                = new FoodOrderItem();

        item.setFoodOrderItemId(
                resultSet.getInt(
                        "food_order_item_id"
                )
        );

        item.setFoodOrderId(
                resultSet.getInt(
                        "food_order_id"
                )
        );

        int foodItemId
                = resultSet.getInt(
                        "food_item_id"
                );

        if (resultSet.wasNull()) {

            item.setFoodItemId(
                    null
            );

        } else {

            item.setFoodItemId(
                    foodItemId
            );
        }

        int comboId
                = resultSet.getInt(
                        "combo_id"
                );

        if (resultSet.wasNull()) {

            item.setComboId(
                    null
            );

        } else {

            item.setComboId(
                    comboId
            );
        }

        item.setQuantity(
                resultSet.getInt(
                        "quantity"
                )
        );

        item.setUnitPrice(
                resultSet.getBigDecimal(
                        "unit_price"
                )
        );

        item.setLineTotal(
                resultSet.getBigDecimal(
                        "line_total"
                )
        );

        return item;
    }
}