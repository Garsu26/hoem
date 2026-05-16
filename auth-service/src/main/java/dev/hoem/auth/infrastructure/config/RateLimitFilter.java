package dev.hoem.auth.infrastructure.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    private static final String REGISTER_PATH = "/api/v1/auth/register";
    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String RESET_REQUEST_PATH = "/api/v1/auth/password-reset/request";

    private final Map<String, Bucket> registerBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> resetRequestBuckets = new ConcurrentHashMap<>();

    private final int registerCapacity;
    private final int registerRefillMinutes;
    private final int loginCapacity;
    private final int loginRefillMinutes;
    private final int resetCapacity;
    private final int resetRefillMinutes;

    public RateLimitFilter(
            @Value("${app.rate-limit.register.capacity}") int registerCapacity,
            @Value("${app.rate-limit.register.refill-minutes}") int registerRefillMinutes,
            @Value("${app.rate-limit.login.capacity}") int loginCapacity,
            @Value("${app.rate-limit.login.refill-minutes}") int loginRefillMinutes,
            @Value("${app.rate-limit.password-reset.capacity}") int resetCapacity,
            @Value("${app.rate-limit.password-reset.refill-minutes}") int resetRefillMinutes) {
        this.registerCapacity = registerCapacity;
        this.registerRefillMinutes = registerRefillMinutes;
        this.loginCapacity = loginCapacity;
        this.loginRefillMinutes = loginRefillMinutes;
        this.resetCapacity = resetCapacity;
        this.resetRefillMinutes = resetRefillMinutes;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if ("POST".equalsIgnoreCase(httpRequest.getMethod())) {
            String uri = httpRequest.getRequestURI();
            String ip = resolveClientIp(httpRequest);

            if (REGISTER_PATH.equals(uri)) {
                Bucket bucket = registerBuckets.computeIfAbsent(ip,
                        k -> newBucket(registerCapacity, registerRefillMinutes));
                if (!bucket.tryConsume(1)) {
                    rejectWithRateLimit((HttpServletResponse) response);
                    return;
                }
            } else if (LOGIN_PATH.equals(uri)) {
                Bucket bucket = loginBuckets.computeIfAbsent(ip,
                        k -> newBucket(loginCapacity, loginRefillMinutes));
                if (!bucket.tryConsume(1)) {
                    rejectWithRateLimit((HttpServletResponse) response);
                    return;
                }
            } else if (RESET_REQUEST_PATH.equals(uri)) {
                Bucket bucket = resetRequestBuckets.computeIfAbsent(ip,
                        k -> newBucket(resetCapacity, resetRefillMinutes));
                if (!bucket.tryConsume(1)) {
                    rejectWithRateLimit((HttpServletResponse) response);
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    private Bucket newBucket(int capacity, int refillMinutes) {
        Bandwidth limit = Bandwidth.classic(capacity,
                Refill.greedy(capacity, Duration.ofMinutes(refillMinutes)));
        return Bucket.builder().addLimit(limit).build();
    }

    private void rejectWithRateLimit(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many requests. Try again later.\"}");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}