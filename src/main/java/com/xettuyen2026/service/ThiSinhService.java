package com.xettuyen2026.service;

import java.io.File;
import java.io.FileInputStream;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.xettuyen2026.dao.ThiSinhDAO;
import com.xettuyen2026.entity.ThiSinh;

public class ThiSinhService {

    private final ThiSinhDAO thiSinhDAO;

    public ThiSinhService() {
        this.thiSinhDAO = new ThiSinhDAO();
    }

    public List<ThiSinh> findAll() {
        return thiSinhDAO.findAll();
    }

    public ThiSinh findByCccd(String cccd) {
        return thiSinhDAO.findByCccd(cccd);
    }

    public List<ThiSinh> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return thiSinhDAO.searchByKeyword(keyword.trim());
    }

    public void save(ThiSinh entity) {
        validate(entity);
        if (thiSinhDAO.existsByCccd(entity.getCccd())) {
            throw new RuntimeException("CCCD '" + entity.getCccd() + "' đã tồn tại!");
        }
        thiSinhDAO.save(entity);
    }

    public void update(ThiSinh entity) {
        validate(entity);
        thiSinhDAO.update(entity);
    }

    public void delete(ThiSinh entity) {
        thiSinhDAO.delete(entity);
    }

    public int importFromExcel(File file) throws Exception {
        int affected = 0;
        DataFormatter formatter = new DataFormatter();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) return 0;

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> headerMap = mapHeader(headerRow, formatter);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row, formatter)) continue;

                ThiSinh ts = mapRowToThiSinh(row, headerMap, formatter);
                if (ts == null) continue;
                if (!hasDisplayName(ts)) {
                    continue;
                }

                ThiSinh existing = thiSinhDAO.findByCccd(ts.getCccd());
                if (existing != null) {
                    thiSinhDAO.update(mergeImportedData(existing, ts));
                } else {
                    thiSinhDAO.save(ts);
                }
                affected++;
            }
        }
        return affected;
    }

    public int importDefaultDataFileIfPresent() {
        File defaultFile = new File("src/main/data/Ds thi sinh.xlsx");
        if (!defaultFile.exists()) return 0;
        try {
            return importFromExcel(defaultFile);
        } catch (Exception e) {
            return 0;
        }
    }

    private void validate(ThiSinh entity) {
        if (entity.getCccd() == null || entity.getCccd().trim().isEmpty()) {
            throw new RuntimeException("CCCD không được để trống!");
        }
        if (entity.getHo() == null || entity.getHo().trim().isEmpty()) {
            throw new RuntimeException("Họ không được để trống!");
        }
        if (entity.getTen() == null || entity.getTen().trim().isEmpty()) {
            throw new RuntimeException("Tên không được để trống!");
        }
    }

    private String normalize(String s) {
        if (s == null) return null;
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }

    private ThiSinh mapRowToThiSinh(Row row, Map<String, Integer> headerMap, DataFormatter formatter) {
        String cccd = firstNonBlank(
                readByHeader(row, headerMap, formatter, "cccd", "can cuoc cong dan"),
                readByIndex(row, formatter, 1),
                readByIndex(row, formatter, 0)
        );
        cccd = normalize(cccd);
        if (cccd == null) return null;

        ThiSinh ts = new ThiSinh();
        ts.setCccd(limit(cccd, 20));
        String hoTen = firstNonBlank(
                readByHeader(row, headerMap, formatter, "ho ten", "ho va ten", "ten day du", "fullname"),
                readByIndex(row, formatter, 2)
        );
        String[] splitName = splitFullName(hoTen);
        ts.setHo(splitName[0]);
        ts.setTen(splitName[1]);

        if (!hasDisplayName(ts)) {
            String fallbackName = firstNonBlank(hoTen, cccd);
            ts.setHo(fallbackName);
            ts.setTen(fallbackName);
        }

        ts.setSobaodanh(limit(firstNonBlank(
                readByHeader(row, headerMap, formatter, "so bao danh", "sbd"),
                cccd
        ), 45));
        if (ts.getHo() == null || ts.getTen() == null) {
            ts.setHo(limit(firstNonBlank(
                    ts.getHo(),
                    readByHeader(row, headerMap, formatter, "ho"),
                    readByIndex(row, formatter, 3)
            ), 100));
            ts.setTen(limit(firstNonBlank(
                    ts.getTen(),
                    readByHeader(row, headerMap, formatter, "ten"),
                    readByIndex(row, formatter, 4)
            ), 100));
        }
        ts.setHo(limit(ts.getHo(), 100));
        ts.setTen(limit(ts.getTen(), 100));
        ts.setNgaySinh(limit(firstNonBlank(
                readByHeader(row, headerMap, formatter, "ngay sinh", "ngaysinh"),
                readByIndex(row, formatter, 3)
        ), 45));
        ts.setGioiTinh(limit(normalizeGioiTinh(firstNonBlank(
                readByHeader(row, headerMap, formatter, "gioi tinh", "gioitinh"),
                readByIndex(row, formatter, 4)
        )), 10));
        // ts.setDienThoai(limit(firstNonBlank(
        //         readByHeader(row, headerMap, formatter, "dien thoai", "sdt", "so dien thoai"),
        //         readByIndex(row, formatter, 7)
        // ), 20));
        // ts.setEmail(limit(firstNonBlank(
        //         readByHeader(row, headerMap, formatter, "email"),
        //         readByIndex(row, formatter, 8)
        // ), 100));
        ts.setKhuVuc(limit(firstNonBlank(
                normalizeKhuVuc(readByHeader(row, headerMap, formatter, "khu vuc", "kvut", "kv ut", "kv uu tien", "kv", "khuvuc")),
                normalizeKhuVuc(readByIndex(row, formatter, 6))
        ), 45));
        ts.setDoiTuong(limit(normalizeDoiTuong(firstNonBlank(
                readByHeader(row, headerMap, formatter, "doi tuong", "dtut", "dt ut", "doi tuong uu tien", "dt", "doituong"),
                readByIndex(row, formatter, 5)
        )), 45));
        ts.setNoiSinh(limit(firstNonBlank(
                readByHeader(row, headerMap, formatter, "noi sinh"),
                readByIndex(row, formatter, 11)
        ), 45));
        ts.setDanToc(limit(firstNonBlank(
                readByHeader(row, headerMap, formatter, "dan toc", "dantoc"),
                readByIndex(row, formatter, 37)
        ), 100));
        ts.setPassword(generatePassword(ts.getNgaySinh()));
        return ts;
    }

    private String generatePassword(String ngaySinh) {
        ngaySinh = normalize(ngaySinh);

        String password = "";

        if (ngaySinh != null && ngaySinh.contains("/")) {
            String[] parts = ngaySinh.split("/");
            if (parts.length >= 2) {
                password += parts[0] + parts[1] + parts[2];
            }
        }

        return password;
    }

    private ThiSinh mergeImportedData(ThiSinh existing, ThiSinh imported) {
        existing.setSobaodanh(limit(imported.getSobaodanh(), 45));
        existing.setHo(limit(imported.getHo(), 100));
        existing.setTen(limit(imported.getTen(), 100));
        existing.setNgaySinh(limit(imported.getNgaySinh(), 45));
        existing.setDienThoai(limit(imported.getDienThoai(), 20));
        existing.setGioiTinh(limit(imported.getGioiTinh(), 10));
        existing.setEmail(limit(imported.getEmail(), 100));
        existing.setNoiSinh(limit(imported.getNoiSinh(), 45));
        existing.setDanToc(limit(imported.getDanToc(), 100));
        existing.setDoiTuong(limit(imported.getDoiTuong(), 45));
        existing.setKhuVuc(limit(imported.getKhuVuc(), 45));
        existing.setPassword(generatePassword(imported.getNgaySinh()));
        return existing;
    }

    private boolean hasDisplayName(ThiSinh thiSinh) {
        return normalize(thiSinh.getHo()) != null || normalize(thiSinh.getTen()) != null;
    }

    private Map<String, Integer> mapHeader(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null) return map;
        for (Cell cell : headerRow) {
            String key = normalizeHeader(formatter.formatCellValue(cell));
            if (key != null && !key.isEmpty()) {
                map.put(key, cell.getColumnIndex());
            }
        }
        return map;
    }

    private String readByHeader(Row row, Map<String, Integer> headerMap, DataFormatter formatter, String... headerNames) {
        for (String name : headerNames) {
            Integer idx = headerMap.get(normalizeHeader(name));
            if (idx == null) continue;
            String value = readByIndex(row, formatter, idx);
            if (value != null) return value;
        }
        return null;
    }

    private String readByIndex(Row row, DataFormatter formatter, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) return null;
        String value = formatter.formatCellValue(cell);
        return normalize(value);
    }

    private String readLastNonBlankCell(Row row, DataFormatter formatter) {
        short lastCellNum = row.getLastCellNum();
        for (int i = lastCellNum - 1; i >= 0; i--) {
            String value = readByIndex(row, formatter, i);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter) {
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK) {
                String value = normalize(formatter.formatCellValue(cell));
                if (value != null) return false;
            }
        }
        return true;
    }

    private String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            String normalized = normalize(candidate);
            if (normalized != null) return normalized;
        }
        return null;
    }

    private String normalizeHeader(String s) {
        String v = normalize(s);
        if (v == null) return null;
        String noAccent = Normalizer.normalize(v, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noAccent.toLowerCase().replaceAll("[^a-z0-9]+", " ").trim();
    }

    private String limit(String value, int maxLen) {
        String v = normalize(value);
        if (v == null) return null;
        if (v.length() <= maxLen) return v;
        return v.substring(0, maxLen);
    }

    private String[] splitFullName(String fullName) {
        String name = normalize(fullName);
        if (name == null) return new String[]{null, null};
        String[] parts = name.split("\\s+");
        if (parts.length == 1) return new String[]{"", parts[0]};
        String ten = parts[parts.length - 1];
        StringBuilder ho = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (i > 0) ho.append(' ');
            ho.append(parts[i]);
        }
        return new String[]{ho.toString(), ten};
    }

    private String normalizeKhuVuc(String kv) {
        String v = normalize(kv);
        if (v == null) return null;
        if ("nan".equalsIgnoreCase(v)) return null;
        if ("1".equals(v)) return "KV1";
        if ("2".equals(v)) return "KV2";
        if ("2NT".equalsIgnoreCase(v) || "KV2NT".equalsIgnoreCase(v)) return "KV2NT";
        if ("3".equals(v)) return "KV3";
        return v.toUpperCase();
    }

    private String normalizeDoiTuong(String doiTuong) {
        String v = normalize(doiTuong);
        if (v == null || "nan".equalsIgnoreCase(v)) return null;
        if (v.matches("\\d")) {
            return "0" + v;
        }
        return v.toUpperCase();
    }

    private String normalizeGioiTinh(String gioiTinh) {
        String v = normalize(gioiTinh);
        if (v == null || "nan".equalsIgnoreCase(v)) return null;
        if ("nu".equalsIgnoreCase(v) || "nữ".equalsIgnoreCase(v)) return "Nữ";
        if ("nam".equalsIgnoreCase(v)) return "Nam";
        return v;
    }


    public boolean authenticate(String cccd, String password) {
        ThiSinh ts = thiSinhDAO.findByCccdAndPassword(cccd, password);
        return ts != null;
    }
}
