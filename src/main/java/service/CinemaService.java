package service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Cinema;
import repository.CinemaRepository;

/**
 * Provides business logic for cinemas.
 *
 * @author Kiet
 */
public class CinemaService {

    private CinemaRepository cinemaRepository;

    public CinemaService() {
        cinemaRepository = new CinemaRepository();
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
            throw new IllegalArgumentException("Rạp không hợp lệ.");
        }

        return cinemaRepository.countRooms(cinemaId);
    }

    public void updateCinemaContact(int cinemaId,
            String address, String phone) {

        if (cinemaId <= 0) {
            throw new IllegalArgumentException("Rạp không hợp lệ.");
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

        boolean updated = cinemaRepository.updateContact(
                cinemaId, address, phone
        );

        if (!updated) {
            throw new IllegalArgumentException(
                    "Không tìm thấy rạp cần cập nhật."
            );
        }
    }

    /**
     * Gets all branches for Admin management.
     */
    public List<Cinema> getAllCinemas() {
        return cinemaRepository.getAll();
    }

    public String createCinema(Cinema cinema) {

        String error = validateCinema(cinema);
        if (error != null) {
            return error;
        }

        if (cinemaRepository.existsByName(cinema.getCinemaName(), 0)) {
            return "Tên chi nhánh đã tồn tại.";
        }

        if (!cinemaRepository.insert(cinema)) {
            return "Không thể thêm chi nhánh.";
        }

        return null;
    }

    public String updateCinema(Cinema cinema) {

        if (cinema == null || cinema.getCinemaId() <= 0) {
            return "Chi nhánh không hợp lệ.";
        }

        if (cinemaRepository.findById(cinema.getCinemaId()) == null) {
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

        if (!cinemaRepository.update(cinema)) {
            return "Không thể cập nhật chi nhánh.";
        }

        return null;
    }

    public String updateCinemaStatus(int cinemaId, String status) {

        if (cinemaId <= 0) {
            return "Chi nhánh không hợp lệ.";
        }

        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            return "Trạng thái chi nhánh không hợp lệ.";
        }

        if (cinemaRepository.findById(cinemaId) == null) {
            return "Không tìm thấy chi nhánh.";
        }

        if (!cinemaRepository.updateStatus(cinemaId, status)) {
            return "Không thể cập nhật trạng thái chi nhánh.";
        }

        return null;
    }

    public String deleteCinema(int cinemaId) {

        if (cinemaId <= 0) {
            return "Chi nhánh không hợp lệ.";
        }

        if (cinemaRepository.findById(cinemaId) == null) {
            return "Không tìm thấy chi nhánh.";
        }

        try {
            String blockReason =
                    cinemaRepository.getDeleteBlockReason(cinemaId);

            if (blockReason != null) {
                return blockReason;
            }

        } catch (IllegalStateException e) {
            return e.getMessage();
        }

        if (!cinemaRepository.delete(cinemaId)) {
            return "Không thể xóa chi nhánh. Dữ liệu có thể đang được sử dụng.";
        }

        return null;
    }

    private String validateCinema(Cinema cinema) {

        if (cinema == null) {
            return "Thông tin chi nhánh không hợp lệ.";
        }

        String cinemaName = cinema.getCinemaName();
        String address = cinema.getAddress();
        String phone = cinema.getPhone();
        String status = cinema.getStatus();

        if (cinemaName == null || cinemaName.trim().isEmpty()) {
            return "Tên chi nhánh không được để trống.";
        }

        cinemaName = cinemaName.trim();

        if (cinemaName.length() > 100) {
            return "Tên chi nhánh không được vượt quá 100 ký tự.";
        }

        if (address == null || address.trim().isEmpty()) {
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

        if (status == null || status.trim().isEmpty()) {
            status = "ACTIVE";
        }

        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            return "Trạng thái chi nhánh không hợp lệ.";
        }

        cinema.setCinemaName(cinemaName);
        cinema.setAddress(address);
        cinema.setPhone(phone);
        cinema.setStatus(status);

        return null;
    }

}
