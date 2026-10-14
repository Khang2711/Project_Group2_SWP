package integration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Builds and verifies VNPay Pay API requests.
 * Secrets are read from environment variables and must never be committed.
 */
public class VnPayService {

    private static final String SANDBOX_URL
            = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final DateTimeFormatter VNP_DATE_FORMAT
            = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final String tmnCode = environment("VNPAY_TMN_CODE");
    private final String hashSecret = environment("VNPAY_HASH_SECRET");
    private final String paymentUrl = defaultIfBlank(
            environment("VNPAY_PAYMENT_URL"),
            SANDBOX_URL
    );
    private final String returnUrl = environment("VNPAY_RETURN_URL");

    public boolean isConfigured() {
        return !isBlank(tmnCode)
                && !isBlank(hashSecret)
                && !isBlank(returnUrl)
                && !isBlank(paymentUrl);
    }

    public String getTmnCode() {
        return tmnCode;
    }

    public String createPaymentUrl(
            String merchantReference,
            BigDecimal amount,
            String clientIp
    ) {
        if (!isConfigured()) {
            throw new IllegalStateException(
                    "VNPay chưa được cấu hình. Hãy thiết lập VNPAY_TMN_CODE, "
                    + "VNPAY_HASH_SECRET và VNPAY_RETURN_URL rồi khởi động lại Tomcat."
            );
        }

        if (isBlank(merchantReference) || amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Thông tin thanh toán không hợp lệ.");
        }

        LocalDateTime now = LocalDateTime.now(
                ZoneId.of("Asia/Ho_Chi_Minh")
        );

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", toVnPayAmount(amount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", merchantReference);
        params.put("vnp_OrderInfo", "Thanh toan don dat ve " + merchantReference);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", normalizeIp(clientIp));
        params.put("vnp_CreateDate", now.format(VNP_DATE_FORMAT));
        LocalDateTime referenceCreatedAt = referenceCreatedAt(merchantReference, now);
        params.put("vnp_ExpireDate", referenceCreatedAt.plusMinutes(15).format(VNP_DATE_FORMAT));

        String secureHash = hmacSha512(hashSecret, buildHashData(params));
        params.put("vnp_SecureHash", secureHash);

        return paymentUrl
                + (paymentUrl.contains("?") ? "&" : "?")
                + buildQueryString(params);
    }

    /**
     * Verifies the callback checksum using the VNPay HMAC-SHA512 rule.
     * vnp_SecureHash and vnp_SecureHashType are not included in the signed data.
     */
    public boolean verifySignature(Map<String, String> params) {
        if (!isConfigured() || params == null) {
            return false;
        }

        String receivedHash = params.get("vnp_SecureHash");
        if (isBlank(receivedHash)
                || !receivedHash.matches("(?i)[0-9a-f]{128}")) {
            return false;
        }

        String expectedHash = hmacSha512(
                hashSecret,
                buildHashData(params)
        );

        return MessageDigest.isEqual(
                expectedHash.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII),
                receivedHash.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII)
        );
    }

    public static String toVnPayAmount(BigDecimal amount) {
        return amount.movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY)
                .toBigIntegerExact()
                .toString();
    }

    private String buildHashData(Map<String, String> params) {
        List<String> keys = new ArrayList<>();

        for (String key : params.keySet()) {
            if (key != null
                    && key.startsWith("vnp_")
                    && !"vnp_SecureHash".equals(key)
                    && !"vnp_SecureHashType".equals(key)) {
                keys.add(key);
            }
        }

        Collections.sort(keys);

        StringBuilder data = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key);
            if (value == null || value.isEmpty()) {
                continue;
            }

            if (data.length() > 0) {
                data.append('&');
            }

            data.append(key)
                    .append('=')
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }

        return data.toString();
    }

    private String buildQueryString(Map<String, String> params) {
        StringBuilder query = new StringBuilder();

        for (Map.Entry<String, String> entry : new TreeMap<>(params).entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }

            if (query.length() > 0) {
                query.append('&');
            }

            query.append(URLEncoder.encode(
                    entry.getKey(),
                    StandardCharsets.UTF_8
            ));
            query.append('=');
            query.append(URLEncoder.encode(
                    entry.getValue(),
                    StandardCharsets.UTF_8
            ));
        }

        return query.toString();
    }

    private String hmacSha512(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA512"
            ));

            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);

            for (byte value : bytes) {
                hex.append(String.format(
                        Locale.ROOT,
                        "%02x",
                        value & 0xff
                ));
            }

            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Không thể tạo chữ ký VNPay.", e);
        }
    }

    private String normalizeIp(String ipAddress) {
        if (isBlank(ipAddress) || "::1".equals(ipAddress)) {
            return "127.0.0.1";
        }

        if (ipAddress.length() > 45) {
            throw new IllegalArgumentException("Địa chỉ IP không hợp lệ.");
        }

        return ipAddress;
    }

    private static String environment(String name) {
        String value = System.getenv(name);
        return value == null ? null : value.trim();
    }

    private static String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
