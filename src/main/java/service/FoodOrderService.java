package service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.Combo;
import model.FoodItem;
import model.FoodOrder;
import model.FoodOrderItem;
import repository.FoodOrderItemRepository;
import repository.FoodOrderRepository;

public class FoodOrderService {

    private final FoodOrderRepository foodOrderRepository
            = new FoodOrderRepository();

    private final FoodOrderItemRepository foodOrderItemRepository
            = new FoodOrderItemRepository();

    public int getStaffCinema(int staffId) {

        try {
            int cinemaId = foodOrderRepository.findStaffCinema(staffId);

            if (cinemaId <= 0) {
                throw new IllegalArgumentException(
                        "Nhân viên chưa được phân công rạp đang hoạt động.");
            }

            return cinemaId;

        } catch (SQLException e) {
            e.printStackTrace();

            throw new IllegalStateException(
                    "Không thể kiểm tra rạp của nhân viên.");
        }
    }

    public List<FoodItem> getFoodsForCounter(int cinemaId) {

        try {
            return foodOrderRepository.findFoodsForCounter(cinemaId);

        } catch (SQLException e) {
            e.printStackTrace();

            throw new IllegalStateException(
                    "Không thể tải danh sách món ăn, nước uống.");
        }
    }

    public List<Combo> getCombosForCounter(int cinemaId) {

        try {
            return foodOrderRepository.findCombosForCounter(cinemaId);

        } catch (SQLException e) {
            e.printStackTrace();

            throw new IllegalStateException(
                    "Không thể tải danh sách combo.");
        }
    }

    public int createPaidOrder(
            FoodOrder order,
            List<FoodOrderItem> items) {

        validateOrder(order, items);

        if (getStaffCinema(order.getStaffId()) != order.getCinemaId()) {
            throw new IllegalArgumentException(
                    "Nhân viên không thuộc rạp của đơn hàng.");
        }

        if (order.getBookingId() != null) {
            validateBookingForFood(
                    order.getBookingId(),
                    order.getStaffId(),
                    order.getCinemaId()
            );
        }

        BigDecimal total = calculateSubtotal(
                order.getCinemaId(), items);

        order.setSubtotal(total);
        order.setTotalAmount(total);
        order.setPaymentMethod("CASH");
        order.setPaymentStatus("PAID");
        order.setOrderStatus("COMPLETED");

        int orderId = foodOrderRepository.insertWithItems(order, items);

        if (orderId <= 0) {
            throw new IllegalStateException(
                    "Không thể lưu đơn food/combo.");
        }

        return orderId;
    }

    public BigDecimal calculateSubtotal(
            int cinemaId,
            List<FoodOrderItem> items) {

        if (cinemaId <= 0 || items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Đơn bắp nước phải có ít nhất một sản phẩm hợp lệ.");
        }

        List<FoodItem> foods = getFoodsForCounter(cinemaId);
        List<Combo> combos = getCombosForCounter(cinemaId);

        List<String> selectedKeys = new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;

        for (FoodOrderItem item : items) {

            validateItem(item);

            BigDecimal price = null;
            String key;

            if (item.getFoodItemId() != null) {
                key = "FOOD-" + item.getFoodItemId();

                for (FoodItem food : foods) {
                    if (food.getFoodItemId() == item.getFoodItemId()) {
                        price = food.getPrice();
                        break;
                    }
                }

            } else {
                key = "COMBO-" + item.getComboId();

                for (Combo combo : combos) {
                    if (combo.getComboId() == item.getComboId()) {
                        price = combo.getComboPrice();
                        break;
                    }
                }
            }

            if (selectedKeys.contains(key)) {
                throw new IllegalArgumentException(
                        "Sản phẩm bị lặp trong đơn hàng.");
            }

            selectedKeys.add(key);

            if (price == null || price.signum() < 0) {
                throw new IllegalArgumentException(
                        "Sản phẩm không thuộc rạp hoặc đã ngừng bán. "
                        + "Vui lòng chọn lại.");
            }

            // Luôn lấy giá từ database.
            item.setUnitPrice(price);

            BigDecimal lineTotal = price.multiply(
                    BigDecimal.valueOf(item.getQuantity())
            );

            item.setLineTotal(lineTotal);
            total = total.add(lineTotal);
        }

        return total;
    }

    // Lưu bắp nước trong transaction chung với đơn vé.
    // TicketService sẽ commit sau khi tất cả đều lưu thành công.
    public int createPaidOrder(
            Connection connection,
            FoodOrder order,
            List<FoodOrderItem> items,
            BigDecimal expectedTotal) throws SQLException {

        if (connection == null || connection.getAutoCommit()) {
            throw new SQLException(
                    "Đơn mua kèm phải được lưu trong transaction chung.");
        }

        validateOrder(order, items);

        if (getStaffCinema(order.getStaffId()) != order.getCinemaId()) {
            throw new IllegalArgumentException(
                    "Nhân viên không thuộc rạp của đơn hàng.");
        }

        BigDecimal total = calculateSubtotal(
                order.getCinemaId(), items);

        if (expectedTotal == null
                || total.compareTo(expectedTotal) != 0) {

            throw new IllegalArgumentException(
                    "Giá bắp nước/combo đã thay đổi. "
                    + "Vui lòng kiểm tra lại tổng tiền trước khi xác nhận.");
        }

        order.setSubtotal(total);
        order.setTotalAmount(total);
        order.setPaymentMethod("CASH");
        order.setPaymentStatus("PAID");
        order.setOrderStatus("COMPLETED");

        int orderId = foodOrderRepository.insert(
                connection, order);

        if (orderId <= 0) {
            throw new SQLException(
                    "Không thể lưu đơn bắp nước mua kèm.");
        }

        for (FoodOrderItem item : items) {

            item.setFoodOrderId(orderId);

            int itemId = foodOrderItemRepository.insert(
                    connection, item);

            if (itemId <= 0) {
                throw new SQLException(
                        "Không thể lưu sản phẩm mua kèm.");
            }
        }

        order.setFoodOrderId(orderId);

        return orderId;
    }

    public void validateBookingForFood(
            int bookingId,
            int staffId,
            int cinemaId) {

        if (bookingId <= 0 || staffId <= 0 || cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Thông tin đơn vé đi kèm không hợp lệ.");
        }

        try {
            boolean valid
                    = foodOrderRepository.isBookingAvailableForFood(
                            bookingId, staffId, cinemaId);

            if (!valid) {
                throw new IllegalArgumentException(
                        "Chỉ được mua kèm đơn vé đã xác nhận "
                        + "do bạn bán tại rạp này.");
            }

        } catch (SQLException e) {
            e.printStackTrace();

            throw new IllegalStateException(
                    "Không thể kiểm tra đơn vé đi kèm.");
        }
    }

    public List<FoodOrder> getOrdersByBooking(
            int bookingId,
            int staffId) {

        if (bookingId <= 0 || staffId <= 0) {
            throw new IllegalArgumentException(
                    "Thông tin đơn vé hoặc nhân viên không hợp lệ.");
        }

        int cinemaId = getStaffCinema(staffId);

        try {
            return foodOrderRepository.findCompletedByBooking(
                    bookingId, staffId, cinemaId);

        } catch (SQLException e) {
            e.printStackTrace();

            throw new IllegalStateException(
                    "Không thể tải các đơn bắp nước đi kèm.");
        }
    }

    public FoodOrder getOrderById(int orderId) {

        if (orderId <= 0) {
            return null;
        }

        return foodOrderRepository.getById(orderId);
    }

    public List<FoodOrderItem> getOrderItems(int orderId) {

        if (orderId <= 0) {
            return List.of();
        }

        return foodOrderItemRepository.getByFoodOrderId(orderId);
    }

    public Map<Integer, String> getReceiptNames(
            int orderId,
            int cinemaId) {

        try {
            return foodOrderRepository.findReceiptNames(
                    orderId, cinemaId);

        } catch (SQLException e) {
            e.printStackTrace();

            throw new IllegalStateException(
                    "Không thể tải tên sản phẩm trong hóa đơn.");
        }
    }

    private void validateOrder(
            FoodOrder order,
            List<FoodOrderItem> items) {

        if (order == null
                || order.getStaffId() <= 0
                || order.getCinemaId() <= 0) {

            throw new IllegalArgumentException(
                    "Thông tin đơn hàng không hợp lệ.");
        }

        if (!"CASH".equals(order.getPaymentMethod())) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn thanh toán tiền mặt.");
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Đơn hàng phải có ít nhất một sản phẩm.");
        }
    }

    private void validateItem(FoodOrderItem item) {

        if (item == null || item.getQuantity() <= 0) {
            throw new IllegalArgumentException(
                    "Sản phẩm hoặc số lượng không hợp lệ.");
        }

        boolean hasFood = item.getFoodItemId() != null;
        boolean hasCombo = item.getComboId() != null;

        if (hasFood == hasCombo) {
            throw new IllegalArgumentException(
                    "Mỗi dòng phải là món ăn hoặc combo.");
        }

        if ((hasFood && item.getFoodItemId() <= 0)
                || (hasCombo && item.getComboId() <= 0)) {

            throw new IllegalArgumentException(
                    "Mã sản phẩm không hợp lệ.");
        }
    }
}
