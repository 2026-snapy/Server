package com.gbsw.snapy.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gbsw.snapy.global.security.ratelimit.RateLimitProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-05-14T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void blocksRequestsOverLimit() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(properties(2), objectMapper(), FIXED_CLOCK);

        MockHttpServletResponse firstResponse = doFilter(filter, request("203.0.113.1"));
        MockHttpServletResponse secondResponse = doFilter(filter, request("203.0.113.1"));
        MockHttpServletResponse thirdResponse = doFilter(filter, request("203.0.113.1"));

        assertThat(firstResponse.getStatus()).isEqualTo(200);
        assertThat(secondResponse.getStatus()).isEqualTo(200);
        assertThat(thirdResponse.getStatus()).isEqualTo(429);
        assertThat(thirdResponse.getHeader("Retry-After")).isEqualTo("60");
        assertThat(thirdResponse.getContentAsString()).contains("요청이 너무 많습니다");
    }

    @Test
    void usesFirstForwardedForIpAsClientKey() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(properties(1), objectMapper(), FIXED_CLOCK);

        MockHttpServletResponse firstResponse = doFilter(filter, request("198.51.100.10", "203.0.113.10, 198.51.100.10"));
        MockHttpServletResponse secondResponse = doFilter(filter, request("198.51.100.11", "203.0.113.10, 198.51.100.11"));

        assertThat(firstResponse.getStatus()).isEqualTo(200);
        assertThat(secondResponse.getStatus()).isEqualTo(429);
    }

    @Test
    void skipsOptionsPreflight() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(properties(0), objectMapper(), FIXED_CLOCK);
        MockHttpServletRequest request = request("203.0.113.1");
        request.setMethod("OPTIONS");

        MockHttpServletResponse response = doFilter(filter, request);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void countsConcurrentRequestsConsistently() throws Exception {
        int maxRequests = 20;
        int totalRequests = 50;
        RateLimitFilter filter = new RateLimitFilter(properties(maxRequests), objectMapper(), FIXED_CLOCK);
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Callable<Integer>> tasks = IntStream.range(0, totalRequests)
                .mapToObj(ignored -> (Callable<Integer>) () -> doFilter(filter, request("203.0.113.1")).getStatus())
                .toList();

        List<Integer> statuses = executorService.invokeAll(tasks).stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                })
                .toList();
        executorService.shutdown();
        boolean terminated = executorService.awaitTermination(1, TimeUnit.SECONDS);

        assertThat(terminated).isTrue();
        assertThat(statuses).filteredOn(status -> status == 200).hasSize(maxRequests);
        assertThat(statuses).filteredOn(status -> status == 429).hasSize(totalRequests - maxRequests);
    }

    private RateLimitProperties properties(int maxRequests) {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setMaxRequests(maxRequests);
        properties.setWindow(Duration.ofMinutes(1));
        return properties;
    }

    private ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    private MockHttpServletRequest request(String remoteAddr) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(remoteAddr);
        request.setRequestURI("/api/albums");
        return request;
    }

    private MockHttpServletRequest request(String remoteAddr, String forwardedFor) {
        MockHttpServletRequest request = request(remoteAddr);
        request.addHeader("X-Forwarded-For", forwardedFor);
        return request;
    }

    private MockHttpServletResponse doFilter(RateLimitFilter filter, MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
