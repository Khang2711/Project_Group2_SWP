package service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Cinema;
import model.Movie;
import model.Room;
import model.Showtime;
import repository.CinemaRepository;
import repository.MovieRepository;
import repository.PricingRepository;
import repository.RoomRepository;
import repository.ShowtimeRepository;

/**
 * Provides business logic for movies.
 *
 * @author Kiet
 */
public class MovieService {

    /**
     * Heading for movies that have no upcoming showtime yet.
     */
    public static String NO_SCHEDULE_GROUP = "Sắp chiếu · Chưa mở lịch";

    /**
     * Display format of a show date: day/month/year.
     */
    private static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private MovieRepository movieRepository;
    private ShowtimeRepository showtimeRepository;
    private RoomRepository roomRepository;
    private CinemaRepository cinemaRepository;
    private PricingRepository pricingRepository;

    public MovieService() {
        movieRepository = new MovieRepository();
        showtimeRepository = new ShowtimeRepository();
        roomRepository = new RoomRepository();
        cinemaRepository = new CinemaRepository();
        pricingRepository = new PricingRepository();
    }

    /**
     * Gets all active movies.
     *
     * @return a list of active movies
     */
    public List<Movie> getAllMovies() {
        return movieRepository.getAll();
    }

    /**
     * Gets a movie by its ID.
     *
     * @param id the movie ID
     * @return the movie if found, otherwise null
     */
    public Movie getMovieById(int id) {
        return movieRepository.getById(id);
    }

    /**
     * Gets all active rooms by room ID.
     *
     * @return a map of room ID and room
     */
    public Map<Integer, Room> getRoomById() {
        Map<Integer, Room> rooms = new HashMap<>();
        for (Room room : roomRepository.getAllActive()) {
            rooms.put(room.getRoomId(), room);
        }
        return rooms;
    }

    /**
     * Builds the movie list page data: for each show date, the movies that
     * have showtimes on that date and their showtimes. Each filter is optional
     * (null or empty means no filter). When no date or cinema filter is
     * applied, active movies without any upcoming showtime are added in a last
     * group with an empty showtime list.
     *
     * @param keyword the movie title keyword, empty string for all movies
     * @param date the show date filter, null for all dates
     * @param cinemaId the cinema filter, null for all cinemas
     * @return movies and showtimes grouped by date (dd/MM/yyyy) in chronological order
     */
    public Map<String, Map<Movie, List<Showtime>>> getSchedulesByDate(String keyword, LocalDate date, Integer cinemaId) {

        // Active movies whose title matches the keyword.
        Map<Integer, Movie> movies = new LinkedHashMap<>();
        for (Movie movie : movieRepository.search(keyword)) {
            movies.put(movie.getMovieId(), movie);
        }

        Map<Integer, Room> rooms = getRoomById();

        Set<Integer> activeCinemaIds = new HashSet<>();
        for (Cinema cinema : cinemaRepository.getAllActive()) {
            activeCinemaIds.add(cinema.getCinemaId());
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());

        // date -> (movie -> showtimes). The repository returns showtimes ordered by start time.
        Map<String, Map<Movie, List<Showtime>>> result = new LinkedHashMap<>();
        Set<Integer> scheduledMovieIds = new HashSet<>();

        for (Showtime showtime : showtimeRepository.getAll()) {
            Movie movie = movies.get(showtime.getMovieId());
            Room room = rooms.get(showtime.getRoomId());

            // Skip showtimes that already started.
            if (showtime.getStartTime().before(now)) {
                continue;
            }

            // Skip other movies, inactive rooms and rooms of inactive cinemas.
            if (movie == null || room == null || !activeCinemaIds.contains(room.getCinemaId())) {
                continue;
            }

            // Apply the cinema filter.
            if (cinemaId != null && room.getCinemaId() != cinemaId) {
                continue;
            }

            // Apply the date filter.
            LocalDate showDate = showtime.getStartTime().toLocalDateTime().toLocalDate();
            if (date != null && !showDate.equals(date)) {
                continue;
            }

            String dateKey = showDate.format(DATE_FORMAT);
            Map<Movie, List<Showtime>> dayMovies = result.get(dateKey);
            if (dayMovies == null) {
                dayMovies = new LinkedHashMap<>();
                result.put(dateKey, dayMovies);
            }

            List<Showtime> showtimes = dayMovies.get(movie);
            if (showtimes == null) {
                showtimes = new ArrayList<>();
                dayMovies.put(movie, showtimes);
            }
            showtimes.add(showtime);
            scheduledMovieIds.add(movie.getMovieId());
        }

        if (date == null && cinemaId == null) {
            Map<Movie, List<Showtime>> unscheduled = new LinkedHashMap<>();
            for (Movie movie : movies.values()) {
                if (!scheduledMovieIds.contains(movie.getMovieId())) {
                    unscheduled.put(movie, new ArrayList<>());
                }
            }
            if (!unscheduled.isEmpty()) {
                result.put(NO_SCHEDULE_GROUP, unscheduled);
            }
        }
        return result;
    }

    /**
     * Gets the lowest current ticket price of each movie, taken from the
     * cinemas that show it.
     *
     * @param scheduleByDate the result of getSchedulesByDate
     * @param rooms the active rooms by room ID
     * @return a map of movie ID and its lowest price; movies without pricing are absent
     */
    public Map<Integer, BigDecimal> getMinPriceByMovie(Map<String, Map<Movie, List<Showtime>>> scheduleByDate, Map<Integer, Room> rooms) {

        Map<Integer, BigDecimal> priceByCinema = pricingRepository.getMinPriceByCinema();
        Map<Integer, BigDecimal> priceByMovie = new HashMap<>();

        for (Map<Movie, List<Showtime>> dayMovies : scheduleByDate.values()) {
            for (Map.Entry<Movie, List<Showtime>> entry : dayMovies.entrySet()) {
                int movieId = entry.getKey().getMovieId();

                for (Showtime showtime : entry.getValue()) {
                    Room room = rooms.get(showtime.getRoomId());
                    if (room == null) {
                        continue;
                    }

                    BigDecimal price = priceByCinema.get(room.getCinemaId());
                    BigDecimal current = priceByMovie.get(movieId);
                    if (price != null && (current == null || price.compareTo(current) < 0)) {
                        priceByMovie.put(movieId, price);
                    }
                }
            }
        }
        return priceByMovie;
    }
}