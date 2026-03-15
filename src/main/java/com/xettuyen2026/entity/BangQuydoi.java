package com.xettuyen2026.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_bangquydoi")
public class BangQuydoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idqd")
    private Integer idqd;

    @Column(name = "d_phuongthuc", length = 45)
    private String dPhuongthuc;

    @Column(name = "d_tohop", length = 45)
    private String dTohop;

    @Column(name = "d_mon", length = 45)
    private String dMon;

    @Column(name = "d_diema", precision = 6, scale = 2)
    private BigDecimal dDiema;

    @Column(name = "d_diemb", precision = 6, scale = 2)
    private BigDecimal dDiemb;

    @Column(name = "d_diemc", precision = 6, scale = 2)
    private BigDecimal dDiemc;

    @Column(name = "d_diemd", precision = 6, scale = 2)
    private BigDecimal dDiemd;

    @Column(name = "d_maquydoi", length = 45, unique = true)
    private String dMaquydoi;

    @Column(name = "d_phanvi", length = 45)
    private String dPhanvi;

    // Getters and Setters
    public Integer getIdqd() { return idqd; }
    public void setIdqd(Integer idqd) { this.idqd = idqd; }
    public String getdPhuongthuc() { return dPhuongthuc; }
    public void setdPhuongthuc(String dPhuongthuc) { this.dPhuongthuc = dPhuongthuc; }
    public String getdTohop() { return dTohop; }
    public void setdTohop(String dTohop) { this.dTohop = dTohop; }
    public String getdMon() { return dMon; }
    public void setdMon(String dMon) { this.dMon = dMon; }
    public BigDecimal getdDiema() { return dDiema; }
    public void setdDiema(BigDecimal dDiema) { this.dDiema = dDiema; }
    public BigDecimal getdDiemb() { return dDiemb; }
    public void setdDiemb(BigDecimal dDiemb) { this.dDiemb = dDiemb; }
    public BigDecimal getdDiemc() { return dDiemc; }
    public void setdDiemc(BigDecimal dDiemc) { this.dDiemc = dDiemc; }
    public BigDecimal getdDiemd() { return dDiemd; }
    public void setdDiemd(BigDecimal dDiemd) { this.dDiemd = dDiemd; }
    public String getdMaquydoi() { return dMaquydoi; }
    public void setdMaquydoi(String dMaquydoi) { this.dMaquydoi = dMaquydoi; }
    public String getdPhanvi() { return dPhanvi; }
    public void setdPhanvi(String dPhanvi) { this.dPhanvi = dPhanvi; }
}
