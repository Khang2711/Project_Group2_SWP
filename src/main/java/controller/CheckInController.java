package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import service.CheckInService;

@WebServlet(urlPatterns = {"/staff/check-in"})
public class CheckInController extends HttpServlet {

    private CheckInService checkInService;

    @Override
    public void init() {
        checkInService = new CheckInService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        showPage(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String ticketCode = request.getParameter("ticketCode");

        String error = checkInService.validateTicketCode(ticketCode);

        if (error != null) {
            request.setAttribute("error", error);
            showPage(request, response);
            return;
        }

        ticketCode = ticketCode.trim().toUpperCase();

        request.setAttribute("ticketCode", ticketCode);

        showPage(request, response);
    }

    private void showPage(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/WEB-INF/views/staff/check-in.jsp"
        ).forward(request, response);
    }
}
