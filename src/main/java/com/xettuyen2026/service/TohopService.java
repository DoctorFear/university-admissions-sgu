package com.xettuyen2026.service;

import com.xettuyen2026.dao.TohopMonthiDAO;
import com.xettuyen2026.entity.TohopMonthi;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TohopService {

    private final TohopMonthiDAO dao = new TohopMonthiDAO();

    public List<TohopMonthi> findAll() {
        return dao.findAll();
    }

    public TohopMonthi findByMaTohop(String maTohop) {
        return dao.findByMaTohop(maTohop);
    }

    public void save(TohopMonthi tohop) {
        dao.save(tohop);
    }

    public void update(TohopMonthi tohop) {
        dao.update(tohop);
    }

    public void delete(TohopMonthi tohop) {
        dao.delete(tohop);
    }

    /**
     * Import tổ hợp môn từ file tohopmon.txt.
     * Trích xuất các mã tổ hợp duy nhất từ cột MA_TO_HOP có dạng "A00(TO-3,LI-3,HO-1)".
     * Trả về số bản ghi đã import.
     */
    public int importFromTohopMonFile(File file) throws IOException {
        Map<String, TohopMonthi> tohopMap = new LinkedHashMap<>();

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

                // Parse: STT MANGANH TEN_NGANHCHUAN MA_TO_HOP(MON-HS,...) tb_keys TEN_TO_HOP Gốc Độ_lệch
                String[] parts = line.split("\\s{2,}");
                if (parts.length < 5) continue;

                // Find field containing parenthesis — that's MA_TO_HOP
                String maToHopField = null;
                for (String part : parts) {
                    if (part.contains("(") && part.contains(")")) {
                        maToHopField = part.trim();
                        break;
                    }
                }
                if (maToHopField == null) continue;

                // Parse: "A00(TO-3,LI-3,HO-1)" -> maTohop="A00", subjects=[TO,LI,HO]
                int parenIdx = maToHopField.indexOf('(');
                String maTohop = maToHopField.substring(0, parenIdx).trim();
                String subjectsStr = maToHopField.substring(parenIdx + 1, maToHopField.indexOf(')'));
                String[] subjectParts = subjectsStr.split(",");

                if (subjectParts.length < 3) continue;
                if (tohopMap.containsKey(maTohop)) continue;

                String mon1 = subjectParts[0].split("-")[0].trim();
                String mon2 = subjectParts[1].split("-")[0].trim();
                String mon3 = subjectParts[2].split("-")[0].trim();

                // Find TEN_TO_HOP from the parts after tb_keys
                String tenTohop = maTohop; // default

                TohopMonthi th = new TohopMonthi();
                th.setMatohop(maTohop);
                th.setMon1(mon1);
                th.setMon2(mon2);
                th.setMon3(mon3);
                th.setTentohop(tenTohop);
                tohopMap.put(maTohop, th);
            }
        }

        List<TohopMonthi> toImport = new ArrayList<>();
        for (TohopMonthi th : tohopMap.values()) {
            TohopMonthi existing = dao.findByMaTohop(th.getMatohop());
            if (existing == null) {
                toImport.add(th);
            }
        }

        if (!toImport.isEmpty()) {
            dao.saveAll(toImport);
        }
        return toImport.size();
    }

    /**
     * Import tổ hợp môn từ file Excel (.xlsx/.xls).
     * Cột: matohop, mon1, mon2, mon3, tentohop
     */
    public int importFromExcel(File file) throws IOException {
        Map<String, TohopMonthi> tohopMap = new LinkedHashMap<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = file.getName().toLowerCase().endsWith(".xlsx")
                     ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean headerSkipped = false;

            for (Row row : sheet) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                String maTohop = getCellString(row, 0);
                if (maTohop.isEmpty()) continue;
                if (tohopMap.containsKey(maTohop)) continue;

                String mon1 = getCellString(row, 1);
                String mon2 = getCellString(row, 2);
                String mon3 = getCellString(row, 3);
                String tenTohop = getCellString(row, 4);
                if (tenTohop.isEmpty()) tenTohop = maTohop;

                TohopMonthi th = new TohopMonthi();
                th.setMatohop(maTohop);
                th.setMon1(mon1.toUpperCase());
                th.setMon2(mon2.toUpperCase());
                th.setMon3(mon3.toUpperCase());
                th.setTentohop(tenTohop);
                tohopMap.put(maTohop, th);
            }
        }

        List<TohopMonthi> toImport = new ArrayList<>();
        for (TohopMonthi th : tohopMap.values()) {
            TohopMonthi existing = dao.findByMaTohop(th.getMatohop());
            if (existing == null) {
                toImport.add(th);
            }
        }

        if (!toImport.isEmpty()) {
            dao.saveAll(toImport);
        }
        return toImport.size();
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
}
