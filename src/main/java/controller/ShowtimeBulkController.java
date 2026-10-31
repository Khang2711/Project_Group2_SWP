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
import java.util.List;
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

            String status =
                    request.getParameter(
                            "status"
                    );

            String[] roomValues =
                    request.getParameterValues(
                            "bulkRoomId"
                    );

            String[] dateValues =
                    request.getParameterValues(
                            "bulkDate"
                    );

            String[] timeValues =
                    request.getParameterValues(
                            "bulkTime"
                    );

            if (roomValues == null
                    || dateValues == null
                    || timeValues == null
                    || roomValues.length == 0) {

                throw new IllegalArgumentException(
                        "Vui lòng thêm ít nhất một ca chiếu."
                );
            }

            if (roomValues.length
                    != dateValues.length
                    || roomValues.length
                    != timeValues.length) {

                throw new IllegalArgumentException(
                        "Dữ liệu các ca chiếu không hợp lệ."
                );
            }

            List<Integer> roomIds =
                    new ArrayList<>();

            List<LocalDateTime> startTimes =
                    new ArrayList<>();

            for (int i = 0;
                    i < roomValues.length;
                    i++) {

                if (roomValues[i] == null
                        || roomValues[i].trim().isEmpty()) {

                    throw new IllegalArgumentException(
                            "Ca "
                            + (i + 1)
                            + ": chưa chọn phòng chiếu."
                    );
                }

                if (dateValues[i] == null
                        || dateValues[i].trim().isEmpty()) {

                    throw new IllegalArgumentException(
                            "Ca "
                            + (i + 1)
                            + ": chưa chọn ngày chiếu."
                    );
                }

                if (timeValues[i] == null
                        || timeValues[i].trim().isEmpty()) {

                    throw new IllegalArgumentException(
                            "Ca "
                            + (i + 1)
                            + ": chưa chọn giờ bắt đầu."
                    );
                }

                int roomId =
                        Integer.parseInt(
                                roomValues[i]
                        );

                LocalDate date =
                        LocalDate.parse(
                                dateValues[i]
                        );

                LocalTime time =
                        LocalTime.parse(
                                timeValues[i]
                        );

                roomIds.add(
                        roomId
                );

                startTimes.add(
                        LocalDateTime.of(
                                date,
                                time
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

        } catch (Exception e) {

            forwardWithError(
                    request,
                    response,
                    manager,
                    e.getMessage()
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
                || session.getAttribute("user")
                == null) {

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