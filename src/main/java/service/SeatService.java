
package service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Room;
import model.Seat;
import repository.RoomRepository;
import repository.SeatRepository;

public class SeatService {

    private final SeatRepository seatRepository;
    private final RoomRepository roomRepository;

    /** Khởi tạo Repository để xử lý nghiệp vụ ghế. */
    public SeatService() {
        seatRepository = new SeatRepository();
        roomRepository = new RoomRepository();
    }

    // =====================================================
    // LẤY DANH SÁCH GHẾ THEO HÀNG
    // =====================================================

    /** Lấy ghế thuộc phòng của quản lý. */
    public List<Seat> getSeatsByRoom(
            int roomId,
            int cinemaId) {
        //Kiểm tra phòng thuộc rạp Manager.
        checkRoom(roomId, cinemaId);

        return seatRepository.findByRoomId(roomId);
    }

    // =====================================================
    // NHÓM GHẾ THEO HÀNG
    // =====================================================

    /** Nhóm danh sách ghế theo hàng. */
    public Map<Integer, List<Seat>> getSeatRowsByRoom(
            int roomId,
            int cinemaId) {

        List<Seat> seats =
                getSeatsByRoom(roomId, cinemaId);
        //Mỗi số hàng tương ứng một danh sách ghế.
        Map<Integer, List<Seat>> rows =
                new LinkedHashMap<>();

        for (Seat seat : seats) {

            rows.computeIfAbsent(
                    seat.getSeatRow(),
                    key -> new ArrayList<>()
            ).add(seat);
        }

        return rows;
    }

    // =====================================================
    // LẤY THÔNG TIN 1 GHẾ
    // =====================================================

    /** Lấy một ghế trong phòng. */
    public Seat getSeat(
            int seatId,
            int roomId,
            int cinemaId) {

        checkRoom(roomId, cinemaId);

        if (seatId <= 0) {
            throw new IllegalArgumentException(
                    "Ghế không hợp lệ."
            );
        }

        return seatRepository.findByIdAndRoom(
                seatId,
                roomId
        );
    }

    // =====================================================
    // THÊM 1 GHẾ
    // =====================================================

    /** Kiểm tra vị trí rồi thêm một ghế. */
    public void createSeat(
            int roomId,
            int cinemaId,
            int seatRow,
            int seatNumber,
            String seatType,
            String status) {
        //Kiểm tra phòng thuộc rạp Manager.
        checkRoom(roomId, cinemaId);
        //Kiểm tra thông tin ghế hợp lệ
        validateSeat(
                roomId,
                null,
                seatRow,
                seatNumber,
                seatType,
                status
        );
        //Tạo đối tượng Seat mới trong Java
        Seat seat = new Seat(
                0,
                roomId,
                seatRow,
                seatNumber,
                normalizeSeatType(seatType),
                status
        );
        //Gọi Repository lưu ghế.
        if (!seatRepository.insert(seat)) {
            throw new IllegalArgumentException(
                    "Không thể thêm ghế."
            );
        }
        //Đếm lại ghế và cập nhật sức chứa phòng.
        roomRepository.syncCapacityFromSeats(roomId);
    }

    // =====================================================
    // Thên nguyên 1 hàng ghế mới
    //
    // Vi du:
    // Dang co hang A, B, C
    // -> Tao hang D voi 8 ghe.
    // =====================================================

    /** Thêm hàng ghế mới sau hàng cuối. */
    public void addRow(
            int roomId,
            int cinemaId,
            int seatCount,
            String seatType) {
        //Kiểm tra phòng và số ghế cần thêm.
        checkRoom(roomId, cinemaId);

        if (seatCount <= 0 || seatCount > 50) {
            throw new IllegalArgumentException(
                    "Số ghế phải từ 1 đến 50."
            );
        }
        //Chuẩn hóa loại ghế.
        String normalizedType =
                normalizeSeatType(seatType);
        //getMaxRow(): Lấy số thứ tự hàng lớn nhất hiện có.
        //+ 1: Tạo hàng mới sau hàng cuối cùng.
        int nextRow =
                seatRepository.getMaxRow(roomId) + 1;

        if (nextRow > 30) {
            throw new IllegalArgumentException(
                    "Phòng tối đa 30 hàng ghế."
            );
        }

        // Repository them ghe va dong bo capacity
        // trong cung mot transaction.
        boolean created =
                seatRepository.insertRowAtomic(
                        roomId,
                        nextRow,
                        seatCount,
                        normalizedType
                );

        if (!created) {
            throw new IllegalArgumentException(
                    "Không thể thêm hàng ghế."
            );
        }
    }

    // =====================================================
    // Thêm ghế vào hàng đã có
    //
    // Vi du:
    // Hang C co C1 - C8.
    // Chon hang C va them 5 ghe.
    // -> Them C9 - C13.
    //
    // Khong tao hang moi.
    // Khong sua ghe cu.
    // Khong lam thay doi ticket cu.
    // =====================================================

    /** Thêm ghế nối tiếp vào hàng có sẵn. */
    public void addSeatsToExistingRow(
            int roomId,
            int cinemaId,
            int seatRow,
            int seatCount,
            String seatType) {
        //Xác minh phòng thuộc đúng rạp.
        checkRoom(roomId, cinemaId);
        //Kiểm tra hàng và số lượng ghế cần thêm.
        if (seatRow <= 0 || seatRow > 30) {
            throw new IllegalArgumentException(
                    "Hàng ghế không hợp lệ."
            );
        }

        if (seatCount <= 0 || seatCount > 50) {
            throw new IllegalArgumentException(
                    "Số ghế cần thêm phải từ 1 đến 50."
            );
        }

        String normalizedType =
                normalizeSeatType(seatType);

        // Phải là hàng đã tồn tại.
        boolean rowExists = false;

        List<Seat> seats =
                seatRepository.findByRoomId(roomId);
        //Duyệt toàn bộ ghế trong phòng để tìm hàng muốn thêm.
        for (Seat seat : seats) {
            if (seat.getSeatRow() == seatRow) {
                rowExists = true;
                break;
            }
        }

        if (!rowExists) {
            throw new IllegalArgumentException(
                    "Hàng ghế chưa tồn tại. "
                    + "Vui lòng dùng chức năng Thêm hàng mới."
            );
        }

        // Ham nay se:
        // 1. Khoa phong.
        // 2. Lay so ghe lon nhat cua hang.
        // 3. Them cac ghe tiep theo.
        // 4. Cap nhat Room.capacity.
        // 5. Commit hoac rollback toan bo.
        boolean created =
                seatRepository.appendSeatsToRowAtomic(
                        roomId,
                        seatRow,
                        seatCount,
                        normalizedType
                );

        if (!created) {
            throw new IllegalArgumentException(
                    "Không thể thêm ghế vào hàng đã chọn."
            );
        }
    }

    // =====================================================
    // CẬP NHẬT 1 GHẾ
    //
    // Khong cho thay doi vi tri, loai ghe
    // hoac trang thai khi ghe da co ticket.
    // =====================================================

    /** Sửa ghế nhưng bảo vệ ghế đã có vé. */
    public void updateSeat(
            int seatId,
            int roomId,
            int cinemaId,
            int seatRow,
            int seatNumber,
            String seatType,
            String status) {
        //Kiểm tra phòng thuộc rạp, sau đó lấy ghế muốn sửa.
        checkRoom(roomId, cinemaId);

        Seat currentSeat =
                seatRepository.findByIdAndRoom(
                        seatId,
                        roomId
                );

        if (currentSeat == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy ghế."
            );
        }
        //Kiểm tra vị trí, loại ghế, trạng thái và trùng vị trí.
        validateSeat(
                roomId,
                seatId,
                seatRow,
                seatNumber,
                seatType,
                status
        );

        String normalizedSeatType =
                normalizeSeatType(seatType);

        boolean positionChanged =
                currentSeat.getSeatRow() != seatRow
                || currentSeat.getSeatNumber()
                != seatNumber;
        //positionChanged bằng true nếu hàng hoặc số ghế thay đổi.
        boolean typeChanged =
                !normalizedSeatType.equalsIgnoreCase(
                        currentSeat.getSeatType()
                );
        //
        boolean statusChanged =
                !status.equalsIgnoreCase(
                        currentSeat.getStatus()
                );
        //statusChanged bằng true nếu trạng thái thay đổi.
        if ((positionChanged
                || typeChanged
                || statusChanged)
                && seatRepository.hasTicketHistory(seatId)) {

            throw new IllegalArgumentException(
                    "Không thể thay đổi vị trí, loại ghế "
                    + "hoặc trạng thái của ghế đã có vé."
            );
        }
        //Tạo đối tượng chứa dữ liệu mới rồi gọi Repository cập nhật.
        Seat seat = new Seat(
                seatId,
                roomId,
                seatRow,
                seatNumber,
                normalizedSeatType,
                status
        );

        if (!seatRepository.update(seat)) {
            throw new IllegalArgumentException(
                    "Không thể cập nhật ghế."
            );
        }
    }

    // =====================================================
    // CẬP NHẬT NHIỀU GHẾ
    // =====================================================

    /** Kiểm tra rồi sửa nhiều ghế cùng lúc. */
    public void bulkUpdateSeats(
            List<Integer> seatIds,
            int roomId,
            int cinemaId,
            String seatType,
            String status) {

        checkRoom(roomId, cinemaId);
        //Kiểm tra danh sách ghế được chọn hợp lệ.
        List<Integer> ids =
                validateSelectedSeats(
                        seatIds,
                        roomId
                );
        //Hai giá trị này có thể là null nếu Manager không muốn sửa trường tương ứng.
        String normalizedSeatType =
                normalizeOptionalSeatType(seatType);

        String normalizedStatus =
                normalizeOptionalStatus(status);
        //Nếu không chọn sửa loại hoặc trạng thái thì báo lỗi.
        if (normalizedSeatType == null
                && normalizedStatus == null) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn thông tin cần cập nhật."
            );
        }
        //Sau đó Service duyệt từng ghế
        for (Integer seatId : ids) {
            //Trong vòng lặp, Service kiểm tra thông tin ghế hiện tại và xác định loại/trạng thái có thay đổi hay không.
            Seat currentSeat =
                    seatRepository.findByIdAndRoom(
                            seatId,
                            roomId
                    );
                  
            if (currentSeat == null) {
                throw new IllegalArgumentException(
                        "Không tìm thấy ghế ID: " + seatId
                );
            }

            boolean typeChanged =
                    normalizedSeatType != null
                    && !normalizedSeatType.equalsIgnoreCase(
                            currentSeat.getSeatType()
                    );

            boolean statusChanged =
                    normalizedStatus != null
                    && !normalizedStatus.equalsIgnoreCase(
                            currentSeat.getStatus()
                    );
            //Nếu một ghế cần thay đổi mà đã có lịch sử vé, Service chặn thao tác.
            if ((typeChanged || statusChanged)
                    && seatRepository.hasTicketHistory(
                            seatId)) {

                throw new IllegalArgumentException(
                        "Không thể cập nhật hàng loạt "
                        + "vì ghế ID " + seatId
                        + " đã có lịch sử vé."
                );
            }
        }
        //Repository cập nhật nhiều ghế và trả số dòng bị ảnh hưởng. Service so sánh với số ghế được chọn.
        int updated =
                seatRepository.updateBulk(
                        ids,
                        roomId,
                        normalizedSeatType,
                        normalizedStatus
                );

        if (updated != ids.size()) {
            throw new IllegalArgumentException(
                    "Không thể cập nhật đầy đủ "
                    + "các ghế đã chọn."
            );
        }
    }

    // =====================================================
    // XÓA NHIỀU GHẾ TRONG 1 TRANSACTION
    // =====================================================

    /** Xóa các ghế được chọn nếu đủ điều kiện. */
    public void deleteSeats(
            List<Integer> seatIds,
            int roomId,
            int cinemaId) {

        checkRoom(roomId, cinemaId);

        List<Integer> ids =
                validateSelectedSeats(
                        seatIds,
                        roomId
                );
        //epository chịu trách nhiệm thực hiện việc xóa theo các điều kiện và transaction.
        boolean deleted =
                seatRepository.deleteSeatsAtomic(
                        ids,
                        roomId
                );

        if (!deleted) {
            throw new IllegalArgumentException(
                    "Không thể xóa các ghế đã chọn."
            );
        }

        // Repository da dong bo capacity.
        // Khong can goi syncCapacityFromSeats().
    }

    // =====================================================
    // KIEM TRA DANH SACH GHE
    // =====================================================

    /** Kiểm tra các ghế thuộc phòng đang chọn. */
    private List<Integer> validateSelectedSeats(
            List<Integer> seatIds,
            int roomId) {

        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn ít nhất một ghế."
            );
        }
        //LinkedHashSet giúp loại ID ghế trùng nhau và giữ thứ tự ban đầu.
        Set<Integer> uniqueIds =
                new LinkedHashSet<>(seatIds);
        //Kiểm tra từng ID và bảo đảm ghế thuộc đúng phòng đang quản lý.
        for (Integer seatId : uniqueIds) {

            if (seatId == null || seatId <= 0) {
                throw new IllegalArgumentException(
                        "Ghế được chọn không hợp lệ."
                );
            }

            Seat seat =
                    seatRepository.findByIdAndRoom(
                            seatId,
                            roomId
                    );

            if (seat == null) {
                throw new IllegalArgumentException(
                        "Có ghế không thuộc phòng của bạn."
                );
            }
        }

        return new ArrayList<>(uniqueIds);
    }

    // =====================================================
    // KIỂM TRA PHÒNG THUỘC RẠP
    // =====================================================

    /** Kiểm tra phòng thuộc rạp quản lý. */
    //Manager rạp Ninh Kiều không thể thao tác phòng thuộc Cái Răng nếu findByIdAndCinema() lọc đúng cả hai ID.
    private Room checkRoom(
            int roomId,
            int cinemaId) {

        if (roomId <= 0 || cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Phòng không hợp lệ."
            );
        }

        Room room =
                roomRepository.findByIdAndCinema(
                        roomId,
                        cinemaId
                );

        if (room == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy phòng thuộc rạp của bạn."
            );
        }

        return room;
    }

    // =====================================================
    // VALIDATE THÔNG TIN GHẾ
    // =====================================================

    /** Kiểm tra hàng, số ghế, loại và trạng thái. */
    private void validateSeat(
            int roomId,
            Integer seatId,
            int seatRow,
            int seatNumber,
            String seatType,
            String status) {

        if (seatRow <= 0 || seatRow > 30) {
            throw new IllegalArgumentException(
                    "Hàng ghế phải từ 1 đến 30."
            );
        }

        if (seatNumber <= 0 || seatNumber > 50) {
            throw new IllegalArgumentException(
                    "Số ghế phải từ 1 đến 50."
            );
        }

        normalizeSeatType(seatType);

        if (!"ACTIVE".equals(status)
                && !"INACTIVE".equals(status)) {

            throw new IllegalArgumentException(
                    "Trạng thái ghế không hợp lệ."
            );
        }

        if (seatRepository.existsPosition(
                roomId,
                seatRow,
                seatNumber,
                seatId)) {

            throw new IllegalArgumentException(
                    "Vị trí ghế đã tồn tại trong phòng."
            );
        }
    }

    // =====================================================
    // CHUẨN HÓA LOẠI GHẾ
    // =====================================================

    /** Chuẩn hóa loại ghế. */
    private String normalizeSeatType(String seatType) {

        if (seatType == null
                || seatType.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Loại ghế không được để trống."
            );
        }

        String type =
                seatType.trim().toUpperCase();

        if (!"STANDARD".equals(type)
                && !"VIP".equals(type)
                && !"COUPLE".equals(type)) {

            throw new IllegalArgumentException(
                    "Loại ghế không hợp lệ."
            );
        }

        return type;
    }

    // =====================================================
    // CHUAN HOA LOAI GHE KHONG BAT BUOC
    // =====================================================

    /** Kiểm tra loại ghế khi sửa hàng loạt. */
    private String normalizeOptionalSeatType(
            String seatType) {

        if (seatType == null
                || seatType.trim().isEmpty()) {
            return null;
        }

        return normalizeSeatType(seatType);
    }

    // =====================================================
    // CHUAN HOA TRANG THAI KHONG BAT BUOC
    // =====================================================

    /** Kiểm tra trạng thái khi sửa hàng loạt. */
    private String normalizeOptionalStatus(
            String status) {

        if (status == null
                || status.trim().isEmpty()) {
            return null;
        }

        String normalized =
                status.trim().toUpperCase();

        if (!"ACTIVE".equals(normalized)
                && !"INACTIVE".equals(normalized)) {

            throw new IllegalArgumentException(
                    "Trạng thái ghế không hợp lệ."
            );
        }

        return normalized;
    }
}
