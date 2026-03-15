package com.xettuyen2026.dao;

import com.xettuyen2026.config.HibernateConfig;
import com.xettuyen2026.entity.DiemCongXetTuyen;
import org.hibernate.Session;
import java.util.List;

public class DiemCongDAO {
    
    public List<DiemCongXetTuyen> findByCccd(String cccd) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery("from DiemCongXetTuyen d where d.tsCccd = :cccd", DiemCongXetTuyen.class)
                    .setParameter("cccd", cccd)
                    .list();
        }
    }
}
