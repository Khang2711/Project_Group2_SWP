package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class FoodOrder {

    private int foodOrderId;
    private int staffId;
    private int cinemaId;
    private BigDecimal subtotal;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String orderStatus;
    private Timestamp createdAt;

    public FoodOrder() {
    }

    public FoodOrder(
            int foodOrderId,
            int staffId,
            int cinemaId,
            BigDecimal subtotal,
            BigDecimal totalAmount,
            String paymentMethod,
            String paymentStatus,
            String orderStatus,
            Timestamp createdAt) {

        this.foodOrderId = foodOrderId;
        this.staffId = staffId;
        this.cinemaId = cinemaId;
        this.subtotal = subtotal;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.orderStatus = orderStatus;
        this.createdAt = createdAt;
    }

    public int getFoodOrderId() {
        return foodOrderId;
    }

    public void setFoodOrderId(int foodOrderId) {
        this.foodOrderId = foodOrderId;
    }

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public int getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(int cinemaId) {
        this.cinemaId = cinemaId;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
