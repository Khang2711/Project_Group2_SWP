package model;

public class ComboItem {
    private String comboID;
    private String comboName;
    private float price;
    private int quantity;

    public ComboItem() {
    }

    public ComboItem(String comboID, String comboName, float price, int quantity) {
        this.comboID = comboID;
        this.comboName = comboName;
        this.price = price;
        this.quantity = quantity;
    }

    public String getComboID() {
        return comboID;
    }

    public void setComboID(String comboID) {
        this.comboID = comboID;
    }

    public String getComboName() {
        return comboName;
    }

    public void setComboName(String comboName) {
        this.comboName = comboName;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getTotal() {
        return price * quantity;
    }
}
