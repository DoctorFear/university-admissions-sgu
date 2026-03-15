package com.xettuyen2026.dao;

import com.xettuyen2026.config.HibernateConfig;
import com.xettuyen2026.entity.NguyenVongXetTuyen;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class NguyenVongDAO {

    public List<NguyenVongXetTuyen> findAll() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery("from NguyenVongXetTuyen", NguyenVongXetTuyen.class).list();
        }
    }

    public NguyenVongXetTuyen findById(int id) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.get(NguyenVongXetTuyen.class, id);
        }
    }

    public List<NguyenVongXetTuyen> findByCccd(String cccd) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery("from NguyenVongXetTuyen nv where nv.nnCccd = :cccd order by nv.nvTt", NguyenVongXetTuyen.class)
                    .setParameter("cccd", cccd)
                    .list();
        }
    }

    public List<NguyenVongXetTuyen> findByMaNganh(String maNganh) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery("from NguyenVongXetTuyen nv where nv.nvManganh = :ma order by nv.diemXettuyen desc",
                    NguyenVongXetTuyen.class)
                    .setParameter("ma", maNganh)
                    .list();
        }
    }

    public NguyenVongXetTuyen findByNvKeys(String keys) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            List<NguyenVongXetTuyen> list = session.createQuery("from NguyenVongXetTuyen nv where nv.nvKeys = :keys", NguyenVongXetTuyen.class)
                    .setParameter("keys", keys)
                    .list();
            return list.isEmpty() ? null : list.get(0);
        }
    }

    public List<NguyenVongXetTuyen> findAllOrdered() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery("from NguyenVongXetTuyen order by nnCccd, nvTt", NguyenVongXetTuyen.class).list();
        }
    }

    public void save(NguyenVongXetTuyen nv) {
        Transaction tx = null;
        Session session = HibernateConfig.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();
            session.persist(nv);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi khi lưu nguyện vọng: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public void update(NguyenVongXetTuyen nv) {
        Transaction tx = null;
        Session session = HibernateConfig.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();
            session.merge(nv);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi khi cập nhật nguyện vọng: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public void delete(NguyenVongXetTuyen nv) {
        Transaction tx = null;
        Session session = HibernateConfig.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();
            NguyenVongXetTuyen managed = session.merge(nv);
            session.remove(managed);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi khi xóa nguyện vọng: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /** Batch update danh sách nguyện vọng (dùng sau xét tuyển). */
    public void batchUpdate(List<NguyenVongXetTuyen> list) {
        Transaction tx = null;
        Session session = HibernateConfig.getSessionFactory().openSession();
        try {
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
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
