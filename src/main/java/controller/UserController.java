package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import model.User;
import service.UserService;

@WebServlet(urlPatterns = {"/system/users", "/system/user-form"})
public class UserController extends HttpServlet {
    private final UserService service = new UserService();

    // Đọc lại quyền từ DB: tài khoản bị khóa không dùng tiếp session cũ được.
    private User requireAdmin(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("user") instanceof User)) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return null;
        }
        User login = (User) session.getAttribute("user");
        User current = service.findById(login.getUserId());
        if (!service.isActiveAdmin(current)) {
            response.sendError(403);
            return null;
        }
        current.setPasswordHash(null);
        session.setAttribute("user", current);
        session.setAttribute("roleName", current.getRoleName());
        if (session.getAttribute("userCsrfToken") == null)
            session.setAttribute("userCsrfToken", UUID.randomUUID().toString());
        return current;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            if (requireAdmin(request, response) == null) return;
            if ("/system/users".equals(request.getServletPath())) {
                showList(request, response);
            } else {
                User user = new User();
                user.setStatus("ACTIVE");
                if (request.getParameter("id") != null) {
                    user = service.findById(positiveId(request.getParameter("id")));
                    if (user == null) { response.sendError(404); return; }
                }
                showForm(request, response, user);
            }
        } catch (IllegalArgumentException ex) {
            response.sendError(400);
        } catch (SQLException ex) { databaseError(response, ex); }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User form = null;
        try {
            User admin = requireAdmin(request, response);
            if (admin == null) return;
            Object token = request.getSession().getAttribute("userCsrfToken");
            if (token == null || !token.equals(request.getParameter("csrfToken"))) {
                response.sendError(403); return;
            }
            if ("/system/users".equals(request.getServletPath())) {
                if (!"deactivate".equals(request.getParameter("action"))) { response.sendError(400); return; }
                service.deactivate(positiveId(request.getParameter("id")), admin.getUserId());
                request.getSession().setAttribute("userMessage", "Đã vô hiệu hóa tài khoản.");
            } else {
                form = new User();
                String id = request.getParameter("id");
                form.setUserId(id == null || id.isEmpty() ? 0 : positiveId(id));
                form.setFullName(request.getParameter("fullName"));
                form.setEmail(request.getParameter("email"));
                form.setPhone(request.getParameter("phone"));
                form.setStatus(request.getParameter("status"));
                form.setRoleId(positiveId(request.getParameter("roleId")));
                String cinema = request.getParameter("cinemaId");
                form.setCinemaId(cinema == null || cinema.isEmpty() ? null : positiveId(cinema));
                service.save(form, request.getParameter("password"), admin.getUserId());
                request.getSession().setAttribute("userMessage", "Đã lưu tài khoản.");
            }
            response.sendRedirect(request.getContextPath() + "/system/users");
        } catch (IllegalArgumentException ex) {
            showValidationError(request, response, form, ex.getMessage());
        } catch (SQLException ex) {
            // Unique constraint vẫn bảo vệ email khi hai admin cùng tạo một lúc.
            if (ex.getErrorCode() == 2601 || ex.getErrorCode() == 2627)
                showValidationError(request, response, form, "Email đã được sử dụng. Vui lòng kiểm tra lại.");
            else databaseError(response, ex);
        }
    }

    private int positiveId(String value) {
        try {
            int id = Integer.parseInt(value);
            if (id > 0) return id;
        } catch (NumberFormatException ex) { /* Thông báo chung bên dưới. */ }
        throw new IllegalArgumentException("Mã tài khoản, vai trò hoặc chi nhánh không hợp lệ.");
    }
    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        request.setAttribute("users", service.findAll());
        request.getRequestDispatcher("/WEB-INF/views/system/users.jsp").forward(request, response);
    }
    private void showForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws SQLException, ServletException, IOException {
        request.setAttribute("formUser", user);
        request.setAttribute("roles", service.findRoles());
        request.setAttribute("cinemas", service.findCinemas());
        request.getRequestDispatcher("/WEB-INF/views/system/user-form.jsp").forward(request, response);
    }
    private void showValidationError(HttpServletRequest request, HttpServletResponse response, User form, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        response.setStatus(400);
        try {
            if (form == null) showList(request, response);
            else showForm(request, response, form);
        } catch (SQLException ex) { databaseError(response, ex); }
    }
    private void databaseError(HttpServletResponse response, SQLException ex) throws IOException {
        log("User management database error", ex);
        response.sendError(500, "Không thể xử lý dữ liệu tài khoản lúc này.");
    }
}
