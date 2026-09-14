package com.corhuila.sgie.common.Reporting;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GeneradorReporteUtilTest {

    @Test
    void generarEnMemoriaCsvSanitizaNombre() throws Exception {
        GeneradorReporteUtil.GeneratedReport report = GeneradorReporteUtil.generarEnMemoria(
                ReportFormat.CSV,
                List.of(new SampleRow("Item", 5)),
                SampleRow.class,
                "reporte demo",
                "Titulo");

        assertThat(report.fileName()).isEqualTo("reporte_demo.csv");
        assertThat(report.mediaType().toString()).contains("csv");
        assertThat(report.resource().contentLength()).isGreaterThan(0);
    }

    @Test
    void generarEnMemoriaPdfProduceContenido() throws Exception {
        GeneradorReporteUtil.GeneratedReport report = GeneradorReporteUtil.generarEnMemoria(
                ReportFormat.PDF,
                Stream.of(new SampleRow("Item", 3)),
                SampleRow.class,
                "reporte",
                "Titulo");

        Resource resource = report.resource();
        assertThat(resource.contentLength()).isGreaterThan(0);
    }

    @Test
    void resolveWriterPermiteEscribirXlsx() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ReportWriter writer = GeneradorReporteUtil.resolveWriter(ReportFormat.XLSX);
        writer.write(baos, SampleRow.class, Stream.of(new SampleRow("Item", 1)), "Titulo");

        assertThat(baos.toByteArray()).isNotEmpty();
    }

    @Test
    void resolveWriterXlsxSanitizaInyeccionDeFormulas() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ReportWriter writer = GeneradorReporteUtil.resolveWriter(ReportFormat.XLSX);
        writer.write(baos, SampleRow.class, Stream.of(new SampleRow("=cmd|'/c calc'!A1", 1)), "Titulo");

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(baos.toByteArray()))) {
            Sheet sheet = workbook.getSheetAt(0);
            Row dataRow = sheet.getRow(2);
            assertThat(dataRow.getCell(0).getStringCellValue()).startsWith("'=");
        }
    }

    private record SampleRow(
            @ReportColumn(header = "Nombre", order = 0) String nombre,
            @ReportColumn(header = "Cantidad", order = 1) int cantidad) {
    }
}
