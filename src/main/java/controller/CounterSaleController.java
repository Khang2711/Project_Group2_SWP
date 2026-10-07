package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import model.FoodOrder;
import model.FoodOrderItem;
import model.User;
import service.FoodOrderService;

@WebServlet(urlPatterns = {
    "/staff/counter",
    "/staff/ticket-sale",
    "/staff/food-sale"
})
public class CounterSaleController extends HttpServlet {

    private FoodOrderService foodOrderService;

    @Override
    public void init() {
        foodOrderService = new FoodOrderService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        switch (path) {

            case "/staff/counter":
                request.getRequestDispatcher(
                        "/WEB-INF/views/staff/counter.jsp"
                ).forward(request, response);
                break;

            case "/staff/ticket-sale":
                request.getRequestDispatcher(
                        "/WEB-INF/views/staff/ticket-sale.jsp"
                ).forward(request, response);
                break;

            case "/staff/food-sale":
                showFoodSale(request, response);
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

        request.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();

        if ("/staff/food-sale".equals(path)) {
            createFoodOrder(request, response);
        } else {
            response.sendError(
                    HttpServletResponse.SC_METHOD_NOT_ALLOWED
            );
        }
    }

    private void showFoodSale(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String orderIdParam = request.getParameter("orderId");

        if (orderIdParam != null
                && !orderIdParam.trim().isEmpty()) {

            try {
                int orderId = Integer.parseInt(orderIdParam);

                FoodOrder order
                        = foodOrderService.getOrderById(orderId);

                if (order != null) {

                    request.setAttribute(
                            "completedOrder",
                            order
                    );

                    request.setAttribute(
                            "completedItems",
                            foodOrderService.getOrderItems(orderId)
                    );

                    request.setAttribute(
                            "success",
                            "Thanh toán đơn hàng thành công."
                    );
                }

            } catch (NumberFormatException e) {
                // Ignore invalid order id
            }
        }

        // UC-26 will provide active Food/Combo lists here later.
        request.getRequestDispatcher(
                "/WEB-INF/views/staff/food-sale.jsp"
        ).forward(request, response);
    }

    private void createFoodOrder(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {
            HttpSession session = request.getSession(false);

            if (session == null
                    || session.getAttribute("user") == null) {

                response.sendRedirect(
                        request.getContextPath() + "/auth/login"
                );
                return;
            }

            User user = (User) session.getAttribute("user");

            if (user.getCinemaId() == null
                    || user.getCinemaId() <= 0) {

                throw new IllegalArgumentException(
                        "Nhân viên chưa được phân công rạp."
                );
            }

            List<FoodOrderItem> items
                    = buildOrderItems(request);

            FoodOrder foodOrder = new FoodOrder();

            foodOrder.setStaffId(
                    user.getUserId()
            );

            foodOrder.setCinemaId(
                    user.getCinemaId()
            );

            foodOrder.setPaymentMethod(
                    request.getParameter("paymentMethod")
            );

            int foodOrderId
                    = foodOrderService.createPaidOrder(
                            foodOrder,
                            items
                    );

            response.sendRedirect(
                    request.getContextPath()
                    + "/staff/food-sale?orderId="
                    + foodOrderId
            );

        } catch (IllegalArgumentException
                | IllegalStateException e) {

            request.setAttribute(
                    "error",
                    e.getMessage()
            );

            showFoodSale(request, response);
        }
    }

    private List<FoodOrderItem> buildOrderItems(
            HttpServletRequest request) {

        String[] itemTypes
                = request.getParameterValues("itemType");

        String[] itemIds
                = request.getParameterValues("itemId");

        String[] quantities
                = request.getParameterValues("quantity");

        String[] unitPrices
                = request.getParameterValues("unitPrice");

        if (itemTypes == null
                || itemIds == null
                || quantities == null
                || unitPrices == null) {

            throw new IllegalArgumentException(
                    "Đơn hàng chưa có sản phẩm."
            );
        }

        if (itemTypes.length != itemIds.length
                || itemTypes.length != quantities.length
                || itemTypes.length != unitPrices.length) {

            throw new IllegalArgumentException(
                    "Dữ liệu đơn hàng không hợp lệ."
            );
        }

        List<FoodOrderItem> items = new ArrayList<>();

        for (int i = 0; i < itemTypes.length; i++) {

            FoodOrderItem item = new FoodOrderItem();

            int itemId = parsePositiveInt(
                    itemIds[i],
                    "Sản phẩm không hợp lệ."
            );

            int quantity = parsePositiveInt(
                    quantities[i],
                    "Số lượng sản phẩm không hợp lệ."
            );

            BigDecimal unitPrice = parsePrice(
                    unitPrices[i]
            );

            if ("FOOD".equals(itemTypes[i])) {

                item.setFoodItemId(itemId);

            } else if ("COMBO".equals(itemTypes[i])) {

                item.setComboId(itemId);

            } else {

                throw new IllegalArgumentException(
                        "Loại sản phẩm không hợp lệ."
                );
            }

            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);

            items.add(item);
        }

        return items;
    }

    private int parsePositiveInt(
            String value,
            String errorMessage) {

        try {
            int number = Integer.parseInt(value);

            if (number <= 0) {
                throw new NumberFormatException();
            }

            return number;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    errorMessage
            );
        }
    }

    private BigDecimal parsePrice(String value) {

        try {
            BigDecimal price = new BigDecimal(value);

            if (price.compareTo(BigDecimal.ZERO) < 0) {
                throw new NumberFormatException();
            }

            return price;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Giá sản phẩm không hợp lệ."
            );
        }
    }
}
