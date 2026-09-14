package com.corhuila.sgie.Security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordAuthenticationTest {

    private static final String EMAIL = "demo@mail.com";
    private static final String PASSWORD_CORRECTA = "correcta123";

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    private AuthenticationManager buildAuthenticationManager() {
        UserDetails userDetails = User.withUsername(EMAIL)
                .password(passwordEncoder.encode(PASSWORD_CORRECTA))
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))
                .build();

        UserDetailsService userDetailsService = username -> userDetails;

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(List.of(provider));
    }

    @Test
    void rechazaUnaContraseñaIncorrecta() {
        AuthenticationManager authenticationManager = buildAuthenticationManager();

        assertThatThrownBy(() -> authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(EMAIL, "cualquierOtraCosa")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void aceptaLaContraseñaCorrecta() {
        AuthenticationManager authenticationManager = buildAuthenticationManager();

        Authentication result = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD_CORRECTA));

        assertThat(result.isAuthenticated()).isTrue();
    }
}
