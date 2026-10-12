package service;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import model.Pricing;
import repository.PricingRepository;

public class PricingService {

    private final PricingRepository pricingRepository;

    public PricingService() {

        pricingRepository =
                new PricingRepository();
    }

    // =========================================================
    // DANH SÁCH GIÁ CỦA RẠP
    // =========================================================
    public List<Pricing> getPricingByCinema(
            int cinemaId) {

        if (cinemaId <= 0) {

            throw new IllegalArgumentException(
                    "Rạp không hợp lệ."
            );
        }

        return pricingRepository
                .findByCinemaId(
                        cinemaId
                );
    }

    // =========================================================
    // LẤY 1 GIÁ
    // =========================================================
    public Pricing getPricing(
            int pricingId,
            int cinemaId) {

        if (pricingId <= 0
                || cinemaId <= 0) {

            throw new IllegalArgumentException(
                    "Thông tin giá vé không hợp lệ."
            );
        }

        return pricingRepository
                .findByIdAndCinema(
                        pricingId,
                        cinemaId
                );
    }

    // =========================================================
    // TẠO GIÁ
    // =========================================================
    public void createPricing(
            int cinemaId,
            String seatType,
            BigDecimal price,
            Date startDate,
            Date endDate,
            String status) {

        String normalizedSeatType =
                normalizeSeatType(
                        seatType
                );

        validatePricing(
                cinemaId,
                null,
                normalizedSeatType,
                price,
                startDate,
                endDate,
                status
        );

        Pricing pricing =
                new Pricing(
                        0,
                        cinemaId,
                        normalizedSeatType,
                        price,
                        startDate,
                        endDate,
                        status
                );

        if (!pricingRepository
                .insert(pricing)) {

            throw new IllegalArgumentException(
                    "Không thể thêm giá vé."
            );
        }
    }

    // =========================================================
    // CẬP NHẬT GIÁ
    // =========================================================
    public void updatePricing(
            int pricingId,
            int cinemaId,
            String seatType,
            BigDecimal price,
            Date startDate,
            Date endDate,
            String status) {

        Pricing currentPricing =
                pricingRepository
                        .findByIdAndCinema(
                                pricingId,
                                cinemaId
                        );

        if (currentPricing == null) {

            throw new IllegalArgumentException(
                    "Không tìm thấy giá vé "
                    + "thuộc rạp của bạn."
            );
        }

        String normalizedSeatType =
                normalizeSeatType(
                        seatType
                );

        validatePricing(
                cinemaId,
                pricingId,
                normalizedSeatType,
                price,
                startDate,
                endDate,
                status
        );

        Pricing pricing =
                new Pricing(
                        pricingId,
                        cinemaId,
                        normalizedSeatType,
                        price,
                        startDate,
                        endDate,
                        status
                );

        if (!pricingRepository
                .update(pricing)) {

            throw new IllegalArgumentException(
                    "Không thể cập nhật giá vé."
            );
        }
    }

    // =========================================================
    // XÓA GIÁ
    // =========================================================
    public void deletePricing(
            int pricingId,
            int cinemaId) {

        if (pricingId <= 0
                || cinemaId <= 0) {

            throw new IllegalArgumentException(
                    "Thông tin giá vé không hợp lệ."
            );
        }

        Pricing pricing =
                pricingRepository
                        .findByIdAndCinema(
                                pricingId,
                                cinemaId
                        );

        if (pricing == null) {

            throw new IllegalArgumentException(
                    "Không tìm thấy giá vé "
                    + "thuộc rạp của bạn."
            );
        }

        if (!pricingRepository
                .delete(
                        pricingId,
                        cinemaId
                )) {

            throw new IllegalArgumentException(
                    "Không thể xóa giá vé."
            );
        }
    }

    // =========================================================
    // VALIDATE
    // =========================================================
    private void validatePricing(
            int cinemaId,
            Integer pricingId,
            String seatType,
            BigDecimal price,
            Date startDate,
            Date endDate,
            String status) {

        if (cinemaId <= 0) {

            throw new IllegalArgumentException(
                    "Rạp không hợp lệ."
            );
        }

        // -----------------------------------------
        // LOẠI GHẾ
        // -----------------------------------------
        if (!"STANDARD".equals(seatType)
                && !"VIP".equals(seatType)
                && !"COUPLE".equals(seatType)) {

            throw new IllegalArgumentException(
                    "Loại ghế không hợp lệ."
            );
        }

        // -----------------------------------------
        // GIÁ
        // DB cho phép price >= 0
        // -----------------------------------------
        if (price == null
                || price.compareTo(
                        BigDecimal.ZERO
                ) < 0) {

            throw new IllegalArgumentException(
                    "Giá vé phải lớn hơn "
                    + "hoặc bằng 0."
            );
        }

        // -----------------------------------------
        // START DATE
        // -----------------------------------------
        if (startDate == null) {

            throw new IllegalArgumentException(
                    "Ngày bắt đầu không được để trống."
            );
        }

        // -----------------------------------------
        // END DATE
        // -----------------------------------------
        if (endDate != null
                && endDate.before(
                        startDate
                )) {

            throw new IllegalArgumentException(
                    "Ngày kết thúc không được "
                    + "trước ngày bắt đầu."
            );
        }

        // -----------------------------------------
        // STATUS
        // -----------------------------------------
        if (!"ACTIVE".equals(status)
                && !"INACTIVE".equals(status)) {

            throw new IllegalArgumentException(
                    "Trạng thái giá vé không hợp lệ."
            );
        }

        // -----------------------------------------
        // RULE QUAN TRỌNG:
        // Active pricing cùng cinema + seat type
        // không được overlap.
        // -----------------------------------------
        if ("ACTIVE".equals(status)
                && pricingRepository
                        .hasOverlappingActivePeriod(
                                cinemaId,
                                seatType,
                                startDate,
                                endDate,
                                pricingId
                        )) {

            throw new IllegalArgumentException(
                    "Khoảng thời gian giá vé ACTIVE "
                    + "bị trùng với một giá "
                    + seatType
                    + " khác đang tồn tại."
            );
        }
    }

    // =========================================================
    // NORMALIZE SEAT TYPE
    // =========================================================
    private String normalizeSeatType(
            String seatType) {

        if (seatType == null
                || seatType.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn loại ghế."
            );
        }

        String type =
                seatType.trim()
                        .toUpperCase();

        if (!"STANDARD".equals(type)
                && !"VIP".equals(type)
                && !"COUPLE".equals(type)) {

            throw new IllegalArgumentException(
                    "Loại ghế không hợp lệ."
            );
        }

        return type;
    }
}