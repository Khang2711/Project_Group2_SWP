package security;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class LoginAttemptLimiter {

    private static final int MAX_PAIR_ATTEMPTS = 5;
    private static final int MAX_IP_ATTEMPTS = 30;

    private static final long WINDOW_MS = 15 * 60 * 1000L;
    private static final long BLOCK_MS = 5 * 60 * 1000L;

    private static final int MAX_BUCKETS = 10000;

    // Giới hạn số phần tử để tránh Map tăng vô hạn.
    private final Map<String, AttemptBucket> buckets =
            new LinkedHashMap<String, AttemptBucket>(
                    128, 0.75f, true) {

        @Override
        protected boolean removeEldestEntry(
                Map.Entry<String, AttemptBucket> eldest) {
            return size() > MAX_BUCKETS;
        }
    };

    public synchronized boolean isBlocked(
            String email, String ip) {

        long now = System.currentTimeMillis();

        return isBlocked(pairKey(email, ip), now)
                || isBlocked(ipKey(ip), now);
    }

    public synchronized void recordFailure(
            String email, String ip) {

        long now = System.currentTimeMillis();

        record(pairKey(email, ip),
                MAX_PAIR_ATTEMPTS, now);

        record(ipKey(ip),
                MAX_IP_ATTEMPTS, now);
    }

    public synchronized void recordSuccess(
            String email, String ip) {

        // Chỉ reset bộ đếm của cặp email + IP.
        buckets.remove(pairKey(email, ip));
    }

    private boolean isBlocked(String key, long now) {

        AttemptBucket bucket = buckets.get(key);

        if (bucket == null) {
            return false;
        }

        removeExpired(bucket, now);

        return bucket.blockedUntil > now;
    }

    private void record(
            String key, int maxAttempts, long now) {

        AttemptBucket bucket =
                buckets.computeIfAbsent(
                        key, k -> new AttemptBucket());

        removeExpired(bucket, now);

        if (bucket.blockedUntil > now) {
            return;
        }

        bucket.failures.addLast(now);

        if (bucket.failures.size() >= maxAttempts) {

            bucket.blockedUntil = now + BLOCK_MS;
            bucket.failures.clear();
        }
    }

    private void removeExpired(
            AttemptBucket bucket, long now) {

        while (!bucket.failures.isEmpty()
                && now - bucket.failures.peekFirst()
                > WINDOW_MS) {

            bucket.failures.removeFirst();
        }

        if (bucket.blockedUntil <= now) {
            bucket.blockedUntil = 0L;
        }
    }

    private String pairKey(String email, String ip) {

        String normalizedEmail = email == null
                ? ""
                : email.trim().toLowerCase(Locale.ROOT);

        if (normalizedEmail.length() > 100) {
            normalizedEmail =
                    normalizedEmail.substring(0, 100);
        }

        return "PAIR:" + normalizedEmail
                + "|" + safeIp(ip);
    }

    private String ipKey(String ip) {
        return "IP:" + safeIp(ip);
    }

    private String safeIp(String ip) {
        return ip == null ? "unknown" : ip;
    }

    private static class AttemptBucket {

        private final Deque<Long> failures =
                new ArrayDeque<>();

        private long blockedUntil = 0L;
    }
}