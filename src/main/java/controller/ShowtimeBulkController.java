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

        response.sendRedirect(
                request.getContextPath()
                + "/admin/showtime-form?mode=bulk"
        );
    }

    @Override
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

            if (status == null
                    || status.trim().isEmpty()) {

                status =
                        "ACTIVE";
            }

            String dateMode =
                    request.getParameter(
                            "dateMode"
                    );

            if (dateMode == null
                    || dateMode.trim().isEmpty()) {

                dateMode =
                        "specific";
            }

            Set<LocalDate> selectedDates =
                    new LinkedHashSet<>();

            if ("specific".equals(
                    dateMode)) {

                loadSpecificDates(
                        request,
                        selectedDates
                );

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

            List<LocalDate> sortedDates =
                    new ArrayList<>(
                            selectedDates
                    );

            sortedDates.sort(
                    LocalDate::compareTo
            );

            List<Integer> roomIds =
                    new ArrayList<>();

            List<LocalDateTime> startTimes =
                    new ArrayList<>();

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

            showtimeService
                    .createShowtimesBulk(
                            manager.getCinemaId(),
                            movieId,
                            roomIds,
                            startTimes,
                            status
                    );

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
    private void loadSpecificDates(
            HttpServletRequest request,
            Set<LocalDate> selectedDates) {

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

        for (String value
                : values) {

            if (value == null
                    || value.trim().isEmpty()) {

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
    private void loadRangeDates(
            HttpServletRequest request,
            Set<LocalDate> selectedDates) {

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

        if (endDate.isBefore(
                startDate)) {

            throw new IllegalArgumentException(
                    "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu."
            );
        }

        if (endDate.isAfter(
                startDate.plusYears(1))) {

            throw new IllegalArgumentException(
                    "Khoảng ngày tối đa là 1 năm."
            );
        }

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

        LocalDate current =
                startDate;

        while (!current.isAfter(
                endDate)) {

            int dayOfWeek =
                    current
                            .getDayOfWeek()
                            .getValue();

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

            current =
                    current.plusDays(1);
        }

        if (selectedDates.isEmpty()) {

            throw new IllegalArgumentException(
                    "Không có ngày nào phù hợp với các thứ đã chọn."
            );
        }
    }

    private void forwardWithError(
            HttpServletRequest request,
            HttpServletResponse response,
            User manager,
            String message)
            throws ServletException, IOException {

        request.setAttribute(
                "error",
                message
        );

        request.setAttribute(
                "bulkMode",
                true
        );

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