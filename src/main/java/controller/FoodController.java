package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.math.BigDecimal;
import model.FoodItem;
import model.User;
import service.FoodService;
import util.FoodImageStorage;

@MultipartConfig(
        maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 8L * 1024 * 1024
)
@WebServlet(urlPatterns = {
    "/admin/food-items",
    "/admin/food-item-form",
    "/admin/food-item-status"
})
public class FoodController extends HttpServlet {

    private final FoodService foodService = new FoodService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User manager = getManager(request, response);
        if (manager == null) {
            return;
        }

        String path = request.getServletPath();
        if ("/admin/food-items".equals(path)) {
            showList(request, response, manager);
            return;
        }

        if ("/admin/food-item-form".equals(path)) {
            String id = request.getParameter("id");
            FoodItem item;

            if (id == null || id.trim().isEmpty()) {
                item = new FoodItem();
                item.setCinemaId(manager.getCinemaId());
                item.setItemType("FOOD");
                item.setStatus("ACTIVE");
            } else {
                try {
                    int foodItemId = Integer.parseInt(id);
                    item = foodService.getItem(foodItemId, manager.getCinemaId());
                    if (item == null) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND,
                                "Không tìm thấy sản phẩm trong rạp này.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Mã sản phẩm không hợp lệ.");
                    return;
                }
            }

            request.setAttribute("foodItem", item);
            request.getRequestDispatcher("/WEB-INF/views/admin/food-item-form.jsp")
                    .forward(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User manager = getManager(request, response);
        if (manager == null) {
            return;
        }

        String path = request.getServletPath();
        if ("/admin/food-item-status".equals(path)) {
            try {
                int id = Integer.parseInt(request.getParameter("id"));
                String status = request.getParameter("status");
                foodService.changeStatus(id, manager.getCinemaId(), status);
                response.sendRedirect(request.getContextPath() + "/admin/food-items?statusUpdated=1");
            } catch (NumberFormatException e) {
                request.setAttribute("error", "Mã sản phẩm không hợp lệ.");
                showList(request, response, manager);
            } catch (IllegalArgumentException | IllegalStateException e) {
                request.setAttribute("error", e.getMessage());
                showList(request, response, manager);
            }
            return;
        }

        if (!"/admin/food-item-form".equals(path)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        FoodItem item = new FoodItem();
        item.setCinemaId(manager.getCinemaId());
        item.setItemName(request.getParameter("itemName"));
        item.setItemType(request.getParameter("itemType"));
        item.setDescription(request.getParameter("description"));
        item.setStatus(request.getParameter("status"));

        String existingImageUrl = null;
        String uploadedImageUrl = null;

        try {
            String id = request.getParameter("id");
            if (id != null && !id.trim().isEmpty()) {
                int foodItemId = Integer.parseInt(id);
                item.setFoodItemId(foodItemId);

                FoodItem existingItem = foodService.getItem(
                        foodItemId, manager.getCinemaId());
                if (existingItem == null) {
                    throw new IllegalArgumentException(
                            "Không tìm thấy sản phẩm trong rạp này.");
                }
                existingImageUrl = existingItem.getImageUrl();
                item.setImageUrl(existingImageUrl);
            }

            String price = request.getParameter("price");
            if (price == null || price.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập giá bán.");
            }
            item.setPrice(new BigDecimal(price.trim()));

            Part imagePart = request.getPart("foodImage");
            if (imagePart != null && imagePart.getSize() > 0) {
                try {
                    uploadedImageUrl = FoodImageStorage.saveFoodImage(imagePart);
                } catch (IOException e) {
                    throw new IllegalStateException(
                            "Không thể lưu ảnh sản phẩm. Vui lòng thử lại.", e);
                }
                if (uploadedImageUrl != null) {
                    item.setImageUrl(uploadedImageUrl);
                }
            }

            foodService.saveItem(item);
            response.sendRedirect(request.getContextPath() + "/admin/food-items?saved=1");
        } catch (NumberFormatException e) {
            FoodImageStorage.deleteUploadedImage(uploadedImageUrl);
            item.setImageUrl(existingImageUrl);
            request.setAttribute("error", "Mã sản phẩm hoặc giá bán không hợp lệ.");
            request.setAttribute("foodItem", item);
            request.getRequestDispatcher("/WEB-INF/views/admin/food-item-form.jsp")
                    .forward(request, response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            FoodImageStorage.deleteUploadedImage(uploadedImageUrl);
            item.setImageUrl(existingImageUrl);
            request.setAttribute("error", e.getMessage());
            request.setAttribute("foodItem", item);
            request.getRequestDispatcher("/WEB-INF/views/admin/food-item-form.jsp")
                    .forward(request, response);
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response, User manager)
            throws ServletException, IOException {
        request.setAttribute("foodItems", foodService.getItemsByCinema(manager.getCinemaId()));
        request.getRequestDispatcher("/WEB-INF/views/admin/food-items.jsp").forward(request, response);
    }

    private User getManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return null;
        }

        if (!"CINEMA_MANAGER".equals(session.getAttribute("roleName"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        User user = (User) session.getAttribute("user");
        if (user.getCinemaId() == null || user.getCinemaId() <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Tài khoản Manager chưa được phân công rạp.");
            return null;
        }
        return user;
    }
}
