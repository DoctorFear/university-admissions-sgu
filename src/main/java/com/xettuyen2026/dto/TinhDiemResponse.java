package com.xettuyen2026.dto;

import java.util.List;
import java.util.Map;

public class TinhDiemResponse {
    private boolean success;
    private String message;
    private String maNganh;
    private String tenNganh;
    private String toHop;
    
    private Double diemThi;
    private Double diemQuyDoi;
    private Double diemCong;
    private Double diemUuTien;
    private Double diemXetTuyen;
    private Double diemNguong; // diem san
    private Boolean isDat;
    
    // For DGNL interpolation formula
    private Double diema;
    private Double diemb;
    private Double diemc;
    private Double diemd;
    
    // For VSAT combinations
    private List<Map<String, Object>> toHopDetails;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getMaNganh() { return maNganh; }
    public void setMaNganh(String maNganh) { this.maNganh = maNganh; }
    public String getTenNganh() { return tenNganh; }
    public void setTenNganh(String tenNganh) { this.tenNganh = tenNganh; }
    public String getToHop() { return toHop; }
    public void setToHop(String toHop) { this.toHop = toHop; }
    public Double getDiemThi() { return diemThi; }
    public void setDiemThi(Double diemThi) { this.diemThi = diemThi; }
    public Double getDiemQuyDoi() { return diemQuyDoi; }
    public void setDiemQuyDoi(Double diemQuyDoi) { this.diemQuyDoi = diemQuyDoi; }
    public Double getDiemCong() { return diemCong; }
    public void setDiemCong(Double diemCong) { this.diemCong = diemCong; }
    public Double getDiemUuTien() { return diemUuTien; }
    public void setDiemUuTien(Double diemUuTien) { this.diemUuTien = diemUuTien; }
    public Double getDiemXetTuyen() { return diemXetTuyen; }
    public void setDiemXetTuyen(Double diemXetTuyen) { this.diemXetTuyen = diemXetTuyen; }
    public Double getDiemNguong() { return diemNguong; }
    public void setDiemNguong(Double diemNguong) { this.diemNguong = diemNguong; }
    public Boolean getIsDat() { return isDat; }
    public void setIsDat(Boolean isDat) { this.isDat = isDat; }
    public Double getDiema() { return diema; }
    public void setDiema(Double diema) { this.diema = diema; }
    public Double getDiemb() { return diemb; }
    public void setDiemb(Double diemb) { this.diemb = diemb; }
    public Double getDiemc() { return diemc; }
    public void setDiemc(Double diemc) { this.diemc = diemc; }
    public Double getDiemd() { return diemd; }
    public void setDiemd(Double diemd) { this.diemd = diemd; }
    public List<Map<String, Object>> getToHopDetails() { return toHopDetails; }
    public void setToHopDetails(List<Map<String, Object>> toHopDetails) { this.toHopDetails = toHopDetails; }
}
