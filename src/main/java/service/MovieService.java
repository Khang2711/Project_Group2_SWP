
package service;

import java.sql.Timestamp;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import model.Cinema;
import model.Movie;
import model.Room;
import model.Showtime;
import model.Genre;
import repository.CinemaRepository;
import repository.MovieRepository;
import repository.RoomRepository;
import repository.ShowtimeRepository;
import repository.GenreRepository;

/**
 * Provides business logic for movies.
 *
 * @author Kiet
 */
public class MovieService {

    public static String NO_SCHEDULE_GROUP =
            "Sắp chiếu · Chưa mở lịch";

    private static DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private MovieRepository movieRepository;
    private ShowtimeRepository showtimeRepository;
    private RoomRepository roomRepository;
    private CinemaRepository cinemaRepository;
    private GenreRepository genreRepository;

    public MovieService() {
        movieRepository = new MovieRepository();
        showtimeRepository = new ShowtimeRepository();
        roomRepository = new RoomRepository();
        cinemaRepository = new CinemaRepository();
        genreRepository = new GenreRepository();
    }

    public Movie getMovieById(int id) {
        return movieRepository.getById(id);
    }

    public Map<Integer, Room> getRoomById() {
        Map<Integer, Room> rooms = new HashMap<>();

        for (Room room : roomRepository.getAllActive()) {
            rooms.put(room.getRoomId(), room);
        }

        return rooms;
    }

    public Map<String, Map<Movie, List<Showtime>>> getSchedulesByDate(
            String keyword,
            LocalDate date,
            Integer cinemaId) {

        Map<Integer, Movie> movies = new LinkedHashMap<>();

        for (Movie movie : movieRepository.search(keyword)) {
            movies.put(movie.getMovieId(), movie);
        }

        Map<Integer, Room> rooms = getRoomById();

        Set<Integer> activeCinemaIds = new HashSet<>();

        for (Cinema cinema : cinemaRepository.getAllActive()) {
            activeCinemaIds.add(cinema.getCinemaId());
        }

        Timestamp now =
                new Timestamp(System.currentTimeMillis());

        Map<String, Map<Movie, List<Showtime>>> result =
                new LinkedHashMap<>();

        Set<Integer> scheduledMovieIds = new HashSet<>();

        for (Showtime showtime : showtimeRepository.getAll()) {

            Movie movie = movies.get(showtime.getMovieId());
            Room room = rooms.get(showtime.getRoomId());

            if (showtime.getStartTime().before(now)) {
                continue;
            }

            if (movie == null
                    || room == null
                    || !activeCinemaIds.contains(
                            room.getCinemaId())) {
                continue;
            }

            if (cinemaId != null
                    && room.getCinemaId() != cinemaId) {
                continue;
            }

            LocalDate showDate =
                    showtime.getStartTime()
                            .toLocalDateTime()
                            .toLocalDate();

            if (date != null && !showDate.equals(date)) {
                continue;
            }

            String dateKey = showDate.format(DATE_FORMAT);

            Map<Movie, List<Showtime>> dayMovies =
                    result.get(dateKey);

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

            Map<Movie, List<Showtime>> unscheduled =
                    new LinkedHashMap<>();

            for (Movie movie : movies.values()) {
                if (!scheduledMovieIds.contains(
                        movie.getMovieId())) {

                    unscheduled.put(
                            movie,
                            new ArrayList<>()
                    );
                }
            }

            if (!unscheduled.isEmpty()) {
                result.put(
                        NO_SCHEDULE_GROUP,
                        unscheduled
                );
            }
        }

        return result;
    }

    public List<Movie> getNowShowingMovies() {
        return movieRepository.getNowShowing();
    }

    public List<Movie> getComingSoonMovies() {
        return movieRepository.getComingSoon();
    }

    public List<Movie> getMoviesForList(
            String type,
            String keyword) {

        if (keyword == null) {
            keyword = "";
        }

        keyword = keyword.trim();

        if ("coming".equals(type)) {
            return movieRepository.searchComingSoon(keyword);
        }

        return movieRepository.searchNowShowing(keyword);
    }

    public List<Movie> getFeaturedMovies() {

        List<Movie> featuredMovies = new ArrayList<>();

        List<Movie> nowShowingMovies =
                movieRepository.getNowShowing();

        for (Movie movie : nowShowingMovies) {

            if (movie.getPosterUrl() != null
                    && !movie.getPosterUrl()
                            .trim().isEmpty()) {

                featuredMovies.add(movie);
            }

            if (featuredMovies.size() == 3) {
                break;
            }
        }

        return featuredMovies;
    }

    public Map<Integer, List<Genre>> getGenresByMovie(
            List<Movie> movies) {

        Map<Integer, List<Genre>> genresByMovie =
                new HashMap<>();

        List<Genre> allGenres =
                genreRepository.getAll();

        for (Movie movie : movies) {

            List<Integer> genreIds =
                    movieRepository.getGenreIds(
                            movie.getMovieId()
                    );

            List<Genre> movieGenres = new ArrayList<>();

            for (Genre genre : allGenres) {

                if (genreIds.contains(
                        genre.getGenreId())) {

                    movieGenres.add(genre);
                }
            }

            genresByMovie.put(
                    movie.getMovieId(),
                    movieGenres
            );
        }

        return genresByMovie;
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

    public void createMovie(
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

        List<Integer> genreIds =
                parseGenreIds(genreValues);

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

    // =====================================================
    // BUG 02 FIX: BẢO VỆ PHIM ĐÃ CÓ SUẤT CHIẾU
    // =====================================================

    public void updateMovie(
            int movieId,
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

        Movie oldMovie =
                movieRepository.getByIdAnyStatus(movieId);

        if (oldMovie == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy phim cần cập nhật."
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

        List<Integer> genreIds =
                parseGenreIds(genreValues);

        if (genreIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Phim phải có ít nhất một thể loại."
            );
        }

        // Kiểm tra các suất chiếu ACTIVE của phim.
        // Movie là dữ liệu dùng chung giữa các rạp nên
        // không chỉ kiểm tra suất chiếu tại cinemaId.
        List<Showtime> activeShowtimes =
                showtimeRepository.getByMovieId(movieId);

        if (!activeShowtimes.isEmpty()) {

            boolean durationChanged =
                    oldMovie.getDurationMinutes()
                    != movie.getDurationMinutes();

            boolean releaseDateChanged =
                    !Objects.equals(
                            oldMovie.getReleaseDate(),
                            movie.getReleaseDate()
                    );

            boolean endDateChanged =
                    !Objects.equals(
                            oldMovie.getEndDate(),
                            movie.getEndDate()
                    );

            boolean statusChanged =
                    !Objects.equals(
                            oldMovie.getStatus(),
                            movie.getStatus()
                    );

            if (durationChanged
                    || releaseDateChanged
                    || endDateChanged
                    || statusChanged) {

                throw new IllegalArgumentException(
                        "Không thể thay đổi thời lượng, "
                        + "ngày phát hành, ngày kết thúc "
                        + "hoặc trạng thái của phim "
                        + "đang có suất chiếu hoạt động."
                );
            }
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

    private Movie buildMovie(
            int movieId,
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

    public void deleteMovieFromCinema(
            int movieId,
            int cinemaId) {

        if (!movieRepository.belongsToCinema(
                movieId,
                cinemaId)) {

            throw new IllegalArgumentException(
                    "Phim không thuộc rạp của bạn."
            );
        }

        // Giữ nguyên ràng buộc xóa phim của project.
        if (movieRepository.hasShowtimeAtCinema(
                movieId,
                cinemaId)) {

            throw new IllegalArgumentException(
                    "Không thể xóa phim vì phim "
                    + "đã có suất chiếu tại rạp."
            );
        }

        if (!movieRepository.deleteFromCinema(
                movieId,
                cinemaId)) {

            throw new IllegalArgumentException(
                    "Không thể xóa phim."
            );
        }
    }
}
