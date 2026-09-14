package com.corhuila.sgie.Site.Controller;

import com.corhuila.sgie.Site.DTO.IInstalacionCampusDTO;
import com.corhuila.sgie.Site.DTO.InstalacionReporteDTO;
import com.corhuila.sgie.Site.Entity.Instalacion;
import com.corhuila.sgie.Site.IService.IInstalacionService;
import com.corhuila.sgie.Site.Service.InstalacionService;
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
@RequestMapping("v1/api/instalacion")
public class InstalacionController extends BaseController<Instalacion, IInstalacionService> {

    private final ReporteGenericoService reporteGenericoService;
    private final InstalacionService instalacionService;

    public InstalacionController(IInstalacionService service, ReporteGenericoService reporteGenericoService, InstalacionService instalacionService) {
        super(service, "INSTALACION");
        this.reporteGenericoService = reporteGenericoService;
        this.instalacionService = instalacionService;
    }

    @GetMapping("/instalacion-campus")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'CONSULTAR')")
    public ResponseEntity<List<IInstalacionCampusDTO>> findInstalacionesCampus(@RequestParam String nombreInstalacion, @RequestParam String nombreCampus) {
        List<IInstalacionCampusDTO> instalacionesCampus = service.findInstalacionesCampus(nombreInstalacion, nombreCampus);
        return ResponseEntity.ok(instalacionesCampus);
    }

    @GetMapping(value = "/reporte/{formato}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'CONSULTAR')")
    @Transactional(readOnly = true)
    public ResponseEntity<StreamingResponseBody> exportarInstalaciones(@PathVariable("formato") String formato, @RequestParam(defaultValue = "stream") String modo) {
        ReportFormat format = ReportFormat.fromName(formato);
        Supplier<Stream<InstalacionReporteDTO>> supplier = instalacionService.proveedorStream();

        if (!isStreaming(modo)) {
            List<InstalacionReporteDTO> datos = instalacionService.obtenerDatosEnMemoria();
            GeneradorReporteUtil.GeneratedReport reporte = reporteGenericoService.generar(
                    format,
                    datos,
                    InstalacionReporteDTO.class,
                    "reporte_instalaciones",
                    "Reporte de instalaciones"
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
                InstalacionReporteDTO.class,
                "reporte_instalaciones",
                "Reporte de instalaciones"
        );
    }
}
