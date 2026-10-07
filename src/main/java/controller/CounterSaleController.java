package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Opens staff counter sale pages.
 * Business logic will be added later through service classes.
 */
@WebServlet(urlPatterns = {
    "/staff/counter",
    "/staff/ticket-sale",
    "/staff/food-sale"
})
public class CounterSaleController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String view;

        switch (request.getServletPath()) {
            case "/staff/counter":
                view = "/WEB-INF/views/staff/counter.jsp";
                break;

            case "/staff/ticket-sale":
                view = "/WEB-INF/views/staff/ticket-sale.jsp";
                break;

            case "/staff/food-sale":
                view = "/WEB-INF/views/staff/food-sale.jsp";
                break;

            default:
                response.sendError(404);
                return;
        }

        request.getRequestDispatcher(view).forward(request, response);
    }

    // TODO: add doPost after TicketSaleService/FoodOrderService are implemented.
}
