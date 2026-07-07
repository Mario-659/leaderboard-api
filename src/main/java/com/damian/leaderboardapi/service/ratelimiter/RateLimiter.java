package com.damian.leaderboardapi.service.ratelimiter;

import com.damian.leaderboardapi.dto.RateLimitAttemptDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Order(Ordered.HIGHEST_PRECEDENCE)
public abstract class RateLimiter extends OncePerRequestFilter {
    protected final Long windowDurationsSeconds;
    protected final Long maxRequestsPerWindow;

    protected RateLimiter(
            Long windowDurationSeconds,
            Long maxRequestsPerWindow
    ) {
        this.windowDurationsSeconds = windowDurationSeconds;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        RateLimitAttemptDto attempt = attempt(request.getRemoteAddr());

        if (!attempt.isAllowed()) {
            response.setStatus(429); // too many requests status code
            response.setHeader("Retry-After", attempt.retryAfter().toString());
            return;
        }

        filterChain.doFilter(request, response);
    }

    protected abstract RateLimitAttemptDto attempt(String origin);
}
