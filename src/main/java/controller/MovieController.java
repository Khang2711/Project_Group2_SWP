package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import model.Movie;
import service.CinemaService;
import service.MovieService;
import java.util.ArrayList;
import model.Genre;
import model.Room;
import model.Showtime;
import model.User;
import util.ProjectImageStorage;

/**
 * Handles movie-related requests.
 *
 * @author Kiet
 */
@MultipartConfig(
        maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 8L * 1024 * 1024
)
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

                String type = request.getParameter("type");

                if (!"coming".equals(type)) {
                    type = "now";
                }

                String keyword = request.getParameter("keyword");

                if (keyword == null) {
                    keyword = "";
                }

                keyword = keyword.trim();

                int page = parsePage(request.getParameter("page"));
                int pageSize = 8;

                List<Movie> filteredMovies = movieService.getMoviesForList(type, keyword);

                int totalMovies = filteredMovies.size();
                LocalDate date = parseDate(
                        request.getParameter("date")
                );

                Integer cinemaId = parseCinemaId(
                        request.getParameter("cinemaId")
                );

                Map<String, Map<Movie, List<Showtime>>> scheduleByDate
                        = movieService.getSchedulesByDate(
                                keyword,
                                date,
                                cinemaId
                        );

                int totalPages = (int) Math.ceil((double) totalMovies / pageSize);

                if (page > totalPages && totalPages > 0) {
                    page = totalPages;
                }

                int start = (page - 1) * pageSize;
                int end = Math.min(start + pageSize, totalMovies);

                List<Movie> movies = new java.util.ArrayList<>();

                if (start < totalMovies) {
                    movies.addAll(filteredMovies.subList(start, end));
                }

                Map<Integer, List<Genre>> genresByMovie = movieService.getGenresByMovie(movies);

                request.setAttribute("movies", movies);
                request.setAttribute("genresByMovie", genresByMovie);
                request.setAttribute("type", type);
                request.setAttribute("keyword", keyword);
                request.setAttribute("currentPage", page);
                request.setAttribute("totalPages", totalPages);
                request.setAttribute("totalMovies", totalMovies);
                request.setAttribute("nowShowingMovies", movieService.getNowShowingMovies());

                int resultCount = 0;

                for (Map<Movie, List<Showtime>> dayMovies
                        : scheduleByDate.values()) {

                    resultCount += dayMovies.size();
                }

                Map<Integer, Room> roomById
                        = movieService.getRoomById();

                request.setAttribute(
                        "scheduleByDate",
                        scheduleByDate
                );

                request.setAttribute("priceByMovie",movieService.getMinPriceByMovie(scheduleByDate));
                request.setAttribute("roomById", roomById);
                request.setAttribute("cinemaById", cinemaService.getCinemaById());
                request.setAttribute("resultCount",resultCount);
                request.setAttribute("keyword",keyword);
                request.setAttribute("date",date == null ? "" : date.toString());
                request.setAttribute( "cinemaId", cinemaId);

                view = "/WEB-INF/views/user/movies.jsp";

                break;

            case "/user/movie-detail":

                String id = request.getParameter("id");
                int movieId;

                try {
                    movieId = Integer.parseInt(id);
                } catch (NumberFormatException e) {
                    response.sendError(400);
                    return;
                }

                Movie movie = movieService.getMovieById(movieId);

                if (movie == null) {
                    response.sendError(404);
                    return;
                }

                LocalDate today = LocalDate.now();

                boolean comingSoon = movie.getReleaseDate() != null
                        && movie.getReleaseDate().toLocalDate().isAfter(today);

                boolean ended = movie.getEndDate() != null
                        && movie.getEndDate().toLocalDate().isBefore(today);

                List<Movie> detailMovies = new ArrayList<>();
                detailMovies.add(movie);

                Map<Integer, List<Genre>> detailGenres = movieService.getGenresByMovie(detailMovies);

                request.setAttribute("movie", movie);
                request.setAttribute("movieGenres", detailGenres.get(movieId));
                request.setAttribute("comingSoon", comingSoon);
                request.setAttribute("ended", ended);

                view = "/WEB-INF/views/user/movie-detail.jsp";

                break;

            case "/admin/movies":

                User manager
                        = getManager(
                                request,
                                response
                        );

                if (manager == null) {
                    return;
                }

                request.setAttribute("movies", movieService.getMoviesByCinema(manager.getCinemaId()));

                view = "/WEB-INF/views/admin/movies.jsp";

                break;

            case "/admin/movie-form":

                User formManager
                        = getManager(
                                request,
                                response
                        );

                if (formManager == null) {
                    return;
                }

                request.setAttribute(
                        "genres",
                        movieService.getActiveGenres()
                );

                String movieIdText
                        = request.getParameter("id");

                if (movieIdText != null
                        && !movieIdText.trim().isEmpty()) {

                    try {

                        int editMovieId
                                = Integer.parseInt(
                                        movieIdText
                                );

                        Movie editMovie
                                = movieService.getMovieForManager(
                                        editMovieId,
                                        formManager.getCinemaId()
                                );

                        if (editMovie == null) {

                            response.sendError(
                                    HttpServletResponse.SC_NOT_FOUND
                            );

                            return;
                        }

                        request.setAttribute(
                                "movie",
                                editMovie
                        );

                        request.setAttribute(
                                "selectedGenreIds",
                                movieService.getGenreIds(
                                        editMovieId
                                )
                        );

                    } catch (NumberFormatException e) {

                        response.sendError(
                                HttpServletResponse.SC_BAD_REQUEST
                        );

                        return;
                    }
                }

                view = "/WEB-INF/views/admin/movie-form.jsp";

                break;

            default:

                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND
                );

                return;
        }

        request.getRequestDispatcher(view)
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // ==========================
        // XÓA PHIM KHỎI RẠP
        // ==========================
        if ("/admin/movies".equals(path)) {

            User manager
                    = getManager(
                            request,
                            response
                    );

            if (manager == null) {
                return;
            }

            try {

                int movieId
                        = Integer.parseInt(
                                request.getParameter("id")
                        );

                movieService.deleteMovieFromCinema(
                        movieId,
                        manager.getCinemaId()
                );

                response.sendRedirect(
                        request.getContextPath()
                        + "/admin/movies?deleted=1"
                );

            } catch (NumberFormatException e) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST
                );

            } catch (IllegalArgumentException e) {

                request.setAttribute(
                        "error",
                        e.getMessage()
                );

                request.setAttribute(
                        "movies",
                        movieService.getMoviesByCinema(
                                manager.getCinemaId()
                        )
                );

                request.getRequestDispatcher(
                        "/WEB-INF/views/admin/movies.jsp"
                ).forward(
                        request,
                        response
                );
            }

            return;
        }

        // ==========================
        // THÊM / SỬA PHIM
        // ==========================
        if (!"/admin/movie-form".equals(path)) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }

        User manager
                = getManager(
                        request,
                        response
                );

        if (manager == null) {
            return;
        }

        String id = request.getParameter("id");
        String existingPosterUrl = null;
        String uploadedPosterUrl = null;

        try {
            // Lấy poster hiện tại từ DB để khi sửa mà không chọn ảnh mới,
            // poster cũ vẫn được giữ nguyên.
            if (id != null && !id.trim().isEmpty()) {
                int existingMovieId = Integer.parseInt(id);
                Movie existingMovie = movieService.getMovieForManager(
                        existingMovieId, manager.getCinemaId());
                if (existingMovie == null) {
                    throw new IllegalArgumentException("Không tìm thấy phim trong rạp này.");
                }
                existingPosterUrl = existingMovie.getPosterUrl();
            }

            Part posterPart = request.getPart("moviePoster");
            if (posterPart != null && posterPart.getSize() > 0) {
                try {
                    uploadedPosterUrl = ProjectImageStorage.saveImage(
                            posterPart, "movies", getServletContext());
                } catch (IOException e) {
                    throw new IllegalStateException(
                            "Không thể lưu poster phim. Vui lòng thử lại.", e);
                }
            }
            String posterUrl = uploadedPosterUrl != null
                    ? uploadedPosterUrl : existingPosterUrl;

            // THÊM PHIM
            if (id == null
                    || id.trim().isEmpty()) {

                movieService.createMovie(
                        manager.getCinemaId(),
                        request.getParameter("title"),
                        request.getParameter("description"),
                        request.getParameter("durationMinutes"),
                        request.getParameter("ageRating"),
                        request.getParameter("releaseDate"),
                        request.getParameter("endDate"),
                        posterUrl,
                        request.getParameter("trailerUrl"),
                        request.getParameter("status"),
                        request.getParameterValues("genreId")
                );

            } else {

                // SỬA PHIM
                int movieId
                        = Integer.parseInt(id);

                movieService.updateMovie(
                        movieId,
                        manager.getCinemaId(),
                        request.getParameter("title"),
                        request.getParameter("description"),
                        request.getParameter("durationMinutes"),
                        request.getParameter("ageRating"),
                        request.getParameter("releaseDate"),
                        request.getParameter("endDate"),
                        posterUrl,
                        request.getParameter("trailerUrl"),
                        request.getParameter("status"),
                        request.getParameterValues("genreId")
                );
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/admin/movies?saved=1"
            );

        } catch (NumberFormatException e) {

            ProjectImageStorage.deleteUploadedImage(uploadedPosterUrl, getServletContext());
            request.setAttribute("existingPosterUrl", existingPosterUrl);
            request.setAttribute(
                    "error",
                    "Mã phim không hợp lệ."
            );

            forwardAdminForm(
                    request,
                    response,
                    manager
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            ProjectImageStorage.deleteUploadedImage(uploadedPosterUrl, getServletContext());
            request.setAttribute("existingPosterUrl", existingPosterUrl);
            request.setAttribute(
                    "error",
                    e.getMessage()
            );

            forwardAdminForm(
                    request,
                    response,
                    manager
            );
        }
    }

    /**
     * Parses a yyyy-MM-dd date.
     *
     * @param value the request parameter
     * @return the date, or null if it is empty or invalid
     */
    private LocalDate parseDate(String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        try {

            return LocalDate.parse(
                    value.trim()
            );

        } catch (DateTimeParseException e) {

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

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        try {

            return Integer.valueOf(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            return null;
        }
    }

    /**
     * Parses the page number from the request.
     *
     * @param value the page parameter
     * @return a valid page number starting from 1
     */
    private int parsePage(String value) {

        if (value == null || value.trim().isEmpty()) {
            return 1;
        }

        try {
            int page = Integer.parseInt(value.trim());

            if (page < 1) {
                return 1;
            }

            return page;

        } catch (NumberFormatException e) {
            return 1;
        }
    }

    /* Forward lại form khi dữ liệu thêm/sửa bị lỗi.
     */
    private void forwardAdminForm(
            HttpServletRequest request,
            HttpServletResponse response,
            User manager)
            throws ServletException, IOException {

        Movie movie = new Movie();

        String id
                = request.getParameter("id");

        if (id != null
                && !id.trim().isEmpty()) {

            try {

                movie.setMovieId(
                        Integer.parseInt(id)
                );

            } catch (NumberFormatException e) {

                movie.setMovieId(0);
            }
        }

        movie.setTitle(
                request.getParameter("title")
        );

        movie.setDescription(
                request.getParameter("description")
        );

        try {

            movie.setDurationMinutes(
                    Integer.parseInt(
                            request.getParameter(
                                    "durationMinutes"
                            )
                    )
            );

        } catch (NumberFormatException e) {

            movie.setDurationMinutes(0);
        }

        movie.setAgeRating(
                request.getParameter(
                        "ageRating"
                )
        );

        try {

            String release
                    = request.getParameter(
                            "releaseDate"
                    );

            if (release != null
                    && !release.trim().isEmpty()) {

                movie.setReleaseDate(
                        java.sql.Date.valueOf(
                                release
                        )
                );
            }

        } catch (IllegalArgumentException e) {

            movie.setReleaseDate(null);
        }

        try {

            String end
                    = request.getParameter(
                            "endDate"
                    );

            if (end != null
                    && !end.trim().isEmpty()) {

                movie.setEndDate(
                        java.sql.Date.valueOf(
                                end
                        )
                );
            }

        } catch (IllegalArgumentException e) {

            movie.setEndDate(null);
        }

        movie.setPosterUrl(
                (String) request.getAttribute("existingPosterUrl")
        );

        movie.setTrailerUrl(
                request.getParameter(
                        "trailerUrl"
                )
        );

        movie.setStatus(
                request.getParameter(
                        "status"
                )
        );

        List<Integer> selectedGenreIds
                = new java.util.ArrayList<>();

        String[] genreValues
                = request.getParameterValues(
                        "genreId"
                );

        if (genreValues != null) {

            for (String value : genreValues) {

                try {

                    selectedGenreIds.add(
                            Integer.parseInt(
                                    value
                            )
                    );

                } catch (NumberFormatException e) {

                    // Service sẽ xử lý dữ liệu không hợp lệ.
                }
            }
        }

        request.setAttribute(
                "movie",
                movie
        );

        request.setAttribute(
                "genres",
                movieService.getActiveGenres()
        );

        request.setAttribute(
                "selectedGenreIds",
                selectedGenreIds
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/movie-form.jsp"
        ).forward(
                request,
                response
        );
    }

    /**
     * Kiểm tra tài khoản Cinema Manager.
     */
    private User getManager(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session
                = request.getSession(false);

        if (session == null
                || session.getAttribute("user")
                == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/login"
            );

            return null;
        }

        String roleName
                = (String) session.getAttribute(
                        "roleName"
                );

        if (!"CINEMA_MANAGER".equals(
                roleName)) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return null;
        }

        User user
                = (User) session.getAttribute(
                        "user"
                );

        if (user.getCinemaId() == null) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Tài khoản Manager chưa được phân công rạp."
            );

            return null;
        }

        return user;
    }
}
