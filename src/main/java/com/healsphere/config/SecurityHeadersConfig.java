package com.healsphere.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Adds basic safe HTTP headers to every response. */
@Configuration
public class SecurityHeadersConfig {

    @Bean
    public OncePerRequestFilter securityHeadersFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                    throws ServletException, IOException {
                res.setHeader("X-Content-Type-Options", "nosniff");
                res.setHeader("X-Frame-Options", "SAMEORIGIN"); // SAMEORIGIN so the H2 console still works locally
                res.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                res.setHeader("Permissions-Policy", "xr-spatial-tracking=(self), camera=(), microphone=(), geolocation=()");
                chain.doFilter(req, res);
            }
        };
    }
}
