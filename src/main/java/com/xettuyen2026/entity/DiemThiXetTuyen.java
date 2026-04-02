package com.xettuyen2026.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "xt_diemthixettuyen",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_diemthi_cccd_pt", columnNames = {"cccd", "d_phuongthuc"})
    }
)
public class DiemThiXetTuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemthi")
    private Integer iddiemthi;

    @Column(name = "cccd", nullable = false, length = 20)
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

    // Nang khieu - NK1 den NK6
    @Column(name = "NK1", precision = 8, scale = 2)
    private BigDecimal nk1;

    @Column(name = "NK2", precision = 8, scale = 2)
    private BigDecimal nk2;

    @Column(name = "NK3", precision = 8, scale = 2)
    private BigDecimal nk3;

    @Column(name = "NK4", precision = 8, scale = 2)
    private BigDecimal nk4;

    @Column(name = "NK5", precision = 8, scale = 2)
    private BigDecimal nk5;

    @Column(name = "NK6", precision = 8, scale = 2)
    private BigDecimal nk6;

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
    public BigDecimal getNk1() { return nk1; }
    public void setNk1(BigDecimal nkDiem1) { this.nk1 = nkDiem1; }
    public BigDecimal getNk2() { return nk2; }
    public void setNk2(BigDecimal nkDiem2) { this.nk2 = nkDiem2; }
    public BigDecimal getNk3() { return nk3; }
    public void setNk3(BigDecimal nkDiem3) { this.nk3 = nkDiem3; }
    public BigDecimal getNk4() { return nk4; }
    public void setNk4(BigDecimal nkDiem4) { this.nk4 = nkDiem4; }
    public BigDecimal getNk5() { return nk5; }
    public void setNk5(BigDecimal nkDiem5) { this.nk5 = nkDiem5; }
    public BigDecimal getNk6() { return nk6; }
    public void setNk6(BigDecimal nkDiem6) { this.nk6 = nkDiem6; }
    public BigDecimal getNl1() { return nl1; }
    public void setNl1(BigDecimal nl1) { this.nl1 = nl1; }
}
