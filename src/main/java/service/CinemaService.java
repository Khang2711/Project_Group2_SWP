package service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Cinema;
import model.Showtime;
import repository.CinemaRepository;
import repository.ShowtimeRepository;

/**
 * Provides business logic for cinemas.
 *
 * @author Kiet
 */
public class CinemaService {

    private final ShowtimeRepository showtimeRepository
        = new ShowtimeRepository();

public Cinema getCinemaByShowtime(Showtime showtime) {
    if (showtime == null) {
        return null;
    }

    int cinemaId = showtimeRepositorys
            .getCinemaIdByShowtimeId(showtime.getShowtimeId());

    return cinemaId > 0
            ? cinemaRepository.findById(cinemaId)
            : null;
}


    private CinemaRepository cinemaRepository;
    private ShowtimeRepository showtimeRepositorys;

    public CinemaService() {
        cinemaRepository = new CinemaRepository();
        showtimeRepositorys = new ShowtimeRepository();
    }

       /**
     * Gets all active cinemas.
     *
     * @return a list of active cinemas
     */
    public List<Cinema> getActiveCinemas() {
        return cinemaRepository.getAllActive();
    }

    /**
     * Gets all active cinemas by cinema ID, in the same order as the list.
     *
     * @return a map of cinema ID and cinema
     */
    public Map<Integer, Cinema> getCinemaById() {
        Map<Integer, Cinema> cinemas = new LinkedHashMap<>();

        for (Cinema cinema : getActiveCinemas()) {
            cinemas.put(cinema.getCinemaId(), cinema);
            
        }

        return cinemas;
    }

    public Cinema getCinema(int cinemaId) {

        if (cinemaId <= 0) {
            throw new IllegalArgumentException("Rạp không hợp lệ.");

        }

        return cinemaRepository.findById(cinemaId);
    }

    public int getRoomCount(int cinemaId) {

        if (cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Rạp không hợp lệ."
            );
        }

        return cinemaRepository.countRooms(cinemaId);
    }

    public void updateCinemaContact(
            int cinemaId,
            String address,
            String phone) {

        if (cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Rạp không hợp lệ."
            );
        }

        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Địa chỉ không được để trống."
            );
        }

        address = address.trim();

        if (address.length() > 255) {
            throw new IllegalArgumentException(
                    "Địa chỉ không được vượt quá 255 ký tự."
            );
        }

        if (phone != null) {
            phone = phone.trim();

            if (phone.isEmpty()) {
                phone = null;
            } else if (phone.length() > 20) {
                throw new IllegalArgumentException(
                        "Số điện thoại không được vượt quá 20 ký tự."
                );
            }
        }

        boolean updated =
                cinemaRepository.updateContact(
                        cinemaId,
                        address,
                        phone
                );

        if (!updated) {
            throw new IllegalArgumentException(
                    "Không tìm thấy rạp cần cập nhật."
            );
        }
    }

    public List<Cinema> getAllCinemas() {
        return cinemaRepository.getAll();
    }

    // =====================================================
    // TẠO RẠP
    // =====================================================

    public String createCinema(Cinema cinema) {

        String error = validateCinema(cinema);

        if (error != null) {
            return error;
        }

        if (cinemaRepository.existsByName(
                cinema.getCinemaName(),
                0)) {

            return "Tên chi nhánh đã tồn tại.";
        }

        if (!cinemaRepository.insert(cinema)) {
            return "Không thể thêm chi nhánh.";
        }

        return null;
    }

    // =====================================================
    // BUG-07 FIX:
    // BẢO VỆ TRẠNG THÁI RẠP KHI SỬA THÔNG TIN
    // =====================================================

    public String updateCinema(Cinema cinema) {

        if (cinema == null
                || cinema.getCinemaId() <= 0) {

            return "Chi nhánh không hợp lệ.";
        }

        Cinema currentCinema =
                cinemaRepository.findById(
                        cinema.getCinemaId()
                );

        if (currentCinema == null) {
            return "Không tìm thấy chi nhánh.";
        }

        String error = validateCinema(cinema);

        if (error != null) {
            return error;
        }

        if (cinemaRepository.existsByName(
                cinema.getCinemaName(),
                cinema.getCinemaId())) {

            return "Tên chi nhánh đã tồn tại.";
        }

        // BUG-07 FIX:
        // Không cho updateCinema() bỏ qua kiểm tra
        // bằng cách thay trạng thái ngay khi sửa rạp.
        if ("ACTIVE".equalsIgnoreCase(
                currentCinema.getStatus())
                && "INACTIVE".equalsIgnoreCase(
                        cinema.getStatus())) {

            String blockReason =
                    getDeactivationBlockReason(
                            cinema.getCinemaId()
                    );

            if (blockReason != null) {
                return blockReason;
            }
        }

        if (!cinemaRepository.update(cinema)) {
            return "Không thể cập nhật chi nhánh.";
        }

        return null;
    }

    // =====================================================
    // BUG-07 FIX:
    // CHẶN VÔ HIỆU HÓA RẠP CÒN LỊCH HOẶC VÉ
    // =====================================================

    public String updateCinemaStatus(
            int cinemaId,
            String status) {

        if (cinemaId <= 0) {
            return "Chi nhánh không hợp lệ.";
        }

        if (!"ACTIVE".equals(status)
                && !"INACTIVE".equals(status)) {

            return "Trạng thái chi nhánh không hợp lệ.";
        }

        Cinema currentCinema =
                cinemaRepository.findById(cinemaId);

        if (currentCinema == null) {
            return "Không tìm thấy chi nhánh.";
        }

        if ("ACTIVE".equalsIgnoreCase(
                currentCinema.getStatus())
                && "INACTIVE".equals(status)) {

            String blockReason =
                    getDeactivationBlockReason(cinemaId);

            if (blockReason != null) {
                return blockReason;
            }
        }

        if (!cinemaRepository.updateStatus(
                cinemaId,
                status)) {

            return "Không thể cập nhật trạng thái chi nhánh.";
        }

        return null;
    }

    // =====================================================
    // BUG-07 FIX:
    // KIỂM TRA TRƯỚC KHI NGỪNG HOẠT ĐỘNG RẠP
    //
    // Chặn nếu còn:
    // 1. Suất chiếu ACTIVE chưa kết thúc.
    // 2. Vé thuộc suất chiếu chưa kết thúc,
    //    kể cả suất đã bị chuyển INACTIVE.
    //
    // Không tự động hủy vé hoặc hoàn tiền.
    // =====================================================

    private String getDeactivationBlockReason(
            int cinemaId) {

        List<Showtime> showtimes =
                showtimeRepository.getByCinemaId(
                        cinemaId
                );

        LocalDateTime now =
                LocalDateTime.now();

        for (Showtime showtime : showtimes) {

            if (showtime.getEndTime() == null) {
                return "Không thể ngừng hoạt động rạp "
                        + "vì có suất chiếu thiếu "
                        + "thời gian kết thúc.";
            }

            LocalDateTime endTime =
                    showtime.getEndTime()
                            .toLocalDateTime();

            // Suất đã kết thúc không ngăn rạp
            // ngừng hoạt động theo chính sách này.
            if (!endTime.isAfter(now)) {
                continue;
            }

            if ("ACTIVE".equalsIgnoreCase(
                    showtime.getStatus())) {

                return "Không thể ngừng hoạt động rạp "
                        + "vì vẫn còn suất chiếu "
                        + "đang hoạt động hoặc "
                        + "chưa kết thúc.";
            }

            // Kiểm tra vé ngay cả khi showtime
            // đã chuyển INACTIVE.
            if (showtimeRepository.hasTickets(
                    showtime.getShowtimeId())) {

                return "Không thể ngừng hoạt động rạp "
                        + "vì vẫn còn vé thuộc "
                        + "suất chiếu chưa kết thúc. "
                        + "Vui lòng xử lý vé trước.";
            }
        }

        return null;
    }

    // =====================================================
    // XÓA RẠP
    // GIỮ RÀNG BUỘC DỮ LIỆU CỦA PROJECT
    // =====================================================

    public String deleteCinema(int cinemaId) {

        if (cinemaId <= 0) {
            return "Chi nhánh không hợp lệ.";
        }

        if (cinemaRepository.findById(cinemaId) == null) {
            return "Không tìm thấy chi nhánh.";
        }

        try {

            String blockReason =
                    cinemaRepository.getDeleteBlockReason(
                            cinemaId
                    );

            if (blockReason != null) {
                return blockReason;
            }

        } catch (IllegalStateException e) {
            return e.getMessage();
        }

        if (!cinemaRepository.delete(cinemaId)) {
            return "Không thể xóa chi nhánh. "
                    + "Dữ liệu có thể đang được sử dụng.";
        }

        return null;
    }

    // =====================================================
    // VALIDATE THÔNG TIN RẠP
    // =====================================================

    private String validateCinema(Cinema cinema) {

        if (cinema == null) {
            return "Thông tin chi nhánh không hợp lệ.";
        }

        String cinemaName = cinema.getCinemaName();
        String address = cinema.getAddress();
        String phone = cinema.getPhone();
        String status = cinema.getStatus();

        if (cinemaName == null
                || cinemaName.trim().isEmpty()) {

            return "Tên chi nhánh không được để trống.";
        }

        cinemaName = cinemaName.trim();

        if (cinemaName.length() > 100) {
            return "Tên chi nhánh không được vượt quá 100 ký tự.";
        }

        if (address == null
                || address.trim().isEmpty()) {

            return "Địa chỉ không được để trống.";
        }

        address = address.trim();

        if (address.length() > 255) {
            return "Địa chỉ không được vượt quá 255 ký tự.";
        }

        if (phone != null) {

            phone = phone.trim();

            if (phone.isEmpty()) {
                phone = null;
            } else if (phone.length() > 20) {
                return "Số điện thoại không được vượt quá 20 ký tự.";
            }
        }

        if (status == null
                || status.trim().isEmpty()) {

            status = "ACTIVE";
        }

        if (!"ACTIVE".equals(status)
                && !"INACTIVE".equals(status)) {

            return "Trạng thái chi nhánh không hợp lệ.";
        }

        cinema.setCinemaName(cinemaName);
        cinema.setAddress(address);
        cinema.setPhone(phone);
        cinema.setStatus(status);

        return null;
    }
}
