package service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import model.Promotion;
import repository.PromotionRepository;

public class PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionService() {
        promotionRepository = new PromotionRepository();
    }

    public List<Promotion> getAllPromotions() {
        return promotionRepository.getAll();
    }

    public Promotion getPromotionById(int promotionId) {

        if (promotionId <= 0) {
            return null;
        }

        return promotionRepository.getById(
                promotionId
        );
    }

    public String createPromotion(
            Promotion promotion) {

        normalizePromotion(
                promotion
        );

        String error
                = validatePromotion(
                        promotion
                );

        if (error != null) {
            return error;
        }

        if (promotionRepository.existsByCode(
                promotion.getPromoCode())) {

            return "Mã khuyến mãi đã tồn tại.";
        }

        boolean inserted
                = promotionRepository.insert(
                        promotion
                );

        if (!inserted) {
            return "Không thể tạo khuyến mãi.";
        }

        return null;
    }

    public String updatePromotion(
            Promotion promotion) {

        if (promotion == null
                || promotion.getPromotionId() <= 0) {

            return "Khuyến mãi không hợp lệ.";
        }

        normalizePromotion(
                promotion
        );

        String error
                = validatePromotion(
                        promotion
                );

        if (error != null) {
            return error;
        }

        Promotion existingPromotion
                = promotionRepository.getById(
                        promotion.getPromotionId()
                );

        if (existingPromotion == null) {
            return "Không tìm thấy khuyến mãi.";
        }

        if (promotionRepository
                .existsByCodeExceptId(
                        promotion.getPromoCode(),
                        promotion.getPromotionId())) {

            return "Mã khuyến mãi đã tồn tại.";
        }

        boolean updated
                = promotionRepository.update(
                        promotion
                );

        if (!updated) {
            return "Không thể cập nhật khuyến mãi.";
        }

        return null;
    }

    public String updateStatus(
            int promotionId,
            String status) {

        if (promotionId <= 0) {
            return "Khuyến mãi không hợp lệ.";
        }

        if (status == null) {
            return "Trạng thái không hợp lệ.";
        }

        String normalizedStatus
                = status.trim().toUpperCase();

        if (!"ACTIVE".equals(
                normalizedStatus)
                && !"INACTIVE".equals(
                        normalizedStatus)) {

            return "Trạng thái không hợp lệ.";
        }

        Promotion promotion
                = promotionRepository.getById(
                        promotionId
                );

        if (promotion == null) {
            return "Không tìm thấy khuyến mãi.";
        }

        boolean updated
                = promotionRepository.updateStatus(
                        promotionId,
                        normalizedStatus
                );

        if (!updated) {
            return "Không thể cập nhật trạng thái.";
        }

        return null;
    }

    private String validatePromotion(
            Promotion promotion) {

        if (promotion == null) {
            return "Dữ liệu khuyến mãi không hợp lệ.";
        }

        if (promotion.getPromoCode() == null
                || promotion.getPromoCode()
                        .trim()
                        .isEmpty()) {

            return "Vui lòng nhập mã khuyến mãi.";
        }

        if (promotion.getPromoCode()
                .length() > 50) {

            return "Mã khuyến mãi không được vượt quá 50 ký tự.";
        }

        if (promotion.getDiscountType() == null
                || (!"PERCENT".equals(
                        promotion.getDiscountType())
                && !"FIXED".equals(
                        promotion.getDiscountType()))) {

            return "Loại giảm giá không hợp lệ.";
        }

        BigDecimal discountValue
                = promotion.getDiscountValue();

        if (discountValue == null
                || discountValue.compareTo(
                        BigDecimal.ZERO) < 0) {

            return "Giá trị giảm giá không hợp lệ.";
        }

        BigDecimal minOrderAmount
                = promotion.getMinOrderAmount();

        if (minOrderAmount == null
                || minOrderAmount.compareTo(
                        BigDecimal.ZERO) < 0) {

            return "Giá trị đơn hàng tối thiểu không hợp lệ.";
        }

        BigDecimal maxDiscountAmount
                = promotion.getMaxDiscountAmount();

        if (maxDiscountAmount != null
                && maxDiscountAmount.compareTo(
                        BigDecimal.ZERO) < 0) {

            return "Giá trị giảm tối đa không hợp lệ.";
        }

        Timestamp startDate
                = promotion.getStartDate();

        Timestamp endDate
                = promotion.getEndDate();

        if (startDate == null
                || endDate == null) {

            return "Vui lòng chọn ngày bắt đầu và ngày kết thúc.";
        }

        if (endDate.before(
                startDate)) {

            return "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu.";
        }

        Integer usageLimit
                = promotion.getUsageLimit();

        if (usageLimit != null
                && usageLimit < 0) {

            return "Giới hạn sử dụng không hợp lệ.";
        }

        if (promotion.getStatus() == null
                || (!"ACTIVE".equals(
                        promotion.getStatus())
                && !"INACTIVE".equals(
                        promotion.getStatus()))) {

            return "Trạng thái không hợp lệ.";
        }

        return null;
    }

    private void normalizePromotion(
            Promotion promotion) {

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

            String description
                    = promotion.getDescription()
                            .trim();

            if (description.isEmpty()) {

                promotion.setDescription(
                        null
                );

            } else {

                promotion.setDescription(
                        description
                );
            }
        }

        if (promotion.getDiscountType() != null) {

            promotion.setDiscountType(
                    promotion.getDiscountType()
                            .trim()
                            .toUpperCase()
            );
        }

        if (promotion.getMinOrderAmount() == null) {

            promotion.setMinOrderAmount(
                    BigDecimal.ZERO
            );
        }

        if (promotion.getStatus() == null
                || promotion.getStatus()
                        .trim()
                        .isEmpty()) {

            promotion.setStatus(
                    "ACTIVE"
            );

        } else {

            promotion.setStatus(
                    promotion.getStatus()
                            .trim()
                            .toUpperCase()
            );
        }
    }
}