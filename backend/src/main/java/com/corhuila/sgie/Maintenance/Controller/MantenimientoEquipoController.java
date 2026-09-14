package com.corhuila.sgie.Maintenance.Controller;

import com.corhuila.sgie.Maintenance.DTO.*;
import com.corhuila.sgie.Maintenance.Entity.MantenimientoEquipo;
import com.corhuila.sgie.Maintenance.Service.MantenimientoEquipoService;
import com.corhuila.sgie.User.Service.AuthenticatedPersonaResolver;
import com.corhuila.sgie.common.BaseController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/api/mantenimiento-equipo")
public class MantenimientoEquipoController extends BaseController<MantenimientoEquipo, MantenimientoEquipoService> {

    private final MantenimientoEquipoService mantenimientoEquipoService;
    private final AuthenticatedPersonaResolver authenticatedPersonaResolver;

    public MantenimientoEquipoController(MantenimientoEquipoService service, MantenimientoEquipoService mantenimientoEquipoService,
                                         AuthenticatedPersonaResolver authenticatedPersonaResolver) {
        super(service, "MANTENIMIENTO_EQUIPO");
        this.mantenimientoEquipoService = mantenimientoEquipoService;
        this.authenticatedPersonaResolver = authenticatedPersonaResolver;
    }

    @PutMapping("/{idDetalle}/cerrar-mantenimiento-equipo")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'ACTUALIZAR')")
    public ResponseEntity<CerrarMantenimientoEquipoResponseDTO> cerrarMantenimientoEquipo(
            @PathVariable Long idDetalle,
            @RequestBody CerrarMantenimientoEquipoDTO request) {

        CerrarMantenimientoEquipoResponseDTO actualizado =
                mantenimientoEquipoService.cerrarMantenimientoEquipo(idDetalle, request.getFechaProximaMantenimiento(), request.getResultadoMantenimiento());

        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{idMantenimiento}/actualizar-mantenimiento-equipo")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'ACTUALIZAR')")
    public ResponseEntity<MantenimientoEquipoResponseDTO> actualizarMantenimientoEquipo(
            @PathVariable Long idMantenimiento,
            @RequestBody ActualizarMantenimientoEquipoRequestDTO request) {

        MantenimientoEquipoResponseDTO actualizado = mantenimientoEquipoService.actualizarMantenimientoEquipo(idMantenimiento, request);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/mantenimientos-equipos")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'CONSULTAR')")
    public ResponseEntity<List<IMantenimientoEquipoDTO>> findMantenimientosEquipoByNumeroIdentificacion(
            @RequestParam String numeroIdentificacion, Authentication authentication) {
        String idPermitido = authenticatedPersonaResolver.resolverNumeroIdentificacionPermitido(authentication, numeroIdentificacion);
        List<IMantenimientoEquipoDTO> mantenimientosEquipos = service.findMantenimientosEquipoByNumeroIdentificacion(idPermitido);
        return ResponseEntity.ok(mantenimientosEquipos);
    }

}