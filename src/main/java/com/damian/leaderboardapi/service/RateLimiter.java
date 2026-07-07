package com.damian.leaderboardapi.service;

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
    protected abstract boolean isAllowed(String origin);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!isAllowed(request.getRemoteAddr())) {
            response.setStatus(429); // too many requests status code
            return;
        }

        filterChain.doFilter(request, response);
    }
}
