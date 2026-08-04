package com.paramount.test.ff.uitests.helpers.ptspackaging;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Reads Fulfillment Console Excel export header row (BSD-29441). */
public final class FulfillmentExportExcelUtil {

    private FulfillmentExportExcelUtil() {
    }

    public static List<String> readHeaderRow(File excelFile) throws IOException {
        List<String> headers = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(excelFile);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return headers;
            }
            Row row = sheet.getRow(0);
            if (row == null) {
                return headers;
            }
            DataFormatter formatter = new DataFormatter();
            short lastCell = row.getLastCellNum();
            for (int i = 0; i < lastCell; i++) {
                Cell cell = row.getCell(i);
                headers.add(cell == null ? "" : formatter.formatCellValue(cell).trim());
            }
        }
        return headers;
    }

    public static boolean headerRowContains(List<String> headers, String columnLabel) {
        if (columnLabel == null || columnLabel.trim().isEmpty()) {
            return false;
        }
        String target = columnLabel.trim().toLowerCase();
        for (String header : headers) {
            if (header == null) {
                continue;
            }
            String normalized = header.trim().toLowerCase();
            if (normalized.equals(target) || normalized.contains(target)) {
                return true;
            }
        }
        return false;
    }

    public static int findColumnIndex(List<String> headers, String columnLabel) {
        if (headers == null || columnLabel == null) {
            return -1;
        }
        String target = columnLabel.trim().toLowerCase();
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            if (header == null) {
                continue;
            }
            String normalized = header.trim().toLowerCase();
            if (normalized.equals(target) || normalized.contains("pts") && normalized.contains("packag")) {
                return i;
            }
        }
        return -1;
    }

    /** First populated data row value for the given column (row 1 in sheet — row 0 is header). */
    public static String readFirstDataRowColumnValue(File excelFile, String columnLabel) throws IOException {
        List<String> headers = readHeaderRow(excelFile);
        int colIndex = findColumnIndex(headers, columnLabel);
        if (colIndex < 0) {
            return "";
        }
        try (FileInputStream fis = new FileInputStream(excelFile);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return "";
            }
            DataFormatter formatter = new DataFormatter();
            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) {
                    continue;
                }
                Cell cell = row.getCell(colIndex);
                String value = cell == null ? "" : formatter.formatCellValue(cell).trim();
                if (!value.isEmpty() && !"-".equals(value)) {
                    return value;
                }
            }
        }
        return "";
    }

    public static String normalizePtsId(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", "").replace('_', '-');
    }
}
