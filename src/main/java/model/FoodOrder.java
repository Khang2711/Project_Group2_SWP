package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** Model cho bảng Food_Order. */
public class FoodOrder {
    private int foodOrderId;
    private Integer bookingId;
    private int customerId;
    private int cinemaId;
    private Timestamp orderTime;
    private BigDecimal totalAmount;
    private String status;

    public FoodOrder() {}

    public FoodOrder(int foodOrderId, Integer bookingId, int customerId, int cinemaId,
                     Timestamp orderTime, BigDecimal totalAmount, String status) {
        this.foodOrderId = foodOrderId;
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.cinemaId = cinemaId;
        this.orderTime = orderTime;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getFoodOrderId() { return foodOrderId; }
    public void setFoodOrderId(int foodOrderId) { this.foodOrderId = foodOrderId; }
    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getCinemaId() { return cinemaId; }
    public void setCinemaId(int cinemaId) { this.cinemaId = cinemaId; }
    public Timestamp getOrderTime() { return orderTime; }
    public void setOrderTime(Timestamp orderTime) { this.orderTime = orderTime; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
