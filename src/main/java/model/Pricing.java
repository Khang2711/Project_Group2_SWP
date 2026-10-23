package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

public class Pricing {

    private int pricingId;
    private int cinemaId;

    // NULL = áp dụng cho tất cả phim.
    private Integer movieId;

    // Dùng để hiển thị.
    private String movieTitle;

    // NULL = áp dụng cho tất cả loại phòng.
    private String roomType;

    private String seatType;

    // ALL / WEEKDAY / WEEKEND
    private String dayType;

    // NULL = không phải ngày đặc biệt.
    private Date specialDate;

    // Cả hai NULL = áp dụng cả ngày.
    private Time startTime;
    private Time endTime;

    private BigDecimal price;

    // Khoảng thời gian rule có hiệu lực.
    private Date startDate;
    private Date endDate;

    private String status;

    public Pricing() {
    }

    public Pricing(
            int pricingId,
            int cinemaId,
            Integer movieId,
            String movieTitle,
            String roomType,
            String seatType,
            String dayType,
            Date specialDate,
            Time startTime,
            Time endTime,
            BigDecimal price,
            Date startDate,
            Date endDate,
            String status) {

        this.pricingId = pricingId;
        this.cinemaId = cinemaId;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.roomType = roomType;
        this.seatType = seatType;
        this.dayType = dayType;
        this.specialDate = specialDate;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public String getDayType() {
        return dayType;
    }

    public void setDayType(String dayType) {
        this.dayType = dayType;
    }

    public Date getSpecialDate() {
        return specialDate;
    }

    public void setSpecialDate(Date specialDate) {
        this.specialDate = specialDate;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
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