package com.xettuyen2026.entity;

//entity/ThiSinh.java
@Entity
@Table(name = "xt_thisinhxettuyen25")
public class ThiSinh {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private int idthisinh;

 @Column(name = "cccd")
 private String cccd;

 @Column(name = "ho")
 private String ho;

 @Column(name = "ten")
 private String ten;

 @Column(name = "ngay_sinh")
 private String ngaySinh;

 // getters / setters...
}