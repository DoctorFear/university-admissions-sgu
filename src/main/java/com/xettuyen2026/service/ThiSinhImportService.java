package com.xettuyen2026.service;

import com.xettuyen2026.dao.ThiSinhDAO;
import com.xettuyen2026.entity.ThiSinh;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.*;

/**
 * Service import danh sách thí sinh từ file Excel (.xlsx).
 *
 * Cấu trúc file mẫu "Ds thi sinh.xlsx":
 *   STT | CCCD | Họ Tên | Ngày sinh | Giới tính | ĐTƯT | KVƯT | TO | VA | ...
 *
 * Nhận diện cột tự động từ header dòng 1. Hỗ trợ cả Họ+Tên gộp lẫn tách riêng.
 */
public class ThiSinhImportService {

    private ThiSinhDAO dao = new ThiSinhDAO();

    public static class ImportResult {
        public int insertCount = 0;
        public int updateCount = 0;
        public int skipCount  = 0;
        public int errorCount = 0;
        public List<String> errors = new ArrayList<>();
    }

    public ImportResult importFromExcel(File file) throws Exception {
        ImportResult result = new ImportResult();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new Exception("File Excel không có header (dòng 1 trống).");

            // ── Nhận diện chỉ số cột ──
            int colCccd = -1, colHoTen = -1, colHo = -1, colTen = -1;
            int colNgaySinh = -1, colGioiTinh = -1, colDoiTuong = -1, colKhuVuc = -1;
            int colSoBaoDanh = -1, colDienThoai = -1, colEmail = -1, colNoiSinh = -1;

            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                String raw = getCellString(headerRow, c);
                if (raw == null) continue;
                String h = normalize(raw);

                if (h.contains("cccd") || h.contains("cmnd")) {
                    colCccd = c;
                } else if (h.contains("hoten") && colHoTen < 0) {
                    colHoTen = c;
                } else if (h.equals("ho") && colHoTen < 0 && colHo < 0) {
                    colHo = c;
                } else if (h.equals("ten") && colHoTen < 0 && colTen < 0) {
                    colTen = c;
                } else if (h.contains("ngaysinh")) {
                    colNgaySinh = c;
                } else if (h.contains("gioitinh")) {
                    colGioiTinh = c;
                } else if ((h.contains("dtut") || h.equals("dt") || h.contains("doituong")) && colDoiTuong < 0) {
                    colDoiTuong = c;
                } else if ((h.contains("kvut") || h.contains("khuvuc")) && colKhuVuc < 0) {
                    colKhuVuc = c;
                } else if (h.contains("sobaodanh") || h.contains("baodanh")) {
                    colSoBaoDanh = c;
                } else if (h.contains("dienthoai") || h.contains("phone") || h.equals("sdt")) {
                    colDienThoai = c;
                } else if (h.contains("email")) {
                    colEmail = c;
                } else if (h.contains("noisinh")) {
                    colNoiSinh = c;
                }
            }

            if (colCccd < 0) {
                throw new Exception("Không tìm thấy cột CCCD trong file Excel.\n"
                        + "Header cần có cột tên 'CCCD' hoặc 'cccd'.");
            }

            // ── FIX N+1: Preload TẤT CẢ thí sinh hiện có vào Map 1 lần duy nhất ──
            // Key = cccd (trimmed), Value = ThiSinh entity
            Map<String, ThiSinh> existingMap = new HashMap<>();
            for (ThiSinh ts : dao.findAll()) {
                if (ts.getCccd() != null) {
                    existingMap.put(ts.getCccd().trim(), ts);
                }
            }

            // ── Parse từng dòng ──
            List<ThiSinh> toSave = new ArrayList<>();
            int lastRow = sheet.getLastRowNum();

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) { result.skipCount++; continue; }

                try {
                    String cccd = getCellString(row, colCccd);
                    if (cccd == null || cccd.isEmpty()
                            || cccd.equalsIgnoreCase("cccd")
                            || cccd.contains("(*)")) {
                        result.skipCount++;
                        continue;
                    }
                    cccd = cccd.trim();

                    // Xác định entity (update existing hoặc tạo mới)
                    ThiSinh ts;
                    boolean isNew;
                    if (existingMap.containsKey(cccd)) {
                        ts = existingMap.get(cccd);  // dùng entity đã có (giữ nguyên id)
                        isNew = false;
                    } else {
                        ts = new ThiSinh();
                        ts.setCccd(cccd);
                        isNew = true;
                    }

                    // ── Họ Tên ──
                    if (colHoTen >= 0) {
                        String hoTen = getCellString(row, colHoTen);
                        if (hoTen != null && !hoTen.isEmpty()) {
                            hoTen = hoTen.trim();
                            int lastSpace = hoTen.lastIndexOf(' ');
                            if (lastSpace > 0) {
                                ts.setHo(hoTen.substring(0, lastSpace).trim());
                                ts.setTen(hoTen.substring(lastSpace + 1).trim());
                            } else {
                                ts.setHo("");
                                ts.setTen(hoTen);
                            }
                        }
                    } else {
                        if (colHo >= 0)  ts.setHo(getCellString(row, colHo));
                        if (colTen >= 0) ts.setTen(getCellString(row, colTen));
                    }

                    // ── Các trường khác ──
                    if (colNgaySinh >= 0) ts.setNgaySinh(getCellString(row, colNgaySinh));
                    if (colGioiTinh >= 0) ts.setGioiTinh(getCellString(row, colGioiTinh));
                    if (colDoiTuong >= 0) ts.setDoiTuong(parseDoiTuong(getCellString(row, colDoiTuong)));
                    if (colKhuVuc  >= 0) ts.setKhuVuc(parseKhuVuc(getCellString(row, colKhuVuc)));
                    if (colSoBaoDanh >= 0) ts.setSobaodanh(getCellString(row, colSoBaoDanh));
                    if (colDienThoai >= 0) ts.setDienThoai(getCellString(row, colDienThoai));
                    if (colEmail    >= 0) ts.setEmail(getCellString(row, colEmail));
                    if (colNoiSinh  >= 0) ts.setNoiSinh(getCellString(row, colNoiSinh));
                    ts.setUpdatedAt(LocalDate.now());

                    toSave.add(ts);
                    if (isNew) result.insertCount++;
                    else       result.updateCount++;

                } catch (Exception e) {
                    result.errorCount++;
                    result.errors.add("Dòng " + (i + 1) + ": " + e.getMessage());
                }
            }

            // ── Lưu vào DB theo batch ──
            if (!toSave.isEmpty()) {
                // Chia thành các batch nhỏ 500 bản ghi để tránh quá tải
                int batchSize = 500;
                for (int start = 0; start < toSave.size(); start += batchSize) {
                    int end = Math.min(start + batchSize, toSave.size());
                    List<ThiSinh> chunk = toSave.subList(start, end);
                    try {
                        dao.batchSaveOrUpdate(new ArrayList<>(chunk));
                    } catch (Exception e) {
                        int failed = chunk.size();
                        result.errorCount += failed;
                        result.insertCount -= chunk.stream().filter(t -> t.getIdthisinh() == null).mapToInt(t -> 1).sum();
                        result.updateCount -= chunk.stream().filter(t -> t.getIdthisinh() != null).mapToInt(t -> 1).sum();
                        result.errors.add("Lỗi lưu batch " + (start / batchSize + 1) + ": " + e.getMessage());
                    }
                }
            }
        }

        return result;
    }

    // ════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════

    /** Parse đối tượng ưu tiên: số → "01".."07", trống → "00". */
    private String parseDoiTuong(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "00";
        raw = raw.trim();
        try {
            int v = (int) Double.parseDouble(raw);
            return String.format("%02d", Math.max(0, Math.min(v, 9)));
        } catch (NumberFormatException e) {
            return raw.length() == 1 ? "0" + raw : raw.toUpperCase();
        }
    }

    /**
     * Parse khu vực: số 1→KV1, 2→KV2, 3→KV2NT, 4→KV3.
     * Nếu đã là "KV…" giữ nguyên.
     */
    private String parseKhuVuc(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "KV3";
        raw = raw.trim();
        if (raw.toUpperCase().startsWith("KV")) return raw.toUpperCase();
        try {
            int v = (int) Double.parseDouble(raw);
            switch (v) {
                case 1: return "KV1";
                case 2: return "KV2";
                case 3: return "KV2NT";
                default: return "KV3";
            }
        } catch (NumberFormatException e) {
            return raw.toUpperCase();
        }
    }

    /**
     * Chuẩn hóa header: lowercase, bỏ dấu tiếng Việt, bỏ khoảng trắng & ký tự đặc biệt.
     */
    private String normalize(String s) {
        if (s == null) return "";
        s = s.toLowerCase().trim();
        s = s.replaceAll("[àáạảãăắặẳẵâấậẩẫ]", "a");
        s = s.replaceAll("[èéẹẻẽêếệểễ]", "e");
        s = s.replaceAll("[ìíịỉĩ]", "i");
        s = s.replaceAll("[òóọỏõôốộổỗơớợởỡ]", "o");
        s = s.replaceAll("[ùúụủũưứựửữ]", "u");
        s = s.replaceAll("[ỳýỵỷỹ]", "y");
        s = s.replaceAll("[đ]", "d");
        s = s.replaceAll("[\\s_()*/\\-.ưđ*]", "");
        return s;
    }

    /**
     * Đọc nội dung cell thành String.
     * Xử lý số nguyên (tránh ".0"), ngày tháng, formula.
     */
    private String getCellString(Row row, int col) {
        if (col < 0) return null;
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    java.util.Date date = cell.getDateCellValue();
                    return String.format("%02d/%02d/%04d",
                            date.getDate(), date.getMonth() + 1, date.getYear() + 1900);
                }
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d) && !Double.isInfinite(d))
                    return String.valueOf((long) d);
                return String.valueOf(d);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try { return cell.getStringCellValue().trim(); }
                catch (Exception ex) {
                    double fd = cell.getNumericCellValue();
                    return fd == Math.floor(fd) ? String.valueOf((long) fd) : String.valueOf(fd);
                }
            default:
                return null;
        }
    }
}
