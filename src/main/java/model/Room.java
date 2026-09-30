package model;

/**
 * Represents a screening room in a cinema.
 *
 * @author Kiet
 */
public class Room {

    private int roomId;
    private int cinemaId;
    private String roomName;
    private int capacity;
    private String roomType;
    private String status;

    public Room() {
    }

    public Room(int roomId, int cinemaId, String roomName, int capacity, String roomType, String status) {
        this.roomId = roomId;
        this.cinemaId = cinemaId;
        this.roomName = roomName;
        this.capacity = capacity;
        this.roomType = roomType;
        this.status = status;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(int cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}