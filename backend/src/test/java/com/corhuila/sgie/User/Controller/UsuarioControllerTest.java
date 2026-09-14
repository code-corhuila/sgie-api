package com.corhuila.sgie.User.Controller;

import com.corhuila.sgie.Config.JwtCookieProperties;
import com.corhuila.sgie.Security.JwtUtil;
import com.corhuila.sgie.User.DTO.LoginRequest;
import com.corhuila.sgie.User.DTO.UsuarioCreateRequest;
import com.corhuila.sgie.User.DTO.UsuarioUpdateRequest;
import com.corhuila.sgie.User.Entity.Persona;
import com.corhuila.sgie.User.Entity.Rol;
import com.corhuila.sgie.User.Entity.Usuario;
import com.corhuila.sgie.User.IRepository.IUsuarioRepository;
import com.corhuila.sgie.User.IService.IUsuarioService;
import com.corhuila.sgie.common.ApiResponseDto;
import com.corhuila.sgie.common.EstadoDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private IUsuarioService usuarioService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private HttpServletResponse httpServletResponse;

    private JwtCookieProperties cookieProperties;

    @InjectMocks
    private UsuarioController controller;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        cookieProperties = new JwtCookieProperties();
        cookieProperties.setName("token");
        controller = new UsuarioController(usuarioService, authenticationManager,
                jwtUtil, usuarioRepository, cookieProperties);

        Rol rol = new Rol();
        rol.setNombre("ADMIN");

        Persona persona = new Persona();
        persona.setId(5L);
        persona.setNombres("Demo");
        persona.setRol(rol);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("demo@mail.com");
        usuario.setPersona(persona);
        usuario.setCreatedAt(LocalDateTime.now());
        usuario.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void listarActivosDevuelveDto() {
        when(usuarioService.findByStateTrue()).thenReturn(List.of(usuario));

        ResponseEntity<ApiResponseDto<List<com.corhuila.sgie.User.DTO.UsuarioResponse>>> response = controller.listarActivos();
        assertThat(response.getBody().getData()).hasSize(1);
    }

    @Test
    void obtenerPorIdUsaServicio() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(usuario);

        ResponseEntity<ApiResponseDto<com.corhuila.sgie.User.DTO.UsuarioResponse>> response = controller.obtenerPorId(1L);
        assertThat(response.getBody().getData().getEmail()).isEqualTo("demo@mail.com");
    }

    @Test
    void crearInvocaServicio() throws Exception {
        UsuarioCreateRequest request = new UsuarioCreateRequest();
        request.setEmail("nuevo@mail.com");
        request.setPassword("password123");
        request.setPersonaId(5L);

        when(usuarioService.save(any(Usuario.class))).thenReturn(usuario);

        ResponseEntity<ApiResponseDto<com.corhuila.sgie.User.DTO.UsuarioResponse>> response = controller.crear(request);
        assertThat(response.getBody().getData().getEmail()).isEqualTo("demo@mail.com");
    }

    @Test
    void actualizarInvocaServicio() throws Exception {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setEmail("cambio@mail.com");

        controller.actualizar(1L, request);
        verify(usuarioService).update(eq(1L), any(Usuario.class));
    }

    @Test
    void cambiarEstadoInvocaServicio() throws Exception {
        EstadoDTO dto = new EstadoDTO();
        dto.setEstado(false);

        controller.cambiarEstado(1L, dto);
        verify(usuarioService).cambiarEstado(1L, false);
    }

    @Test
    void eliminarInvocaDelete() throws Exception {
        controller.eliminar(1L);
        verify(usuarioService).delete(1L);
    }

    @Test
    void loginGeneraCookieYRespuesta() {
        cookieProperties.setSecure(true);
        cookieProperties.setSameSite("None");
        cookieProperties.setMaxAgeSeconds(7200L);

        LoginRequest request = new LoginRequest();
        request.setEmail("demo@mail.com");
        request.setPassword("plain");

        User springUser = new User("demo@mail.com", "hash",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        Authentication authResult = new UsernamePasswordAuthenticationToken(springUser, "plain", springUser.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authResult);
        when(usuarioRepository.findByEmail("demo@mail.com")).thenReturn(Optional.of(usuario));
        when(jwtUtil.generateToken(eq(1L), eq("demo@mail.com"), any())).thenReturn("jwt-token");

        ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);

        ResponseEntity<Map<String, Object>> response = controller.login(request, httpServletResponse);

        assertThat(response.getBody().get("token")).isEqualTo("jwt-token");
        verify(httpServletResponse).addHeader(eq(HttpHeaders.SET_COOKIE), cookieCaptor.capture());
        String rawCookie = cookieCaptor.getValue();
        assertThat(rawCookie).contains("token=jwt-token")
                .contains("SameSite=None")
                .contains("Secure")
                .contains("Max-Age=7200");
    }

    @Test
    void loginLanzaCuandoCredencialesInvalidas() {
        LoginRequest request = new LoginRequest();
        request.setEmail("demo@mail.com");
        request.setPassword("incorrecta");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> controller.login(request, httpServletResponse));
        verify(httpServletResponse, never()).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        verifyNoInteractions(usuarioRepository, jwtUtil);
    }

    @Test
    void loginLanzaSiUsuarioNoExisteTrasAutenticar() {
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@mail.com");
        request.setPassword("plain");

        User springUser = new User("missing@mail.com", "hash", List.of());
        Authentication authResult = new UsernamePasswordAuthenticationToken(springUser, "plain", springUser.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authResult);
        when(usuarioRepository.findByEmail("missing@mail.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> controller.login(request, httpServletResponse));
        verify(httpServletResponse, never()).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }

    @Test
    void meDevuelveInformacionCuándoAutenticado() {
        User springUser = new User("demo@mail.com", "hash",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        Authentication auth = new UsernamePasswordAuthenticationToken(springUser, null, springUser.getAuthorities());

        when(usuarioRepository.findByEmail("demo@mail.com")).thenReturn(Optional.of(usuario));

        ResponseEntity<Map<String, Object>> response = controller.me(auth);
        assertThat(response.getBody()).containsEntry("email", "demo@mail.com");
    }

    @Test
    void meRetorna401SiNoAutenticado() {
        ResponseEntity<Map<String, Object>> response = controller.me(null);
        assertThat(response.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    void meLanzaCuandoUsuarioNoExiste() {
        User springUser = new User("ghost@mail.com", "hash", List.of());
        Authentication auth = new UsernamePasswordAuthenticationToken(springUser, null, springUser.getAuthorities());

        when(usuarioRepository.findByEmail("ghost@mail.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> controller.me(auth));
    }

    @Test
    void listarActivosSinRolRetornaRolesVacios() {
        Persona personaSinRol = new Persona();
        personaSinRol.setId(77L);
        personaSinRol.setNombres("Sin Rol");

        Usuario sinRol = new Usuario();
        sinRol.setId(2L);
        sinRol.setEmail("sinrol@mail.com");
        sinRol.setPersona(personaSinRol);

        when(usuarioService.findByStateTrue()).thenReturn(List.of(sinRol));

        ResponseEntity<ApiResponseDto<List<com.corhuila.sgie.User.DTO.UsuarioResponse>>> response = controller.listarActivos();
        assertThat(response.getBody().getData())
                .singleElement()
                .extracting(com.corhuila.sgie.User.DTO.UsuarioResponse::getRoles)
                .asList()
                .isEmpty();
    }

    @Test
    void logoutLimpiaCookie() {
        ResponseEntity<Void> response = controller.logout(httpServletResponse);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(httpServletResponse).addHeader(eq(HttpHeaders.SET_COOKIE), contains("token="));
    }
}
