package com.xettuyen2026.service;

import com.xettuyen2026.dao.TohopMonthiDAO;
import com.xettuyen2026.entity.TohopMonthi;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

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
        // Regex matches: A00(TO-3,LI-3,HO-1) — captures maTohop and 3 subjects
        Pattern pattern = Pattern.compile("(\\w+)\\((\\w+)-(\\d+),(\\w+)-(\\d+),(\\w+)-(\\d+)\\)");

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

                Matcher matcher = pattern.matcher(line);
                if (!matcher.find()) continue;

                String maTohop = matcher.group(1);
                if (tohopMap.containsKey(maTohop)) continue;

                String mon1 = matcher.group(2);
                String mon2 = matcher.group(4);
                String mon3 = matcher.group(6);

                TohopMonthi th = new TohopMonthi();
                th.setMatohop(maTohop);
                th.setMon1(mon1);
                th.setMon2(mon2);
                th.setMon3(mon3);
                th.setTentohop(maTohop);
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
     * Import tổ hợp môn từ file Excel (.xlsx/.xls) — file tohopmon.xlsx.
     * Cột trong file: STT(0), MANGANH(1), TEN_NGANHCHUAN(2), MA_TO_HOP(3), tb_keys(4), TEN_TO_HOP(5), Gốc(6), Độ lệch(7)
     * Trích xuất mã tổ hợp duy nhất từ cột MA_TO_HOP có dạng "A00(TO-3,LI-3,HO-1)".
     */
    public int importFromExcel(File file) throws IOException {
        Map<String, TohopMonthi> tohopMap = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile("(\\w+)\\((\\w+)-(\\d+),(\\w+)-(\\d+),(\\w+)-(\\d+)\\)");

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

                // Cột 3: MA_TO_HOP dạng "B03(TO-3,VA-3,SI-1)"
                String maToHopRaw = getCellString(row, 3);
                if (maToHopRaw.isEmpty()) continue;

                Matcher matcher = pattern.matcher(maToHopRaw);
                if (!matcher.find()) continue;

                String maTohop = matcher.group(1);
                if (tohopMap.containsKey(maTohop)) continue;

                String mon1 = matcher.group(2);
                String mon2 = matcher.group(4);
                String mon3 = matcher.group(6);

                // Cột 5: TEN_TO_HOP
                String tenTohop = getCellString(row, 5);
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
