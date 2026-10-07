package service;

import java.util.List;
import model.Room;
import repository.RoomRepository;

public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService() {
        roomRepository = new RoomRepository();
    }

    public List<Room> getRoomsByCinema(int cinemaId) {

        if (cinemaId <= 0) {
            throw new IllegalArgumentException("Rạp không hợp lệ.");
        }

        return roomRepository.findByCinemaId(cinemaId);
    }

    public Room getRoom(int roomId, int cinemaId) {

        if (roomId <= 0 || cinemaId <= 0) {
            throw new IllegalArgumentException("Phòng không hợp lệ.");
        }

        return roomRepository.findByIdAndCinema(roomId, cinemaId);
    }

    public void createRoom(int cinemaId, String roomName,
            int capacity, String roomType, String status) {

        validateRoom(
                cinemaId,
                null,
                roomName,
                capacity,
                roomType,
                status
        );

        Room room = new Room(
                0,
                cinemaId,
                roomName.trim(),
                capacity,
                normalizeRoomType(roomType),
                status
        );

        if (!roomRepository.insert(room)) {
            throw new IllegalArgumentException(
                    "Không thể thêm phòng."
            );
        }
    }

    public void updateRoom(int roomId, int cinemaId,
            String roomName, int capacity,
            String roomType, String status) {

        Room currentRoom =
                roomRepository.findByIdAndCinema(roomId, cinemaId);

        if (currentRoom == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy phòng."
            );
        }

        validateRoom(
                cinemaId,
                roomId,
                roomName,
                capacity,
                roomType,
                status
        );

        int seatCount = roomRepository.countSeats(roomId);

        if (capacity < seatCount) {
            throw new IllegalArgumentException(
                    "Sức chứa không được nhỏ hơn số ghế hiện có: "
                    + seatCount + "."
            );
        }

        if ("ACTIVE".equals(currentRoom.getStatus())
                && "INACTIVE".equals(status)
                && roomRepository.hasFutureActiveShowtime(roomId)) {

            throw new IllegalArgumentException(
                    "Không thể ngừng hoạt động phòng "
                    + "đang có suất chiếu trong tương lai."
            );
        }

        Room room = new Room(
                roomId,
                cinemaId,
                roomName.trim(),
                capacity,
                normalizeRoomType(roomType),
                status
        );

        if (!roomRepository.update(room)) {
            throw new IllegalArgumentException(
                    "Không thể cập nhật phòng."
            );
        }
    }

    private void validateRoom(int cinemaId, Integer roomId,
            String roomName, int capacity,
            String roomType, String status) {

        if (cinemaId <= 0) {
            throw new IllegalArgumentException("Rạp không hợp lệ.");
        }

        if (roomName == null || roomName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Tên phòng không được để trống."
            );
        }

        if (roomName.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Tên phòng không được vượt quá 100 ký tự."
            );
        }

        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Sức chứa phải lớn hơn 0."
            );
        }

        if (roomType != null && roomType.trim().length() > 50) {
            throw new IllegalArgumentException(
                    "Loại phòng không được vượt quá 50 ký tự."
            );
        }

        if (!"ACTIVE".equals(status)
                && !"INACTIVE".equals(status)) {

            throw new IllegalArgumentException(
                    "Trạng thái phòng không hợp lệ."
            );
        }

        if (roomRepository.existsRoomName(
                cinemaId,
                roomName.trim(),
                roomId)) {

            throw new IllegalArgumentException(
                    "Tên phòng đã tồn tại trong rạp."
            );
        }
    }

    private String normalizeRoomType(String roomType) {

        if (roomType == null || roomType.trim().isEmpty()) {
            return null;
        }

        return roomType.trim();
    }
}