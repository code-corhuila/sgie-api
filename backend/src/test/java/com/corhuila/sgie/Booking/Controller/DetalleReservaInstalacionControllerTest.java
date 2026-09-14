package com.corhuila.sgie.Booking.Controller;

import com.corhuila.sgie.Booking.DTO.*;
import com.corhuila.sgie.Booking.IService.IDetalleReservaInstalacionService;
import com.corhuila.sgie.Booking.Service.DetalleReservaInstalacionService;
import com.corhuila.sgie.User.Service.AuthenticatedPersonaResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetalleReservaInstalacionControllerTest {

    @Mock
    private IDetalleReservaInstalacionService detalleReservaInstalacionServiceFacade;
    @Mock
    private DetalleReservaInstalacionService detalleReservaInstalacionService;
    @Mock
    private AuthenticatedPersonaResolver authenticatedPersonaResolver;

    private DetalleReservaInstalacionController controller;

    @BeforeEach
    void setup() {
        controller = new DetalleReservaInstalacionController(detalleReservaInstalacionServiceFacade, detalleReservaInstalacionService, authenticatedPersonaResolver);
    }

    @Test
    void cerrarDetalleReservaInstalacionDevuelveRespuesta() {
        CerrarDetalleReservaInstalacionRequestDTO request = new CerrarDetalleReservaInstalacionRequestDTO();
        request.setEntregaInstalacion("Devuelto");

        CerrarDetalleReservaInstalacionResponseDTO dto = new CerrarDetalleReservaInstalacionResponseDTO(1L, false, "Devuelto", LocalDateTime.now(), 2L);
        when(detalleReservaInstalacionService.cerrarDetalleReservaInstalacion(1L, "Devuelto"))
                .thenReturn(dto);

        ResponseEntity<CerrarDetalleReservaInstalacionResponseDTO> response = controller.cerrarDetalleReservaInstalacion(1L, request);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void actualizarDetalleReservaInstalacionDevuelveRespuesta() {
        ActualizarReservaDetalleInstalacionRequestDTO request = new ActualizarReservaDetalleInstalacionRequestDTO();
        request.setProgramaAcademico("Ingeniería");

        DetalleReservaInstalacionResponseDTO dto = new DetalleReservaInstalacionResponseDTO();
        dto.setProgramaAcademico("Ingeniería");
        when(detalleReservaInstalacionService.actualizarDetalleReservaInstalacion(2L, request)).thenReturn(dto);

        ResponseEntity<DetalleReservaInstalacionResponseDTO> response = controller.actualizarDetalleReservaInstalacion(2L, request);
        assertThat(response.getBody().getProgramaAcademico()).isEqualTo("Ingeniería");
    }

    @Test
    void findReservaInstalacionDelegatesToFacade() {
        Authentication authentication = mock(Authentication.class);
        IReservaInstalacionDTO dto = mock(IReservaInstalacionDTO.class);
        when(authenticatedPersonaResolver.resolverNumeroIdentificacionPermitido(authentication, "123")).thenReturn("123");
        when(detalleReservaInstalacionServiceFacade.findReservaInstalacionByNumeroIdentificacion("123"))
                .thenReturn(List.of(dto));

        ResponseEntity<List<IReservaInstalacionDTO>> response = controller.findReservaInstalacionByNumeroIdentificacion("123", authentication);
        assertThat(response.getBody()).containsExactly(dto);
    }
}
