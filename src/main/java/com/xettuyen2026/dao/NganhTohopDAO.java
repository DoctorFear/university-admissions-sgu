package com.xettuyen2026.dao;

import com.xettuyen2026.config.HibernateConfig;
import com.xettuyen2026.entity.NganhTohop;
import org.hibernate.Session;
import java.util.List;

public class NganhTohopDAO {
    
    public List<NganhTohop> findByMaNganh(String maNganh) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery("from NganhTohop n where n.manganh = :maNganh", NganhTohop.class)
                    .setParameter("maNganh", maNganh)
                    .list();
        }
    }
}
