package service;

import java.math.BigDecimal;
import java.util.List;
import model.FoodItem;
import repository.FoodItemRepository;

/** Nghiệp vụ quản lý món ăn/uống. */
public class FoodService {

    private final FoodItemRepository repository = new FoodItemRepository();

    public List<FoodItem> getItemsByCinema(int cinemaId) {
        requireCinema(cinemaId);
        return repository.findByCinema(cinemaId);
    }

    public List<FoodItem> getActiveItemsByCinema(int cinemaId) {
        requireCinema(cinemaId);
        return repository.findActiveByCinema(cinemaId);
    }

    public FoodItem getItem(int foodItemId, int cinemaId) {
        requireCinema(cinemaId);
        if (foodItemId <= 0) {
            return null;
        }
        return repository.findById(foodItemId, cinemaId);
    }

    public FoodItem getActiveItem(int foodItemId, int cinemaId) {
        requireCinema(cinemaId);
        if (foodItemId <= 0) {
            return null;
        }
        return repository.findActiveById(foodItemId, cinemaId);
    }

    public void saveItem(FoodItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Thông tin sản phẩm không hợp lệ.");
        }
        requireCinema(item.getCinemaId());

        String name = trim(item.getItemName());
        if (name == null) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Tên sản phẩm tối đa 100 ký tự.");
        }

        String type = trim(item.getItemType());
        if (type == null) {
            throw new IllegalArgumentException("Vui lòng chọn loại sản phẩm.");
        }
        type = type.toUpperCase();
        if (!"FOOD".equals(type) && !"DRINK".equals(type)) {
            throw new IllegalArgumentException("Loại sản phẩm chỉ được là món ăn hoặc thức uống.");
        }

        String description = trim(item.getDescription());
        if (description != null && description.length() > 255) {
            throw new IllegalArgumentException("Mô tả tối đa 255 ký tự.");
        }

        String imageUrl = trim(item.getImageUrl());
        if (imageUrl != null && imageUrl.length() > 500) {
            throw new IllegalArgumentException("Đường dẫn hình ảnh tối đa 500 ký tự.");
        }

        if (item.getPrice() == null
                || item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá bán phải lớn hơn hoặc bằng 0.");
        }

        String status = trim(item.getStatus());
        if (status == null) {
            status = "ACTIVE";
        }
        status = status.toUpperCase();
        validateStatus(status);

        item.setItemName(name);
        item.setItemType(type);
        item.setDescription(description);
        item.setImageUrl(imageUrl);
        item.setStatus(status);

        boolean saved;
        if (item.getFoodItemId() <= 0) {
            saved = repository.insert(item);
        } else {
            if (repository.findById(item.getFoodItemId(), item.getCinemaId()) == null) {
                throw new IllegalArgumentException("Không tìm thấy sản phẩm trong rạp này.");
            }
            saved = repository.update(item);
        }

        if (!saved) {
            throw new IllegalStateException("Không thể lưu sản phẩm. Vui lòng kiểm tra kết nối database.");
        }
    }

    public void changeStatus(int foodItemId, int cinemaId, String status) {
        requireCinema(cinemaId);
        if (foodItemId <= 0 || repository.findById(foodItemId, cinemaId) == null) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm trong rạp này.");
        }
        String normalizedStatus = trim(status);
        if (normalizedStatus == null) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ.");
        }
        normalizedStatus = normalizedStatus.toUpperCase();
        validateStatus(normalizedStatus);

        if (!repository.updateStatus(foodItemId, cinemaId, normalizedStatus)) {
            throw new IllegalStateException("Không thể cập nhật trạng thái sản phẩm.");
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
