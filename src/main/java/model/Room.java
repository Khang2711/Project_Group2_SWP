package model;

public class Room {
    private String roomID;
    private String roomName;
    private int totalSeats;
    private boolean status;

    public Room() {
    }

    public Room(String roomID, String roomName, int totalSeats, boolean status) {
        this.roomID = roomID;
        this.roomName = roomName;
        this.totalSeats = totalSeats;
        this.status = status;
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

    public boolean isStatus() {
        return status;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
