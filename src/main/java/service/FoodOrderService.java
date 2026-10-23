package service;

import java.math.BigDecimal;
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

        List<FoodItem> foods = getFoodsForCounter(order.getCinemaId());
        List<Combo> combos = getCombosForCounter(order.getCinemaId());

        List<String> selectedKeys = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (FoodOrderItem item : items) {
            validateItem(item);

            BigDecimal price = null;
            String key;

            // Tìm sản phẩm trong danh sách ACTIVE của đúng rạp.
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

            // Giá thanh toán lấy từ database, không lấy từ trình duyệt.
            item.setUnitPrice(price);

            BigDecimal lineTotal = price.multiply(
                    BigDecimal.valueOf(item.getQuantity()));

            item.setLineTotal(lineTotal);
            total = total.add(lineTotal);
        }

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
            int orderId, int cinemaId) {

        try {
            return foodOrderRepository.findReceiptNames(orderId, cinemaId);

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
