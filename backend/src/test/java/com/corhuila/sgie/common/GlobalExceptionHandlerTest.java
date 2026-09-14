package com.corhuila.sgie.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationExceptionsMapeaErrores() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "demo");
        bindingResult.addError(new FieldError("demo", "email", "Formato inválido"));
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiResponseDto<Map<String, String>>> response = handler.handleValidationExceptions(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getData()).containsEntry("email", "Formato inválido");
    }

    @Test
    void handleConstraintViolationsExtraeMensajes() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        jakarta.validation.Path path = mock(jakarta.validation.Path.class);
        when(path.toString()).thenReturn("campo");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("Requerido");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ApiResponseDto<Map<String, String>>> response = handler.handleConstraintViolations(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getData()).containsEntry("campo", "Requerido");
    }

    @Test
    void handleAuthenticationExceptionsDevuelve401() {
        ResponseEntity<ApiResponseDto<String>> response =
                handler.handleAuthenticationExceptions(new BadCredentialsException("Credenciales inválidas"),
                        new MockHttpServletRequest("POST", "/v1/api/usuario/login"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).contains("Credenciales");
    }

    @Test
    void handleAccessDeniedDevuelve403() {
        ResponseEntity<ApiResponseDto<String>> response =
                handler.handleAccessDenied(new AccessDeniedException("Sin permisos"),
                        new MockHttpServletRequest("GET", "/v1/api/reserva"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void handleResourceNotFoundDevuelve404() {
        ResponseEntity<ApiResponseDto<String>> response =
                handler.handleResourceNotFound(new ResourceNotFoundException("Registro no encontrado"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("Registro no encontrado");
    }

    @Test
    void handleIllegalArgumentDevuelve400() {
        ResponseEntity<ApiResponseDto<String>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("Fecha y horas son obligatorias."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleIllegalStateDevuelve409() {
        ResponseEntity<ApiResponseDto<String>> response =
                handler.handleIllegalState(new IllegalStateException("La reserva se solapa con otra existente"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handleGenericExceptionDevuelve500() {
        ResponseEntity<ApiResponseDto<String>> response =
                handler.handleGenericException(new RuntimeException("Error interno"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo("Error interno");
    }
}
