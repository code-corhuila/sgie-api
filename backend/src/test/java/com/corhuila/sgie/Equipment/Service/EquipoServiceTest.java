package com.corhuila.sgie.Equipment.Service;

import com.corhuila.sgie.Equipment.DTO.EquipoReporteDTO;
import com.corhuila.sgie.Equipment.DTO.IEquipoInstalacionDTO;
import com.corhuila.sgie.Equipment.IRepository.IEquipoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipoServiceTest {

    @Mock
    private IEquipoRepository repository;

    @InjectMocks
    private EquipoService service;

    @Test
    void findEquiposInstalacionesUsaRepositorio() {
        IEquipoInstalacionDTO dto = org.mockito.Mockito.mock(IEquipoInstalacionDTO.class);
        when(repository.findEquiposInstalaciones(anyString(), anyString())).thenReturn(List.of(dto));

        assertThat(service.findEquiposInstalaciones("EQ-1", "AUDITORIO"))
                .containsExactly(dto);
    }

    @Test
    void proveedorStreamRecuperaDatos() {
        EquipoReporteDTO dto = new EquipoReporteDTO(1L, "EQ-1", "Proyector",
                true, "Sala 101", "Multimedia", "Campus Norte");
        when(repository.generarReporteEquipos()).thenReturn(Stream.of(dto));

        try (Stream<EquipoReporteDTO> stream = service.proveedorStream().get()) {
            assertThat(stream).containsExactly(dto);
        }
    }

    @Test
    void obtenerDatosEnMemoriaConsumeStream() {
        EquipoReporteDTO dto = new EquipoReporteDTO(1L, "EQ-1", "Proyector",
                true, "Sala 101", "Multimedia", "Campus Norte");
        when(repository.generarReporteEquipos()).thenReturn(Stream.of(dto));

        List<EquipoReporteDTO> data = service.obtenerDatosEnMemoria();
        assertThat(data).containsExactly(dto);
    }
}
