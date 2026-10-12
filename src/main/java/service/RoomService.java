package service;

import java.util.List;
import model.Room;
import repository.RoomRepository;

public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService() {

        roomRepository =
                new RoomRepository();
    }

    // =========================================================
    // DANH SÁCH PHÒNG
    // =========================================================
    public List<Room> getRoomsByCinema(
            int cinemaId) {

        if (cinemaId <= 0) {

            throw new IllegalArgumentException(
                    "Rạp không hợp lệ."
            );
        }

        return roomRepository
                .findByCinemaId(
                        cinemaId
                );
    }

    // =========================================================
    // LẤY 1 PHÒNG
    // =========================================================
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
    //
    // 2D   = 5 x 8  = 40 ghế
    // 3D   = 5 x 10 = 50 ghế
    // IMAX = 6 x 10 = 60 ghế
    // =========================================================
    public void createRoom(
            int cinemaId,
            String roomName,
            String roomType,
            String status) {

        String normalizedType =
                normalizeRoomType(
                        roomType
                );

        int rowCount =
                getRowCount(
                        normalizedType
                );

        int seatsPerRow =
                getSeatsPerRow(
                        normalizedType
                );

        int capacity =
                rowCount
                * seatsPerRow;

        validateRoom(
                cinemaId,
                null,
                roomName,
                capacity,
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
    // CẬP NHẬT PHÒNG
    //
    // Loại phòng và capacity được giữ cố định.
    // Manager chỉ đổi:
    // - tên phòng
    // - trạng thái
    // =========================================================
    public void updateRoom(
            int roomId,
            int cinemaId,
            String roomName,
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

        validateRoom(
                cinemaId,
                roomId,
                roomName,
                currentRoom.getCapacity(),
                currentRoom.getRoomType(),
                status
        );

        // Không cho tắt phòng đang có suất chiếu tương lai.
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
                        currentRoom.getRoomType(),
                        status
                );

        if (!roomRepository
                .update(room)) {

            throw new IllegalArgumentException(
                    "Không thể cập nhật phòng."
            );
        }
    }

    // =========================================================
    // XÓA PHÒNG
    //
    // Phòng mới được tạo có ghế tự động,
    // nên khi xóa phòng sẽ xóa ghế cùng lúc.
    // =========================================================
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

        // Phòng đã có showtime thì giữ lại lịch sử.
        if (roomRepository
                .hasAnyShowtime(
                        roomId
                )) {

            throw new IllegalArgumentException(
                    "Không thể xóa phòng đã có lịch sử suất chiếu."
            );
        }

        // Kiểm tra thêm lịch sử ticket.
        if (roomRepository
                .hasSeatTicketHistory(
                        roomId
                )) {

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

    // =========================================================
    // LẤY CAPACITY THEO LOẠI PHÒNG
    // Dùng cho Controller/JSP khi có lỗi form.
    // =========================================================
    public int getCapacityByRoomType(
            String roomType) {

        String type =
                normalizeRoomType(
                        roomType
                );

        return getRowCount(type)
                * getSeatsPerRow(type);
    }

    // =========================================================
    // VALIDATE
    // =========================================================
    private void validateRoom(
            int cinemaId,
            Integer roomId,
            String roomName,
            int capacity,
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

        if (roomName.trim().length()
                > 100) {

            throw new IllegalArgumentException(
                    "Tên phòng không được vượt quá 100 ký tự."
            );
        }

        if (capacity <= 0) {

            throw new IllegalArgumentException(
                    "Sức chứa phòng không hợp lệ."
            );
        }

        validateRoomType(
                roomType
        );

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

    // =========================================================
    // VALIDATE ROOM TYPE
    // =========================================================
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

    // =========================================================
    // CHUẨN HÓA ROOM TYPE
    // =========================================================
    private String normalizeRoomType(
            String roomType) {

        if (roomType == null
                || roomType.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn loại phòng."
            );
        }

        String type =
                roomType.trim()
                        .toUpperCase();

        validateRoomType(type);

        return type;
    }

    // =========================================================
    // SỐ HÀNG THEO LOẠI PHÒNG
    // =========================================================
    private int getRowCount(
            String roomType) {

        switch (roomType) {

            case "2D":
                return 5;

            case "3D":
                return 5;

            case "IMAX":
                return 6;

            default:
                throw new IllegalArgumentException(
                        "Loại phòng không hợp lệ."
                );
        }
    }

    // =========================================================
    // SỐ GHẾ MỖI HÀNG
    // =========================================================
    private int getSeatsPerRow(
            String roomType) {

        switch (roomType) {

            case "2D":
                return 8;

            case "3D":
                return 10;

            case "IMAX":
                return 10;

            default:
                throw new IllegalArgumentException(
                        "Loại phòng không hợp lệ."
                );
        }
    }
}