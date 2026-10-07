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
import java.sql.Date;
import model.Genre;
import repository.GenreRepository;

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
    private GenreRepository genreRepository;

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
     * Builds the movie list page data: for each show date, the movies that have
     * showtimes on that date and their showtimes. Each filter is optional (null
     * or empty means no filter). When no date or cinema filter is applied,
     * active movies without any upcoming showtime are added in a last group
     * with an empty showtime list.
     *
     * @param keyword the movie title keyword, empty string for all movies
     * @param date the show date filter, null for all dates
     * @param cinemaId the cinema filter, null for all cinemas
     * @return movies and showtimes grouped by date (dd/MM/yyyy) in
     * chronological order
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
     * @return a map of movie ID and its lowest price; movies without pricing
     * are absent
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

    public List<Movie> getMoviesByCinema(int cinemaId) {

        if (cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Rạp không hợp lệ."
            );
        }

        return movieRepository.getByCinemaId(cinemaId);
    }

    public List<Genre> getActiveGenres() {
        return genreRepository.getAllActive();
    }

    public List<Integer> getGenreIds(int movieId) {
        return movieRepository.getGenreIds(movieId);
    }

    public Movie getMovieForManager(
            int movieId,
            int cinemaId) {

        if (!movieRepository.belongsToCinema(
                movieId,
                cinemaId)) {

            return null;
        }

        return movieRepository.getByIdAnyStatus(movieId);
    }

    public void createMovie(int cinemaId,
            String title,
            String description,
            String durationText,
            String ageRating,
            String releaseDateText,
            String endDateText,
            String posterUrl,
            String trailerUrl,
            String status,
            String[] genreValues) {

        Movie movie = buildMovie(
                0,
                title,
                description,
                durationText,
                ageRating,
                releaseDateText,
                endDateText,
                posterUrl,
                trailerUrl,
                status
        );

        List<Integer> genreIds
                = parseGenreIds(genreValues);

        if (genreIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Phim phải có ít nhất một thể loại."
            );
        }

        if (!movieRepository.insertForCinema(
                movie,
                genreIds,
                cinemaId)) {

            throw new IllegalArgumentException(
                    "Không thể thêm phim."
            );
        }
    }

    public void updateMovie(int movieId,
            int cinemaId,
            String title,
            String description,
            String durationText,
            String ageRating,
            String releaseDateText,
            String endDateText,
            String posterUrl,
            String trailerUrl,
            String status,
            String[] genreValues) {

        if (!movieRepository.belongsToCinema(
                movieId,
                cinemaId)) {

            throw new IllegalArgumentException(
                    "Bạn không có quyền sửa phim này."
            );
        }

        Movie movie = buildMovie(
                movieId,
                title,
                description,
                durationText,
                ageRating,
                releaseDateText,
                endDateText,
                posterUrl,
                trailerUrl,
                status
        );

        List<Integer> genreIds
                = parseGenreIds(genreValues);

        if (genreIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Phim phải có ít nhất một thể loại."
            );
        }

        if (!movieRepository.updateForCinema(
                movie,
                genreIds,
                cinemaId)) {

            throw new IllegalArgumentException(
                    "Không thể cập nhật phim."
            );
        }
    }

    private Movie buildMovie(int movieId,
            String title,
            String description,
            String durationText,
            String ageRating,
            String releaseDateText,
            String endDateText,
            String posterUrl,
            String trailerUrl,
            String status) {

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Tên phim không được để trống."
            );
        }

        if (title.trim().length() > 255) {
            throw new IllegalArgumentException(
                    "Tên phim không được vượt quá 255 ký tự."
            );
        }

        int duration;

        try {
            duration = Integer.parseInt(durationText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Thời lượng phim không hợp lệ."
            );
        }

        if (duration <= 0) {
            throw new IllegalArgumentException(
                    "Thời lượng phim phải lớn hơn 0."
            );
        }

        Date releaseDate = parseSqlDate(
                releaseDateText,
                "Ngày phát hành không hợp lệ."
        );

        Date endDate = parseSqlDate(
                endDateText,
                "Ngày kết thúc không hợp lệ."
        );

        if (releaseDate != null
                && endDate != null
                && endDate.before(releaseDate)) {

            throw new IllegalArgumentException(
                    "Ngày kết thúc không được trước ngày phát hành."
            );
        }

        if (!"ACTIVE".equals(status)
                && !"INACTIVE".equals(status)) {

            throw new IllegalArgumentException(
                    "Trạng thái phim không hợp lệ."
            );
        }

        Movie movie = new Movie();

        movie.setMovieId(movieId);
        movie.setTitle(title.trim());
        movie.setDescription(emptyToNull(description));
        movie.setDurationMinutes(duration);
        movie.setAgeRating(emptyToNull(ageRating));
        movie.setReleaseDate(releaseDate);
        movie.setEndDate(endDate);
        movie.setPosterUrl(emptyToNull(posterUrl));
        movie.setTrailerUrl(emptyToNull(trailerUrl));
        movie.setStatus(status);

        return movie;
    }

    private Date parseSqlDate(
            String value,
            String errorMessage) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Date.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private String emptyToNull(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private List<Integer> parseGenreIds(
            String[] genreValues) {

        List<Integer> genreIds = new ArrayList<>();

        if (genreValues == null) {
            return genreIds;
        }

        for (String value : genreValues) {

            try {
                genreIds.add(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Thể loại không hợp lệ."
                );
            }
        }

        return genreIds;
    }
}
