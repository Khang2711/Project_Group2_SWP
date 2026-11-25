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
    /** Hiển thị danh sách hoặc form phòng chiếu. */
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        //Kiểm tra Manager
        User user =
                getManager(request, response);

        if (user == null) {
            return;
        }
        //Lấy URL hiện tại, ví dụ /admin/rooms.
        String path =
                request.getServletPath();
        //Xem danh sách phòng
        if ("/admin/rooms".equals(path)) {

            request.setAttribute(
                    "rooms",
                    roomService.getRoomsByCinema(
                            user.getCinemaId()
                    )
            );
            //Chọn trang JSP.
            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/rooms.jsp"
            ).forward(request, response);

            return;
        }
        //Mở form thêm/sửa
        if ("/admin/room-form".equals(path)) {

            String id =
                    request.getParameter("id");
            //Trường hợp sửa phòng
            if (id != null
                    && !id.trim().isEmpty()) {

                try {

                    int roomId =
                            Integer.parseInt(id);
                    //Gọi Service lấy phòng theo cả roomId và cinemaId của Manager.
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
                    //Đưa thông tin phòng hiện tại sang form để hiển thị dữ liệu cần sửa.
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
                //Trường hợp tạo phòng
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
        //Nếu URL không khớp hai đường dẫn được xử lý thì trả lỗi 404.
        response.sendError(
                HttpServletResponse.SC_NOT_FOUND
        );
    }

    @Override
    /** Xử lý thêm, sửa và xóa phòng. */
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
            //đọc tham số action.
            String action =
                    request.getParameter("action");
            //Kiểm tra có phải yêu cầu xóa không.
            //Không phải xóa thì từ chối.
            if (!"delete".equals(action)) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                return;
            }

            try {
                //Lấy ID phòng cần xóa từ form và chuyển thành số.
                int roomId =
                        Integer.parseInt(
                                request.getParameter("id")
                        );

                roomService.deleteRoom(
                        roomId,
                        user.getCinemaId()
                );
                //Nếu thành công, quay lại danh sách phòng
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
                //Tải lại danh sách phòng của Manager.
                request.setAttribute(
                        "rooms",
                        roomService.getRoomsByCinema(
                                user.getCinemaId()
                        )
                );
                //Hiển thị lại trang danh sách kèm lỗi.
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
        //Lấy dữ liệu form
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
            //Thêm/sửa thành công thì quay về danh sách phòng.
            response.sendRedirect(
                    request.getContextPath()
                    + "/admin/rooms?saved=1"
            );
        //Nếu dữ liệu số không hợp lệ thì gửi lỗi và mở lại form.
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
            //hiển thị lại form
            forwardForm(
                    request,
                    response,
                    user
            );
        }
    }

    /** Mở lại form phòng khi dữ liệu sai. */
    private void forwardForm(
            HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws ServletException, IOException {
        //Lấy ID và tạo đối tượng phòng
        //Tạo đối tượng phòng tạm để hiển thị lại form, không phải tạo bản ghi trong database.
        String id =
                request.getParameter("id");

        Room room =
                new Room();
        //Nếu đang sửa phòng
        if (id != null
                && !id.trim().isEmpty()) {

            try {

                int roomId =
                        Integer.parseInt(id);
                //Lấy phòng hiện tại thuộc rạp của Manager.
                Room currentRoom =
                        roomService.getRoom(
                                roomId,
                                user.getCinemaId()
                        );
                //Giữ lại các thông tin quan trọng của phòng
                //Điều này giúp form sửa phòng không làm mất thông tin cấu trúc phòng.
                if (currentRoom != null) {
                    //ID phòng.
                    room.setRoomId(
                            currentRoom.getRoomId()
                    );
                    //Rạp sở hữu phòng.
                    room.setCinemaId(
                            currentRoom.getCinemaId()
                    );
                    //Sức chứa hiện tại.
                    room.setCapacity(
                            currentRoom.getCapacity()
                    );
                }

            } catch (NumberFormatException e) {

                room.setRoomId(0);
            }

        } else {
            //Nếu đang thêm phòng
            //Gán phòng mới cho rạp của Manager
            room.setCinemaId(
                    user.getCinemaId()
            );

            try {
                //Giữ lại số hàng đã nhập.
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
        //Gán lại tên phòng, loại phòng và trạng thái người dùng vừa nhập.
        room.setRoomName(
                request.getParameter("roomName")
        );

        room.setRoomType(
                request.getParameter("roomType")
        );

        room.setStatus(
                request.getParameter("status")
        );
        //Mở lại form
        //Gửi đối tượng phòng tạm sang JSP.
        request.setAttribute(
                "room",
                room
        );
        //Hiển thị lại form, để Manager có thể sửa những giá trị bị lỗi.
        request.getRequestDispatcher(
                "/WEB-INF/views/admin/room-form.jsp"
        ).forward(request, response);
    }

    /** Kiểm tra quyền và rạp của quản lý. */
    private User getManager(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        //Nếu chưa đăng nhập, chuyển về /auth/login.
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
        //Nếu không phải CINEMA_MANAGER, trả lỗi HTTP 403.
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
        //Nếu Manager chưa có rạp được phân công thì không cho quản lý phòng.
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