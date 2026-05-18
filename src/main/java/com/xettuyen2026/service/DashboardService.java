package com.xettuyen2026.service;

import com.xettuyen2026.dao.NganhDAO;
import com.xettuyen2026.dao.NguyenVongDAO;
import com.xettuyen2026.dao.ThiSinhDAO;
import com.xettuyen2026.dto.DashboardData;
import com.xettuyen2026.entity.Nganh;
import com.xettuyen2026.entity.NguyenVongXetTuyen;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardService {

    private final ThiSinhDAO thiSinhDAO;
    private final NganhDAO nganhDAO;
    private final NguyenVongDAO nguyenVongDAO;

    public DashboardService() {
        this.thiSinhDAO = new ThiSinhDAO();
        this.nganhDAO = new NganhDAO();
        this.nguyenVongDAO = new NguyenVongDAO();
    }

    // Lấy toàn bộ dữ liệu tổng hợp cho trang chủ
    public DashboardData getDashboardData() {
        List<Nganh> nganhList = nganhDAO.findAll();
        List<NguyenVongXetTuyen> nguyenVongList = nguyenVongDAO.findAll();

        DashboardData data = new DashboardData();
        data.setTotalThiSinh(thiSinhDAO.count());
        data.setTotalNganh(nganhList.size());
        data.setTotalNguyenVong(nguyenVongList.size());
        data.setTotalTrungTuyen(getTotalTrungTuyen(nganhList, nguyenVongList));
        data.setTopNganhItems(getTopNganhItems(nganhList, nguyenVongList));
        data.setMethodStats(getMethodStats(nguyenVongList));
        return data;
    }

    // Tính số lượng trúng tuyển sau khi đã có điểm chuẩn ngành
    private Long getTotalTrungTuyen(List<Nganh> nganhList, List<NguyenVongXetTuyen> nguyenVongList) {
        boolean daCoDiemChuan = nganhList.stream().anyMatch(n -> n.getnDiemtrungtuyen() != null);
        if (!daCoDiemChuan) {
            return null;
        }
        return nguyenVongList.stream()
                .filter(nv -> "yes".equalsIgnoreCase(nv.getNvKetqua()))
                .count();
    }

    // Lấy top ngành còn tồn tại có nhiều nguyện vọng nhất
    private List<DashboardData.TopNganhItem> getTopNganhItems(List<Nganh> nganhList, List<NguyenVongXetTuyen> nguyenVongList) {
        Map<String, Long> countByNganh = new LinkedHashMap<>();
        for (NguyenVongXetTuyen nv : nguyenVongList) {
            String maNganh = nv.getNvManganh();
            if (maNganh != null && !maNganh.trim().isEmpty()) {
                countByNganh.put(maNganh, countByNganh.getOrDefault(maNganh, 0L) + 1);
            }
        }

        List<DashboardData.TopNganhItem> rows = new ArrayList<>();
        for (Nganh nganh : nganhList) {
            long soNguyenVong = countByNganh.getOrDefault(nganh.getManganh(), 0L);
            if (soNguyenVong > 0) {
                rows.add(new DashboardData.TopNganhItem(
                        nganh.getManganh(),
                        nganh.getTennganh(),
                        soNguyenVong
                ));
            }
        }

        rows.sort(Comparator
                .comparingLong(DashboardData.TopNganhItem::getSoNguyenVong).reversed()
                .thenComparing(DashboardData.TopNganhItem::getTenNganh));
        return rows.size() > 10 ? new ArrayList<>(rows.subList(0, 10)) : rows;
    }

    // Thống kê số lượng nguyện vọng theo phương thức xét tuyển
    private List<DashboardData.MethodStatItem> getMethodStats(List<NguyenVongXetTuyen> nguyenVongList) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("PT1 - Tuyển thẳng", 0L);
        counts.put("THPT - Xét điểm THPT", 0L);
        counts.put("ĐGNL - Đánh giá năng lực", 0L);
        counts.put("VSAT - Đánh giá V-SAT", 0L);

        long total = 0;
        for (NguyenVongXetTuyen nv : nguyenVongList) {
            String label = normalizePhuongThuc(nv.getTtPhuongthuc());
            if (label != null) {
                counts.put(label, counts.get(label) + 1);
                total++;
            }
        }

        List<DashboardData.MethodStatItem> stats = new ArrayList<>();
        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            int percent = total > 0 ? (int) Math.round(entry.getValue() * 100.0 / total) : 0;
            stats.add(new DashboardData.MethodStatItem(entry.getKey(), entry.getValue(), percent));
        }
        return stats;
    }

    // Chuẩn hóa phương thức xét tuyển để thống kê
    private String normalizePhuongThuc(String phuongThuc) {
        if (phuongThuc == null) {
            return null;
        }
        String value = normalizeText(phuongThuc);
        if (value.isEmpty()) {
            return null;
        }
        if ("1".equals(value) || value.startsWith("PT1") || value.contains("TUYEN THANG") || "TT".equals(value)) {
            return "PT1 - Tuyển thẳng";
        }
        if ("2".equals(value) || value.startsWith("PT2") || value.contains("THPT")) {
            return "THPT - Xét điểm THPT";
        }
        if ("4".equals(value) || value.startsWith("PT4") || value.contains("DGNL")) {
            return "ĐGNL - Đánh giá năng lực";
        }
        if ("3".equals(value) || "5".equals(value) || value.startsWith("PT3") || value.startsWith("PT5")
                || value.contains("VSAT") || value.contains("V-SAT")) {
            return "VSAT - Đánh giá V-SAT";
        }
        return null;
    }

    // Chuẩn hóa chuỗi phương thức để thống kê đúng các kiểu lưu PT hiện tại
    private String normalizeText(String value) {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toUpperCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }
}
