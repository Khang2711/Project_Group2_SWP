package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import jakarta.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.UUID;
import model.User;
import service.ProfileService;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import service.AvatarService;

@MultipartConfig(
        maxFileSize = 5242880,
        maxRequestSize = 6291456
)
@WebServlet(urlPatterns = {"/user/profile", "/user/edit-profile"})
public class ProfileController extends HttpServlet {

    private final ProfileService profileService = new ProfileService();
    private final AvatarService avatarService = new AvatarService();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User sessionUser = requireCustomer(request, response);

        if (sessionUser == null) {
            return;
        }

        try {
            User profile
                    = profileService.getProfile(sessionUser.getUserId());

            request.getSession().setAttribute("user", profile);
            request.setAttribute("profile", profile);

            if (request.getSession().getAttribute("profileToken") == null) {
                request.getSession().setAttribute(
                        "profileToken",
                        UUID.randomUUID().toString());
            }

            request.setAttribute(
                    "success",
                    request.getSession().getAttribute("profileSuccess"));

            request.getSession().removeAttribute("profileSuccess");

            showPage(request, response);

        } catch (IllegalArgumentException e) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    e.getMessage());

        } catch (SQLException e) {
            log("Cannot load profile.", e);

            request.setAttribute(
                    "error",
                    "Không thể tải thông tin cá nhân. "
                    + "Vui lòng thử lại sau.");

            showPage(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User sessionUser = requireCustomer(request, response);

        if (sessionUser == null) {
            return;
        }

        if (!"/user/edit-profile".equals(request.getServletPath())) {
            response.sendError(
                    HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        HttpSession session = request.getSession(false);

        String uploadedAvatar = null;
        boolean saved = false;

        try {
            String token
                    = (String) session.getAttribute("profileToken");

            if (token == null
                    || !token.equals(
                            request.getParameter("profileToken"))) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Phiên gửi biểu mẫu không hợp lệ.");
                return;
            }

            User currentProfile
                    = profileService.getProfile(sessionUser.getUserId());

            User form = new User();

            form.setFullName(request.getParameter("fullName"));
            form.setEmail(currentProfile.getEmail());
            form.setPhone(request.getParameter("phone"));
            form.setAvatarUrl(currentProfile.getAvatarUrl());

            request.setAttribute("profile", form);

            Part avatarFile = request.getPart("avatarFile");

            if (avatarFile != null && avatarFile.getSize() > 0) {
                uploadedAvatar = avatarService.save(avatarFile);
            }

            String avatarUrl = uploadedAvatar == null
                    ? currentProfile.getAvatarUrl()
                    : uploadedAvatar;

            User updatedUser = profileService.updateProfile(
                    sessionUser.getUserId(),
                    form.getFullName(),
                    form.getEmail(),
                    form.getPhone(),
                    avatarUrl,
                    request.getParameter("currentPassword"),
                    request.getParameter("newPassword"));

            saved = true;

            session.setAttribute("user", updatedUser);

            session.setAttribute(
                    "profileToken",
                    UUID.randomUUID().toString());

            session.setAttribute(
                    "profileSuccess",
                    "Đã lưu thông tin cá nhân thành công.");

            response.sendRedirect(
                    request.getContextPath() + "/user/profile");

        } catch (IllegalStateException e) {
            request.setAttribute(
                    "error",
                    "Ảnh tối đa 5 MB; "
                    + "tổng dữ liệu biểu mẫu tối đa 6 MB.");

            showUploadError(request, response);

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            showUploadError(request, response);

        } catch (SQLException e) {
            log("Cannot update profile.", e);

            request.setAttribute(
                    "error",
                    "Không thể lưu thông tin. Vui lòng thử lại sau.");
            showUploadError(request, response);

        } catch (IOException | ServletException e) {
            if (saved) {
                throw e;
            }

            log("Cannot read or save avatar.", e);

            request.setAttribute(
                    "error",
                    "Không thể đọc hoặc lưu ảnh. "
                    + "Vui lòng chọn ảnh hợp lệ và thử lại.");

            showUploadError(request, response);

        } finally {
            if (!saved && uploadedAvatar != null) {
                try {
                    avatarService.delete(uploadedAvatar);

                } catch (IOException e) {
                    log("Cannot remove unused avatar.", e);
                }
            }
        }
    }

    private void showUploadError(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (request.getAttribute("profile") == null) {
            request.setAttribute(
                    "profile",
                    request.getSession().getAttribute("user"));
        }

        showPage(request, response);
    }

    private User requireCustomer(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        response.setHeader(
                "Cache-Control",
                "no-store, no-cache, must-revalidate");

        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("user") instanceof User)) {

            response.sendRedirect(
                    request.getContextPath() + "/auth/login");
            return null;
        }

        if (!"CUSTOMER".equals(session.getAttribute("roleName"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        return (User) session.getAttribute("user");
    }

    private void showPage(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String view
                = "/user/edit-profile".equals(request.getServletPath())
                ? "/WEB-INF/views/user/edit-profile.jsp"
                : "/WEB-INF/views/user/profile.jsp";

        request.getRequestDispatcher(view).forward(request, response);
    }
}
