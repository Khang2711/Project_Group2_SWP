package filter;

import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import model.User;
import dao.UserDAO;
import security.Access;

@WebFilter(urlPatterns = {"/*"})
public class AuthenFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");
        String p = req.getServletPath(), ctx = req.getContextPath();
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("X-Frame-Options", "SAMEORIGIN");
        if (p.startsWith("/assets/")) {
            chain.doFilter(req, res);
            return;
        }
        HttpSession session = req.getSession();
        if (session.getAttribute("CSRF") == null) {
            session.setAttribute("CSRF", java.util.UUID.randomUUID().toString());
        }
        String csrf = req.getParameter("csrf");
        if ("/staff/qr-decode".equals(p)) {
            csrf = req.getHeader("X-CSRF-Token");
        }
        if ("POST".equals(req.getMethod()) && !p.equals("/payment/ipn") && !session.getAttribute("CSRF").equals(csrf)) {
            res.sendError(403, "Phiên biểu mẫu đã hết hạn. Vui lòng tải lại trang.");
            return;
        }
        if (p.equals("/auth") && "signup".equals(req.getParameter("action")) && session.getAttribute("LOGIN_USER") != null) {
            res.sendError(403);
            return;
        }
        if (p.equals("/auth") || p.equals("/reset") || p.equals("/payment/ipn") || p.equals("/payment/return")) {
            chain.doFilter(req, res);
            return;
        }
        User u = (User) session.getAttribute("LOGIN_USER");
        if (u != null) {
            try {
                User fresh = new UserDAO().getUserByID(u.getUserID());
                if (fresh != null && !fresh.getPassword().equals(u.getPassword())) {
                    fresh = null;
                }
                u = fresh;
                session.setAttribute("LOGIN_USER", u);
            } catch (Exception e) {
                res.sendError(503, "Không thể kiểm tra quyền truy cập.");
                return;
            }
        }
        String role = u == null ? null : u.getRoleID();
        if (!Access.allowed(role, p)) {
            if (u == null) {
                if ("/staff/qr-decode".equals(p)) {
                    res.sendError(401);
                } else {
                    res.sendRedirect(ctx + "/auth");
                }
            } else {
                res.sendError(403, "Bạn không có quyền truy cập chức năng này.");
            }
            return;
        }
        if (u != null) {
            res.setHeader("Cache-Control", "no-store");
        }
        chain.doFilter(req, res);
    }
}
