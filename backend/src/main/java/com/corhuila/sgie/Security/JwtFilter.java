package com.corhuila.sgie.Security;

import com.corhuila.sgie.Config.JwtCookieProperties;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtCookieProperties jwtCookieProperties;

    public JwtFilter(JwtUtil jwtUtil,
                     CustomUserDetailsService customUserDetailsService,
                     JwtCookieProperties jwtCookieProperties) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtCookieProperties = jwtCookieProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Permitir todas las solicitudes OPTIONS (preflight CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (esRutaPublica(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extraerToken(request);

        if (token != null) {
            autenticarUsuario(token);
        }

        filterChain.doFilter(request, response);
    }

    private boolean esRutaPublica(String path) {
        return path.startsWith("/v1/api/usuario/login") ||
                //path.startsWith("/v1/api/usuario/me") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api/equipos/reportes") ||
                path.startsWith("/actuator") ||
                path.startsWith("/error") ||
                path.startsWith("/auth");
    }

    private String extraerToken(HttpServletRequest request) {
        String token = extraerTokenDeCookie(request);

        if (token == null) {
            token = extraerTokenDeHeader(request);
        }

        return token;
    }

    private String extraerTokenDeCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (jwtCookieProperties.getName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private String extraerTokenDeHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    private void autenticarUsuario(String token) {
        try {
            String username = jwtUtil.extractUsername(token);

            if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                return;
            }

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(token, userDetails)) {
                establecerAutenticacion(token, userDetails);
            }
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
            log.debug("Token JWT inválido o usuario no disponible: {}", ex.getMessage());
        }
    }

    private void establecerAutenticacion(String token, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        Long idUsuario = jwtUtil.extractUserId(token);
        authToken.setDetails(idUsuario);
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }


    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }
}
