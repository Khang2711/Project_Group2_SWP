package service;

import java.util.List;
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

    public List<Seat> getSeatsByRoom(int roomId, int cinemaId) {

        checkRoom(roomId, cinemaId);

        return seatRepository.findByRoomId(roomId);
    }

    public Seat getSeat(int seatId, int roomId, int cinemaId) {

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

    public void createSeat(int roomId, int cinemaId,
            int seatRow, int seatNumber,
            String seatType, String status) {

        Room room = checkRoom(roomId, cinemaId);

        validateSeat(
                roomId,
                null,
                seatRow,
                seatNumber,
                seatType,
                status
        );

        int currentSeats = seatRepository.countSeats(roomId);

        if (currentSeats >= room.getCapacity()) {
            throw new IllegalArgumentException(
                    "Số ghế đã đạt sức chứa tối đa của phòng."
            );
        }

        Seat seat = new Seat(
                0,
                roomId,
                seatRow,
                seatNumber,
                seatType.trim(),
                status
        );

        if (!seatRepository.insert(seat)) {
            throw new IllegalArgumentException(
                    "Không thể thêm ghế."
            );
        }
    }

    public void updateSeat(int seatId,
            int roomId, int cinemaId,
            int seatRow, int seatNumber,
            String seatType, String status) {

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

        boolean positionChanged =
                currentSeat.getSeatRow() != seatRow
                || currentSeat.getSeatNumber() != seatNumber;

        if (positionChanged
                && seatRepository.hasTicketHistory(seatId)) {

            throw new IllegalArgumentException(
                    "Không thể đổi vị trí ghế "
                    + "đã có lịch sử vé."
            );
        }

        Seat seat = new Seat(
                seatId,
                roomId,
                seatRow,
                seatNumber,
                seatType.trim(),
                status
        );

        if (!seatRepository.update(seat)) {
            throw new IllegalArgumentException(
                    "Không thể cập nhật ghế."
            );
        }
    }

    private Room checkRoom(int roomId, int cinemaId) {

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

    private void validateSeat(int roomId,
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

        seatType = seatType.trim();

        if (!"STANDARD".equals(seatType)
                && !"VIP".equals(seatType)
                && !"COUPLE".equals(seatType)) {

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
}