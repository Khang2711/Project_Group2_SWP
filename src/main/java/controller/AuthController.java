package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import model.User;
import service.AuthService;

@WebServlet(urlPatterns = {
    "/auth/login",
    "/auth/logout",
    "/auth/register",
    "/auth/forgot-password",
    "/auth/reset-password"
})
public class AuthController extends HttpServlet {

    private AuthService authService;

    @Override
    public void init() {
        authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");

        String path = request.getServletPath();

        switch (path) {
            case "/auth/login":
                if (redirectLoggedInUser(request, response)) {
                    return;
                }

                showLogin(request, response);
                break;

            case "/auth/logout":
                logout(request, response);
                break;

            case "/auth/register":
                request.getRequestDispatcher(
                        "/WEB-INF/views/auth/register.jsp"
                ).forward(request, response);
                break;

            case "/auth/forgot-password":
                request.getRequestDispatcher(
                        "/WEB-INF/views/auth/forgot-password.jsp"
                ).forward(request, response);
                break;

            case "/auth/reset-password":
                request.getRequestDispatcher(
                        "/WEB-INF/views/auth/reset-password.jsp"
                ).forward(request, response);
                break;

            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        String path = request.getServletPath();

        switch (path) {
            case "/auth/login":
                if (redirectLoggedInUser(request, response)) {
                    return;
                }

                login(request, response);
                break;

            case "/auth/logout":
                logout(request, response);
                break;

            default:
                response.sendError(
                        HttpServletResponse.SC_METHOD_NOT_ALLOWED
                );
        }
    }

    private void login(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        request.setAttribute("email", email == null ? "" : email.trim());

        try {
            User user = authService.login(email, password);

            String roleName = authService.getRoleName(user.getRoleId());
            String homePath = authService.getHomePath(roleName);

            HttpSession oldSession = request.getSession(false);

            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession session = request.getSession(true);

            session.setAttribute("user", user);
            session.setAttribute("roleName", roleName);
            session.setMaxInactiveInterval(30 * 60);

            response.sendRedirect(
                    request.getContextPath() + homePath
            );

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            showLogin(request, response);

        } catch (SQLException e) {
            log("Cannot process login.", e);

            request.setAttribute(
                    "error",
                    "Không thể đăng nhập lúc này. "
                    + "Vui lòng kiểm tra kết nối database."
            );

            showLogin(request, response);
        }
    }

    private boolean redirectLoggedInUser(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            return false;
        }

        String roleName = (String) session.getAttribute("roleName");
        String homePath = authService.getHomePath(roleName);

        if (homePath == null) {
            session.invalidate();
            return false;
        }

        response.sendRedirect(request.getContextPath() + homePath);
        return true;
    }

    private void logout(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(
                request.getContextPath() + "/auth/login?logout=1"
        );
    }

    private void showLogin(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/WEB-INF/views/auth/login.jsp"
        ).forward(request, response);
    }
}