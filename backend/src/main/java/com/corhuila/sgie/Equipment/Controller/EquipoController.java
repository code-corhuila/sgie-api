package com.corhuila.sgie.Equipment.Controller;

import com.corhuila.sgie.Equipment.DTO.EquipoReporteDTO;
import com.corhuila.sgie.Equipment.DTO.HojaDeVidaEquipoDTO;
import com.corhuila.sgie.Equipment.DTO.IEquipoInstalacionDTO;
import com.corhuila.sgie.Equipment.Entity.Equipo;
import com.corhuila.sgie.Equipment.IService.IEquipoService;
import com.corhuila.sgie.Equipment.Service.EquipoService;
import com.corhuila.sgie.Equipment.Service.HojaDeVidaEquipoService;
import com.corhuila.sgie.common.BaseController;
import com.corhuila.sgie.common.Reporting.GeneradorReporteUtil;
import com.corhuila.sgie.common.Reporting.ReportFormat;
import com.corhuila.sgie.common.Reporting.ReporteGenericoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.corhuila.sgie.common.Reporting.HelperUtils.buildHeaders;
import static com.corhuila.sgie.common.Reporting.HelperUtils.isStreaming;

@RestController
@RequestMapping("v1/api/equipo")
public class EquipoController extends BaseController<Equipo, IEquipoService> {

    private final ReporteGenericoService reporteGenericoService;
    private final HojaDeVidaEquipoService hojaDeVidaEquipoService;
    private final EquipoService equipoService;

    public EquipoController(IEquipoService service, HojaDeVidaEquipoService hojaDeVidaEquipoService, ReporteGenericoService reporteGenericoService, EquipoService equipoService) {
        super(service, "EQUIPO");
        this.hojaDeVidaEquipoService = hojaDeVidaEquipoService;
        this.reporteGenericoService = reporteGenericoService;
        this.equipoService = equipoService;
    }

    @GetMapping("/equipo-instalacion")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'CONSULTAR')")
    public ResponseEntity<List<IEquipoInstalacionDTO>> findEquiposInstalaciones(@RequestParam(required = false) String codigoEquipo, @RequestParam(required = false) String nombreInstalacion) {
        List<IEquipoInstalacionDTO> equiposInstalaciones = service.findEquiposInstalaciones(codigoEquipo, nombreInstalacion);
        return ResponseEntity.ok(equiposInstalaciones);
    }

    @GetMapping("/hoja-vida-equipo/{idEquipo}")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'CONSULTAR')")
    public ResponseEntity<HojaDeVidaEquipoDTO> getHojaDeVida(@PathVariable Long idEquipo) {
        HojaDeVidaEquipoDTO hoja = hojaDeVidaEquipoService.getHojaDeVidaEquipo(idEquipo);
        return ResponseEntity.ok(hoja);
    }

    @GetMapping(value = "/reporte/{formato}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'CONSULTAR')")
    @Transactional(readOnly = true)
    public ResponseEntity<StreamingResponseBody> exportarEquipos(@PathVariable("formato") String formato, @RequestParam(defaultValue = "stream") String modo) {
        ReportFormat format = ReportFormat.fromName(formato);
        Supplier<Stream<EquipoReporteDTO>> supplier = equipoService.proveedorStream();

        if (!isStreaming(modo)) {
            List<EquipoReporteDTO> datos = equipoService.obtenerDatosEnMemoria();
            GeneradorReporteUtil.GeneratedReport reporte = reporteGenericoService.generar(
                    format,
                    datos,
                    EquipoReporteDTO.class,
                    "reporte_equipos",
                    "Reporte de equipos"
            );

            StreamingResponseBody body = outputStream -> {
                try (var input = reporte.resource().getInputStream()) {
                    input.transferTo(outputStream);
                }
            };

            HttpHeaders headers = buildHeaders(reporte);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(body);
        }

        return reporteGenericoService.generarStreaming(
                format,
                supplier,
                EquipoReporteDTO.class,
                "reporte_equipos",
                "Reporte de equipos"
        );
    }
}
