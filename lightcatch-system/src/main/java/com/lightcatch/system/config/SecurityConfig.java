package com.lightcatch.system.config;

import com.lightcatch.common.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
public class SecurityConfig {

    private static final List<String> IGNORE_PATHS = Arrays.asList(
            "/api/auth/login", "/api/auth/register",
            "/api/auth/test", "/api/debug",
            "/swagger-ui", "/api-docs", "/v3/api-docs"
    );
    private static final Logger apiLog = LoggerFactory.getLogger("API_LOGGER");

    @Bean
    public FilterRegistrationBean<LogFilter> logFilter() {
        FilterRegistrationBean<LogFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new LogFilter());
        bean.addUrlPatterns("/api/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilter() {
        FilterRegistrationBean<JwtAuthFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new JwtAuthFilter());
        bean.addUrlPatterns("/api/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return bean;
    }

    /** Logs every API request and response */
    public static class LogFilter implements Filter {
        @Override
        public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest request = (HttpServletRequest) req;
            long start = System.currentTimeMillis();
            String qs = request.getQueryString();
            String params = qs != null ? "?" + qs : "";
            apiLog.info(">>> {} {}{}", request.getMethod(), request.getRequestURI(), params);
            try {
                chain.doFilter(req, res);
            } finally {
                long ms = System.currentTimeMillis() - start;
                apiLog.info("<<< {} ({}ms)", request.getRequestURI(), ms);
            }
        }
    }

    public static class JwtAuthFilter implements Filter {
        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                             FilterChain chain) throws IOException, ServletException {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;

            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "X-Access-Token, Content-Type, Authorization");
            response.setHeader("Access-Control-Expose-Headers", "X-Access-Token");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                response.setStatus(200);
                return;
            }

            String path = request.getRequestURI();
            if (IGNORE_PATHS.stream().anyMatch(path::startsWith)) {
                chain.doFilter(request, response);
                return;
            }

            String token = request.getHeader("X-Access-Token");
            if (token == null || token.isEmpty()) {
                token = request.getParameter("token");
            }

            if (token == null || token.isEmpty() || !JwtUtil.validateToken(token)) {
                apiLog.warn("401 REJECTED: {} (token={})", path, token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null");
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"未登录或token已过期\"}");
                return;
            }

            chain.doFilter(request, response);
        }
    }
}
