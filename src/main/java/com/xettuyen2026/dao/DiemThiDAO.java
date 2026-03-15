package com.xettuyen2026.dao;

import com.xettuyen2026.config.HibernateConfig;
import com.xettuyen2026.entity.DiemThiXetTuyen;
import org.hibernate.Session;
import java.util.List;

public class DiemThiDAO {

    public List<DiemThiXetTuyen> findAll() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery("from DiemThiXetTuyen", DiemThiXetTuyen.class).list();
        }
    }

    public DiemThiXetTuyen findByCccd(String cccd) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery("from DiemThiXetTuyen d where d.cccd = :cccd", DiemThiXetTuyen.class)
                    .setParameter("cccd", cccd)
                    .uniqueResult();
        }
    }
}
