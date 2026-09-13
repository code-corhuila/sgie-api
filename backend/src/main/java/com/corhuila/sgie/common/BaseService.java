package com.corhuila.sgie.common;

import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public abstract class BaseService<T extends Auditoria> implements IBaseService<T> {

    protected abstract IBaseRepository<T, Long> getRepository();

    @Override
    public List<T> all() {
        return getRepository().findAll();
    }

    @Override
    public List<T> findByStateTrue() {
        return getRepository().findAll()
                .stream()
                .filter(t -> Boolean.TRUE.equals(t.getState()))
                .toList();
    }

    @Override
    public T findById(Long id) throws DataAccessException {
        Optional<T> op = getRepository().findById(id);
        if (op.isEmpty()) {
            throw new ResourceNotFoundException("Registro no encontrado");
        }
        return op.get();
    }

    protected void beforeSave(T entity) {
        // Hook opcional
    }

    protected void afterSave(T entity) {
        // Hook opcional
    }

    protected String[] ignoredPropertiesOnUpdate() {
        return new String[]{"id", "createdAt", "updatedAt", "deletedAt", "state", "password"};
    }

    protected void copyUpdatableFields(T source, T target) {
        BeanUtils.copyProperties(source, target, ignoredPropertiesOnUpdate());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public T save(T entity) throws DataAccessException {
        beforeSave(entity);
        T saved = getRepository().save(entity);
        afterSave(saved);
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, T entity) throws DataAccessException {
        Optional<T> op = getRepository().findById(id);

        if (op.isEmpty()) {
            throw new ResourceNotFoundException("Registro no encontrado");
        } else if (op.get().getDeletedAt() != null) {
            throw new IllegalStateException("Registro inhabilitado");
        }

        T entityUpdate = op.get();
        copyUpdatableFields(entity, entityUpdate);
        entityUpdate.setUpdatedAt(LocalDateTime.now());
        getRepository().save(entityUpdate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) throws DataAccessException {
        Optional<T> op = getRepository().findById(id);
        if (op.isEmpty()) {
            throw new ResourceNotFoundException("Registro no encontrado");
        }

        T entityUpdate = op.get();
        entityUpdate.setDeletedAt(LocalDateTime.now());
        entityUpdate.setState(Boolean.FALSE);
        getRepository().save(entityUpdate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cambiarEstado(Long id, Boolean estado) throws DataAccessException {
        Optional<T> optional = getRepository().findById(id);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Registro no encontrado para actualizar estado");
        }

        T entity = optional.get();
        entity.setState(estado);
        getRepository().save(entity); // Aquí se activa @PreUpdate + AuditoriaListener
    }
}
