package com.corhuila.sgie.common.Reporting;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ReporteGenericoServiceTest {

    private ReporteGenericoService service;

    @BeforeEach
    void setup() {
        service = new ReporteGenericoService(new NoOpTransactionManager());
    }

    @Test
    void generarCreaReporteEnMemoria() {
        GeneradorReporteUtil.GeneratedReport report = service.generar(ReportFormat.CSV,
                List.of(new Row("Item", 1)),
                Row.class,
                "demo",
                "Titulo");

        assertThat(report.fileName()).isEqualTo("demo.csv");
    }

    @Test
    void generarStreamingEscribeSalida() throws Exception {
        ResponseEntity<StreamingResponseBody> response = service.generarStreaming(
                ReportFormat.CSV,
                () -> Stream.of(new Row("Item", 1)),
                Row.class,
                "reporte",
                "Titulo");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.getBody().writeTo(baos);
        assertThat(baos.toByteArray()).isNotEmpty();
    }

    @Test
    void escribirAlResponseConfiguraCabeceras() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        service.escribirAlResponse(response,
                ReportFormat.CSV,
                () -> Stream.of(new Row("Item", 1)),
                Row.class,
                "reporte",
                "Titulo");

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getContentAsByteArray()).isNotEmpty();
        assertThat(response.getHeader("Content-Disposition")).contains("reporte.csv");
    }

    private record Row(@ReportColumn(header = "Nombre", order = 0) String nombre,
                       @ReportColumn(header = "Cantidad", order = 1) int cantidad) {
    }

    private static class NoOpTransactionManager extends AbstractPlatformTransactionManager {
        @Override
        protected Object doGetTransaction() throws TransactionException {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {}

        @Override
        protected void doCommit(DefaultTransactionStatus status) {}

        @Override
        protected void doRollback(DefaultTransactionStatus status) {}
    }
}
