package repository;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.Promotion;

public class PromotionRepository {

    public List<Promotion> getAll() {

        List<Promotion> promotions = new ArrayList<>();

        String sql
                = "SELECT promotion_id, promo_code, description, "
                + "discount_type, discount_value, min_order_amount, "
                + "max_discount_amount, start_date, end_date, "
                + "usage_limit, status "
                + "FROM Promotion "
                + "ORDER BY promotion_id DESC";

        try (
                Connection connection
                = new DBContext().getConnection();
                PreparedStatement statement
                = connection.prepareStatement(sql);
                ResultSet resultSet
                = statement.executeQuery()) {

            while (resultSet.next()) {
                promotions.add(
                        mapPromotion(resultSet)
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return promotions;
    }

    public Promotion getById(int promotionId) {

        String sql
                = "SELECT promotion_id, promo_code, description, "
                + "discount_type, discount_value, min_order_amount, "
                + "max_discount_amount, start_date, end_date, "
                + "usage_limit, status "
                + "FROM Promotion "
                + "WHERE promotion_id = ?";

        try (
                Connection connection
                = new DBContext().getConnection();
                PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    promotionId
            );

            try (
                    ResultSet resultSet
                    = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapPromotion(
                            resultSet
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean existsByCode(String promoCode) {

        String sql
                = "SELECT 1 "
                + "FROM Promotion "
                + "WHERE promo_code = ?";

        try (
                Connection connection
                = new DBContext().getConnection();
                PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    promoCode
            );

            try (
                    ResultSet resultSet
                    = statement.executeQuery()) {

                return resultSet.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean existsByCodeExceptId(
            String promoCode,
            int promotionId) {

        String sql
                = "SELECT 1 "
                + "FROM Promotion "
                + "WHERE promo_code = ? "
                + "AND promotion_id <> ?";

        try (
                Connection connection
                = new DBContext().getConnection();
                PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    promoCode
            );

            statement.setInt(
                    2,
                    promotionId
            );

            try (
                    ResultSet resultSet
                    = statement.executeQuery()) {

                return resultSet.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean insert(Promotion promotion) {

        String sql
                = "INSERT INTO Promotion "
                + "(promo_code, description, discount_type, "
                + "discount_value, min_order_amount, "
                + "max_discount_amount, start_date, end_date, "
                + "usage_limit, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection connection
                = new DBContext().getConnection();
                PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    promotion.getPromoCode()
            );

            statement.setString(
                    2,
                    promotion.getDescription()
            );

            statement.setString(
                    3,
                    promotion.getDiscountType()
            );

            statement.setBigDecimal(
                    4,
                    promotion.getDiscountValue()
            );

            statement.setBigDecimal(
                    5,
                    promotion.getMinOrderAmount()
            );

            if (promotion.getMaxDiscountAmount() == null) {

                statement.setNull(
                        6,
                        Types.DECIMAL
                );

            } else {

                statement.setBigDecimal(
                        6,
                        promotion.getMaxDiscountAmount()
                );
            }

            statement.setTimestamp(
                    7,
                    promotion.getStartDate()
            );

            statement.setTimestamp(
                    8,
                    promotion.getEndDate()
            );

            if (promotion.getUsageLimit() == null) {

                statement.setNull(
                        9,
                        Types.INTEGER
                );

            } else {

                statement.setInt(
                        9,
                        promotion.getUsageLimit()
                );
            }

            statement.setString(
                    10,
                    promotion.getStatus()
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(Promotion promotion) {

        String sql
                = "UPDATE Promotion SET "
                + "promo_code = ?, "
                + "description = ?, "
                + "discount_type = ?, "
                + "discount_value = ?, "
                + "min_order_amount = ?, "
                + "max_discount_amount = ?, "
                + "start_date = ?, "
                + "end_date = ?, "
                + "usage_limit = ?, "
                + "status = ? "
                + "WHERE promotion_id = ?";

        try (
                Connection connection
                = new DBContext().getConnection();
                PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    promotion.getPromoCode()
            );

            statement.setString(
                    2,
                    promotion.getDescription()
            );

            statement.setString(
                    3,
                    promotion.getDiscountType()
            );

            statement.setBigDecimal(
                    4,
                    promotion.getDiscountValue()
            );

            statement.setBigDecimal(
                    5,
                    promotion.getMinOrderAmount()
            );

            if (promotion.getMaxDiscountAmount() == null) {

                statement.setNull(
                        6,
                        Types.DECIMAL
                );

            } else {

                statement.setBigDecimal(
                        6,
                        promotion.getMaxDiscountAmount()
                );
            }

            statement.setTimestamp(
                    7,
                    promotion.getStartDate()
            );

            statement.setTimestamp(
                    8,
                    promotion.getEndDate()
            );

            if (promotion.getUsageLimit() == null) {

                statement.setNull(
                        9,
                        Types.INTEGER
                );

            } else {

                statement.setInt(
                        9,
                        promotion.getUsageLimit()
                );
            }

            statement.setString(
                    10,
                    promotion.getStatus()
            );

            statement.setInt(
                    11,
                    promotion.getPromotionId()
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateStatus(
            int promotionId,
            String status) {

        String sql
                = "UPDATE Promotion "
                + "SET status = ? "
                + "WHERE promotion_id = ?";

        try (
                Connection connection
                = new DBContext().getConnection();
                PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    status
            );

            statement.setInt(
                    2,
                    promotionId
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private Promotion mapPromotion(
            ResultSet resultSet)
            throws SQLException {

        Promotion promotion
                = new Promotion();

        promotion.setPromotionId(
                resultSet.getInt(
                        "promotion_id"
                )
        );

        promotion.setPromoCode(
                resultSet.getString(
                        "promo_code"
                )
        );

        promotion.setDescription(
                resultSet.getString(
                        "description"
                )
        );

        promotion.setDiscountType(
                resultSet.getString(
                        "discount_type"
                )
        );

        promotion.setDiscountValue(
                resultSet.getBigDecimal(
                        "discount_value"
                )
        );

        promotion.setMinOrderAmount(
                resultSet.getBigDecimal(
                        "min_order_amount"
                )
        );

        promotion.setMaxDiscountAmount(
                resultSet.getBigDecimal(
                        "max_discount_amount"
                )
        );

        promotion.setStartDate(
                resultSet.getTimestamp(
                        "start_date"
                )
        );

        promotion.setEndDate(
                resultSet.getTimestamp(
                        "end_date"
                )
        );

        int usageLimit
                = resultSet.getInt(
                        "usage_limit"
                );

        if (resultSet.wasNull()) {

            promotion.setUsageLimit(
                    null
            );

        } else {

            promotion.setUsageLimit(
                    usageLimit
            );
        }

        promotion.setStatus(
                resultSet.getString(
                        "status"
                )
        );

        return promotion;
    }
}