package com.xettuyen2026.dto;

import java.util.List;

public class TraCuuResponse {
    private boolean success;
    private String message;
    private List<NguyenVongDTO> nguyenVongs;
    
    // Tổng hợp chung
    private boolean isPending; // Có NV chưa xét
    private boolean isAdmitted; // Đậu 1 NV
    private NguyenVongDTO admittedNV;

    public static class NguyenVongDTO {
        private String maNganh;
        private String tenNganh;
        private Integer thuTu;
        private String toHop;
        private String phuongThuc;
        private Double diemXetTuyen;
        private Double diemSan;        // Điểm sàn ngành
        private Double diemChuan;      // Điểm chuẩn (điểm trúng tuyển thấp nhất)
        private String ketQua;
        
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
}
