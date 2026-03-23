package com.xettuyen2026.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "xt_diemthixettuyen")
public class DiemThiXetTuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemthi")
    private Integer iddiemthi;

    @Column(name = "cccd", nullable = false, length = 20, unique = true)
    private String cccd;

    @Column(name = "sobaodanh", length = 45)
    private String sobaodanh;

    @Column(name = "d_phuongthuc", length = 10)
    private String dPhuongthuc;

    // Mon bat buoc
    @Column(name = "`TO`", precision = 8, scale = 2)
    private BigDecimal to;

    @Column(name = "VA", precision = 8, scale = 2)
    private BigDecimal va;

    // Mon tu chon KHTN
    @Column(name = "LI", precision = 8, scale = 2)
    private BigDecimal li;

    @Column(name = "HO", precision = 8, scale = 2)
    private BigDecimal ho;

    @Column(name = "SI", precision = 8, scale = 2)
    private BigDecimal si;

    // Mon tu chon KHXH
    @Column(name = "SU", precision = 8, scale = 2)
    private BigDecimal su;

    @Column(name = "DI", precision = 8, scale = 2)
    private BigDecimal di;

    @Column(name = "GDCD", precision = 8, scale = 2)
    private BigDecimal gdcd;

    // Ngoai ngu
    @Column(name = "N1_THI", precision = 8, scale = 2)
    private BigDecimal n1Thi;

    @Column(name = "N1_CC", precision = 8, scale = 2)
    private BigDecimal n1Cc;

    // Mon khac
    @Column(name = "CNCN", precision = 8, scale = 2)
    private BigDecimal cncn;

    @Column(name = "CNNN", precision = 8, scale = 2)
    private BigDecimal cnnn;

    @Column(name = "TI", precision = 8, scale = 2)
    private BigDecimal ti;

    @Column(name = "KTPL", precision = 8, scale = 2)
    private BigDecimal ktpl;

    // Nang khieu - luu 2 mon thi + ma mon tuong ung
    @Column(name = "NK_MON1", length = 20)
    private String nkMon1;

    @Column(name = "NK_DIEM1", precision = 8, scale = 2)
    private BigDecimal nkDiem1;

    @Column(name = "NK_MON2", length = 20)
    private String nkMon2;

    @Column(name = "NK_DIEM2", precision = 8, scale = 2)
    private BigDecimal nkDiem2;

    // DGNL
    @Column(name = "NL1", precision = 8, scale = 2)
    private BigDecimal nl1;

    // Getters & Setters
    public Integer getIddiemthi() { return iddiemthi; }
    public void setIddiemthi(Integer iddiemthi) { this.iddiemthi = iddiemthi; }
    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public String getSobaodanh() { return sobaodanh; }
    public void setSobaodanh(String sobaodanh) { this.sobaodanh = sobaodanh; }
    public String getdPhuongthuc() { return dPhuongthuc; }
    public void setdPhuongthuc(String dPhuongthuc) { this.dPhuongthuc = dPhuongthuc; }
    public BigDecimal getTo() { return to; }
    public void setTo(BigDecimal to) { this.to = to; }
    public BigDecimal getVa() { return va; }
    public void setVa(BigDecimal va) { this.va = va; }
    public BigDecimal getLi() { return li; }
    public void setLi(BigDecimal li) { this.li = li; }
    public BigDecimal getHo() { return ho; }
    public void setHo(BigDecimal ho) { this.ho = ho; }
    public BigDecimal getSi() { return si; }
    public void setSi(BigDecimal si) { this.si = si; }
    public BigDecimal getSu() { return su; }
    public void setSu(BigDecimal su) { this.su = su; }
    public BigDecimal getDi() { return di; }
    public void setDi(BigDecimal di) { this.di = di; }
    public BigDecimal getGdcd() { return gdcd; }
    public void setGdcd(BigDecimal gdcd) { this.gdcd = gdcd; }
    public BigDecimal getN1Thi() { return n1Thi; }
    public void setN1Thi(BigDecimal n1Thi) { this.n1Thi = n1Thi; }
    public BigDecimal getN1Cc() { return n1Cc; }
    public void setN1Cc(BigDecimal n1Cc) { this.n1Cc = n1Cc; }
    public BigDecimal getCncn() { return cncn; }
    public void setCncn(BigDecimal cncn) { this.cncn = cncn; }
    public BigDecimal getCnnn() { return cnnn; }
    public void setCnnn(BigDecimal cnnn) { this.cnnn = cnnn; }
    public BigDecimal getTi() { return ti; }
    public void setTi(BigDecimal ti) { this.ti = ti; }
    public BigDecimal getKtpl() { return ktpl; }
    public void setKtpl(BigDecimal ktpl) { this.ktpl = ktpl; }
    public String getNkMon1() { return nkMon1; }
    public void setNkMon1(String nkMon1) { this.nkMon1 = nkMon1; }
    public BigDecimal getNkDiem1() { return nkDiem1; }
    public void setNkDiem1(BigDecimal nkDiem1) { this.nkDiem1 = nkDiem1; }
    public String getNkMon2() { return nkMon2; }
    public void setNkMon2(String nkMon2) { this.nkMon2 = nkMon2; }
    public BigDecimal getNkDiem2() { return nkDiem2; }
    public void setNkDiem2(BigDecimal nkDiem2) { this.nkDiem2 = nkDiem2; }
    public BigDecimal getNl1() { return nl1; }
    public void setNl1(BigDecimal nl1) { this.nl1 = nl1; }
}