package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import model.Promotion;
import model.User;
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

        if ("/admin/promotions".equals(path)) {
            showPromotionList(request, response);

        } else if ("/admin/promotion-form".equals(path)) {
            showPromotionForm(request, response);

        } else if ("/user/apply-promo".equals(path)) {
            showApplyPromo(request, response);

        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();

        if ("/admin/promotion-form".equals(path)) {
            savePromotion(request, response);

        } else if ("/admin/promotions".equals(path)) {
            changePromotionStatus(request, response);

        } else if ("/user/apply-promo".equals(path)) {
            applyPromotion(request, response);

        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private Integer getCurrentCinemaId(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object sessionUser = session.getAttribute("user");

        if (!(sessionUser instanceof User)) {
            return null;
        }

        User user = (User) sessionUser;

        return user.getCinemaId();
    }

    private void showPromotionList(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Integer cinemaId = getCurrentCinemaId(request);

        if (cinemaId == null || cinemaId <= 0) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Tài khoản chưa được phân công rạp."
            );
            return;
        }

        String keyword = request.getParameter("keyword");
        String filter = request.getParameter("filter");

        if (keyword == null) {
            keyword = "";
        }

        keyword = keyword.trim();

        if (filter == null || filter.trim().isEmpty()) {
            filter = "ALL";
        }

        filter = filter.trim().toUpperCase(Locale.ROOT);

        if (!"AVAILABLE".equals(filter)
                && !"UPCOMING".equals(filter)
                && !"EXPIRED".equals(filter)
                && !"INACTIVE".equals(filter)) {
            filter = "ALL";
        }

        List<Promotion> allPromotions
                = promotionService.getAllPromotions(cinemaId);

        List<Promotion> promotions = new ArrayList<>();

        Map<Integer, String> displayStatuses = new HashMap<>();

        String searchText = keyword.toUpperCase(Locale.ROOT);

        for (Promotion promotion : allPromotions) {

            String status = promotionService.getDisplayStatus(promotion);

            String code = promotion.getPromoCode();

            boolean matchesKeyword = keyword.isEmpty();

            if (!matchesKeyword && code != null) {
                matchesKeyword = code.toUpperCase(Locale.ROOT)
                        .contains(searchText);
            }

            boolean matchesFilter = "ALL".equals(filter)
                    || filter.equals(status);

            if (matchesKeyword && matchesFilter) {

                promotions.add(promotion);

                displayStatuses.put(
                        promotion.getPromotionId(),
                        status
                );
            }
        }

        request.setAttribute("promotions", promotions);
        request.setAttribute("displayStatuses", displayStatuses);
        request.setAttribute("keyword", keyword);
        request.setAttribute("filter", filter);
        request.setAttribute("totalRecords", promotions.size());

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/promotions.jsp"
        ).forward(request, response);
    }

    private void showPromotionForm(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Integer cinemaId = getCurrentCinemaId(request);

        if (cinemaId == null || cinemaId <= 0) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Tài khoản chưa được phân công rạp."
            );
            return;
        }

        String idParam = request.getParameter("id");

        if (idParam != null && !idParam.trim().isEmpty()) {

            try {
                int promotionId = parsePositiveInt(
                        idParam,
                        "Mã khuyến mãi không hợp lệ."
                );

                Promotion promotion
                        = promotionService.getPromotionById(
                                promotionId,
                                cinemaId
                        );

                if (promotion == null) {
                    response.sendRedirect(
                            request.getContextPath() + "/admin/promotions"
                    );
                    return;
                }

                request.setAttribute("promotion", promotion);

            } catch (IllegalArgumentException e) {
                response.sendRedirect(
                        request.getContextPath() + "/admin/promotions"
                );
                return;
            }
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/promotion-form.jsp"
        ).forward(request, response);
    }

    private void savePromotion(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Integer cinemaId = getCurrentCinemaId(request);

        if (cinemaId == null || cinemaId <= 0) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Tài khoản chưa được phân công rạp."
            );
            return;
        }

        try {
            Promotion promotion = buildPromotionFromRequest(request);

            promotion.setCinemaId(cinemaId);

            String idParam = request.getParameter("promotionId");

            String error;

            if (idParam == null || idParam.trim().isEmpty()) {

                error = promotionService.createPromotion(
                        promotion,
                        cinemaId
                );

            } else {

                int promotionId = parsePositiveInt(
                        idParam,
                        "Mã khuyến mãi không hợp lệ."
                );

                promotion.setPromotionId(promotionId);

                error = promotionService.updatePromotion(
                        promotion,
                        cinemaId
                );
            }

            if (error != null) {

                request.setAttribute("error", error);
                request.setAttribute("promotion", promotion);

                request.getRequestDispatcher(
                        "/WEB-INF/views/admin/promotion-form.jsp"
                ).forward(request, response);

                return;
            }

            response.sendRedirect(
                    request.getContextPath() + "/admin/promotions"
            );

        } catch (IllegalArgumentException e) {

            request.setAttribute("error", e.getMessage());

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/promotion-form.jsp"
            ).forward(request, response);
        }
    }

    private void changePromotionStatus(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        Integer cinemaId = getCurrentCinemaId(request);

        if (cinemaId == null || cinemaId <= 0) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Tài khoản chưa được phân công rạp."
            );
            return;
        }

        String idParam = request.getParameter("promotionId");
        String status = request.getParameter("status");

        try {
            int promotionId = parsePositiveInt(
                    idParam,
                    "Mã khuyến mãi không hợp lệ."
            );

            String error = promotionService.updateStatus(
                    promotionId,
                    status,
                    cinemaId
            );

            if (error != null) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        error
                );
                return;
            }

            response.sendRedirect(
                    request.getContextPath() + "/admin/promotions"
            );

        } catch (IllegalArgumentException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    private void showApplyPromo(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute(
                "showtimeId",
                request.getParameter("showtimeId")
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/user/apply-promo.jsp"
        ).forward(request, response);
    }

    private void applyPromotion(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String promoCode = request.getParameter("promoCode");

            if (promoCode == null || promoCode.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Vui lòng nhập mã khuyến mãi."
                );
            }

            int showtimeId = parsePositiveInt(
                    request.getParameter("showtimeId"),
                    "Suất chiếu không hợp lệ."
            );

            int cinemaId = promotionService.getCinemaIdByShowtimeId(
                    showtimeId
            );

            if (cinemaId <= 0) {
                throw new IllegalArgumentException(
                        "Không xác định được rạp của suất chiếu."
                );
            }

            BigDecimal orderAmount = parseBigDecimal(
                    request.getParameter("orderAmount"),
                    "Giá trị đơn hàng không hợp lệ."
            );

            if (orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Giá trị đơn hàng phải lớn hơn 0."
                );
            }

            Promotion promotion = promotionService.getPromotionByCode(
                    promoCode,
                    cinemaId
            );

            String error = promotionService.validateApplyPromotion(
                    promotion,
                    orderAmount
            );

            if (error != null) {
                request.setAttribute("error", error);
                request.setAttribute("promoCode", promoCode);
                request.setAttribute("orderAmount", orderAmount);
                request.setAttribute("showtimeId", showtimeId);

                showApplyPromo(request, response);
                return;
            }

            BigDecimal discountAmount
                    = promotionService.calculateDiscount(
                            promotion,
                            orderAmount
                    );

            BigDecimal finalAmount = orderAmount.subtract(discountAmount);

            request.setAttribute(
                    "success",
                    "Áp dụng mã khuyến mãi thành công."
            );

            request.setAttribute("promoCode", promotion.getPromoCode());
            request.setAttribute("orderAmount", orderAmount);
            request.setAttribute("discountAmount", discountAmount);
            request.setAttribute("finalAmount", finalAmount);
            request.setAttribute("appliedPromotion", promotion);
            request.setAttribute("showtimeId", showtimeId);

            showApplyPromo(request, response);

        } catch (IllegalArgumentException e) {

            request.setAttribute("error", e.getMessage());

            request.setAttribute(
                    "promoCode",
                    request.getParameter("promoCode")
            );

            request.setAttribute(
                    "orderAmount",
                    request.getParameter("orderAmount")
            );

            request.setAttribute(
                    "showtimeId",
                    request.getParameter("showtimeId")
            );

            showApplyPromo(request, response);
        }
    }

    private Promotion buildPromotionFromRequest(
            HttpServletRequest request) {

        Promotion promotion = new Promotion();

        promotion.setPromoCode(request.getParameter("promoCode"));
        promotion.setDescription(request.getParameter("description"));
        promotion.setDiscountType(request.getParameter("discountType"));

        promotion.setDiscountValue(
                parseBigDecimal(
                        request.getParameter("discountValue"),
                        "Giá trị giảm không hợp lệ."
                )
        );

        String minOrderAmount = request.getParameter("minOrderAmount");

        if (minOrderAmount == null || minOrderAmount.trim().isEmpty()) {
            promotion.setMinOrderAmount(BigDecimal.ZERO);
        } else {
            promotion.setMinOrderAmount(
                    parseBigDecimal(
                            minOrderAmount,
                            "Giá trị đơn hàng tối thiểu không hợp lệ."
                    )
            );
        }

        String maxDiscountAmount
                = request.getParameter("maxDiscountAmount");

        if (maxDiscountAmount != null
                && !maxDiscountAmount.trim().isEmpty()) {

            promotion.setMaxDiscountAmount(
                    parseBigDecimal(
                            maxDiscountAmount,
                            "Giá trị giảm tối đa không hợp lệ."
                    )
            );
        }

        promotion.setStartDate(
                parseStartDate(
                        request.getParameter("startDate"),
                        "Ngày bắt đầu không hợp lệ."
                )
        );

        promotion.setEndDate(
                parseEndDate(
                        request.getParameter("endDate"),
                        "Ngày kết thúc không hợp lệ."
                )
        );

        String usageLimit = request.getParameter("usageLimit");

        if (usageLimit != null && !usageLimit.trim().isEmpty()) {

            try {
                promotion.setUsageLimit(
                        Integer.valueOf(usageLimit.trim())
                );

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Giới hạn sử dụng không hợp lệ."
                );
            }
        }

        promotion.setStatus(request.getParameter("status"));

        return promotion;
    }

    private int parsePositiveInt(String value, String errorMessage) {

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }

        try {
            int number = Integer.parseInt(value.trim());

            if (number <= 0) {
                throw new NumberFormatException();
            }

            return number;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private BigDecimal parseBigDecimal(
            String value,
            String errorMessage) {

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }

        try {
            return new BigDecimal(value.trim());

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private Timestamp parseStartDate(
            String value,
            String errorMessage) {

        LocalDate date = parseDate(value, errorMessage);

        return Timestamp.valueOf(date.atStartOfDay());
    }

    private Timestamp parseEndDate(
            String value,
            String errorMessage) {

        LocalDate date = parseDate(value, errorMessage);

        return Timestamp.valueOf(date.atTime(23, 59, 59));
    }

    private LocalDate parseDate(
            String value,
            String errorMessage) {

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }

        String input = value.trim();

        try {
            return LocalDate.parse(input);

        } catch (DateTimeParseException e) {

            try {
                DateTimeFormatter formatter
                        = DateTimeFormatter.ofPattern("d/M/yyyy");

                return LocalDate.parse(input, formatter);

            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }
}
