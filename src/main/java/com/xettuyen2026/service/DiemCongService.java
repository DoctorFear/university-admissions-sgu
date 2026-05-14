package com.xettuyen2026.service;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.xettuyen2026.dao.DiemCongDAO;
import com.xettuyen2026.dao.NganhTohopDAO;
import com.xettuyen2026.dao.ThiSinhDAO;
import com.xettuyen2026.entity.DiemCongXetTuyen;
import com.xettuyen2026.entity.NganhTohop;

public class DiemCongService {
    private final DiemCongDAO dao = new DiemCongDAO();
    private final ThiSinhDAO thiSinhDAO = new ThiSinhDAO();
    private final NganhTohopDAO nganhTohopDAO = new NganhTohopDAO();

    private static final String TYPE_THI_SINH = "THI_SINH";
    private static final String TYPE_TIENG_ANH = "TIENG_ANH";
    private static final String TYPE_UU_TIEN = "UU_TIEN";

    public void importAll(String fileAnh, String fileThiSinh, String fileUuTien) throws Exception {

        Map<String, DiemCongXetTuyen> map = new HashMap<>();

        loadTiengAnh(fileAnh, map);
        loadThiSinh(fileThiSinh, map);
        loadUuTienXT(fileUuTien, map);

        for (DiemCongXetTuyen d : map.values()) {
            prepare(d);

            DiemCongXetTuyen existed = dao.findByKey(d.getDcKeys());
            if (existed == null) {
                dao.save(d);
            } else {
                existed.setDiemCC(d.getDiemCC());
                existed.setDiemUtxt(d.getDiemUtxt());
                existed.setDiemTong(d.getDiemTong());
                existed.setGhichu(d.getGhichu());
                dao.update(existed);
            }
        }
    }

    private void loadTiengAnh(String path, Map<String, DiemCongXetTuyen> map) throws Exception {
        try (FileInputStream fis = new FileInputStream(path);
            Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String cccd = getString(row.getCell(1));
                BigDecimal diem = getNumber(row.getCell(5));

                List<Object[]> ds = dao.findNguyenVongAndToHopByCccd(cccd);

                for (Object[] item : ds) {
                    NganhTohop n = (NganhTohop) item[1];

                    DiemCongXetTuyen d = getOrCreate(
                        map,
                        cccd,
                        n.getManganh(),
                        n.getMatohop()
                    );

                    d.setDiemCC(d.getDiemCC().add(diem));
                    appendNote(d, TYPE_TIENG_ANH);
                }
            }
        }
    }

    private void loadUuTienXT(String path, Map<String, DiemCongXetTuyen> map) throws Exception {
        try (FileInputStream fis = new FileInputStream(path);
            Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(1);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String cccd = getString(row.getCell(1));
                String maMon = getString(row.getCell(4));
                BigDecimal dat = getNumber(row.getCell(6));
                BigDecimal khong = getNumber(row.getCell(7));

                List<Object[]> ds = dao.findNguyenVongAndToHopByCccd(cccd);

                for (Object[] item : ds) {
                    NganhTohop n = (NganhTohop) item[1];

                    BigDecimal diem = checkMon(n, maMon) ? dat : khong;

                    DiemCongXetTuyen d = getOrCreate(
                        map,
                        cccd,
                        n.getManganh(),
                        n.getMatohop()
                    );

                    d.setDiemCC(d.getDiemCC().add(diem));
                    appendNote(d, TYPE_UU_TIEN);
                }
            }
        }
    }

    private static final Set<String> DTUT_2 = Set.of("01", "02", "03", "04", "05");
    private static final Set<String> DTUT_1 = Set.of("06", "07");

    private BigDecimal mapUuTien(String dtut, String kvut) {
        BigDecimal diemCong = BigDecimal.ZERO;

        if (dtut != null && dtut.length() >= 2) {
            dtut = dtut.substring(0, 2);
        }

        if (DTUT_2.contains(dtut)) {
            diemCong = diemCong.add(BigDecimal.valueOf(2));
        } else if (DTUT_1.contains(dtut)) {
            diemCong = diemCong.add(BigDecimal.valueOf(1));
        }

        if (kvut != null) {
            switch (kvut) {
                case "1":
                    diemCong = diemCong.add(BigDecimal.valueOf(0.75));
                    break;
                case "2NT":
                    diemCong = diemCong.add(BigDecimal.valueOf(0.5));
                    break;
                case "2":
                    diemCong = diemCong.add(BigDecimal.valueOf(0.25));
                    break;
            }
        }

        return diemCong;
    }
    private void loadThiSinh(String path, Map<String, DiemCongXetTuyen> map) throws Exception {
        try (FileInputStream fis = new FileInputStream(path);
            Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String cccd = getString(row.getCell(1));
                String dtut = getString(row.getCell(5));
                String kvut = getString(row.getCell(6));

                BigDecimal diem = mapUuTien(dtut, kvut);

                List<Object[]> ds = dao.findNguyenVongAndToHopByCccd(cccd);

                for (Object[] item : ds) {
                    NganhTohop n = (NganhTohop) item[1];

                    DiemCongXetTuyen d = getOrCreate(
                        map,
                        cccd,
                        n.getManganh(),
                        n.getMatohop()
                    );

                    d.setDiemUtxt(d.getDiemUtxt().add(diem));
                    appendNote(d, TYPE_THI_SINH);
                }
            }
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
        BigDecimal cc = d.getDiemCC() == null ? BigDecimal.ZERO : d.getDiemCC();
        BigDecimal ut = d.getDiemUtxt() == null ? BigDecimal.ZERO : d.getDiemUtxt();

        d.setDiemTong(cc.add(ut));

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

    private DiemCongXetTuyen getOrCreate(Map<String, DiemCongXetTuyen> map, String cccd, String manganh, String matohop) {
        String key = cccd + "_" + manganh + "_" + matohop;

        return map.computeIfAbsent(key, k -> {
            DiemCongXetTuyen d = new DiemCongXetTuyen();
            d.setTsCccd(cccd);
            d.setManganh(manganh);
            d.setMatohop(matohop);
            d.setDiemCC(BigDecimal.ZERO);
            d.setDiemUtxt(BigDecimal.ZERO);
            return d;
        });
    }

    private boolean checkMon(NganhTohop n, String maMon) {
        if (n == null || maMon == null || maMon.isBlank()) return false;

        return switch (maMon.trim().toUpperCase()) {
            case "N1" -> Boolean.TRUE.equals(n.getN1());
            case "TO" -> Boolean.TRUE.equals(n.getTo());
            case "LI" -> Boolean.TRUE.equals(n.getLi());
            case "HO" -> Boolean.TRUE.equals(n.getHo());
            case "SI" -> Boolean.TRUE.equals(n.getSi());
            case "VA" -> Boolean.TRUE.equals(n.getVa());
            case "SU" -> Boolean.TRUE.equals(n.getSu());
            case "DI" -> Boolean.TRUE.equals(n.getDi());
            case "TI" -> Boolean.TRUE.equals(n.getTi());
            case "KTPL" -> Boolean.TRUE.equals(n.getKtpl());
            case "KHAC" -> Boolean.TRUE.equals(n.getKhac());
            default -> false;
        };
    }

    public void importTiengAnh(String path) throws Exception {
        Map<String, DiemCongXetTuyen> map = new HashMap<>();
        loadTiengAnh(path, map);
        saveAll(map);
    }

    public void importThiSinh(String path) throws Exception {
        Map<String, DiemCongXetTuyen> map = new HashMap<>();
        loadThiSinh(path, map);
        saveAll(map);
    }

    public void importUuTien(String path) throws Exception {
        Map<String, DiemCongXetTuyen> map = new HashMap<>();
        loadUuTienXT(path, map);
        saveAll(map);
    }

    private void saveAll(Map<String, DiemCongXetTuyen> map) {
        for (DiemCongXetTuyen d : map.values()) {
            prepare(d);

            DiemCongXetTuyen existed = dao.findByKey(d.getDcKeys());

            if (existed == null) {
                dao.save(d);
            } else {
                BigDecimal oldCC = existed.getDiemCC() == null
                        ? BigDecimal.ZERO
                        : existed.getDiemCC();

                BigDecimal oldUT = existed.getDiemUtxt() == null
                        ? BigDecimal.ZERO
                        : existed.getDiemUtxt();

                BigDecimal newCC = d.getDiemCC() == null
                        ? BigDecimal.ZERO
                        : d.getDiemCC();

                BigDecimal newUT = d.getDiemUtxt() == null
                        ? BigDecimal.ZERO
                        : d.getDiemUtxt();

                existed.setDiemCC(oldCC.add(newCC));
                existed.setDiemUtxt(oldUT.add(newUT));

                existed.setDiemTong(
                        existed.getDiemCC().add(existed.getDiemUtxt())
                );

                if (d.getGhichu() != null && !d.getGhichu().isBlank()) {
                    for (String type : d.getGhichu().split(",")) {
                        appendNote(existed, type);
                    }
                }

                dao.update(existed);
            }
        }
    }

    private void appendNote(DiemCongXetTuyen d, String type) {
        if (d.getGhichu() == null || d.getGhichu().isBlank()) {
            d.setGhichu(type);
        } else if (!d.getGhichu().contains(type)) {
            d.setGhichu(d.getGhichu() + "," + type);
        }
    }

    public void validate(DiemCongXetTuyen d) {

        String cccd = safe(d.getTsCccd());
        String manganh = safe(d.getManganh());
        String matohop = safe(d.getMatohop());

        if (cccd.isBlank()) {
            throw new RuntimeException("CCCD bắt buộc");
        }

        boolean thiSinhExists = thiSinhDAO.existsByCccd(cccd);

        if (!thiSinhExists) {
            throw new RuntimeException(
                    "CCCD không tồn tại trong danh sách thí sinh"
            );
        }

        if (manganh.isBlank()) {
            throw new RuntimeException("Mã ngành bắt buộc");
        }

        if (matohop.isBlank()) {
            throw new RuntimeException("Mã tổ hợp bắt buộc");
        }

        boolean exists = nganhTohopDAO.validate(manganh, matohop);

        if (!exists) {
            throw new RuntimeException(
                    "Không tồn tại tổ hợp " + matohop +
                    " cho ngành " + manganh
            );
        }
    }
}