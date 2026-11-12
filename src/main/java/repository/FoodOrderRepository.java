package repository;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Combo;
import model.FoodItem;
import model.FoodOrder;
import model.FoodOrderItem;

public class FoodOrderRepository {

    public List<Map<String, Object>> findPaidItemsByBooking(
            int bookingId) throws SQLException {

        List<Map<String, Object>> items = new ArrayList<>();

        String sql
                = "SELECT "
                + "COALESCE(f.item_name, c.combo_name) AS product_name, "
                + "oi.quantity, oi.unit_price, oi.line_total "
                + "FROM dbo.Food_Order o "
                + "JOIN dbo.Food_Order_Item oi "
                + "ON oi.food_order_id = o.food_order_id "
                + "LEFT JOIN dbo.Food_Item f "
                + "ON f.food_item_id = oi.food_item_id "
                + "LEFT JOIN dbo.Combo c "
                + "ON c.combo_id = oi.combo_id "
                + "WHERE o.booking_id = ? "
                + "AND o.payment_status = 'PAID' "
                + "ORDER BY oi.food_order_item_id";

        try (Connection connection = openConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Map<String, Object> item = new HashMap<>();

                    item.put("productName",
                            rs.getString("product_name"));

                    item.put("quantity",
                            rs.getInt("quantity"));

                    item.put("unitPrice",
                            rs.getBigDecimal("unit_price"));

                    item.put("lineTotal",
                            rs.getBigDecimal("line_total"));

                    items.add(item);
                }
            }
        }

        return items;
    }

    public int insert(FoodOrder foodOrder) {

        try (Connection connection = new DBContext().getConnection()) {

            if (connection == null) {
                return -1;
            }

            return insert(connection, foodOrder);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int insert(Connection connection, FoodOrder foodOrder)
            throws SQLException {

        if (foodOrder.getBookingId() != null) {
            boolean validBooking = foodOrder.getStaffId() <= 0
                    ? isPendingCustomerBookingForFood(connection, foodOrder.getBookingId(), foodOrder.getCinemaId())
                    : isBookingAvailableForFood(connection, foodOrder.getBookingId(), foodOrder.getStaffId(), foodOrder.getCinemaId());
            if (foodOrder.getBookingId() <= 0 || !validBooking) {

                throw new IllegalArgumentException(
                        "Đơn vé không hợp lệ hoặc không phải do bạn bán tại rạp này.");
            }
        }

        String sql = "INSERT INTO Food_Order "
                + "(staff_id, cinema_id, subtotal, total_amount, "
                + "payment_method, payment_status, order_status, booking_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            setOrderData(statement, foodOrder);

            if (statement.executeUpdate() == 0) {
                return -1;
            }

            try (ResultSet resultSet = statement.getGeneratedKeys()) {

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

        if (foodOrder == null || items == null || items.isEmpty()) {
            return -1;
        }

        try (Connection connection = new DBContext().getConnection()) {

            if (connection == null) {
                return -1;
            }

            boolean originalAutoCommit = connection.getAutoCommit();

            try {
                connection.setAutoCommit(false);

                int foodOrderId = insert(connection, foodOrder);

                if (foodOrderId <= 0) {
                    connection.rollback();
                    return -1;
                }

                FoodOrderItemRepository itemRepository
                        = new FoodOrderItemRepository();

                // Lưu đơn và sản phẩm cùng nhau; lỗi một phần thì hủy toàn bộ.
                for (FoodOrderItem item : items) {

                    item.setFoodOrderId(foodOrderId);

                    int itemId = itemRepository.insert(connection, item);

                    if (itemId <= 0) {
                        connection.rollback();
                        return -1;
                    }
                }

                connection.commit();
                return foodOrderId;

            } catch (SQLException | RuntimeException e) {

                try {
                    connection.rollback();

                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }

                e.printStackTrace();
                return -1;

            } finally {

                try {
                    connection.setAutoCommit(originalAutoCommit);

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public FoodOrder getById(int foodOrderId) {

        String sql = "SELECT food_order_id, staff_id, cinema_id, "
                + "subtotal, total_amount, payment_method, "
                + "payment_status, order_status, created_at, booking_id "
                + "FROM Food_Order WHERE food_order_id = ?";

        try (Connection connection = new DBContext().getConnection()) {

            if (connection == null) {
                return null;
            }

            try (PreparedStatement statement
                    = connection.prepareStatement(sql)) {

                statement.setInt(1, foodOrderId);

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (resultSet.next()) {
                        return mapFoodOrder(resultSet);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean isBookingAvailableForFood(
            int bookingId,
            int staffId,
            int cinemaId) throws SQLException {

        try (Connection connection = openConnection()) {

            return isBookingAvailableForFood(
                    connection,
                    bookingId,
                    staffId,
                    cinemaId
            );
        }
    }

    private boolean isBookingAvailableForFood(
            Connection connection,
            int bookingId,
            int staffId,
            int cinemaId) throws SQLException {

        String sql = "SELECT 1 FROM dbo.Booking b "
                + "JOIN dbo.Showtime st ON st.showtime_id = b.showtime_id "
                + "JOIN dbo.Room r ON r.room_id = st.room_id "
                + "WHERE b.booking_id = ? AND b.staff_id = ? "
                + "AND r.cinema_id = ? AND b.status = 'CONFIRMED'";

        try (PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, bookingId);
            statement.setInt(2, staffId);
            statement.setInt(3, cinemaId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean isPendingCustomerBookingForFood(Connection connection, int bookingId, int cinemaId) throws SQLException {
        String sql = "SELECT 1 FROM dbo.Booking b JOIN dbo.Showtime st ON st.showtime_id=b.showtime_id "
                + "JOIN dbo.Room r ON r.room_id=st.room_id WHERE b.booking_id=? AND r.cinema_id=? "
                + "AND b.status='PENDING' AND b.staff_id IS NULL";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, cinemaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<FoodOrder> findCompletedByBooking(
            int bookingId,
            int staffId,
            int cinemaId) throws SQLException {

        List<FoodOrder> orders = new ArrayList<>();

        String sql = "SELECT o.food_order_id, o.staff_id, o.cinema_id, "
                + "o.booking_id, o.subtotal, o.total_amount, "
                + "o.payment_method, o.payment_status, o.order_status, "
                + "o.created_at "
                + "FROM dbo.Food_Order o "
                + "JOIN dbo.Booking b ON b.booking_id = o.booking_id "
                + "JOIN dbo.Showtime st ON st.showtime_id = b.showtime_id "
                + "JOIN dbo.Room r ON r.room_id = st.room_id "
                + "WHERE o.booking_id = ? "
                + "AND o.staff_id = ? AND b.staff_id = ? "
                + "AND o.cinema_id = ? AND r.cinema_id = ? "
                + "AND o.payment_status = 'PAID' "
                + "AND o.order_status = 'COMPLETED' "
                + "ORDER BY o.food_order_id";

        try (Connection connection = openConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, bookingId);
            statement.setInt(2, staffId);
            statement.setInt(3, staffId);
            statement.setInt(4, cinemaId);
            statement.setInt(5, cinemaId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    orders.add(mapFoodOrder(resultSet));
                }
            }
        }

        return orders;
    }

    public List<FoodItem> findFoodsForCounter(int cinemaId)
            throws SQLException {

        List<FoodItem> items = new ArrayList<>();

        String sql = "SELECT food_item_id, cinema_id, item_name, "
                + "description, price, image_url, status "
                + "FROM Food_Item "
                + "WHERE cinema_id = ? AND status = 'ACTIVE' "
                + "ORDER BY item_name";

        try (Connection connection = openConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, cinemaId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    FoodItem item = new FoodItem();

                    item.setFoodItemId(
                            resultSet.getInt("food_item_id"));

                    item.setCinemaId(
                            resultSet.getInt("cinema_id"));

                    item.setItemName(
                            resultSet.getString("item_name"));

                    item.setDescription(
                            resultSet.getString("description"));

                    item.setPrice(
                            resultSet.getBigDecimal("price"));

                    item.setImageUrl(
                            resultSet.getString("image_url"));

                    item.setStatus(
                            resultSet.getString("status"));

                    items.add(item);
                }
            }
        }

        return items;
    }

    public List<Combo> findCombosForCounter(int cinemaId)
            throws SQLException {

        List<Combo> combos = new ArrayList<>();

        // Combo phải có món và các món phải còn bán tại cùng rạp.
        String sql = "SELECT c.combo_id, c.cinema_id, c.combo_name, "
                + "c.description, c.combo_price, c.image_url, c.status "
                + "FROM Combo c "
                + "WHERE c.cinema_id = ? AND c.status = 'ACTIVE' "
                + "AND EXISTS ("
                + "SELECT 1 FROM Combo_Item ci "
                + "WHERE ci.combo_id = c.combo_id) "
                + "AND NOT EXISTS ("
                + "SELECT 1 FROM Combo_Item ci "
                + "LEFT JOIN Food_Item f ON f.food_item_id = ci.food_item_id "
                + "WHERE ci.combo_id = c.combo_id "
                + "AND (f.food_item_id IS NULL "
                + "OR f.status <> 'ACTIVE' "
                + "OR f.cinema_id <> c.cinema_id)) "
                + "ORDER BY c.combo_name";

        try (Connection connection = openConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, cinemaId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    Combo combo = new Combo();

                    combo.setComboId(
                            resultSet.getInt("combo_id"));

                    combo.setCinemaId(
                            resultSet.getInt("cinema_id"));

                    combo.setComboName(
                            resultSet.getString("combo_name"));

                    combo.setDescription(
                            resultSet.getString("description"));

                    combo.setComboPrice(
                            resultSet.getBigDecimal("combo_price"));

                    combo.setImageUrl(
                            resultSet.getString("image_url"));

                    combo.setStatus(
                            resultSet.getString("status"));

                    combos.add(combo);
                }
            }
        }

        return combos;
    }

    public int findStaffCinema(int staffId) throws SQLException {

        String sql = "SELECT sf.cinema_id "
                + "FROM Staff sf "
                + "JOIN [User] u ON u.user_id = sf.staff_id "
                + "JOIN Cinema c ON c.cinema_id = sf.cinema_id "
                + "WHERE sf.staff_id = ? "
                + "AND u.status = 'ACTIVE' "
                + "AND c.status = 'ACTIVE'";

        try (Connection connection = openConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, staffId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt("cinema_id");
                }
            }
        }

        return 0;
    }

    public Map<Integer, String> findReceiptNames(
            int orderId,
            int cinemaId) throws SQLException {

        Map<Integer, String> names = new HashMap<>();

        String sql = "SELECT oi.food_order_item_id, "
                + "COALESCE(f.item_name, c.combo_name) AS product_name "
                + "FROM Food_Order_Item oi "
                + "JOIN Food_Order o ON o.food_order_id = oi.food_order_id "
                + "LEFT JOIN Food_Item f ON f.food_item_id = oi.food_item_id "
                + "LEFT JOIN Combo c ON c.combo_id = oi.combo_id "
                + "WHERE o.food_order_id = ? AND o.cinema_id = ?";

        try (Connection connection = openConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);
            statement.setInt(2, cinemaId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    names.put(
                            resultSet.getInt("food_order_item_id"),
                            resultSet.getString("product_name")
                    );
                }
            }
        }

        return names;
    }

    private Connection openConnection() throws SQLException {

        Connection connection = new DBContext().getConnection();

        if (connection == null) {
            throw new SQLException("Không thể kết nối database.");
        }

        return connection;
    }

    private void setOrderData(
            PreparedStatement statement,
            FoodOrder foodOrder) throws SQLException {

        if (foodOrder.getStaffId() <= 0) {
            statement.setNull(1, Types.INTEGER);
        } else {
            statement.setInt(1, foodOrder.getStaffId());
        }
        statement.setInt(2, foodOrder.getCinemaId());
        statement.setBigDecimal(3, foodOrder.getSubtotal());
        statement.setBigDecimal(4, foodOrder.getTotalAmount());
        statement.setString(5, foodOrder.getPaymentMethod());
        statement.setString(6, foodOrder.getPaymentStatus());
        statement.setString(7, foodOrder.getOrderStatus());

        if (foodOrder.getBookingId() == null) {
            statement.setNull(8, Types.INTEGER);

        } else {
            statement.setInt(8, foodOrder.getBookingId());
        }
    }

    private FoodOrder mapFoodOrder(ResultSet resultSet)
            throws SQLException {

        FoodOrder foodOrder = new FoodOrder();

        foodOrder.setFoodOrderId(
                resultSet.getInt("food_order_id"));

        foodOrder.setStaffId(
                resultSet.getInt("staff_id"));

        foodOrder.setCinemaId(
                resultSet.getInt("cinema_id"));

        int bookingId = resultSet.getInt("booking_id");

        if (resultSet.wasNull()) {
            foodOrder.setBookingId(null);

        } else {
            foodOrder.setBookingId(bookingId);
        }

        foodOrder.setSubtotal(
                resultSet.getBigDecimal("subtotal"));

        foodOrder.setTotalAmount(
                resultSet.getBigDecimal("total_amount"));

        foodOrder.setPaymentMethod(
                resultSet.getString("payment_method"));

        foodOrder.setPaymentStatus(
                resultSet.getString("payment_status"));

        foodOrder.setOrderStatus(
                resultSet.getString("order_status"));

        foodOrder.setCreatedAt(
                resultSet.getTimestamp("created_at"));

        return foodOrder;
    }

    public List<Map<String, Object>> findOnlineItemsByBooking(
            int bookingId) throws SQLException {

        List<Map<String, Object>> items = new ArrayList<>();

        String sql
                = "SELECT "
                + "COALESCE(f.item_name, c.combo_name) AS product_name, "
                + "oi.quantity, oi.unit_price, oi.line_total, "
                + "o.payment_status "
                + "FROM dbo.Food_Order o "
                + "JOIN dbo.Food_Order_Item oi "
                + "ON oi.food_order_id = o.food_order_id "
                + "LEFT JOIN dbo.Food_Item f "
                + "ON f.food_item_id = oi.food_item_id "
                + "LEFT JOIN dbo.Combo c "
                + "ON c.combo_id = oi.combo_id "
                + "WHERE o.booking_id = ? "
                + "AND o.staff_id IS NULL "
                + "AND o.payment_method = 'VNPAY' "
                + "AND o.payment_status IN ('PENDING', 'PAID') "
                + "ORDER BY o.food_order_id, oi.food_order_item_id";

        try (Connection conn = openConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Map<String, Object> item = new HashMap<>();

                    item.put("productName",
                            rs.getString("product_name"));

                    item.put("quantity",
                            rs.getInt("quantity"));

                    item.put("unitPrice",
                            rs.getBigDecimal("unit_price"));

                    item.put("lineTotal",
                            rs.getBigDecimal("line_total"));

                    item.put("paymentStatus",
                            rs.getString("payment_status"));

                    items.add(item);
                }
            }
        }

        return items;
    }
}
