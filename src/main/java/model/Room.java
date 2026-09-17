package model;


/** Model cho bảng Room. Chỉ chứa dữ liệu, chưa có xử lý nghiệp vụ. */
public class Room {
    private int roomId; // room_id
    private int cinemaId; // cinema_id
    private String roomName; // room_name
    private int capacity; // capacity
    private String status; // status

    public Room() {
    }

    public Room(int roomId, int cinemaId, String roomName, int capacity, String status) {
        this.roomId = roomId;
        this.cinemaId = cinemaId;
        this.roomName = roomName;
        this.capacity = capacity;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
