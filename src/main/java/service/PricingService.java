
package service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Objects;
import model.Pricing;
import repository.MovieRepository;
import repository.PricingRepository;

public class PricingService {

    private final PricingRepository pricingRepository;
    private final MovieRepository movieRepository;

    public PricingService() {
        pricingRepository = new PricingRepository();
        movieRepository = new MovieRepository();
    }

    public List<Pricing> getPricingByCinema(int cinemaId) {

        if (cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Rạp không hợp lệ."
            );
        }

        return pricingRepository.findByCinemaId(cinemaId);
    }

    public Pricing getPricing(int pricingId, int cinemaId) {

        if (pricingId <= 0 || cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Thông tin giá vé không hợp lệ."
            );
        }

        return pricingRepository.findByIdAndCinema(
                pricingId, cinemaId
        );
    }

    // =====================================================
    // TẠO QUY TẮC GIÁ
    // =====================================================

    public void createPricing(
            int cinemaId,
            Integer movieId,
            String roomType,
            String seatType,
            String dayType,
            Date specialDate,
            Time startTime,
            Time endTime,
            BigDecimal price,
            Date startDate,
            Date endDate,
            String status) {

        String normalizedRoomType =
                normalizeRoomType(roomType);

        String normalizedSeatType =
                normalizeSeatType(seatType);

        String normalizedDayType =
                normalizeDayType(dayType);

        String normalizedStatus =
                normalizeStatus(status);

        // Ngày đặc biệt được ưu tiên riêng,
        // không phụ thuộc WEEKDAY hoặc WEEKEND.
        if (specialDate != null) {
            normalizedDayType = "ALL";
        }

        Pricing pricing = new Pricing(
                0,
                cinemaId,
                movieId,
                null,
                normalizedRoomType,
                normalizedSeatType,
                normalizedDayType,
                specialDate,
                startTime,
                endTime,
                price,
                startDate,
                endDate,
                normalizedStatus
        );

        validatePricing(pricing);

        // BUG 05 FIX:
        // Không cho tạo quy tắc cùng phạm vi
        // có khoảng ngày và giờ giao nhau.
        if (hasConflictingRule(pricing, null)) {
            throw new IllegalArgumentException(
                    "Đã tồn tại quy tắc giá ACTIVE "
                    + "cùng phạm vi và có thời gian "
                    + "áp dụng bị chồng chéo."
            );
        }

        if (!pricingRepository.insert(pricing)) {
            throw new IllegalArgumentException(
                    "Không thể thêm giá vé."
            );
        }
    }

    // =====================================================
    // CẬP NHẬT QUY TẮC GIÁ
    // =====================================================

    public void updatePricing(
            int pricingId,
            int cinemaId,
            Integer movieId,
            String roomType,
            String seatType,
            String dayType,
            Date specialDate,
            Time startTime,
            Time endTime,
            BigDecimal price,
            Date startDate,
            Date endDate,
            String status) {

        Pricing currentPricing =
                pricingRepository.findByIdAndCinema(
                        pricingId, cinemaId
                );

        if (currentPricing == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy giá vé thuộc rạp của bạn."
            );
        }

        String normalizedRoomType =
                normalizeRoomType(roomType);

        String normalizedSeatType =
                normalizeSeatType(seatType);

        String normalizedDayType =
                normalizeDayType(dayType);

        String normalizedStatus =
                normalizeStatus(status);

        if (specialDate != null) {
            normalizedDayType = "ALL";
        }

        Pricing pricing = new Pricing(
                pricingId,
                cinemaId,
                movieId,
                null,
                normalizedRoomType,
                normalizedSeatType,
                normalizedDayType,
                specialDate,
                startTime,
                endTime,
                price,
                startDate,
                endDate,
                normalizedStatus
        );

        validatePricing(pricing);

        // Bỏ qua chính quy tắc đang được sửa.
        if (hasConflictingRule(pricing, pricingId)) {
            throw new IllegalArgumentException(
                    "Không thể cập nhật vì quy tắc giá "
                    + "bị chồng chéo với quy tắc "
                    + "ACTIVE khác cùng phạm vi."
            );
        }

        if (!pricingRepository.update(pricing)) {
            throw new IllegalArgumentException(
                    "Không thể cập nhật giá vé."
            );
        }
    }

    // =====================================================
    // XÓA QUY TẮC GIÁ
    // =====================================================

    public void deletePricing(int pricingId, int cinemaId) {

        Pricing pricing = getPricing(pricingId, cinemaId);

        if (pricing == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy giá vé thuộc rạp của bạn."
            );
        }

        if (!pricingRepository.delete(pricingId, cinemaId)) {
            throw new IllegalArgumentException(
                    "Không thể xóa giá vé."
            );
        }
    }

    // =====================================================
    // VALIDATE DỮ LIỆU GIÁ VÉ
    // =====================================================

    private void validatePricing(Pricing pricing) {

        if (pricing.getCinemaId() <= 0) {
            throw new IllegalArgumentException(
                    "Rạp không hợp lệ."
            );
        }

        if (pricing.getMovieId() != null) {

            if (pricing.getMovieId() <= 0
                    || !movieRepository.belongsToCinema(
                            pricing.getMovieId(),
                            pricing.getCinemaId())) {

                throw new IllegalArgumentException(
                        "Phim không thuộc rạp của bạn."
                );
            }
        }

        if (pricing.getPrice() == null
                || pricing.getPrice().compareTo(
                        BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Giá vé phải lớn hơn 0."
            );
        }

        if (pricing.getStartDate() == null) {
            throw new IllegalArgumentException(
                    "Ngày bắt đầu không được để trống."
            );
        }

        if (pricing.getEndDate() != null
                && pricing.getEndDate().before(
                        pricing.getStartDate())) {

            throw new IllegalArgumentException(
                    "Ngày kết thúc không được "
                    + "trước ngày bắt đầu."
            );
        }

        if (pricing.getSpecialDate() != null) {

            if (pricing.getSpecialDate().before(
                    pricing.getStartDate())) {

                throw new IllegalArgumentException(
                        "Ngày đặc biệt phải nằm trong "
                        + "thời gian áp dụng."
                );
            }

            if (pricing.getEndDate() != null
                    && pricing.getSpecialDate().after(
                            pricing.getEndDate())) {

                throw new IllegalArgumentException(
                        "Ngày đặc biệt phải nằm trong "
                        + "thời gian áp dụng."
                );
            }
        }

        boolean hasStartTime =
                pricing.getStartTime() != null;

        boolean hasEndTime =
                pricing.getEndTime() != null;

        if (hasStartTime != hasEndTime) {
            throw new IllegalArgumentException(
                    "Nếu chọn khung giờ thì phải nhập "
                    + "cả giờ bắt đầu và giờ kết thúc."
            );
        }

        if (hasStartTime
                && !pricing.getEndTime().after(
                        pricing.getStartTime())) {

            throw new IllegalArgumentException(
                    "Giờ kết thúc phải sau giờ bắt đầu."
            );
        }
    }

    // =====================================================
    // BUG 05 FIX:
    // KIỂM TRA QUY TẮC GIÁ CHỒNG CHÉO
    //
    // Hai quy tắc bị xung đột khi:
    // 1. Cùng phạm vi nghiệp vụ.
    // 2. Đều ACTIVE.
    // 3. Khoảng ngày hiệu lực giao nhau.
    // 4. Khoảng giờ hiệu lực giao nhau.
    //
    // Quy tắc tổng quát và cụ thể được phép
    // cùng tồn tại theo cơ chế ưu tiên giá.
    // =====================================================

    private boolean hasConflictingRule(
            Pricing candidate,
            Integer excludePricingId) {

        if (!"ACTIVE".equals(candidate.getStatus())) {
            return false;
        }

        List<Pricing> pricingList =
                pricingRepository.findByCinemaId(
                        candidate.getCinemaId()
                );

        for (Pricing current : pricingList) {

            if (excludePricingId != null
                    && current.getPricingId()
                    == excludePricingId) {
                continue;
            }

            if (!"ACTIVE".equals(current.getStatus())) {
                continue;
            }

            if (!sameScope(current, candidate)) {
                continue;
            }

            if (!dateRangesOverlap(current, candidate)) {
                continue;
            }

            // BUG 05 FIX:
            // So sánh khoảng giờ giao nhau,
            // thay vì yêu cầu hai khung giờ
            // phải giống nhau hoàn toàn.
            if (!timeRangesOverlap(current, candidate)) {
                continue;
            }

            return true;
        }

        return false;
    }

    // =====================================================
    // SO SÁNH PHẠM VI ÁP DỤNG
    //
    // Không so sánh start_time và end_time tại đây.
    // Khung giờ được kiểm tra riêng bằng overlap.
    // =====================================================

    private boolean sameScope(
            Pricing first,
            Pricing second) {

        return Objects.equals(
                first.getMovieId(),
                second.getMovieId()
        )
                && Objects.equals(
                        first.getRoomType(),
                        second.getRoomType()
                )
                && Objects.equals(
                        first.getSeatType(),
                        second.getSeatType()
                )
                && Objects.equals(
                        first.getDayType(),
                        second.getDayType()
                )
                && Objects.equals(
                        first.getSpecialDate(),
                        second.getSpecialDate()
                );
    }

    // =====================================================
    // KIỂM TRA KHOẢNG NGÀY GIAO NHAU
    // =====================================================

    private boolean dateRangesOverlap(
            Pricing first,
            Pricing second) {

        boolean firstEndsAfterSecondStarts =
                first.getEndDate() == null
                || !first.getEndDate().before(
                        second.getStartDate()
                );

        boolean secondEndsAfterFirstStarts =
                second.getEndDate() == null
                || !second.getEndDate().before(
                        first.getStartDate()
                );

        return firstEndsAfterSecondStarts
                && secondEndsAfterFirstStarts;
    }

    // =====================================================
    // BUG 05 FIX:
    // KIỂM TRA KHOẢNG GIỜ GIAO NHAU
    //
    // Không nhập giờ => áp dụng cả ngày.
    // Khung giờ được hiểu là [start, end).
    //
    // Ví dụ:
    // 18:00 - 21:00 và 19:00 - 22:00
    // => Có xung đột.
    //
    // 18:00 - 21:00 và 21:00 - 23:00
    // => Không xung đột.
    // =====================================================

    private boolean timeRangesOverlap(
            Pricing first,
            Pricing second) {

        Time firstStart = first.getStartTime();
        Time firstEnd = first.getEndTime();

        Time secondStart = second.getStartTime();
        Time secondEnd = second.getEndTime();

        // Quy tắc không giới hạn giờ
        // áp dụng cả ngày.
        if (firstStart == null || firstEnd == null) {
            return true;
        }

        if (secondStart == null || secondEnd == null) {
            return true;
        }

        // Hai khoảng [start, end) giao nhau khi:
        // start1 < end2 và start2 < end1.
        return firstStart.before(secondEnd)
                && secondStart.before(firstEnd);
    }

    // =====================================================
    // CHUẨN HÓA LOẠI GHẾ
    // =====================================================

    private String normalizeSeatType(String seatType) {

        if (seatType == null
                || seatType.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn loại ghế."
            );
        }

        String type = seatType.trim().toUpperCase();

        if (!"STANDARD".equals(type)
                && !"VIP".equals(type)
                && !"COUPLE".equals(type)) {

            throw new IllegalArgumentException(
                    "Loại ghế không hợp lệ."
            );
        }

        return type;
    }

    // =====================================================
    // CHUẨN HÓA LOẠI PHÒNG
    // =====================================================

    private String normalizeRoomType(String roomType) {

        if (roomType == null
                || roomType.trim().isEmpty()) {
            return null;
        }

        String type = roomType.trim().toUpperCase();

        if (!"2D".equals(type)
                && !"3D".equals(type)
                && !"IMAX".equals(type)) {

            throw new IllegalArgumentException(
                    "Loại phòng không hợp lệ."
            );
        }

        return type;
    }

    // =====================================================
    // CHUẨN HÓA LOẠI NGÀY
    // =====================================================

    private String normalizeDayType(String dayType) {

        if (dayType == null
                || dayType.trim().isEmpty()) {
            return "ALL";
        }

        String type = dayType.trim().toUpperCase();

        if (!"ALL".equals(type)
                && !"WEEKDAY".equals(type)
                && !"WEEKEND".equals(type)) {

            throw new IllegalArgumentException(
                    "Loại ngày không hợp lệ."
            );
        }

        return type;
    }

    // =====================================================
    // CHUẨN HÓA TRẠNG THÁI
    // =====================================================

    private String normalizeStatus(String status) {

        if (status == null
                || status.trim().isEmpty()) {
            return "ACTIVE";
        }

        String normalized =
                status.trim().toUpperCase();

        if (!"ACTIVE".equals(normalized)
                && !"INACTIVE".equals(normalized)) {

            throw new IllegalArgumentException(
                    "Trạng thái giá vé không hợp lệ."
            );
        }

        return normalized;
    }
}
