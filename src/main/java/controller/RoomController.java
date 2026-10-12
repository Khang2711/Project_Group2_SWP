package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.Room;
import model.User;
import service.RoomService;

@WebServlet(urlPatterns = {
    "/admin/rooms",
    "/admin/room-form"
})
public class RoomController extends HttpServlet {

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

        String path =
                request.getServletPath();

        // -----------------------------------------------------
        // DANH SÁCH PHÒNG
        // -----------------------------------------------------
        if ("/admin/rooms".equals(path)) {

            request.setAttribute(
                    "rooms",
                    roomService
                            .getRoomsByCinema(
                                    user.getCinemaId()
                            )
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/rooms.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }

        // -----------------------------------------------------
        // FORM THÊM / SỬA
        // -----------------------------------------------------
        if ("/admin/room-form".equals(path)) {

            String id =
                    request.getParameter(
                            "id"
                    );

            if (id != null
                    && !id.trim().isEmpty()) {

                try {

                    int roomId =
                            Integer.parseInt(
                                    id
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

                    request.setAttribute(
                            "room",
                            room
                    );

                } catch (NumberFormatException e) {

                    response.sendError(
                            HttpServletResponse.SC_BAD_REQUEST
                    );

                    return;
                }
            }

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/room-form.jsp"
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
        // XÓA PHÒNG
        // -----------------------------------------------------
        if ("/admin/rooms".equals(path)) {

            String action =
                    request.getParameter(
                            "action"
                    );

            if (!"delete".equals(action)) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                return;
            }

            try {

                int roomId =
                        Integer.parseInt(
                                request.getParameter(
                                        "id"
                                )
                        );

                roomService.deleteRoom(
                        roomId,
                        user.getCinemaId()
                );

                response.sendRedirect(
                        request.getContextPath()
                        + "/admin/rooms?deleted=1"
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
                        "rooms",
                        roomService
                                .getRoomsByCinema(
                                        user.getCinemaId()
                                )
                );

                request.getRequestDispatcher(
                        "/WEB-INF/views/admin/rooms.jsp"
                ).forward(
                        request,
                        response
                );
            }

            return;
        }

        // -----------------------------------------------------
        // THÊM / SỬA PHÒNG
        // -----------------------------------------------------
        if (!"/admin/room-form".equals(path)) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }

        String id =
                request.getParameter(
                        "id"
                );

        String roomName =
                request.getParameter(
                        "roomName"
                );

        String roomType =
                request.getParameter(
                        "roomType"
                );

        String status =
                request.getParameter(
                        "status"
                );

        try {

            // -------------------------------------------------
            // TẠO PHÒNG
            // -------------------------------------------------
            if (id == null
                    || id.trim().isEmpty()) {

                roomService.createRoom(
                        user.getCinemaId(),
                        roomName,
                        roomType,
                        status
                );

            } else {

                // ---------------------------------------------
                // SỬA PHÒNG
                // ---------------------------------------------
                int roomId =
                        Integer.parseInt(
                                id
                        );

                roomService.updateRoom(
                        roomId,
                        user.getCinemaId(),
                        roomName,
                        status
                );
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/admin/rooms?saved=1"
            );

        } catch (NumberFormatException e) {

            request.setAttribute(
                    "error",
                    "Mã phòng không hợp lệ."
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
    // LOAD LẠI FORM KHI LỖI
    // =========================================================
    private void forwardForm(
            HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws ServletException, IOException {

        String id =
                request.getParameter(
                        "id"
                );

        Room room =
                new Room();

        // -----------------------------------------------------
        // ĐANG SỬA
        // -----------------------------------------------------
        if (id != null
                && !id.trim().isEmpty()) {

            try {

                int roomId =
                        Integer.parseInt(id);

                Room currentRoom =
                        roomService
                                .getRoom(
                                        roomId,
                                        user.getCinemaId()
                                );

                if (currentRoom != null) {

                    room.setRoomId(
                            currentRoom.getRoomId()
                    );

                    room.setCinemaId(
                            currentRoom.getCinemaId()
                    );

                    room.setCapacity(
                            currentRoom.getCapacity()
                    );

                    room.setRoomType(
                            currentRoom.getRoomType()
                    );
                }

            } catch (NumberFormatException e) {

                room.setRoomId(0);
            }

        } else {

            // -------------------------------------------------
            // ĐANG TẠO
            // -------------------------------------------------
            room.setCinemaId(
                    user.getCinemaId()
            );

            String roomType =
                    request.getParameter(
                            "roomType"
                    );

            room.setRoomType(
                    roomType
            );

            try {

                room.setCapacity(
                        roomService
                                .getCapacityByRoomType(
                                        roomType
                                )
                );

            } catch (IllegalArgumentException e) {

                room.setCapacity(0);
            }
        }

        room.setRoomName(
                request.getParameter(
                        "roomName"
                )
        );

        room.setStatus(
                request.getParameter(
                        "status"
                )
        );

        request.setAttribute(
                "room",
                room
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/room-form.jsp"
        ).forward(
                request,
                response
        );
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