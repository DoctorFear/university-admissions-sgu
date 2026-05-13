package com.xettuyen2026.dao;

import com.xettuyen2026.config.HibernateConfig;
import com.xettuyen2026.dao.base.BaseDAO;
import com.xettuyen2026.entity.NguyenVongXetTuyen;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NguyenVongDAO extends BaseDAO<NguyenVongXetTuyen> {

    public NguyenVongDAO() {
        super(NguyenVongXetTuyen.class);
    }

    public List<NguyenVongXetTuyen> findByCccd(String cccd) {
        return query(
            "FROM NguyenVongXetTuyen nv WHERE nv.nnCccd = :cccd ORDER BY nv.nvTt",
            q -> q.setParameter("cccd", cccd)
        );
    }

    public List<NguyenVongXetTuyen> findByMaNganh(String maNganh) {
        return query(
            "FROM NguyenVongXetTuyen nv WHERE nv.nvManganh = :ma ORDER BY nv.diemXettuyen DESC",
            q -> q.setParameter("ma", maNganh)
        );
    }

    public NguyenVongXetTuyen findByNvKeys(String keys) {
        List<NguyenVongXetTuyen> list = query(
            "FROM NguyenVongXetTuyen nv WHERE nv.nvKeys = :keys",
            q -> q.setParameter("keys", keys)
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public List<NguyenVongXetTuyen> findAllOrdered() {
        return query(
            "FROM NguyenVongXetTuyen ORDER BY nnCccd, nvTt",
            null
        );
    }

    /**
     * Đếm số lượng nguyện vọng theo từng ngành và phương thức.
     * Trả về Map: key = "maNganh|ttPhuongthuc", value = số lượng
     */
    public Map<String, Integer> countByNganhAndPhuongThuc() {
        String hql = "SELECT nv.nvManganh, nv.ttPhuongthuc, COUNT(nv) " +
                     "FROM NguyenVongXetTuyen nv " +
                     "WHERE nv.ttPhuongthuc IS NOT NULL " +
                     "GROUP BY nv.nvManganh, nv.ttPhuongthuc";

        try (Session session = openSession()) {
            List<Object[]> rows = session.createQuery(hql, Object[].class).list();
            Map<String, Integer> result = new HashMap<>();
            for (Object[] row : rows) {
                String maNganh     = (String) row[0];
                String phuongThuc  = (String) row[1];
                Long   count       = (Long)   row[2];
                if (maNganh == null || phuongThuc == null) continue;
                // Chuẩn hóa khóa đếm nguyện vọng theo ngành và phương thức
                result.merge(maNganh.trim().toUpperCase() + "|" + normalizePhuongThuc(phuongThuc),
                        count.intValue(), Integer::sum);
            }
            return result;
        }
    }

    // Chuẩn hóa mã phương thức xét tuyển khi đếm nguyện vọng
    private String normalizePhuongThuc(String phuongThuc) {
        String pt = phuongThuc != null ? phuongThuc.trim().toUpperCase() : "";
        if (pt.startsWith("PT2") || pt.contains("THPT")) return "PT2";
        if (pt.startsWith("PT4") || pt.contains("DGNL") || pt.contains("ĐGNL")) return "PT4";
        if (pt.startsWith("PT5") || pt.startsWith("PT3") || pt.contains("VSAT") || pt.contains("V-SAT")) return "PT5";
        return pt;
    }

    /** Giữ lại batchUpdate vì các service khác còn dùng */
    public void batchUpdate(List<NguyenVongXetTuyen> list) {
        Transaction tx = null;
        try (Session session = openSession()) {
            tx = session.beginTransaction();
            int count = 0;
            for (NguyenVongXetTuyen nv : list) {
                session.merge(nv);
                if (++count % 50 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi khi batch update nguyện vọng: " + e.getMessage(), e);
        }
    }
}
