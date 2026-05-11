package com.xettuyen2026.util;

import com.xettuyen2026.entity.NguyenVongXetTuyen;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExportUtil {

    public static void exportNguyenVongToExcel(List<NguyenVongXetTuyen> list, Map<String, String> nganhNameMap,
            File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách Nguyện vọng");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = { "STT", "CCCD", "Mã ngành", "Tên ngành", "NV Thứ", "Phương thức",
                    "Điểm THXT", "Điểm Cộng", "Điểm ƯT", "Điểm XT", "Kết quả" };

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data
            int rowNum = 1;
            for (NguyenVongXetTuyen nv : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(nv.getNnCccd() != null ? nv.getNnCccd() : "");
                row.createCell(2).setCellValue(nv.getNvManganh() != null ? nv.getNvManganh() : "");
                row.createCell(3)
                        .setCellValue(nv.getNvManganh() != null
                                ? nganhNameMap.getOrDefault(nv.getNvManganh(), nv.getNvManganh())
                                : "");
                if (nv.getNvTt() != null)
                    row.createCell(4).setCellValue(nv.getNvTt());
                row.createCell(5).setCellValue(nv.getTtPhuongthuc() != null ? nv.getTtPhuongthuc() : "");
                if (nv.getDiemThxt() != null)
                    row.createCell(6).setCellValue(nv.getDiemThxt().doubleValue());
                if (nv.getDiemCong() != null)
                    row.createCell(7).setCellValue(nv.getDiemCong().doubleValue());
                if (nv.getDiemUtqd() != null)
                    row.createCell(8).setCellValue(nv.getDiemUtqd().doubleValue());
                if (nv.getDiemXettuyen() != null)
                    row.createCell(9).setCellValue(nv.getDiemXettuyen().doubleValue());
                row.createCell(10).setCellValue(nv.getNvKetqua() != null ? nv.getNvKetqua() : "");
            }

            // Autosize columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
        }
    }
}
