package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import model.User;
import service.MovieService;
import service.RoomService;
import service.ShowtimeService;

@WebServlet(
        urlPatterns = {
            "/admin/showtime-bulk"
        }
)
public class ShowtimeBulkController
        extends HttpServlet {

    private final ShowtimeService showtimeService =
            new ShowtimeService();

    private final MovieService movieService =
            new MovieService();

    private final RoomService roomService =
            new RoomService();

    @Override
    /** Mở form tạo nhiều lịch chiếu. */
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User manager =
                getManager(
                        request,
                        response
                );

        if (manager == null) {
            return;
        }
        //Nếu hợp lệ, chuyển sang form lịch chiếu với tham số mode=bulk.
        response.sendRedirect(
                request.getContextPath()
                + "/admin/showtime-form?mode=bulk"
        );
    }

    @Override
    /** Tạo lịch cho các ngày đã chọn. */
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User manager =
                getManager(
                        request,
                        response
                );

        if (manager == null) {
            return;
        }

        try {
            //Nhận phim và phòng
            //request.getParameter() lấy dữ liệu từ form dưới dạng chuỗi. Integer.parseInt() chuyển thành số nguyên.
            int movieId =
                    Integer.parseInt(
                            request.getParameter(
                                    "movieId"
                            )
                    );

            int roomId =
                    Integer.parseInt(
                            request.getParameter(
                                    "roomId"
                            )
                    );
            //Nhận giờ bắt đầu
            String startClockText =
                    request.getParameter(
                            "startClock"
                    );

            if (startClockText == null
                    || startClockText
                            .trim()
                            .isEmpty()) {

                throw new IllegalArgumentException(
                        "Giờ bắt đầu không được để trống."
                );
            }

            LocalTime startClock;
            //Sau đó chuyển sang LocalTime
            //Ví dụ "19:30" được chuyển thành thời gian 19 giờ 30 phút.
            try {

                startClock =
                        LocalTime.parse(
                                startClockText.trim()
                        );

            } catch (Exception e) {

                throw new IllegalArgumentException(
                        "Giờ bắt đầu không hợp lệ."
                );
            }

            String status =
                    request.getParameter(
                            "status"
                    );
            //Nếu Manager không chọn trạng thái, code mặc định là ACTIVE.
            if (status == null
                    || status.trim().isEmpty()) {

                status =
                        "ACTIVE";
            }
            //Xác định cách chọn ngày
            String dateMode =
                    request.getParameter(
                            "dateMode"
                    );
            
            if (dateMode == null
                    || dateMode.trim().isEmpty()) {

                dateMode =
                        "specific";
            }
            //Dùng LinkedHashSet vì nó lưu các ngày được chọn nhưng không cho trùng lặp.
            //Ví dụ Manager chọn ngày 26/11 hai lần thì danh sách chỉ giữ một ngày.
            Set<LocalDate> selectedDates =
                    new LinkedHashSet<>();
            //Chọn từng ngày riêng lẻ
            if ("specific".equals(
                    dateMode)) {

                loadSpecificDates(
                        request,
                        selectedDates
                );
            //Chọn khoảng ngày và các thứ trong tuần
            } else if ("range".equals(
                    dateMode)) {

                loadRangeDates(
                        request,
                        selectedDates
                );

            } else {

                throw new IllegalArgumentException(
                        "Cách chọn ngày không hợp lệ."
                );
            }
            //Kiểm tra số lượng lịch chiếu
            if (selectedDates.isEmpty()) {

                throw new IllegalArgumentException(
                        "Vui lòng chọn ít nhất một ngày chiếu."
                );
            }

            if (selectedDates.size() > 50) {

                throw new IllegalArgumentException(
                        "Mỗi lần chỉ được tạo tối đa 50 lịch chiếu."
                );
            }
            //Chuyển Set thành List rồi sắp xếp các ngày theo thứ tự tăng dần.
            //Ví dụ nhập 28/11, 26/11, 27/11 thì danh sách thành 26/11, 27/11, 28/11.
            List<LocalDate> sortedDates =
                    new ArrayList<>(
                            selectedDates
                    );

            sortedDates.sort(
                    LocalDate::compareTo
            );
            //Tạo danh sách phòng và thời gian
            //Hai danh sách dùng để gửi dữ liệu sang Service
            List<Integer> roomIds =
                    new ArrayList<>();

            List<LocalDateTime> startTimes =
                    new ArrayList<>();
            //Mỗi ngày được chọn:
            //  Thêm cùng một roomId vào danh sách phòng.
            //  Ghép ngày đó với giờ bắt đầu để tạo một LocalDateTime.VD: 26/11 19:30
            for (LocalDate date
                    : sortedDates) {

                roomIds.add(
                        roomId
                );

                startTimes.add(
                        LocalDateTime.of(
                                date,
                                startClock
                        )
                );
            }
            //Gọi Service
            //Controller truyền:
            //- ID rạp của Manager.
            //- ID phim.
            //- Danh sách phòng.
            //- Danh sách thời gian.
            //- Trạng thái.
            showtimeService
                    .createShowtimesBulk(
                            manager.getCinemaId(),
                            movieId,
                            roomIds,
                            startTimes,
                            status
                    );
            //Nếu Service không phát sinh lỗi, Controller chuyển về danh sách với tham số bulkSaved=1.
            response.sendRedirect(
                    request.getContextPath()
                    + "/admin/showtimes?bulkSaved=1"
            );

        } catch (NumberFormatException e) {

            forwardWithError(
                    request,
                    response,
                    manager,
                    "Phim hoặc phòng chiếu không hợp lệ."
            );

        } catch (IllegalArgumentException e) {

            forwardWithError(
                    request,
                    response,
                    manager,
                    e.getMessage()
            );

        } catch (Exception e) {

            e.printStackTrace();

            forwardWithError(
                    request,
                    response,
                    manager,
                    "Không thể tạo loạt lịch chiếu."
            );
        }
    }

    /*
     * =====================================================
     * CHỌN TỪNG NGÀY
     * =====================================================
     */
    /** Lấy các ngày người dùng chọn riêng. */
    private void loadSpecificDates(
            HttpServletRequest request,
            Set<LocalDate> selectedDates) {
        //getParameterValues() lấy nhiều giá trị cùng tên từ form.
        //Ví dụ: 26/11, 28/11, 30/11.
        String[] values =
                request.getParameterValues(
                        "selectedDate"
                );

        if (values == null
                || values.length == 0) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn ít nhất một ngày chiếu."
            );
        }
        //Duyệt từng chuỗi ngày, chuyển thành LocalDate và thêm vào Set.
        for (String value
                : values) {

            if (value == null
                    || value.trim().isEmpty()) {
                //continue nghĩa là bỏ qua giá trị rỗng rồi tiếp tục vòng lặp.
                continue;
            }

            try {

                selectedDates.add(
                        LocalDate.parse(
                                value.trim()
                        )
                );

            } catch (Exception e) {

                throw new IllegalArgumentException(
                        "Có ngày chiếu không hợp lệ."
                );
            }
        }
    }

    /*
     * =====================================================
     * CHỌN KHOẢNG NGÀY
     * =====================================================
     */
    /** Tạo danh sách ngày theo khoảng và thứ trong tuần. */
    private void loadRangeDates(
            HttpServletRequest request,
            Set<LocalDate> selectedDates) {
        //Nếu thiếu ngày bắt đầu hoặc ngày kết thúc thì báo lỗi.
        String startText =
                request.getParameter(
                        "rangeStart"
                );

        String endText =
                request.getParameter(
                        "rangeEnd"
                );

        if (startText == null
                || startText.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Ngày bắt đầu không được để trống."
            );
        }

        if (endText == null
                || endText.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Ngày kết thúc không được để trống."
            );
        }

        LocalDate startDate;
        LocalDate endDate;

        try {
            //Chuyển chuỗi thành ngày
            startDate =
                    LocalDate.parse(
                            startText.trim()
                    );

            endDate =
                    LocalDate.parse(
                            endText.trim()
                    );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Khoảng ngày không hợp lệ."
            );
        }
        //Không cho ngày kết thúc trước ngày bắt đầu.
        if (endDate.isBefore(
                startDate)) {

            throw new IllegalArgumentException(
                    "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu."
            );
        }
        //Khoảng ngày được chọn không được vượt quá một năm.
        if (endDate.isAfter(
                startDate.plusYears(1))) {

            throw new IllegalArgumentException(
                    "Khoảng ngày tối đa là 1 năm."
            );
        }
        // 1-thứ hai ; 7-CN
        //Các giá trị từ 1 đến 7 được đưa vào Set<Integer> weekdays.
        String[] weekdayValues =
                request.getParameterValues(
                        "weekday"
                );

        if (weekdayValues == null
                || weekdayValues.length == 0) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn ít nhất một thứ trong tuần."
            );
        }

        Set<Integer> weekdays =
                new LinkedHashSet<>();

        for (String value
                : weekdayValues) {

            try {

                int weekday =
                        Integer.parseInt(
                                value
                        );

                if (weekday >= 1
                        && weekday <= 7) {

                    weekdays.add(
                            weekday
                    );
                }

            } catch (NumberFormatException e) {

                throw new IllegalArgumentException(
                        "Thứ trong tuần không hợp lệ."
                );
            }
        }
        //Vòng lặp chạy từ ngày bắt đầu đến ngày kết thúc, tính cả hai ngày.
        LocalDate current =
                startDate;

        while (!current.isAfter(
                endDate)) {
            //Lấy thứ trong tuần của ngày đang xét.
            int dayOfWeek =
                    current
                            .getDayOfWeek()
                            .getValue();
            //Nếu thứ đó nằm trong danh sách Manager đã chọn thì thêm ngày vào danh sách.                
            if (weekdays.contains(
                    dayOfWeek)) {

                selectedDates.add(
                        current
                );

                if (selectedDates.size() > 50) {

                    throw new IllegalArgumentException(
                            "Khoảng ngày tạo ra hơn 50 lịch chiếu. "
                            + "Vui lòng thu hẹp khoảng ngày."
                    );
                }
            }
            //Đây là dòng giúp vòng lặp chuyển sang ngày kế tiếp.
            current =
                    current.plusDays(1);
        }

        if (selectedDates.isEmpty()) {

            throw new IllegalArgumentException(
                    "Không có ngày nào phù hợp với các thứ đã chọn."
            );
        }
    }

    /** Hiển thị lại form hàng loạt khi lỗi. */
    private void forwardWithError(
            HttpServletRequest request,
            HttpServletResponse response,
            User manager,
            String message)
            throws ServletException, IOException {
        //Lỗi
        request.setAttribute(
                "error",
                message
        );
        //Đánh dấu đây là form tạo nhiều lịch chiếu.
        request.setAttribute(
                "bulkMode",
                true
        );
        //Cuối cùng forward về showtime-form.jsp.
        request.setAttribute(
                "movies",
                movieService
                        .getMoviesByCinema(
                                manager.getCinemaId()
                        )
        );

        request.setAttribute(
                "rooms",
                roomService
                        .getRoomsByCinema(
                                manager.getCinemaId()
                        )
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

        HttpSession session =
                request.getSession(false);

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

        String roleName =
                (String) session.getAttribute(
                        "roleName"
                );

        if (!"CINEMA_MANAGER".equals(
                roleName)) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return null;
        }
        //Manager có được phân công rạp không?
        User manager =
                (User) session.getAttribute(
                        "user"
                );

        if (manager.getCinemaId()
                == null) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Tài khoản Manager chưa được phân công rạp."
            );

            return null;
        }

        return manager;
    }
}