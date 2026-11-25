
package service;

import java.util.List;
import model.Room;
import repository.RoomRepository;

public class RoomService {

    private final RoomRepository roomRepository;

    /** Khởi tạo Repository phục vụ quản lý phòng. */
    public RoomService() {
        roomRepository = new RoomRepository();
    }

    /** Lấy các phòng thuộc rạp. */
    public List<Room> getRoomsByCinema(int cinemaId) {

        if (cinemaId <= 0) {
            throw new IllegalArgumentException("Rạp không hợp lệ.");
        }

        return roomRepository.findByCinemaId(cinemaId);
    }

    // =====================================================
    // LẤY THÔNG TIN PHÒNG
    // =====================================================

    /** Lấy thông tin một phòng và kiểm tra nó thuộc đúng rạp. */
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

    /** Kiểm tra sơ đồ và tạo phòng kèm ghế. */
    public void createRoom(
            int cinemaId,
            String roomName,
            String roomType,
            int rowCount,
            int seatsPerRow,
            String status) {
        //Chuẩn hóa loại phòng
        String normalizedType =
                normalizeRoomType(roomType);
        //Kiểm tra số hàng, số ghế mỗi hàng và tổng sức chứa có vượt giới hạn không.
        validateLayout(
                rowCount,
                seatsPerRow
        );
        //Tính sức chứa
        int capacity =
                rowCount * seatsPerRow;
        //Kiểm tra thông tin phòng
        validateRoom(
                cinemaId,
                null,
                roomName,
                normalizedType,
                status
        );
        //Tạo đối tượng phòng
        Room room =
                new Room(
                        0,
                        cinemaId,
                        roomName.trim(),
                        capacity,
                        normalizedType,
                        status
                );
        //Lưu phòng và ghế
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
    // 1. Không cho đổi loại phòng đã có lịch chiếu.
    // 2. Không cho vô hiệu hóa phòng còn suất chiếu
    //    ACTIVE trong tương lai.
    // 3. Cho phép sửa tên nếu dữ liệu hợp lệ.
    // =====================================================

    /** Sửa phòng nhưng giữ an toàn lịch sử chiếu. */
    public void updateRoom(
            int roomId,
            int cinemaId,
            String roomName,
            String roomType,
            String status) {
        //Lấy phòng hiện tại
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
        //Kiểm tra thông tin mới
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
        // Không cho vô hiệu hóa phòng khi vẫn còn
        // suất chiếu ACTIVE trong tương lai.
        // =================================================
        //kiểm tra xem Manager có đang chuyển phòng từ ACTIVE sang INACTIVE hay không.
        boolean disablingRoom =
                "ACTIVE".equalsIgnoreCase(
                        currentRoom.getStatus()
                )
                && "INACTIVE".equalsIgnoreCase(status);
        //Nếu phòng có suất chiếu ACTIVE trong tương lai, không cho ngừng hoạt động.
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
        //Gọi Repository cập nhật thông tin phòng. Nếu thất bại thì báo lỗi.
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

    /** Kiểm tra điều kiện trước khi xóa phòng. */
    public void deleteRoom(
            int roomId,
            int cinemaId) {
        //Kiểm tra mã phòng và rạp
        if (roomId <= 0
                || cinemaId <= 0) {

            throw new IllegalArgumentException(
                    "Phòng không hợp lệ."
            );
        }
        //Kiểm tra phòng thuộc rạp
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

    /** Kiểm tra số hàng, ghế và sức chứa tối đa. */
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

    /** Kiểm tra thông tin và tên phòng trùng. */
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

    /** Kiểm tra loại phòng hợp lệ. */
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

    /** Chuẩn hóa loại phòng trước khi lưu. */
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
        //Kiểm tra có thuộc 2D, 3D, IMAX không
        validateRoomType(type);

        return type;
    }
}
