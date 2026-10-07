package service;

import java.math.BigDecimal;
import java.util.List;
import model.FoodOrder;
import model.FoodOrderItem;
import repository.FoodOrderItemRepository;
import repository.FoodOrderRepository;

public class FoodOrderService {

    private final FoodOrderRepository foodOrderRepository
            = new FoodOrderRepository();

    private final FoodOrderItemRepository foodOrderItemRepository
            = new FoodOrderItemRepository();

    public int createPaidOrder(
            FoodOrder foodOrder,
            List<FoodOrderItem> items) {

        validateOrder(foodOrder, items);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (FoodOrderItem item : items) {

            validateItem(item);

            BigDecimal lineTotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            item.setLineTotal(lineTotal);
            subtotal = subtotal.add(lineTotal);
        }

        foodOrder.setSubtotal(subtotal);
        foodOrder.setTotalAmount(subtotal);

        foodOrder.setPaymentMethod(
                foodOrder.getPaymentMethod()
                        .trim()
                        .toUpperCase()
        );

        foodOrder.setPaymentStatus("PAID");
        foodOrder.setOrderStatus("COMPLETED");

        int foodOrderId = foodOrderRepository.insertWithItems(
                foodOrder,
                items
        );

        if (foodOrderId <= 0) {
            throw new IllegalStateException(
                    "Không thể tạo đơn đồ ăn/combo."
            );
        }

        return foodOrderId;
    }

    public FoodOrder getOrderById(int foodOrderId) {

        if (foodOrderId <= 0) {
            return null;
        }

        return foodOrderRepository.getById(foodOrderId);
    }

    public List<FoodOrderItem> getOrderItems(int foodOrderId) {

        if (foodOrderId <= 0) {
            return List.of();
        }

        return foodOrderItemRepository.getByFoodOrderId(foodOrderId);
    }

    private void validateOrder(
            FoodOrder foodOrder,
            List<FoodOrderItem> items) {

        if (foodOrder == null) {
            throw new IllegalArgumentException(
                    "Đơn hàng không hợp lệ."
            );
        }

        if (foodOrder.getStaffId() <= 0) {
            throw new IllegalArgumentException(
                    "Nhân viên không hợp lệ."
            );
        }

        if (foodOrder.getCinemaId() <= 0) {
            throw new IllegalArgumentException(
                    "Rạp không hợp lệ."
            );
        }

        if (foodOrder.getPaymentMethod() == null
                || foodOrder.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn phương thức thanh toán."
            );
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Đơn hàng phải có ít nhất một sản phẩm."
            );
        }
    }

    private void validateItem(FoodOrderItem item) {

        if (item == null) {
            throw new IllegalArgumentException(
                    "Sản phẩm trong đơn không hợp lệ."
            );
        }

        boolean hasFoodItem = item.getFoodItemId() != null
                && item.getFoodItemId() > 0;

        boolean hasCombo = item.getComboId() != null
                && item.getComboId() > 0;

        if (hasFoodItem == hasCombo) {
            throw new IllegalArgumentException(
                    "Mỗi dòng chỉ được chọn món ăn hoặc combo."
            );
        }

        if (item.getQuantity() <= 0) {
            throw new IllegalArgumentException(
                    "Số lượng sản phẩm phải lớn hơn 0."
            );
        }

        if (item.getUnitPrice() == null
                || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Giá sản phẩm không hợp lệ."
            );
        }
    }
}
