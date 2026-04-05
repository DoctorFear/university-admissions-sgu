package com.xettuyen2026.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "xt_thisinh_tohop")
public class ThiSinhToHop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "idthisinh")
    private ThiSinh thiSinh;

    @ManyToOne
    @JoinColumn(name = "nganh_tohop_id")
    private NganhTohop nganhTohop;

    @Column(name = "diem_mon1")
    private Float diemMon1;

    @Column(name = "diem_mon2")
    private Float diemMon2;

    @Column(name = "diem_mon3")
    private Float diemMon3;

    @Column(name = "tong_diem")
    private Float tongDiem;

    @Column(name = "loai_diem")
    private String loaiDiem;

    // getters/setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public ThiSinh getThiSinh() { return thiSinh; }
    public void setThiSinh(ThiSinh thiSinh) { this.thiSinh = thiSinh; }
    public NganhTohop getNganhTohop() { return nganhTohop; }
    public void setNganhTohop(NganhTohop nganhTohop) { this.nganhTohop = nganhTohop; }
    public Float getDiemMon1() { return diemMon1; }
    public void setDiemMon1(Float diemMon1) { this.diemMon1 = diemMon1; }
    public Float getDiemMon2() { return diemMon2; }
    public void setDiemMon2(Float diemMon2) { this.diemMon2 = diemMon2; }
    public Float getDiemMon3() { return diemMon3; }
    public void setDiemMon3(Float diemMon3) { this.diemMon3 = diemMon3; }
    public Float getTongDiem() { return tongDiem; }
    public void setTongDiem(Float tongDiem) { this.tongDiem = tongDiem; }
    public String getLoaiDiem() { return loaiDiem; }
    public void setLoaiDiem(String loaiDiem) { this.loaiDiem = loaiDiem; }
}