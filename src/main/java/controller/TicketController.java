package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Booking;
import model.Movie;
import model.Seat;
import model.Showtime;
import model.Ticket;
import model.User;
import service.TicketService;

@WebServlet(urlPatterns = {
    "/user/tickets",
    "/user/ticket-detail"
})
public class TicketController extends HttpServlet {

    private TicketService ticketService;

    @Override
    public void init() {

        ticketService
                = new TicketService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String path
                = request.getServletPath();

        try {

            switch (path) {

                case "/user/tickets":

                    showTickets(
                            request,
                            response
                    );

                    break;


                case "/user/ticket-detail":

                    showTicketDetail(
                            request,
                            response
                    );

                    break;


                default:

                    response.sendError(
                            HttpServletResponse.SC_NOT_FOUND
                    );
            }

        } catch (IllegalArgumentException e) {

            request.setAttribute(
                    "error",
                    e.getMessage()
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/error.jsp"
            ).forward(
                    request,
                    response
            );

        } catch (SQLException e) {

            throw new ServletException(e);
        }
    }


    private void showTickets(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException,
            IOException,
            SQLException {

        int customerId
                = getCurrentCustomerId(
                        request
                );

        List<Ticket> tickets
                = ticketService
                        .getTicketsByCustomer(
                                customerId
                        );

        Map<Integer, Showtime> showtimeMap
                = new LinkedHashMap<>();

        Map<Integer, Movie> movieMap
                = new LinkedHashMap<>();

        Map<Integer, Seat> seatMap
                = new LinkedHashMap<>();


        for (Ticket ticket : tickets) {

            Booking booking
                    = ticketService
                            .getBookingForTicket(
                                    ticket,
                                    customerId
                            );

            if (booking == null) {
                continue;
            }


            Showtime showtime
                    = ticketService
                            .getShowtimeForBooking(
                                    booking
                            );

            if (showtime == null) {
                continue;
            }


            showtimeMap.put(
                    ticket.getTicketId(),
                    showtime
            );


            Movie movie
                    = ticketService
                            .getMovieForShowtime(
                                    showtime
                            );

            if (movie != null) {

                movieMap.put(
                        ticket.getTicketId(),
                        movie
                );
            }


            Seat seat
                    = ticketService
                            .getSeatForTicket(
                                    ticket,
                                    showtime
                            );

            if (seat != null) {

                seatMap.put(
                        ticket.getTicketId(),
                        seat
                );
            }
        }


        request.setAttribute(
                "tickets",
                tickets
        );

        request.setAttribute(
                "showtimeMap",
                showtimeMap
        );

        request.setAttribute(
                "movieMap",
                movieMap
        );

        request.setAttribute(
                "seatMap",
                seatMap
        );


        request.getRequestDispatcher(
                "/WEB-INF/views/user/tickets.jsp"
        ).forward(
                request,
                response
        );
    }


    private void showTicketDetail(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException,
            IOException,
            SQLException {

        int customerId
                = getCurrentCustomerId(
                        request
                );

        int ticketId
                = parsePositiveInt(
                        request.getParameter("id"),
                        "Vé không hợp lệ."
                );


        Ticket ticket
                = ticketService
                        .getTicketDetail(
                                ticketId,
                                customerId
                        );


        Booking booking
                = ticketService
                        .getBookingForTicket(
                                ticket,
                                customerId
                        );


        Showtime showtime
                = ticketService
                        .getShowtimeForBooking(
                                booking
                        );


        Movie movie
                = ticketService
                        .getMovieForShowtime(
                                showtime
                        );


        Seat seat
                = ticketService
                        .getSeatForTicket(
                                ticket,
                                showtime
                        );


        request.setAttribute(
                "ticket",
                ticket
        );

        request.setAttribute(
                "showtime",
                showtime
        );

        request.setAttribute(
                "movie",
                movie
        );

        request.setAttribute(
                "seat",
                seat
        );


        request.getRequestDispatcher(
                "/WEB-INF/views/user/ticket-detail.jsp"
        ).forward(
                request,
                response
        );
    }


    private int getCurrentCustomerId(
            HttpServletRequest request
    ) {

        HttpSession session
                = request.getSession(false);


        if (session == null
                || !(session.getAttribute("user")
                instanceof User)) {

            throw new IllegalArgumentException(
                    "Vui lòng đăng nhập lại."
            );
        }


        User user
                = (User) session
                        .getAttribute("user");

        return user.getUserId();
    }


    private int parsePositiveInt(
            String value,
            String errorMessage
    ) {

        try {

            int number
                    = Integer.parseInt(
                            value
                    );


            if (number <= 0) {
                throw new NumberFormatException();
            }


            return number;

        } catch (NumberFormatException
                | NullPointerException e) {

            throw new IllegalArgumentException(
                    errorMessage
            );
        }
    }
}