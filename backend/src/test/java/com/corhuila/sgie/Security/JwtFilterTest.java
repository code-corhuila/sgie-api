package com.corhuila.sgie.Security;

import com.corhuila.sgie.Config.JwtCookieProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private CustomUserDetailsService customUserDetailsService;

    private JwtFilter filter;

    @BeforeEach
    void setup() {
        JwtCookieProperties properties = new JwtCookieProperties();
        properties.setName("token");
        filter = new JwtFilter(jwtUtil, customUserDetailsService, properties);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void autenticaSolicitudCuandoTokenValidoEnCookie() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/api/usuario");
        request.setCookies(new jakarta.servlet.http.Cookie("token", "abc"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        UserDetails userDetails = new User("demo@mail.com", "pass",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        when(jwtUtil.extractUsername("abc")).thenReturn("demo@mail.com");
        when(customUserDetailsService.loadUserByUsername("demo@mail.com")).thenReturn(userDetails);
        when(jwtUtil.validateToken("abc", userDetails)).thenReturn(true);
        when(jwtUtil.extractUserId("abc")).thenReturn(9L);

        filter.doFilter(request, response, chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.getDetails()).isEqualTo(9L);
    }

    @Test
    void ignoraRutasPublicas() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/api/usuario/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, customUserDetailsService);
    }

    @Test
    void noAutenticaCuandoTokenInvalido() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/api/usuario");
        request.setCookies(new jakarta.servlet.http.Cookie("token", "abc"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.extractUsername("abc")).thenReturn("demo@mail.com");
        UserDetails userDetails = new User("demo@mail.com", "pass", List.of());
        when(customUserDetailsService.loadUserByUsername("demo@mail.com")).thenReturn(userDetails);
        when(jwtUtil.validateToken("abc", userDetails)).thenReturn(false);

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void noLanzaExcepcionCuandoTokenExpiradoOMalformado() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/api/usuario");
        request.setCookies(new jakarta.servlet.http.Cookie("token", "expirado"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtUtil.extractUsername("expirado")).thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "token expirado"));

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
