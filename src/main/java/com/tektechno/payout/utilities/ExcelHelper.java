package com.tektechno.payout.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utility class for handling Excel file operations.
 * @author kousik
 */
public class ExcelHelper {

    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public static List<Map<String, String>> readExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is null or empty");
        }

        if (!EXCEL_CONTENT_TYPE.equals(file.getContentType())) {
            throw new IllegalArgumentException("Please upload an Excel file (xlsx)");
        }

        List<Map<String, String>> dataList = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("Excel file contains no sheets");
            }

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            List<String> headers = extractHeaders(rowIterator);
            if (headers.isEmpty()) {
                throw new IllegalArgumentException("Excel file contains no headers");
            }

            processRows(rowIterator, headers, dataList);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + e.getMessage(), e);
        }

        return dataList;
    }

    private static List<String> extractHeaders(Iterator<Row> rowIterator) {
        List<String> headers = new ArrayList<>();
        if (rowIterator.hasNext()) {
            Row headerRow = rowIterator.next();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String header = getCellValue(cell).trim();
                headers.add(header.isEmpty() ? "Column" + i : header); // fallback name
            }

            // Debug print
            System.out.println("Extracted Headers:");
            for (int i = 0; i < headers.size(); i++) {
                System.out.println("[" + i + "] " + headers.get(i));
            }
        }
        return headers;
    }

    private static void processRows(Iterator<Row> rowIterator, List<String> headers,
                                    List<Map<String, String>> dataList) {
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Map<String, String> rowData = new HashMap<>();

            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                rowData.put(header, getCellValue(cell));
            }

            if (!isRowEmpty(rowData)) {
                dataList.add(rowData);
            }
        }
    }

    private static boolean isRowEmpty(Map<String, String> rowData) {
        return rowData.values().stream().allMatch(String::isEmpty);
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    return new SimpleDateFormat("yyyy-MM-dd").format(date);
                } else {
                    return new BigDecimal(cell.getNumericCellValue()).toPlainString(); // prevent E notation
                }
            case FORMULA:
                try {
                    return new BigDecimal(cell.getNumericCellValue()).toPlainString();
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}