package com.corhuila.sgie.User.Controller;

import com.corhuila.sgie.Config.JwtCookieProperties;
import com.corhuila.sgie.Security.JwtUtil;
import com.corhuila.sgie.User.DTO.LoginRequest;
import com.corhuila.sgie.User.DTO.UsuarioCreateRequest;
import com.corhuila.sgie.User.DTO.UsuarioResponse;
import com.corhuila.sgie.User.DTO.UsuarioUpdateRequest;
import com.corhuila.sgie.User.Entity.Persona;
import com.corhuila.sgie.User.Entity.Usuario;
import com.corhuila.sgie.User.IRepository.IUsuarioRepository;
import com.corhuila.sgie.User.IService.IUsuarioService;
import com.corhuila.sgie.common.ApiResponseDto;
import com.corhuila.sgie.common.EstadoDTO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("v1/api/usuario")
public class UsuarioController {

    private static final String ENTITY_NAME = "USUARIO";
    private static final String ROLE_PREFIX = "ROLE_";

    private final IUsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final IUsuarioRepository usuarioRepository;
    private final JwtCookieProperties jwtCookieProperties;

    public UsuarioController(IUsuarioService usuarioService,
                             AuthenticationManager authenticationManager,
                             JwtUtil jwtUtil,
                             IUsuarioRepository usuarioRepository,
                             JwtCookieProperties jwtCookieProperties) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
        this.jwtCookieProperties = jwtCookieProperties;
    }

    @GetMapping
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, '" + ENTITY_NAME + "', 'CONSULTAR')")
    public ResponseEntity<ApiResponseDto<List<UsuarioResponse>>> listarActivos() {
        List<UsuarioResponse> data = usuarioService.findByStateTrue()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(new ApiResponseDto<>("Datos obtenidos", data, true));
    }

    @GetMapping("{id}")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, '" + ENTITY_NAME + "', 'CONSULTAR')")
    public ResponseEntity<ApiResponseDto<UsuarioResponse>> obtenerPorId(@PathVariable Long id) throws Exception {
        Usuario usuario = usuarioService.findById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Registro encontrado", toResponse(usuario), true));
    }

    @PostMapping
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, '" + ENTITY_NAME + "', 'CREAR')")
    public ResponseEntity<ApiResponseDto<UsuarioResponse>> crear(@Valid @RequestBody UsuarioCreateRequest request) throws Exception {
        Usuario nuevo = toEntity(request);
        Usuario guardado = usuarioService.save(nuevo);
        return ResponseEntity.ok(new ApiResponseDto<>("Datos guardados", toResponse(guardado), true));
    }

    @PutMapping("{id}")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, '" + ENTITY_NAME + "', 'ACTUALIZAR')")
    public ResponseEntity<ApiResponseDto<Void>> actualizar(@PathVariable Long id,
                                                           @Valid @RequestBody UsuarioUpdateRequest request) throws Exception {
        Usuario cambios = toEntity(request);
        usuarioService.update(id, cambios);
        return ResponseEntity.ok(new ApiResponseDto<>("Datos actualizados", null, true));
    }

    @PutMapping("{id}/cambiar-estado")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, '" + ENTITY_NAME + "', 'ACTUALIZAR')")
    public ResponseEntity<ApiResponseDto<Void>> cambiarEstado(@PathVariable Long id,
                                                              @Valid @RequestBody EstadoDTO estadoDto) throws Exception {
        usuarioService.cambiarEstado(id, estadoDto.getEstado());
        return ResponseEntity.ok(new ApiResponseDto<>("Estado actualizado", null, true));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, '" + ENTITY_NAME + "', 'ELIMINAR')")
    public ResponseEntity<ApiResponseDto<Void>> eliminar(@PathVariable Long id) throws Exception {
        usuarioService.delete(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Registro eliminado", null, true));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest req, HttpServletResponse response) {

        Authentication authResult = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        UserDetails ud = (UserDetails) authResult.getPrincipal();

        Usuario usuario = usuarioRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String token = jwtUtil.generateToken(usuario.getId(), ud.getUsername(), ud.getAuthorities());

        ResponseCookie cookie = buildTokenCookie(token, jwtCookieProperties.getMaxAgeSeconds());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        List<String> roles = ud.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith(ROLE_PREFIX))
                .map(a -> a.substring(ROLE_PREFIX.length()))
                .toList();

        List<String> permisos = ud.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith(ROLE_PREFIX))
                .toList();

        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("email", usuario.getEmail());
        resp.put("idUsuario", usuario.getId());
        resp.put("roles", roles);
        resp.put("permisos", permisos);

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith(ROLE_PREFIX))
                .map(a -> a.substring(ROLE_PREFIX.length()))
                .toList();

        List<String> permisos = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith(ROLE_PREFIX))
                .toList();

        Map<String, Object> resp = new HashMap<>();
        resp.put("idUsuario", usuario.getId());
        resp.put("email", usuario.getEmail());
        resp.put("roles", roles);
        resp.put("permisos", permisos);

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = buildTokenCookie("", 0);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }

    private ResponseCookie buildTokenCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(jwtCookieProperties.getName(), value)
                .httpOnly(true)
                .secure(jwtCookieProperties.isSecure())
                .sameSite(jwtCookieProperties.getSameSite())
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        UsuarioResponse dto = new UsuarioResponse();
        dto.setId(usuario.getId());
        dto.setEmail(usuario.getEmail());
        dto.setState(usuario.getState());
        dto.setCreatedAt(usuario.getCreatedAt());
        dto.setUpdatedAt(usuario.getUpdatedAt());

        if (usuario.getPersona() != null) {
            dto.setPersonaId(usuario.getPersona().getId());
            dto.setPersonaNombres(usuario.getPersona().getNombres());
            dto.setPersonaApellidos(usuario.getPersona().getApellidos());
            dto.setPersonaNumeroIdentificacion(usuario.getPersona().getNumeroIdentificacion());
            if (usuario.getPersona().getRol() != null) {
                dto.setRoles(List.of(usuario.getPersona().getRol().getNombre()));
            } else {
                dto.setRoles(Collections.emptyList());
            }
        } else {
            dto.setRoles(Collections.emptyList());
        }

        return dto;
    }

    private Usuario toEntity(UsuarioCreateRequest request) {
        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setPassword(request.getPassword());
        usuario.setPersona(buildPersonaReference(request.getPersonaId()));
        return usuario;
    }

    private Usuario toEntity(UsuarioUpdateRequest request) {
        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setPassword(request.getPassword());
        if (request.getPersonaId() != null) {
            usuario.setPersona(buildPersonaReference(request.getPersonaId()));
        }
        return usuario;
    }

    private Persona buildPersonaReference(Long personaId) {
        Persona persona = new Persona();
        persona.setId(personaId);
        return persona;
    }
}
