package com.xettuyen2026.dao;

import com.xettuyen2026.config.HibernateConfig;
import com.xettuyen2026.entity.ThiSinh;
import org.hibernate.Session;
import java.util.List;

public class ThiSinhDAO {

    public List<ThiSinh> findAll() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery("from ThiSinh", ThiSinh.class).list();
        }
    }

    public ThiSinh findByCccd(String cccd) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery("from ThiSinh t where t.cccd = :cccd", ThiSinh.class)
                    .setParameter("cccd", cccd)
                    .uniqueResult();
        }
    }
}
