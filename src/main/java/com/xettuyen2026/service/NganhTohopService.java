package com.xettuyen2026.service;

import com.xettuyen2026.dao.NganhTohopDAO;
import com.xettuyen2026.entity.NganhTohop;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class NganhTohopService {

    private final NganhTohopDAO dao = new NganhTohopDAO();

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

                String[] parts = line.split("\\s{2,}");
                if (parts.length < 5) continue;

                // Extract MANGANH (second field after STT)
                String maNganh = null;
                String maToHopField = null;
                String tbKeys = null;
                String doLechStr = null;

                // Parse fields
                for (int i = 0; i < parts.length; i++) {
                    String p = parts[i].trim();
                    if (i == 0) continue; // STT
                    if (maNganh == null && p.matches("\\d{7}(CLC)?")) {
                        maNganh = p;
                        continue;
                    }
                    if (maToHopField == null && p.contains("(") && p.contains(")")) {
                        maToHopField = p;
                        continue;
                    }
                    if (tbKeys == null && p.contains("_") && p.matches("\\d{7}(CLC)?_.*")) {
                        tbKeys = p;
                        continue;
                    }
                }
                if (maNganh == null || maToHopField == null) continue;

                // Parse MA_TO_HOP field: "A00(TO-3,LI-3,HO-1)"
                int parenIdx = maToHopField.indexOf('(');
                String maTohop = maToHopField.substring(0, parenIdx).trim();
                String subjectsStr = maToHopField.substring(parenIdx + 1, maToHopField.indexOf(')'));
                String[] subjectParts = subjectsStr.split(",");
                if (subjectParts.length < 3) continue;

                String mon1 = subjectParts[0].split("-")[0].trim();
                byte hs1 = Byte.parseByte(subjectParts[0].split("-")[1].trim());
                String mon2 = subjectParts[1].split("-")[0].trim();
                byte hs2 = Byte.parseByte(subjectParts[1].split("-")[1].trim());
                String mon3 = subjectParts[2].split("-")[0].trim();
                byte hs3 = Byte.parseByte(subjectParts[2].split("-")[1].trim());

                // Parse Độ lệch (last numeric field)
                BigDecimal doLech = BigDecimal.ZERO;
                String lastField = parts[parts.length - 1].trim();
                try {
                    doLech = new BigDecimal(lastField);
                } catch (NumberFormatException e) {
                    // ignore
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
     * Import ngành-tổ hợp từ file Excel (.xlsx/.xls).
     * Cột: manganh, matohop, th_mon1, hsmon1, th_mon2, hsmon2, th_mon3, hsmon3, dolech
     */
    public int importFromExcel(File file) throws IOException {
        List<NganhTohop> toImport = new ArrayList<>();

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

                String maNganh = getCellString(row, 0);
                String maTohop = getCellString(row, 1);
                if (maNganh.isEmpty() || maTohop.isEmpty()) continue;

                String mon1 = getCellString(row, 2);
                byte hs1 = (byte) getCellNumeric(row, 3);
                String mon2 = getCellString(row, 4);
                byte hs2 = (byte) getCellNumeric(row, 5);
                String mon3 = getCellString(row, 6);
                byte hs3 = (byte) getCellNumeric(row, 7);

                BigDecimal doLech = BigDecimal.ZERO;
                double doLechVal = getCellNumeric(row, 8);
                if (doLechVal != 0) {
                    doLech = BigDecimal.valueOf(doLechVal);
                }

                String key = maNganh + "_" + maTohop;

                NganhTohop nt = new NganhTohop();
                nt.setManganh(maNganh);
                nt.setMatohop(maTohop);
                nt.setThMon1(mon1.toUpperCase());
                nt.setHsmon1(hs1 == 0 ? 1 : hs1);
                nt.setThMon2(mon2.toUpperCase());
                nt.setHsmon2(hs2 == 0 ? 1 : hs2);
                nt.setThMon3(mon3.toUpperCase());
                nt.setHsmon3(hs3 == 0 ? 1 : hs3);
                nt.setTbKeys(key);
                nt.setDolech(doLech);
                setSubjectFlags(nt);
                toImport.add(nt);
            }
        }

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
