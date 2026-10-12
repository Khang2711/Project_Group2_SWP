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

        seatRepository =
                new SeatRepository();

        roomRepository =
                new RoomRepository();
    }

    // =========================================================
    // DANH SÁCH GHẾ
    // =========================================================
    public List<Seat> getSeatsByRoom(
            int roomId,
            int cinemaId) {

        checkRoom(
                roomId,
                cinemaId
        );

        return seatRepository
                .findByRoomId(
                        roomId
                );
    }

    // =========================================================
    // DANH SÁCH GHẾ NHÓM THEO HÀNG
    //
    // row 1 -> List Seat
    // row 2 -> List Seat
    // ...
    // =========================================================
    public Map<Integer, List<Seat>>
            getSeatRowsByRoom(
                    int roomId,
                    int cinemaId) {

        List<Seat> seats =
                getSeatsByRoom(
                        roomId,
                        cinemaId
                );

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

    // =========================================================
    // LẤY 1 GHẾ
    // =========================================================
    public Seat getSeat(
            int seatId,
            int roomId,
            int cinemaId) {

        checkRoom(
                roomId,
                cinemaId
        );

        if (seatId <= 0) {

            throw new IllegalArgumentException(
                    "Ghế không hợp lệ."
            );
        }

        return seatRepository
                .findByIdAndRoom(
                        seatId,
                        roomId
                );
    }

    // =========================================================
    // THÊM 1 GHẾ
    // Giữ lại để tương thích code cũ.
    // =========================================================
    public void createSeat(
            int roomId,
            int cinemaId,
            int seatRow,
            int seatNumber,
            String seatType,
            String status) {

        Room room =
                checkRoom(
                        roomId,
                        cinemaId
                );

        validateSeat(
                roomId,
                null,
                seatRow,
                seatNumber,
                seatType,
                status
        );

        int currentSeats =
                seatRepository
                        .countSeats(
                                roomId
                        );

        if (currentSeats
                >= room.getCapacity()) {

            throw new IllegalArgumentException(
                    "Số ghế đã đạt sức chứa tối đa của phòng."
            );
        }

        Seat seat =
                new Seat(
                        0,
                        roomId,
                        seatRow,
                        seatNumber,
                        seatType.trim(),
                        status
                );

        if (!seatRepository
                .insert(seat)) {

            throw new IllegalArgumentException(
                    "Không thể thêm ghế."
            );
        }
    }

    // =========================================================
    // UPDATE 1 GHẾ
    // =========================================================
    public void updateSeat(
            int seatId,
            int roomId,
            int cinemaId,
            int seatRow,
            int seatNumber,
            String seatType,
            String status) {

        checkRoom(
                roomId,
                cinemaId
        );

        Seat currentSeat =
                seatRepository
                        .findByIdAndRoom(
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

        boolean positionChanged =
                currentSeat.getSeatRow()
                != seatRow
                || currentSeat.getSeatNumber()
                != seatNumber;

        // Ghế đã có ticket history
        // thì không cho đổi vị trí.
        if (positionChanged
                && seatRepository
                        .hasTicketHistory(
                                seatId
                        )) {

            throw new IllegalArgumentException(
                    "Không thể đổi vị trí ghế "
                    + "đã có lịch sử vé."
            );
        }

        Seat seat =
                new Seat(
                        seatId,
                        roomId,
                        seatRow,
                        seatNumber,
                        seatType.trim(),
                        status
                );

        if (!seatRepository
                .update(seat)) {

            throw new IllegalArgumentException(
                    "Không thể cập nhật ghế."
            );
        }
    }

    // =========================================================
    // UPDATE NHIỀU GHẾ
    //
    // Cho phép:
    // - đổi loại ghế
    // - đổi ACTIVE / INACTIVE
    //
    // Không đổi row/number hàng loạt vì
    // sẽ gây trùng vị trí.
    // =========================================================
    public void bulkUpdateSeats(
            List<Integer> seatIds,
            int roomId,
            int cinemaId,
            String seatType,
            String status) {

        checkRoom(
                roomId,
                cinemaId
        );

        if (seatIds == null
                || seatIds.isEmpty()) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn ít nhất một ghế."
            );
        }

        String normalizedSeatType =
                normalizeOptionalSeatType(
                        seatType
                );

        String normalizedStatus =
                normalizeOptionalStatus(
                        status
                );

        if (normalizedSeatType == null
                && normalizedStatus == null) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn thông tin cần cập nhật."
            );
        }

        // Loại bỏ ID trùng.
        Set<Integer> uniqueIds =
                new LinkedHashSet<>(
                        seatIds
                );

        // Kiểm tra tất cả ghế đều thuộc phòng.
        for (Integer seatId
                : uniqueIds) {

            if (seatId == null
                    || seatId <= 0) {

                throw new IllegalArgumentException(
                        "Ghế được chọn không hợp lệ."
                );
            }

            Seat seat =
                    seatRepository
                            .findByIdAndRoom(
                                    seatId,
                                    roomId
                            );

            if (seat == null) {

                throw new IllegalArgumentException(
                        "Có ghế không thuộc phòng của bạn."
                );
            }
        }

        List<Integer> ids =
                new ArrayList<>(
                        uniqueIds
                );

        int updated =
                seatRepository
                        .updateBulk(
                                ids,
                                roomId,
                                normalizedSeatType,
                                normalizedStatus
                        );

        if (updated
                != ids.size()) {

            throw new IllegalArgumentException(
                    "Không thể cập nhật đầy đủ "
                    + "các ghế đã chọn."
            );
        }
    }

    // =========================================================
    // CHECK ROOM
    // =========================================================
    private Room checkRoom(
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

        return room;
    }

    // =========================================================
    // VALIDATE 1 GHẾ
    // =========================================================
    private void validateSeat(
            int roomId,
            Integer seatId,
            int seatRow,
            int seatNumber,
            String seatType,
            String status) {

        if (seatRow <= 0) {

            throw new IllegalArgumentException(
                    "Hàng ghế phải lớn hơn 0."
            );
        }

        if (seatNumber <= 0) {

            throw new IllegalArgumentException(
                    "Số ghế phải lớn hơn 0."
            );
        }

        if (seatType == null
                || seatType.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Loại ghế không được để trống."
            );
        }

        String type =
                seatType.trim()
                        .toUpperCase();

        if (!"STANDARD".equals(type)
                && !"VIP".equals(type)
                && !"COUPLE".equals(type)) {

            throw new IllegalArgumentException(
                    "Loại ghế không hợp lệ."
            );
        }

        if (!"ACTIVE".equals(status)
                && !"INACTIVE".equals(status)) {

            throw new IllegalArgumentException(
                    "Trạng thái ghế không hợp lệ."
            );
        }

        if (seatRepository
                .existsPosition(
                        roomId,
                        seatRow,
                        seatNumber,
                        seatId
                )) {

            throw new IllegalArgumentException(
                    "Vị trí ghế đã tồn tại trong phòng."
            );
        }
    }

    // =========================================================
    // TYPE CHO BULK UPDATE
    // Blank = giữ nguyên
    // =========================================================
    private String normalizeOptionalSeatType(
            String seatType) {

        if (seatType == null
                || seatType.trim().isEmpty()) {

            return null;
        }

        String type =
                seatType.trim()
                        .toUpperCase();

        if (!"STANDARD".equals(type)
                && !"VIP".equals(type)
                && !"COUPLE".equals(type)) {

            throw new IllegalArgumentException(
                    "Loại ghế không hợp lệ."
            );
        }

        return type;
    }

    // =========================================================
    // STATUS CHO BULK UPDATE
    // Blank = giữ nguyên
    // =========================================================
    private String normalizeOptionalStatus(
            String status) {

        if (status == null
                || status.trim().isEmpty()) {

            return null;
        }

        String normalized =
                status.trim()
                        .toUpperCase();

        if (!"ACTIVE".equals(normalized)
                && !"INACTIVE".equals(normalized)) {

            throw new IllegalArgumentException(
                    "Trạng thái ghế không hợp lệ."
            );
        }

        return normalized;
    }
}