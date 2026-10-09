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

        HttpServletRequest request =
                (HttpServletRequest) servletRequest;

        HttpServletResponse response =
                (HttpServletResponse) servletResponse;

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
            response.sendRedirect(
                    request.getContextPath() + "/auth/login"
            );
            return;
        }

        String roleName = (String) session.getAttribute("roleName");

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
