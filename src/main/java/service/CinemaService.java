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
}
