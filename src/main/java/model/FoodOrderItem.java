package model;

import java.math.BigDecimal;

/** Chi tiết món/combo trong một đơn đồ ăn. */
public class FoodOrderItem {
    private int foodOrderItemId;
    private int foodOrderId;
    private Integer foodItemId;
    private Integer comboId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public FoodOrderItem() {}

    public FoodOrderItem(int foodOrderItemId, int foodOrderId, Integer foodItemId, Integer comboId,
                         int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        this.foodOrderItemId = foodOrderItemId;
        this.foodOrderId = foodOrderId;
        this.foodItemId = foodItemId;
        this.comboId = comboId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public int getFoodOrderItemId() { return foodOrderItemId; }
    public void setFoodOrderItemId(int foodOrderItemId) { this.foodOrderItemId = foodOrderItemId; }
    public int getFoodOrderId() { return foodOrderId; }
    public void setFoodOrderId(int foodOrderId) { this.foodOrderId = foodOrderId; }
    public Integer getFoodItemId() { return foodItemId; }
    public void setFoodItemId(Integer foodItemId) { this.foodItemId = foodItemId; }
    public Integer getComboId() { return comboId; }
    public void setComboId(Integer comboId) { this.comboId = comboId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
