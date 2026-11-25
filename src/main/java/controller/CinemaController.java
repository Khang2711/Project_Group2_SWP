package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.Cinema;
import model.User;
import service.CinemaService;

@WebServlet(urlPatterns = {
    "/admin/cinemas",
    "/admin/cinema-form"
})
public class CinemaController extends HttpServlet {

    private final CinemaService cinemaService = new CinemaService();

    @Override
    /** Hiển thị thông tin và form chỉnh sửa rạp. */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user = getManager(request, response);

        if (user == null) {
            return;
        }

        int cinemaId = user.getCinemaId();
        //Phân luồng theo URL
        switch (request.getServletPath()) {
            //rường hợp xem thông tin rạp
            case "/admin/cinemas":
                showCinema(request, response, cinemaId);
                break;
            //Trường hợp mở form sửa rạp
            case "/admin/cinema-form":
                showForm(request, response, cinemaId);
                break;
            //Trường hợp URL không hợp lệ
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    /** Nhận yêu cầu sửa địa chỉ và số điện thoại rạp. */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        //Kiểm tra Manager
        User user = getManager(request, response);

        if (user == null) {
            return;
        }
        //Kiểm tra URL nhận POST
        if (!"/admin/cinema-form".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //Lấy thông tin từ form
        String address = request.getParameter("address");
        String phone = request.getParameter("phone");
        //Gọi Service cập nhật
        try {
            cinemaService.updateCinemaContact(
                    user.getCinemaId(),
                    address,
                    phone
            );
            //Chuyển trang khi thành công
            response.sendRedirect(
                    request.getContextPath()
                    + "/admin/cinemas?updated=1"
            );
            //Xử lý khi sửa thất bại
        } catch (IllegalArgumentException e) {

            request.setAttribute("error", e.getMessage());

            Cinema cinema = cinemaService.getCinema(
                    user.getCinemaId()
            );

            request.setAttribute("cinema", cinema);

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/cinema-form.jsp"
            ).forward(request, response);
        }
    }

    /** Tải thông tin rạp để hiển thị. */
    private void showCinema(HttpServletRequest request,
            HttpServletResponse response,
            int cinemaId)
            throws ServletException, IOException {
        //Lấy thông tin rạp
        Cinema cinema = cinemaService.getCinema(cinemaId);

        if (cinema == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy rạp."
            );
            return;
        }

        request.setAttribute("cinema", cinema);
        request.setAttribute(
                "roomCount",
                cinemaService.getRoomCount(cinemaId)
        );
        //Chuyển dữ liệu sang cinemas.jsp để hiển thị.
        request.getRequestDispatcher(
                "/WEB-INF/views/admin/cinemas.jsp"
        ).forward(request, response);
    }

    /** Mở form sửa liên hệ rạp. */
    private void showForm(HttpServletRequest request,
            HttpServletResponse response,
            int cinemaId)
            throws ServletException, IOException {
        //Lấy thông tin rạp hiện tại.
        Cinema cinema = cinemaService.getCinema(cinemaId);

        if (cinema == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy rạp."
            );
            return;
        }
        //Gửi thông tin rạp sang form để hiển thị dữ liệu hiện tại.
        request.setAttribute("cinema", cinema);
        //Hiển thị giao diện sửa rạp.
        request.getRequestDispatcher(
                "/WEB-INF/views/admin/cinema-form.jsp"
        ).forward(request, response);
    }

    /** Kiểm tra tài khoản quản lý rạp. */
    private User getManager(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        //Lấy session
        HttpSession session = request.getSession(false);
        //Nếu không có session hoặc không có user trong session, chuyển về trang đăng nhập.
        if (session == null
                || session.getAttribute("user") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/auth/login"
            );

            return null;
        }

        String roleName =
                (String) session.getAttribute("roleName");

        if (!"CINEMA_MANAGER".equals(roleName)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return null;
        }
        //Lấy đối tượng User đã lưu trong session.
        User user = (User) session.getAttribute("user");
        //Nếu cinemaId bằng null, tài khoản chưa có rạp để quản lý nên bị từ chối.
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