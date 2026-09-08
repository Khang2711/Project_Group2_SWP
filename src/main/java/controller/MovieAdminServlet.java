package controller;

import dao.BookingDAO;
import dao.MovieDAO;
import dao.RoomDAO;
import dao.ShowtimeDAO;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Movie;
import model.Room;
import model.Showtime;
import model.User;
import utils.MonthlyReportExcelExporter;

@WebServlet(name = "MovieAdminServlet", urlPatterns = {
    "/admin/movies", "/admin/rooms", "/admin/showtimes", "/admin/reports",
    "/admin/reports/export"
})
public class MovieAdminServlet extends HttpServlet {

    private static final String MOVIE_LIST_PAGE = "/WEB-INF/views/admin/movieList.jsp";
    private static final String MOVIE_FORM_PAGE = "/WEB-INF/views/admin/movieForm.jsp";
    private static final String DELETE_MOVIE_PAGE = "/WEB-INF/views/admin/deleteMovie.jsp";
    private static final String ROOM_LIST_PAGE = "/WEB-INF/views/admin/rooms.jsp";
    private static final String SHOWTIME_LIST_PAGE = "/WEB-INF/views/admin/showtimeList.jsp";
    private static final String SHOWTIME_FORM_PAGE = "/WEB-INF/views/admin/showtimeForm.jsp";
    private static final String REPORT_PAGE = "/WEB-INF/views/admin/reports.jsp";
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


    private boolean isFutureShowtime(String showDate, String startTime) {
        try {
            LocalDate date = LocalDate.parse(showDate);
            LocalTime time = LocalTime.parse(startTime);
            return LocalDateTime.of(date, time).isAfter(LocalDateTime.now());
        } catch (Exception e) {
            return false;
        }
    }

    private YearMonth getReportMonth(HttpServletRequest request) {
        String monthParam = request.getParameter("month");
        try {
            return YearMonth.parse(monthParam);
        } catch (Exception e) {
            return YearMonth.now();
        }
    }

    private String getReportMonthLabel(YearMonth month) {
        return String.format("Tháng %02d/%04d", month.getMonthValue(), month.getYear());
    }

    private String normalizeImageURL(String imageURL) {
        if (imageURL == null) {
            return null;
        }
        imageURL = imageURL.trim();
        return imageURL.isEmpty() ? null : imageURL;
    }

    private User getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("LOGIN_USER");
    }

    private boolean checkAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = getLoginUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return false;
        }
        if (!"AD".equals(user.getRoleID())) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!checkAdmin(request, response)) {
            return;
        }

        String path = request.getServletPath();
        try {
            if ("/admin/rooms".equals(path)) {
                loadRooms(request, response);
            } else if ("/admin/showtimes".equals(path)) {
                loadShowtimes(request, response);
            } else if ("/admin/reports/export".equals(path)) {
                exportMonthlyReport(request, response);
            } else if ("/admin/reports".equals(path)) {
                loadReports(request, response);
            } else {
                loadMovies(request, response);
            }
        } catch (Exception ex) {
            Logger.getLogger(MovieAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
            if ("/admin/reports/export".equals(path)) {
                if (!response.isCommitted()) {
                    response.reset();
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Không thể xuất file báo cáo Excel.");
                }
                return;
            }

            request.setAttribute("error", "Không thể tải dữ liệu.");
            String errorPage = "/admin/reports".equals(path) ? REPORT_PAGE : MOVIE_LIST_PAGE;
            request.getRequestDispatcher(errorPage).forward(request, response);
        }
    }

    private void loadMovies(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String action = request.getParameter("action");
        MovieDAO dao = new MovieDAO();
        new ShowtimeDAO().refreshExpiredStatuses();

        if ("create".equals(action) || "add".equals(action)) {
            request.setAttribute("mode", "create");
            request.getRequestDispatcher(MOVIE_FORM_PAGE).forward(request, response);
            return;
        }

        if ("edit".equals(action)) {
            String movieID = request.getParameter("movieID");
            Movie movie = dao.getMovieByID(movieID);
            request.setAttribute("mode", "update");
            request.setAttribute("movie", movie);
            request.getRequestDispatcher(MOVIE_FORM_PAGE).forward(request, response);
            return;
        }

        if ("delete".equals(action)) {
            String movieID = request.getParameter("movieID");
            Movie movie = dao.getMovieByID(movieID);
            request.setAttribute("movie", movie);
            request.getRequestDispatcher(DELETE_MOVIE_PAGE).forward(request, response);
            return;
        }

        String keyword = getKeyword(request);
        int page = parsePage(request.getParameter("page"));
        int totalRecords = dao.countMovies(keyword, false);
        int totalPages = (int) Math.ceil(totalRecords * 1.0 / PAGE_SIZE);
        if (totalPages < 1) {
            totalPages = 1;
        }
        if (page > totalPages) {
            page = totalPages;
        }

        request.setAttribute("movies", dao.searchMovies(keyword, page, PAGE_SIZE, false));
        request.setAttribute("keyword", keyword);
        request.setAttribute("page", page);
        request.setAttribute("pageSize", PAGE_SIZE);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", totalRecords);
        request.getRequestDispatcher(MOVIE_LIST_PAGE).forward(request, response);
    }

    private void loadRooms(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        request.setAttribute("rooms", new RoomDAO().getAllRooms());
        request.getRequestDispatcher(ROOM_LIST_PAGE).forward(request, response);
    }

    private void loadShowtimes(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String action = request.getParameter("action");
        ShowtimeDAO showtimeDAO = new ShowtimeDAO();
        MovieDAO movieDAO = new MovieDAO();
        RoomDAO roomDAO = new RoomDAO();

        if ("create".equals(action) || "add".equals(action)) {
            request.setAttribute("mode", "create");
            request.setAttribute("movies", movieDAO.getAllMovies());
            request.setAttribute("rooms", roomDAO.getActiveRooms());
            request.getRequestDispatcher(SHOWTIME_FORM_PAGE).forward(request, response);
            return;
        }

        if ("edit".equals(action)) {
            String showtimeID = request.getParameter("showtimeID");
            Showtime showtime = showtimeDAO.getShowtimeByID(showtimeID);
            request.setAttribute("mode", "update");
            request.setAttribute("showtime", showtime);
            request.setAttribute("movies", movieDAO.getAllMovies());
            request.setAttribute("rooms", roomDAO.getAllRooms());
            request.getRequestDispatcher(SHOWTIME_FORM_PAGE).forward(request, response);
            return;
        }

        request.setAttribute("showtimes", showtimeDAO.getAllShowtimes());
        request.getRequestDispatcher(SHOWTIME_LIST_PAGE).forward(request, response);
    }

    private void loadReports(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        YearMonth reportMonth = getReportMonth(request);
        LocalDate startDate = reportMonth.atDay(1);
        LocalDate endDate = reportMonth.plusMonths(1).atDay(1);
        BookingDAO bookingDAO = new BookingDAO();

        request.setAttribute("selectedMonth", reportMonth.toString());
        request.setAttribute("currentMonth", YearMonth.now().toString());
        request.setAttribute("reportMonthLabel", getReportMonthLabel(reportMonth));
        request.setAttribute("movieCount", bookingDAO.countMoviesWithRevenue(startDate, endDate));
        request.setAttribute("orderCount", bookingDAO.countOrders(startDate, endDate));
        request.setAttribute("ticketCount", bookingDAO.countTickets(startDate, endDate));
        request.setAttribute("ticketRevenue", bookingDAO.getTicketRevenue(startDate, endDate));
        request.setAttribute("comboRevenue", bookingDAO.getComboRevenue(startDate, endDate));
        request.setAttribute("revenue", bookingDAO.getRevenue(startDate, endDate));
        request.setAttribute("movieRevenueList", bookingDAO.getRevenueByMovie(startDate, endDate));
        request.getRequestDispatcher(REPORT_PAGE).forward(request, response);
    }

    private void exportMonthlyReport(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        YearMonth reportMonth = getReportMonth(request);
        LocalDate startDate = reportMonth.atDay(1);
        LocalDate endDate = reportMonth.plusMonths(1).atDay(1);
        BookingDAO bookingDAO = new BookingDAO();

        int movieCount = bookingDAO.countMoviesWithRevenue(startDate, endDate);
        int orderCount = bookingDAO.countOrders(startDate, endDate);
        int ticketCount = bookingDAO.countTickets(startDate, endDate);
        float ticketRevenue = bookingDAO.getTicketRevenue(startDate, endDate);
        float comboRevenue = bookingDAO.getComboRevenue(startDate, endDate);
        float totalRevenue = bookingDAO.getRevenue(startDate, endDate);
        List<model.MovieRevenue> movieRevenueList
                = bookingDAO.getRevenueByMovie(startDate, endDate);

        String fileName = String.format("F-Cinema_Bao-cao-doanh-thu_%04d-%02d.xlsx",
                reportMonth.getYear(), reportMonth.getMonthValue());

        response.reset();
        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

        MonthlyReportExcelExporter.write(response.getOutputStream(), reportMonth,
                movieCount, orderCount, ticketCount,
                ticketRevenue, comboRevenue, totalRevenue, movieRevenueList);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!checkAdmin(request, response)) {
            return;
        }

        try {
            String path = request.getServletPath();
            if ("/admin/rooms".equals(path)) {
                processRoom(request, response);
            } else if ("/admin/showtimes".equals(path)) {
                processShowtime(request, response);
            } else {
                processMovie(request, response);
            }
        } catch (Exception ex) {
            Logger.getLogger(MovieAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
            request.setAttribute("error", "Có lỗi xảy ra: " + ex.getMessage());
            try {
                loadMovies(request, response);
            } catch (Exception e) {
                Logger.getLogger(MovieAdminServlet.class.getName()).log(Level.SEVERE, null, e);
                request.getRequestDispatcher(MOVIE_LIST_PAGE).forward(request, response);
            }
        }
    }

    private void processMovie(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String action = request.getParameter("action");
        MovieDAO dao = new MovieDAO();
        new ShowtimeDAO().refreshExpiredStatuses();

        if ("create".equals(action) || "update".equals(action)) {
            Movie movie = new Movie(
                    request.getParameter("movieID"),
                    request.getParameter("title"),
                    request.getParameter("description"),
                    Integer.parseInt(request.getParameter("duration")),
                    Float.parseFloat(request.getParameter("basePrice")),
                    Boolean.parseBoolean(request.getParameter("status")),
                    normalizeImageURL(request.getParameter("imageURL"))
            );

            if ("create".equals(action)) {
                dao.createMovie(movie);
            } else {
                dao.updateMovie(movie);
            }
            new ShowtimeDAO().refreshExpiredStatuses();
            response.sendRedirect(request.getContextPath() + "/admin/movies");
            return;
        }

        if ("delete".equals(action)) {
            dao.deleteMovie(request.getParameter("movieID"));
            new ShowtimeDAO().refreshExpiredStatuses();
            response.sendRedirect(request.getContextPath() + "/admin/movies");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/admin/movies");
    }

    private void processRoom(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String action = request.getParameter("action");
        RoomDAO dao = new RoomDAO();

        if ("create".equals(action)) {
            Room room = new Room(
                    request.getParameter("roomID"),
                    request.getParameter("roomName"),
                    Integer.parseInt(request.getParameter("totalSeats")),
                    Boolean.parseBoolean(request.getParameter("status"))
            );
            dao.createRoom(room);
        } else if ("update".equals(action)) {
            Room room = new Room(
                    request.getParameter("roomID"),
                    request.getParameter("roomName"),
                    Integer.parseInt(request.getParameter("totalSeats")),
                    Boolean.parseBoolean(request.getParameter("status"))
            );
            dao.updateRoom(room);
        } else {
            String roomID = request.getParameter("roomID");
            boolean status = Boolean.parseBoolean(request.getParameter("status"));
            dao.updateRoomStatus(roomID, status);
        }

        new ShowtimeDAO().refreshExpiredStatuses();
        response.sendRedirect(request.getContextPath() + "/admin/rooms");
    }

    private void processShowtime(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String action = request.getParameter("action");
        ShowtimeDAO dao = new ShowtimeDAO();

        if ("delete".equals(action)) {
            dao.deleteShowtime(request.getParameter("showtimeID"));
            response.sendRedirect(request.getContextPath() + "/admin/showtimes");
            return;
        }

        Showtime showtime = new Showtime();
        showtime.setShowtimeID(request.getParameter("showtimeID"));
        showtime.setMovieID(request.getParameter("movieID"));
        showtime.setRoomID(request.getParameter("roomID"));
        showtime.setShowDate(request.getParameter("showDate"));
        showtime.setStartTime(request.getParameter("startTime"));
        showtime.setEndTime(request.getParameter("endTime"));
        showtime.setStatus(Boolean.parseBoolean(request.getParameter("status")));

        if (showtime.isStatus() && !isFutureShowtime(showtime.getShowDate(), showtime.getStartTime())) {
            forwardShowtimeError(request, response, action, showtime,
                    "Ngày và giờ bắt đầu phải lớn hơn thời gian hiện tại.");
            return;
        }

        if (showtime.getEndTime().compareTo(showtime.getStartTime()) <= 0) {
            forwardShowtimeError(request, response, action, showtime, "Giờ kết thúc phải sau giờ bắt đầu.");
            return;
        }

        Room room = new RoomDAO().getRoomByID(showtime.getRoomID());
        if (showtime.isStatus() && room != null && !room.isStatus()) {
            forwardShowtimeError(request, response, action, showtime, "Phòng đang bảo trì, không thể mở bán suất chiếu.");
            return;
        }

        String exceptID = "update".equals(action) ? showtime.getShowtimeID() : null;
        boolean conflict = dao.isRoomTimeConflict(
                showtime.getRoomID(),
                showtime.getShowDate(),
                showtime.getStartTime(),
                showtime.getEndTime(),
                exceptID
        );

        if (conflict) {
            forwardShowtimeError(request, response, action, showtime, "Phòng đã có suất chiếu trong khung giờ này.");
            return;
        }

        if ("create".equals(action) || "add".equals(action)) {
            dao.createShowtime(showtime);
        } else if ("update".equals(action)) {
            dao.updateShowtime(showtime);
        }

        response.sendRedirect(request.getContextPath() + "/admin/showtimes");
    }

    private void forwardShowtimeError(HttpServletRequest request, HttpServletResponse response,
            String action, Showtime showtime, String error) throws Exception {

        request.setAttribute("error", error);
        request.setAttribute("mode", "update".equals(action) ? "update" : "create");
        request.setAttribute("showtime", showtime);
        request.setAttribute("movies", new MovieDAO().getAllMovies());
        request.setAttribute("rooms", new RoomDAO().getAllRooms());
        request.getRequestDispatcher(SHOWTIME_FORM_PAGE).forward(request, response);
    }
}
