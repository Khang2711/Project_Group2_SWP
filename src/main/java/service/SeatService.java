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

    public List<Seat> getSeatsByRoom(
            int roomId,
            int cinemaId) {

        checkRoom(
                roomId,
                cinemaId
        );

        return seatRepository
                .findByRoomId(roomId);
    }

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
    // Không còn bị giới hạn bởi capacity cũ.
    // Sau khi thêm -> capacity tăng theo số ghế.
    // =========================================================
    public void createSeat(
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

        validateSeat(
                roomId,
                null,
                seatRow,
                seatNumber,
                seatType,
                status
        );

        Seat seat =
                new Seat(
                        0,
                        roomId,
                        seatRow,
                        seatNumber,
                        normalizeSeatType(
                                seatType
                        ),
                        status
                );

        if (!seatRepository.insert(seat)) {

            throw new IllegalArgumentException(
                    "Không thể thêm ghế."
            );
        }

        roomRepository
                .syncCapacityFromSeats(
                        roomId
                );
    }

    // =========================================================
    // THÊM MỘT HÀNG GHẾ MỚI
    // =========================================================
    public void addRow(
            int roomId,
            int cinemaId,
            int seatCount,
            String seatType) {

        checkRoom(
                roomId,
                cinemaId
        );

        if (seatCount <= 0) {

            throw new IllegalArgumentException(
                    "Số ghế của hàng phải lớn hơn 0."
            );
        }

        if (seatCount > 50) {

            throw new IllegalArgumentException(
                    "Một hàng tối đa 50 ghế."
            );
        }

        String normalizedType =
                normalizeSeatType(
                        seatType
                );

        int nextRow =
                seatRepository
                        .getMaxRow(roomId)
                + 1;

        if (nextRow > 30) {

            throw new IllegalArgumentException(
                    "Phòng tối đa 30 hàng ghế."
            );
        }

        if (!seatRepository
                .insertRow(
                        roomId,
                        nextRow,
                        seatCount,
                        normalizedType
                )) {

            throw new IllegalArgumentException(
                    "Không thể thêm hàng ghế."
            );
        }

        roomRepository
                .syncCapacityFromSeats(
                        roomId
                );
    }

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
                        normalizeSeatType(
                                seatType
                        ),
                        status
                );

        if (!seatRepository.update(seat)) {

            throw new IllegalArgumentException(
                    "Không thể cập nhật ghế."
            );
        }
    }

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

        List<Integer> ids =
                validateSelectedSeats(
                        seatIds,
                        roomId
                );

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

        int updated =
                seatRepository
                        .updateBulk(
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

    // =========================================================
    // XÓA NHIỀU GHẾ
    //
    // - Chưa từng bán vé -> hard delete.
    // - Có lịch sử Ticket -> không xóa DB,
    //   chỉ chuyển INACTIVE.
    // =========================================================
    public void deleteSeats(
            List<Integer> seatIds,
            int roomId,
            int cinemaId) {

        checkRoom(
                roomId,
                cinemaId
        );

        List<Integer> ids =
                validateSelectedSeats(
                        seatIds,
                        roomId
                );

        int currentSeatCount =
                seatRepository
                        .countSeats(roomId);

        int hardDeleteCount = 0;

        for (Integer seatId : ids) {

            if (!seatRepository
                    .hasTicketHistory(
                            seatId
                    )) {

                hardDeleteCount++;
            }
        }

        if (currentSeatCount
                - hardDeleteCount <= 0) {

            throw new IllegalArgumentException(
                    "Phòng phải còn ít nhất một ghế."
            );
        }

        for (Integer seatId : ids) {

            if (seatRepository
                    .hasTicketHistory(
                            seatId
                    )) {

                seatRepository.updateStatus(
                        seatId,
                        roomId,
                        "INACTIVE"
                );

            } else {

                seatRepository.delete(
                        seatId,
                        roomId
                );
            }
        }

        roomRepository
                .syncCapacityFromSeats(
                        roomId
                );
    }

    private List<Integer>
            validateSelectedSeats(
                    List<Integer> seatIds,
                    int roomId) {

        if (seatIds == null
                || seatIds.isEmpty()) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn ít nhất một ghế."
            );
        }

        Set<Integer> uniqueIds =
                new LinkedHashSet<>(
                        seatIds
                );

        for (Integer seatId : uniqueIds) {

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

        return new ArrayList<>(
                uniqueIds
        );
    }

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

        if (seatRow > 30) {

            throw new IllegalArgumentException(
                    "Hàng ghế tối đa là 30."
            );
        }

        if (seatNumber <= 0) {

            throw new IllegalArgumentException(
                    "Số ghế phải lớn hơn 0."
            );
        }

        if (seatNumber > 50) {

            throw new IllegalArgumentException(
                    "Số ghế trong hàng tối đa là 50."
            );
        }

        normalizeSeatType(
                seatType
        );

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

    private String normalizeSeatType(
            String seatType) {

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

        return type;
    }

    private String normalizeOptionalSeatType(
            String seatType) {

        if (seatType == null
                || seatType.trim().isEmpty()) {

            return null;
        }

        return normalizeSeatType(
                seatType
        );
    }

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