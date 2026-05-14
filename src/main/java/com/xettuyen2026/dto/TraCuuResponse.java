package com.xettuyen2026.dto;

import java.util.List;
import java.util.Map;

public class TraCuuResponse {
    private boolean success;
    private String message;
    private List<NguyenVongDTO> nguyenVongs;
    
    // Tổng hợp chung
    private boolean isPending; // Có NV chưa xét
    private boolean isAdmitted; // Đậu 1 NV
    private NguyenVongDTO admittedNV;

    // Thông tin thí sinh
    private String hoTen;
    private String cccd;
    private String ngaySinh;
    private String gioiTinh;
    private String danToc;
    private String noiSinh;

    public static class NguyenVongDTO {
        private String maNganh;
        private String tenNganh;
        private Integer thuTu;
        private String toHop;           // Tổ hợp gốc ngành
        private String phuongThuc;
        private Double diemXetTuyen;
        private Double diemSan;         // Điểm sàn ngành
        private Double diemChuan;       // Điểm chuẩn (điểm trúng tuyển thấp nhất)
        private String ketQua;

        // Chi tiết điểm từ NguyenVongXetTuyen
        private Double diemThxt;        // Điểm tổ hợp xét tuyển
        private Double diemUtqd;        // Điểm ưu tiên quy đổi
        private Double diemCongDetail;  // Điểm cộng
        private String toHopXetTuyen;   // Tổ hợp được chọn (tt_thm)

        // Chi tiết điểm thi từng môn (mã môn -> điểm)
        private Map<String, Double> diemThiChiTiet;
        
        // Tên hiển thị phương thức
        private String phuongThucDisplay;

        public String getMaNganh() { return maNganh; }
        public void setMaNganh(String maNganh) { this.maNganh = maNganh; }
        public String getTenNganh() { return tenNganh; }
        public void setTenNganh(String tenNganh) { this.tenNganh = tenNganh; }
        public Integer getThuTu() { return thuTu; }
        public void setThuTu(Integer thuTu) { this.thuTu = thuTu; }
        public String getToHop() { return toHop; }
        public void setToHop(String toHop) { this.toHop = toHop; }
        public String getPhuongThuc() { return phuongThuc; }
        public void setPhuongThuc(String phuongThuc) { this.phuongThuc = phuongThuc; }
        public Double getDiemXetTuyen() { return diemXetTuyen; }
        public void setDiemXetTuyen(Double diemXetTuyen) { this.diemXetTuyen = diemXetTuyen; }
        public Double getDiemSan() { return diemSan; }
        public void setDiemSan(Double diemSan) { this.diemSan = diemSan; }
        public Double getDiemChuan() { return diemChuan; }
        public void setDiemChuan(Double diemChuan) { this.diemChuan = diemChuan; }
        public String getKetQua() { return ketQua; }
        public void setKetQua(String ketQua) { this.ketQua = ketQua; }

        // Chi tiết
        public Double getDiemThxt() { return diemThxt; }
        public void setDiemThxt(Double diemThxt) { this.diemThxt = diemThxt; }
        public Double getDiemUtqd() { return diemUtqd; }
        public void setDiemUtqd(Double diemUtqd) { this.diemUtqd = diemUtqd; }
        public Double getDiemCongDetail() { return diemCongDetail; }
        public void setDiemCongDetail(Double diemCongDetail) { this.diemCongDetail = diemCongDetail; }
        public String getToHopXetTuyen() { return toHopXetTuyen; }
        public void setToHopXetTuyen(String toHopXetTuyen) { this.toHopXetTuyen = toHopXetTuyen; }
        public Map<String, Double> getDiemThiChiTiet() { return diemThiChiTiet; }
        public void setDiemThiChiTiet(Map<String, Double> diemThiChiTiet) { this.diemThiChiTiet = diemThiChiTiet; }
        public String getPhuongThucDisplay() { return phuongThucDisplay; }
        public void setPhuongThucDisplay(String phuongThucDisplay) { this.phuongThucDisplay = phuongThucDisplay; }
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<NguyenVongDTO> getNguyenVongs() { return nguyenVongs; }
    public void setNguyenVongs(List<NguyenVongDTO> nguyenVongs) { this.nguyenVongs = nguyenVongs; }
    public boolean isPending() { return isPending; }
    public void setPending(boolean pending) { isPending = pending; }
    public boolean isAdmitted() { return isAdmitted; }
    public void setAdmitted(boolean admitted) { isAdmitted = admitted; }
    public NguyenVongDTO getAdmittedNV() { return admittedNV; }
    public void setAdmittedNV(NguyenVongDTO admittedNV) { this.admittedNV = admittedNV; }

    // Getters/Setters thông tin thí sinh
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }
    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }
    public String getDanToc() { return danToc; }
    public void setDanToc(String danToc) { this.danToc = danToc; }
    public String getNoiSinh() { return noiSinh; }
    public void setNoiSinh(String noiSinh) { this.noiSinh = noiSinh; }
}
