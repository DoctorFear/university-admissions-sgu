package com.xettuyen2026.entity;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "xt_nganh_tohop")
public class NganhTohop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "manganh", nullable = false, length = 45)
    private String manganh;

    @Column(name = "matohop", nullable = false, length = 45)
    private String matohop;

    @Column(name = "th_mon1", length = 10)
    private String thMon1;

    @Column(name = "hsmon1")
    private Byte hsmon1;

    @Column(name = "th_mon2", length = 10)
    private String thMon2;

    @Column(name = "hsmon2")
    private Byte hsmon2;

    @Column(name = "th_mon3", length = 10)
    private String thMon3;

    @Column(name = "hsmon3")
    private Byte hsmon3;

    @Column(name = "tb_keys", length = 45, unique = true)
    private String tbKeys;

    @Column(name = "N1")
    private Boolean n1;

    @Column(name = "`TO`")
    private Boolean to;

    @Column(name = "LI")
    private Boolean li;

    @Column(name = "HO")
    private Boolean ho;

    @Column(name = "SI")
    private Boolean si;

    @Column(name = "VA")
    private Boolean va;

    @Column(name = "SU")
    private Boolean su;

    @Column(name = "DI")
    private Boolean di;

    @Column(name = "TI")
    private Boolean ti;

    @Column(name = "KHAC")
    private Boolean khac;

    @Column(name = "KTPL")
    private Boolean ktpl;

    @Column(name = "dolech", precision = 6, scale = 2)
    private BigDecimal dolech;

    @OneToMany(mappedBy = "nganhTohop")
    private List<ThiSinhToHop> danhSachThiSinh;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getManganh() { return manganh; }
    public void setManganh(String manganh) { this.manganh = manganh; }
    public String getMatohop() { return matohop; }
    public void setMatohop(String matohop) { this.matohop = matohop; }
    public String getThMon1() { return thMon1; }
    public void setThMon1(String thMon1) { this.thMon1 = thMon1; }
    public Byte getHsmon1() { return hsmon1; }
    public void setHsmon1(Byte hsmon1) { this.hsmon1 = hsmon1; }
    public String getThMon2() { return thMon2; }
    public void setThMon2(String thMon2) { this.thMon2 = thMon2; }
    public Byte getHsmon2() { return hsmon2; }
    public void setHsmon2(Byte hsmon2) { this.hsmon2 = hsmon2; }
    public String getThMon3() { return thMon3; }
    public void setThMon3(String thMon3) { this.thMon3 = thMon3; }
    public Byte getHsmon3() { return hsmon3; }
    public void setHsmon3(Byte hsmon3) { this.hsmon3 = hsmon3; }
    public String getTbKeys() { return tbKeys; }
    public void setTbKeys(String tbKeys) { this.tbKeys = tbKeys; }
    public Boolean getN1() { return n1; }
    public void setN1(Boolean n1) { this.n1 = n1; }
    public Boolean getTo() { return to; }
    public void setTo(Boolean to) { this.to = to; }
    public Boolean getLi() { return li; }
    public void setLi(Boolean li) { this.li = li; }
    public Boolean getHo() { return ho; }
    public void setHo(Boolean ho) { this.ho = ho; }
    public Boolean getSi() { return si; }
    public void setSi(Boolean si) { this.si = si; }
    public Boolean getVa() { return va; }
    public void setVa(Boolean va) { this.va = va; }
    public Boolean getSu() { return su; }
    public void setSu(Boolean su) { this.su = su; }
    public Boolean getDi() { return di; }
    public void setDi(Boolean di) { this.di = di; }
    public Boolean getTi() { return ti; }
    public void setTi(Boolean ti) { this.ti = ti; }
    public Boolean getKhac() { return khac; }
    public void setKhac(Boolean khac) { this.khac = khac; }
    public Boolean getKtpl() { return ktpl; }
    public void setKtpl(Boolean ktpl) { this.ktpl = ktpl; }
    public BigDecimal getDolech() { return dolech; }
    public void setDolech(BigDecimal dolech) { this.dolech = dolech; }
    public List<ThiSinhToHop> getDanhSachThiSinh() { return danhSachThiSinh; }
    public void setDanhSachThiSinh(List<ThiSinhToHop> danhSachThiSinh) { this.danhSachThiSinh = danhSachThiSinh; }
}
