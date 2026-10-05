package service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;
import model.Promotion;
import repository.PromotionRepository;

public class PromotionService {

    private PromotionRepository promotionRepository;

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

    public Promotion getPromotionByCode(
            String promoCode) {

        if (promoCode == null
                || promoCode.trim().isEmpty()) {

            return null;
        }

        return promotionRepository.getByCode(
                promoCode.trim()
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

        if (!promotionRepository.insert(
                promotion)) {

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

    public boolean updateStatus(
            int promotionId,
            String status) {

        if (promotionId <= 0
                || status == null) {

            return false;
        }

        String normalizedStatus
                = status.trim().toUpperCase();

        if (!normalizedStatus.equals("ACTIVE")
                && !normalizedStatus.equals("INACTIVE")) {

            return false;
        }

        return promotionRepository.updateStatus(
                promotionId,
                normalizedStatus
        );
    }

    public String checkEligibility(
            String promoCode,
            BigDecimal orderAmount) {

        if (promoCode == null
                || promoCode.trim().isEmpty()) {

            return "Vui lòng nhập mã khuyến mãi.";
        }

        if (orderAmount == null
                || orderAmount.compareTo(
                        BigDecimal.ZERO) < 0) {

            return "Giá trị đơn hàng không hợp lệ.";
        }

        Promotion promotion
                = promotionRepository.getByCode(
                        promoCode.trim()
                );

        if (promotion == null) {

            return "Mã khuyến mãi không tồn tại.";
        }

        if (!"ACTIVE".equalsIgnoreCase(
                promotion.getStatus())) {

            return "Mã khuyến mãi hiện không khả dụng.";
        }

        if (promotion.getStartDate() == null
                || promotion.getEndDate() == null) {

            return "Thời gian áp dụng của mã khuyến mãi không hợp lệ.";
        }

        Timestamp now
                = new Timestamp(
                        System.currentTimeMillis()
                );

        if (now.before(
                promotion.getStartDate())) {

            return "Mã khuyến mãi chưa đến thời gian áp dụng.";
        }

        if (now.after(
                promotion.getEndDate())) {

            return "Mã khuyến mãi đã hết hạn.";
        }

        BigDecimal minOrderAmount
                = promotion.getMinOrderAmount();

        if (minOrderAmount != null
                && orderAmount.compareTo(
                        minOrderAmount) < 0) {

            return "Đơn hàng chưa đạt giá trị tối thiểu "
                    + formatAmount(
                            minOrderAmount
                    )
                    + " VNĐ.";
        }

        Integer usageLimit
                = promotion.getUsageLimit();

        if (usageLimit != null) {

            int confirmedUsage
                    = promotionRepository
                            .countConfirmedUsageByPromotionId(
                                    promotion.getPromotionId()
                            );

            if (confirmedUsage < 0) {

                return "Không thể kiểm tra lượt sử dụng của mã khuyến mãi.";
            }

            if (confirmedUsage >= usageLimit) {

                return "Mã khuyến mãi đã hết lượt sử dụng.";
            }
        }

        return null;
    }

    public BigDecimal calculateDiscount(
            String promoCode,
            BigDecimal orderAmount) {

        String eligibilityError
                = checkEligibility(
                        promoCode,
                        orderAmount
                );

        if (eligibilityError != null) {

            throw new IllegalArgumentException(
                    eligibilityError
            );
        }

        Promotion promotion
                = promotionRepository.getByCode(
                        promoCode.trim()
                );

        BigDecimal discount;

        if ("PERCENT".equalsIgnoreCase(
                promotion.getDiscountType())) {

            discount
                    = orderAmount
                            .multiply(
                                    promotion.getDiscountValue()
                            )
                            .divide(
                                    new BigDecimal("100"),
                                    2,
                                    RoundingMode.HALF_UP
                            );

        } else if ("FIXED".equalsIgnoreCase(
                promotion.getDiscountType())) {

            discount
                    = promotion.getDiscountValue();

        } else {

            throw new IllegalArgumentException(
                    "Loại giảm giá không hợp lệ."
            );
        }

        BigDecimal maxDiscountAmount
                = promotion.getMaxDiscountAmount();

        if (maxDiscountAmount != null
                && discount.compareTo(
                        maxDiscountAmount) > 0) {

            discount
                    = maxDiscountAmount;
        }

        if (discount.compareTo(
                orderAmount) > 0) {

            discount
                    = orderAmount;
        }

        if (discount.compareTo(
                BigDecimal.ZERO) < 0) {

            discount
                    = BigDecimal.ZERO;
        }

        return discount.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    public BigDecimal calculateFinalAmount(
            String promoCode,
            BigDecimal orderAmount) {

        BigDecimal discount
                = calculateDiscount(
                        promoCode,
                        orderAmount
                );

        BigDecimal finalAmount
                = orderAmount.subtract(
                        discount
                );

        if (finalAmount.compareTo(
                BigDecimal.ZERO) < 0) {

            finalAmount
                    = BigDecimal.ZERO;
        }

        return finalAmount.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private String validatePromotion(
            Promotion promotion) {

        if (promotion == null) {

            return "Thông tin khuyến mãi không được để trống.";
        }

        if (promotion.getPromoCode() == null
                || promotion.getPromoCode().isEmpty()) {

            return "Mã khuyến mãi không được để trống.";
        }

        if (promotion.getPromoCode().length() > 50) {

            return "Mã khuyến mãi không được vượt quá 50 ký tự.";
        }

        if (promotion.getDescription() != null
                && promotion.getDescription().length() > 255) {

            return "Mô tả không được vượt quá 255 ký tự.";
        }

        if (promotion.getDiscountType() == null
                || (!promotion.getDiscountType()
                        .equals("PERCENT")
                && !promotion.getDiscountType()
                        .equals("FIXED"))) {

            return "Loại giảm giá phải là PERCENT hoặc FIXED.";
        }

        if (promotion.getDiscountValue() == null) {

            return "Giá trị giảm không được để trống.";
        }

        if (promotion.getDiscountValue()
                .compareTo(BigDecimal.ZERO) < 0) {

            return "Giá trị giảm phải lớn hơn hoặc bằng 0.";
        }

        if (promotion.getMinOrderAmount() == null) {

            return "Giá trị đơn hàng tối thiểu không được để trống.";
        }

        if (promotion.getMinOrderAmount()
                .compareTo(BigDecimal.ZERO) < 0) {

            return "Giá trị đơn hàng tối thiểu phải lớn hơn hoặc bằng 0.";
        }

        if (promotion.getMaxDiscountAmount() != null
                && promotion.getMaxDiscountAmount()
                        .compareTo(BigDecimal.ZERO) < 0) {

            return "Giá trị giảm tối đa phải lớn hơn hoặc bằng 0.";
        }

        if (promotion.getStartDate() == null) {

            return "Ngày bắt đầu không được để trống.";
        }

        if (promotion.getEndDate() == null) {

            return "Ngày kết thúc không được để trống.";
        }

        if (promotion.getEndDate()
                .before(
                        promotion.getStartDate()
                )) {

            return "Ngày kết thúc không được trước ngày bắt đầu.";
        }

        if (promotion.getUsageLimit() != null
                && promotion.getUsageLimit() < 0) {

            return "Giới hạn sử dụng phải lớn hơn hoặc bằng 0.";
        }

        if (promotion.getStatus() == null
                || (!promotion.getStatus()
                        .equals("ACTIVE")
                && !promotion.getStatus()
                        .equals("INACTIVE"))) {

            return "Trạng thái phải là ACTIVE hoặc INACTIVE.";
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
            );
        }

        if (promotion.getDescription() != null) {

            promotion.setDescription(
                    promotion.getDescription()
                            .trim()
            );
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

    private String formatAmount(
            BigDecimal amount) {

        if (amount == null) {
            return "0";
        }

        return String.format(
                "%,.0f",
                amount
        ).replace(
                ',',
                '.'
        );
    }
}