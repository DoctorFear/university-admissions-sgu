package com.xettuyen2026.dao;

import com.xettuyen2026.config.HibernateConfig;
import com.xettuyen2026.entity.BangQuydoi;
import org.hibernate.Session;
import java.util.List;
import com.xettuyen2026.dao.base.BaseDAO;

public class BangQuydoiDAO extends BaseDAO<BangQuydoi> {

    public BangQuydoiDAO() {
        super(BangQuydoi.class);
    }

    public List<BangQuydoi> findAll() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery("from BangQuydoi", BangQuydoi.class).list();
        }
    }

    public List<BangQuydoi> findByPhuongthucAndTohop(String phuongthuc, String tohop) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from BangQuydoi b where b.dPhuongthuc = :pt and b.dTohop = :th order by b.dDiema desc",
                    BangQuydoi.class)
                    .setParameter("pt", phuongthuc)
                    .setParameter("th", tohop)
                    .list();
        }
    }

    public List<BangQuydoi> findByPhuongthucAndMon(String phuongthuc, String mon) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from BangQuydoi b where b.dPhuongthuc = :pt and b.dMon = :mon order by b.dDiema desc",
                    BangQuydoi.class)
                    .setParameter("pt", phuongthuc)
                    .setParameter("mon", mon)
                    .list();
        }
    }

    // Tìm theo mã quy đổi (unique)
    public BangQuydoi findByMaQuydoi(String maQuydoi) {
        return queryOne("FROM BangQuydoi b WHERE b.dMaquydoi = :ma",
                q -> q.setParameter("ma", maQuydoi));
    }

    // Tìm kiếm theo từ khóa (phương thức, tổ hợp, môn, mã quy đổi)
    public List<BangQuydoi> search(String keyword) {
        String kw = "%" + keyword.toLowerCase().trim() + "%";
        return query("FROM BangQuydoi b WHERE " +
                "LOWER(b.dPhuongthuc) LIKE :kw OR " +
                "LOWER(b.dTohop) LIKE :kw OR " +
                "LOWER(b.dMon) LIKE :kw OR " +
                "LOWER(b.dMaquydoi) LIKE :kw",
                q -> q.setParameter("kw", kw));
    }

    // Kiểm tra mã quy đổi đã tồn tại
    public boolean existsByMaQuydoi(String maQuydoi) {
        long count = countQuery("SELECT COUNT(*) FROM BangQuydoi b WHERE b.dMaquydoi = :ma",
                q -> q.setParameter("ma", maQuydoi));
        return count > 0;
    }

}
