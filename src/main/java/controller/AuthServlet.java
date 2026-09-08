package controller;

import dao.UserDAO;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import utils.PasswordUtils;

@WebServlet(name = "AuthServlet", urlPatterns = {"/auth", "/profile"})
public class AuthServlet extends HttpServlet {

    private static final String LOGIN_PAGE = "/WEB-INF/views/auth/login.jsp";
    private static final String SIGNUP_PAGE = "/WEB-INF/views/auth/signup.jsp";
    private static final String PROFILE_PAGE = "/WEB-INF/views/user/profile.jsp";
    private static final int MIN_PASSWORD_LENGTH = 6;

    private User getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("LOGIN_USER");
    }

    private String getCookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return contextPath == null || contextPath.isEmpty() ? "/" : contextPath;
    }

    /**
     * Cookie chỉ dùng để hiển thị lời chào trên header.
     * Họ tên được URL encode để lưu đúng dấu tiếng Việt.
     */
    private void saveDisplayNameCookie(HttpServletRequest request,
            HttpServletResponse response, String fullName) {

        String safeName = fullName == null ? "" : fullName.trim();
        String encodedName = URLEncoder.encode(safeName, StandardCharsets.UTF_8);
        Cookie cookie = new Cookie("DISPLAY_NAME", encodedName);
        cookie.setMaxAge(60 * 60 * 24 * 7);
        cookie.setPath(getCookiePath(request));
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private void deleteDisplayNameCookie(HttpServletRequest request,
            HttpServletResponse response) {

        Cookie cookie = new Cookie("DISPLAY_NAME", "");
        cookie.setMaxAge(0);
        cookie.setPath(getCookiePath(request));
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private void loadLastUser(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("LAST_USER".equals(c.getName())) {
                    request.setAttribute("lastUser", c.getValue());
                    return;
                }
            }
        }
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/profile".equals(path)) {
            User user = getLoginUser(request);
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/auth");
                return;
            }
            request.setAttribute("user", user);
            request.getRequestDispatcher(PROFILE_PAGE).forward(request, response);
            return;
        }

        String action = request.getParameter("action");

        if ("logout".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            deleteDisplayNameCookie(request, response);
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        if ("signup".equals(action)) {
            request.getRequestDispatcher(SIGNUP_PAGE).forward(request, response);
            return;
        }

        loadLastUser(request);
        request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        if ("/profile".equals(path)) {
            updateProfile(request, response);
            return;
        }

        String action = request.getParameter("action");

        if ("signup".equals(action)) {
            signup(request, response);
            return;
        }

        if (!"signin".equals(action)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        signin(request, response);
    }

    private void signin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userID = request.getParameter("userID");
        String password = request.getParameter("password");

        if (!isValidPassword(password)) {
            request.setAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
            request.setAttribute("lastUser", userID);
            request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);
            return;
        }

        try {
            UserDAO dao = new UserDAO();
            User user = dao.checkLogin(userID, PasswordUtils.md5(password));

            if (user != null) {
                HttpSession session = request.getSession();
                session.setAttribute("LOGIN_USER", user);

                Cookie cookie = new Cookie("LAST_USER", user.getUserID());
                cookie.setMaxAge(60 * 60 * 24 * 7);
                cookie.setPath(getCookiePath(request));
                response.addCookie(cookie);

                // Header sẽ hiện "Hi, [họ tên]", không hiện username.
                saveDisplayNameCookie(request, response, user.getFullName());

                if ("AD".equals(user.getRoleID())) {
                    response.sendRedirect(request.getContextPath() + "/admin/movies");
                } else {
                    response.sendRedirect(request.getContextPath() + "/movies");
                }
            } else {
                request.setAttribute("error", "Sai tài khoản hoặc mật khẩu.");
                request.setAttribute("lastUser", userID);
                request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);
            }
        } catch (Exception e) {
            request.setAttribute("error", "Không thể kết nối database.");
            request.setAttribute("lastUser", userID);
            request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);
        }
    }

    private void signup(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userID = request.getParameter("userID");
        String fullName = request.getParameter("fullName");
        String password = request.getParameter("password");

        userID = userID == null ? "" : userID.trim();
        fullName = fullName == null ? "" : fullName.trim();

        if (userID.isEmpty() || fullName.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ tài khoản và họ tên.");
            request.getRequestDispatcher(SIGNUP_PAGE).forward(request, response);
            return;
        }

        if (!isValidPassword(password)) {
            request.setAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
            request.getRequestDispatcher(SIGNUP_PAGE).forward(request, response);
            return;
        }

        try {
            UserDAO dao = new UserDAO();

            if (dao.isUserIDExists(userID)) {
                request.setAttribute("error", "Tài khoản đã tồn tại.");
                request.getRequestDispatcher(SIGNUP_PAGE).forward(request, response);
                return;
            }

            User user = new User(userID, fullName, PasswordUtils.md5(password), "US");
            dao.createUser(user);
            response.sendRedirect(request.getContextPath() + "/auth");
        } catch (Exception e) {
            request.setAttribute("error", "Không thể đăng ký tài khoản.");
            request.getRequestDispatcher(SIGNUP_PAGE).forward(request, response);
        }
    }

    private void updateProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        String fullName = request.getParameter("fullName");
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        fullName = fullName == null ? "" : fullName.trim();
        boolean changePassword = newPassword != null && !newPassword.trim().isEmpty();

        if (fullName.isEmpty()) {
            request.setAttribute("user", loginUser);
            request.setAttribute("error", "Họ tên không được để trống.");
            request.getRequestDispatcher(PROFILE_PAGE).forward(request, response);
            return;
        }

        if (changePassword && !isValidPassword(newPassword)) {
            request.setAttribute("user", loginUser);
            request.setAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự.");
            request.getRequestDispatcher(PROFILE_PAGE).forward(request, response);
            return;
        }

        try {
            UserDAO dao = new UserDAO();

            if (changePassword) {
                if (currentPassword == null || currentPassword.trim().isEmpty()) {
                    request.setAttribute("user", loginUser);
                    request.setAttribute("error", "Vui lòng nhập mật khẩu hiện tại để đổi mật khẩu.");
                    request.getRequestDispatcher(PROFILE_PAGE).forward(request, response);
                    return;
                }

                User verifiedUser = dao.checkLogin(
                        loginUser.getUserID(), PasswordUtils.md5(currentPassword));
                if (verifiedUser == null) {
                    request.setAttribute("user", loginUser);
                    request.setAttribute("error", "Mật khẩu hiện tại không đúng.");
                    request.getRequestDispatcher(PROFILE_PAGE).forward(request, response);
                    return;
                }
            }

            dao.updateProfile(loginUser.getUserID(), fullName);

            if (changePassword) {
                dao.updatePassword(loginUser.getUserID(), PasswordUtils.md5(newPassword));
            }

            User updatedUser = dao.getUserByID(loginUser.getUserID());
            request.getSession().setAttribute("LOGIN_USER", updatedUser);
            saveDisplayNameCookie(request, response, updatedUser.getFullName());
            request.setAttribute("user", updatedUser);
            request.setAttribute("success", "Cập nhật thành công.");
            request.getRequestDispatcher(PROFILE_PAGE).forward(request, response);
        } catch (Exception e) {
            request.setAttribute("user", loginUser);
            request.setAttribute("error", "Không thể cập nhật thông tin.");
            request.getRequestDispatcher(PROFILE_PAGE).forward(request, response);
        }
    }
}
