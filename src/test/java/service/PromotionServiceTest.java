package service;

import java.math.BigDecimal;
import model.Promotion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests cho PromotionService.calculateDiscount().
 *
 * Các test chỉ gọi method tính toán, không gọi truy vấn repository.
 * ShowtimeRepository được khởi tạo trễ, vì vậy calculateDiscount()
 * có thể được kiểm thử độc lập mà không cần SQL Server.
 */
class PromotionServiceTest {

    private static PromotionService service;

    @BeforeAll
    static void setUp() {
        // Khởi tạo một lần để tránh tạo nhiều kết nối không cần thiết.
        service = new PromotionService();
    }

    @Test
    void shouldReturnZeroWhenPromotionIsNull() {
        BigDecimal actual = service.calculateDiscount(
                null,
                new BigDecimal("100.00")
        );

        assertAmount("0", actual);
    }

    @Test
    void shouldReturnZeroWhenOrderAmountIsNull() {
        Promotion promotion = createPromotion("PERCENT", "10", null);

        BigDecimal actual = service.calculateDiscount(promotion, null);

        assertAmount("0", actual);
    }

    @Test
    void shouldCalculatePercentDiscount() {
        // 10% của 200 = 20; không cấu hình mức giảm tối đa.
        Promotion promotion = createPromotion("PERCENT", "10", null);

        BigDecimal actual = service.calculateDiscount(
                promotion,
                new BigDecimal("200")
        );

        assertAmount("20", actual);
    }

    @Test
    void shouldHandlePercentAtOneHundredPercentBoundary() {
        // 100% của đơn hàng bằng chính giá trị đơn hàng.
        Promotion promotion = createPromotion("PERCENT", "100", null);

        BigDecimal actual = service.calculateDiscount(
                promotion,
                new BigDecimal("100")
        );

        assertAmount("100", actual);
    }

    @Test
    void shouldCalculateFixedDiscount() {
        // Mã FIXED giảm trực tiếp 40 đơn vị tiền.
        Promotion promotion = createPromotion("FIXED", "40", null);

        BigDecimal actual = service.calculateDiscount(
                promotion,
                new BigDecimal("100")
        );

        assertAmount("40", actual);
    }

    @Test
    void shouldNotCapWhenDiscountEqualsMaximumDiscount() {
        // Giá trị giảm bằng maxDiscountAmount: điều kiện '>' là false.
        Promotion promotion = createPromotion("FIXED", "80", "80");

        BigDecimal actual = service.calculateDiscount(
                promotion,
                new BigDecimal("100")
        );

        assertAmount("80", actual);
    }

    @Test
    void shouldCapDiscountAtMaximumDiscountAmount() {
        // Giảm cố định 120 nhưng maxDiscountAmount chỉ cho phép 30.
        Promotion promotion = createPromotion("FIXED", "120", "30");

        BigDecimal actual = service.calculateDiscount(
                promotion,
                new BigDecimal("200")
        );

        assertAmount("30", actual);
    }

    @Test
    void shouldNotLetDiscountExceedOrderAmount() {
        // Không có max discount; số giảm 120 phải bị chặn ở đơn hàng 100.
        Promotion promotion = createPromotion("FIXED", "120", null);

        BigDecimal actual = service.calculateDiscount(
                promotion,
                new BigDecimal("100")
        );

        assertAmount("100", actual);
    }

    @Test
    void shouldReturnZeroForUnsupportedDiscountType() {
        // Loại giảm giá không thuộc PERCENT hoặc FIXED.
        Promotion promotion = createPromotion("UNKNOWN", "10", null);

        BigDecimal actual = service.calculateDiscount(
                promotion,
                new BigDecimal("100")
        );

        assertAmount("0", actual);
    }

    private static Promotion createPromotion(
            String discountType,
            String discountValue,
            String maxDiscountAmount) {

        Promotion promotion = new Promotion();
        promotion.setDiscountType(discountType);
        promotion.setDiscountValue(new BigDecimal(discountValue));

        promotion.setMaxDiscountAmount(
                maxDiscountAmount == null
                        ? null
                        : new BigDecimal(maxDiscountAmount)
        );

        return promotion;
    }

    /**
     * BigDecimal.compareTo so sánh giá trị số, không bị ảnh hưởng bởi scale.
     */
    private static void assertAmount(String expected, BigDecimal actual) {
        assertNotNull(actual, "Method không được trả về null.");
        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual),
                "Giá trị giảm giá thực tế không đúng."
        );
    }
}
