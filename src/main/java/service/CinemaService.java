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
}