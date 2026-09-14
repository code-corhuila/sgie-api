package com.corhuila.sgie.common.Reporting;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class HelperUtilsTest {

    @Test
    void buildHeadersIncluyeNombreYTipo() throws Exception {
        GeneradorReporteUtil.GeneratedReport report =
                new GeneradorReporteUtil.GeneratedReport(
                        new ByteArrayResource("demo".getBytes()),
                        MediaType.APPLICATION_OCTET_STREAM,
                        "archivo.csv");

        HttpHeaders headers = HelperUtils.buildHeaders(report);

        assertThat(headers.getContentDisposition().getFilename()).isEqualTo("archivo.csv");
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Test
    void isStreamingReconoceValores() {
        assertThat(HelperUtils.isStreaming(null)).isTrue();
        assertThat(HelperUtils.isStreaming("stream")).isTrue();
        assertThat(HelperUtils.isStreaming(" STREAM ")).isTrue();
        assertThat(HelperUtils.isStreaming("memoria")).isFalse();
    }

    @Test
    void normalizeRecortaOCuandoVacio() {
        assertThat(HelperUtils.normalize(" texto ")).isEqualTo("texto");
        assertThat(HelperUtils.normalize("   ")).isNull();
    }

    @Test
    void sanitizeFormulaInjectionEscapaCaracteresPeligrosos() {
        assertThat(HelperUtils.sanitizeFormulaInjection("=cmd|'/c calc'!A1")).startsWith("'=");
        assertThat(HelperUtils.sanitizeFormulaInjection("+1+1")).startsWith("'+");
        assertThat(HelperUtils.sanitizeFormulaInjection("-1+1")).startsWith("'-");
        assertThat(HelperUtils.sanitizeFormulaInjection("@SUM(1,1)")).startsWith("'@");
    }

    @Test
    void sanitizeFormulaInjectionNoAlteraValoresNormales() {
        assertThat(HelperUtils.sanitizeFormulaInjection("Laboratorio 101")).isEqualTo("Laboratorio 101");
        assertThat(HelperUtils.sanitizeFormulaInjection("")).isEqualTo("");
        assertThat(HelperUtils.sanitizeFormulaInjection(null)).isNull();
    }
}
