package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import model.User;
import service.PaymentService;
import service.PaymentService.PaymentResult;

/**
 * UC-13 VNPay controller.
 * The return URL is for the customer's browser; the IPN URL is for VNPay
 * server-to-server notifications. Both use the same signature-checked service.
 */
@WebServlet(urlPatterns = {
    "/user/payment",
    "/user/payment-result",
    "/payment/vnpay-return",
    "/payment/vnpay-ipn"
})
public class PaymentController extends HttpServlet {

    private PaymentService paymentService;

    @Override
    public void init() {
        paymentService = new PaymentService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        String path = request.getServletPath();

        switch (path) {
            case "/user/payment":
                startPayment(request, response);
                break;

            case "/payment/vnpay-return":
            case "/user/payment-result":
                if (request.getParameter("vnp_TxnRef") == null) {
                    response.sendRedirect(
                            request.getContextPath() + "/user/bookings"
                    );
                    return;
                }
                showBrowserResult(request, response);
                break;

            case "/payment/vnpay-ipn":
                handleIpn(request, response);
                break;

            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    private void startPayment(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("user") instanceof User)
                || !"CUSTOMER".equals(session.getAttribute("roleName"))) {
            response.sendRedirect(
                    request.getContextPath() + "/auth/login"
            );
            return;
        }

        User user = (User) session.getAttribute("user");

        try {
            int bookingId = parsePositiveInt(request.getParameter("bookingId"));
            String paymentUrl = paymentService.createPaymentUrl(
                    bookingId,
                    user.getUserId(),
                    request.getRemoteAddr()
            );

            response.sendRedirect(paymentUrl);

        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher(
                    "/WEB-INF/views/user/payment.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            log("Cannot start VNPay payment.", e);
            request.setAttribute(
                    "error",
                    "Không thể tạo giao dịch thanh toán. Vui lòng thử lại sau."
            );
            request.getRequestDispatcher(
                    "/WEB-INF/views/user/payment.jsp"
            ).forward(request, response);
        }
    }

    private void showBrowserResult(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");

        try {
            PaymentResult result = paymentService.processCallback(
                    readVnPayParameters(request)
            );
            request.setAttribute("paymentResult", result);

        } catch (SQLException e) {
            log("Cannot process VNPay browser return.", e);
            request.setAttribute(
                    "error",
                    "Máy chủ chưa thể ghi nhận kết quả thanh toán. "
                    + "Đừng thanh toán lại ngay; hãy kiểm tra đơn đặt vé hoặc liên hệ nhân viên."
            );
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/user/payment-result.jsp"
        ).forward(request, response);
    }

    private void handleIpn(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        String code;
        String message;

        try {
            PaymentResult result = paymentService.processCallback(
                    readVnPayParameters(request)
            );
            code = result.getIpnResponseCode();
            message = result.getIpnMessage();

        } catch (SQLException e) {
            log("Cannot process VNPay IPN.", e);
            code = "99";
            message = "Unknown error";
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"RspCode\":\"" + escapeJson(code)
                + "\",\"Message\":\"" + escapeJson(message) + "\"}"
        );
    }

    private Map<String, String> readVnPayParameters(
            HttpServletRequest request
    ) {
        Map<String, String> params = new LinkedHashMap<>();

        for (Map.Entry<String, String[]> entry
                : request.getParameterMap().entrySet()) {
            String key = entry.getKey();

            if (key == null || !key.startsWith("vnp_")) {
                continue;
            }

            String[] values = entry.getValue();

            // Reject duplicated query parameters rather than choosing one value.
            if (values == null || values.length != 1) {
                return Collections.emptyMap();
            }

            params.put(key, values[0]);
        }

        return params;
    }

    private int parsePositiveInt(String value) {
        try {
            int number = Integer.parseInt(value);
            if (number > 0) {
                return number;
            }
        } catch (NumberFormatException | NullPointerException e) {
            // Return the same validation error for missing and malformed IDs.
        }

        throw new IllegalArgumentException("Mã đơn đặt vé không hợp lệ.");
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
