package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.Room;
import model.Seat;
import model.User;
import service.RoomService;
import service.SeatService;

@WebServlet(urlPatterns = {
    "/admin/seats",
    "/admin/seat-form"
})
public class SeatController extends HttpServlet {

    private final SeatService seatService =
            new SeatService();

    private final RoomService roomService =
            new RoomService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user =
                getManager(request, response);

        if (user == null) {
            return;
        }

        int roomId;

        try {

            roomId =
                    Integer.parseInt(
                            request.getParameter(
                                    "roomId"
                            )
                    );

        } catch (NumberFormatException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Mã phòng không hợp lệ."
            );

            return;
        }

        Room room =
                roomService.getRoom(
                        roomId,
                        user.getCinemaId()
                );

        if (room == null) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }

        String path =
                request.getServletPath();

        if ("/admin/seats".equals(path)) {

            loadSeatMap(
                    request,
                    room,
                    user
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/seats.jsp"
            ).forward(request, response);

            return;
        }

        if ("/admin/seat-form".equals(path)) {

            String id =
                    request.getParameter("id");

            if (id != null
                    && !id.trim().isEmpty()) {

                try {

                    int seatId =
                            Integer.parseInt(id);

                    Seat seat =
                            seatService.getSeat(
                                    seatId,
                                    roomId,
                                    user.getCinemaId()
                            );

                    if (seat == null) {

                        response.sendError(
                                HttpServletResponse.SC_NOT_FOUND
                        );

                        return;
                    }

                    request.setAttribute(
                            "seat",
                            seat
                    );

                } catch (NumberFormatException e) {

                    response.sendError(
                            HttpServletResponse.SC_BAD_REQUEST
                    );

                    return;
                }
            }

            request.setAttribute(
                    "room",
                    room
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/seat-form.jsp"
            ).forward(request, response);

            return;
        }

        response.sendError(
                HttpServletResponse.SC_NOT_FOUND
        );
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user =
                getManager(request, response);

        if (user == null) {
            return;
        }

        String path =
                request.getServletPath();

        // =====================================================
        // SEAT MAP ACTION
        // =====================================================
        if ("/admin/seats".equals(path)) {

            int roomId;

            try {

                roomId =
                        Integer.parseInt(
                                request.getParameter(
                                        "roomId"
                                )
                        );

            } catch (NumberFormatException e) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                return;
            }

            String action =
                    request.getParameter(
                            "action"
                    );

            try {

                if ("bulkUpdate".equals(action)) {

                    List<Integer> seatIds =
                            parseSeatIds(
                                    request.getParameterValues(
                                            "seatId"
                                    )
                            );

                    seatService.bulkUpdateSeats(
                            seatIds,
                            roomId,
                            user.getCinemaId(),
                            request.getParameter(
                                    "seatType"
                            ),
                            request.getParameter(
                                    "status"
                            )
                    );

                    response.sendRedirect(
                            request.getContextPath()
                            + "/admin/seats?roomId="
                            + roomId
                            + "&updated=1"
                    );

                    return;
                }

                if ("addRow".equals(action)) {

                    int seatCount =
                            Integer.parseInt(
                                    request.getParameter(
                                            "seatCount"
                                    )
                            );

                    seatService.addRow(
                            roomId,
                            user.getCinemaId(),
                            seatCount,
                            request.getParameter(
                                    "rowSeatType"
                            )
                    );

                    response.sendRedirect(
                            request.getContextPath()
                            + "/admin/seats?roomId="
                            + roomId
                            + "&rowAdded=1"
                    );

                    return;
                }

                if ("deleteSelected".equals(
                        action)) {

                    List<Integer> seatIds =
                            parseSeatIds(
                                    request.getParameterValues(
                                            "seatId"
                                    )
                            );

                    seatService.deleteSeats(
                            seatIds,
                            roomId,
                            user.getCinemaId()
                    );

                    response.sendRedirect(
                            request.getContextPath()
                            + "/admin/seats?roomId="
                            + roomId
                            + "&deleted=1"
                    );

                    return;
                }

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST
                );

            } catch (NumberFormatException e) {

                showSeatMapError(
                        request,
                        response,
                        user,
                        roomId,
                        "Số lượng ghế không hợp lệ."
                );

            } catch (IllegalArgumentException e) {

                showSeatMapError(
                        request,
                        response,
                        user,
                        roomId,
                        e.getMessage()
                );
            }

            return;
        }

        // =====================================================
        // CREATE / UPDATE 1 SEAT
        // =====================================================
        if (!"/admin/seat-form".equals(path)) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }

        String id =
                request.getParameter("id");

        try {

            int roomId =
                    Integer.parseInt(
                            request.getParameter(
                                    "roomId"
                            )
                    );

            int seatRow =
                    Integer.parseInt(
                            request.getParameter(
                                    "seatRow"
                            )
                    );

            int seatNumber =
                    Integer.parseInt(
                            request.getParameter(
                                    "seatNumber"
                            )
                    );

            String seatType =
                    request.getParameter(
                            "seatType"
                    );

            String status =
                    request.getParameter(
                            "status"
                    );

            if (id == null
                    || id.trim().isEmpty()) {

                seatService.createSeat(
                        roomId,
                        user.getCinemaId(),
                        seatRow,
                        seatNumber,
                        seatType,
                        status
                );

            } else {

                int seatId =
                        Integer.parseInt(id);

                seatService.updateSeat(
                        seatId,
                        roomId,
                        user.getCinemaId(),
                        seatRow,
                        seatNumber,
                        seatType,
                        status
                );
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/admin/seats?roomId="
                    + roomId
                    + "&updated=1"
            );

        } catch (NumberFormatException e) {

            request.setAttribute(
                    "error",
                    "Hàng ghế hoặc số ghế không hợp lệ."
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

    private void showSeatMapError(
            HttpServletRequest request,
            HttpServletResponse response,
            User user,
            int roomId,
            String message)
            throws ServletException, IOException {

        Room room =
                roomService.getRoom(
                        roomId,
                        user.getCinemaId()
                );

        if (room == null) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }

        request.setAttribute(
                "error",
                message
        );

        loadSeatMap(
                request,
                room,
                user
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/seats.jsp"
        ).forward(request, response);
    }

    private void loadSeatMap(
            HttpServletRequest request,
            Room room,
            User user) {

        // Load room lại để capacity mới nhất.
        Room latestRoom =
                roomService.getRoom(
                        room.getRoomId(),
                        user.getCinemaId()
                );

        request.setAttribute(
                "room",
                latestRoom
        );

        request.setAttribute(
                "seatRows",
                seatService.getSeatRowsByRoom(
                        room.getRoomId(),
                        user.getCinemaId()
                )
        );
    }

    private List<Integer> parseSeatIds(
            String[] values) {

        if (values == null
                || values.length == 0) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn ít nhất một ghế."
            );
        }

        List<Integer> ids =
                new ArrayList<>();

        for (String value : values) {

            try {

                int id =
                        Integer.parseInt(value);

                if (id <= 0) {
                    throw new NumberFormatException();
                }

                ids.add(id);

            } catch (NumberFormatException e) {

                throw new IllegalArgumentException(
                        "Ghế được chọn không hợp lệ."
                );
            }
        }

        return ids;
    }

    private void forwardForm(
            HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws ServletException, IOException {

        try {

            int roomId =
                    Integer.parseInt(
                            request.getParameter(
                                    "roomId"
                            )
                    );

            Room room =
                    roomService.getRoom(
                            roomId,
                            user.getCinemaId()
                    );

            if (room == null) {

                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND
                );

                return;
            }

            Seat seat = new Seat();

            String id =
                    request.getParameter("id");

            if (id != null
                    && !id.trim().isEmpty()) {

                try {

                    seat.setSeatId(
                            Integer.parseInt(id)
                    );

                } catch (NumberFormatException e) {

                    seat.setSeatId(0);
                }
            }

            seat.setRoomId(roomId);

            try {

                seat.setSeatRow(
                        Integer.parseInt(
                                request.getParameter(
                                        "seatRow"
                                )
                        )
                );

            } catch (NumberFormatException e) {

                seat.setSeatRow(0);
            }

            try {

                seat.setSeatNumber(
                        Integer.parseInt(
                                request.getParameter(
                                        "seatNumber"
                                )
                        )
                );

            } catch (NumberFormatException e) {

                seat.setSeatNumber(0);
            }

            seat.setSeatType(
                    request.getParameter(
                            "seatType"
                    )
            );

            seat.setStatus(
                    request.getParameter(
                            "status"
                    )
            );

            request.setAttribute(
                    "room",
                    room
            );

            request.setAttribute(
                    "seat",
                    seat
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/seat-form.jsp"
            ).forward(request, response);

        } catch (NumberFormatException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
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

        User user =
                (User) session.getAttribute(
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