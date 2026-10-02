package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import model.Movie;
import model.Room;
import model.Showtime;
import service.CinemaService;
import service.MovieService;

/**
 * Handles movie-related requests.
 *
 * @author Kiet
 */
@WebServlet(urlPatterns = {
    "/user/movies",
    "/user/movie-detail",
    "/admin/movies",
    "/admin/movie-form"
})
public class MovieController extends HttpServlet {

    private MovieService movieService;
    private CinemaService cinemaService;

    @Override
    public void init() {
        movieService = new MovieService();
        cinemaService = new CinemaService();
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String view;

        switch (request.getServletPath()) {

            case "/user/movies":

                // Each filter is optional: use one, two or all three of them.
                String keyword = request.getParameter("keyword");

                if (keyword == null) {
                    keyword = "";
                }

                keyword = keyword.trim();

                LocalDate date = parseDate(request.getParameter("date"));
                Integer cinemaId = parseCinemaId(request.getParameter("cinemaId"));

                Map<String, Map<Movie, List<Showtime>>> scheduleByDate = movieService.getSchedulesByDate(keyword, date, cinemaId);

                int resultCount = 0;

                for (Map<Movie, List<Showtime>> dayMovies : scheduleByDate.values()) {
                    resultCount += dayMovies.size();
                }

                Map<Integer, Room> roomById = movieService.getRoomById();

                request.setAttribute("scheduleByDate", scheduleByDate);
                request.setAttribute("priceByMovie", movieService.getMinPriceByMovie(scheduleByDate, roomById));
                request.setAttribute("roomById", roomById);
                request.setAttribute("cinemaById", cinemaService.getCinemaById());
                request.setAttribute("resultCount", resultCount);

                // Send the filters back so the form keeps what the user typed.
                request.setAttribute("keyword", keyword);
                request.setAttribute("date", date == null ? "" : date.toString());
                request.setAttribute("cinemaId", cinemaId);

                view = "/WEB-INF/views/user/movies.jsp";
                break;

            case "/user/movie-detail":

                String id = request.getParameter("id");

                if (id == null || id.isEmpty()) {
                    response.sendError(400);
                    return;
                }

                int movieId = Integer.parseInt(id);

                Movie movie = movieService.getMovieById(movieId);

                if (movie == null) {
                    response.sendError(400);
                    return;
                }

                request.setAttribute("movie", movie);

                view = "/WEB-INF/views/user/movie-detail.jsp";
                break;

            case "/admin/movies":

                view = "/WEB-INF/views/admin/movies.jsp";
                break;

            case "/admin/movie-form":

                view = "/WEB-INF/views/admin/movie-form.jsp";
                break;

            default:

                response.sendError(404);
                return;
        }

        request.getRequestDispatcher(view).forward(request, response);
    }

    /**
     * Parses a yyyy-MM-dd date.
     *
     * @param value the request parameter
     * @return the date, or null if it is empty or invalid
     */
    private LocalDate parseDate(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            // Invalid date: ignore the date filter.
            return null;
        }
    }

    /**
     * Parses a cinema ID.
     *
     * @param value the request parameter
     * @return the cinema ID, or null if it is empty or invalid
     */
    private Integer parseCinemaId(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            // Invalid cinema ID: ignore the cinema filter.
            return null;
        }
    }
}