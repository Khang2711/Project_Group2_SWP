package model;

import java.math.BigDecimal;
import java.sql.Date;

/** Model cho bảng Pricing. Chỉ chứa dữ liệu, chưa có xử lý nghiệp vụ. */
public class Pricing {
    private int pricingId; // pricing_id
    private int cinemaId; // cinema_id
    private String seatType; // seat_type
    private BigDecimal price; // price
    private Date startDate; // start_date
    private Date endDate; // end_date
    private String status; // status

    public Pricing() {
    }

    public Pricing(int pricingId, int cinemaId, String seatType, BigDecimal price, Date startDate, Date endDate, String status) {
        this.pricingId = pricingId;
        this.cinemaId = cinemaId;
        this.seatType = seatType;
        this.price = price;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public int getPricingId() {
        return pricingId;
    }

    public void setPricingId(int pricingId) {
        this.pricingId = pricingId;
    }

    public int getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(int cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
