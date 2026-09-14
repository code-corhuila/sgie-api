package com.corhuila.sgie.User.Controller;

import com.corhuila.sgie.User.DTO.IPermisoPorPersonaDTO;
import com.corhuila.sgie.User.DTO.IPermisoRolEntidadDTO;
import com.corhuila.sgie.User.Entity.PermisoRolEntidad;
import com.corhuila.sgie.User.IService.IPermisoRolEntidadService;
import com.corhuila.sgie.common.BaseController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/api/permiso-rol-entidad")
public class PermisoRolEntidadController extends BaseController<PermisoRolEntidad, IPermisoRolEntidadService> {
    public PermisoRolEntidadController(IPermisoRolEntidadService service) {
        super(service, "PERMISO_ROL_ENTIDAD");
    }

    @GetMapping("/persona-permisos-rol-entidad")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'CONSULTAR')")
    public ResponseEntity<List<IPermisoPorPersonaDTO>> obtenerPermisos(@RequestParam String numeroIdentificacion) {
        List<IPermisoPorPersonaDTO> permisos = service.obtenerPermisos(numeroIdentificacion);
        return ResponseEntity.ok(permisos);
    }

    @GetMapping("/todos-permisos-rol-entidad")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'CONSULTAR')")
    public ResponseEntity<List<IPermisoRolEntidadDTO>> findPermisosByRolByEntidad() {
        List<IPermisoRolEntidadDTO> permisos = service.findPermisosByRolByEntidad();
        return ResponseEntity.ok(permisos);
    }
}
