package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Cinema;
import model.Movie;
import model.Room;
import model.Showtime;
import service.CinemaService;
import service.MovieService;
import service.RoomService;
import service.ShowtimeService;

@WebServlet(urlPatterns = {"/user/showtimes", "/admin/showtimes", "/admin/showtime-form"})
public class ShowtimeController extends HttpServlet {

    private ShowtimeService showtimeService = new ShowtimeService();
    private MovieService movieService = new MovieService();
    private CinemaService cinemaService = new CinemaService();
    private RoomService roomService = new RoomService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String view;

        switch (request.getServletPath()) {
            case "/user/showtimes":

                int movieId;

                try {
                    movieId = Integer.parseInt(request.getParameter("movieId"));
                } catch (Exception e) {
                    response.sendError(400);
                    return;
                }

                Movie movie = movieService.getMovieById(movieId);

                if (movie == null) {
                    response.sendError(404);
                    return;
                }

                LocalDate today = LocalDate.now();
                LocalDate selectedDate = today;

                String date = request.getParameter("date");

                if (date != null && !date.isEmpty()) {
                    try {
                        selectedDate = LocalDate.parse(date);
                    } catch (Exception e) {
                        selectedDate = today;
                    }
                }

                // Chỉ cho chọn 7 ngày kể từ hôm nay
                if (selectedDate.isBefore(today) || selectedDate.isAfter(today.plusDays(6))) {
                    selectedDate = today;
                }

                // Tạo danh sách 7 ngày
                List<Date> days = new ArrayList<>();

                for (int i = 0; i < 7; i++) {
                    days.add(Date.valueOf(today.plusDays(i)));
                }

                // Lấy ID rạp đang chọn
                int selectedCinemaId = 0;

                String cinemaId = request.getParameter("cinemaId");

                if (cinemaId != null && !cinemaId.isEmpty()) {
                    try {
                        selectedCinemaId = Integer.parseInt(cinemaId);
                    } catch (NumberFormatException e) {
                        selectedCinemaId = 0;
                    }
                }

                // Lấy suất chiếu từ database
                List<Showtime> showtimes = showtimeService.getByMovieAndDate(movieId, selectedDate);

                // Lấy các rạp đang hoạt động
                List<Cinema> cinemas = cinemaService.getActiveCinemas();

                // Lấy các phòng đang hoạt động
                Map<Integer, Room> roomsById = new HashMap<>();

                for (Cinema cinema : cinemas) {

                    List<Room> rooms = roomService.getRoomsByCinema(cinema.getCinemaId());

                    for (Room room : rooms) {

                        if ("ACTIVE".equalsIgnoreCase(room.getStatus())) {
                            roomsById.put(room.getRoomId(), room);
                        }
                    }
                }

                // Nhóm suất chiếu theo từng rạp
                Map<Integer, List<Showtime>> cinemaSlots  = new LinkedHashMap<>();

                for (Showtime showtime : showtimes) {

                    Room room = roomsById.get(showtime.getRoomId());

                    if (room != null) {

                        int id = room.getCinemaId();

                        if (!cinemaSlots.containsKey(id)) {
                            cinemaSlots.put(id, new ArrayList<>());
                        }

                        cinemaSlots.get(id).add(showtime);
                    }
                }

                // Kiểm tra có suất chiếu hay không
                boolean hasShowtimes;

                if (selectedCinemaId == 0) {
                    hasShowtimes = !cinemaSlots.isEmpty();
                } else {
                    hasShowtimes = cinemaSlots.containsKey(selectedCinemaId);
                }

                request.setAttribute("movie", movie);
                request.setAttribute("days", days);
                request.setAttribute("selectedDate", Date.valueOf(selectedDate));
                request.setAttribute("selectedCinemaId", selectedCinemaId);
                request.setAttribute("cinemas", cinemas);
                request.setAttribute("roomsById", roomsById);
                request.setAttribute("cinemaSlots", cinemaSlots);
                request.setAttribute("hasShowtimes", hasShowtimes);
                request.setAttribute("nowMillis", System.currentTimeMillis());

                view = "/WEB-INF/views/user/showtimes.jsp";
                break;
            case "/admin/showtimes":
                view = "/WEB-INF/views/admin/showtimes.jsp";
                break;
            case "/admin/showtime-form":
                view = "/WEB-INF/views/admin/showtime-form.jsp";
                break;
            default:
                response.sendError(404);
                return;
        }
        request.getRequestDispatcher(view).forward(request, response);
    }

}
