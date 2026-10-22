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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Combo;
import model.ComboItem;
import model.FoodItem;
import model.User;
import service.ComboService;
import service.FoodService;
import util.ComboImageStorage;

@MultipartConfig(
        maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 8L * 1024 * 1024
)
@WebServlet(urlPatterns = {
    "/admin/combos",
    "/admin/combo-form",
    "/admin/combo-status"
})
public class ComboController extends HttpServlet {

    private final ComboService comboService = new ComboService();
    private final FoodService foodService = new FoodService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User manager = getManager(request, response);
        if (manager == null) {
            return;
        }

        String path = request.getServletPath();
        if ("/admin/combos".equals(path)) {
            showList(request, response, manager);
            return;
        }

        if ("/admin/combo-form".equals(path)) {
            Combo combo;
            Map<Integer, Integer> selectedItems = new HashMap<>();
            String id = request.getParameter("id");

            if (id == null || id.trim().isEmpty()) {
                combo = new Combo(0, manager.getCinemaId(), null, null,
                        BigDecimal.ZERO, null, "ACTIVE");
            } else {
                try {
                    int comboId = Integer.parseInt(id);
                    combo = comboService.getCombo(comboId, manager.getCinemaId());
                    if (combo == null) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND,
                                "Không tìm thấy combo trong rạp này.");
                        return;
                    }
                    for (ComboItem item : comboService.getComboItems(comboId, manager.getCinemaId())) {
                        selectedItems.put(item.getFoodItemId(), item.getQuantity());
                    }
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã combo không hợp lệ.");
                    return;
                }
            }

            request.setAttribute("combo", combo);
            request.setAttribute("selectedItems", selectedItems);
            request.setAttribute("foodItems", foodService.getItemsByCinema(manager.getCinemaId()));
            request.getRequestDispatcher("/WEB-INF/views/admin/combo-form.jsp")
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
        if ("/admin/combo-status".equals(path)) {
            try {
                int comboId = Integer.parseInt(request.getParameter("id"));
                comboService.changeStatus(comboId, manager.getCinemaId(),
                        request.getParameter("status"));
                response.sendRedirect(request.getContextPath() + "/admin/combos?statusUpdated=1");
            } catch (NumberFormatException e) {
                request.setAttribute("error", "Mã combo không hợp lệ.");
                showList(request, response, manager);
            } catch (IllegalArgumentException | IllegalStateException e) {
                request.setAttribute("error", e.getMessage());
                showList(request, response, manager);
            }
            return;
        }

        if (!"/admin/combo-form".equals(path)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Combo combo = new Combo();
        combo.setCinemaId(manager.getCinemaId());
        List<ComboItem> items = new ArrayList<>();
        String existingImageUrl = null;
        String uploadedImageUrl = null;

        try {
            combo.setComboName(request.getParameter("comboName"));
            combo.setDescription(request.getParameter("description"));
            combo.setStatus(request.getParameter("status"));

            String id = request.getParameter("id");
            if (id != null && !id.trim().isEmpty()) {
                combo.setComboId(Integer.parseInt(id));
                Combo existingCombo = comboService.getCombo(
                        combo.getComboId(), manager.getCinemaId());
                if (existingCombo == null) {
                    throw new IllegalArgumentException(
                            "Không tìm thấy combo trong rạp này.");
                }
                existingImageUrl = existingCombo.getImageUrl();
                combo.setImageUrl(existingImageUrl);
            }

            String price = request.getParameter("comboPrice");
            if (price == null || price.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập giá bán combo.");
            }
            combo.setComboPrice(new BigDecimal(price.trim()));

            items = readComboItems(request);
            if (items.isEmpty()) {
                throw new IllegalArgumentException("Combo phải có ít nhất một món.");
            }

            Part imagePart = request.getPart("comboImage");
            if (imagePart != null && imagePart.getSize() > 0) {
                try {
                    uploadedImageUrl = ComboImageStorage.saveComboImage(imagePart);
                } catch (IOException e) {
                    throw new IllegalStateException(
                            "Không thể lưu ảnh combo. Vui lòng thử lại.", e);
                }
                if (uploadedImageUrl != null) {
                    combo.setImageUrl(uploadedImageUrl);
                }
            }

            comboService.saveCombo(combo, items);
            response.sendRedirect(request.getContextPath() + "/admin/combos?saved=1");
        } catch (NumberFormatException e) {
            ComboImageStorage.deleteUploadedImage(uploadedImageUrl);
            combo.setImageUrl(existingImageUrl);
            request.setAttribute("error", "Mã combo, giá bán hoặc số lượng không hợp lệ.");
            forwardForm(request, response, manager, combo, items);
        } catch (IllegalArgumentException | IllegalStateException e) {
            ComboImageStorage.deleteUploadedImage(uploadedImageUrl);
            combo.setImageUrl(existingImageUrl);
            request.setAttribute("error", e.getMessage());
            forwardForm(request, response, manager, combo, items);
        }
    }

    private List<ComboItem> readComboItems(HttpServletRequest request) {
        String[] foodIds = request.getParameterValues("foodItemId");
        List<ComboItem> items = new ArrayList<>();
        if (foodIds == null) {
            return items;
        }

        Set<Integer> seen = new HashSet<>();
        for (String foodIdValue : foodIds) {
            int foodId;
            try {
                foodId = Integer.parseInt(foodIdValue);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Mã món trong combo không hợp lệ.");
            }
            if (foodId <= 0 || !seen.add(foodId)) {
                throw new IllegalArgumentException("Món trong combo không hợp lệ hoặc bị chọn trùng.");
            }

            String quantityValue = request.getParameter("quantity_" + foodId);
            int quantity;
            try {
                quantity = Integer.parseInt(quantityValue);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Số lượng mỗi món trong combo không hợp lệ.");
            }

            items.add(new ComboItem(0, foodId, quantity));
        }
        return items;
    }

    private void forwardForm(HttpServletRequest request, HttpServletResponse response,
            User manager, Combo combo, List<ComboItem> items)
            throws ServletException, IOException {
        Map<Integer, Integer> selectedItems = new HashMap<>();
        if (items != null) {
            for (ComboItem item : items) {
                if (item != null && item.getFoodItemId() > 0) {
                    selectedItems.put(item.getFoodItemId(), item.getQuantity());
                }
            }
        }
        request.setAttribute("combo", combo);
        request.setAttribute("selectedItems", selectedItems);
        request.setAttribute("foodItems", foodService.getItemsByCinema(manager.getCinemaId()));
        request.getRequestDispatcher("/WEB-INF/views/admin/combo-form.jsp")
                .forward(request, response);
    }

    private void showList(HttpServletRequest request, HttpServletResponse response, User manager)
            throws ServletException, IOException {
        List<Combo> combos = comboService.getCombosByCinema(manager.getCinemaId());
        Map<Integer, List<ComboItem>> comboItemsByComboId = new HashMap<>();
        Map<Integer, String> foodItemNames = new HashMap<>();

        for (Combo combo : combos) {
            comboItemsByComboId.put(combo.getComboId(),
                    comboService.getComboItems(combo.getComboId(), manager.getCinemaId()));
        }
        for (FoodItem item : foodService.getItemsByCinema(manager.getCinemaId())) {
            foodItemNames.put(item.getFoodItemId(), item.getItemName());
        }

        request.setAttribute("combos", combos);
        request.setAttribute("comboItemsByComboId", comboItemsByComboId);
        request.setAttribute("foodItemNames", foodItemNames);
        request.getRequestDispatcher("/WEB-INF/views/admin/combos.jsp").forward(request, response);
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
