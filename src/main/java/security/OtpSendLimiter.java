package security;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Shared, in-memory OTP send limiter for registration and password reset.
 * Intended for the single-Tomcat classroom deployment; in a clustered
 * deployment the buckets must be moved to a shared persistent store.
 */
public final class OtpSendLimiter {

    private static final long COOLDOWN_MS = 60_000L;
    private static final long WINDOW_MS = 15 * 60_000L;
    private static final int MAX_PER_EMAIL = 5;
    private static final int MAX_PER_IP = 30;
    private static final int MAX_BUCKETS = 10_000;

    private static final Map<String, Bucket> BUCKETS
            = new LinkedHashMap<String, Bucket>(128, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Bucket> entry) {
            return size() > MAX_BUCKETS;
        }
    };

    private OtpSendLimiter() {
    }

    /**
     * Reserve a send slot before contacting SMTP, preventing concurrent
     * requests from flooding the same email address.
     *
     * @return null when allowed, otherwise a safe user-facing message
     */
    public static synchronized String checkAndRecord(
            String purpose, String email, String ip) {

        String normalizedEmail = email == null
                ? "" : email.trim().toLowerCase(Locale.ROOT);
        String normalizedIp = ip == null ? "unknown" : ip.trim();

        if (normalizedEmail.isEmpty()) {
            return "Vui lòng nhập email.";
        }

        long now = System.currentTimeMillis();
        String prefix = "OTP:" + purpose + ":";
        String emailKey = prefix + "EMAIL:" + normalizedEmail;
        String ipKey = prefix + "IP:" + normalizedIp;

        Bucket emailBucket = BUCKETS.computeIfAbsent(emailKey, k -> new Bucket());
        Bucket ipBucket = BUCKETS.computeIfAbsent(ipKey, k -> new Bucket());

        expire(emailBucket, now);
        expire(ipBucket, now);

        if ((!emailBucket.sentAt.isEmpty()
                && now - emailBucket.sentAt.peekLast() < COOLDOWN_MS)
                || emailBucket.sentAt.size() >= MAX_PER_EMAIL
                || ipBucket.sentAt.size() >= MAX_PER_IP) {
            return "Bạn đã yêu cầu OTP quá nhanh. Vui lòng thử lại sau.";
        }

        emailBucket.sentAt.addLast(now);
        ipBucket.sentAt.addLast(now);
        return null;
    }

    private static void expire(Bucket bucket, long now) {
        while (!bucket.sentAt.isEmpty()
                && now - bucket.sentAt.peekFirst() > WINDOW_MS) {
            bucket.sentAt.removeFirst();
        }
    }

    private static final class Bucket {
        final Deque<Long> sentAt = new ArrayDeque<>();
    }
}
