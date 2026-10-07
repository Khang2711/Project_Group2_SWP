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
                updateAccountStatus(request, response);
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
                User managedUser = userService.getUserById(userId);

                if (managedUser == null) {
                    response.sendError(
                            HttpServletResponse.SC_NOT_FOUND
                    );
                    return;
                }

                if (!userService.canEditAccount(managedUser)) {
                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "Chỉ được chỉnh sửa tài khoản STAFF hoặc CINEMA_MANAGER."
                    );
                    return;
                }

                request.setAttribute("managedUser", managedUser);

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

        String idText = request.getParameter("userId");

        try {
            int roleId = Integer.parseInt(
                    request.getParameter("roleId")
            );

            Integer cinemaId = parseCinemaId(
                    request.getParameter("cinemaId")
            );

            if (idText == null || idText.trim().isEmpty()) {
                createUser(request, response, roleId, cinemaId);
                return;
            }

            int userId = Integer.parseInt(idText);

            userService.updateRoleAndCinema(
                    userId,
                    roleId,
                    cinemaId
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/system/users?updated=1"
            );

        } catch (NumberFormatException e) {
            forwardFormError(
                    request,
                    response,
                    "Mã tài khoản, vai trò hoặc rạp không hợp lệ."
            );

        } catch (IllegalArgumentException e) {
            forwardFormError(
                    request,
                    response,
                    e.getMessage()
            );
        }
    }

    private void createUser(HttpServletRequest request,
            HttpServletResponse response,
            int roleId,
            Integer cinemaId)
            throws SQLException, IOException,
            ServletException {

        User managedUser = new User();

        managedUser.setFullName(
                request.getParameter("fullName")
        );
        managedUser.setEmail(
                request.getParameter("email")
        );
        managedUser.setPhone(
                request.getParameter("phone")
        );
        managedUser.setRoleId(roleId);
        managedUser.setCinemaId(cinemaId);

        String password =
                request.getParameter("password");

        userService.createUser(
                managedUser,
                password,
                cinemaId
        );

        response.sendRedirect(
                request.getContextPath()
                + "/system/users?created=1"
        );
    }

    private void forwardFormError(HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws SQLException, ServletException, IOException {

        request.setAttribute("error", message);

        String idText = request.getParameter("userId");

        if (idText != null && !idText.trim().isEmpty()) {
            try {
                int userId = Integer.parseInt(idText);
                User managedUser =
                        userService.getUserById(userId);

                request.setAttribute(
                        "managedUser",
                        managedUser
                );

            } catch (NumberFormatException e) {
                // Invalid id is already shown by the error message.
            }
        } else {
            User managedUser = new User();

            managedUser.setFullName(
                    request.getParameter("fullName")
            );
            managedUser.setEmail(
                    request.getParameter("email")
            );
            managedUser.setPhone(
                    request.getParameter("phone")
            );

            try {
                managedUser.setRoleId(
                        Integer.parseInt(
                                request.getParameter("roleId")
                        )
                );
            } catch (NumberFormatException e) {
                // Keep default role id.
            }

            managedUser.setCinemaId(
                    parseCinemaIdSafe(
                            request.getParameter("cinemaId")
                    )
            );

            request.setAttribute(
                    "managedUser",
                    managedUser
            );
        }

        loadFormData(request);

        request.getRequestDispatcher(
                "/WEB-INF/views/system/user-form.jsp"
        ).forward(request, response);
    }

    private void updateAccountStatus(HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String action = request.getParameter("action");

        try {
            int userId = Integer.parseInt(
                    request.getParameter("userId")
            );

            if ("deactivate".equals(action)) {
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
                return;
            }

            if ("activate".equals(action)) {
                userService.activateUser(userId);

                response.sendRedirect(
                        request.getContextPath()
                        + "/system/users?activated=1"
                );
                return;
            }

            response.sendError(HttpServletResponse.SC_BAD_REQUEST);

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

    private Integer parseCinemaIdSafe(String cinemaIdText) {
        try {
            return parseCinemaId(cinemaIdText);
        } catch (NumberFormatException e) {
            return null;
        }
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
