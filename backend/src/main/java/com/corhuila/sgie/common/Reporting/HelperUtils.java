package com.corhuila.sgie.common.Reporting;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;

import java.util.Locale;

public class HelperUtils {

    private HelperUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static HttpHeaders buildHeaders(GeneradorReporteUtil.GeneratedReport reporte) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(reporte.mediaType());
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(reporte.fileName())
                .build());
        try {
            long size = reporte.resource().contentLength();
            if (size >= 0) {
                headers.setContentLength(size);
            }
        } catch (Exception ignored) {
            // Se ignora la excepción porque contentLength() puede fallar en algunos recursos
            // y no queremos que esto afecte la generación del reporte
        }
        return headers;
    }

    public static boolean isStreaming(String value) {
        if (value == null) {
            return true;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() || normalized.equals("stream");
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public static String sanitizeFormulaInjection(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        char first = value.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@') {
            return "'" + value;
        }
        return value;
    }
}
