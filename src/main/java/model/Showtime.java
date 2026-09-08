package model;

public class Showtime {
    private String showtimeID;
    private String movieID;
    private String movieTitle;
    private String roomID;
    private String roomName;
    private int totalSeats;
    private String showDate;
    private String startTime;
    private String endTime;
    private boolean status;
    private float basePrice;
    private boolean roomStatus;

    public Showtime() {
    }

    public Showtime(String showtimeID, String movieID, String movieTitle, String roomID, String roomName,
            int totalSeats, String showDate, String startTime, String endTime, boolean status, float basePrice) {
        this.showtimeID = showtimeID;
        this.movieID = movieID;
        this.movieTitle = movieTitle;
        this.roomID = roomID;
        this.roomName = roomName;
        this.totalSeats = totalSeats;
        this.showDate = showDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.basePrice = basePrice;
        this.roomStatus = true;
    }


    public Showtime(String showtimeID, String movieID, String movieTitle, String roomID, String roomName,
            int totalSeats, String showDate, String startTime, String endTime, boolean status, float basePrice, boolean roomStatus) {
        this(showtimeID, movieID, movieTitle, roomID, roomName, totalSeats, showDate, startTime, endTime, status, basePrice);
        this.roomStatus = roomStatus;
    }

    public String getShowtimeID() {
        return showtimeID;
    }

    public void setShowtimeID(String showtimeID) {
        this.showtimeID = showtimeID;
    }

    public String getMovieID() {
        return movieID;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
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

    public boolean isStatus() {
        return status;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public float getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(float basePrice) {
        this.basePrice = basePrice;
    }

    public boolean isRoomStatus() {
        return roomStatus;
    }

    public boolean getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(boolean roomStatus) {
        this.roomStatus = roomStatus;
    }
}
