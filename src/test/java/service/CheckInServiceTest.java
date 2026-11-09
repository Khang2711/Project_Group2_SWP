package service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests cho CheckInService.validateTicketCode().
 *
 * Method chỉ xử lý chuỗi đầu vào, không truy cập database.
 */
class CheckInServiceTest {

    private final CheckInService service = new CheckInService();

    private static final String EMPTY_CODE_ERROR = "Vui lòng nhập mã vé.";
    private static final String INVALID_CODE_ERROR = "Mã vé không hợp lệ.";

    @Test
    void shouldRejectNullTicketCode() {
        assertEquals(
                EMPTY_CODE_ERROR,
                service.validateTicketCode(null)
        );
    }

    @Test
    void shouldRejectEmptyTicketCode() {
        assertEquals(
                EMPTY_CODE_ERROR,
                service.validateTicketCode("")
        );
    }

    @Test
    void shouldRejectWhitespaceOnlyTicketCode() {
        // trim() loại bỏ tab và ký tự xuống dòng ở hai đầu.
        assertEquals(
                EMPTY_CODE_ERROR,
                service.validateTicketCode("   \t\n  ")
        );
    }

    @Test
    void shouldAcceptNormalTicketCode() {
        assertNull(service.validateTicketCode("FC-123456"));
    }

    @Test
    void shouldAcceptOneCharacterTicketCode() {
        // Biên dưới của nhóm chuỗi không rỗng.
        assertNull(service.validateTicketCode("A"));
    }

    @Test
    void shouldAcceptTicketCodeWithExactlyOneHundredCharacters() {
        String ticketCode = "A".repeat(100);

        assertNull(service.validateTicketCode(ticketCode));
    }

    @Test
    void shouldRejectTicketCodeWithOneHundredAndOneCharacters() {
        String ticketCode = "A".repeat(101);

        assertEquals(
                INVALID_CODE_ERROR,
                service.validateTicketCode(ticketCode)
        );
    }

    @Test
    void shouldCountTicketCodeLengthAfterTrimmingOuterSpaces() {
        // Chuỗi ban đầu dài 104 ký tự nhưng sau trim còn đúng 100.
        String ticketCode = "  " + "A".repeat(100) + "  ";

        assertNull(service.validateTicketCode(ticketCode));
    }
}
