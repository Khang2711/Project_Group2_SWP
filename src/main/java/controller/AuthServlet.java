package controller;

import dao.UserDAO;
import dao.Sql;
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
            try {request.setAttribute("profileData",Sql.rows("SELECT email,phone,avatar,birthDate FROM tblUsers WHERE userID=?",user.getUserID()).get(0));} catch(Exception e){request.setAttribute("error","Không tải được thông tin bổ sung.");}
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
            User user = dao.checkLogin(userID, password);

            if (user != null) {
                HttpSession session = request.getSession();
                request.changeSessionId();
                session.removeAttribute("CART");session.removeAttribute("COMBO_CART");session.removeAttribute("PENDING_ORDER");session.removeAttribute("FULL_THEATER");
                session.setAttribute("LOGIN_USER", user);

                Cookie cookie = new Cookie("LAST_USER", user.getUserID());
                cookie.setMaxAge(60 * 60 * 24 * 7);
                cookie.setPath(getCookiePath(request));
                response.addCookie(cookie);

                // Header sẽ hiện "Hi, [họ tên]", không hiện username.
                saveDisplayNameCookie(request, response, user.getFullName());

                response.sendRedirect(request.getContextPath() + security.Access.home(user.getRoleID()));
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
        String email = request.getParameter("email");
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

            User user = new User(userID, fullName, PasswordUtils.hash(password), "US");
            if(!userID.matches("[A-Za-z0-9_-]{1,30}")||fullName.length()>100||email==null||email.length()>150||!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
                request.setAttribute("error","Tên đăng nhập tối đa 30 ký tự chữ/số/gạch, họ tên tối đa 100 ký tự và email hợp lệ.");request.getRequestDispatcher(SIGNUP_PAGE).forward(request,response);return;
            }
            dao.createUser(user,email.trim());
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
                        loginUser.getUserID(), currentPassword);
                if (verifiedUser == null) {
                    request.setAttribute("user", loginUser);
                    request.setAttribute("error", "Mật khẩu hiện tại không đúng.");
                    request.getRequestDispatcher(PROFILE_PAGE).forward(request, response);
                    return;
                }
            }

            String email=request.getParameter("email"),phone=request.getParameter("phone"),avatar=request.getParameter("avatar"),birth=request.getParameter("birthDate");
            if(email==null)email="";if(phone==null)phone="";if(avatar==null)avatar="";
            email=email.trim();phone=phone.trim();avatar=avatar.trim();
            if(!email.isEmpty()&&!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))throw new IllegalArgumentException("Email không hợp lệ.");
            if(email.length()>150||phone.length()>20||avatar.length()>500)throw new IllegalArgumentException("Thông tin quá dài.");
            if(!avatar.isEmpty()&&!avatar.startsWith("https://"))throw new IllegalArgumentException("Ảnh đại diện phải dùng đường dẫn HTTPS.");
            if(birth!=null&&!birth.isBlank()&&java.time.LocalDate.parse(birth).isAfter(java.time.LocalDate.now()))throw new IllegalArgumentException("Ngày sinh không hợp lệ.");
            java.util.Map<String,Object> old=Sql.rows("SELECT email FROM tblUsers WHERE userID=?",loginUser.getUserID()).get(0);
            String oldEmail=old.get("email")==null?"":old.get("email").toString();
            if(!email.equals(oldEmail)&&dao.checkLogin(loginUser.getUserID(),currentPassword)==null)throw new IllegalArgumentException("Nhập mật khẩu hiện tại để thay đổi email khôi phục.");
            try(java.sql.Connection connection=utils.DBUtils.getConnection()) {
                connection.setAutoCommit(false);
                try {
                    Sql.update(connection,"UPDATE tblUsers SET fullName=?,email=?,phone=?,avatar=?,birthDate=? WHERE userID=?",fullName,email.isEmpty()?null:email,phone,avatar,birth==null||birth.isBlank()?null:java.sql.Date.valueOf(birth),loginUser.getUserID());
                    if(changePassword)Sql.update(connection,"UPDATE tblUsers SET password=? WHERE userID=?",PasswordUtils.hash(newPassword),loginUser.getUserID());
                    connection.commit();
                }catch(Exception ex){connection.rollback();throw ex;}
            }

            User updatedUser = dao.getUserByID(loginUser.getUserID());
            request.getSession().setAttribute("LOGIN_USER", updatedUser);
            saveDisplayNameCookie(request, response, updatedUser.getFullName());
            request.setAttribute("user", updatedUser);
            request.setAttribute("profileData",Sql.rows("SELECT email,phone,avatar,birthDate FROM tblUsers WHERE userID=?",loginUser.getUserID()).get(0));
            request.setAttribute("success", "Cập nhật thành công.");
            request.getRequestDispatcher(PROFILE_PAGE).forward(request, response);
        } catch (Exception e) {
            request.setAttribute("user", loginUser);
            request.setAttribute("error", e instanceof IllegalArgumentException?e.getMessage():"Không thể cập nhật thông tin.");
            request.getRequestDispatcher(PROFILE_PAGE).forward(request, response);
        }
    }
}
