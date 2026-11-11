package security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import model.User;
import repository.RoleRepository;
import repository.UserRepository;
import model.Role;

@WebFilter(urlPatterns = {
    "/user/*",
    "/staff/*",
    "/admin/*",
    "/system/*"
})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request
                = (HttpServletRequest) servletRequest;

        HttpServletResponse response
                = (HttpServletResponse) servletResponse;

        String path = request.getServletPath();

        response.setHeader(
                "Cache-Control",
                "no-store, no-cache, must-revalidate"
        );
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        if (isPublicPage(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {

            // Save selected showtime before login
            if ("/user/select-seats".equals(path)
                    && "GET".equals(request.getMethod())) {

                String showtimeId = request.getParameter("showtimeId");

                if (showtimeId != null
                        && showtimeId.matches("[1-9][0-9]*")) {

                    request.getSession(true).setAttribute(
                            "redirectAfterLogin",
                            "/user/select-seats?showtimeId=" + showtimeId
                    );
                }
            }

            response.sendRedirect(
                    request.getContextPath() + "/auth/login"
            );
            return;
        }

        User loggedIn = (User) session.getAttribute("user");
        String roleName = (String) session.getAttribute("roleName");

        // Verify current database state on protected requests. The session
        // must not remain authorized after the account or role is disabled.
        try {
            User current = new UserRepository().findById(loggedIn.getUserId());
            Role currentRole = current == null ? null
                    : new RoleRepository().findById(current.getRoleId());

            if (current == null
                    || !"ACTIVE".equals(current.getStatus())
                    || !current.isEmailVerified()
                    || currentRole == null
                    || !"ACTIVE".equals(currentRole.getStatus())
                    || !currentRole.getRoleName().equals(roleName)
                    || loggedIn.getRoleId() != current.getRoleId()
                    || (loggedIn.getUpdatedAt() != null
                        && current.getUpdatedAt() != null
                        && current.getUpdatedAt().after(loggedIn.getUpdatedAt()))) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/auth/login");
                return;
            }
        } catch (SQLException e) {
            request.getServletContext().log("Could not validate active session.", e);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Không thể xác thực tài khoản lúc này.");
            return;
        }

        if (!hasPermission(path, roleName)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập chức năng này."
            );
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicPage(String path) {
        return "/user/movies".equals(path)
                || "/user/movie-detail".equals(path)
                || "/user/showtimes".equals(path);
    }

    private boolean hasPermission(String path, String roleName) {
        if (path.startsWith("/system/")) {
            return "ADMIN".equals(roleName);
        }

        if (path.startsWith("/admin/")) {
            return "CINEMA_MANAGER".equals(roleName);
        }

        if (path.startsWith("/staff/")) {
            return "STAFF".equals(roleName);
        }

        if (path.startsWith("/user/")) {
            return "CUSTOMER".equals(roleName);
        }

        return false;
    }
}
