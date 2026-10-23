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

        String path =
                request.getServletPath();

        if ("/admin/rooms".equals(path)) {

            request.setAttribute(
                    "rooms",
                    roomService.getRoomsByCinema(
                            user.getCinemaId()
                    )
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/rooms.jsp"
            ).forward(request, response);

            return;
        }

        if ("/admin/room-form".equals(path)) {

            String id =
                    request.getParameter("id");

            if (id != null
                    && !id.trim().isEmpty()) {

                try {

                    int roomId =
                            Integer.parseInt(id);

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
                            "room",
                            room
                    );

                } catch (NumberFormatException e) {

                    response.sendError(
                            HttpServletResponse.SC_BAD_REQUEST
                    );

                    return;
                }

            } else {

                // Giá trị mặc định khi tạo phòng.
                request.setAttribute(
                        "rowCount",
                        5
                );

                request.setAttribute(
                        "seatsPerRow",
                        8
                );
            }

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/room-form.jsp"
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
        // DELETE ROOM
        // =====================================================
        if ("/admin/rooms".equals(path)) {

            String action =
                    request.getParameter("action");

            if (!"delete".equals(action)) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                return;
            }

            try {

                int roomId =
                        Integer.parseInt(
                                request.getParameter("id")
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
                        roomService.getRoomsByCinema(
                                user.getCinemaId()
                        )
                );

                request.getRequestDispatcher(
                        "/WEB-INF/views/admin/rooms.jsp"
                ).forward(request, response);
            }

            return;
        }

        if (!"/admin/room-form".equals(path)) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }

        String id =
                request.getParameter("id");

        String roomName =
                request.getParameter("roomName");

        String roomType =
                request.getParameter("roomType");

        String status =
                request.getParameter("status");

        try {

            // =================================================
            // CREATE ROOM
            // =================================================
            if (id == null
                    || id.trim().isEmpty()) {

                int rowCount =
                        Integer.parseInt(
                                request.getParameter(
                                        "rowCount"
                                )
                        );

                int seatsPerRow =
                        Integer.parseInt(
                                request.getParameter(
                                        "seatsPerRow"
                                )
                        );

                roomService.createRoom(
                        user.getCinemaId(),
                        roomName,
                        roomType,
                        rowCount,
                        seatsPerRow,
                        status
                );

            } else {

                // =================================================
                // UPDATE ROOM
                // =================================================
                int roomId =
                        Integer.parseInt(id);

                roomService.updateRoom(
                        roomId,
                        user.getCinemaId(),
                        roomName,
                        roomType,
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
                    "Số hàng, số ghế hoặc mã phòng không hợp lệ."
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

    private void forwardForm(
            HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws ServletException, IOException {

        String id =
                request.getParameter("id");

        Room room =
                new Room();

        if (id != null
                && !id.trim().isEmpty()) {

            try {

                int roomId =
                        Integer.parseInt(id);

                Room currentRoom =
                        roomService.getRoom(
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
                }

            } catch (NumberFormatException e) {

                room.setRoomId(0);
            }

        } else {

            room.setCinemaId(
                    user.getCinemaId()
            );

            try {

                request.setAttribute(
                        "rowCount",
                        Integer.parseInt(
                                request.getParameter(
                                        "rowCount"
                                )
                        )
                );

            } catch (NumberFormatException e) {

                request.setAttribute(
                        "rowCount",
                        5
                );
            }

            try {

                request.setAttribute(
                        "seatsPerRow",
                        Integer.parseInt(
                                request.getParameter(
                                        "seatsPerRow"
                                )
                        )
                );

            } catch (NumberFormatException e) {

                request.setAttribute(
                        "seatsPerRow",
                        8
                );
            }
        }

        room.setRoomName(
                request.getParameter("roomName")
        );

        room.setRoomType(
                request.getParameter("roomType")
        );

        room.setStatus(
                request.getParameter("status")
        );

        request.setAttribute(
                "room",
                room
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/room-form.jsp"
        ).forward(request, response);
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