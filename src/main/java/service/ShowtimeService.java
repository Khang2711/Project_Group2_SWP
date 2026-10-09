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

    private ShowtimeRepository showtimeRepository = new ShowtimeRepository();
    private RoomRepository roomRepository = new RoomRepository();
    private CinemaRepository cinemaRepository = new CinemaRepository();
    private MovieRepository movieRepository = new MovieRepository();

    public List<Showtime> getByMovieAndDate(int movieId, LocalDate date) {
        return showtimeRepository.getByMovieIdAndDate(movieId, date);
    }

    public List<Cinema> getActiveCinema() {
        return cinemaRepository.getAllActive();
    }

    public Map<Integer, Room> getActiveRoomById() {
        Map<Integer, Room> rooms = new HashMap<>();

        for (Room room : roomRepository.getAllActive()) {
            rooms.put(room.getRoomId(), room);
        }

        return rooms;
    }

    public Map<Integer, List<Showtime>> groupByCinema(List<Showtime> showtimes, Map<Integer, Room> roomsById, List<Cinema> cinemas) {

        Map<Integer, List<Showtime>> result = new LinkedHashMap<>();

        for (Cinema cinema : cinemas) {

            for (Showtime showtime : showtimes) {

                Room room = roomsById.get(showtime.getRoomId());

                if (room != null && room.getCinemaId() == cinema.getCinemaId()) {

                    if (!result.containsKey(cinema.getCinemaId())) {

                        result.put(cinema.getCinemaId(), new ArrayList<>());
                    }

                    result.get(cinema.getCinemaId()).add(showtime);
                }
            }
        }

        return result;
    }

    public boolean isBookable(int showtimeId) {

        Showtime showtime = showtimeRepository.getById(showtimeId);

        if (showtime == null) {
            return false;
        }

        if (!"ACTIVE".equalsIgnoreCase(showtime.getStatus())) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (showtime.getStartTime() == null || !showtime.getStartTime().toLocalDateTime().isAfter(now)) {
            return false;
        }

        Movie movie = movieRepository.getById(showtime.getMovieId());

        if (movie == null) {
            return false;
        }

        LocalDate today = LocalDate.now();

        if (movie.getReleaseDate() == null || movie.getReleaseDate().toLocalDate().isAfter(today)) {
            return false;
        }

        if (movie.getEndDate() != null && movie.getEndDate().toLocalDate().isBefore(today)) {
            return false;
        }

        Room room = null;

        for (Room r : roomRepository.getAllActive()) {

            if (r.getRoomId() == showtime.getRoomId()) {
                room = r;
                break;
            }
        }

        if (room == null) {
            return false;
        }

        Cinema cinema = cinemaRepository.findById(room.getCinemaId());

        if (cinema == null || !"ACTIVE".equalsIgnoreCase(cinema.getStatus())) {
            return false;
        }

        return true;
    }
}
