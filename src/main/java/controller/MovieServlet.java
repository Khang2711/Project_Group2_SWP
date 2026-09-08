package controller;

import dao.MovieDAO;
import dao.ShowtimeDAO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Movie;
import model.MovieSchedule;
import model.Showtime;

@WebServlet(name = "MovieServlet", urlPatterns = {"/movies", "/movie"})
public class MovieServlet extends HttpServlet {

    private static final String MOVIE_LIST_PAGE = "/WEB-INF/views/user/movieList.jsp";
    private static final String MOVIE_DETAIL_PAGE = "/WEB-INF/views/user/movieDetail.jsp";
    private static final int PAGE_SIZE = 8;

    private int parsePage(String pageParam) {
        try {
            int page = Integer.parseInt(pageParam);
            return page < 1 ? 1 : page;
        } catch (Exception e) {
            return 1;
        }
    }

    private String getKeyword(HttpServletRequest request) {
        String keyword = request.getParameter("q");
        return keyword == null ? "" : keyword.trim();
    }

    /**
     * Gom các suất chiếu thành từng card theo ngày và theo phim.
     * Ví dụ: ngày 20/07 có phim M001 lúc 09:00 và 15:00 thì chỉ tạo một card.
     */
    private List<MovieSchedule> buildMovieSchedules(List<Showtime> showtimes, MovieDAO movieDAO)
            throws Exception {

        Map<String, MovieSchedule> scheduleMap = new LinkedHashMap<>();
        Map<String, Movie> movieCache = new LinkedHashMap<>();

        for (Showtime showtime : showtimes) {
            String key = showtime.getShowDate() + "|" + showtime.getMovieID();
            MovieSchedule schedule = scheduleMap.get(key);

            if (schedule == null) {
                Movie movie = movieCache.get(showtime.getMovieID());
                if (movie == null) {
                    movie = movieDAO.getMovieByID(showtime.getMovieID());
                    movieCache.put(showtime.getMovieID(), movie);
                }

                if (movie == null) {
                    continue;
                }

                schedule = new MovieSchedule(movie, showtime.getShowDate());
                scheduleMap.put(key, schedule);
            }

            schedule.addShowtime(showtime);
        }

        return new ArrayList<>(scheduleMap.values());
    }

    private Map<String, List<MovieSchedule>> groupByDate(List<MovieSchedule> schedules) {
        Map<String, List<MovieSchedule>> result = new LinkedHashMap<>();

        for (MovieSchedule schedule : schedules) {
            List<MovieSchedule> dateSchedules = result.get(schedule.getShowDate());
            if (dateSchedules == null) {
                dateSchedules = new ArrayList<>();
                result.put(schedule.getShowDate(), dateSchedules);
            }
            dateSchedules.add(schedule);
        }

        return result;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        try {
            MovieDAO movieDAO = new MovieDAO();
            ShowtimeDAO showtimeDAO = new ShowtimeDAO();
            String action = request.getParameter("action");

            if ("/movie".equals(path) || "detail".equals(action)) {
                String movieID = request.getParameter("movieID");
                Movie movie = movieDAO.getMovieByID(movieID);
                List<Showtime> showtimes = showtimeDAO.getActiveShowtimesByMovie(movieID);

                request.setAttribute("movie", movie);
                request.setAttribute("showtimes", showtimes);
                request.getRequestDispatcher(MOVIE_DETAIL_PAGE).forward(request, response);
                return;
            }

            String keyword = getKeyword(request);
            int page = parsePage(request.getParameter("page"));

            // DAO chỉ trả về suất chiếu còn ở tương lai, phim và phòng đều đang hoạt động.
            List<Showtime> upcomingShowtimes = showtimeDAO.getUpcomingShowtimes(keyword);
            List<MovieSchedule> allSchedules = buildMovieSchedules(upcomingShowtimes, movieDAO);

            int totalRecords = allSchedules.size();
            int totalPages = (int) Math.ceil(totalRecords * 1.0 / PAGE_SIZE);
            if (totalPages < 1) {
                totalPages = 1;
            }
            if (page > totalPages) {
                page = totalPages;
            }

            int fromIndex = (page - 1) * PAGE_SIZE;
            int toIndex = Math.min(fromIndex + PAGE_SIZE, totalRecords);
            List<MovieSchedule> pageSchedules = totalRecords == 0
                    ? new ArrayList<MovieSchedule>()
                    : new ArrayList<>(allSchedules.subList(fromIndex, toIndex));

            request.setAttribute("scheduleByDate", groupByDate(pageSchedules));
            request.setAttribute("keyword", keyword);
            request.setAttribute("page", page);
            request.setAttribute("pageSize", PAGE_SIZE);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalRecords", totalRecords);
            request.getRequestDispatcher(MOVIE_LIST_PAGE).forward(request, response);
        } catch (Exception ex) {
            Logger.getLogger(MovieServlet.class.getName()).log(Level.SEVERE, null, ex);
            request.setAttribute("error", "Không thể tải lịch chiếu phim.");
            request.getRequestDispatcher(MOVIE_LIST_PAGE).forward(request, response);
        }
    }
}
