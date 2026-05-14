package com.gbsw.snapy.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbsw.snapy.global.exception.ErrorCode;
import com.gbsw.snapy.global.exception.ErrorResponse;
import com.gbsw.snapy.global.security.ratelimit.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_RATE_LIMIT_LIMIT = "X-RateLimit-Limit";
    private static final String X_RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";
    private static final String X_RATE_LIMIT_RESET = "X-RateLimit-Reset";

    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final ConcurrentMap<String, RequestCounter> counters = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanupAt = new AtomicLong();

    public RateLimitFilter(RateLimitProperties properties, ObjectMapper objectMapper) {
        this(properties, objectMapper, Clock.systemUTC());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!properties.isEnabled() || isPreflight(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        long now = clock.millis();
        long windowMillis = properties.getWindow().toMillis();
        cleanupExpiredCounters(now, windowMillis);

        String key = resolveClientIp(request);
        AtomicReference<RateLimitResult> result = new AtomicReference<>();
        counters.compute(key, (ignored, current) -> {
            if (current == null || current.isExpired(now)) {
                RequestCounter next = new RequestCounter(now + windowMillis, 1);
                result.set(next.toResult());
                return next;
            }
            current.increment();
            result.set(current.toResult());
            return current;
        });

        RateLimitResult rateLimitResult = result.get();
        long remaining = Math.max(0, properties.getMaxRequests() - rateLimitResult.count());
        addRateLimitHeaders(response, rateLimitResult, remaining);

        if (rateLimitResult.count() > properties.getMaxRequests()) {
            writeRateLimitResponse(response, rateLimitResult);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPreflight(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void cleanupExpiredCounters(long now, long windowMillis) {
        long cleanupInterval = Math.max(windowMillis, 1);
        long previousCleanupAt = lastCleanupAt.get();
        if (now - previousCleanupAt < cleanupInterval
                || !lastCleanupAt.compareAndSet(previousCleanupAt, now)) {
            return;
        }

        counters.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private void addRateLimitHeaders(HttpServletResponse response, RateLimitResult result, long remaining) {
        response.setHeader(X_RATE_LIMIT_LIMIT, String.valueOf(properties.getMaxRequests()));
        response.setHeader(X_RATE_LIMIT_REMAINING, String.valueOf(remaining));
        response.setHeader(X_RATE_LIMIT_RESET, String.valueOf(result.resetAtEpochSeconds()));
    }

    private void writeRateLimitResponse(HttpServletResponse response, RateLimitResult result) throws IOException {
        long retryAfterSeconds = Math.max(1, (result.resetAtMillis() - clock.millis() + 999) / 1000);

        response.setStatus(ErrorCode.RATE_LIMIT_EXCEEDED.getStatus().value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.of(ErrorCode.RATE_LIMIT_EXCEEDED)));
    }

    private static class RequestCounter {

        private final long resetAtMillis;
        private int count;

        private RequestCounter(long resetAtMillis, int count) {
            this.resetAtMillis = resetAtMillis;
            this.count = count;
        }

        private boolean isExpired(long now) {
            return now >= resetAtMillis;
        }

        private void increment() {
            count++;
        }

        private RateLimitResult toResult() {
            return new RateLimitResult(count, resetAtMillis);
        }
    }

    private record RateLimitResult(int count, long resetAtMillis) {

        private long resetAtEpochSeconds() {
            return (resetAtMillis + 999) / 1000;
        }
    }
}
