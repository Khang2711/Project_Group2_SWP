package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.Cinema;
import model.User;
import service.CinemaService;

@WebServlet(urlPatterns = {
    "/admin/cinemas",
    "/admin/cinema-form"
})
public class CinemaController extends HttpServlet {

    private final CinemaService cinemaService = new CinemaService();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user = getManager(request, response);

        if (user == null) {
            return;
        }

        int cinemaId = user.getCinemaId();

        switch (request.getServletPath()) {

            case "/admin/cinemas":
                showCinema(request, response, cinemaId);
                break;

            case "/admin/cinema-form":
                showForm(request, response, cinemaId);
                break;

            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user = getManager(request, response);

        if (user == null) {
            return;
        }

        if (!"/admin/cinema-form".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String address = request.getParameter("address");
        String phone = request.getParameter("phone");

        try {
            cinemaService.updateCinemaContact(
                    user.getCinemaId(),
                    address,
                    phone
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/admin/cinemas?updated=1"
            );

        } catch (IllegalArgumentException e) {

            request.setAttribute("error", e.getMessage());

            Cinema cinema = cinemaService.getCinema(
                    user.getCinemaId()
            );

            request.setAttribute("cinema", cinema);

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/cinema-form.jsp"
            ).forward(request, response);
        }
    }

    private void showCinema(HttpServletRequest request,
            HttpServletResponse response,
            int cinemaId)
            throws ServletException, IOException {

        Cinema cinema = cinemaService.getCinema(cinemaId);

        if (cinema == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy rạp."
            );
            return;
        }

        request.setAttribute("cinema", cinema);
        request.setAttribute(
                "roomCount",
                cinemaService.getRoomCount(cinemaId)
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/cinemas.jsp"
        ).forward(request, response);
    }

    private void showForm(HttpServletRequest request,
            HttpServletResponse response,
            int cinemaId)
            throws ServletException, IOException {

        Cinema cinema = cinemaService.getCinema(cinemaId);

        if (cinema == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy rạp."
            );
            return;
        }

        request.setAttribute("cinema", cinema);

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/cinema-form.jsp"
        ).forward(request, response);
    }

    private User getManager(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("user") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/auth/login"
            );

            return null;
        }

        String roleName =
                (String) session.getAttribute("roleName");

        if (!"CINEMA_MANAGER".equals(roleName)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return null;
        }

        User user = (User) session.getAttribute("user");

        if (user.getCinemaId() == null) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Tài khoản Manager chưa được phân công rạp."
            );

            return null;
        }

        return user;
    }
}