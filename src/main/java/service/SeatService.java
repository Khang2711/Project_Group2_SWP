
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

    public SeatService() {
        seatRepository = new SeatRepository();
        roomRepository = new RoomRepository();
    }

    // =====================================================
    // LAY DANH SACH GHE THEO PHONG
    // =====================================================

    public List<Seat> getSeatsByRoom(
            int roomId,
            int cinemaId) {

        checkRoom(roomId, cinemaId);

        return seatRepository.findByRoomId(roomId);
    }

    // =====================================================
    // NHOM GHE THEO HANG
    // =====================================================

    public Map<Integer, List<Seat>> getSeatRowsByRoom(
            int roomId,
            int cinemaId) {

        List<Seat> seats =
                getSeatsByRoom(roomId, cinemaId);

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
    // LAY THONG TIN MOT GHE
    // =====================================================

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
    // THEM MOT GHE
    // =====================================================

    public void createSeat(
            int roomId,
            int cinemaId,
            int seatRow,
            int seatNumber,
            String seatType,
            String status) {

        checkRoom(roomId, cinemaId);

        validateSeat(
                roomId,
                null,
                seatRow,
                seatNumber,
                seatType,
                status
        );

        Seat seat = new Seat(
                0,
                roomId,
                seatRow,
                seatNumber,
                normalizeSeatType(seatType),
                status
        );

        if (!seatRepository.insert(seat)) {
            throw new IllegalArgumentException(
                    "Không thể thêm ghế."
            );
        }

        roomRepository.syncCapacityFromSeats(roomId);
    }

    // =====================================================
    // BUG-16 FIX
    // THEM NGUYEN MOT HANG GHE MOI
    //
    // Vi du:
    // Dang co hang A, B, C
    // -> Tao hang D voi 8 ghe.
    // =====================================================

    public void addRow(
            int roomId,
            int cinemaId,
            int seatCount,
            String seatType) {

        checkRoom(roomId, cinemaId);

        if (seatCount <= 0 || seatCount > 50) {
            throw new IllegalArgumentException(
                    "Số ghế phải từ 1 đến 50."
            );
        }

        String normalizedType =
                normalizeSeatType(seatType);

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
    // CHUC NANG MOI
    // THEM NHIEU GHE VAO HANG DA CO
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

    public void addSeatsToExistingRow(
            int roomId,
            int cinemaId,
            int seatRow,
            int seatCount,
            String seatType) {

        checkRoom(roomId, cinemaId);

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

        // Phai la hang da ton tai.
        boolean rowExists = false;

        List<Seat> seats =
                seatRepository.findByRoomId(roomId);

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
    // BUG-04 FIX
    // CAP NHAT MOT GHE
    //
    // Khong cho thay doi vi tri, loai ghe
    // hoac trang thai khi ghe da co ticket.
    // =====================================================

    public void updateSeat(
            int seatId,
            int roomId,
            int cinemaId,
            int seatRow,
            int seatNumber,
            String seatType,
            String status) {

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

        boolean typeChanged =
                !normalizedSeatType.equalsIgnoreCase(
                        currentSeat.getSeatType()
                );

        boolean statusChanged =
                !status.equalsIgnoreCase(
                        currentSeat.getStatus()
                );

        if ((positionChanged
                || typeChanged
                || statusChanged)
                && seatRepository.hasTicketHistory(seatId)) {

            throw new IllegalArgumentException(
                    "Không thể thay đổi vị trí, loại ghế "
                    + "hoặc trạng thái của ghế đã có vé."
            );
        }

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
    // BUG-04 FIX
    // CAP NHAT NHIEU GHE
    // =====================================================

    public void bulkUpdateSeats(
            List<Integer> seatIds,
            int roomId,
            int cinemaId,
            String seatType,
            String status) {

        checkRoom(roomId, cinemaId);

        List<Integer> ids =
                validateSelectedSeats(
                        seatIds,
                        roomId
                );

        String normalizedSeatType =
                normalizeOptionalSeatType(seatType);

        String normalizedStatus =
                normalizeOptionalStatus(status);

        if (normalizedSeatType == null
                && normalizedStatus == null) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn thông tin cần cập nhật."
            );
        }

        for (Integer seatId : ids) {

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
    // BUG-17 FIX
    // XOA NHIEU GHE TRONG MOT TRANSACTION
    // =====================================================

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

    private List<Integer> validateSelectedSeats(
            List<Integer> seatIds,
            int roomId) {

        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn ít nhất một ghế."
            );
        }

        Set<Integer> uniqueIds =
                new LinkedHashSet<>(seatIds);

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
    // KIEM TRA PHONG THUOC RAP
    // =====================================================

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
    // VALIDATE THONG TIN GHE
    // =====================================================

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
    // CHUAN HOA LOAI GHE
    // =====================================================

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
