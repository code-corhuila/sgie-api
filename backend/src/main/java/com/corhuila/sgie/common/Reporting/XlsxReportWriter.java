package com.corhuila.sgie.common.Reporting;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class XlsxReportWriter implements ReportWriter {

    private static final int EXCEL_MAX_ROWS = 1_048_576;

    @Override
    public String contentType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    @Override
    public String extension() {
        return ".xlsx";
    }

    @Override
    public void write(OutputStream out, Class<?> dtoType, Stream<?> rows, String title) {
        List<BeanRowExtractor.ColumnMeta> metas = BeanRowExtractor.metas(dtoType);
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(200)) {
            workbook.setCompressTempFiles(true);

            Map<String, CellStyle> styleCache = new HashMap<>();
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);

            List<SheetContext> contexts = new java.util.ArrayList<>();
            SheetContext initialContext = createSheet(workbook, metas, title, 1, headerStyle, titleStyle);
            contexts.add(initialContext);
            SheetContext[] holder = new SheetContext[]{initialContext};
            AtomicInteger sheetIndex = new AtomicInteger(1);

            rows.forEach(bean -> {
                try {
                    SheetContext current = holder[0];
                    if (current.nextRowIndex >= EXCEL_MAX_ROWS) {
                        SheetContext newContext = createSheet(workbook, metas, title, sheetIndex.incrementAndGet(), headerStyle, titleStyle);
                        contexts.add(newContext);
                        holder[0] = newContext;
                        current = newContext;
                    }
                    Row row = current.sheet.createRow(current.nextRowIndex++);
                    for (int columnIndex = 0; columnIndex < metas.size(); columnIndex++) {
                        BeanRowExtractor.ColumnMeta meta = metas.get(columnIndex);
                        Object value = meta.get(bean);
                        writeCell(workbook, row, columnIndex, value, meta, styleCache);
                    }
                } catch (Exception ex) {
                    throw new IllegalStateException("Error generando XLSX", ex);
                }
            });

            for (SheetContext ctx : contexts) {
                for (int columnIndex = 0; columnIndex < metas.size(); columnIndex++) {
                    if (metas.get(columnIndex).autosize()) {
                        ctx.sheet.autoSizeColumn(columnIndex);
                    }
                }
            }
            workbook.write(out);
        } catch (Exception ex) {
            throw new IllegalStateException("Error escribiendo reporte XLSX", ex);
        }
    }


    private SheetContext createSheet(SXSSFWorkbook workbook,
                                     List<BeanRowExtractor.ColumnMeta> metas,
                                     String title,
                                     int sheetNumber,
                                     CellStyle headerStyle,
                                     CellStyle titleStyle) {
        String name = sheetNumber == 1 ? "Reporte" : "Reporte (" + sheetNumber + ")";
        Sheet sheet = workbook.createSheet(name);
        if (sheet instanceof SXSSFSheet streamingSheet) {
            streamingSheet.setRandomAccessWindowSize(200);
        }

        for (int columnIndex = 0; columnIndex < metas.size(); columnIndex++) {
            int width = metas.get(columnIndex).width();
            if (width > -1) {
                sheet.setColumnWidth(columnIndex, width);
            }
        }

        int rowIndex = 0;
        boolean hasTitle = title != null && !title.isBlank();
        if (hasTitle) {
            Row titleRow = sheet.createRow(rowIndex++);
            Cell cell = titleRow.createCell(0, CellType.STRING);
            cell.setCellValue(title);
            cell.setCellStyle(titleStyle);
            if (metas.size() > 1) {
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, metas.size() - 1));
            }
        }

        Row headerRow = sheet.createRow(rowIndex++);
        for (int columnIndex = 0; columnIndex < metas.size(); columnIndex++) {
            Cell cell = headerRow.createCell(columnIndex, CellType.STRING);
            cell.setCellValue(metas.get(columnIndex).header());
            cell.setCellStyle(headerStyle);
        }

        applySheetDecorations(sheet, metas.size(), headerRow.getRowNum());

        return new SheetContext(sheet, rowIndex);
    }

    private void applySheetDecorations(Sheet sheet, int columnCount, int headerRowIndex) {
        sheet.createFreezePane(0, headerRowIndex + 1);
        sheet.setAutoFilter(new CellRangeAddress(headerRowIndex, headerRowIndex, 0, Math.max(columnCount - 1, 0)));
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setWrapText(true);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFont(font);
        return style;
    }

    private void writeCell(Workbook workbook,
                           Row row,
                           int columnIndex,
                           Object value,
                           BeanRowExtractor.ColumnMeta meta,
                           Map<String, CellStyle> styleCache) {
        Cell cell = row.createCell(columnIndex);

        if (value == null) {
            cell.setBlank();
            return;
        }

        if (meta.text()) {
            setCellAsText(workbook, cell, value, meta.wrap(), styleCache);
            return;
        }

        if (value instanceof Number number) {
            setCellAsNumber(workbook, cell, number, meta.format(), meta.wrap(), styleCache);
            return;
        }

        if (value instanceof Boolean bool) {
            setCellAsBoolean(workbook, cell, bool, meta.wrap(), styleCache);
            return;
        }

        if (handleDateTypes(workbook, cell, value, meta.format(), meta.wrap(), styleCache)) {
            return;
        }

        setCellAsText(workbook, cell, value, meta.wrap(), styleCache);
    }

    private void setCellAsText(Workbook workbook, Cell cell, Object value, boolean wrap, Map<String, CellStyle> styleCache) {
        cell.setCellValue(HelperUtils.sanitizeFormulaInjection(String.valueOf(value)));
        if (wrap) {
            cell.setCellStyle(textStyle(workbook, styleCache, true));
        }
    }

    private void setCellAsNumber(Workbook workbook, Cell cell, Number number, String format, boolean wrap, Map<String, CellStyle> styleCache) {
        cell.setCellValue(number.doubleValue());
        if (!format.isEmpty() || wrap) {
            cell.setCellStyle(numberStyle(workbook, styleCache, format, wrap));
        }
    }

    private void setCellAsBoolean(Workbook workbook, Cell cell, Boolean bool, boolean wrap, Map<String, CellStyle> styleCache) {
        cell.setCellValue(bool);
        if (wrap) {
            cell.setCellStyle(textStyle(workbook, styleCache, true));
        }
    }

    private boolean handleDateTypes(Workbook workbook, Cell cell, Object value, String format, boolean wrap, Map<String, CellStyle> styleCache) {
        if (value instanceof Date date) {
            cell.setCellValue(date);
            cell.setCellStyle(dateStyle(workbook, styleCache, format, wrap));
            return true;
        }

        if (value instanceof LocalDate localDate) {
            cell.setCellValue(java.sql.Date.valueOf(localDate));
            cell.setCellStyle(dateStyle(workbook, styleCache, format, wrap));
            return true;
        }

        if (value instanceof LocalDateTime localDateTime) {
            cell.setCellValue(java.sql.Timestamp.valueOf(localDateTime));
            cell.setCellStyle(dateStyle(workbook, styleCache, format, wrap));
            return true;
        }

        if (value instanceof OffsetDateTime offsetDateTime) {
            cell.setCellValue(java.sql.Timestamp.valueOf(offsetDateTime.toLocalDateTime()));
            cell.setCellStyle(dateStyle(workbook, styleCache, format, wrap));
            return true;
        }

        if (value instanceof ZonedDateTime zonedDateTime) {
            cell.setCellValue(java.sql.Timestamp.valueOf(zonedDateTime.toLocalDateTime()));
            cell.setCellStyle(dateStyle(workbook, styleCache, format, wrap));
            return true;
        }

        return false;
    }


    private CellStyle baseStyle(Workbook workbook, boolean wrap) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(wrap);
        return style;
    }


    private CellStyle textStyle(Workbook workbook, Map<String, CellStyle> cache, boolean wrap) {
        String key = "text_" + wrap;
        return cache.computeIfAbsent(key, k -> {
            CellStyle style = baseStyle(workbook, wrap);
            DataFormat dataFormat = workbook.createDataFormat();
            style.setDataFormat(dataFormat.getFormat("@"));
            return style;
        });
    }


    private CellStyle numberStyle(Workbook workbook, Map<String, CellStyle> cache, String format, boolean wrap) {
        String key = "number_" + format + "_" + wrap;
        return cache.computeIfAbsent(key, k -> {
            CellStyle style = baseStyle(workbook, wrap);
            if (!format.isEmpty()) {
                DataFormat dataFormat = workbook.createDataFormat();
                style.setDataFormat(dataFormat.getFormat(format));
            }
            return style;
        });
    }

    private CellStyle dateStyle(Workbook workbook, Map<String, CellStyle> cache, String format, boolean wrap) {
        String key = "date_" + format + "_" + wrap;
        return cache.computeIfAbsent(key, k -> {
            CellStyle style = baseStyle(workbook, wrap);
            DataFormat dataFormat = workbook.createDataFormat();
            style.setDataFormat(dataFormat.getFormat(format.isEmpty() ? "yyyy-mm-dd" : format));
            return style;
        });
    }

    private static final class SheetContext {
        private final Sheet sheet;
        private int nextRowIndex;

        private SheetContext(Sheet sheet, int nextRowIndex) {
            this.sheet = sheet;
            this.nextRowIndex = nextRowIndex;
        }
    }
}
