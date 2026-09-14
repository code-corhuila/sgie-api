package com.corhuila.sgie.Booking.Controller;

import com.corhuila.sgie.Booking.DTO.HoraDisponibleDTO;
import com.corhuila.sgie.Booking.DTO.IReservaGeneralDTO;
import com.corhuila.sgie.Booking.DTO.ReservaGeneralReporteDTO;
import com.corhuila.sgie.Booking.IService.IReservaService;
import com.corhuila.sgie.Booking.Service.ReservaService;
import com.corhuila.sgie.User.Service.AuthenticatedPersonaResolver;
import com.corhuila.sgie.common.Reporting.GeneradorReporteUtil;
import com.corhuila.sgie.common.Reporting.ReportFormat;
import com.corhuila.sgie.common.Reporting.ReporteGenericoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaControllerTest {

    @Mock
    private IReservaService reservaServiceFacade;
    @Mock
    private ReporteGenericoService reporteGenericoService;
    @Mock
    private ReservaService reservaService;
    @Mock
    private AuthenticatedPersonaResolver authenticatedPersonaResolver;

    private ReservaController controller;

    @BeforeEach
    void setup() {
        controller = new ReservaController(reservaServiceFacade, reporteGenericoService, reservaService, authenticatedPersonaResolver);
    }

    @Test
    void getHorasDisponiblesInstalacionDelegatesToService() {
        List<HoraDisponibleDTO> horas = List.of(new HoraDisponibleDTO("08:00"));
        when(reservaServiceFacade.getHorasDisponiblesInstalacion(any(LocalDate.class), anyInt(), any(), any()))
                .thenReturn(horas);

        List<HoraDisponibleDTO> result = controller.getHorasDisponiblesInstalacion(LocalDate.now(), 1, null, null);

        assertThat(result).isEqualTo(horas);
        verify(reservaServiceFacade).getHorasDisponiblesInstalacion(any(), anyInt(), any(), any());
    }

    @Test
    void getHorasDisponiblesEquipoDelegatesToService() {
        List<HoraDisponibleDTO> horas = List.of(new HoraDisponibleDTO("09:00"));
        when(reservaServiceFacade.getHorasDisponiblesEquipo(any(LocalDate.class), anyInt(), any(), any()))
                .thenReturn(horas);

        assertThat(controller.getHorasDisponiblesEquipo(LocalDate.now(), 2, 10L, null)).isEqualTo(horas);
        verify(reservaServiceFacade).getHorasDisponiblesEquipo(any(), anyInt(), any(), any());
    }

    @Test
    void findReservasYMantenimientosDevuelveRespuesta() {
        Authentication authentication = mock(Authentication.class);
        IReservaGeneralDTO dto = mock(IReservaGeneralDTO.class);
        when(authenticatedPersonaResolver.resolverNumeroIdentificacionPermitido(authentication, "123")).thenReturn("123");
        when(reservaServiceFacade.findReservasYMantenimientosByNumeroIdentificacion("123"))
                .thenReturn(List.of(dto));

        ResponseEntity<List<IReservaGeneralDTO>> response = controller.findReservasYMantenimientosByNumeroIdentificacion("123", authentication);

        assertThat(response.getBody()).containsExactly(dto);
    }

    @Test
    void exportarReservasMemoriaConstruyeReporte() throws Exception {
        Authentication authentication = mock(Authentication.class);
        ReservaGeneralReporteDTO dto = sampleReporte();
        when(authenticatedPersonaResolver.resolverNumeroIdentificacionPermitido(authentication, "123")).thenReturn("123");
        when(reservaService.obtenerDatosEnMemoria("123")).thenReturn(List.of(dto));

        GeneradorReporteUtil.GeneratedReport report = new GeneradorReporteUtil.GeneratedReport(
                new ByteArrayResource("demo".getBytes()),
                MediaType.APPLICATION_OCTET_STREAM,
                "reporte_reservas.csv");
        when(reporteGenericoService.generar(eq(ReportFormat.CSV), anyList(), eq(ReservaGeneralReporteDTO.class), anyString(), anyString()))
                .thenReturn(report);

        ResponseEntity<StreamingResponseBody> response = controller.exportarReservas("csv", "memoria", "123", authentication);

        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("reporte_reservas.csv");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.getBody().writeTo(baos);
        assertThat(baos.toByteArray()).isNotEmpty();
    }

    @Test
    void exportarReservasStreamingDelegatesToReporteGenerico() {
        Authentication authentication = mock(Authentication.class);
        ReservaGeneralReporteDTO dto = sampleReporte();
        when(authenticatedPersonaResolver.resolverNumeroIdentificacionPermitido(authentication, null)).thenReturn(null);
        when(reservaService.proveedorStream(null)).thenReturn(() -> Stream.of(dto));

        ResponseEntity<StreamingResponseBody> streaming = ResponseEntity.ok(outputStream -> {
        });
        when(reporteGenericoService.generarStreaming(eq(ReportFormat.PDF), any(), eq(ReservaGeneralReporteDTO.class), anyString(), anyString()))
                .thenReturn(streaming);

        ResponseEntity<StreamingResponseBody> response = controller.exportarReservas("pdf", "stream", null, authentication);
        assertThat(response).isSameAs(streaming);
    }

    private ReservaGeneralReporteDTO sampleReporte() {
        ReservaGeneralReporteDTO dto = new ReservaGeneralReporteDTO();
        dto.setTipoReserva("Equipo");
        dto.setNombreReserva("Laboratorio");
        dto.setDescripcionReserva("Práctica");
        dto.setFechaReserva(java.sql.Date.valueOf(LocalDate.now()));
        dto.setHoraInicioReserva(java.sql.Time.valueOf(LocalTime.of(8, 0)));
        dto.setHoraFinReserva(java.sql.Time.valueOf(LocalTime.of(9, 0)));
        dto.setNombrePersona("Usuario");
        dto.setNumeroIdentificacion("123");
        dto.setNombreInstalacion("Sala 101");
        dto.setNombreEquipo("Proyector");
        dto.setProgramaAcademico("Ingeniería");
        dto.setNumeroEstudiantes(20);
        dto.setTipoMantenimiento("Preventivo");
        dto.setDescripcionMantenimiento("Rev");
        dto.setEstadoReserva("ACTIVA");
        return dto;
    }
}
