package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Cinema;
import model.Movie;
import model.Room;
import model.Showtime;
import model.User;
import service.CinemaService;
import service.MovieService;
import service.RoomService;
import service.ShowtimeService;
//Controller xử lý ba đường dẫn
@WebServlet(urlPatterns = {
    "/user/showtimes",
    "/admin/showtimes",
    "/admin/showtime-form"
})
public class ShowtimeController extends HttpServlet {

    private final ShowtimeService showtimeService
            = new ShowtimeService();

    private final MovieService movieService
            = new MovieService();

    private final CinemaService cinemaService
            = new CinemaService();

    private final RoomService roomService
            = new RoomService();

    @Override
    /** Hiển thị danh sách hoặc form lịch chiếu. */
    protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
        //Xác định URL
        String path = request.getServletPath();

        if ("/user/showtimes".equals(path)) {
            //Controller gọi loadUserShowtimes() để hiển thị lịch chiếu cho khách.
            loadUserShowtimes(request,response);
            return;
        }
        //Kiểm tra Manager
        User user = getManager(request, response);

        if (user == null) {
            return;
        }

        if ("/admin/showtimes".equals(path)) {
            //Lấy lịch chiếu, phim và phòng của rạp.
            loadManagerList(
                    request,
                    user
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/showtimes.jsp"
            ).forward(  
                    request,
                    response        //Hiển thị trang showtimes.jsp
            );

            return;
        }
        //Mở form thêm hoặc sửa
        if ("/admin/showtime-form".equals(path)) {
            
            String id
                    = request.getParameter("id");
            //Không có id: Mở form thêm lịch chiếu.
            //Có id: Mở form sửa lịch chiếu.
            if (id != null
                    && !id.trim().isEmpty()) {

                try {
                    //Service tìm lịch chiếu theo cả ID lịch và ID rạp của Manager.
                    int showtimeId
                            = Integer.parseInt(id);

                    Showtime showtime
                            = showtimeService
                                    .getShowtimeForManager(
                                            showtimeId,
                                            user.getCinemaId()
                                    );

                    if (showtime == null) {

                        response.sendError(
                                HttpServletResponse.SC_NOT_FOUND,
                                "Không tìm thấy lịch chiếu."
                        );

                        return;
                    }
                    //Đưa dữ liệu lịch chiếu hiện tại sang form.
                    request.setAttribute(
                            "showtime",
                            showtime
                    );

                } catch (NumberFormatException e) {

                    response.sendError(
                            HttpServletResponse.SC_BAD_REQUEST,
                            "Mã lịch chiếu không hợp lệ."
                    );

                    return;
                }
            }
            //Tải danh sách phim và phòng để Manager lựa chọn.
            loadFormData(
                    request,
                    user
            );
            //Hiển thị form.
            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/showtime-form.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }

        response.sendError(
                HttpServletResponse.SC_NOT_FOUND
        );
    }

    @Override
    /** Xử lý tạo, sửa và xóa một lịch chiếu. */
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user
                = getManager(
                        request,
                        response
                );

        if (user == null) {
            return;
        }

        String path
                = request.getServletPath();
        //Khi POST tới /admin/showtimes
        //Controller chỉ chấp nhận yêu cầu có action=delete.
        if ("/admin/showtimes".equals(path)) {

            String action
                    = request.getParameter(
                            "action"
                    );

            if (!"delete".equals(action)) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                return;
            }

            try {
                //Lấy ID lịch chiếu cần xóa.
                int showtimeId
                        = Integer.parseInt(
                                request.getParameter(
                                        "id"
                                )
                        );
                //hàm Service xử lý xóa lịch chiếu.
                showtimeService
                        .deleteShowtime(
                                showtimeId,
                                user.getCinemaId()
                        );
                //Nếu thành công, chuyển về trang danh sách lịch chiếu.
                response.sendRedirect(
                        request.getContextPath()
                        + "/admin/showtimes?deleted=1"
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

                loadManagerList(
                        request,
                        user
                );

                request.getRequestDispatcher(
                        "/WEB-INF/views/admin/showtimes.jsp"
                ).forward(
                        request,
                        response
                );
            }

            return;
        }
        //Khi POST tới /admin/showtime-form
        //Đọc ID phim và ID phòng từ form.
        if (!"/admin/showtime-form".equals(path)) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }

        try {

            int movieId
                    = Integer.parseInt(
                            request.getParameter(
                                    "movieId"
                            )
                    );

            int roomId
                    = Integer.parseInt(
                            request.getParameter(
                                    "roomId"
                            )
                    );
            //Đọc ngày và giờ chiếu.
            //vd: startDate = "2026-11-26"; startClock = "19:30"
            String startDateText
                    = request.getParameter(
                            "startDate"
                    );

            String startClockText
                    = request.getParameter(
                            "startClock"
                    );
            //Kiểm tra ngày không được để trống
            if (startDateText == null
                    || startDateText
                            .trim()
                            .isEmpty()) {

                throw new IllegalArgumentException(
                        "Ngày chiếu không được để trống."
                );
            }
            //Kiểm tra giờ không được để trống
            if (startClockText == null
                    || startClockText
                            .trim()
                            .isEmpty()) {

                throw new IllegalArgumentException(
                        "Giờ bắt đầu không được để trống."
                );
            }

            LocalDateTime startTime;

            try {
                //Chuyển chuỗi thành ngày
                LocalDate showDate
                        = LocalDate.parse(
                                startDateText.trim()
                        );
                //Chuyển chuỗi thành giờ
                LocalTime showClock
                        = LocalTime.parse(
                                startClockText.trim()
                        );
                //Ghép ngày và giờ thành thời điểm bắt đầu
                startTime
                        = LocalDateTime.of(
                                showDate,
                                showClock
                        );

            } catch (Exception e) {

                throw new IllegalArgumentException(
                        "Ngày hoặc giờ chiếu không hợp lệ."
                );
            }

            String status
                    = request.getParameter(
                            "status"
                    );

            String id
                    = request.getParameter(
                            "id"
                    );
            //Tạo lịch chiếu mới
            if (id == null
                    || id.trim().isEmpty()) {

                showtimeService
                        .createShowtime(
                                user.getCinemaId(),
                                movieId,
                                roomId,
                                startTime,
                                status
                        );

            } else {
                //Sửa lịch chiếu
                int showtimeId
                        = Integer.parseInt(
                                id
                        );
                //Nếu có ID lịch chiếu, gọi updateShowtime().
                //Khác với tạo mới, hàm sửa cần thêm showtimeId để xác định suất chiếu đang cập nhật.
                showtimeService
                        .updateShowtime(
                                showtimeId,
                                user.getCinemaId(),
                                movieId,
                                roomId,
                                startTime,
                                status
                        );
            }
            //Nếu tạo hoặc sửa thành công. Quay về danh sách lịch chiếu.
            response.sendRedirect(
                    request.getContextPath()
                    + "/admin/showtimes?saved=1"
            );

        } catch (NumberFormatException e) {

            request.setAttribute(
                    "error",
                    "Phim, phòng hoặc mã lịch chiếu không hợp lệ."
            );

            forwardForm(
                    request,
                    response,
                    user
            );

        } catch (IllegalArgumentException e) {

            request.setAttribute(
                    "error",
                    e.getMessage()
            );

            forwardForm(
                    request,
                    response,
                    user
            );
        }
    }

    /** Tải lịch chiếu của rạp cho quản lý. */
    private void loadManagerList(
            HttpServletRequest request,
            User user) {
        //Lấy lịch chiếu của rạp.
        List<Showtime> showtimes
                = showtimeService
                        .getShowtimesForManager(
                                user.getCinemaId()
                        );
        //Lấy các phim của rạp.
        List<Movie> movies
                = movieService
                        .getMoviesByCinema(
                                user.getCinemaId()
                        );
        //Lấy các phòng chiếu của rạp.
        List<Room> rooms
                = roomService
                        .getRoomsByCinema(
                                user.getCinemaId()
                        );
        //movieById giúp tra cứu đối tượng phim theo ID.
        Map<Integer, Movie> movieById
                = new HashMap<>();

        for (Movie movie : movies) {

            movieById.put(
                    movie.getMovieId(),
                    movie
            );
        }
        //Giúp tra cứu thông tin phòng từ ID phòng.
        Map<Integer, Room> roomById
                = new HashMap<>();

        for (Room room : rooms) {

            roomById.put(
                    room.getRoomId(),
                    room
            );
        }
        //Sau đó Controller đưa ba dữ liệu sang JSP
        request.setAttribute(
                "showtimes",
                showtimes
        );

        request.setAttribute(
                "movieById",
                movieById
        );

        request.setAttribute(
                "roomById",
                roomById
        );
    }

    /** Tải danh sách phim và phòng cho form thêm/sửa lịch chiếu. */
    private void loadFormData(
            HttpServletRequest request,
            User user) {

        request.setAttribute(
                "movies",
                movieService
                        .getMoviesByCinema(
                                user.getCinemaId()
                        )
        );

        request.setAttribute(
                "rooms",
                roomService
                        .getRoomsByCinema(
                                user.getCinemaId()
                        )
        );
    }

    /** Mở lại form lịch chiếu khi có lỗi. */
    private void forwardForm(
            HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws ServletException, IOException {
        //Tạo đối tượng Showtime tạm.
        Showtime showtime
                = new Showtime();
        //Sau đó lấy lại những thông tin người dùng vừa nhập
        try {

            String id
                    = request.getParameter(
                            "id"
                    );

            if (id != null
                    && !id.trim().isEmpty()) {

                showtime.setShowtimeId(
                        Integer.parseInt(id)
                );
            }

        } catch (Exception e) {

            showtime.setShowtimeId(0);
        }

        try {

            showtime.setMovieId(
                    Integer.parseInt(
                            request.getParameter(
                                    "movieId"
                            )
                    )
            );

        } catch (Exception e) {

            showtime.setMovieId(0);
        }

        try {

            showtime.setRoomId(
                    Integer.parseInt(
                            request.getParameter(
                                    "roomId"
                            )
                    )
            );

        } catch (Exception e) {

            showtime.setRoomId(0);
        }

        try {

            String dateText
                    = request.getParameter(
                            "startDate"
                    );

            String clockText
                    = request.getParameter(
                            "startClock"
                    );

            if (dateText != null
                    && !dateText.trim().isEmpty()
                    && clockText != null
                    && !clockText.trim().isEmpty()) {

                LocalDate showDate
                        = LocalDate.parse(
                                dateText.trim()
                        );

                LocalTime showClock
                        = LocalTime.parse(
                                clockText.trim()
                        );
                //Nếu dữ liệu ngày giờ hợp lệ:
                //Chuyển LocalDateTime sang Timestamp rồi đưa vào đối tượng.
                showtime.setStartTime(
                        Timestamp.valueOf(
                                LocalDateTime.of(
                                        showDate,
                                        showClock
                                )
                        )
                );
            }

        } catch (Exception e) {

            showtime.setStartTime(null);
        }

        showtime.setStatus(
                request.getParameter(
                        "status"
                )
        );
        //Tải lại form cùng dữ liệu cần thiết.
        request.setAttribute(
                "showtime",
                showtime
        );

        loadFormData(
                request,
                user
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/showtime-form.jsp"
        ).forward(
                request,
                response
        );
    }

    /** Kiểm tra quyền và rạp quản lý. */
    private User getManager(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session
                = request.getSession(false);

        if (session == null
                || session.getAttribute(
                        "user"
                ) == null) {

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

    /** Tải lịch chiếu để khách xem và chọn. */
    private void loadUserShowtimes(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Trường hợp 1: Chưa chọn phim
        //Controller xử lý tìm kiếm lịch chiếu bằng: keyword, date, cinemaId//
        if (request.getParameter("movieId") == null 
                || request.getParameter("movieId").trim().isEmpty()) {

            String keyword = request.getParameter("keyword");

            if (keyword == null) {
                keyword = "";
            }

            keyword = keyword.trim();

            LocalDate searchDate = null;

            String searchDateText = request.getParameter("date");

            if (searchDateText != null && !searchDateText.isEmpty()) {

                try {

                    searchDate = LocalDate.parse(searchDateText);

                } catch (Exception e) {
                    
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Ngày chiếu không hợp lệ.");
                    return;
                    
                }
            }

            Integer searchCinemaId = null;

            String searchCinemaText = request.getParameter("cinemaId");

            if (searchCinemaText != null && !searchCinemaText.isEmpty()) {

                try {

                    int id = Integer.parseInt(searchCinemaText);

                    if (id > 0) {
                        searchCinemaId = id;
                    }

                } catch (NumberFormatException e) {

                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Rạp không hợp lệ.");
                    return;
                    
                }
            }
            //Tạo dữ liệu lịch chiếu được nhóm theo ngày và phim.
            Map<String, Map<Movie, List<Showtime>>> scheduleByDate
                    = movieService.getSchedulesByDate(keyword,searchDate,searchCinemaId);
            //Loại bỏ nhóm không có lịch chiếu.
            scheduleByDate.remove(MovieService.NO_SCHEDULE_GROUP);
            request.setAttribute("scheduleByDate",scheduleByDate);
            request.setAttribute("roomById",movieService.getRoomById());
            request.setAttribute("cinemaById",cinemaService.getCinemaById());
            request.setAttribute("cinemas",cinemaService.getActiveCinemas());
            request.setAttribute("nowShowingMovies",movieService.getNowShowingMovies());

            request.setAttribute("keyword",keyword);
            request.setAttribute("date",searchDate == null? "" : searchDate.toString());
            request.setAttribute("cinemaId", searchCinemaId == null ? 0 : searchCinemaId);
            //Cuối cùng hiển thị
            request.getRequestDispatcher("/WEB-INF/views/user/showtime-search.jsp").forward(request,response);
            return;
        }
        //Trường hợp 2: Đã chọn phim
        //Controller lấy movieId, kiểm tra phim tồn tại rồi xử lý ngày được chọn.
        int movieId;

        try {

            movieId = Integer.parseInt(request.getParameter("movieId"));

        } catch (Exception e) {

            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        Movie movie = movieService.getMovieById(movieId);

        if (movie == null) {

            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
            
        }
        //Mặc định chọn ngày hôm nay.
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
        //Giới hạn ngày khách chọn trong khoảng hôm nay đến sáu ngày tiếp theo, tức bảy ngày tính cả hôm nay.
        if (selectedDate.isBefore(today)|| selectedDate.isAfter(today.plusDays(6))) {

            selectedDate = today;
            
        }

        List<Date> days = new ArrayList<>();

        for (int i = 0; i < 7; i++) {

            days.add(Date.valueOf(today.plusDays(i)));
            
        }

        int selectedCinemaId = 0;

        String cinemaId = request.getParameter("cinemaId");

        if (cinemaId != null && !cinemaId.isEmpty()) {

            try {

                selectedCinemaId = Integer.parseInt(cinemaId);

            } catch (NumberFormatException e) {

                selectedCinemaId = 0;
                
            }
        }
        //Lấy lịch chiếu theo phim và ngày.
        List<Showtime> showtimes = showtimeService.getByMovieAndDate( movieId,selectedDate);
        List<Cinema> cinemas= cinemaService.getActiveCinemas();
        //Controller còn lấy các rạp ACTIVE, các phòng ACTIVE rồi nhóm suất chiếu theo rạp
        Map<Integer, Room> roomsById = new HashMap<>();

        for (Cinema cinema : cinemas) {

            List<Room> rooms = roomService.getRoomsByCinema(cinema.getCinemaId());

            for (Room room : rooms) {

                if ("ACTIVE".equalsIgnoreCase(room.getStatus())) {

                    roomsById.put(room.getRoomId(),room);
                    
                }
            }
        }

        Map<Integer, List<Showtime>> cinemaSlots = new LinkedHashMap<>();

        for (Showtime showtime : showtimes) {

            Room room = roomsById.get(showtime.getRoomId());

            if (room != null) {

                int id = room.getCinemaId();

                if (!cinemaSlots.containsKey(id)) {

                    cinemaSlots.put(id,new ArrayList<>());
                    
                }

                cinemaSlots.get(id).add(showtime);
            }
        }

        boolean hasShowtimes;

        if (selectedCinemaId == 0) {

            hasShowtimes = !cinemaSlots.isEmpty();

        } else {

            hasShowtimes = cinemaSlots.containsKey(selectedCinemaId);
        }

        request.setAttribute("movie",movie);
        request.setAttribute("days",days);
        request.setAttribute("selectedDate",Date.valueOf(selectedDate));
        request.setAttribute("selectedCinemaId",selectedCinemaId);
        request.setAttribute("cinemas",cinemas);
        request.setAttribute("roomsById",roomsById);
        request.setAttribute("cinemaSlots",cinemaSlots);
        request.setAttribute("hasShowtimes",hasShowtimes);
        request.setAttribute("nowMillis",System.currentTimeMillis());
        //Kết quả hiển thị ở...
        request.getRequestDispatcher("/WEB-INF/views/user/showtimes.jsp").forward(request,response);
    }
}
