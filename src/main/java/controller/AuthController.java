package controller;

import jakarta.mail.MessagingException;
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
import service.PasswordResetService;

@WebServlet(urlPatterns = {
    "/auth/login",
    "/auth/logout",
    "/auth/register",
    "/auth/forgot-password",
    "/auth/verify-otp",
    "/auth/reset-password"
})
public class AuthController extends HttpServlet {

    private static final long OTP_VALID_MILLIS = 5 * 60 * 1000L;

    private AuthService authService;
    private PasswordResetService passwordResetService;

    @Override
    public void init() {
        authService = new AuthService();
        passwordResetService = new PasswordResetService();
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        preventCache(response);

        String path = request.getServletPath();

        switch (path) {
            case "/auth/login":
                HttpSession currentSession = request.getSession(false);

                if (currentSession != null
                        && currentSession.getAttribute("user") != null) {
                    currentSession.invalidate();
                }

                showLogin(request, response);
                break;

            case "/auth/logout":
                logout(request, response);
                break;

            case "/auth/register":
                if (redirectLoggedInUser(request, response)) {
                    return;
                }

                showRegister(request, response);
                break;

            case "/auth/forgot-password":
                showForgotPassword(request, response);
                break;

            case "/auth/verify-otp":
                showVerifyOtp(request, response);
                break;

            case "/auth/reset-password":
                showResetPassword(request, response);
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
        preventCache(response);

        String path = request.getServletPath();

        switch (path) {
            case "/auth/login":
                login(request, response);
                break;

            case "/auth/logout":
                logout(request, response);
                break;

            case "/auth/register":
                if (redirectLoggedInUser(request, response)) {
                    return;
                }

                register(request, response);
                break;

            case "/auth/forgot-password":
                sendOtp(request, response);
                break;

            case "/auth/verify-otp":
                verifyOtp(request, response);
                break;

            case "/auth/reset-password":
                resetPassword(request, response);
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

        String error;

        try {
            User user = authService.login(email, password);

            String roleName = authService.getRoleName(user.getRoleId());
            String homePath = getHomePath(roleName);

            if (homePath != null) {
                startSession(request, user, roleName);

                response.sendRedirect(
                        request.getContextPath() + homePath
                );
                return;
            }

            error = "Vai trò của tài khoản chưa được hỗ trợ.";

        } catch (IllegalArgumentException e) {
            error = e.getMessage();

        } catch (SQLException e) {
            log("Cannot process login.", e);

            error = "Không thể đăng nhập lúc này. "
                    + "Vui lòng kiểm tra kết nối database.";
        }

        request.getSession().setAttribute("loginError", error);

        response.sendRedirect(
                request.getContextPath() + "/auth/login"
        );
    }

    private void register(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        request.setAttribute(
                "fullName", fullName == null ? "" : fullName.trim()
        );
        request.setAttribute(
                "email", email == null ? "" : email.trim()
        );
        request.setAttribute(
                "phone", phone == null ? "" : phone.trim()
        );

        try {
            authService.register(
                    fullName, email, phone, password, confirmPassword
            );

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            showRegister(request, response);
            return;

        } catch (SQLException e) {
            log("Cannot register customer.", e);

            request.setAttribute(
                    "error",
                    "Không thể đăng ký lúc này. Vui lòng thử lại sau."
            );
            showRegister(request, response);
            return;
        }

        HttpSession session = request.getSession();

        session.removeAttribute("loginError");
        session.setAttribute(
                "registrationMessage",
                "Đăng ký thành công. Bạn có thể đăng nhập."
        );

        response.sendRedirect(
                request.getContextPath() + "/auth/login"
        );
    }

    private void sendOtp(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");

        request.setAttribute(
                "email",
                email == null ? "" : email.trim()
        );

        try {
            String otp = passwordResetService.sendOtp(email);

            String resetEmail = email.trim().toLowerCase();

            HttpSession session = request.getSession(true);
            clearResetData(session);

            session.setAttribute("resetEmail", resetEmail);
            session.setAttribute("resetOtp", otp);
            session.setAttribute(
                    "resetOtpExpiresAt",
                    System.currentTimeMillis() + OTP_VALID_MILLIS
            );
            session.setAttribute("resetOtpVerified", false);

            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/verify-otp?sent=1"
            );

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            showForgotPassword(request, response);

        } catch (SQLException e) {
            log("Cannot find account for password reset.", e);

            request.setAttribute(
                    "error",
                    "Không thể kiểm tra tài khoản lúc này."
            );

            showForgotPassword(request, response);

        } catch (MessagingException e) {
            log("Cannot send password reset OTP.", e);

            request.setAttribute(
                    "error",
                    "Không thể gửi OTP. "
                    + "Vui lòng kiểm tra cấu hình email."
            );

            showForgotPassword(request, response);
        }
    }

    private void verifyOtp(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (!hasResetRequest(session)) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/forgot-password"
            );
            return;
        }

        String enteredOtp = request.getParameter("otp");
        String expectedOtp =
                (String) session.getAttribute("resetOtp");
        Long expiresAt =
                (Long) session.getAttribute("resetOtpExpiresAt");

        if (enteredOtp == null || enteredOtp.trim().isEmpty()) {
            request.setAttribute(
                    "error",
                    "Vui lòng nhập mã OTP."
            );
            showVerifyOtp(request, response);
            return;
        }

        if (expectedOtp == null || expiresAt == null
                || System.currentTimeMillis() > expiresAt) {

            session.removeAttribute("resetOtp");
            session.removeAttribute("resetOtpExpiresAt");

            request.setAttribute(
                    "error",
                    "Mã OTP đã hết hạn. Vui lòng gửi lại mã mới."
            );

            showVerifyOtp(request, response);
            return;
        }

        if (!expectedOtp.equals(enteredOtp.trim())) {
            request.setAttribute(
                    "error",
                    "Mã OTP không đúng."
            );
            showVerifyOtp(request, response);
            return;
        }

        session.setAttribute("resetOtpVerified", true);
        session.removeAttribute("resetOtp");
        session.removeAttribute("resetOtpExpiresAt");

        response.sendRedirect(
                request.getContextPath()
                + "/auth/reset-password"
        );
    }

    private void resetPassword(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (!isOtpVerified(session)) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/forgot-password"
            );
            return;
        }

        String email =
                (String) session.getAttribute("resetEmail");

        String newPassword =
                request.getParameter("newPassword");

        String confirmPassword =
                request.getParameter("confirmPassword");

        try {
            passwordResetService.resetPassword(
                    email,
                    newPassword,
                    confirmPassword
            );

            clearResetData(session);

            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/login?reset=1"
            );

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            showResetPassword(request, response);

        } catch (SQLException e) {
            log("Cannot update password.", e);

            request.setAttribute(
                    "error",
                    "Không thể cập nhật mật khẩu lúc này."
            );

            showResetPassword(request, response);
        }
    }

    private void logout(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(
                request.getContextPath() + "/auth/login"
        );
    }

    private void startSession(HttpServletRequest request,
            User user, String roleName) {

        HttpSession oldSession = request.getSession(false);

        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = request.getSession(true);

        session.setAttribute("user", user);
        session.setAttribute("roleName", roleName);
        session.setMaxInactiveInterval(30 * 60);
    }

    private String getHomePath(String roleName) {
        if (roleName == null) {
            return null;
        }

        switch (roleName) {
            case "CUSTOMER":
                return "/user/movies";

            case "STAFF":
                return "/staff/check-in";

            case "CINEMA_MANAGER":
                return "/admin/dashboard";

            case "ADMIN":
                return "/system/users";

            default:
                return null;
        }
    }

    private boolean redirectLoggedInUser(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            return false;
        }

        String homePath = getHomePath(
                (String) session.getAttribute("roleName")
        );

        if (homePath == null) {
            session.invalidate();
            return false;
        }

        response.sendRedirect(request.getContextPath() + homePath);
        return true;
    }

    private void showLogin(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            request.setAttribute(
                    "error", session.getAttribute("loginError")
            );
            request.setAttribute(
                    "success", session.getAttribute("registrationMessage")
            );

            session.removeAttribute("loginError");
            session.removeAttribute("registrationMessage");
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/auth/login.jsp"
        ).forward(request, response);
    }

    private void showRegister(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/WEB-INF/views/auth/register.jsp"
        ).forward(request, response);
    }

    private void showForgotPassword(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/WEB-INF/views/auth/forgot-password.jsp"
        ).forward(request, response);
    }

    private void showVerifyOtp(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (!hasResetRequest(session)) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/forgot-password"
            );
            return;
        }

        request.setAttribute(
                "resetEmail",
                session.getAttribute("resetEmail")
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/auth/verify-otp.jsp"
        ).forward(request, response);
    }

    private void showResetPassword(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (!isOtpVerified(session)) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/forgot-password"
            );
            return;
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/auth/reset-password.jsp"
        ).forward(request, response);
    }

    private boolean hasResetRequest(HttpSession session) {
        return session != null
                && session.getAttribute("resetEmail") != null;
    }

    private boolean isOtpVerified(HttpSession session) {
        return hasResetRequest(session)
                && Boolean.TRUE.equals(
                        session.getAttribute("resetOtpVerified")
                );
    }

    private void clearResetData(HttpSession session) {
        if (session == null) {
            return;
        }

        session.removeAttribute("resetEmail");
        session.removeAttribute("resetOtp");
        session.removeAttribute("resetOtpExpiresAt");
        session.removeAttribute("resetOtpVerified");
    }

    private void preventCache(HttpServletResponse response) {
        response.setHeader(
                "Cache-Control",
                "no-store, no-cache, must-revalidate"
        );
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}
