package com.corhuila.sgie.User.Service;

import com.corhuila.sgie.User.Entity.Persona;
import com.corhuila.sgie.User.Entity.Usuario;
import com.corhuila.sgie.User.IRepository.IUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticatedPersonaResolverTest {

    @Mock
    private IUsuarioRepository usuarioRepository;

    private AuthenticatedPersonaResolver resolver;

    @BeforeEach
    void setup() {
        resolver = new AuthenticatedPersonaResolver(usuarioRepository);
    }

    @Test
    void usuarioSinRolElevadoSoloPuedeConsultarSuPropiaIdentificacion() {
        Authentication authentication = autenticacionCon("estudiante@mail.com", "ROLE_ESTUDIANTE");

        Persona persona = new Persona();
        persona.setNumeroIdentificacion("111");
        Usuario usuario = new Usuario();
        usuario.setPersona(persona);
        when(usuarioRepository.findByEmail("estudiante@mail.com")).thenReturn(Optional.of(usuario));

        String idPermitido = resolver.resolverNumeroIdentificacionPermitido(authentication, "999-de-otra-persona");

        assertThat(idPermitido).isEqualTo("111");
    }

    @Test
    void usuarioConRolAdministradorPuedeConsultarCualquierIdentificacion() {
        Authentication authentication = autenticacionCon("admin@mail.com", "ROLE_ADMINISTRADOR");

        String idPermitido = resolver.resolverNumeroIdentificacionPermitido(authentication, "999-de-otra-persona");

        assertThat(idPermitido).isEqualTo("999-de-otra-persona");
    }

    @Test
    void usuarioConRolCoordinadorReservasPuedeConsultarCualquierIdentificacion() {
        Authentication authentication = autenticacionCon("coordinador@mail.com", "ROLE_COORDINADOR_RESERVAS");

        String idPermitido = resolver.resolverNumeroIdentificacionPermitido(authentication, "999-de-otra-persona");

        assertThat(idPermitido).isEqualTo("999-de-otra-persona");
    }

    @Test
    void lanzaExcepcionSiElUsuarioAutenticadoNoExiste() {
        Authentication authentication = autenticacionCon("fantasma@mail.com", "ROLE_ESTUDIANTE");
        when(usuarioRepository.findByEmail("fantasma@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.resolverNumeroIdentificacionPermitido(authentication, "123"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void lanzaExcepcionSiElUsuarioNoTienePersonaAsociada() {
        Authentication authentication = autenticacionCon("sinpersona@mail.com", "ROLE_ESTUDIANTE");
        when(usuarioRepository.findByEmail("sinpersona@mail.com")).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> resolver.resolverNumeroIdentificacionPermitido(authentication, "123"))
                .isInstanceOf(IllegalStateException.class);
    }

    private Authentication autenticacionCon(String email, String rol) {
        return new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority(rol)));
    }
}
