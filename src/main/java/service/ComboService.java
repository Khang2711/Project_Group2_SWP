package service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Combo;
import model.ComboItem;
import model.FoodItem;
import repository.ComboItemRepository;
import repository.ComboRepository;
import repository.FoodItemRepository;

/** Nghiệp vụ quản lý combo đồ ăn/uống. */
public class ComboService {

    private final ComboRepository comboRepository = new ComboRepository();
    private final ComboItemRepository comboItemRepository = new ComboItemRepository();
    private final FoodItemRepository foodItemRepository = new FoodItemRepository();

    public List<Combo> getCombosByCinema(int cinemaId) {
        requireCinema(cinemaId);
        return comboRepository.findByCinema(cinemaId);
    }

    public List<Combo> getActiveCombosByCinema(int cinemaId) {
        requireCinema(cinemaId);
        return comboRepository.findActiveForSale(cinemaId);
    }

    public Combo getCombo(int comboId, int cinemaId) {
        requireCinema(cinemaId);
        if (comboId <= 0) {
            return null;
        }
        return comboRepository.findById(comboId, cinemaId);
    }

    public Combo getActiveComboForSale(int comboId, int cinemaId) {
        requireCinema(cinemaId);
        if (comboId <= 0) {
            return null;
        }
        return comboRepository.findActiveForSaleById(comboId, cinemaId);
    }

    public List<ComboItem> getComboItems(int comboId, int cinemaId) {
        requireCinema(cinemaId);
        if (comboRepository.findById(comboId, cinemaId) == null) {
            return java.util.Collections.emptyList();
        }
        return comboItemRepository.findByComboId(comboId);
    }

    public void saveCombo(Combo combo, List<ComboItem> items) {
        if (combo == null) {
            throw new IllegalArgumentException("Thông tin combo không hợp lệ.");
        }
        requireCinema(combo.getCinemaId());

        String name = trim(combo.getComboName());
        if (name == null) {
            throw new IllegalArgumentException("Tên combo không được để trống.");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Tên combo tối đa 100 ký tự.");
        }

        String description = trim(combo.getDescription());
        if (description != null && description.length() > 255) {
            throw new IllegalArgumentException("Mô tả combo tối đa 255 ký tự.");
        }

        String imageUrl = trim(combo.getImageUrl());
        if (imageUrl != null && imageUrl.length() > 500) {
            throw new IllegalArgumentException("Đường dẫn hình ảnh tối đa 500 ký tự.");
        }

        if (combo.getComboPrice() == null
                || combo.getComboPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá combo phải lớn hơn hoặc bằng 0.");
        }

        String status = trim(combo.getStatus());
        if (status == null) {
            status = "ACTIVE";
        }
        status = status.toUpperCase();
        validateStatus(status);

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Combo phải có ít nhất một món.");
        }

        Set<Integer> seenFoodIds = new HashSet<>();
        for (ComboItem item : items) {
            if (item == null || item.getFoodItemId() <= 0) {
                throw new IllegalArgumentException("Sản phẩm trong combo không hợp lệ.");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng mỗi món trong combo phải lớn hơn 0.");
            }
            if (!seenFoodIds.add(item.getFoodItemId())) {
                throw new IllegalArgumentException("Không được chọn trùng một món trong combo.");
            }
            FoodItem foodItem = foodItemRepository.findActiveById(
                    item.getFoodItemId(), combo.getCinemaId());
            if (foodItem == null) {
                throw new IllegalArgumentException(
                        "Món trong combo phải đang hoạt động và thuộc cùng rạp.");
            }
        }

        if (combo.getComboId() > 0
                && comboRepository.findById(combo.getComboId(), combo.getCinemaId()) == null) {
            throw new IllegalArgumentException("Không tìm thấy combo trong rạp này.");
        }

        combo.setComboName(name);
        combo.setDescription(description);
        combo.setImageUrl(imageUrl);
        combo.setStatus(status);

        if (!comboRepository.saveWithItems(combo, items)) {
            throw new IllegalStateException(
                    "Không thể lưu combo. Vui lòng kiểm tra kết nối database và các món đã chọn.");
        }
    }

    public void changeStatus(int comboId, int cinemaId, String status) {
        requireCinema(cinemaId);
        Combo combo = comboRepository.findById(comboId, cinemaId);
        if (combo == null) {
            throw new IllegalArgumentException("Không tìm thấy combo trong rạp này.");
        }

        String normalizedStatus = trim(status);
        if (normalizedStatus == null) {
            throw new IllegalArgumentException("Trạng thái combo không hợp lệ.");
        }
        normalizedStatus = normalizedStatus.toUpperCase();
        validateStatus(normalizedStatus);

        if ("ACTIVE".equals(normalizedStatus)
                && !comboItemRepository.hasOnlyActiveItems(comboId, cinemaId)) {
            throw new IllegalArgumentException(
                    "Không thể kích hoạt combo vì combo chưa có món hoặc có món đã ngừng kinh doanh.");
        }

        if (!comboRepository.updateStatus(comboId, cinemaId, normalizedStatus)) {
            throw new IllegalStateException("Không thể cập nhật trạng thái combo.");
        }
    }

    private void validateStatus(String status) {
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            throw new IllegalArgumentException("Trạng thái chỉ được là ACTIVE hoặc INACTIVE.");
        }
    }

    private void requireCinema(int cinemaId) {
        if (cinemaId <= 0) {
            throw new IllegalArgumentException("Rạp không hợp lệ.");
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }
}
