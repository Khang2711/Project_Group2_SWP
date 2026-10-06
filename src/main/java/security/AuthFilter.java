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
import model.Role;
import repository.UserRepository;
import repository.RoleRepository;

@WebFilter(urlPatterns = {
    "/user/*",
    "/staff/*",
    "/admin/*",
    "/system/*"
})
public class AuthFilter implements Filter {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AuthFilter() {
        this(new UserRepository(), new RoleRepository());
    }

    // Cho phép kiểm tra filter bằng repository giả, không cần DB thật.
    AuthFilter(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

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

        if (isPublicPage(path)) {
            chain.doFilter(request, response);
            return;
        }

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession(false);

        if (session == null || !(session.getAttribute("user") instanceof User)) {
            response.sendRedirect(
                    request.getContextPath() + "/auth/login"
            );
            return;
        }

        // Đọc lại trạng thái/quyền để thay đổi của admin có hiệu lực với session cũ.
        String roleName;
        try {
            User loginUser = (User) session.getAttribute("user");
            User currentUser = userRepository.findById(loginUser.getUserId());
            Role role = currentUser == null ? null
                    : roleRepository.findById(currentUser.getRoleId());
            if (currentUser == null || !"ACTIVE".equals(currentUser.getStatus())
                    || role == null || !"ACTIVE".equals(role.getStatus())) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/auth/login");
                return;
            }
            roleName = role.getRoleName();
            currentUser.setPasswordHash(null);
            session.setAttribute("user", currentUser);
            session.setAttribute("roleName", roleName);
        } catch (SQLException ex) {
            request.getServletContext().log("Cannot refresh account permissions.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể kiểm tra tài khoản lúc này.");
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
