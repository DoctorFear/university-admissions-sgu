package com.xettuyen2026.dao;

import com.xettuyen2026.dao.base.BaseDAO;
import com.xettuyen2026.entity.DiemThiXetTuyen;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DiemThiDAO extends BaseDAO<DiemThiXetTuyen> {

    public DiemThiDAO() {
        super(DiemThiXetTuyen.class);
    }

    /**
     * Lấy TẤT CẢ bản ghi điểm thi theo CCCD (tất cả phương thức).
     */
    public List<DiemThiXetTuyen> findAllByCccd(String cccd) {
        return query(
            "FROM DiemThiXetTuyen d WHERE d.cccd = :cccd ORDER BY d.dPhuongthuc",
            q -> q.setParameter("cccd", cccd)
        );
    }

    public DiemThiXetTuyen findByCccd(String cccd) {
        List<DiemThiXetTuyen> list = query(
            "FROM DiemThiXetTuyen d " +
            "WHERE d.cccd = :cccd " +
            "ORDER BY CASE d.dPhuongthuc " +
            "WHEN '1' THEN 1 " +
            "WHEN '4' THEN 2 " +
            "WHEN '5' THEN 3 " +
            "ELSE 9 END",
            q -> q.setParameter("cccd", cccd)
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public DiemThiXetTuyen findByCccdAndPhuongThuc(String cccd, String phuongThucCode) {
        return queryOne(
            "FROM DiemThiXetTuyen d WHERE d.cccd = :cccd AND d.dPhuongthuc = :pt",
            q -> q.setParameter("cccd", cccd).setParameter("pt", phuongThucCode)
        );
    }

    public List<DiemThiXetTuyen> findByPhuongThuc(String phuongThucCode) {
        return query(
            "FROM DiemThiXetTuyen d WHERE d.dPhuongthuc = :pt ORDER BY d.cccd",
            q -> q.setParameter("pt", phuongThucCode)
        );
    }

    public List<DiemThiXetTuyen> searchByCccd(String keyword, String phuongThucCode) {
        String kw = "%" + keyword.toLowerCase() + "%";
        return query(
            "FROM DiemThiXetTuyen d " +
            "WHERE d.dPhuongthuc = :pt AND LOWER(d.cccd) LIKE :kw " +
            "ORDER BY d.cccd",
            q -> q.setParameter("pt", phuongThucCode).setParameter("kw", kw)
        );
    }

    public List<DiemThiXetTuyen> searchByMon(String fieldName, String phuongThucCode) {
        return query(
            "FROM DiemThiXetTuyen d WHERE d.dPhuongthuc = :pt AND d." + fieldName + " > 0 ORDER BY d.cccd",
            q -> q.setParameter("pt", phuongThucCode)
        );
    }

    public List<DiemThiXetTuyen> searchByAnyMon(Collection<String> fieldNames, String phuongThucCode) {
        if (fieldNames == null || fieldNames.isEmpty()) {
            return findByPhuongThuc(phuongThucCode);
        }

        String condition = fieldNames.stream()
            .map(field -> "d." + field + " > 0")
            .collect(Collectors.joining(" OR "));

        return query(
            "FROM DiemThiXetTuyen d WHERE d.dPhuongthuc = :pt AND (" + condition + ") ORDER BY d.cccd",
            q -> q.setParameter("pt", phuongThucCode)
        );
    }

    public boolean existsByCccd(String cccd) {
        long count = countQuery(
            "SELECT COUNT(*) FROM DiemThiXetTuyen d WHERE d.cccd = :cccd",
            q -> q.setParameter("cccd", cccd)
        );
        return count > 0;
    }

    public boolean existsByCccdAndPhuongThuc(String cccd, String phuongThucCode) {
        long count = countQuery(
            "SELECT COUNT(*) FROM DiemThiXetTuyen d WHERE d.cccd = :cccd AND d.dPhuongthuc = :pt",
            q -> q.setParameter("cccd", cccd).setParameter("pt", phuongThucCode)
        );
        return count > 0;
    }
}
