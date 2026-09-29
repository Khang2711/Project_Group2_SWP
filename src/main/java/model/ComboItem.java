package model;

/** Bảng trung gian Combo_Item, lưu các món thuộc một combo. */
public class ComboItem {
    private int comboId;
    private int foodItemId;
    private int quantity;

    public ComboItem() {}

    public ComboItem(int comboId, int foodItemId, int quantity) {
        this.comboId = comboId;
        this.foodItemId = foodItemId;
        this.quantity = quantity;
    }

    public int getComboId() { return comboId; }
    public void setComboId(int comboId) { this.comboId = comboId; }
    public int getFoodItemId() { return foodItemId; }
    public void setFoodItemId(int foodItemId) { this.foodItemId = foodItemId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
