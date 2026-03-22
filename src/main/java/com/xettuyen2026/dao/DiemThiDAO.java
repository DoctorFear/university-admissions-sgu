package com.xettuyen2026.dao;

import com.xettuyen2026.dao.base.BaseDAO;
import com.xettuyen2026.entity.DiemThiXetTuyen;
import java.util.List;

public class DiemThiDAO extends BaseDAO<DiemThiXetTuyen> {

    public DiemThiDAO() {
        super(DiemThiXetTuyen.class);
    }

    // Tim diem thi theo CCCD
    public DiemThiXetTuyen findByCccd(String cccd) {
        return queryOne("FROM DiemThiXetTuyen d WHERE d.cccd = :cccd",
                q -> q.setParameter("cccd", cccd));
    }

    // Tim diem thi theo phuong thuc (THPT / DGNL / VSAT)
    public List<DiemThiXetTuyen> findByPhuongThuc(String phuongThuc) {
        return query("FROM DiemThiXetTuyen d WHERE d.dPhuongthuc = :pt",
                q -> q.setParameter("pt", phuongThuc));
    }

    // Tim diem thi theo CCCD trong mot phuong thuc cu the
    public List<DiemThiXetTuyen> searchByCccd(String keyword, String phuongThuc) {
        String kw = "%" + keyword.toLowerCase() + "%";
        return query(
            "FROM DiemThiXetTuyen d WHERE d.dPhuongthuc = :pt AND LOWER(d.cccd) LIKE :kw",
                q -> q.setParameter("pt", phuongThuc).setParameter("kw", kw));
    }
    
    // Tim diem thi theo ten mon (mon co diem > 0) trong mot phuong thuc
    // Ma mon hop le: TO, VA, LI, HO, SI, SU, DI, GDCD, N1_THI, N1_CC, CNCN, CNNN, TI, KTPL, NK_DIEM1, NK_DIEM2, NL1
    public List<DiemThiXetTuyen> searchByMon(String fieldName, String phuongThuc) {
        String field = "to".equals(fieldName) ? "`to`" : fieldName;
        return query(
            "FROM DiemThiXetTuyen d WHERE d.dPhuongthuc = :pt AND d." + field + " > 0",
            q -> q.setParameter("pt", phuongThuc));
    }

    // Kiem tra CCCD da ton tai
    public boolean existsByCccd(String cccd) {
        long count = countQuery(
            "SELECT COUNT(*) FROM DiemThiXetTuyen d WHERE d.cccd = :cccd",
                q -> q.setParameter("cccd", cccd));
        return count > 0;
    }
}