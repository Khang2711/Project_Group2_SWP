package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Cinema;
import model.Role;
import model.User;
import service.UserService;

@WebServlet(urlPatterns = {
    "/system/users",
    "/system/user-form"
})
public class UserController extends HttpServlet {

    private UserService userService;

    @Override
    public void init() {
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        try {
            if ("/system/users".equals(path)) {
                showUsers(request, response);
                return;
            }

            if ("/system/user-form".equals(path)) {
                showForm(request, response);
                return;
            }

            response.sendError(HttpServletResponse.SC_NOT_FOUND);

        } catch (SQLException e) {
            log("Cannot load account management data.", e);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể tải dữ liệu tài khoản."
            );
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();

        try {
            if ("/system/user-form".equals(path)) {
                saveUser(request, response);
                return;
            }

            if ("/system/users".equals(path)) {
                deactivateUser(request, response);
                return;
            }

            response.sendError(HttpServletResponse.SC_NOT_FOUND);

        } catch (SQLException e) {
            log("Cannot save account management data.", e);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể xử lý tài khoản lúc này."
            );
        }
    }

    private void showUsers(HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        List<User> users = userService.getAllUsers();
        List<Role> roles = userService.getActiveRoles();
        List<Cinema> cinemas = userService.getActiveCinemas();

        Map<Integer, String> roleNames = new LinkedHashMap<>();
        for (Role role : roles) {
            roleNames.put(role.getRoleId(), role.getRoleName());
        }

        Map<Integer, String> cinemaNames = new LinkedHashMap<>();
        for (Cinema cinema : cinemas) {
            cinemaNames.put(
                    cinema.getCinemaId(),
                    cinema.getCinemaName()
            );
        }

        request.setAttribute("users", users);
        request.setAttribute("roleNames", roleNames);
        request.setAttribute("cinemaNames", cinemaNames);

        request.getRequestDispatcher(
                "/WEB-INF/views/system/users.jsp"
        ).forward(request, response);
    }

    private void showForm(HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String idText = request.getParameter("id");

        if (idText != null && !idText.trim().isEmpty()) {
            try {
                int userId = Integer.parseInt(idText);
                User user = userService.getUserById(userId);

                if (user == null) {
                    response.sendError(
                            HttpServletResponse.SC_NOT_FOUND
                    );
                    return;
                }

                request.setAttribute("user", user);

            } catch (NumberFormatException e) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST
                );
                return;
            }
        }

        loadFormData(request);

        request.getRequestDispatcher(
                "/WEB-INF/views/system/user-form.jsp"
        ).forward(request, response);
    }

    private void saveUser(HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        User user = new User();

        try {
            String idText = request.getParameter("userId");

            if (idText != null && !idText.trim().isEmpty()) {
                user.setUserId(Integer.parseInt(idText));
            }

            user.setFullName(request.getParameter("fullName"));
            user.setEmail(request.getParameter("email"));
            user.setPhone(request.getParameter("phone"));

            int roleId = Integer.parseInt(
                    request.getParameter("roleId")
            );
            user.setRoleId(roleId);

            Integer cinemaId = parseCinemaId(
                    request.getParameter("cinemaId")
            );

            String password = request.getParameter("password");

            boolean creating = user.getUserId() == 0;

            userService.saveUser(user, password, cinemaId);

            String result = creating ? "created=1" : "updated=1";

            response.sendRedirect(
                    request.getContextPath()
                    + "/system/users?"
                    + result
            );

        } catch (NumberFormatException e) {
            request.setAttribute(
                    "error",
                    "Mã vai trò hoặc mã rạp không hợp lệ."
            );
            request.setAttribute("user", user);
            loadFormData(request);

            request.getRequestDispatcher(
                    "/WEB-INF/views/system/user-form.jsp"
            ).forward(request, response);

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("user", user);
            loadFormData(request);

            request.getRequestDispatcher(
                    "/WEB-INF/views/system/user-form.jsp"
            ).forward(request, response);
        }
    }

    private void deactivateUser(HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String action = request.getParameter("action");

        if (!"deactivate".equals(action)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            int userId = Integer.parseInt(
                    request.getParameter("userId")
            );

            HttpSession session = request.getSession(false);
            User currentUser =
                    (User) session.getAttribute("user");

            userService.deactivateUser(
                    userId,
                    currentUser.getUserId()
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/system/users?deactivated=1"
            );

        } catch (NumberFormatException e) {
            request.setAttribute(
                    "error",
                    "Mã tài khoản không hợp lệ."
            );
            showUsers(request, response);

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            showUsers(request, response);
        }
    }

    private Integer parseCinemaId(String cinemaIdText) {
        if (cinemaIdText == null
                || cinemaIdText.trim().isEmpty()) {
            return null;
        }

        return Integer.valueOf(cinemaIdText);
    }

    private void loadFormData(HttpServletRequest request)
            throws SQLException {

        request.setAttribute(
                "roles",
                userService.getActiveRoles()
        );

        request.setAttribute(
                "cinemas",
                userService.getActiveCinemas()
        );
    }
}
