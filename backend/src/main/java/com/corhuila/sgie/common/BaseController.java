package com.corhuila.sgie.common;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public class BaseController<T extends Auditoria, S extends IBaseService<T>> {
    protected S service;
    protected String entityName;

    protected BaseController(S service, String entityName) {
        this.service = service;
        this.entityName = entityName;
    }

    public String getEntityName() {
        return entityName;
    }

    @GetMapping
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'CONSULTAR')")
    public ResponseEntity<ApiResponseDto<List<T>>> findByStateTrue() {
        return ResponseEntity.ok(new ApiResponseDto<List<T>>("Datos obtenidos", service.findByStateTrue(), true));
    }

    @GetMapping("{id}")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'CONSULTAR')")
    public ResponseEntity<ApiResponseDto<T>> show(@PathVariable Long id) {
        T entity = service.findById(id);
        return ResponseEntity.ok(new ApiResponseDto<T>("Registro encontrado", entity, true));
    }

    @PostMapping
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'CREAR')")
    public ResponseEntity<ApiResponseDto<T>> save(@RequestBody T entity) {
        return ResponseEntity.ok(new ApiResponseDto<T>("Datos guardados", service.save(entity), true));
    }

    @PutMapping("{id}")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'ACTUALIZAR')")
    public ResponseEntity<ApiResponseDto<T>> update(@PathVariable Long id, @RequestBody T entity) {
        service.update(id, entity);
        return ResponseEntity.ok(new ApiResponseDto<T>("Datos actualizados", null, true));
    }

    @PutMapping("{id}/cambiar-estado")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'ACTUALIZAR')")
    public ResponseEntity<ApiResponseDto<T>> cambiarEstado(@PathVariable Long id, @RequestBody EstadoDTO estadoDto) {
        service.cambiarEstado(id, estadoDto.getEstado());
        return ResponseEntity.ok(new ApiResponseDto<T>("Estado actualizado", null, true));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'ELIMINAR')")
    public ResponseEntity<ApiResponseDto<T>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponseDto<T>("Registro eliminado", null, true));
    }

}