package com.tektechno.payout.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utility class for handling Excel file operations.
 *
 * @author kousik manik
 */
public class ExcelHelper {

    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * Reads an Excel file and converts it to a list of maps where each map represents a row
     * with column headers as keys and cell values as values.
     *
     * @param file the MultipartFile containing the Excel data
     * @return List of Maps containing the Excel data
     * @throws IllegalArgumentException if the file is null or not an Excel file
     * @throws RuntimeException if there's an error reading the file
     */
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
            headerRow.forEach(cell -> headers.add(getCellValue(cell)));
        }
        return headers;
    }

    private static void processRows(Iterator<Row> rowIterator, List<String> headers, 
                                  List<Map<String, String>> dataList) {
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Map<String, String> rowData = new HashMap<>();

            for (int i = 0; i < headers.size(); i++) {
                Cell cell = row.getCell(i);
                String cellValue = getCellValue(cell);
                rowData.put(headers.get(i), cellValue);
            }

            if (!isRowEmpty(rowData)) {
                dataList.add(rowData);
            }
        }
    }

    private static boolean isRowEmpty(Map<String, String> rowData) {
        return rowData.values().stream().allMatch(String::isEmpty);
    }

    /**
     * Extracts the string value from a cell based on its type.
     *
     * @param cell the cell to extract value from
     * @return String representation of the cell value
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
    
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf(cell.getNumericCellValue());
                } catch (IllegalStateException e) {
                    yield cell.getStringCellValue();
                }
            }
          default -> "";
        };
    }
}