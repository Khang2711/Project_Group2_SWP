package model;

import java.math.BigDecimal;
import java.sql.Date;

/** Model cho bảng Promotion. Chỉ chứa dữ liệu, chưa có xử lý nghiệp vụ. */
public class Promotion {
    private int promotionId; // promotion_id
    private int cinemaId; // cinema_id
    private String promoCode; // promo_code
    private String promotionName; // promotion_name
    private String discountType; // discount_type
    private BigDecimal discountValue; // discount_value
    private Date startDate; // start_date
    private Date endDate; // end_date
    private Integer usageLimit; // usage_limit
    private String status; // status

    public Promotion() {
    }

    public Promotion(int promotionId, int cinemaId, String promoCode, String promotionName, String discountType, BigDecimal discountValue, Date startDate, Date endDate, Integer usageLimit, String status) {
        this.promotionId = promotionId;
        this.cinemaId = cinemaId;
        this.promoCode = promoCode;
        this.promotionName = promotionName;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.usageLimit = usageLimit;
        this.status = status;
    }

    public int getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(int promotionId) {
        this.promotionId = promotionId;
    }

    public int getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(int cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getPromotionName() {
        return promotionName;
    }

    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
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

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(Integer usageLimit) {
        this.usageLimit = usageLimit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
