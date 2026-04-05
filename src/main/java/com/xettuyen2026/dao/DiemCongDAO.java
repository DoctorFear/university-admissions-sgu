package com.xettuyen2026.dao;

import java.util.List;

import org.hibernate.Session;

import com.xettuyen2026.config.HibernateConfig;
import com.xettuyen2026.dao.base.BaseDAO;
import com.xettuyen2026.entity.DiemCongXetTuyen;

public class DiemCongDAO extends BaseDAO<DiemCongXetTuyen> {
    
    public DiemCongDAO() {
        super(DiemCongXetTuyen.class);
    }

    public List<DiemCongXetTuyen> findByCccd(String cccd) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery("from DiemCongXetTuyen d where d.tsCccd = :cccd", DiemCongXetTuyen.class)
                    .setParameter("cccd", cccd)
                    .list();
        }
    }

    public DiemCongXetTuyen findByKey(String key) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from DiemCongXetTuyen d where d.dcKeys = :key",
                    DiemCongXetTuyen.class)
                    .setParameter("key", key)
                    .uniqueResult();
        }
    }

    public void delete(Integer id) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();

            DiemCongXetTuyen d = session.get(DiemCongXetTuyen.class, id);
            if (d != null) session.remove(d);

            tx.commit();
        }
    }
}
