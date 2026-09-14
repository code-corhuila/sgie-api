package com.corhuila.sgie.Booking.Controller;

import com.corhuila.sgie.Booking.DTO.*;
import com.corhuila.sgie.Booking.Entity.DetalleReservaInstalacion;
import com.corhuila.sgie.Booking.IService.IDetalleReservaInstalacionService;
import com.corhuila.sgie.Booking.Service.DetalleReservaInstalacionService;
import com.corhuila.sgie.User.Service.AuthenticatedPersonaResolver;
import com.corhuila.sgie.common.BaseController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/api/detalle-reserva-instalacion")
public class DetalleReservaInstalacionController extends BaseController<DetalleReservaInstalacion, IDetalleReservaInstalacionService> {

    private final DetalleReservaInstalacionService detalleReservaInstalacionService;
    private final AuthenticatedPersonaResolver authenticatedPersonaResolver;


    public DetalleReservaInstalacionController(IDetalleReservaInstalacionService service, DetalleReservaInstalacionService detalleReservaInstalacionService,
                                               AuthenticatedPersonaResolver authenticatedPersonaResolver) {
        super(service, "DETALLE_RESERVA_INSTALACION");
        this.detalleReservaInstalacionService = detalleReservaInstalacionService;
        this.authenticatedPersonaResolver = authenticatedPersonaResolver;
    }


    @PutMapping("/{idDetalle}/cerrar-detalle-reserva-instalacion")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'ACTUALIZAR')")
    public ResponseEntity<CerrarDetalleReservaInstalacionResponseDTO> cerrarDetalleReservaInstalacion(
            @PathVariable Long idDetalle,
            @RequestBody CerrarDetalleReservaInstalacionRequestDTO request) {

        CerrarDetalleReservaInstalacionResponseDTO actualizado =
                detalleReservaInstalacionService.cerrarDetalleReservaInstalacion(idDetalle, request.getEntregaInstalacion());

        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{idDetalle}/actualizar-detalle-reserva")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'ACTUALIZAR')")
    public ResponseEntity<DetalleReservaInstalacionResponseDTO> actualizarDetalleReservaInstalacion(
            @PathVariable Long idDetalle,
            @RequestBody ActualizarReservaDetalleInstalacionRequestDTO request) {

        DetalleReservaInstalacionResponseDTO actualizado =
                detalleReservaInstalacionService.actualizarDetalleReservaInstalacion(idDetalle, request);

        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/reservas-instalaciones")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'CONSULTAR')")
    public ResponseEntity<List<IReservaInstalacionDTO>> findReservaInstalacionByNumeroIdentificacion(
            @RequestParam String numeroIdentificacion, Authentication authentication) {
        String idPermitido = authenticatedPersonaResolver.resolverNumeroIdentificacionPermitido(authentication, numeroIdentificacion);
        List<IReservaInstalacionDTO> reservasInstalaciones = service.findReservaInstalacionByNumeroIdentificacion(idPermitido);
        return ResponseEntity.ok(reservasInstalaciones);
    }
}
