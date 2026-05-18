package com.xettuyen2026.dto;

import java.util.List;
import java.util.Map;

import com.xettuyen2026.entity.DiemThiXetTuyen;

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

    // Tất cả điểm thi theo từng phương thức: ptCode → {label_môn → điểm}
    private java.util.Map<String, java.util.Map<String, Double>> allDiemThi;
    // Tên hiển thị của các phương thức: ptCode → display name
    private java.util.Map<String, String> allDiemThiLabels;

    private DiemThiXetTuyen thpt; 
    private DiemThiXetTuyen vsat; 
    private DiemThiXetTuyen dgnl; 

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

        // Chi tiết nâng cao: hệ số, công thức, thế số
        private Integer heSo1;
        private Integer heSo2;
        private Integer heSo3;
        private String mon1Code;
        private String mon2Code;
        private String mon3Code;
        private Double diemMon1;
        private Double diemMon2;
        private Double diemMon3;
        private Double doLech;
        private String congThucTinh; // Chuỗi mô tả công thức
        private String congThucTheSo; // Chuỗi thế số cụ thể
        // Quy đổi VSAT/DGNL
        private Double diemMon1Conv;
        private Double diemMon2Conv;
        private Double diemMon3Conv;

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

        // Chi tiết nâng cao getters/setters
        public Integer getHeSo1() { return heSo1; }
        public void setHeSo1(Integer heSo1) { this.heSo1 = heSo1; }
        public Integer getHeSo2() { return heSo2; }
        public void setHeSo2(Integer heSo2) { this.heSo2 = heSo2; }
        public Integer getHeSo3() { return heSo3; }
        public void setHeSo3(Integer heSo3) { this.heSo3 = heSo3; }
        public String getMon1Code() { return mon1Code; }
        public void setMon1Code(String mon1Code) { this.mon1Code = mon1Code; }
        public String getMon2Code() { return mon2Code; }
        public void setMon2Code(String mon2Code) { this.mon2Code = mon2Code; }
        public String getMon3Code() { return mon3Code; }
        public void setMon3Code(String mon3Code) { this.mon3Code = mon3Code; }
        public Double getDiemMon1() { return diemMon1; }
        public void setDiemMon1(Double diemMon1) { this.diemMon1 = diemMon1; }
        public Double getDiemMon2() { return diemMon2; }
        public void setDiemMon2(Double diemMon2) { this.diemMon2 = diemMon2; }
        public Double getDiemMon3() { return diemMon3; }
        public void setDiemMon3(Double diemMon3) { this.diemMon3 = diemMon3; }
        public Double getDoLech() { return doLech; }
        public void setDoLech(Double doLech) { this.doLech = doLech; }
        public String getCongThucTinh() { return congThucTinh; }
        public void setCongThucTinh(String congThucTinh) { this.congThucTinh = congThucTinh; }
        public String getCongThucTheSo() { return congThucTheSo; }
        public void setCongThucTheSo(String congThucTheSo) { this.congThucTheSo = congThucTheSo; }
        public Double getDiemMon1Conv() { return diemMon1Conv; }
        public void setDiemMon1Conv(Double diemMon1Conv) { this.diemMon1Conv = diemMon1Conv; }
        public Double getDiemMon2Conv() { return diemMon2Conv; }
        public void setDiemMon2Conv(Double diemMon2Conv) { this.diemMon2Conv = diemMon2Conv; }
        public Double getDiemMon3Conv() { return diemMon3Conv; }
        public void setDiemMon3Conv(Double diemMon3Conv) { this.diemMon3Conv = diemMon3Conv; }
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

    // All điểm thi
    public java.util.Map<String, java.util.Map<String, Double>> getAllDiemThi() { return allDiemThi; }
    public void setAllDiemThi(java.util.Map<String, java.util.Map<String, Double>> allDiemThi) { this.allDiemThi = allDiemThi; }
    public java.util.Map<String, String> getAllDiemThiLabels() { return allDiemThiLabels; }
    public void setAllDiemThiLabels(java.util.Map<String, String> allDiemThiLabels) { this.allDiemThiLabels = allDiemThiLabels; }

    public DiemThiXetTuyen getThpt() { return thpt; }
    public void setThpt(DiemThiXetTuyen thpt) { this.thpt = thpt; }
    public DiemThiXetTuyen getVsat() { return vsat; }
    public void setVsat(DiemThiXetTuyen vsat) { this.vsat = vsat; }
    public DiemThiXetTuyen getDgnl() { return dgnl; }
    public void setDgnl(DiemThiXetTuyen dgnl) { this.dgnl = dgnl; }
}
