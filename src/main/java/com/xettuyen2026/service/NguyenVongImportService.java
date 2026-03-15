package com.xettuyen2026.service;

import com.xettuyen2026.dao.NguyenVongDAO;
import com.xettuyen2026.entity.NguyenVongXetTuyen;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * Service import nguyện vọng từ file Excel (.xlsx).
 *
 * Tự động nhận diện cột dựa trên header row (dòng 1):
 *   - Tìm cột chứa "cccd" → nn_cccd
 *   - Tìm cột chứa "manganh" hoặc "mã ngành" hoặc "ma nganh" → nv_manganh
 *   - Tìm cột chứa "nv_tt" hoặc "nv thứ" hoặc "thứ tự" → nv_tt
 *   - Tìm cột chứa "phuongthuc" hoặc "phương thức" hoặc "pt" → tt_phuongthuc
 *   - Tìm cột chứa "tohop" hoặc "tổ hợp" hoặc "thm" → tt_thm
 */
public class NguyenVongImportService {

    private NguyenVongDAO dao = new NguyenVongDAO();

    public static class ImportResult {
        public int successCount = 0;
        public int skipCount = 0;
        public int errorCount = 0;
        public List<String> errors = new ArrayList<>();
    }

    public ImportResult importFromExcel(File file) throws Exception {
        ImportResult result = new ImportResult();
        List<NguyenVongXetTuyen> toSave = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new Exception("File Excel không có header (dòng 1 trống).");

            // Auto-detect column indices from header
            int colCccd = -1, colMaNganh = -1, colNvTt = -1, colPt = -1, colThm = -1;

            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                String header = getCellString(headerRow, c);
                if (header == null) continue;
                String h = header.toLowerCase()
                        .replace("_", "").replace(" ", "").replace("(", "").replace(")", "").replace("*", "");

                if (h.contains("cccd") || h.contains("nncccd")) {
                    colCccd = c;
                } else if (h.contains("manganh") || h.contains("mãngành") || h.contains("manganh")) {
                    if (colMaNganh < 0) colMaNganh = c; // lấy cột đầu tiên match
                } else if (h.contains("nvtt") || h.contains("nvthứ") || h.contains("thứtự") || h.contains("nvthu")) {
                    colNvTt = c;
                } else if (h.contains("phuongthuc") || h.contains("phươngthức") || h.equals("pt")) {
                    colPt = c;
                } else if (h.contains("tohop") || h.contains("tổhợp") || h.contains("thm") || h.contains("ttthm")) {
                    colThm = c;
                }
            }

            if (colCccd < 0 || colMaNganh < 0) {
                throw new Exception("Không tìm thấy cột CCCD hoặc Mã ngành trong header.\n"
                        + "Header cần chứa: 'cccd' hoặc 'nn_cccd', 'mã ngành' hoặc 'nv_manganh'.");
            }

            int lastRow = sheet.getLastRowNum();

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String cccd = getCellString(row, colCccd);
                    String maNganh = getCellString(row, colMaNganh);

                    if (cccd == null || cccd.isEmpty() || maNganh == null || maNganh.isEmpty()) {
                        result.skipCount++;
                        continue;
                    }
                    
                    if (cccd.contains("(*)") || cccd.contains("nn_cccd")) {
                        result.skipCount++;
                        continue;
                    }

                    // NV thứ tự
                    int nvTt = 1;
                    if (colNvTt >= 0) {
                        Cell cellTt = row.getCell(colNvTt);
                        if (cellTt != null) {
                            if (cellTt.getCellType() == CellType.NUMERIC) {
                                nvTt = (int) cellTt.getNumericCellValue();
                            } else {
                                try { nvTt = Integer.parseInt(getCellString(row, colNvTt)); }
                                catch (NumberFormatException e) { /* default 1 */ }
                            }
                        }
                    }

                    // Phương thức
                    String phuongthuc = "PT2";
                    if (colPt >= 0) {
                        String pt = getCellString(row, colPt);
                        if (pt != null && !pt.isEmpty()) phuongthuc = pt;
                    }

                    // Tổ hợp
                    String tohop = null;
                    if (colThm >= 0) {
                        tohop = getCellString(row, colThm);
                    }

                    NguyenVongXetTuyen nv = new NguyenVongXetTuyen();
                    nv.setNnCccd(cccd);
                    nv.setNvManganh(maNganh);
                    nv.setNvTt(nvTt);
                    nv.setTtPhuongthuc(phuongthuc);
                    nv.setTtThm(tohop);
                    nv.setNvKeys(cccd + "_" + maNganh + "_" + phuongthuc);

                    toSave.add(nv);
                    result.successCount++;

                } catch (Exception e) {
                    result.errorCount++;
                    result.errors.add("Dòng " + (i + 1) + ": " + e.getMessage());
                }
            }
        }

        // Save
        if (!toSave.isEmpty()) {
            for (NguyenVongXetTuyen nv : toSave) {
                try {
                    NguyenVongXetTuyen existing = dao.findByNvKeys(nv.getNvKeys());
                    if (existing != null) {
                        // Cập nhật thông tin nếu đã tồn tại
                        existing.setNvTt(nv.getNvTt());
                        existing.setTtPhuongthuc(nv.getTtPhuongthuc());
                        existing.setTtThm(nv.getTtThm());
                        dao.update(existing);
                    } else {
                        dao.save(nv);
                    }
                } catch (Exception e) {
                    result.successCount--;
                    result.errorCount++;
                    result.errors.add("Lưu CCCD " + nv.getNnCccd() + " ngành " + nv.getNvManganh()
                            + ": " + e.getMessage());
                }
            }
        }

        return result;
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d)) return String.valueOf((long) d);
                return String.valueOf(d);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return null;
        }
    }
}
