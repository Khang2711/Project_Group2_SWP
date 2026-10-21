package service;

import java.util.List;
import model.Room;
import repository.RoomRepository;

public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService() {
        roomRepository = new RoomRepository();
    }

    public List<Room> getRoomsByCinema(
            int cinemaId) {

        if (cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Rạp không hợp lệ."
            );
        }

        return roomRepository
                .findByCinemaId(cinemaId);
    }

    public Room getRoom(
            int roomId,
            int cinemaId) {

        if (roomId <= 0
                || cinemaId <= 0) {

            throw new IllegalArgumentException(
                    "Phòng không hợp lệ."
            );
        }

        return roomRepository
                .findByIdAndCinema(
                        roomId,
                        cinemaId
                );
    }

    // =========================================================
    // TẠO PHÒNG
    // Không còn cố định số ghế theo 2D/3D/IMAX.
    // =========================================================
    public void createRoom(
            int cinemaId,
            String roomName,
            String roomType,
            int rowCount,
            int seatsPerRow,
            String status) {

        String normalizedType =
                normalizeRoomType(roomType);

        validateLayout(
                rowCount,
                seatsPerRow
        );

        int capacity =
                rowCount * seatsPerRow;

        validateRoom(
                cinemaId,
                null,
                roomName,
                normalizedType,
                status
        );

        Room room =
                new Room(
                        0,
                        cinemaId,
                        roomName.trim(),
                        capacity,
                        normalizedType,
                        status
                );

        if (!roomRepository
                .insertWithSeats(
                        room,
                        rowCount,
                        seatsPerRow
                )) {

            throw new IllegalArgumentException(
                    "Không thể tạo phòng và sơ đồ ghế."
            );
        }
    }

    // =========================================================
    // SỬA PHÒNG
    //
    // Capacity không nhập trực tiếp.
    // Muốn đổi số ghế -> Quản lý ghế.
    // =========================================================
    public void updateRoom(
            int roomId,
            int cinemaId,
            String roomName,
            String roomType,
            String status) {

        Room currentRoom =
                roomRepository
                        .findByIdAndCinema(
                                roomId,
                                cinemaId
                        );

        if (currentRoom == null) {

            throw new IllegalArgumentException(
                    "Không tìm thấy phòng."
            );
        }

        String normalizedType =
                normalizeRoomType(roomType);

        validateRoom(
                cinemaId,
                roomId,
                roomName,
                normalizedType,
                status
        );

        if ("ACTIVE".equals(
                currentRoom.getStatus())
                && "INACTIVE".equals(status)
                && roomRepository
                        .hasFutureActiveShowtime(
                                roomId
                        )) {

            throw new IllegalArgumentException(
                    "Không thể ngừng hoạt động phòng "
                    + "đang có suất chiếu trong tương lai."
            );
        }

        Room room =
                new Room(
                        roomId,
                        cinemaId,
                        roomName.trim(),
                        currentRoom.getCapacity(),
                        normalizedType,
                        status
                );

        if (!roomRepository.update(room)) {

            throw new IllegalArgumentException(
                    "Không thể cập nhật phòng."
            );
        }
    }

    public void deleteRoom(
            int roomId,
            int cinemaId) {

        if (roomId <= 0
                || cinemaId <= 0) {

            throw new IllegalArgumentException(
                    "Phòng không hợp lệ."
            );
        }

        Room room =
                roomRepository
                        .findByIdAndCinema(
                                roomId,
                                cinemaId
                        );

        if (room == null) {

            throw new IllegalArgumentException(
                    "Không tìm thấy phòng thuộc rạp của bạn."
            );
        }

        if (roomRepository
                .hasAnyShowtime(roomId)) {

            throw new IllegalArgumentException(
                    "Không thể xóa phòng đã có lịch sử suất chiếu."
            );
        }

        if (roomRepository
                .hasSeatTicketHistory(roomId)) {

            throw new IllegalArgumentException(
                    "Không thể xóa phòng vì ghế đã có lịch sử vé."
            );
        }

        if (!roomRepository
                .deleteRoomWithSeats(
                        roomId,
                        cinemaId
                )) {

            throw new IllegalArgumentException(
                    "Không thể xóa phòng."
            );
        }
    }

    private void validateLayout(
            int rowCount,
            int seatsPerRow) {

        if (rowCount <= 0) {

            throw new IllegalArgumentException(
                    "Số hàng phải lớn hơn 0."
            );
        }

        if (seatsPerRow <= 0) {

            throw new IllegalArgumentException(
                    "Số ghế mỗi hàng phải lớn hơn 0."
            );
        }

        if (rowCount > 30) {

            throw new IllegalArgumentException(
                    "Số hàng tối đa là 30."
            );
        }

        if (seatsPerRow > 50) {

            throw new IllegalArgumentException(
                    "Số ghế mỗi hàng tối đa là 50."
            );
        }

        if (rowCount * seatsPerRow > 500) {

            throw new IllegalArgumentException(
                    "Một phòng không được vượt quá 500 ghế."
            );
        }
    }

    private void validateRoom(
            int cinemaId,
            Integer roomId,
            String roomName,
            String roomType,
            String status) {

        if (cinemaId <= 0) {

            throw new IllegalArgumentException(
                    "Rạp không hợp lệ."
            );
        }

        if (roomName == null
                || roomName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Tên phòng không được để trống."
            );
        }

        if (roomName.trim().length() > 100) {

            throw new IllegalArgumentException(
                    "Tên phòng không được vượt quá 100 ký tự."
            );
        }

        validateRoomType(roomType);

        if (!"ACTIVE".equals(status)
                && !"INACTIVE".equals(status)) {

            throw new IllegalArgumentException(
                    "Trạng thái phòng không hợp lệ."
            );
        }

        if (roomRepository
                .existsRoomName(
                        cinemaId,
                        roomName.trim(),
                        roomId
                )) {

            throw new IllegalArgumentException(
                    "Tên phòng đã tồn tại trong rạp."
            );
        }
    }

    private void validateRoomType(
            String roomType) {

        if (!"2D".equals(roomType)
                && !"3D".equals(roomType)
                && !"IMAX".equals(roomType)) {

            throw new IllegalArgumentException(
                    "Loại phòng không hợp lệ."
            );
        }
    }

    private String normalizeRoomType(
            String roomType) {

        if (roomType == null
                || roomType.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn loại phòng."
            );
        }

        String type =
                roomType.trim().toUpperCase();

        validateRoomType(type);

        return type;
    }
}