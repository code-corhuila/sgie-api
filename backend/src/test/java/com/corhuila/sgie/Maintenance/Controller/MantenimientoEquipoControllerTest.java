package com.corhuila.sgie.Maintenance.Controller;

import com.corhuila.sgie.Maintenance.DTO.*;
import com.corhuila.sgie.Maintenance.Entity.MantenimientoEquipo;
import com.corhuila.sgie.Maintenance.Service.MantenimientoEquipoService;
import com.corhuila.sgie.User.Service.AuthenticatedPersonaResolver;
import com.corhuila.sgie.common.EstadoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MantenimientoEquipoControllerTest {

    @Mock
    private MantenimientoEquipoService service;
    @Mock
    private AuthenticatedPersonaResolver authenticatedPersonaResolver;

    private MantenimientoEquipoController controller;

    @BeforeEach
    void setup() {
        controller = new MantenimientoEquipoController(service, service, authenticatedPersonaResolver);
    }

    @Test
    void cerrarMantenimientoEquipoRetornaRespuesta() {
        CerrarMantenimientoEquipoDTO request = new CerrarMantenimientoEquipoDTO();
        request.setFechaProximaMantenimiento(LocalDate.now().plusDays(10));
        request.setResultadoMantenimiento("OK");

        CerrarMantenimientoEquipoResponseDTO dto = new CerrarMantenimientoEquipoResponseDTO(1L, false,
                LocalDate.now().plusDays(10), "OK", LocalDateTime.now(), 5L);
        when(service.cerrarMantenimientoEquipo(anyLong(), any(), any())).thenReturn(dto);

        ResponseEntity<CerrarMantenimientoEquipoResponseDTO> response = controller.cerrarMantenimientoEquipo(1L, request);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void actualizarMantenimientoEquipoDevuelveDto() {
        ActualizarMantenimientoEquipoRequestDTO request = new ActualizarMantenimientoEquipoRequestDTO();
        request.setDescripcion("Nuevo mantenimiento");

        MantenimientoEquipoResponseDTO dto = new MantenimientoEquipoResponseDTO();
        dto.setDescripcion("Nuevo mantenimiento");
        when(service.actualizarMantenimientoEquipo(3L, request)).thenReturn(dto);

        ResponseEntity<MantenimientoEquipoResponseDTO> response = controller.actualizarMantenimientoEquipo(3L, request);
        assertThat(response.getBody().getDescripcion()).isEqualTo("Nuevo mantenimiento");
    }

    @Test
    void findMantenimientosEquipoDelegatesToService() {
        Authentication authentication = mock(Authentication.class);
        IMantenimientoEquipoDTO dto = mock(IMantenimientoEquipoDTO.class);
        when(authenticatedPersonaResolver.resolverNumeroIdentificacionPermitido(authentication, "123")).thenReturn("123");
        when(service.findMantenimientosEquipoByNumeroIdentificacion("123"))
                .thenReturn(List.of(dto));

        ResponseEntity<List<IMantenimientoEquipoDTO>> response = controller.findMantenimientosEquipoByNumeroIdentificacion("123", authentication);
        assertThat(response.getBody()).containsExactly(dto);
    }

    @Test
    void baseControllerOperacionesFun() throws Exception {
        MantenimientoEquipo entity = new MantenimientoEquipo();
        when(service.findByStateTrue()).thenReturn(List.of(entity));
        when(service.findById(2L)).thenReturn(entity);
        when(service.save(entity)).thenReturn(entity);

        assertThat(controller.findByStateTrue().getBody().getData()).containsExactly(entity);
        assertThat(controller.show(2L).getBody().getData()).isSameAs(entity);
        assertThat(controller.save(entity).getBody().getData()).isSameAs(entity);

        controller.update(2L, entity);
        verify(service).update(2L, entity);

        EstadoDTO estadoDTO = new EstadoDTO();
        estadoDTO.setEstado(false);
        controller.cambiarEstado(2L, estadoDTO);
        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);
        verify(service).cambiarEstado(eq(2L), captor.capture());
        assertThat(captor.getValue()).isFalse();

        controller.delete(4L);
        verify(service).delete(4L);
    }
}
