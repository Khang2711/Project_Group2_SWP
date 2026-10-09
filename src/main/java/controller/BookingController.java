package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Seat;
import model.Showtime;
import model.User;
import service.BookingService;


@WebServlet(urlPatterns = {
    "/user/select-seats",
    "/user/checkout",
    "/user/bookings",
    "/user/booking-detail"
})
public class BookingController
        extends HttpServlet {

    private BookingService bookingService;


    @Override
    public void init() {

        bookingService
                = new BookingService();
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


                case "/user/select-seats":

                    showSeatSelection(
                            request,
                            response
                    );

                    break;


                /*
                 * Không cho mở checkout trực tiếp
                 * bằng URL.
                 *
                 * Checkout phải đi từ POST
                 * của trang chọn ghế.
                 */
                case "/user/checkout":

                    response.sendRedirect(
                            request.getContextPath()
                            + "/user/movies"
                    );

                    break;


                /*
                 * UC-14:
                 * Nếu ông đã code UC-14 rồi
                 * thì giữ code UC-14 hiện tại tại đây.
                 */
                case "/user/bookings":

                    request.getRequestDispatcher(
                            "/WEB-INF/views/user/bookings.jsp"
                    ).forward(
                            request,
                            response
                    );

                    break;


                case "/user/booking-detail":

                    request.getRequestDispatcher(
                            "/WEB-INF/views/user/booking-detail.jsp"
                    ).forward(
                            request,
                            response
                    );

                    break;


                default:

                    response.sendError(
                            HttpServletResponse.SC_NOT_FOUND
                    );

                    break;
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

            throw new ServletException(
                    e
            );
        }
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding(
                "UTF-8"
        );


        String path
                = request.getServletPath();


        try {


            if ("/user/select-seats"
                    .equals(path)) {

                prepareCheckout(
                        request,
                        response
                );

                return;
            }


            if ("/user/checkout"
                    .equals(path)) {

                createBooking(
                        request,
                        response
                );

                return;
            }


            response.sendError(
                    HttpServletResponse
                            .SC_METHOD_NOT_ALLOWED
            );


        } catch (IllegalArgumentException e) {


            if ("/user/select-seats".equals(path)
                    || "/user/checkout".equals(path)) {


                try {

                    showSeatSelectionWithError(
                            request,
                            response,
                            e.getMessage()
                    );


                } catch (SQLException sqlException) {

                    throw new ServletException(
                            sqlException
                    );
                }


                return;
            }


            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );


        } catch (SQLException e) {

            throw new ServletException(
                    e
            );
        }
    }


    private void showSeatSelection(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException,
            IOException,
            SQLException {


        int showtimeId
                = parsePositiveInt(
                        request.getParameter(
                                "showtimeId"
                        ),
                        "Suất chiếu không hợp lệ."
                );


        loadSeatSelection(
                request,
                showtimeId
        );


        request.getRequestDispatcher(
                "/WEB-INF/views/user/select-seats.jsp"
        ).forward(
                request,
                response
        );
    }


    private void prepareCheckout(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException,
            IOException,
            SQLException {


        int showtimeId
                = parsePositiveInt(
                        request.getParameter(
                                "showtimeId"
                        ),
                        "Suất chiếu không hợp lệ."
                );


        boolean fullRoom
                = "FULL_ROOM".equals(
                        request.getParameter(
                                "bookingMode"
                        )
                );


        List<Seat> selectedSeats
                = bookingService
                        .prepareSelectedSeats(
                                showtimeId,
                                request.getParameterValues(
                                        "seatId"
                                ),
                                fullRoom
                        );


        Map<Integer, BigDecimal> seatPrices
                = bookingService
                        .getSeatPrices(
                                showtimeId,
                                selectedSeats
                        );


        BigDecimal subtotal
                = bookingService
                        .calculateSubtotal(
                                showtimeId,
                                selectedSeats
                        );


        request.setAttribute(
                "showtime",
                bookingService
                        .getBookableShowtime(
                                showtimeId
                        )
        );


        request.setAttribute(
                "selectedSeats",
                selectedSeats
        );


        request.setAttribute(
                "seatPrices",
                seatPrices
        );


        request.setAttribute(
                "subtotal",
                subtotal
        );


        request.setAttribute(
                "fullRoom",
                fullRoom
        );


        request.getRequestDispatcher(
                "/WEB-INF/views/user/checkout.jsp"
        ).forward(
                request,
                response
        );
    }


    private void createBooking(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException,
            SQLException {


        int customerId
                = getCurrentCustomerId(
                        request
                );


        int showtimeId
                = parsePositiveInt(
                        request.getParameter(
                                "showtimeId"
                        ),
                        "Suất chiếu không hợp lệ."
                );


        int bookingId
                = bookingService
                        .createPendingBooking(
                                customerId,
                                showtimeId,
                                request.getParameterValues(
                                        "seatId"
                                )
                        );


        /*
         * UC-13 hiện nhóm chưa cấu hình VNPay.
         *
         * Tạm thời chuyển qua UC-14 để
         * nhìn thấy Booking PENDING vừa tạo.
         */
        response.sendRedirect(
                request.getContextPath()
                + "/user/bookings"
        );


        /*
         * Sau này khi UC-13 làm xong,
         * thay redirect phía trên bằng:
         *
         * response.sendRedirect(
         *     request.getContextPath()
         *     + "/user/payment?bookingId="
         *     + bookingId
         * );
         */
    }


    private void loadSeatSelection(
            HttpServletRequest request,
            int showtimeId
    ) throws SQLException {


        Showtime showtime
                = bookingService
                        .getBookableShowtime(
                                showtimeId
                        );


        List<Seat> seats
                = bookingService
                        .getSeatsForShowtime(
                                showtimeId
                        );


        Set<Integer> occupiedSeatIds
                = bookingService
                        .getOccupiedSeatIds(
                                showtimeId
                        );


        Map<Integer, BigDecimal> seatPrices
                = bookingService
                        .getSeatPrices(
                                showtimeId,
                                seats
                        );


        /*
         * Gom ghế theo seat_row.
         *
         * Hàng 1 nằm một dòng.
         * Hàng 2 nằm một dòng.
         */
        Map<Integer, List<Seat>> seatRows
                = new LinkedHashMap<>();


        for (Seat seat : seats) {


            if (!seatRows.containsKey(
                    seat.getSeatRow()
            )) {

                seatRows.put(
                        seat.getSeatRow(),
                        new ArrayList<>()
                );
            }


            seatRows.get(
                    seat.getSeatRow()
            ).add(
                    seat
            );
        }


        request.setAttribute(
                "showtime",
                showtime
        );


        request.setAttribute(
                "seats",
                seats
        );


        request.setAttribute(
                "seatRows",
                seatRows
        );


        request.setAttribute(
                "occupiedSeatIds",
                occupiedSeatIds
        );


        request.setAttribute(
                "seatPrices",
                seatPrices
        );
    }


    private void showSeatSelectionWithError(
            HttpServletRequest request,
            HttpServletResponse response,
            String errorMessage
    ) throws ServletException,
            IOException,
            SQLException {


        String showtimeValue
                = request.getParameter(
                        "showtimeId"
                );


        int showtimeId;


        try {

            showtimeId
                    = Integer.parseInt(
                            showtimeValue
                    );


            if (showtimeId <= 0) {

                throw new NumberFormatException();
            }


        } catch (NumberFormatException
                | NullPointerException e) {


            request.setAttribute(
                    "error",
                    errorMessage
            );


            request.getRequestDispatcher(
                    "/WEB-INF/views/error.jsp"
            ).forward(
                    request,
                    response
            );


            return;
        }


        loadSeatSelection(
                request,
                showtimeId
        );


        request.setAttribute(
                "error",
                errorMessage
        );


        request.getRequestDispatcher(
                "/WEB-INF/views/user/select-seats.jsp"
        ).forward(
                request,
                response
        );
    }


    private int getCurrentCustomerId(
            HttpServletRequest request
    ) {


        HttpSession session
                = request.getSession(
                        false
                );


        if (session == null
                || !(session.getAttribute("user")
                instanceof User)) {


            throw new IllegalArgumentException(
                    "Vui lòng đăng nhập lại."
            );
        }


        User user
                = (User) session
                        .getAttribute(
                                "user"
                        );


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