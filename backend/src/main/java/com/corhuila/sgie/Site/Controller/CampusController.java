package com.corhuila.sgie.Site.Controller;

import com.corhuila.sgie.Site.DTO.CampusReporteDTO;
import com.corhuila.sgie.Site.Entity.Campus;
import com.corhuila.sgie.Site.IRepository.ICampusRepository;
import com.corhuila.sgie.Site.Service.CampusService;
import com.corhuila.sgie.common.ApiResponseDto;
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
@RequestMapping("v1/api/campus")
public class CampusController extends BaseController<Campus, CampusService> {

    private final ReporteGenericoService reporteGenericoService;
    private final ICampusRepository repository;
    private final CampusService campusService;

    public CampusController(CampusService service, ReporteGenericoService reporteGenericoService, ICampusRepository repository) {
        super(service, "CAMPUS");
        this.reporteGenericoService = reporteGenericoService;
        this.repository = repository;
        this.campusService = service;
    }

    @GetMapping("/por-municipio/{id}")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'CONSULTAR')")
    public ResponseEntity<ApiResponseDto<List<Campus>>> byMunicipio(@PathVariable Long id) {
        List<Campus> data = repository.findByMunicipioIdAndStateTrue(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Datos obtenidos", data, true));
    }

    @GetMapping(value = "/reporte/{formato}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'CONSULTAR')")
    @Transactional(readOnly = true)
    public ResponseEntity<StreamingResponseBody> exportarCampus(@PathVariable("formato") String formato, @RequestParam(defaultValue = "stream") String modo) {
        ReportFormat format = ReportFormat.fromName(formato);
        Supplier<Stream<CampusReporteDTO>> supplier = campusService.proveedorStream();

        if (!isStreaming(modo)) {
            List<CampusReporteDTO> datos = campusService.obtenerDatosEnMemoria();
            GeneradorReporteUtil.GeneratedReport reporte = reporteGenericoService.generar(
                    format,
                    datos,
                    CampusReporteDTO.class,
                    "reporte_campus",
                    "Reporte de campus"
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
                CampusReporteDTO.class,
                "reporte_campus",
                "Reporte de campus"
        );
    }
}
