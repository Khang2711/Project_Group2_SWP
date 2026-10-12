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

    // =========================================================
    // GET
    // =========================================================
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user =
                getManager(
                        request,
                        response
                );

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
                roomService
                        .getRoom(
                                roomId,
                                user.getCinemaId()
                        );

        if (room == null) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy phòng."
            );

            return;
        }

        String path =
                request.getServletPath();

        // -----------------------------------------------------
        // SƠ ĐỒ GHẾ
        // -----------------------------------------------------
        if ("/admin/seats".equals(path)) {

            loadSeatMap(
                    request,
                    room,
                    user
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/seats.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }

        // -----------------------------------------------------
        // FORM SỬA CHI TIẾT 1 GHẾ
        // -----------------------------------------------------
        if ("/admin/seat-form".equals(path)) {

            String id =
                    request.getParameter(
                            "id"
                    );

            if (id != null
                    && !id.trim().isEmpty()) {

                try {

                    int seatId =
                            Integer.parseInt(
                                    id
                            );

                    Seat seat =
                            seatService
                                    .getSeat(
                                            seatId,
                                            roomId,
                                            user.getCinemaId()
                                    );

                    if (seat == null) {

                        response.sendError(
                                HttpServletResponse.SC_NOT_FOUND,
                                "Không tìm thấy ghế."
                        );

                        return;
                    }

                    request.setAttribute(
                            "seat",
                            seat
                    );

                } catch (NumberFormatException e) {

                    response.sendError(
                            HttpServletResponse.SC_BAD_REQUEST,
                            "Mã ghế không hợp lệ."
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

    // =========================================================
    // POST
    // =========================================================
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user =
                getManager(
                        request,
                        response
                );

        if (user == null) {
            return;
        }

        String path =
                request.getServletPath();

        // -----------------------------------------------------
        // BULK UPDATE NHIỀU GHẾ
        // -----------------------------------------------------
        if ("/admin/seats".equals(path)) {

            String action =
                    request.getParameter(
                            "action"
                    );

            if (!"bulkUpdate".equals(action)) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST
                );

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

            try {

                List<Integer> seatIds =
                        parseSeatIds(
                                request.getParameterValues(
                                        "seatId"
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

                seatService.bulkUpdateSeats(
                        seatIds,
                        roomId,
                        user.getCinemaId(),
                        seatType,
                        status
                );

                response.sendRedirect(
                        request.getContextPath()
                        + "/admin/seats?roomId="
                        + roomId
                        + "&updated=1"
                );

            } catch (IllegalArgumentException e) {

                Room room =
                        roomService
                                .getRoom(
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
                        e.getMessage()
                );

                loadSeatMap(
                        request,
                        room,
                        user
                );

                request.getRequestDispatcher(
                        "/WEB-INF/views/admin/seats.jsp"
                ).forward(
                        request,
                        response
                );
            }

            return;
        }

        // -----------------------------------------------------
        // FORM CŨ: THÊM / SỬA 1 GHẾ
        // Giữ lại để sửa chi tiết vị trí.
        // -----------------------------------------------------
        if (!"/admin/seat-form".equals(path)) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }

        String id =
                request.getParameter(
                        "id"
                );

        String roomIdText =
                request.getParameter(
                        "roomId"
                );

        String rowText =
                request.getParameter(
                        "seatRow"
                );

        String numberText =
                request.getParameter(
                        "seatNumber"
                );

        String seatType =
                request.getParameter(
                        "seatType"
                );

        String status =
                request.getParameter(
                        "status"
                );

        try {

            int roomId =
                    Integer.parseInt(
                            roomIdText
                    );

            int seatRow =
                    Integer.parseInt(
                            rowText
                    );

            int seatNumber =
                    Integer.parseInt(
                            numberText
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
                        Integer.parseInt(
                                id
                        );

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

    // =========================================================
    // LOAD SƠ ĐỒ GHẾ
    // =========================================================
    private void loadSeatMap(
            HttpServletRequest request,
            Room room,
            User user) {

        request.setAttribute(
                "room",
                room
        );

        request.setAttribute(
                "seatRows",
                seatService
                        .getSeatRowsByRoom(
                                room.getRoomId(),
                                user.getCinemaId()
                        )
        );
    }

    // =========================================================
    // PARSE CHECKBOX SEAT IDs
    // =========================================================
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
                        Integer.parseInt(
                                value
                        );

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

    // =========================================================
    // LOAD LẠI FORM CHI TIẾT
    // =========================================================
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
                    roomService
                            .getRoom(
                                    roomId,
                                    user.getCinemaId()
                            );

            if (room == null) {

                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND
                );

                return;
            }

            Seat seat =
                    new Seat();

            String id =
                    request.getParameter(
                            "id"
                    );

            if (id != null
                    && !id.trim().isEmpty()) {

                try {

                    seat.setSeatId(
                            Integer.parseInt(
                                    id
                            )
                    );

                } catch (NumberFormatException e) {

                    seat.setSeatId(0);
                }
            }

            seat.setRoomId(
                    roomId
            );

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
            ).forward(
                    request,
                    response
            );

        } catch (NumberFormatException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Mã phòng không hợp lệ."
            );
        }
    }

    // =========================================================
    // CHECK CINEMA MANAGER
    // =========================================================
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