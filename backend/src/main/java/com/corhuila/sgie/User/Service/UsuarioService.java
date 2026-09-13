package com.corhuila.sgie.User.Service;

import com.corhuila.sgie.User.Entity.Persona;
import com.corhuila.sgie.User.Entity.Usuario;
import com.corhuila.sgie.User.IRepository.IPersonaRepository;
import com.corhuila.sgie.User.IRepository.IUsuarioRepository;
import com.corhuila.sgie.User.IService.IUsuarioService;
import com.corhuila.sgie.common.BaseService;
import com.corhuila.sgie.common.IBaseRepository;
import com.corhuila.sgie.common.ResourceNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UsuarioService extends BaseService<Usuario> implements IUsuarioService {

    private final IUsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final IPersonaRepository personaRepository;

    public UsuarioService(IUsuarioRepository repository, PasswordEncoder passwordEncoder, IPersonaRepository personaRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.personaRepository = personaRepository;
    }

    @Override
    protected IBaseRepository<Usuario, Long> getRepository() {
        return repository;
    }

    @Override
    @Transactional(rollbackFor = DataAccessException.class)
    public Usuario save(Usuario entity) throws DataAccessException {
        if (entity.getPersona() == null || entity.getPersona().getId() == null) {
            throw new IllegalStateException("La persona asociada es obligatoria");
        }

        Persona persona = personaRepository.findById(entity.getPersona().getId())
                .orElseThrow(() -> new IllegalStateException("La persona no existe"));

        if (persona.getUsuario() != null) {
            throw new IllegalStateException("La persona ya tiene un usuario asociado");
        }

        entity.setPersona(persona);
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        persona.setUsuario(entity);

        return super.save(entity);
    }


    @Override
    @Transactional(rollbackFor = DataAccessException.class)
    public void update(Long id, Usuario entity) throws DataAccessException {
        Optional<Usuario> op = repository.findById(id);

        if (op.isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        } else if (op.get().getDeletedAt() != null) {
            throw new IllegalStateException("Usuario inhabilitado");
        }

        Usuario usuarioUpdate = op.get();

        if (entity.getPersona() != null) {
            if (entity.getPersona().getId() == null) {
                throw new IllegalStateException("La persona asociada es obligatoria");
            }

            Persona persona = personaRepository.findById(entity.getPersona().getId())
                    .orElseThrow(() -> new IllegalStateException("La persona no existe"));

            if (persona.getUsuario() != null && !persona.getUsuario().getId().equals(usuarioUpdate.getId())) {
                throw new IllegalStateException("La persona ya tiene un usuario asociado");
            }

            usuarioUpdate.setPersona(persona);
            persona.setUsuario(usuarioUpdate);
        }

        if (entity.getEmail() != null) {
            usuarioUpdate.setEmail(entity.getEmail());
        }

        if (entity.getPassword() != null && !entity.getPassword().isBlank()) {
            usuarioUpdate.setPassword(passwordEncoder.encode(entity.getPassword()));
        }

        usuarioUpdate.setUpdatedAt(LocalDateTime.now());
        repository.save(usuarioUpdate);
    }

}
