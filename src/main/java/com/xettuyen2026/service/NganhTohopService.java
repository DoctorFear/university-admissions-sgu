package com.xettuyen2026.service;

import com.xettuyen2026.dao.NganhDAO;
import com.xettuyen2026.dao.NganhTohopDAO;
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

public class NganhTohopService {

    private final NganhTohopDAO dao = new NganhTohopDAO();
    private final NganhDAO nganhDAO = new NganhDAO();

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

    public void save(NganhTohop nt) {
        nt.setTbKeys(nt.getManganh() + "_" + nt.getMatohop());
        setSubjectFlags(nt);
        dao.save(nt);
    }

    public void update(NganhTohop nt) {
        nt.setTbKeys(nt.getManganh() + "_" + nt.getMatohop());
        setSubjectFlags(nt);
        dao.update(nt);
    }

    public void delete(NganhTohop nt) {
        dao.delete(nt);
    }

    /**
     * Import ngành-tổ hợp từ file tohopmon.txt.
     * Mỗi dòng: STT MANGANH TEN_NGANHCHUAN MA_TO_HOP(MON1-HS1,MON2-HS2,MON3-HS3) tb_keys TEN_TO_HOP Gốc Độ_lệch
     * Trả về số bản ghi đã import.
     */
    public int importFromTohopMonFile(File file) throws IOException {
        List<NganhTohop> toImport = new ArrayList<>();
        Map<String, String> nganhNames = new LinkedHashMap<>();
        // Regex to extract maNganh (7 digits, optionally CLC)
        Pattern maNganhPattern = Pattern.compile("\\b(\\d{7}(?:CLC)?)\\b");
        // Regex to extract TEN_NGANHCHUAN (text between maNganh and MA_TO_HOP)
        Pattern tenNganhPattern = Pattern.compile("\\d{7}(?:CLC)?\\s+(.+?)\\s+\\w+\\(");
        // Regex to extract MA_TO_HOP with subjects: A00(TO-3,LI-3,HO-1)
        Pattern maToHopPattern = Pattern.compile("(\\w+)\\((\\w+)-(\\d+),(\\w+)-(\\d+),(\\w+)-(\\d+)\\)");
        // Regex to extract doLech (last decimal number in line)
        Pattern doLechPattern = Pattern.compile("(-?\\d+\\.\\d+)\\s*$");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            boolean headerSkipped = false;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("---")) continue;
                if (!headerSkipped) {
                    if (line.contains("STT") && line.contains("MANGANH")) {
                        headerSkipped = true;
                    }
                    continue;
                }

                // Extract maNganh
                Matcher maNganhMatcher = maNganhPattern.matcher(line);
                if (!maNganhMatcher.find()) continue;
                String maNganh = maNganhMatcher.group(1);

                // Extract tenNganh
                if (!nganhNames.containsKey(maNganh)) {
                    Matcher tenMatcher = tenNganhPattern.matcher(line);
                    if (tenMatcher.find()) {
                        nganhNames.put(maNganh, tenMatcher.group(1).trim());
                    }
                }

                // Extract MA_TO_HOP with subjects and coefficients
                Matcher maToHopMatcher = maToHopPattern.matcher(line);
                if (!maToHopMatcher.find()) continue;

                String maTohop = maToHopMatcher.group(1);
                String mon1 = maToHopMatcher.group(2);
                byte hs1 = Byte.parseByte(maToHopMatcher.group(3));
                String mon2 = maToHopMatcher.group(4);
                byte hs2 = Byte.parseByte(maToHopMatcher.group(5));
                String mon3 = maToHopMatcher.group(6);
                byte hs3 = Byte.parseByte(maToHopMatcher.group(7));

                // Extract doLech (last decimal number in line)
                BigDecimal doLech = BigDecimal.ZERO;
                Matcher doLechMatcher = doLechPattern.matcher(line);
                if (doLechMatcher.find()) {
                    doLech = new BigDecimal(doLechMatcher.group(1));
                }

                String key = maNganh + "_" + maTohop;

                NganhTohop nt = new NganhTohop();
                nt.setManganh(maNganh);
                nt.setMatohop(maTohop);
                nt.setThMon1(mon1);
                nt.setHsmon1(hs1);
                nt.setThMon2(mon2);
                nt.setHsmon2(hs2);
                nt.setThMon3(mon3);
                nt.setHsmon3(hs3);
                nt.setTbKeys(key);
                nt.setDolech(doLech);
                setSubjectFlags(nt);
                toImport.add(nt);
            }
        }

        // Auto-create ngành nếu chưa tồn tại
        autoCreateNganh(nganhNames);

        // Filter out existing records
        List<NganhTohop> newRecords = new ArrayList<>();
        for (NganhTohop nt : toImport) {
            NganhTohop existing = dao.findByTbKeys(nt.getTbKeys());
            if (existing == null) {
                newRecords.add(nt);
            }
        }

        if (!newRecords.isEmpty()) {
            dao.saveAll(newRecords);
        }
        return newRecords.size();
    }

    /**
     * Import ngành-tổ hợp từ file Excel (.xlsx/.xls) — file tohopmon.xlsx.
     * Cột trong file: STT(0), MANGANH(1), TEN_NGANHCHUAN(2), MA_TO_HOP(3), tb_keys(4), TEN_TO_HOP(5), Gốc(6), Độ lệch(7)
     * Trích xuất mã ngành, tổ hợp, môn + hệ số từ cột MA_TO_HOP dạng "A00(TO-3,LI-3,HO-1)".
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
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue; // skip header row
                }

                // Cột 1: MANGANH
                String maNganh = getCellString(row, 1);
                if (maNganh.isEmpty()) continue;

                // Cột 2: TEN_NGANHCHUAN — thu thập tên ngành
                String tenNganh = getCellString(row, 2);
                if (!tenNganh.isEmpty() && !nganhNames.containsKey(maNganh)) {
                    nganhNames.put(maNganh, tenNganh);
                }

                // Cột 3: MA_TO_HOP dạng "B03(TO-3,VA-3,SI-1)"
                String maToHopRaw = getCellString(row, 3);
                if (maToHopRaw.isEmpty()) continue;

                Matcher matcher = maToHopPattern.matcher(maToHopRaw);
                if (!matcher.find()) continue;

                String maTohop = matcher.group(1);
                String mon1 = matcher.group(2);
                byte hs1 = Byte.parseByte(matcher.group(3));
                String mon2 = matcher.group(4);
                byte hs2 = Byte.parseByte(matcher.group(5));
                String mon3 = matcher.group(6);
                byte hs3 = Byte.parseByte(matcher.group(7));

                // Cột 7: Độ lệch
                BigDecimal doLech = BigDecimal.ZERO;
                double doLechVal = getCellNumeric(row, 7);
                if (doLechVal != 0) {
                    doLech = BigDecimal.valueOf(doLechVal);
                }

                String key = maNganh + "_" + maTohop;

                NganhTohop nt = new NganhTohop();
                nt.setManganh(maNganh);
                nt.setMatohop(maTohop);
                nt.setThMon1(mon1.toUpperCase());
                nt.setHsmon1(hs1);
                nt.setThMon2(mon2.toUpperCase());
                nt.setHsmon2(hs2);
                nt.setThMon3(mon3.toUpperCase());
                nt.setHsmon3(hs3);
                nt.setTbKeys(key);
                nt.setDolech(doLech);
                setSubjectFlags(nt);
                toImport.add(nt);
            }
        }

        // Auto-create ngành nếu chưa tồn tại
        autoCreateNganh(nganhNames);

        List<NganhTohop> newRecords = new ArrayList<>();
        for (NganhTohop nt : toImport) {
            NganhTohop existing = dao.findByTbKeys(nt.getTbKeys());
            if (existing == null) {
                newRecords.add(nt);
            }
        }

        if (!newRecords.isEmpty()) {
            dao.saveAll(newRecords);
        }
        return newRecords.size();
    }

    /**
     * Tự động tạo ngành trong bảng xt_nganh nếu chưa tồn tại.
     */
    private void autoCreateNganh(Map<String, String> nganhNames) {
        for (Map.Entry<String, String> entry : nganhNames.entrySet()) {
            String maNganh = entry.getKey();
            String tenNganh = entry.getValue();
            if (!nganhDAO.existsByMaNganh(maNganh)) {
                Nganh nganh = new Nganh();
                nganh.setManganh(maNganh);
                nganh.setTennganh(tenNganh);
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
                if (val == Math.floor(val)) yield String.valueOf((long) val);
                else yield String.valueOf(val);
            }
            default -> "";
        };
    }

    private double getCellNumeric(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return 0;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                try { yield Double.parseDouble(cell.getStringCellValue().trim()); }
                catch (NumberFormatException e) { yield 0.0; }
            }
            default -> 0.0;
        };
    }

    /**
     * Set các cờ môn học (N1, TO, LI, HO, SI, VA, SU, DI, TI, KHAC, KTPL)
     * dựa trên th_mon1, th_mon2, th_mon3.
     */
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

        // KHAC = any subject that isn't one of the standard ones
        Set<String> standard = Set.of("N1", "TO", "LI", "HO", "SI", "VA", "SU", "DI", "TI", "KTPL");
        boolean hasOther = false;
        for (String s : subjects) {
            if (!standard.contains(s)) { hasOther = true; break; }
        }
        nt.setKhac(hasOther);
    }
}
