package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;
import model.Pricing;
import model.User;
import service.MovieService;
import service.PricingService;

@WebServlet(urlPatterns = {
    "/admin/pricing",
    "/admin/pricing-form"
})
public class PricingController
        extends HttpServlet {

    private final PricingService pricingService =
            new PricingService();

    private final MovieService movieService =
            new MovieService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user =
                getManager(
                        request,
                        response
                );

        if (user == null) {
            return;
        }

        String path =
                request.getServletPath();

        if ("/admin/pricing".equals(path)) {

            loadPricingList(
                    request,
                    user
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/pricing.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }

        if ("/admin/pricing-form".equals(path)) {

            String id =
                    request.getParameter("id");

            if (id != null
                    && !id.trim().isEmpty()) {

                try {

                    int pricingId =
                            Integer.parseInt(id);

                    Pricing pricing =
                            pricingService
                                    .getPricing(
                                            pricingId,
                                            user.getCinemaId()
                                    );

                    if (pricing == null) {

                        response.sendError(
                                HttpServletResponse.SC_NOT_FOUND,
                                "Không tìm thấy giá vé."
                        );

                        return;
                    }

                    request.setAttribute(
                            "pricing",
                            pricing
                    );

                } catch (NumberFormatException e) {

                    response.sendError(
                            HttpServletResponse.SC_BAD_REQUEST,
                            "Mã giá vé không hợp lệ."
                    );

                    return;
                }
            }

            loadMovies(
                    request,
                    user
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/pricing-form.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }

        response.sendError(
                HttpServletResponse.SC_NOT_FOUND
        );
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user =
                getManager(
                        request,
                        response
                );

        if (user == null) {
            return;
        }

        String path =
                request.getServletPath();

        if ("/admin/pricing".equals(path)) {

            String action =
                    request.getParameter(
                            "action"
                    );

            if (!"delete".equals(action)) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                return;
            }

            try {

                int pricingId =
                        Integer.parseInt(
                                request.getParameter(
                                        "id"
                                )
                        );

                pricingService.deletePricing(
                        pricingId,
                        user.getCinemaId()
                );

                response.sendRedirect(
                        request.getContextPath()
                        + "/admin/pricing?deleted=1"
                );

            } catch (NumberFormatException e) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST
                );

            } catch (IllegalArgumentException e) {

                request.setAttribute(
                        "error",
                        e.getMessage()
                );

                loadPricingList(
                        request,
                        user
                );

                request.getRequestDispatcher(
                        "/WEB-INF/views/admin/pricing.jsp"
                ).forward(
                        request,
                        response
                );
            }

            return;
        }

        if (!"/admin/pricing-form".equals(path)) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }

        try {

            String id =
                    request.getParameter("id");

            Integer movieId =
                    parseOptionalInteger(
                            request.getParameter(
                                    "movieId"
                            )
                    );

            String roomType =
                    request.getParameter(
                            "roomType"
                    );

            String seatType =
                    request.getParameter(
                            "seatType"
                    );

            String dayType =
                    request.getParameter(
                            "dayType"
                    );

            Date specialDate =
                    parseOptionalDate(
                            request.getParameter(
                                    "specialDate"
                            ),
                            "Ngày đặc biệt không hợp lệ."
                    );

            Time startTime =
                    parseOptionalTime(
                            request.getParameter(
                                    "startTime"
                            ),
                            "Giờ bắt đầu không hợp lệ."
                    );

            Time endTime =
                    parseOptionalTime(
                            request.getParameter(
                                    "endTime"
                            ),
                            "Giờ kết thúc không hợp lệ."
                    );

            BigDecimal price =
                    parsePrice(
                            request.getParameter(
                                    "price"
                            )
                    );

            Date startDate =
                    parseRequiredDate(
                            request.getParameter(
                                    "startDate"
                            )
                    );

            Date endDate =
                    parseOptionalDate(
                            request.getParameter(
                                    "endDate"
                            ),
                            "Ngày kết thúc không hợp lệ."
                    );

            String status =
                    request.getParameter(
                            "status"
                    );

            if (id == null
                    || id.trim().isEmpty()) {

                pricingService.createPricing(
                        user.getCinemaId(),
                        movieId,
                        roomType,
                        seatType,
                        dayType,
                        specialDate,
                        startTime,
                        endTime,
                        price,
                        startDate,
                        endDate,
                        status
                );

            } else {

                int pricingId =
                        Integer.parseInt(id);

                pricingService.updatePricing(
                        pricingId,
                        user.getCinemaId(),
                        movieId,
                        roomType,
                        seatType,
                        dayType,
                        specialDate,
                        startTime,
                        endTime,
                        price,
                        startDate,
                        endDate,
                        status
                );
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/admin/pricing?saved=1"
            );

        } catch (NumberFormatException e) {

            request.setAttribute(
                    "error",
                    "Giá vé hoặc mã dữ liệu không hợp lệ."
            );

            forwardForm(
                    request,
                    response,
                    user
            );

        } catch (IllegalArgumentException e) {

            request.setAttribute(
                    "error",
                    e.getMessage()
            );

            forwardForm(
                    request,
                    response,
                    user
            );
        }
    }

    private BigDecimal parsePrice(
            String value) {

        if (value == null
                || value.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Giá vé không được để trống."
            );
        }

        try {

            return new BigDecimal(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Giá vé không hợp lệ."
            );
        }
    }

    private Integer parseOptionalInteger(
            String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        try {

            return Integer.valueOf(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Phim không hợp lệ."
            );
        }
    }

    private Date parseRequiredDate(
            String value) {

        if (value == null
                || value.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Ngày bắt đầu không được để trống."
            );
        }

        try {

            return Date.valueOf(
                    value.trim()
            );

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Ngày bắt đầu không hợp lệ."
            );
        }
    }

    private Date parseOptionalDate(
            String value,
            String errorMessage) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        try {

            return Date.valueOf(
                    value.trim()
            );

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    errorMessage
            );
        }
    }

    private Time parseOptionalTime(
            String value,
            String errorMessage) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        try {

            LocalTime localTime =
                    LocalTime.parse(
                            value.trim()
                    );

            return Time.valueOf(
                    localTime
            );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    errorMessage
            );
        }
    }

    private void loadPricingList(
            HttpServletRequest request,
            User user) {

        request.setAttribute(
                "pricingList",
                pricingService
                        .getPricingByCinema(
                                user.getCinemaId()
                        )
        );
    }

    private void loadMovies(
            HttpServletRequest request,
            User user) {

        request.setAttribute(
                "movies",
                movieService
                        .getMoviesByCinema(
                                user.getCinemaId()
                        )
        );
    }

    private void forwardForm(
            HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws ServletException, IOException {

        Pricing pricing =
                new Pricing();

        try {

            String id =
                    request.getParameter("id");

            if (id != null
                    && !id.trim().isEmpty()) {

                pricing.setPricingId(
                        Integer.parseInt(id)
                );
            }

        } catch (Exception e) {
            pricing.setPricingId(0);
        }

        pricing.setCinemaId(
                user.getCinemaId()
        );

        try {

            pricing.setMovieId(
                    parseOptionalInteger(
                            request.getParameter(
                                    "movieId"
                            )
                    )
            );

        } catch (Exception e) {
            pricing.setMovieId(null);
        }

        pricing.setRoomType(
                request.getParameter(
                        "roomType"
                )
        );

        pricing.setSeatType(
                request.getParameter(
                        "seatType"
                )
        );

        pricing.setDayType(
                request.getParameter(
                        "dayType"
                )
        );

        try {

            pricing.setSpecialDate(
                    parseOptionalDate(
                            request.getParameter(
                                    "specialDate"
                            ),
                            ""
                    )
            );

        } catch (Exception e) {
            pricing.setSpecialDate(null);
        }

        try {

            pricing.setStartTime(
                    parseOptionalTime(
                            request.getParameter(
                                    "startTime"
                            ),
                            ""
                    )
            );

        } catch (Exception e) {
            pricing.setStartTime(null);
        }

        try {

            pricing.setEndTime(
                    parseOptionalTime(
                            request.getParameter(
                                    "endTime"
                            ),
                            ""
                    )
            );

        } catch (Exception e) {
            pricing.setEndTime(null);
        }

        try {

            pricing.setPrice(
                    parsePrice(
                            request.getParameter(
                                    "price"
                            )
                    )
            );

        } catch (Exception e) {
            pricing.setPrice(null);
        }

        try {

            String value =
                    request.getParameter(
                            "startDate"
                    );

            if (value != null
                    && !value.trim().isEmpty()) {

                pricing.setStartDate(
                        Date.valueOf(
                                value.trim()
                        )
                );
            }

        } catch (Exception e) {
            pricing.setStartDate(null);
        }

        try {

            pricing.setEndDate(
                    parseOptionalDate(
                            request.getParameter(
                                    "endDate"
                            ),
                            ""
                    )
            );

        } catch (Exception e) {
            pricing.setEndDate(null);
        }

        pricing.setStatus(
                request.getParameter(
                        "status"
                )
        );

        request.setAttribute(
                "pricing",
                pricing
        );

        loadMovies(
                request,
                user
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/pricing-form.jsp"
        ).forward(
                request,
                response
        );
    }

    private User getManager(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute(
                        "user"
                ) == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/login"
            );

            return null;
        }

        String roleName =
                (String) session.getAttribute(
                        "roleName"
                );

        if (!"CINEMA_MANAGER".equals(
                roleName)) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return null;
        }

        User user =
                (User) session.getAttribute(
                        "user"
                );

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