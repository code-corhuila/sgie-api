package com.corhuila.sgie.common.Reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

public class CsvReportWriter implements ReportWriter {

    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private static void writeLine(OutputStream out, List<String> columns) throws IOException {
        String line = String.join(",", columns) + "\n";
        out.write(line.getBytes(StandardCharsets.UTF_8));
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    @Override
    public String contentType() {
        return "text/csv; charset=UTF-8";
    }

    @Override
    public String extension() {
        return ".csv";
    }

    @Override
    public void write(OutputStream out, Class<?> dtoType, Stream<?> rows, String title) {
        try {
            out.write(UTF8_BOM);

            if (title != null && !title.isBlank()) {
                writeLine(out, List.of(title));
            }

            List<String> headers = BeanRowExtractor.headers(dtoType);
            writeLine(out, headers);

            rows.forEach(bean -> {
                try {
                    List<Object> values = BeanRowExtractor.values(bean);
                    List<String> serialized = values.stream()
                            .map(value -> value == null ? "" : String.valueOf(value))
                            .map(HelperUtils::sanitizeFormulaInjection)
                            .map(CsvReportWriter::escape)
                            .toList();
                    writeLine(out, serialized);
                } catch (Exception ex) {
                    throw new IllegalStateException("Error generando el CSV", ex);
                }
            });
        } catch (IOException ex) {
            throw new IllegalStateException("Error escribiendo reporte CSV", ex);
        }
    }

}
