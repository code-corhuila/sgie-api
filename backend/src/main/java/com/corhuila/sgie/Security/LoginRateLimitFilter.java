package com.corhuila.sgie.Security;

import com.corhuila.sgie.common.ApiResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final Logger securityLog = LoggerFactory.getLogger("SecurityEvents");
    private static final String LOGIN_PATH = "/v1/api/usuario/login";

    private final LoginRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public LoginRateLimitFilter(LoginRateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ConsumptionProbe probe = rateLimiter.tryConsume(request.getRemoteAddr());

        if (!probe.isConsumed()) {
            long waitSeconds = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000);
            securityLog.warn("Rate limit de login excedido para IP {}", request.getRemoteAddr());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(waitSeconds));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(
                    new ApiResponseDto<>("Demasiados intentos de inicio de sesión. Intente de nuevo en " + waitSeconds + " segundos.", null, false)));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        return LOGIN_PATH.equals(request.getServletPath()) || LOGIN_PATH.equals(request.getRequestURI());
    }
}
