package model;

import java.math.BigDecimal;

/** Model cho bảng Combo. */
public class Combo {
    private int comboId;
    private int cinemaId;
    private String comboName;
    private String description;
    private BigDecimal comboPrice;
    private String imageUrl;
    private String status;

    public Combo() {}

    public Combo(int comboId, int cinemaId, String comboName, String description,
                 BigDecimal comboPrice, String imageUrl, String status) {
        this.comboId = comboId;
        this.cinemaId = cinemaId;
        this.comboName = comboName;
        this.description = description;
        this.comboPrice = comboPrice;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    public int getComboId() { return comboId; }
    public void setComboId(int comboId) { this.comboId = comboId; }
    public int getCinemaId() { return cinemaId; }
    public void setCinemaId(int cinemaId) { this.cinemaId = cinemaId; }
    public String getComboName() { return comboName; }
    public void setComboName(String comboName) { this.comboName = comboName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getComboPrice() { return comboPrice; }
    public void setComboPrice(BigDecimal comboPrice) { this.comboPrice = comboPrice; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
