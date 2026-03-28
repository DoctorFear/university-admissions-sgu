package com.xettuyen2026.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


// Tiện ích đọc file Excel dùng chung cho toàn bộ dự án.
public class ImportUtil {

	private ImportUtil() {}
	
	// DOC EXCEL tung row dua tren Entity cua Class 
	/**
     * Đọc file Excel, bỏ qua dòng tiêu đề (row 0),
     * dùng RowMapper để chuyển mỗi row → entity T.
     *
     * @param file      File Excel (.xlsx)
     * @param mapper    Lambda map Row → T (do từng Service tự định nghĩa)
     * @param <T>       Kiểu entity
     * @return          Danh sách entity đã parse
     */
	public static <T> List<T> readExcel(File file, RowMapper<T> mapper) throws Exception {
        List<T> result = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Bỏ qua dòng trống (cột đầu tiên rỗng)
                if (isRowEmpty(row)) continue;

                T entity = mapper.map(row);
                if (entity != null) result.add(entity);
            }
        }

        return result;
    }
	
	
	public static <T> List<T> readExcel(File file, String sheetName, RowMapper<T> mapper) throws Exception {
	    List<T> result = new ArrayList<>();

	    try (FileInputStream fis = new FileInputStream(file);
	         Workbook workbook = new XSSFWorkbook(fis)) {

	        // Lấy sheet theo tên
	        Sheet sheet = workbook.getSheet(sheetName);
	        if (sheet == null) {
	            throw new RuntimeException("Không tìm thấy sheet: " + sheetName);
	        }

	        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
	            Row row = sheet.getRow(i);
	            if (row == null) continue;

	            // Bỏ qua dòng trống
	            if (isRowEmpty(row)) continue;

	            T entity = mapper.map(row);
	            if (entity != null) result.add(entity);
	        }
	    }

	    return result;
	}

	
	// CELL HELPERS - Dung trong lambda cua Service
	public static String getString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }

    public static Integer getInt(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING  -> {
                try { yield Integer.parseInt(cell.getStringCellValue().trim()); }
                catch (NumberFormatException e) { yield null; }
            }
            default -> null;
        };
    }

    public static BigDecimal getDecimal(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING  -> {
                try { yield new BigDecimal(cell.getStringCellValue().trim()); }
                catch (NumberFormatException e) { yield null; }
            }
            default -> null;
        };
    }

    public static Double getDouble(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING  -> {
                try { yield Double.parseDouble(cell.getStringCellValue().trim()); }
                catch (NumberFormatException e) { yield null; }
            }
            default -> null;
        };
    }
    
    // INTERNAL 
    private static boolean isRowEmpty(Row row) {
        Cell first = row.getCell(0);
        if (first == null) return true;
        if (first.getCellType() == CellType.BLANK) return true;
        if (first.getCellType() == CellType.STRING
                && first.getStringCellValue().trim().isEmpty()) return true;
        return false;
    }
    
    // FUNCTIONAL INTERFACE
    @FunctionalInterface
    public interface RowMapper<T> {
        T map(Row row) throws Exception;
    }
}
