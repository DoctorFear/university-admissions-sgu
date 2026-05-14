package com.xettuyen2026.service;

import com.xettuyen2026.dao.NguyenVongDAO;
import com.xettuyen2026.dao.ThiSinhDAO;
import com.xettuyen2026.entity.NguyenVongXetTuyen;
import com.xettuyen2026.entity.ThiSinh;
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
    private ThiSinhDAO thiSinhDAO = new ThiSinhDAO();

    public static class ImportResult {
        public int successCount = 0;
        public int skipCount = 0;
        public int errorCount = 0;
        public List<String> errors = new ArrayList<>();
    }

    public ImportResult importFromExcel(File file) throws Exception {
        ImportResult result = new ImportResult();
        List<NguyenVongXetTuyen> toSave = new ArrayList<>();

        // FIX #5: Preload toàn bộ CCCD hợp lệ từ bảng thí sinh (1 query duy nhất, tránh N+1)
        Set<String> validCccdSet = new HashSet<>();
        try {
            for (ThiSinh ts : thiSinhDAO.findAll()) {
                if (ts.getCccd() != null) validCccdSet.add(ts.getCccd().trim());
            }
        } catch (Exception e) {
            // Nếu không load được danh sách thí sinh thì vẫn cho import (không chặn)
            System.err.println("⚠️ Không thể load danh sách thí sinh để validate: " + e.getMessage());
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Tự động tìm sheet và Header (Quét qua các sheet, tìm dòng chứa CCCD và Mã ngành)
            Sheet targetSheet = null;
            Row headerRow = null;
            int headerRowIndex = -1;
            int colCccd = -1, colMaNganh = -1, colNvTt = -1, colPt = -1, colThm = -1, colTuyenThang = -1;

            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                // Scan 20 dòng đầu
                for (int r = 0; r < Math.min(sheet.getLastRowNum() + 1, 20); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;

                    int tCccd = -1, tMaNganh = -1, tNvTt = -1, tPt = -1, tThm = -1, tTuyenThang = -1;

                    for (int c = 0; c < row.getLastCellNum(); c++) {
                        String header = getCellString(row, c);
                        if (header == null) continue;
                        String h = header.toLowerCase()
                                .replace("_", "").replace(" ", "").replace("(", "").replace(")", "").replace("*", "");

                        if (h.contains("cccd") || h.contains("nncccd")) {
                            tCccd = c;
                        } else if (h.contains("manganh") || h.contains("mãngành") || h.contains("maxettuyen") || h.contains("mãxéttuyển")) {
                            if (tMaNganh < 0) tMaNganh = c; // lấy cột match đầu tiên
                        } else if (h.contains("nvtt") || h.contains("nvthứ") || h.contains("thứtự") || h.contains("nvthu") || h.contains("thutunv")) {
                            tNvTt = c;
                        } else if (h.contains("phuongthuc") || h.contains("phươngthức") || h.equals("pt")) {
                            tPt = c;
                        } else if (h.contains("tohop") || h.contains("tổhợp") || h.contains("thm") || h.contains("ttthm")) {
                            tThm = c;
                        } else if (h.contains("tuyểnthẳng") || h.contains("tuyenthang") || h.contains("điều8") || h.contains("dieu8")) {
                            tTuyenThang = c;
                        }
                    }

                    // Nếu tìm thấy cả 2 cột nòng cốt
                    if (tCccd >= 0 && tMaNganh >= 0) {
                        targetSheet = sheet;
                        headerRow = row;
                        headerRowIndex = r;
                        colCccd = tCccd;
                        colMaNganh = tMaNganh;
                        colNvTt = tNvTt;
                        colPt = tPt;
                        colThm = tThm;
                        colTuyenThang = tTuyenThang;
                        break;
                    }
                }
                if (targetSheet != null) break;
            }

            if (targetSheet == null || headerRow == null) {
                throw new Exception("Không tìm thấy dữ liệu nguyện vọng hợp lệ!\n"
                        + "Chưa tìm ra dòng Header chứa 'CCCD' và 'Mã ngành' / 'Mã xét tuyển' ở bất kỳ sheet nào.");
            }

            int lastRow = targetSheet.getLastRowNum();

            for (int i = headerRowIndex + 1; i <= lastRow; i++) {
                Row row = targetSheet.getRow(i);
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

                    // FIX #5: Kiểm tra CCCD có tồn tại trong bảng thí sinh không
                    if (!validCccdSet.isEmpty() && !validCccdSet.contains(cccd)) {
                        result.skipCount++;
                        result.errors.add("Dòng " + (i + 1) + ": CCCD '" + cccd + "' không tồn tại trong hệ thống thí sinh — bỏ qua.");
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
                    String phuongthuc = "PT2 - THPT";
                    if (colPt >= 0) {
                        String pt = getCellString(row, colPt);
                        if (pt != null && !pt.isEmpty()) phuongthuc = pt;
                    }

                    // Ưu tiên kiểm tra cột tuyển thẳng
                    if (colTuyenThang >= 0) {
                        String tuyenThangVal = getCellString(row, colTuyenThang);
                        if (tuyenThangVal != null && (tuyenThangVal.equalsIgnoreCase("x") || tuyenThangVal.equalsIgnoreCase("có") || tuyenThangVal.equalsIgnoreCase("co") || tuyenThangVal.equalsIgnoreCase("true") || tuyenThangVal.equals("1"))) {
                            phuongthuc = "PT1 - Tuyển thẳng";
                        }
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
