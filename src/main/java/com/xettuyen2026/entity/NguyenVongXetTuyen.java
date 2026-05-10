package com.xettuyen2026.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_nguyenvongxettuyen")
public class NguyenVongXetTuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnv")
    private Integer idnv;

    @Column(name = "nn_cccd", nullable = false, length = 45)
    private String nnCccd;

    @Column(name = "nv_manganh", nullable = false, length = 45)
    private String nvManganh;

    @Column(name = "nv_tt", nullable = false)
    private Integer nvTt;

    @Column(name = "diem_thxt", precision = 10, scale = 5)
    private BigDecimal diemThxt;

    @Column(name = "diem_utqd", precision = 10, scale = 5)
    private BigDecimal diemUtqd;

    @Column(name = "diem_cong", precision = 6, scale = 2)
    private BigDecimal diemCong;

    @Column(name = "diem_xettuyen", precision = 10, scale = 5)
    private BigDecimal diemXettuyen;

    @Column(name = "nv_ketqua", length = 45)
    private String nvKetqua;

    @Column(name = "nv_keys", length = 45, unique = true)
    private String nvKeys;

    @Column(name = "tt_phuongthuc", length = 45)
    private String ttPhuongthuc;

    @Column(name = "tt_thm", length = 45)
    private String ttThm;

    // Getters and Setters
    public Integer getIdnv() {
        return idnv;
    }

    public void setIdnv(Integer idnv) {
        this.idnv = idnv;
    }

    public String getNnCccd() {
        return nnCccd;
    }

    public void setNnCccd(String nnCccd) {
        this.nnCccd = nnCccd;
    }

    public String getNvManganh() {
        return nvManganh;
    }

    public void setNvManganh(String nvManganh) {
        this.nvManganh = nvManganh;
    }

    public Integer getNvTt() {
        return nvTt;
    }

    public void setNvTt(Integer nvTt) {
        this.nvTt = nvTt;
    }

    public BigDecimal getDiemThxt() {
        return diemThxt;
    }

    public void setDiemThxt(BigDecimal diemThxt) {
        this.diemThxt = diemThxt;
    }

    public BigDecimal getDiemUtqd() {
        return diemUtqd;
    }

    public void setDiemUtqd(BigDecimal diemUtqd) {
        this.diemUtqd = diemUtqd;
    }

    public BigDecimal getDiemCong() {
        return diemCong;
    }

    public void setDiemCong(BigDecimal diemCong) {
        this.diemCong = diemCong;
    }

    public BigDecimal getDiemXettuyen() {
        return diemXettuyen;
    }

    public void setDiemXettuyen(BigDecimal diemXettuyen) {
        this.diemXettuyen = diemXettuyen;
    }

    public String getNvKetqua() {
        return nvKetqua;
    }

    public void setNvKetqua(String nvKetqua) {
        this.nvKetqua = nvKetqua;
    }

    public String getNvKeys() {
        return nvKeys;
    }

    public void setNvKeys(String nvKeys) {
        this.nvKeys = nvKeys;
    }

    public String getTtPhuongthuc() {
        return ttPhuongthuc;
    }

    public void setTtPhuongthuc(String ttPhuongthuc) {
        this.ttPhuongthuc = ttPhuongthuc;
    }

    public String getTtThm() {
        return ttThm;
    }

    public void setTtThm(String ttThm) {
        this.ttThm = ttThm;
    }
}
