package repository;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import model.FoodOrder;
import model.FoodOrderItem;

public class FoodOrderRepository {

    public int insert(FoodOrder foodOrder) {

        try (
                Connection connection
                = new DBContext().getConnection()) {

            if (connection == null) {
                return -1;
            }

            return insert(
                    connection,
                    foodOrder
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int insert(
            Connection connection,
            FoodOrder foodOrder)
            throws SQLException {

        String sql
                = "INSERT INTO Food_Order "
                + "(staff_id, cinema_id, subtotal, total_amount, "
                + "payment_method, payment_status, order_status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
                PreparedStatement statement
                = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )) {

            statement.setInt(
                    1,
                    foodOrder.getStaffId()
            );

            statement.setInt(
                    2,
                    foodOrder.getCinemaId()
            );

            statement.setBigDecimal(
                    3,
                    foodOrder.getSubtotal()
            );

            statement.setBigDecimal(
                    4,
                    foodOrder.getTotalAmount()
            );

            statement.setString(
                    5,
                    foodOrder.getPaymentMethod()
            );

            statement.setString(
                    6,
                    foodOrder.getPaymentStatus()
            );

            statement.setString(
                    7,
                    foodOrder.getOrderStatus()
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

    public int insertWithItems(
            FoodOrder foodOrder,
            List<FoodOrderItem> items) {

        if (foodOrder == null
                || items == null
                || items.isEmpty()) {

            return -1;
        }

        try (
                Connection connection
                = new DBContext().getConnection()) {

            if (connection == null) {
                return -1;
            }

            boolean originalAutoCommit
                    = connection.getAutoCommit();

            try {

                connection.setAutoCommit(
                        false
                );

                int foodOrderId
                        = insert(
                                connection,
                                foodOrder
                        );

                if (foodOrderId <= 0) {

                    connection.rollback();

                    return -1;
                }

                FoodOrderItemRepository itemRepository
                        = new FoodOrderItemRepository();

                for (FoodOrderItem item : items) {

                    item.setFoodOrderId(
                            foodOrderId
                    );

                    int foodOrderItemId
                            = itemRepository.insert(
                                    connection,
                                    item
                            );

                    if (foodOrderItemId <= 0) {

                        connection.rollback();

                        return -1;
                    }
                }

                connection.commit();

                return foodOrderId;

            } catch (SQLException
                    | RuntimeException e) {

                try {

                    connection.rollback();

                } catch (SQLException rollbackException) {

                    rollbackException.printStackTrace();
                }

                e.printStackTrace();

                return -1;

            } finally {

                try {

                    connection.setAutoCommit(
                            originalAutoCommit
                    );

                } catch (SQLException e) {

                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            return -1;
        }
    }

    public FoodOrder getById(
            int foodOrderId) {

        String sql
                = "SELECT food_order_id, staff_id, cinema_id, "
                + "subtotal, total_amount, payment_method, "
                + "payment_status, order_status, created_at "
                + "FROM Food_Order "
                + "WHERE food_order_id = ?";

        try (
                Connection connection
                = new DBContext().getConnection()) {

            if (connection == null) {
                return null;
            }

            try (
                    PreparedStatement statement
                    = connection.prepareStatement(sql)) {

                statement.setInt(
                        1,
                        foodOrderId
                );

                try (
                        ResultSet resultSet
                        = statement.executeQuery()) {

                    if (resultSet.next()) {
                        return mapFoodOrder(
                                resultSet
                        );
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private FoodOrder mapFoodOrder(
            ResultSet resultSet)
            throws SQLException {

        FoodOrder foodOrder
                = new FoodOrder();

        foodOrder.setFoodOrderId(
                resultSet.getInt(
                        "food_order_id"
                )
        );

        foodOrder.setStaffId(
                resultSet.getInt(
                        "staff_id"
                )
        );

        foodOrder.setCinemaId(
                resultSet.getInt(
                        "cinema_id"
                )
        );

        foodOrder.setSubtotal(
                resultSet.getBigDecimal(
                        "subtotal"
                )
        );

        foodOrder.setTotalAmount(
                resultSet.getBigDecimal(
                        "total_amount"
                )
        );

        foodOrder.setPaymentMethod(
                resultSet.getString(
                        "payment_method"
                )
        );

        foodOrder.setPaymentStatus(
                resultSet.getString(
                        "payment_status"
                )
        );

        foodOrder.setOrderStatus(
                resultSet.getString(
                        "order_status"
                )
        );

        foodOrder.setCreatedAt(
                resultSet.getTimestamp(
                        "created_at"
                )
        );

        return foodOrder;
    }
}