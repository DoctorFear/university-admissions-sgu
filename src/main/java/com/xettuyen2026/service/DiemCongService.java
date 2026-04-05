package com.xettuyen2026.service;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.xettuyen2026.dao.DiemCongDAO;
import com.xettuyen2026.entity.DiemCongXetTuyen;

public class DiemCongService {
    private final DiemCongDAO dao = new DiemCongDAO();

    public void importAll(String fileAnh, String fileThiSinh, String fileUuTien) throws Exception {

        Map<String, BigDecimal> mapTiengAnh = loadTiengAnh(fileAnh);
        Map<String, BigDecimal> mapUuTienKV_DT = loadThiSinh(fileThiSinh);
        Map<String, BigDecimal> mapUuTienXT = loadUuTienXT(fileUuTien);

        // merge tất cả theo CCCD
        Map<String, BigDecimal> finalMap = new HashMap<>();

        merge(finalMap, mapTiengAnh);
        merge(finalMap, mapUuTienKV_DT);
        merge(finalMap, mapUuTienXT);

        // save DB
        for (String cccd : finalMap.keySet()) {
            DiemCongXetTuyen d = new DiemCongXetTuyen();
            d.setTsCccd(cccd);
            d.setDiemUtxt(finalMap.get(cccd));

            logic.prepare(d);
            dao.save(d);
        }
    }

    private Map<String, BigDecimal> loadTiengAnh(String path) throws Exception {
        Map<String, BigDecimal> map = new HashMap<>();

        Workbook wb = new XSSFWorkbook(new FileInputStream(path));
        Sheet sheet = wb.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String cccd = getString(row.getCell(0));
            BigDecimal diem = getNumber(row.getCell(1)); // cột điểm cộng

            map.put(cccd, diem);
        }

        wb.close();
        return map;
    }

    private Map<String, BigDecimal> loadThiSinh(String path) throws Exception {
        Map<String, BigDecimal> map = new HashMap<>();

        // bảng quy đổi hardcode (theo bạn cung cấp)
        Map<String, BigDecimal> rule = new HashMap<>();
        rule.put("01_1", new BigDecimal("0.75"));
        rule.put("02_2NT", new BigDecimal("0.5"));
        rule.put("03_2", new BigDecimal("0.25"));
        rule.put("04_3", BigDecimal.ZERO);

        Workbook wb = new XSSFWorkbook(new FileInputStream(path));
        Sheet sheet = wb.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String cccd = getString(row.getCell(0));
            String dtut = getString(row.getCell(1));
            String kvut = getString(row.getCell(2));

            String key = dtut + "_" + kvut;
            BigDecimal diem = rule.getOrDefault(key, BigDecimal.ZERO);

            map.put(cccd, diem);
        }

        wb.close();
        return map;
    }

    private Map<String, BigDecimal> loadUuTienXT(String path) throws Exception {
        Map<String, BigDecimal> map = new HashMap<>();

        Workbook wb = new XSSFWorkbook(new FileInputStream(path));
        Sheet sheet = wb.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String cccd = getString(row.getCell(0));

            BigDecimal diemDatGiai = getNumber(row.getCell(1));
            BigDecimal diemKhongDat = getNumber(row.getCell(2));

            // TODO: nếu bạn có tổ hợp → so sánh ở đây
            // tạm thời lấy max
            BigDecimal diem = diemDatGiai.max(diemKhongDat);

            map.put(cccd, diem);
        }

        wb.close();
        return map;
    }

    private void merge(Map<String, BigDecimal> target, Map<String, BigDecimal> source) {
        for (String key : source.keySet()) {
            target.put(
                key,
                target.getOrDefault(key, BigDecimal.ZERO).add(source.get(key))
            );
        }
    }

    private String getString(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private BigDecimal getNumber(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;

        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }

        try {
            return new BigDecimal(cell.getStringCellValue());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public void prepare(DiemCongXetTuyen d) {
        // null safety
        BigDecimal cc = d.getDiemCC() == null ? BigDecimal.ZERO : d.getDiemCC();
        BigDecimal ut = d.getDiemUtxt() == null ? BigDecimal.ZERO : d.getDiemUtxt();

        // tính tổng
        d.setDiemTong(cc.add(ut));

        // tạo key duy nhất
        String key = String.format("%s_%s_%s",
                safe(d.getTsCccd()),
                safe(d.getManganh()),
                safe(d.getMatohop())
        );

        d.setDcKeys(key);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}