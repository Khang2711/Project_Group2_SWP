package model;

public class CartItem {
    private String showtimeID;
    private String movieTitle;
    private String roomName;
    private String showDate;
    private String startTime;
    private String endTime;
    private String seatID;
    private float price;

    public CartItem() {
    }

    public CartItem(String showtimeID, String movieTitle, String roomName, String showDate,
            String startTime, String endTime, String seatID, float price) {
        this.showtimeID = showtimeID;
        this.movieTitle = movieTitle;
        this.roomName = roomName;
        this.showDate = showDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.seatID = seatID;
        this.price = price;
    }

    public String getShowtimeID() {
        return showtimeID;
    }

    public void setShowtimeID(String showtimeID) {
        this.showtimeID = showtimeID;
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

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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

    public String getKey() {
        return showtimeID + "_" + seatID;
    }
}
