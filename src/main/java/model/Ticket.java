package model;

public class Ticket {
    private String ticketID;
    private String orderID;
    private String movieTitle;
    private String roomName;
    private String showDate;
    private String startTime;
    private String seatID;
    private float price;
    private String orderDate;

    public Ticket() {
    }

    public Ticket(String ticketID, String orderID, String movieTitle, String roomName, String showDate,
            String startTime, String seatID, float price, String orderDate) {
        this.ticketID = ticketID;
        this.orderID = orderID;
        this.movieTitle = movieTitle;
        this.roomName = roomName;
        this.showDate = showDate;
        this.startTime = startTime;
        this.seatID = seatID;
        this.price = price;
        this.orderDate = orderDate;
    }

    public String getTicketID() {
        return ticketID;
    }

    public void setTicketID(String ticketID) {
        this.ticketID = ticketID;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getShowDate() {
        return showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getSeatID() {
        return seatID;
    }

    public void setSeatID(String seatID) {
        this.seatID = seatID;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }
}
