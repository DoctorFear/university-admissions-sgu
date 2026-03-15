package com.xettuyen2026.dao;

import com.xettuyen2026.config.HibernateConfig;
import com.xettuyen2026.entity.Nganh;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class NganhDAO {
    
    public List<Nganh> findAll() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery("from Nganh", Nganh.class).list();
        }
    }
    
    public Nganh findByMaNganh(String maNganh) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery("from Nganh n where n.manganh = :maNganh", Nganh.class)
                    .setParameter("maNganh", maNganh)
                    .uniqueResult();
        }
    }

    public void update(Nganh nganh) {
        Transaction tx = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(nganh);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi khi cập nhật ngành: " + e.getMessage(), e);
        }
    }
}
