package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Combo;
import model.FoodItem;
import model.FoodOrder;
import model.FoodOrderItem;
import model.User;
import service.ComboService;
import service.FoodOrderService;
import service.FoodService;

@WebServlet(urlPatterns = {
    "/staff/counter",
    "/staff/ticket-sale",
    "/staff/food-sale"
})
public class CounterSaleController extends HttpServlet {

    private final FoodOrderService foodOrderService = new FoodOrderService();
    private final FoodService foodService = new FoodService();
    private final ComboService comboService = new ComboService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User staff = getStaff(request, response);
        if (staff == null) {
            return;
        }

        String path = request.getServletPath();
        switch (path) {
            case "/staff/counter":
                request.getRequestDispatcher("/WEB-INF/views/staff/counter.jsp")
                        .forward(request, response);
                break;
            case "/staff/ticket-sale":
                request.getRequestDispatcher("/WEB-INF/views/staff/ticket-sale.jsp")
                        .forward(request, response);
                break;
            case "/staff/food-sale":
                showFoodSale(request, response, staff);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        if (!"/staff/food-sale".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        createFoodOrder(request, response);
    }

    private void showFoodSale(HttpServletRequest request, HttpServletResponse response, User staff)
            throws ServletException, IOException {
        String orderIdParam = request.getParameter("orderId");

        if (orderIdParam != null && !orderIdParam.trim().isEmpty()) {
            try {
                int orderId = Integer.parseInt(orderIdParam);
                FoodOrder order = foodOrderService.getOrderById(orderId);

                if (order != null && order.getCinemaId() == staff.getCinemaId()) {
                    request.setAttribute("completedOrder", order);
                    request.setAttribute("completedItems", foodOrderService.getOrderItems(orderId));
                    request.setAttribute("success", "Thanh toán đơn hàng thành công.");
                } else {
                    request.setAttribute("error", "Không tìm thấy đơn hàng trong rạp của bạn.");
                }
            } catch (NumberFormatException e) {
                request.setAttribute("error", "Mã đơn hàng không hợp lệ.");
            }
        }

        request.setAttribute("foodItems", foodService.getActiveItemsByCinema(staff.getCinemaId()));
        request.setAttribute("combos", comboService.getActiveCombosByCinema(staff.getCinemaId()));
        request.getRequestDispatcher("/WEB-INF/views/staff/food-sale.jsp")
                .forward(request, response);
    }

    private void createFoodOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User staff = getStaff(request, response);
        if (staff == null) {
            return;
        }

        try {
            List<FoodOrderItem> items = buildOrderItems(request, staff.getCinemaId());

            FoodOrder foodOrder = new FoodOrder();
            foodOrder.setStaffId(staff.getUserId());
            foodOrder.setCinemaId(staff.getCinemaId());
            foodOrder.setPaymentMethod(request.getParameter("paymentMethod"));

            int foodOrderId = foodOrderService.createPaidOrder(foodOrder, items);
            if (foodOrderId <= 0) {
                throw new IllegalStateException("Không thể tạo đơn hàng. Vui lòng thử lại.");
            }

            response.sendRedirect(request.getContextPath()
                    + "/staff/food-sale?orderId=" + foodOrderId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            showFoodSale(request, response, staff);
        }
    }

    /**
     * Prices are always read from the database. Values sent by the browser are
     * used only for product IDs and quantities, never as the sale price.
     */
    private List<FoodOrderItem> buildOrderItems(HttpServletRequest request, int cinemaId) {
        String[] itemTypes = request.getParameterValues("itemType");
        String[] itemIds = request.getParameterValues("itemId");
        String[] quantities = request.getParameterValues("quantity");

        if (itemTypes == null || itemIds == null || quantities == null) {
            throw new IllegalArgumentException("Đơn hàng chưa có sản phẩm.");
        }
        if (itemTypes.length == 0 || itemTypes.length != itemIds.length
                || itemTypes.length != quantities.length) {
            throw new IllegalArgumentException("Dữ liệu đơn hàng không hợp lệ.");
        }

        List<FoodOrderItem> items = new ArrayList<>();
        Set<String> uniqueProducts = new HashSet<>();

        for (int i = 0; i < itemTypes.length; i++) {
            int itemId = parsePositiveInt(itemIds[i], "Sản phẩm không hợp lệ.");
            int quantity = parsePositiveInt(quantities[i], "Số lượng sản phẩm không hợp lệ.");
            String itemType = itemTypes[i];
            String productKey = itemType + ":" + itemId;

            if (!uniqueProducts.add(productKey)) {
                throw new IllegalArgumentException("Không được gửi trùng sản phẩm trong đơn hàng.");
            }

            FoodOrderItem orderItem = new FoodOrderItem();
            if ("FOOD".equals(itemType)) {
                FoodItem foodItem = foodService.getActiveItem(itemId, cinemaId);
                if (foodItem == null) {
                    throw new IllegalArgumentException(
                            "Món ăn/thức uống đã ngừng bán hoặc không thuộc rạp của bạn.");
                }
                orderItem.setFoodItemId(itemId);
                orderItem.setUnitPrice(foodItem.getPrice());
            } else if ("COMBO".equals(itemType)) {
                Combo combo = comboService.getActiveComboForSale(itemId, cinemaId);
                if (combo == null) {
                    throw new IllegalArgumentException(
                            "Combo đã ngừng bán, chưa có món hoặc có món không còn kinh doanh.");
                }
                orderItem.setComboId(itemId);
                orderItem.setUnitPrice(combo.getComboPrice());
            } else {
                throw new IllegalArgumentException("Loại sản phẩm không hợp lệ.");
            }

            orderItem.setQuantity(quantity);
            items.add(orderItem);
        }
        return items;
    }

    private int parsePositiveInt(String value, String errorMessage) {
        try {
            int number = Integer.parseInt(value);
            if (number <= 0) {
                throw new NumberFormatException();
            }
            return number;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private User getStaff(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return null;
        }

        if (!"STAFF".equals(session.getAttribute("roleName"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        User user = (User) session.getAttribute("user");
        if (user.getCinemaId() == null || user.getCinemaId() <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Tài khoản nhân viên chưa được phân công rạp.");
            return null;
        }
        return user;
    }
}
