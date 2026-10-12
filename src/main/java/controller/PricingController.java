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
import model.Pricing;
import model.User;
import service.PricingService;

@WebServlet(urlPatterns = {
    "/admin/pricing",
    "/admin/pricing-form"
})
public class PricingController
        extends HttpServlet {

    private final PricingService pricingService =
            new PricingService();

    // =========================================================
    // GET
    // =========================================================
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

        // -----------------------------------------------------
        // DANH SÁCH GIÁ VÉ
        // -----------------------------------------------------
        if ("/admin/pricing".equals(path)) {

            request.setAttribute(
                    "pricingList",
                    pricingService
                            .getPricingByCinema(
                                    user.getCinemaId()
                            )
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/pricing.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }

        // -----------------------------------------------------
        // FORM THÊM / SỬA
        // -----------------------------------------------------
        if ("/admin/pricing-form".equals(path)) {

            String id =
                    request.getParameter(
                            "id"
                    );

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

    // =========================================================
    // POST
    // =========================================================
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

        // -----------------------------------------------------
        // XÓA PRICING
        // -----------------------------------------------------
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

        // -----------------------------------------------------
        // THÊM / SỬA PRICING
        // -----------------------------------------------------
        if (!"/admin/pricing-form".equals(path)) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }

        String id =
                request.getParameter(
                        "id"
                );

        String seatType =
                request.getParameter(
                        "seatType"
                );

        String priceText =
                request.getParameter(
                        "price"
                );

        String startDateText =
                request.getParameter(
                        "startDate"
                );

        String endDateText =
                request.getParameter(
                        "endDate"
                );

        String status =
                request.getParameter(
                        "status"
                );

        try {

            // -----------------------------------------
            // PRICE
            // -----------------------------------------
            BigDecimal price =
                    new BigDecimal(
                            priceText.trim()
                    );

            // -----------------------------------------
            // START DATE
            // -----------------------------------------
            if (startDateText == null
                    || startDateText
                            .trim()
                            .isEmpty()) {

                throw new IllegalArgumentException(
                        "Ngày bắt đầu không được để trống."
                );
            }

            Date startDate =
                    Date.valueOf(
                            startDateText
                    );

            // -----------------------------------------
            // END DATE - OPTIONAL
            // -----------------------------------------
            Date endDate = null;

            if (endDateText != null
                    && !endDateText
                            .trim()
                            .isEmpty()) {

                endDate =
                        Date.valueOf(
                                endDateText
                        );
            }

            // -----------------------------------------
            // CREATE
            // -----------------------------------------
            if (id == null
                    || id.trim().isEmpty()) {

                pricingService.createPricing(
                        user.getCinemaId(),
                        seatType,
                        price,
                        startDate,
                        endDate,
                        status
                );

            } else {

                // -------------------------------------
                // UPDATE
                // -------------------------------------
                int pricingId =
                        Integer.parseInt(
                                id
                        );

                pricingService.updatePricing(
                        pricingId,
                        user.getCinemaId(),
                        seatType,
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
                    "Giá vé hoặc mã giá vé "
                    + "không hợp lệ."
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

    // =========================================================
    // LOAD LIST
    // =========================================================
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

    // =========================================================
    // LOAD LẠI FORM KHI LỖI
    // =========================================================
    private void forwardForm(
            HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws ServletException, IOException {

        Pricing pricing =
                new Pricing();

        String id =
                request.getParameter(
                        "id"
                );

        if (id != null
                && !id.trim().isEmpty()) {

            try {

                pricing.setPricingId(
                        Integer.parseInt(id)
                );

            } catch (NumberFormatException e) {

                pricing.setPricingId(0);
            }
        }

        pricing.setCinemaId(
                user.getCinemaId()
        );

        pricing.setSeatType(
                request.getParameter(
                        "seatType"
                )
        );

        // -----------------------------------------
        // PRICE
        // -----------------------------------------
        try {

            String priceText =
                    request.getParameter(
                            "price"
                    );

            if (priceText != null
                    && !priceText
                            .trim()
                            .isEmpty()) {

                pricing.setPrice(
                        new BigDecimal(
                                priceText.trim()
                        )
                );
            }

        } catch (NumberFormatException e) {

            pricing.setPrice(null);
        }

        // -----------------------------------------
        // START DATE
        // -----------------------------------------
        try {

            String startDateText =
                    request.getParameter(
                            "startDate"
                    );

            if (startDateText != null
                    && !startDateText
                            .trim()
                            .isEmpty()) {

                pricing.setStartDate(
                        Date.valueOf(
                                startDateText
                        )
                );
            }

        } catch (IllegalArgumentException e) {

            pricing.setStartDate(null);
        }

        // -----------------------------------------
        // END DATE
        // -----------------------------------------
        try {

            String endDateText =
                    request.getParameter(
                            "endDate"
                    );

            if (endDateText != null
                    && !endDateText
                            .trim()
                            .isEmpty()) {

                pricing.setEndDate(
                        Date.valueOf(
                                endDateText
                        )
                );
            }

        } catch (IllegalArgumentException e) {

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

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/pricing-form.jsp"
        ).forward(
                request,
                response
        );
    }

    // =========================================================
    // CHECK CINEMA MANAGER
    // =========================================================
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