package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import model.Promotion;
import service.PromotionService;

@WebServlet(urlPatterns = {
    "/admin/promotions",
    "/admin/promotion-form",
    "/user/apply-promo"
})
public class PromotionController extends HttpServlet {

    private PromotionService promotionService;

    @Override
    public void init() {
        promotionService = new PromotionService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        switch (path) {

            case "/admin/promotions":
                showPromotionList(
                        request,
                        response
                );
                break;

            case "/admin/promotion-form":
                showPromotionForm(
                        request,
                        response
                );
                break;

            case "/user/apply-promo":
                showApplyPromoPage(
                        request,
                        response
                );
                break;

            default:
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND
                );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(
                "UTF-8"
        );

        String path
                = request.getServletPath();

        switch (path) {

            case "/admin/promotion-form":
                savePromotion(
                        request,
                        response
                );
                break;

            case "/admin/promotions":
                changePromotionStatus(
                        request,
                        response
                );
                break;

            case "/user/apply-promo":
                applyPromoCode(
                        request,
                        response
                );
                break;

            default:
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND
                );
        }
    }

    private void showPromotionList(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute(
                "promotions",
                promotionService
                        .getAllPromotions()
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/promotions.jsp"
        ).forward(
                request,
                response
        );
    }

    private void showPromotionForm(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String idParam
                = request.getParameter(
                        "id"
                );

        if (idParam != null
                && !idParam.trim().isEmpty()) {

            try {

                int promotionId
                        = Integer.parseInt(
                                idParam
                        );

                Promotion promotion
                        = promotionService
                                .getPromotionById(
                                        promotionId
                                );

                if (promotion == null) {

                    response.sendRedirect(
                            request.getContextPath()
                            + "/admin/promotions"
                    );

                    return;
                }

                request.setAttribute(
                        "promotion",
                        promotion
                );

            } catch (NumberFormatException e) {

                response.sendRedirect(
                        request.getContextPath()
                        + "/admin/promotions"
                );

                return;
            }
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/promotion-form.jsp"
        ).forward(
                request,
                response
        );
    }

    private void showApplyPromoPage(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/WEB-INF/views/user/apply-promo.jsp"
        ).forward(
                request,
                response
        );
    }

    private void savePromotion(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {

            Promotion promotion
                    = buildPromotionFromRequest(
                            request
                    );

            String idParam
                    = request.getParameter(
                            "promotionId"
                    );

            String error;

            if (idParam == null
                    || idParam.trim().isEmpty()) {

                error
                        = promotionService
                                .createPromotion(
                                        promotion
                                );

            } else {

                int promotionId
                        = Integer.parseInt(
                                idParam
                        );

                promotion.setPromotionId(
                        promotionId
                );

                error
                        = promotionService
                                .updatePromotion(
                                        promotion
                                );
            }

            if (error != null) {

                request.setAttribute(
                        "error",
                        error
                );

                request.setAttribute(
                        "promotion",
                        promotion
                );

                request.getRequestDispatcher(
                        "/WEB-INF/views/admin/promotion-form.jsp"
                ).forward(
                        request,
                        response
                );

                return;
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/admin/promotions"
            );

        } catch (IllegalArgumentException e) {

            request.setAttribute(
                    "error",
                    e.getMessage()
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/promotion-form.jsp"
            ).forward(
                    request,
                    response
            );
        }
    }

    private void changePromotionStatus(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String idParam
                = request.getParameter(
                        "promotionId"
                );

        String status
                = request.getParameter(
                        "status"
                );

        if (idParam != null
                && status != null) {

            try {

                int promotionId
                        = Integer.parseInt(
                                idParam
                        );

                promotionService.updateStatus(
                        promotionId,
                        status
                );

            } catch (NumberFormatException e) {

                response.sendRedirect(
                        request.getContextPath()
                        + "/admin/promotions"
                );

                return;
            }
        }

        response.sendRedirect(
                request.getContextPath()
                + "/admin/promotions"
        );
    }

    private void applyPromoCode(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String promoCode
                = request.getParameter(
                        "promoCode"
                );

        String orderAmountParam
                = request.getParameter(
                        "orderAmount"
                );

        request.setAttribute(
                "promoCode",
                promoCode
        );

        request.setAttribute(
                "orderAmountInput",
                orderAmountParam
        );

        try {

            BigDecimal orderAmount
                    = parseBigDecimal(
                            orderAmountParam,
                            "Giá trị đơn hàng không hợp lệ."
                    );

            if (orderAmount.compareTo(
                    BigDecimal.ZERO) < 0) {

                throw new IllegalArgumentException(
                        "Giá trị đơn hàng không được nhỏ hơn 0."
                );
            }

            String error
                    = promotionService
                            .checkEligibility(
                                    promoCode,
                                    orderAmount
                            );

            if (error != null) {

                request.setAttribute(
                        "error",
                        error
                );

                request.setAttribute(
                        "orderAmount",
                        orderAmount
                );

                showApplyPromoPage(
                        request,
                        response
                );

                return;
            }

            BigDecimal discountAmount
                    = promotionService
                            .calculateDiscount(
                                    promoCode,
                                    orderAmount
                            );

            BigDecimal finalAmount
                    = promotionService
                            .calculateFinalAmount(
                                    promoCode,
                                    orderAmount
                            );

            Promotion promotion
                    = promotionService
                            .getPromotionByCode(
                                    promoCode
                            );

            request.setAttribute(
                    "success",
                    "Áp dụng mã khuyến mãi thành công."
            );

            request.setAttribute(
                    "promotion",
                    promotion
            );

            request.setAttribute(
                    "orderAmount",
                    orderAmount
            );

            request.setAttribute(
                    "discountAmount",
                    discountAmount
            );

            request.setAttribute(
                    "finalAmount",
                    finalAmount
            );

        } catch (IllegalArgumentException e) {

            request.setAttribute(
                    "error",
                    e.getMessage()
            );
        }

        showApplyPromoPage(
                request,
                response
        );
    }

    private Promotion buildPromotionFromRequest(
            HttpServletRequest request) {

        Promotion promotion
                = new Promotion();

        promotion.setPromoCode(
                request.getParameter(
                        "promoCode"
                )
        );

        promotion.setDescription(
                request.getParameter(
                        "description"
                )
        );

        promotion.setDiscountType(
                request.getParameter(
                        "discountType"
                )
        );

        promotion.setDiscountValue(
                parseBigDecimal(
                        request.getParameter(
                                "discountValue"
                        ),
                        "Giá trị giảm không hợp lệ."
                )
        );

        String minOrderAmount
                = request.getParameter(
                        "minOrderAmount"
                );

        if (minOrderAmount == null
                || minOrderAmount
                        .trim()
                        .isEmpty()) {

            promotion.setMinOrderAmount(
                    BigDecimal.ZERO
            );

        } else {

            promotion.setMinOrderAmount(
                    parseBigDecimal(
                            minOrderAmount,
                            "Giá trị đơn hàng tối thiểu không hợp lệ."
                    )
            );
        }

        String maxDiscountAmount
                = request.getParameter(
                        "maxDiscountAmount"
                );

        if (maxDiscountAmount != null
                && !maxDiscountAmount
                        .trim()
                        .isEmpty()) {

            promotion.setMaxDiscountAmount(
                    parseBigDecimal(
                            maxDiscountAmount,
                            "Giá trị giảm tối đa không hợp lệ."
                    )
            );
        }

        promotion.setStartDate(
                parseStartDate(
                        request.getParameter(
                                "startDate"
                        ),
                        "Ngày bắt đầu không hợp lệ."
                )
        );

        promotion.setEndDate(
                parseEndDate(
                        request.getParameter(
                                "endDate"
                        ),
                        "Ngày kết thúc không hợp lệ."
                )
        );

        String usageLimit
                = request.getParameter(
                        "usageLimit"
                );

        if (usageLimit != null
                && !usageLimit
                        .trim()
                        .isEmpty()) {

            try {

                promotion.setUsageLimit(
                        Integer.valueOf(
                                usageLimit.trim()
                        )
                );

            } catch (NumberFormatException e) {

                throw new IllegalArgumentException(
                        "Giới hạn sử dụng không hợp lệ."
                );
            }
        }

        promotion.setStatus(
                request.getParameter(
                        "status"
                )
        );

        return promotion;
    }

    private BigDecimal parseBigDecimal(
            String value,
            String errorMessage) {

        if (value == null
                || value.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    errorMessage
            );
        }

        try {

            return new BigDecimal(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    errorMessage
            );
        }
    }

    private Timestamp parseStartDate(
            String value,
            String errorMessage) {

        LocalDate date
                = parseDate(
                        value,
                        errorMessage
                );

        return Timestamp.valueOf(
                date.atStartOfDay()
        );
    }

    private Timestamp parseEndDate(
            String value,
            String errorMessage) {

        LocalDate date
                = parseDate(
                        value,
                        errorMessage
                );

        return Timestamp.valueOf(
                date.atTime(
                        23,
                        59,
                        59
                )
        );
    }

    private LocalDate parseDate(
            String value,
            String errorMessage) {

        if (value == null
                || value.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    errorMessage
            );
        }

        String input
                = value.trim();

        try {

            return LocalDate.parse(
                    input
            );

        } catch (DateTimeParseException e) {

            try {

                DateTimeFormatter formatter
                        = DateTimeFormatter
                                .ofPattern(
                                        "d/M/yyyy"
                                );

                return LocalDate.parse(
                        input,
                        formatter
                );

            } catch (DateTimeParseException ex) {

                throw new IllegalArgumentException(
                        errorMessage
                );
            }
        }
    }
}