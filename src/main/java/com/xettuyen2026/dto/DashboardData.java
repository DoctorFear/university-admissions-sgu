package com.xettuyen2026.dto;

import java.util.ArrayList;
import java.util.List;

// Chứa dữ liệu tổng hợp cho trang chủ
public class DashboardData {
    private long totalThiSinh;
    private long totalNganh;
    private long totalNguyenVong;
    private Long totalTrungTuyen;
    private List<TopNganhItem> topNganhItems = new ArrayList<>();
    private List<MethodStatItem> methodStats = new ArrayList<>();

    public long getTotalThiSinh() { return totalThiSinh; }
    public void setTotalThiSinh(long totalThiSinh) { this.totalThiSinh = totalThiSinh; }
    public long getTotalNganh() { return totalNganh; }
    public void setTotalNganh(long totalNganh) { this.totalNganh = totalNganh; }
    public long getTotalNguyenVong() { return totalNguyenVong; }
    public void setTotalNguyenVong(long totalNguyenVong) { this.totalNguyenVong = totalNguyenVong; }
    public Long getTotalTrungTuyen() { return totalTrungTuyen; }
    public void setTotalTrungTuyen(Long totalTrungTuyen) { this.totalTrungTuyen = totalTrungTuyen; }
    public List<TopNganhItem> getTopNganhItems() { return topNganhItems; }
    public void setTopNganhItems(List<TopNganhItem> topNganhItems) { this.topNganhItems = topNganhItems; }
    public List<MethodStatItem> getMethodStats() { return methodStats; }
    public void setMethodStats(List<MethodStatItem> methodStats) { this.methodStats = methodStats; }

    // Chứa một dòng ngành có nhiều nguyện vọng
    public static class TopNganhItem {
        private String maNganh;
        private String tenNganh;
        private long soNguyenVong;

        public TopNganhItem(String maNganh, String tenNganh, long soNguyenVong) {
            this.maNganh = maNganh;
            this.tenNganh = tenNganh;
            this.soNguyenVong = soNguyenVong;
        }

        public String getMaNganh() { return maNganh; }
        public String getTenNganh() { return tenNganh; }
        public long getSoNguyenVong() { return soNguyenVong; }
    }

    // Chứa thống kê nguyện vọng theo phương thức xét tuyển
    public static class MethodStatItem {
        private String label;
        private long count;
        private int percent;

        public MethodStatItem(String label, long count, int percent) {
            this.label = label;
            this.count = count;
            this.percent = percent;
        }

        public String getLabel() { return label; }
        public long getCount() { return count; }
        public int getPercent() { return percent; }
    }
}
