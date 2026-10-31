package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.Cinema;
import service.CinemaService;

@WebServlet(urlPatterns = {
    "/system/cinemas",
    "/system/cinema-form"
})
public class SystemCinemaController extends HttpServlet {

    private final CinemaService cinemaService = new CinemaService();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/system/cinemas".equals(path)) {
            showCinemaList(request, response);
            return;
        }

        if ("/system/cinema-form".equals(path)) {
            showCinemaForm(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();

        if ("/system/cinema-form".equals(path)) {
            saveCinema(request, response);
            return;
        }

        if ("/system/cinemas".equals(path)) {
            updateCinemaAction(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void showCinemaList(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute(
                "cinemas",
                cinemaService.getAllCinemas()
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/system/cinemas.jsp"
        ).forward(request, response);
    }

    private void showCinemaForm(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String idText = request.getParameter("id");
        Cinema cinema = new Cinema();
        cinema.setStatus("ACTIVE");

        boolean isEdit = false;

        if (idText != null && !idText.trim().isEmpty()) {
            try {
                int cinemaId = Integer.parseInt(idText);
                cinema = cinemaService.getCinema(cinemaId);

                if (cinema == null) {
                    response.sendError(
                            HttpServletResponse.SC_NOT_FOUND,
                            "Không tìm thấy chi nhánh."
                    );
                    return;
                }

                isEdit = true;

            } catch (NumberFormatException e) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Mã chi nhánh không hợp lệ."
                );
                return;
            }
        }

        request.setAttribute("cinema", cinema);
        request.setAttribute("isEdit", isEdit);

        request.getRequestDispatcher(
                "/WEB-INF/views/system/cinema-form.jsp"
        ).forward(request, response);
    }

    private void saveCinema(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String idText = request.getParameter("id");
        boolean isEdit = idText != null && !idText.trim().isEmpty();

        Cinema cinema = new Cinema();
        cinema.setCinemaName(request.getParameter("cinemaName"));
        cinema.setAddress(request.getParameter("address"));
        cinema.setPhone(request.getParameter("phone"));
        cinema.setStatus(request.getParameter("status"));

        String error;

        if (isEdit) {
            try {
                cinema.setCinemaId(Integer.parseInt(idText));
            } catch (NumberFormatException e) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Mã chi nhánh không hợp lệ."
                );
                return;
            }

            error = cinemaService.updateCinema(cinema);

        } else {
            error = cinemaService.createCinema(cinema);
        }

        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("cinema", cinema);
            request.setAttribute("isEdit", isEdit);

            request.getRequestDispatcher(
                    "/WEB-INF/views/system/cinema-form.jsp"
            ).forward(request, response);
            return;
        }

        response.sendRedirect(
                request.getContextPath() + "/system/cinemas?saved=1"
        );
    }

    private void updateCinemaAction(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int cinemaId;

        try {
            cinemaId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Mã chi nhánh không hợp lệ."
            );
            return;
        }

        String error;

        if ("delete".equals(request.getParameter("action"))) {
            error = cinemaService.deleteCinema(cinemaId);

            if (error == null) {
                response.sendRedirect(
                        request.getContextPath() + "/system/cinemas?deleted=1"
                );
                return;
            }

        } else {
            error = cinemaService.updateCinemaStatus(
                    cinemaId,
                    request.getParameter("status")
            );

            if (error == null) {
                response.sendRedirect(
                        request.getContextPath()
                        + "/system/cinemas?statusUpdated=1"
                );
                return;
            }
        }

        request.setAttribute("error", error);
        showCinemaList(request, response);
    }
}
