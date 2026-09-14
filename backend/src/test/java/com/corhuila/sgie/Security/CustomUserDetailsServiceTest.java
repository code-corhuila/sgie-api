package com.corhuila.sgie.Security;

import com.corhuila.sgie.User.Entity.*;
import com.corhuila.sgie.User.IRepository.IPermisoRolEntidadRepository;
import com.corhuila.sgie.User.IRepository.IUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private IPermisoRolEntidadRepository permisoRolEntidadRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    private Usuario usuario;
    private Rol rol;

    @BeforeEach
    void setup() {
        rol = new Rol();
        rol.setId(2L);
        rol.setNombre("ADMIN");

        Persona persona = new Persona();
        persona.setId(5L);
        persona.setRol(rol);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("demo@mail.com");
        usuario.setPassword("hash");
        usuario.setState(true);
        usuario.setPersona(persona);
        persona.setUsuario(usuario);
    }

    @Test
    void loadUserByUsernameConstruyeAuthorities() {
        Permiso permiso = new Permiso();
        permiso.setNombre("CONSULTAR");
        Entidad entidad = new Entidad();
        entidad.setNombre("USUARIO");

        PermisoRolEntidad permisoRolEntidad = new PermisoRolEntidad();
        permisoRolEntidad.setPermiso(permiso);
        permisoRolEntidad.setEntidad(entidad);
        permisoRolEntidad.setRol(rol);

        when(usuarioRepository.findByEmail("demo@mail.com")).thenReturn(Optional.of(usuario));
        when(permisoRolEntidadRepository.findByRolIdAndStateTrue(anyLong()))
                .thenReturn(List.of(permisoRolEntidad));

        var userDetails = service.loadUserByUsername("demo@mail.com");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "USUARIO:CONSULTAR");
    }

    @Test
    void loadUserByUsernameLanzaSiNoExiste() {
        when(usuarioRepository.findByEmail("ghost@mail.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserByUsername("ghost@mail.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsernameLanzaSiNoTieneRol() {
        usuario.getPersona().setRol(null);
        when(usuarioRepository.findByEmail("demo@mail.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> service.loadUserByUsername("demo@mail.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsernameMarcaDeshabilitadoCuandoElUsuarioEstaInactivo() {
        usuario.setState(false);
        when(usuarioRepository.findByEmail("demo@mail.com")).thenReturn(Optional.of(usuario));
        when(permisoRolEntidadRepository.findByRolIdAndStateTrue(anyLong())).thenReturn(List.of());

        var userDetails = service.loadUserByUsername("demo@mail.com");

        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsernameMantieneHabilitadoCuandoElUsuarioEstaActivo() {
        when(usuarioRepository.findByEmail("demo@mail.com")).thenReturn(Optional.of(usuario));
        when(permisoRolEntidadRepository.findByRolIdAndStateTrue(anyLong())).thenReturn(List.of());

        var userDetails = service.loadUserByUsername("demo@mail.com");

        assertThat(userDetails.isEnabled()).isTrue();
    }
}
