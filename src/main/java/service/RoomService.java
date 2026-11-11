
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

    // =====================================================
    // LẤY THÔNG TIN PHÒNG
    // =====================================================

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

    // =====================================================
    // TẠO PHÒNG
    // =====================================================

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

    // =====================================================
    // BUG 04 FIX - CẬP NHẬT PHÒNG
    //
    // 1. Không cho đổi loại phòng đã có lịch chiếu.
    // 2. Không cho vô hiệu hóa phòng còn suất chiếu
    //    ACTIVE trong tương lai.
    // 3. Cho phép sửa tên nếu dữ liệu hợp lệ.
    // =====================================================

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

        // =================================================
        // BUG 04 FIX 1:
        // Kiểm tra có thay đổi loại phòng hay không.
        // =================================================

        boolean roomTypeChanged =
                !normalizedType.equalsIgnoreCase(
                        currentRoom.getRoomType()
                );

        // Phòng đã từng có lịch chiếu thì không cho
        // đổi loại để bảo vệ dữ liệu nghiệp vụ.
        if (roomTypeChanged
                && roomRepository.hasAnyShowtime(roomId)) {

            throw new IllegalArgumentException(
                    "Không thể thay đổi loại phòng "
                    + "vì phòng đã có lịch sử suất chiếu. "
                    + "Vui lòng sử dụng phòng khác "
                    + "nếu cần thay đổi loại phòng."
            );
        }

        // =================================================
        // BUG 04 FIX 2:
        // Không cho vô hiệu hóa phòng khi vẫn còn
        // suất chiếu ACTIVE trong tương lai.
        // =================================================

        boolean disablingRoom =
                "ACTIVE".equalsIgnoreCase(
                        currentRoom.getStatus()
                )
                && "INACTIVE".equalsIgnoreCase(status);

        if (disablingRoom
                && roomRepository
                        .hasFutureActiveShowtime(roomId)) {

            throw new IllegalArgumentException(
                    "Không thể ngừng hoạt động phòng "
                    + "đang có suất chiếu trong tương lai. "
                    + "Vui lòng xử lý các suất chiếu "
                    + "liên quan trước."
            );
        }

        // =================================================
        // CẬP NHẬT PHÒNG
        // Capacity được giữ nguyên.
        // Muốn thay đổi số ghế thì dùng Manage Seats.
        // =================================================

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

    // =====================================================
    // XÓA PHÒNG
    //
    // Không được xóa phòng đã có lịch sử suất chiếu
    // hoặc lịch sử bán vé.
    // =====================================================

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

        // Không xóa phòng đã từng có suất chiếu.
        if (roomRepository
                .hasAnyShowtime(roomId)) {

            throw new IllegalArgumentException(
                    "Không thể xóa phòng "
                    + "đã có lịch sử suất chiếu."
            );
        }

        // Không xóa phòng đã có lịch sử vé.
        if (roomRepository
                .hasSeatTicketHistory(roomId)) {

            throw new IllegalArgumentException(
                    "Không thể xóa phòng "
                    + "vì ghế đã có lịch sử vé."
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

    // =====================================================
    // KIỂM TRA SƠ ĐỒ GHẾ KHI TẠO PHÒNG
    // =====================================================

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

    // =====================================================
    // KIỂM TRA THÔNG TIN PHÒNG
    // =====================================================

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

    // =====================================================
    // KIỂM TRA LOẠI PHÒNG
    // =====================================================

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

    // =====================================================
    // CHUẨN HÓA LOẠI PHÒNG
    // =====================================================

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
