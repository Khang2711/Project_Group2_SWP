package service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import model.Promotion;
import repository.PromotionRepository;

public class PromotionService {

    private final PromotionRepository promotionRepository = new PromotionRepository();

    public List<Promotion> getAllPromotions() {
        return promotionRepository.getAll();
    }

    public List<Promotion> getPagedPromotions(
            String keyword,
            String filter,
            int page,
            int pageSize) {

        if (keyword == null) {
            keyword = "";
        }

        keyword = keyword.trim();

        if (filter == null || filter.trim().isEmpty()) {
            filter = "ALL";
        }

        filter = filter.trim().toUpperCase();

        if (page < 1) {
            page = 1;
        }

        return promotionRepository.getPaged(
                keyword,
                filter,
                page,
                pageSize
        );
    }

    public int countPromotions(String keyword, String filter) {

        if (keyword == null) {
            keyword = "";
        }

        keyword = keyword.trim();

        if (filter == null || filter.trim().isEmpty()) {
            filter = "ALL";
        }

        filter = filter.trim().toUpperCase();

        return promotionRepository.countFiltered(
                keyword,
                filter
        );
    }

    public int getTotalPages(
            String keyword,
            String filter,
            int pageSize) {

        int totalRecords = countPromotions(
                keyword,
                filter
        );

        // Add pageSize - 1 before division to round up the last page
        return (totalRecords + pageSize - 1) / pageSize;
    }

    public String getDisplayStatus(Promotion promotion) {

        if ("INACTIVE".equalsIgnoreCase(
                promotion.getStatus())) {
            return "INACTIVE";
        }

        Timestamp now = new Timestamp(
                System.currentTimeMillis()
        );

        // ACTIVE in database can still be expired or not started yet
        if (promotion.getStartDate() != null
                && now.before(promotion.getStartDate())) {
            return "UPCOMING";
        }

        if (promotion.getEndDate() != null
                && now.after(promotion.getEndDate())) {
            return "EXPIRED";
        }

        return "AVAILABLE";
    }

    public Promotion getPromotionById(int promotionId) {
        return promotionRepository.getById(
                promotionId
        );
    }

    public Promotion getPromotionByCode(String promoCode) {

        if (promoCode == null || promoCode.trim().isEmpty()) {
            return null;
        }

        return promotionRepository.getByCode(
                promoCode.trim().toUpperCase()
        );
    }

    public String validateApplyPromotion(
            Promotion promotion,
            BigDecimal orderAmount) {

        if (promotion == null) {
            return "Mã khuyến mãi không tồn tại.";
        }

        if (orderAmount == null
                || orderAmount.compareTo(BigDecimal.ZERO) < 0) {
            return "Giá trị đơn hàng không hợp lệ.";
        }

        if (!"ACTIVE".equalsIgnoreCase(
                promotion.getStatus())) {
            return "Mã khuyến mãi đang bị vô hiệu hóa.";
        }

        Timestamp now = new Timestamp(
                System.currentTimeMillis()
        );

        if (promotion.getStartDate() != null
                && now.before(promotion.getStartDate())) {
            return "Mã khuyến mãi chưa bắt đầu.";
        }

        if (promotion.getEndDate() != null
                && now.after(promotion.getEndDate())) {
            return "Mã khuyến mãi đã hết hạn.";
        }

        if (promotion.getMinOrderAmount() != null
                && orderAmount.compareTo(
                        promotion.getMinOrderAmount()) < 0) {

            return "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã.";
        }
        if (promotion.getUsageLimit() != null) {

            int usedCount
                    = promotionRepository.countUsageByPromotionId(
                            promotion.getPromotionId()
                    );

            if (usedCount >= promotion.getUsageLimit()) {
                return "Mã khuyến mãi đã hết lượt sử dụng.";
            }
        }

        return null;
    }

    public BigDecimal calculateDiscount(
            Promotion promotion,
            BigDecimal orderAmount) {

        if (promotion == null || orderAmount == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;

        if ("PERCENT".equalsIgnoreCase(
                promotion.getDiscountType())) {

            discount = orderAmount
                    .multiply(promotion.getDiscountValue())
                    .divide(new BigDecimal("100"));

        } else if ("FIXED".equalsIgnoreCase(
                promotion.getDiscountType())) {

            discount = promotion.getDiscountValue();

        } else {
            return BigDecimal.ZERO;
        }

        if (promotion.getMaxDiscountAmount() != null
                && discount.compareTo(
                        promotion.getMaxDiscountAmount()) > 0) {

            discount = promotion.getMaxDiscountAmount();
        }

        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount;
    }

    public String createPromotion(Promotion promotion) {

        preparePromotion(promotion);

        String error = validatePromotion(
                promotion
        );

        if (error != null) {
            return error;
        }

        if (promotionRepository.existsByCode(
                promotion.getPromoCode())) {
            return "Mã khuyến mãi đã tồn tại.";
        }

        if (!promotionRepository.insert(
                promotion)) {
            return "Không thể tạo khuyến mãi.";
        }

        return null;
    }

    public String updatePromotion(Promotion promotion) {

        preparePromotion(promotion);

        String error = validatePromotion(
                promotion
        );

        if (error != null) {
            return error;
        }

        Promotion oldPromotion
                = promotionRepository.getById(
                        promotion.getPromotionId()
                );

        if (oldPromotion == null) {
            return "Không tìm thấy khuyến mãi.";
        }

        if (promotionRepository.existsByCodeExceptId(
                promotion.getPromoCode(),
                promotion.getPromotionId())) {
            return "Mã khuyến mãi đã tồn tại.";
        }

        if (!promotionRepository.update(
                promotion)) {
            return "Không thể cập nhật khuyến mãi.";
        }

        return null;
    }

    public String updateStatus(
            int promotionId,
            String status) {

        Promotion promotion
                = promotionRepository.getById(
                        promotionId
                );

        if (promotion == null) {
            return "Không tìm thấy khuyến mãi.";
        }

        if (!"ACTIVE".equals(status)
                && !"INACTIVE".equals(status)) {
            return "Trạng thái không hợp lệ.";
        }

        if (!promotionRepository.updateStatus(
                promotionId,
                status)) {
            return "Không thể cập nhật trạng thái.";
        }

        return null;
    }

    private void preparePromotion(Promotion promotion) {

        if (promotion == null) {
            return;
        }

        if (promotion.getPromoCode() != null) {
            promotion.setPromoCode(
                    promotion.getPromoCode()
                            .trim()
                            .toUpperCase()
            );
        }

        if (promotion.getDescription() != null) {
            promotion.setDescription(
                    promotion.getDescription().trim()
            );
        }

        if (promotion.getDiscountType() != null) {
            promotion.setDiscountType(
                    promotion.getDiscountType()
                            .trim()
                            .toUpperCase()
            );
        }

        if (promotion.getStatus() != null) {
            promotion.setStatus(
                    promotion.getStatus()
                            .trim()
                            .toUpperCase()
            );
        }

        if (promotion.getMinOrderAmount() == null) {
            promotion.setMinOrderAmount(
                    BigDecimal.ZERO
            );
        }
    }

    private String validatePromotion(Promotion promotion) {

        if (promotion == null) {
            return "Dữ liệu khuyến mãi không hợp lệ.";
        }

        if (promotion.getPromoCode() == null
                || promotion.getPromoCode().isEmpty()) {
            return "Vui lòng nhập mã khuyến mãi.";
        }

        if (promotion.getPromoCode().length() > 50) {
            return "Mã khuyến mãi không được vượt quá 50 ký tự.";
        }

        if (!"PERCENT".equals(
                promotion.getDiscountType())
                && !"FIXED".equals(
                        promotion.getDiscountType())) {
            return "Loại giảm giá không hợp lệ.";
        }

        if (promotion.getDiscountValue() == null
                || promotion.getDiscountValue()
                        .compareTo(BigDecimal.ZERO) < 0) {
            return "Giá trị giảm không hợp lệ.";
        }
        if ("PERCENT".equals(promotion.getDiscountType())
                && promotion.getDiscountValue()
                        .compareTo(new BigDecimal("100")) > 0) {
            return "Phần trăm giảm giá không được vượt quá 100%.";
        }

        if (promotion.getMinOrderAmount() == null
                || promotion.getMinOrderAmount()
                        .compareTo(BigDecimal.ZERO) < 0) {
            return "Đơn hàng tối thiểu không hợp lệ.";
        }

        if (promotion.getMaxDiscountAmount() != null
                && promotion.getMaxDiscountAmount()
                        .compareTo(BigDecimal.ZERO) < 0) {
            return "Giảm tối đa không hợp lệ.";
        }

        if (promotion.getStartDate() == null
                || promotion.getEndDate() == null) {
            return "Vui lòng chọn ngày bắt đầu và ngày kết thúc.";
        }

        if (promotion.getEndDate()
                .before(promotion.getStartDate())) {
            return "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu.";
        }

        if (promotion.getUsageLimit() != null
                && promotion.getUsageLimit() < 0) {
            return "Giới hạn sử dụng không hợp lệ.";
        }

        if (!"ACTIVE".equals(promotion.getStatus())
                && !"INACTIVE".equals(promotion.getStatus())) {
            return "Trạng thái không hợp lệ.";
        }

        return null;
    }
}
