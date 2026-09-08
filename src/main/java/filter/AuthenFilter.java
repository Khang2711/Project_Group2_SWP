package filter;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebFilter(filterName = "AuthenFilter", urlPatterns = {"/*"})
public class AuthenFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Bảo đảm request/response luôn dùng UTF-8 để không lỗi dấu tiếng Việt.
        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");
        String ctx = req.getContextPath();
        String uri = req.getRequestURI();

        if (isPublic(uri, ctx)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("LOGIN_USER");

        if (user == null) {
            res.sendRedirect(ctx + "/auth");
            return;
        }

        if (uri.startsWith(ctx + "/admin") && !"AD".equals(user.getRoleID())) {
            res.sendRedirect(ctx + "/movies");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublic(String uri, String ctx) {
        return uri.equals(ctx + "/")
                || uri.startsWith(ctx + "/auth")
                || uri.startsWith(ctx + "/movies")
                || uri.startsWith(ctx + "/movie")
                || uri.startsWith(ctx + "/assets")
                || uri.startsWith(ctx + "/META-INF")
                || uri.startsWith(ctx + "/WEB-INF");
    }

    @Override
    public void destroy() {
    }
}
