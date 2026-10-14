package model;

import java.math.BigDecimal;

/** Model cho bảng Food_Item. */
public class FoodItem {
    private int foodItemId;
    private int cinemaId;
    private String itemName;
    private String itemType;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String status;

    public FoodItem() {
    }

    public FoodItem(int foodItemId, int cinemaId, String itemName,
            String description, BigDecimal price, String imageUrl,
            String status) {
        this(foodItemId, cinemaId, itemName, null, description, price, imageUrl, status);
    }

    public FoodItem(int foodItemId, int cinemaId, String itemName,
            String itemType, String description, BigDecimal price,
            String imageUrl, String status) {
        this.foodItemId = foodItemId;
        this.cinemaId = cinemaId;
        this.itemName = itemName;
        this.itemType = itemType;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    public int getFoodItemId() { return foodItemId; }
    public void setFoodItemId(int foodItemId) { this.foodItemId = foodItemId; }
    public int getCinemaId() { return cinemaId; }
    public void setCinemaId(int cinemaId) { this.cinemaId = cinemaId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
