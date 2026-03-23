package com.xettuyen2026.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_groups")
public class UserGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_nhom", nullable = false)
    private String tenNhom;

    @Column(name = "ma_nhom", unique = true)
    private String maNhom;

    @Column(name = "loai_nhom")
    private String loaiNhom;

    @Column(name = "mo_ta")
    private String moTa;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private UserGroup parent;

    @OneToMany(mappedBy = "group")
    private List<User> users;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public String getTenNhom() { return tenNhom; }
    public void setTenNhom(String tenNhom) { this.tenNhom = tenNhom; }
    public String getMaNhom() { return maNhom; }
    public void setMaNhom(String maNhom) { this.maNhom = maNhom; }
    public String getLoaiNhom() { return loaiNhom; }
    public void setLoaiNhom(String loaiNhom) { this.loaiNhom = loaiNhom; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public UserGroup getParent() { return parent; }
    public void setParent(UserGroup parent) { this.parent = parent; }
    public List<User> getUsers() { return users; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}