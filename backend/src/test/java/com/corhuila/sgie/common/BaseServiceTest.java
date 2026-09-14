package com.corhuila.sgie.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseServiceTest {

    @Mock
    private IBaseRepository<DummyEntity, Long> repository;

    @InjectMocks
    private DummyService service;

    private DummyEntity entity;

    @BeforeEach
    void setUp() {
        entity = new DummyEntity();
        entity.setId(1L);
        entity.setNombre("Demo");
        entity.setState(true);
        entity.setCreatedAt(LocalDateTime.now().minusDays(1));
        entity.setUpdatedAt(LocalDateTime.now().minusHours(2));
    }

    @Test
    void allDelegatesToRepository() {
        when(repository.findAll()).thenReturn(List.of(entity));
        assertThat(service.all()).containsExactly(entity);
    }

    @Test
    void findByStateTrueFiltraInactivos() {
        DummyEntity inactive = new DummyEntity();
        inactive.setState(false);
        when(repository.findAll()).thenReturn(List.of(entity, inactive));

        assertThat(service.findByStateTrue()).containsExactly(entity);
    }

    @Test
    void findByIdLanzaExceptionCuandoNoExiste() {
        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Registro no encontrado");
    }

    @Test
    void updateLanzaResourceNotFoundCuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(99L, new DummyEntity()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateLanzaIllegalStateCuandoRegistroInhabilitado() {
        entity.setDeletedAt(LocalDateTime.now());
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.update(1L, new DummyEntity()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("inhabilitado");
    }

    @Test
    void deleteLanzaResourceNotFoundCuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cambiarEstadoLanzaResourceNotFoundCuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.cambiarEstado(99L, true))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCopiaCamposYActualizaFecha() throws Exception {
        DummyEntity cambios = new DummyEntity();
        cambios.setNombre("Nuevo");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.update(1L, cambios);

        verify(repository).save(any(DummyEntity.class));
        assertThat(entity.getNombre()).isEqualTo("Nuevo");
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void deleteMarcaEntidadComoInactiva() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        service.delete(1L);

        assertThat(entity.getState()).isFalse();
        assertThat(entity.getDeletedAt()).isNotNull();
        verify(repository).save(entity);
    }

    @Test
    void cambiarEstadoActualizaBandera() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        service.cambiarEstado(1L, false);

        assertThat(entity.getState()).isFalse();
        verify(repository).save(entity);
    }

    @Test
    void saveEjecutaHooks() throws Exception {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DummyEntity nuevo = new DummyEntity();
        service.save(nuevo);

        assertThat(service.beforeCalled).isTrue();
        assertThat(service.afterCalled).isTrue();
    }

    private static class DummyService extends BaseService<DummyEntity> {
        private boolean beforeCalled;
        private boolean afterCalled;
        private final IBaseRepository<DummyEntity, Long> repository;

        private DummyService(IBaseRepository<DummyEntity, Long> repository) {
            this.repository = repository;
        }

        @Override
        protected IBaseRepository<DummyEntity, Long> getRepository() {
            return repository;
        }

        @Override
        protected void beforeSave(DummyEntity entity) {
            beforeCalled = true;
        }

        @Override
        protected void afterSave(DummyEntity entity) {
            afterCalled = true;
        }
    }

    private static class DummyEntity extends Auditoria {
        private Long id;
        private String nombre;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }
    }
}
