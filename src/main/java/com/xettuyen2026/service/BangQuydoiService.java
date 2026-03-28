package com.xettuyen2026.service;

import com.xettuyen2026.dao.BangQuydoiDAO;
import com.xettuyen2026.entity.BangQuydoi;
import com.xettuyen2026.util.ImportUtil;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BangQuydoiService {

    private final BangQuydoiDAO bangQuydoiDAO;

    public BangQuydoiService() {
        this.bangQuydoiDAO = new BangQuydoiDAO();
    }

    public List<BangQuydoi> findAll() {
        return bangQuydoiDAO.findAll();
    }

    public List<BangQuydoi> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return bangQuydoiDAO.search(keyword.trim());
    }

    public void save(BangQuydoi q) {
        validate(q);
        if (bangQuydoiDAO.existsByMaQuydoi(q.getdMaquydoi())) {
            throw new RuntimeException("Mã quy đổi '" + q.getdMaquydoi() + "' đã tồn tại!");
        }
        bangQuydoiDAO.save(q);
    }

    public void update(BangQuydoi q) {
        validate(q);
        bangQuydoiDAO.update(q);
    }

    public void delete(BangQuydoi q) {
        bangQuydoiDAO.delete(q);
    }

    // ====================== IMPORT EXCEL ======================
    public int importFromExcel(File file) throws Exception {
        List<BangQuydoi> listToSave = new ArrayList<>();
        int newCount = 0;
        int updatedCount = 0;

        // ==================== SHEET DGNL ====================
        listToSave.addAll(ImportUtil.readExcel(file, "DGNL", row -> {
            String tohop = ImportUtil.getString(row, 0).trim();
            if (tohop.isEmpty() || 
                tohop.equalsIgnoreCase("Tổ hợp") || 
                tohop.equalsIgnoreCase("Đánh giá năng lực") ||
                tohop.equalsIgnoreCase("Kì thi V-SAT")) {
                return null;
            }

            BangQuydoi q = new BangQuydoi();
            q.setdPhuongthuc("DGNL");
            q.setdTohop(tohop);
            q.setdMon(null);
            q.setdDiema(ImportUtil.getDecimal(row, 1));
            q.setdDiemb(ImportUtil.getDecimal(row, 2));
            q.setdDiemc(ImportUtil.getDecimal(row, 3));
            q.setdDiemd(ImportUtil.getDecimal(row, 4));
            q.setdPhanvi(ImportUtil.getString(row, 5));

            // Sinh mã quy đổi sạch (không dấu, không khoảng trắng)
            String tohopClean = removeAccents(tohop).toUpperCase().replaceAll("\\s+", "_");
            q.setdMaquydoi("DGNL_" + tohopClean + "_" + q.getdPhanvi());
            return q;
        }));

        // ==================== SHEET V-SAT ====================
        listToSave.addAll(ImportUtil.readExcel(file, "V-SAT", row -> {
            String mon = ImportUtil.getString(row, 0).trim();
            if (mon.isEmpty() || 
                mon.equalsIgnoreCase("Môn") || 
                mon.equalsIgnoreCase("Đánh giá năng lực")) {
                return null;
            }

            BangQuydoi q = new BangQuydoi();
            q.setdPhuongthuc("V-SAT");
            q.setdTohop(null);
            q.setdMon(mon);
            q.setdDiema(ImportUtil.getDecimal(row, 1));
            q.setdDiemb(ImportUtil.getDecimal(row, 2));
            q.setdDiemc(ImportUtil.getDecimal(row, 3));
            q.setdDiemd(ImportUtil.getDecimal(row, 4));
            q.setdPhanvi(ImportUtil.getString(row, 5));

            // Sinh mã quy đổi sạch
            String monClean = removeAccents(mon).toUpperCase().replaceAll("\\s+", "_");
            q.setdMaquydoi("VSAT_" + monClean + "_" + q.getdPhanvi());
            return q;
        }));

        // Lưu dữ liệu
        for (BangQuydoi q : listToSave) {
            if (q == null) continue;

            if (bangQuydoiDAO.existsByMaQuydoi(q.getdMaquydoi())) {
                BangQuydoi existing = bangQuydoiDAO.findByMaQuydoi(q.getdMaquydoi());
                existing.setdDiema(q.getdDiema());
                existing.setdDiemb(q.getdDiemb());
                existing.setdDiemc(q.getdDiemc());
                existing.setdDiemd(q.getdDiemd());
                existing.setdPhanvi(q.getdPhanvi());
                bangQuydoiDAO.update(existing);
                updatedCount++;
            } else {
                bangQuydoiDAO.save(q);
                newCount++;
            }
        }

        return newCount + updatedCount;
    }

    // ====================== VALIDATE ======================
    private void validate(BangQuydoi q) {
        if (q.getdPhuongthuc() == null || q.getdPhuongthuc().trim().isEmpty()) {
            throw new RuntimeException("Phương thức không được để trống!");
        }
        if (q.getdMaquydoi() == null || q.getdMaquydoi().trim().isEmpty()) {
            throw new RuntimeException("Mã quy đổi không được để trống!");
        }
        if (q.getdDiema() == null || q.getdDiemb() == null) {
            throw new RuntimeException("Khoảng điểm đầu vào không được để trống!");
        }
        if (q.getdDiemc() == null || q.getdDiemd() == null) {
            throw new RuntimeException("Khoảng điểm THPT không được để trống!");
        }
        if (q.getdDiema().compareTo(q.getdDiemb()) >= 0) {
            throw new RuntimeException("Điểm a phải nhỏ hơn điểm b (a < b)!");
        }
        if (q.getdDiemc().compareTo(q.getdDiemd()) >= 0) {
            throw new RuntimeException("Điểm c phải nhỏ hơn điểm d (c < d)!");
        }
    }

    // ====================== HÀM HỖ TRỢ ======================
    /**
     * Loại bỏ dấu tiếng Việt và ký tự đặc biệt, chỉ giữ chữ cái, số và _
     */
    private String removeAccents(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("")
                      .replaceAll("[^a-zA-Z0-9]", "_");   // Thay ký tự lạ thành _
    }
}