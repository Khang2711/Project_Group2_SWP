
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

    /** Khởi tạo Repository để quản lý chính sách giá. */
    public PricingService() {
        pricingRepository = new PricingRepository();
        movieRepository = new MovieRepository();
    }

    /** Lấy chính sách giá của rạp. */
    public List<Pricing> getPricingByCinema(int cinemaId) {

        if (cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Rạp không hợp lệ."
            );
        }

        return pricingRepository.findByCinemaId(cinemaId);
    }

    /** Lấy chính sách giá đúng rạp quản lý. */
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

    /** Kiểm tra rồi tạo chính sách giá. */
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
        //kiểm tra và chuẩn hóa dữ liệu trước khi lưu
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
        //Tạo đối tượng Pricing
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
        //Kiểm tra dữ liệu
        validatePricing(pricing);

        //Kiểm tra chính sách giá chồng chéo
        // Không cho tạo quy tắc cùng phạm vi
        // có khoảng ngày và giờ giao nhau.
        if (hasConflictingRule(pricing, null)) {
            throw new IllegalArgumentException(
                    "Đã tồn tại quy tắc giá ACTIVE "
                    + "cùng phạm vi và có thời gian "
                    + "áp dụng bị chồng chéo."
            );
        }
        //Nếu mọi điều kiện hợp lệ, Service gọi Repository để lưu.
        if (!pricingRepository.insert(pricing)) {
            throw new IllegalArgumentException(
                    "Không thể thêm giá vé."
            );
        }
    }

    // =====================================================
    // CẬP NHẬT QUY TẮC GIÁ
    // =====================================================

    /** Sửa giá sau khi kiểm tra xung đột. */
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
        //Kiểm tra chính sách thuộc rạp
        //Không tìm thấy chính sách thuộc rạp → không cho sửa
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
        //vì nếu không bỏ qua hệ thống có thể nhận nhầm chính sách đó là quy tắc bị trùng
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

    /** Xóa chính sách giá của rạp. */
    public void deletePricing(int pricingId, int cinemaId) {
        //Trước tiên kiểm tra chính sách giá có thuộc rạp không.
        Pricing pricing = getPricing(pricingId, cinemaId);

        if (pricing == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy giá vé thuộc rạp của bạn."
            );
        }
        //Sau đó gọi Repository để thực hiện SQL DELETE.
        if (!pricingRepository.delete(pricingId, cinemaId)) {
            throw new IllegalArgumentException(
                    "Không thể xóa giá vé."
            );
        }
    }

    // =====================================================
    // VALIDATE DỮ LIỆU GIÁ VÉ
    // =====================================================

    /** Kiểm tra giá, ngày, giờ và phạm vi áp dụng. */
    private void validatePricing(Pricing pricing) {
        
        if (pricing.getCinemaId() <= 0) {
            throw new IllegalArgumentException(
                    "Rạp không hợp lệ."
            );
        }

        if (pricing.getMovieId() != null) {
            //Đoạn kiểm tra phim thuộc rạp
            //Đoạn này chỉ thực hiện nếu movieId khác null
            if (pricing.getMovieId() <= 0
                    || !movieRepository.belongsToCinema(
                            pricing.getMovieId(),
                            pricing.getCinemaId())) {

                throw new IllegalArgumentException(
                        "Phim không thuộc rạp của bạn."
                );
            }
        }
        //compareTo(BigDecimal.ZERO) <= 0 nghĩa là giá bằng 0 hoặc âm, nên hệ thống từ chối.
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

    /** Tìm chính sách ACTIVE trùng phạm vi và thời gian. */
    private boolean hasConflictingRule(
            Pricing candidate,
            Integer excludePricingId) {
        //Nếu chính sách mới không ACTIVE thì hàm không kiểm tra xung đột
        if (!"ACTIVE".equals(candidate.getStatus())) {
            return false;
        }
        //Lấy danh sách chính sách giá cùng rạp.
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
            //Bỏ qua chính sách hiện tại không ACTIVE.
            if (!"ACTIVE".equals(current.getStatus())) {
                continue;
            }
            //Bỏ qua nếu khác phạm vi áp dụng.
            if (!sameScope(current, candidate)) {
                continue;
            }
            //Bỏ qua nếu khoảng ngày không giao nhau.
            if (!dateRangesOverlap(current, candidate)) {
                continue;
            }
            //Bỏ qua nếu khoảng giờ không giao nhau.
            // So sánh khoảng giờ giao nhau,
            // thay vì yêu cầu hai khung giờ
            // phải giống nhau hoàn toàn.
            if (!timeRangesOverlap(current, candidate)) {
                continue;
            }
            //Nếu không bị bỏ qua ở bất kỳ điều kiện nào thì phát hiện xung đột.
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

    /** Kiểm tra hai chính sách cùng phạm vi. */
    private boolean sameScope(
            Pricing first,
            Pricing second) {
        //Objects.equals() giúp so sánh cả các giá trị có thể là null.
        //Hai quy tắc được xem là cùng phạm vi khi các trường sau bằng nhau
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

    /** Kiểm tra hai khoảng ngày có giao nhau. */
    private boolean dateRangesOverlap(
            Pricing first,
            Pricing second) {
        //Chính sách 1 chưa kết thúc trước khi chính sách 2 bắt đầu.
        boolean firstEndsAfterSecondStarts =
                first.getEndDate() == null
                || !first.getEndDate().before(
                        second.getStartDate()
                );
        //Chính sách 2 chưa kết thúc trước khi chính sách 1 bắt đầu.
        boolean secondEndsAfterFirstStarts =
                second.getEndDate() == null
                || !second.getEndDate().before(
                        first.getStartDate()
                );
        //Nếu cả hai đúng → khoảng ngày giao nhau.
        return firstEndsAfterSecondStarts
                && secondEndsAfterFirstStarts;
    }

    // =====================================================
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

    /** Kiểm tra hai khoảng giờ có giao nhau. */
    private boolean timeRangesOverlap(
            Pricing first,
            Pricing second) {

        Time firstStart = first.getStartTime();
        Time firstEnd = first.getEndTime();

        Time secondStart = second.getStartTime();
        Time secondEnd = second.getEndTime();
        //Nếu một trong hai chính sách không giới hạn khung giờ thì được xem là có khả năng giao giờ với chính sách còn lại.
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

    /** Chuẩn hóa loại ghế trong chính sách. */
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

    /** Chuẩn hóa loại phòng trong chính sách. */
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

    /** Chuẩn hóa loại ngày áp dụng. */
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

    /** Chuẩn hóa trạng thái chính sách. */
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
