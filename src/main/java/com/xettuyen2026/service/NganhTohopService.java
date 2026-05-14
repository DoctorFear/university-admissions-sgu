package com.xettuyen2026.service;

import com.xettuyen2026.dao.NganhDAO;
import com.xettuyen2026.dao.NganhTohopDAO;
import com.xettuyen2026.dao.TohopMonthiDAO;
import com.xettuyen2026.entity.Nganh;
import com.xettuyen2026.entity.NganhTohop;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

/**
 * Service cho quản lý ngành-tổ hợp xét tuyển.
 */
public class NganhTohopService {

    private final NganhTohopDAO dao = new NganhTohopDAO();
    private final NganhDAO nganhDAO = new NganhDAO();
    private final TohopMonthiDAO tohopDAO = new TohopMonthiDAO();

    public List<NganhTohop> findAll() {
        return dao.findAll();
    }

    public List<NganhTohop> findByMaNganh(String maNganh) {
        return dao.findByMaNganh(maNganh);
    }

    public List<NganhTohop> findByMaTohop(String maTohop) {
        return dao.findByMaTohop(maTohop);
    }

    public NganhTohop findByTbKeys(String tbKeys) {
        return dao.findByTbKeys(tbKeys);
    }

    /**
     * Thêm mới ngành-tổ hợp.
     */
    public void save(NganhTohop nt) {
        if (nt == null) {
            throw new IllegalArgumentException("Dữ liệu ngành-tổ hợp không được null");
        }

        String maNganh = nt.getManganh() != null ? nt.getManganh().trim().toUpperCase() : "";
        String maTohop = nt.getMatohop() != null ? nt.getMatohop().trim().toUpperCase() : "";
        String mon1 = nt.getThMon1() != null ? nt.getThMon1().trim().toUpperCase() : "";
        String mon2 = nt.getThMon2() != null ? nt.getThMon2().trim().toUpperCase() : "";
        String mon3 = nt.getThMon3() != null ? nt.getThMon3().trim().toUpperCase() : "";

        if (maNganh.isEmpty()) throw new IllegalArgumentException("Mã ngành không được trống");
        if (maTohop.isEmpty()) throw new IllegalArgumentException("Mã tổ hợp không được trống");
        if (mon1.isEmpty() || mon2.isEmpty() || mon3.isEmpty()) {
            throw new IllegalArgumentException("3 môn không được trống");
        }

        if (!nganhDAO.existsByMaNganh(maNganh)) {
            throw new IllegalArgumentException("Mã ngành " + maNganh + " không tồn tại!");
        }
        if (!tohopDAO.existsByMaTohop(maTohop)) {
            throw new IllegalArgumentException("Mã tổ hợp " + maTohop + " không tồn tại!");
        }

        String tbKeys = maNganh + "_" + maTohop;
        if (dao.existsByTbKeys(tbKeys)) {
            throw new IllegalArgumentException("Ngành-tổ hợp " + tbKeys + " đã tồn tại!");
        }

        nt.setManganh(maNganh);
        nt.setMatohop(maTohop);
        nt.setThMon1(mon1);
        nt.setThMon2(mon2);
        nt.setThMon3(mon3);
        nt.setTbKeys(tbKeys);
        setSubjectFlags(nt);
        dao.save(nt);
    }

    /**
     * Cập nhật ngành-tổ hợp — chỉ cập nhật hệ số và độ lệch,
     * không cần validate môn vì các trường này read-only trên form.
     */
    public void update(NganhTohop nt) {
        if (nt == null || nt.getId() == null) {
            throw new IllegalArgumentException("Dữ liệu ngành-tổ hợp không hợp lệ");
        }
        if (nt.getManganh() == null || nt.getManganh().trim().isEmpty()
                || nt.getMatohop() == null || nt.getMatohop().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã ngành và mã tổ hợp không được trống");
        }
        dao.update(nt);
    }

    /**
     * Xóa ngành-tổ hợp — không kiểm tra ràng buộc, xóa trực tiếp.
     */
    public void delete(NganhTohop nt) {
        if (nt == null || nt.getId() == null) {
            throw new IllegalArgumentException("Dữ liệu ngành-tổ hợp không hợp lệ");
        }
        dao.delete(nt);
    }

    /**
     * Import ngành-tổ hợp từ file tohopmon.txt.
     */
    public int importFromTohopMonFile(File file) throws IOException {
        List<NganhTohop> toImport = new ArrayList<>();
        Map<String, String> nganhNames = new LinkedHashMap<>();
        Pattern maNganhPattern = Pattern.compile("\\b(\\d{7}(?:CLC)?)\\b");
        Pattern tenNganhPattern = Pattern.compile("\\d{7}(?:CLC)?\\s+(.+?)\\s+\\w+\\(");
        Pattern maToHopPattern = Pattern.compile("(\\w+)\\((\\w+)-(\\d+),(\\w+)-(\\d+),(\\w+)-(\\d+)\\)");
        Pattern doLechPattern = Pattern.compile("(-?\\d+\\.\\d+)\\s*$");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            boolean headerSkipped = false;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("---")) continue;
                if (!headerSkipped) {
                    if (line.contains("STT") && line.contains("MANGANH")) headerSkipped = true;
                    continue;
                }

                Matcher maNganhMatcher = maNganhPattern.matcher(line);
                if (!maNganhMatcher.find()) continue;
                String maNganh = maNganhMatcher.group(1);

                if (!nganhNames.containsKey(maNganh)) {
                    Matcher tenMatcher = tenNganhPattern.matcher(line);
                    if (tenMatcher.find()) nganhNames.put(maNganh, tenMatcher.group(1).trim());
                }

                Matcher maToHopMatcher = maToHopPattern.matcher(line);
                if (!maToHopMatcher.find()) continue;

                String maTohop = maToHopMatcher.group(1);
                String mon1 = maToHopMatcher.group(2);
                byte hs1 = Byte.parseByte(maToHopMatcher.group(3));
                String mon2 = maToHopMatcher.group(4);
                byte hs2 = Byte.parseByte(maToHopMatcher.group(5));
                String mon3 = maToHopMatcher.group(6);
                byte hs3 = Byte.parseByte(maToHopMatcher.group(7));

                BigDecimal doLech = BigDecimal.ZERO;
                Matcher doLechMatcher = doLechPattern.matcher(line);
                if (doLechMatcher.find()) doLech = new BigDecimal(doLechMatcher.group(1));

                String key = maNganh + "_" + maTohop;

                NganhTohop nt = new NganhTohop();
                nt.setManganh(maNganh);
                nt.setMatohop(maTohop);
                nt.setThMon1(mon1); nt.setHsmon1(hs1);
                nt.setThMon2(mon2); nt.setHsmon2(hs2);
                nt.setThMon3(mon3); nt.setHsmon3(hs3);
                nt.setTbKeys(key);
                nt.setDolech(doLech);
                setSubjectFlags(nt);
                toImport.add(nt);
            }
        }

        autoCreateNganh(nganhNames);

        List<NganhTohop> newRecords = new ArrayList<>();
        for (NganhTohop nt : toImport) {
            if (dao.findByTbKeys(nt.getTbKeys()) == null) newRecords.add(nt);
        }
        if (!newRecords.isEmpty()) dao.saveAll(newRecords);
        return newRecords.size();
    }

    /**
     * Import ngành-tổ hợp từ file Excel.
     */
    public int importFromExcel(File file) throws IOException {
        List<NganhTohop> toImport = new ArrayList<>();
        Map<String, String> nganhNames = new LinkedHashMap<>();
        Pattern maToHopPattern = Pattern.compile("(\\w+)\\((\\w+)-(\\d+),(\\w+)-(\\d+),(\\w+)-(\\d+)\\)");

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = file.getName().toLowerCase().endsWith(".xlsx")
                     ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean headerSkipped = false;

            for (Row row : sheet) {
                if (!headerSkipped) { headerSkipped = true; continue; }

                String maNganh = getCellString(row, 1);
                if (maNganh.isEmpty()) continue;

                String tenNganh = getCellString(row, 2);
                if (!tenNganh.isEmpty() && !nganhNames.containsKey(maNganh)) {
                    nganhNames.put(maNganh, tenNganh);
                }

                String maToHopRaw = getCellString(row, 3);
                if (maToHopRaw.isEmpty()) continue;

                Matcher matcher = maToHopPattern.matcher(maToHopRaw);
                if (!matcher.find()) continue;

                String maTohop = matcher.group(1);
                String mon1 = matcher.group(2); byte hs1 = Byte.parseByte(matcher.group(3));
                String mon2 = matcher.group(4); byte hs2 = Byte.parseByte(matcher.group(5));
                String mon3 = matcher.group(6); byte hs3 = Byte.parseByte(matcher.group(7));

                BigDecimal doLech = BigDecimal.ZERO;
                double doLechVal = getCellNumeric(row, 7);
                if (doLechVal != 0) doLech = BigDecimal.valueOf(doLechVal);

                String key = maNganh + "_" + maTohop;

                NganhTohop nt = new NganhTohop();
                nt.setManganh(maNganh); nt.setMatohop(maTohop);
                nt.setThMon1(mon1.toUpperCase()); nt.setHsmon1(hs1);
                nt.setThMon2(mon2.toUpperCase()); nt.setHsmon2(hs2);
                nt.setThMon3(mon3.toUpperCase()); nt.setHsmon3(hs3);
                nt.setTbKeys(key); nt.setDolech(doLech);
                setSubjectFlags(nt);
                toImport.add(nt);
            }
        }

        autoCreateNganh(nganhNames);

        List<NganhTohop> newRecords = new ArrayList<>();
        for (NganhTohop nt : toImport) {
            if (dao.findByTbKeys(nt.getTbKeys()) == null) newRecords.add(nt);
        }
        if (!newRecords.isEmpty()) dao.saveAll(newRecords);
        return newRecords.size();
    }

    private void autoCreateNganh(Map<String, String> nganhNames) {
        for (Map.Entry<String, String> entry : nganhNames.entrySet()) {
            if (!nganhDAO.existsByMaNganh(entry.getKey())) {
                Nganh nganh = new Nganh();
                nganh.setManganh(entry.getKey());
                nganh.setTennganh(entry.getValue());
                nganh.setnChitieu(0);
                nganhDAO.save(nganh);
            }
        }
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                yield val == Math.floor(val) ? String.valueOf((long) val) : String.valueOf(val);
            }
            default -> "";
        };
    }

    private double getCellNumeric(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return 0;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> { try { yield Double.parseDouble(cell.getStringCellValue().trim()); }
                            catch (NumberFormatException e) { yield 0.0; } }
            default -> 0.0;
        };
    }

    private void setSubjectFlags(NganhTohop nt) {
        Set<String> subjects = new HashSet<>();
        if (nt.getThMon1() != null) subjects.add(nt.getThMon1().toUpperCase());
        if (nt.getThMon2() != null) subjects.add(nt.getThMon2().toUpperCase());
        if (nt.getThMon3() != null) subjects.add(nt.getThMon3().toUpperCase());

        nt.setN1(subjects.contains("N1"));
        nt.setTo(subjects.contains("TO"));
        nt.setLi(subjects.contains("LI"));
        nt.setHo(subjects.contains("HO"));
        nt.setSi(subjects.contains("SI"));
        nt.setVa(subjects.contains("VA"));
        nt.setSu(subjects.contains("SU"));
        nt.setDi(subjects.contains("DI"));
        nt.setTi(subjects.contains("TI"));
        nt.setKtpl(subjects.contains("KTPL"));

        Set<String> standard = Set.of("N1","TO","LI","HO","SI","VA","SU","DI","TI","KTPL");
        boolean hasOther = false;
        for (String s : subjects) { if (!standard.contains(s)) { hasOther = true; break; } }
        nt.setKhac(hasOther);
    }
}
