package service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Cinema;
import model.Movie;
import model.Room;
import model.Showtime;
import repository.CinemaRepository;
import repository.MovieRepository;
import repository.RoomRepository;
import repository.ShowtimeRepository;

public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository =
            new ShowtimeRepository();

    private final RoomRepository roomRepository =
            new RoomRepository();

    private final CinemaRepository cinemaRepository =
            new CinemaRepository();

    private final MovieRepository movieRepository =
            new MovieRepository();

    public List<Showtime> getByMovieAndDate(
            int movieId,
            LocalDate date) {

        return showtimeRepository
                .getByMovieIdAndDate(
                        movieId,
                        date
                );
    }

    public List<Cinema> getActiveCinema() {

        return cinemaRepository
                .getAllActive();
    }

    public Map<Integer, Room> getActiveRoomById() {

        Map<Integer, Room> rooms =
                new HashMap<>();

        for (Room room
                : roomRepository.getAllActive()) {

            rooms.put(
                    room.getRoomId(),
                    room
            );
        }

        return rooms;
    }

    public Map<Integer, List<Showtime>>
            groupByCinema(
                    List<Showtime> showtimes,
                    Map<Integer, Room> roomsById,
                    List<Cinema> cinemas) {

        Map<Integer, List<Showtime>> result =
                new LinkedHashMap<>();

        for (Cinema cinema : cinemas) {

            for (Showtime showtime
                    : showtimes) {

                Room room =
                        roomsById.get(
                                showtime.getRoomId()
                        );

                if (room != null
                        && room.getCinemaId()
                        == cinema.getCinemaId()) {

                    if (!result.containsKey(
                            cinema.getCinemaId())) {

                        result.put(
                                cinema.getCinemaId(),
                                new ArrayList<>()
                        );
                    }

                    result
                            .get(
                                    cinema.getCinemaId()
                            )
                            .add(showtime);
                }
            }
        }

        return result;
    }

    public boolean isBookable(
            int showtimeId) {

        Showtime showtime =
                showtimeRepository.getById(
                        showtimeId
                );

        if (showtime == null) {
            return false;
        }

        if (!"ACTIVE".equalsIgnoreCase(
                showtime.getStatus())) {

            return false;
        }

        LocalDateTime now =
                LocalDateTime.now();

        if (showtime.getStartTime() == null
                || !showtime
                        .getStartTime()
                        .toLocalDateTime()
                        .isAfter(now)) {

            return false;
        }

        Movie movie =
                movieRepository.getById(
                        showtime.getMovieId()
                );

        if (movie == null) {
            return false;
        }

        LocalDate today =
                LocalDate.now();

        if (movie.getReleaseDate() == null
                || movie.getReleaseDate()
                        .toLocalDate()
                        .isAfter(today)) {

            return false;
        }

        if (movie.getEndDate() != null
                && movie.getEndDate()
                        .toLocalDate()
                        .isBefore(today)) {

            return false;
        }

        Room room = null;

        for (Room currentRoom
                : roomRepository.getAllActive()) {

            if (currentRoom.getRoomId()
                    == showtime.getRoomId()) {

                room = currentRoom;
                break;
            }
        }

        if (room == null) {
            return false;
        }

        Cinema cinema =
                cinemaRepository.findById(
                        room.getCinemaId()
                );

        return cinema != null
                && "ACTIVE".equalsIgnoreCase(
                        cinema.getStatus()
                );
    }

    public List<Showtime>
            getShowtimesForManager(
                    int cinemaId) {

        if (cinemaId <= 0) {

            throw new IllegalArgumentException(
                    "Rạp không hợp lệ."
            );
        }

        return showtimeRepository
                .getByCinemaId(
                        cinemaId
                );
    }

    public Showtime getShowtimeForManager(
            int showtimeId,
            int cinemaId) {

        if (showtimeId <= 0
                || cinemaId <= 0) {

            return null;
        }

        return showtimeRepository
                .getByIdAndCinema(
                        showtimeId,
                        cinemaId
                );
    }

    public void createShowtime(
            int cinemaId,
            int movieId,
            int roomId,
            LocalDateTime startTime,
            String status) {

        Showtime showtime =
                buildShowtime(
                        0,
                        cinemaId,
                        movieId,
                        roomId,
                        startTime,
                        status,
                        null
                );

        if (!showtimeRepository.insert(
                showtime)) {

            throw new IllegalArgumentException(
                    "Không thể thêm lịch chiếu."
            );
        }
    }

    /*
     * Tạo nhiều ca cho cùng một phim.
     */
    public void createShowtimesBulk(
            int cinemaId,
            int movieId,
            List<Integer> roomIds,
            List<LocalDateTime> startTimes,
            String status) {

        if (roomIds == null
                || startTimes == null
                || roomIds.isEmpty()
                || startTimes.isEmpty()) {

            throw new IllegalArgumentException(
                    "Vui lòng thêm ít nhất một ca chiếu."
            );
        }

        if (roomIds.size()
                != startTimes.size()) {

            throw new IllegalArgumentException(
                    "Dữ liệu các ca chiếu không hợp lệ."
            );
        }

        if (roomIds.size() > 50) {

            throw new IllegalArgumentException(
                    "Mỗi lần chỉ được tạo tối đa 50 ca chiếu."
            );
        }

        List<Showtime> newShowtimes =
                new ArrayList<>();

        for (int i = 0;
                i < roomIds.size();
                i++) {

            try {

                Showtime showtime =
                        buildShowtime(
                                0,
                                cinemaId,
                                movieId,
                                roomIds.get(i),
                                startTimes.get(i),
                                status,
                                null
                        );

                newShowtimes.add(
                        showtime
                );

            } catch (IllegalArgumentException e) {

                throw new IllegalArgumentException(
                        "Ca "
                        + (i + 1)
                        + ": "
                        + e.getMessage()
                );
            }
        }

        /*
         * buildShowtime kiểm tra với dữ liệu
         * đang có trong database.
         *
         * Đoạn này kiểm tra các ca vừa nhập
         * có tự đè lên nhau hay không.
         */
        if ("ACTIVE".equals(status)) {

            for (int i = 0;
                    i < newShowtimes.size();
                    i++) {

                Showtime first =
                        newShowtimes.get(i);

                for (int j = i + 1;
                        j < newShowtimes.size();
                        j++) {

                    Showtime second =
                            newShowtimes.get(j);

                    if (first.getRoomId()
                            != second.getRoomId()) {

                        continue;
                    }

                    boolean overlap =
                            first.getStartTime()
                                    .before(
                                            second.getEndTime()
                                    )
                            && first.getEndTime()
                                    .after(
                                            second.getStartTime()
                                    );

                    if (overlap) {

                        throw new IllegalArgumentException(
                                "Ca "
                                + (i + 1)
                                + " và ca "
                                + (j + 1)
                                + " bị trùng thời gian trong cùng phòng."
                        );
                    }
                }
            }
        }

        if (!showtimeRepository
                .insertBatch(
                        newShowtimes
                )) {

            throw new IllegalArgumentException(
                    "Không thể tạo loạt lịch chiếu."
            );
        }
    }

    public void updateShowtime(
            int showtimeId,
            int cinemaId,
            int movieId,
            int roomId,
            LocalDateTime startTime,
            String status) {

        Showtime oldShowtime =
                showtimeRepository
                        .getByIdAndCinema(
                                showtimeId,
                                cinemaId
                        );

        if (oldShowtime == null) {

            throw new IllegalArgumentException(
                    "Không tìm thấy lịch chiếu."
            );
        }

        Showtime showtime =
                buildShowtime(
                        showtimeId,
                        cinemaId,
                        movieId,
                        roomId,
                        startTime,
                        status,
                        showtimeId
                );

        if (!showtimeRepository.update(
                showtime,
                cinemaId)) {

            throw new IllegalArgumentException(
                    "Không thể cập nhật lịch chiếu."
            );
        }
    }

    public void deleteShowtime(
            int showtimeId,
            int cinemaId) {

        Showtime showtime =
                showtimeRepository
                        .getByIdAndCinema(
                                showtimeId,
                                cinemaId
                        );

        if (showtime == null) {

            throw new IllegalArgumentException(
                    "Không tìm thấy lịch chiếu."
            );
        }

        if (showtimeRepository.hasTickets(
                showtimeId)) {

            if (!showtimeRepository
                    .setInactive(
                            showtimeId,
                            cinemaId
                    )) {

                throw new IllegalArgumentException(
                        "Không thể ngừng lịch chiếu."
                );
            }

            return;
        }

        if (!showtimeRepository.delete(
                showtimeId,
                cinemaId)) {

            throw new IllegalArgumentException(
                    "Không thể xóa lịch chiếu."
            );
        }
    }

    private Showtime buildShowtime(
            int showtimeId,
            int cinemaId,
            int movieId,
            int roomId,
            LocalDateTime startTime,
            String status,
            Integer excludeShowtimeId) {

        if (cinemaId <= 0) {

            throw new IllegalArgumentException(
                    "Rạp không hợp lệ."
            );
        }

        Movie movie =
                movieRepository
                        .getByIdAnyStatus(
                                movieId
                        );

        if (movie == null
                || !movieRepository
                        .belongsToCinema(
                                movieId,
                                cinemaId
                        )) {

            throw new IllegalArgumentException(
                    "Phim không thuộc rạp của bạn."
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
                    "Phòng chiếu không thuộc rạp của bạn."
            );
        }

        if (startTime == null) {

            throw new IllegalArgumentException(
                    "Thời gian bắt đầu không được để trống."
            );
        }

        if (!"ACTIVE".equals(status)
                && !"INACTIVE".equals(status)) {

            throw new IllegalArgumentException(
                    "Trạng thái lịch chiếu không hợp lệ."
            );
        }

        if ("ACTIVE".equals(status)) {

            if (!"ACTIVE".equalsIgnoreCase(
                    movie.getStatus())) {

                throw new IllegalArgumentException(
                        "Phim đang không hoạt động."
                );
            }

            if (!"ACTIVE".equalsIgnoreCase(
                    room.getStatus())) {

                throw new IllegalArgumentException(
                        "Phòng chiếu đang không hoạt động."
                );
            }

            if (!startTime.isAfter(
                    LocalDateTime.now())) {

                throw new IllegalArgumentException(
                        "Suất chiếu phải bắt đầu trong tương lai."
                );
            }
        }

        LocalDate showDate =
                startTime.toLocalDate();

        if (movie.getReleaseDate() != null
                && showDate.isBefore(
                        movie.getReleaseDate()
                                .toLocalDate()
                )) {

            throw new IllegalArgumentException(
                    "Ngày chiếu không được trước ngày phát hành phim."
            );
        }

        if (movie.getEndDate() != null
                && showDate.isAfter(
                        movie.getEndDate()
                                .toLocalDate()
                )) {

            throw new IllegalArgumentException(
                    "Phim đã hết thời gian chiếu tại rạp."
            );
        }

        if (movie.getDurationMinutes()
                <= 0) {

            throw new IllegalArgumentException(
                    "Thời lượng phim không hợp lệ."
            );
        }

        LocalDateTime endTime =
                startTime.plusMinutes(
                        movie.getDurationMinutes()
                );

        Timestamp startTimestamp =
                Timestamp.valueOf(
                        startTime
                );

        Timestamp endTimestamp =
                Timestamp.valueOf(
                        endTime
                );

        if ("ACTIVE".equals(status)
                && showtimeRepository.hasConflict(
                        roomId,
                        startTimestamp,
                        endTimestamp,
                        excludeShowtimeId
                )) {

            throw new IllegalArgumentException(
                    "Phòng chiếu đã có lịch khác trong khoảng thời gian này."
            );
        }

        Showtime showtime =
                new Showtime();

        showtime.setShowtimeId(
                showtimeId
        );

        showtime.setMovieId(
                movieId
        );

        showtime.setRoomId(
                roomId
        );

        showtime.setStartTime(
                startTimestamp
        );

        showtime.setEndTime(
                endTimestamp
        );

        showtime.setStatus(
                status
        );

        return showtime;
    }
}