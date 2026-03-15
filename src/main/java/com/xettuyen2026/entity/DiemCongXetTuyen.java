package com.xettuyen2026.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_diemcongxetuyen")
public class DiemCongXetTuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemcong")
    private Integer iddiemcong;

    @Column(name = "ts_cccd", nullable = false, length = 45)
    private String tsCccd;

    @Column(name = "manganh", length = 20)
    private String manganh;

    @Column(name = "matohop", length = 10)
    private String matohop;

    @Column(name = "phuongthuc", length = 45)
    private String phuongthuc;

    @Column(name = "diemCC", precision = 6, scale = 2)
    private BigDecimal diemCC;

    @Column(name = "diemUtxt", precision = 6, scale = 2)
    private BigDecimal diemUtxt;

    @Column(name = "diemTong", precision = 6, scale = 2)
    private BigDecimal diemTong;

    @Column(name = "ghichu", columnDefinition = "TEXT")
    private String ghichu;

    @Column(name = "dc_keys", nullable = false, length = 45, unique = true)
    private String dcKeys;

    // Getters and Setters
    public Integer getIddiemcong() { return iddiemcong; }
    public void setIddiemcong(Integer iddiemcong) { this.iddiemcong = iddiemcong; }
    public String getTsCccd() { return tsCccd; }
    public void setTsCccd(String tsCccd) { this.tsCccd = tsCccd; }
    public String getManganh() { return manganh; }
    public void setManganh(String manganh) { this.manganh = manganh; }
    public String getMatohop() { return matohop; }
    public void setMatohop(String matohop) { this.matohop = matohop; }
    public String getPhuongthuc() { return phuongthuc; }
    public void setPhuongthuc(String phuongthuc) { this.phuongthuc = phuongthuc; }
    public BigDecimal getDiemCC() { return diemCC; }
    public void setDiemCC(BigDecimal diemCC) { this.diemCC = diemCC; }
    public BigDecimal getDiemUtxt() { return diemUtxt; }
    public void setDiemUtxt(BigDecimal diemUtxt) { this.diemUtxt = diemUtxt; }
    public BigDecimal getDiemTong() { return diemTong; }
    public void setDiemTong(BigDecimal diemTong) { this.diemTong = diemTong; }
    public String getGhichu() { return ghichu; }
    public void setGhichu(String ghichu) { this.ghichu = ghichu; }
    public String getDcKeys() { return dcKeys; }
    public void setDcKeys(String dcKeys) { this.dcKeys = dcKeys; }
}
