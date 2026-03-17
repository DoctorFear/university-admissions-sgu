package com.xettuyen2026.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "xt_tohop_monthi")
public class TohopMonthi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtohop")
    private Integer idtohop;

    @Column(name = "matohop", nullable = false, length = 45, unique = true)
    private String matohop;

    @Column(name = "mon1", nullable = false, length = 10)
    private String mon1;

    @Column(name = "mon2", nullable = false, length = 10)
    private String mon2;

    @Column(name = "mon3", nullable = false, length = 10)
    private String mon3;

    @Column(name = "tentohop", length = 100)
    private String tentohop;

    // Getters and Setters
    public Integer getIdtohop() { return idtohop; }
    public void setIdtohop(Integer idtohop) { this.idtohop = idtohop; }
    public String getMatohop() { return matohop; }
    public void setMatohop(String matohop) { this.matohop = matohop; }
    public String getMon1() { return mon1; }
    public void setMon1(String mon1) { this.mon1 = mon1; }
    public String getMon2() { return mon2; }
    public void setMon2(String mon2) { this.mon2 = mon2; }
    public String getMon3() { return mon3; }
    public void setMon3(String mon3) { this.mon3 = mon3; }
    public String getTentohop() { return tentohop; }
    public void setTentohop(String tentohop) { this.tentohop = tentohop; }
}
