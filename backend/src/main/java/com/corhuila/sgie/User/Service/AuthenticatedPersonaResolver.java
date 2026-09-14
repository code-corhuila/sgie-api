package com.corhuila.sgie.User.Service;

import com.corhuila.sgie.User.Entity.Usuario;
import com.corhuila.sgie.User.IRepository.IUsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AuthenticatedPersonaResolver {

    private static final Set<String> ROLES_CONSULTA_GLOBAL = Set.of("ROLE_ADMINISTRADOR", "ROLE_COORDINADOR_RESERVAS");

    private final IUsuarioRepository usuarioRepository;

    public AuthenticatedPersonaResolver(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String resolverNumeroIdentificacionPermitido(Authentication authentication, String numeroIdentificacionSolicitado) {
        if (tieneAccesoGlobal(authentication)) {
            return numeroIdentificacionSolicitado;
        }
        return numeroIdentificacionPropia(authentication);
    }

    private boolean tieneAccesoGlobal(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ROLES_CONSULTA_GLOBAL::contains);
    }

    private String numeroIdentificacionPropia(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (usuario.getPersona() == null) {
            throw new IllegalStateException("El usuario autenticado no tiene una persona asociada");
        }

        return usuario.getPersona().getNumeroIdentificacion();
    }
}
