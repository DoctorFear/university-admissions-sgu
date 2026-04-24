package com.xettuyen2026.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.xettuyen2026.config.HibernateConfig;
import com.xettuyen2026.dao.base.BaseDAO;
import com.xettuyen2026.entity.ThiSinh;

public class ThiSinhDAO extends BaseDAO<ThiSinh> {

    public ThiSinhDAO() {
        super(ThiSinh.class);
    }

    public List<ThiSinh> findAll() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery("from ThiSinh order by cccd", ThiSinh.class).list();
        }
    }

    public ThiSinh findByCccd(String cccd) {
        return queryOne("FROM ThiSinh t WHERE t.cccd = :cccd",
                q -> q.setParameter("cccd", cccd));
    }

    public boolean existsByCccd(String cccd) {
        long count = countQuery("SELECT COUNT(*) FROM ThiSinh t WHERE t.cccd = :cccd",
                q -> q.setParameter("cccd", cccd));
        return count > 0;
    }

    public List<ThiSinh> searchByKeyword(String keyword) {
        String kw = "%" + keyword.toLowerCase() + "%";
        return query("FROM ThiSinh t WHERE LOWER(t.cccd) LIKE :kw OR LOWER(t.ho) LIKE :kw OR LOWER(t.ten) LIKE :kw",
                q -> q.setParameter("kw", kw));
    }

    /** Tìm kiếm theo CCCD, họ tên (LIKE, case-insensitive). */
    public List<ThiSinh> search(String keyword) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            String kw = "%" + keyword.toLowerCase().trim() + "%";
            return session.createQuery(
                    "from ThiSinh t where lower(t.cccd) like :kw " +
                    "or lower(t.ho) like :kw or lower(t.ten) like :kw " +
                    "or lower(t.sobaodanh) like :kw order by t.cccd",
                    ThiSinh.class)
                .setParameter("kw", kw)
                .list();
        }
    }

    public void save(ThiSinh ts) {
        Transaction tx = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(ts);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi lưu thí sinh: " + e.getMessage(), e);
        }
    }

    public void update(ThiSinh ts) {
        Transaction tx = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(ts);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi cập nhật thí sinh: " + e.getMessage(), e);
        }
    }

    public void delete(ThiSinh ts) {
        Transaction tx = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            ThiSinh managed = session.merge(ts);
            session.remove(managed);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi xóa thí sinh: " + e.getMessage(), e);
        }
    }

    /**
     * Batch insert/update — dùng sau khi import Excel.
     * Flush mỗi 50 bản ghi để tránh out-of-memory.
     * Dùng explicit finally thay vì try-with-resources để session vẫn open khi rollback.
     */
    public void batchSaveOrUpdate(List<ThiSinh> list) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateConfig.getSessionFactory().openSession();
            tx = session.beginTransaction();
            int count = 0;
            for (ThiSinh ts : list) {
                session.merge(ts);
                if (++count % 50 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                try { tx.rollback(); } catch (Exception ex) { /* ignore rollback error */ }
            }
            throw new RuntimeException("Lỗi batch import thí sinh: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public ThiSinh findByCccdAndPassword(String cccd, String password) {
        return queryOne(
            "FROM ThiSinh t where t.cccd = :cccd AND t.password = :password",
            (query) -> {
                query.setParameter("cccd", cccd);
                query.setParameter("password", password);
            }
        );
    }
}
