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
import service.EmailVerificationService;

@WebServlet(urlPatterns = {
    "/auth/login",
    "/auth/logout",
    "/auth/register",
    "/auth/verify-email",
    "/auth/forgot-password",
    "/auth/verify-otp",
    "/auth/reset-password"
})
public class AuthController extends HttpServlet {

    private static final long OTP_VALID_MILLIS = 5 * 60 * 1000L;

    private AuthService authService;
    private PasswordResetService passwordResetService;
    private EmailVerificationService emailVerificationService;

    @Override
    public void init() {
        authService = new AuthService();
        passwordResetService = new PasswordResetService();
        emailVerificationService = new EmailVerificationService();
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

            case "/auth/verify-email":
                showVerifyEmail(request, response);
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

            case "/auth/verify-email":
                if ("verify".equals(request.getParameter("action"))) {
                    verifyEmailOtp(request, response);
                } else {
                    sendEmailVerificationOtp(request, response);
                }
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
                        HttpServletResponse.SC_METHOD_NOT_ALLOWED);
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

                // Get saved URL before creating a new session
                HttpSession oldSession = request.getSession(false);
                String redirectAfterLogin = null;

                if (oldSession != null) {
                    redirectAfterLogin
                            = (String) oldSession.getAttribute("redirectAfterLogin");
                }

                startSession(request, user, roleName);

                // Continue booking after customer login
                if ("CUSTOMER".equals(roleName)
                        && redirectAfterLogin != null) {

                    response.sendRedirect(
                            request.getContextPath() + redirectAfterLogin
                    );

                } else {

                    response.sendRedirect(
                            request.getContextPath() + homePath
                    );
                }

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
                request.getContextPath() + "/auth/login");
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
                "fullName", fullName == null ? "" : fullName.trim());

        request.setAttribute(
                "email", email == null ? "" : email.trim());

        request.setAttribute(
                "phone", phone == null ? "" : phone.trim());

        try {
            authService.register(
                    fullName, email, phone, password, confirmPassword);

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            showRegister(request, response);
            return;

        } catch (SQLException e) {
            log("Cannot register customer.", e);

            request.setAttribute(
                    "error",
                    "Không thể đăng ký lúc này. Vui lòng thử lại sau.");

            showRegister(request, response);
            return;
        }

        HttpSession session = request.getSession(true);

        session.removeAttribute("loginError");
        session.removeAttribute("registrationMessage");
        clearEmailVerificationData(session);

        String normalizedEmail
                = email.trim().toLowerCase(java.util.Locale.ROOT);

        session.setAttribute(
                "emailVerificationEmail", normalizedEmail);

        try {
            String otp = emailVerificationService.sendOtp(normalizedEmail);
            saveEmailVerificationOtp(session, otp);

            response.sendRedirect(
                    request.getContextPath() + "/auth/verify-email?sent=1");

        } catch (MessagingException e) {
            log("Cannot send registration email verification OTP.", e);

            clearEmailVerificationOtp(session);

            session.setAttribute(
                    "emailVerificationError",
                    "Tài khoản đã được tạo nhưng chưa gửi được OTP. "
                    + "Vui lòng kiểm tra cấu hình email rồi bấm Gửi lại OTP.");

            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/verify-email?sendError=1");

        } catch (IllegalArgumentException e) {
            clearEmailVerificationOtp(session);

            session.setAttribute(
                    "emailVerificationError", e.getMessage());

            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/verify-email?sendError=1");

        } catch (SQLException e) {
            log("Cannot prepare registration email verification.", e);

            clearEmailVerificationOtp(session);

            session.setAttribute(
                    "emailVerificationError",
                    "Tài khoản đã được tạo nhưng không thể chuẩn bị OTP. "
                    + "Vui lòng thử gửi lại mã.");

            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/verify-email?sendError=1");
        }
    }

    private void sendEmailVerificationOtp(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);
        String email = request.getParameter("email");

        if (email == null || email.trim().isEmpty()) {
            email = (String) session.getAttribute(
                    "emailVerificationEmail");
        }

        email = email == null
                ? ""
                : email.trim().toLowerCase(java.util.Locale.ROOT);

        request.setAttribute("emailVerificationEmail", email);
        session.setAttribute("emailVerificationEmail", email);

        clearEmailVerificationOtp(session);

        try {
            if (email.isEmpty()) {
                throw new IllegalArgumentException(
                        "Vui lòng nhập email.");
            }

            String otp = emailVerificationService.sendOtp(email);
            saveEmailVerificationOtp(session, otp);

            session.removeAttribute("emailVerificationError");

            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/verify-email?sent=1");

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            showVerifyEmail(request, response);

        } catch (MessagingException e) {
            log("Cannot send email verification OTP.", e);

            request.setAttribute(
                    "error",
                    "Không thể gửi OTP. Vui lòng kiểm tra cấu hình email rồi thử lại.");

            showVerifyEmail(request, response);

        } catch (SQLException e) {
            log("Cannot prepare email verification OTP.", e);

            request.setAttribute(
                    "error",
                    "Không thể kiểm tra tài khoản lúc này. Vui lòng thử lại sau.");

            showVerifyEmail(request, response);
        }
    }

    private void verifyEmailOtp(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("emailVerificationEmail") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/auth/verify-email");
            return;
        }

        String email
                = (String) session.getAttribute("emailVerificationEmail");

        String expectedOtp
                = (String) session.getAttribute("emailVerificationOtp");

        Long expiresAt
                = (Long) session.getAttribute(
                        "emailVerificationOtpExpiresAt");

        if (expectedOtp == null || expiresAt == null) {
            request.setAttribute(
                    "error",
                    "Bạn cần gửi OTP trước khi xác thực email. Hãy bấm Gửi OTP.");

            showVerifyEmail(request, response);
            return;
        }

        if (System.currentTimeMillis() > expiresAt) {
            clearEmailVerificationOtp(session);

            request.setAttribute(
                    "error",
                    "Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.");

            showVerifyEmail(request, response);
            return;
        }

        String enteredOtp = request.getParameter("otp");

        if (enteredOtp == null || enteredOtp.trim().isEmpty()) {
            request.setAttribute(
                    "error", "Vui lòng nhập mã OTP.");

            showVerifyEmail(request, response);
            return;
        }

        Integer attemptsValue
                = (Integer) session.getAttribute(
                        "emailVerificationOtpAttempts");

        int attempts = attemptsValue == null ? 0 : attemptsValue;

        if (attempts >= 5) {
            clearEmailVerificationOtp(session);

            request.setAttribute(
                    "error",
                    "Bạn đã nhập sai OTP quá 5 lần. Vui lòng gửi lại mã mới.");

            showVerifyEmail(request, response);
            return;
        }

        if (!expectedOtp.equals(enteredOtp.trim())) {
            attempts++;

            if (attempts >= 5) {
                clearEmailVerificationOtp(session);

                request.setAttribute(
                        "error",
                        "Bạn đã nhập sai OTP 5 lần. Vui lòng gửi lại mã mới.");

            } else {
                session.setAttribute(
                        "emailVerificationOtpAttempts", attempts);

                request.setAttribute(
                        "error",
                        "Mã OTP không đúng. Bạn còn "
                        + (5 - attempts) + " lần thử.");
            }

            showVerifyEmail(request, response);
            return;
        }

        try {
            emailVerificationService.markEmailVerified(email);

            clearEmailVerificationData(session);

            session.removeAttribute("loginError");

            session.setAttribute(
                    "registrationMessage",
                    "Xác thực email thành công. Bạn có thể đăng nhập.");

            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/login?verified=1");

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            showVerifyEmail(request, response);

        } catch (SQLException e) {
            log("Cannot mark customer email as verified.", e);

            request.setAttribute(
                    "error",
                    "Không thể lưu trạng thái xác thực email lúc này. Vui lòng thử lại.");

            showVerifyEmail(request, response);
        }
    }

    private void saveEmailVerificationOtp(
            HttpSession session, String otp) {

        session.setAttribute("emailVerificationOtp", otp);

        session.setAttribute(
                "emailVerificationOtpExpiresAt",
                System.currentTimeMillis() + OTP_VALID_MILLIS);

        session.setAttribute(
                "emailVerificationOtpAttempts", 0);
    }

    private void clearEmailVerificationOtp(HttpSession session) {

        if (session == null) {
            return;
        }

        session.removeAttribute("emailVerificationOtp");
        session.removeAttribute("emailVerificationOtpExpiresAt");
        session.removeAttribute("emailVerificationOtpAttempts");
    }

    private void clearEmailVerificationData(HttpSession session) {

        if (session == null) {
            return;
        }

        session.removeAttribute("emailVerificationEmail");
        session.removeAttribute("emailVerificationError");

        clearEmailVerificationOtp(session);
    }

    private void sendOtp(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");

        request.setAttribute(
                "email",
                email == null ? "" : email.trim());

        try {
            String otp = passwordResetService.sendOtp(email);

            String resetEmail = email.trim().toLowerCase();

            HttpSession session = request.getSession(true);
            clearResetData(session);

            session.setAttribute("resetEmail", resetEmail);
            session.setAttribute("resetOtp", otp);

            session.setAttribute(
                    "resetOtpExpiresAt",
                    System.currentTimeMillis() + OTP_VALID_MILLIS);

            session.setAttribute("resetOtpVerified", false);

            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/verify-otp?sent=1");

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            showForgotPassword(request, response);

        } catch (SQLException e) {
            log("Cannot find account for password reset.", e);

            request.setAttribute(
                    "error",
                    "Không thể kiểm tra tài khoản lúc này.");

            showForgotPassword(request, response);

        } catch (MessagingException e) {
            log("Cannot send password reset OTP.", e);

            request.setAttribute(
                    "error",
                    "Không thể gửi OTP. "
                    + "Vui lòng kiểm tra cấu hình email.");

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
                    + "/auth/forgot-password");
            return;
        }

        String enteredOtp = request.getParameter("otp");

        String expectedOtp
                = (String) session.getAttribute("resetOtp");

        Long expiresAt
                = (Long) session.getAttribute("resetOtpExpiresAt");

        if (enteredOtp == null || enteredOtp.trim().isEmpty()) {
            request.setAttribute(
                    "error",
                    "Vui lòng nhập mã OTP.");

            showVerifyOtp(request, response);
            return;
        }

        if (expectedOtp == null || expiresAt == null
                || System.currentTimeMillis() > expiresAt) {

            session.removeAttribute("resetOtp");
            session.removeAttribute("resetOtpExpiresAt");

            request.setAttribute(
                    "error",
                    "Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.");

            showVerifyOtp(request, response);
            return;
        }

        if (!expectedOtp.equals(enteredOtp.trim())) {
            request.setAttribute(
                    "error",
                    "Mã OTP không đúng.");

            showVerifyOtp(request, response);
            return;
        }

        session.setAttribute("resetOtpVerified", true);

        session.removeAttribute("resetOtp");
        session.removeAttribute("resetOtpExpiresAt");

        response.sendRedirect(
                request.getContextPath()
                + "/auth/reset-password");
    }

    private void resetPassword(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (!isOtpVerified(session)) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/forgot-password");
            return;
        }

        String email
                = (String) session.getAttribute("resetEmail");

        String newPassword
                = request.getParameter("newPassword");

        String confirmPassword
                = request.getParameter("confirmPassword");

        try {
            passwordResetService.resetPassword(
                    email,
                    newPassword,
                    confirmPassword);

            clearResetData(session);

            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/login?reset=1");

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            showResetPassword(request, response);

        } catch (SQLException e) {
            log("Cannot update password.", e);

            request.setAttribute(
                    "error",
                    "Không thể cập nhật mật khẩu lúc này.");

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
                request.getContextPath() + "/auth/login");
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
                return "/home";

            case "STAFF":
                return "/staff/counter";

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
                (String) session.getAttribute("roleName"));

        if (homePath == null) {
            session.invalidate();
            return false;
        }

        response.sendRedirect(
                request.getContextPath() + homePath);

        return true;
    }

    private void showLogin(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            request.setAttribute(
                    "error",
                    session.getAttribute("loginError"));

            request.setAttribute(
                    "success",
                    session.getAttribute("registrationMessage"));

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

    private void showVerifyEmail(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (request.getAttribute("emailVerificationEmail") == null) {
            request.setAttribute(
                    "emailVerificationEmail",
                    session == null
                            ? ""
                            : session.getAttribute("emailVerificationEmail"));
        }

        request.setAttribute(
                "otpSent",
                session != null
                && session.getAttribute("emailVerificationOtp") != null);

        if (request.getAttribute("error") == null && session != null) {
            Object storedError
                    = session.getAttribute("emailVerificationError");

            if (storedError != null) {
                request.setAttribute("error", storedError);

                session.removeAttribute(
                        "emailVerificationError");
            }
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/auth/verify-email.jsp"
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
                    + "/auth/forgot-password");
            return;
        }

        request.setAttribute(
                "resetEmail",
                session.getAttribute("resetEmail"));

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
                    + "/auth/forgot-password");
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
                        session.getAttribute("resetOtpVerified"));
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
                "no-store, no-cache, must-revalidate");

        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}
