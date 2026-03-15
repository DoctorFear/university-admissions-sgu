package com.xettuyen2026.dao;

import com.xettuyen2026.config.HibernateConfig;
import com.xettuyen2026.entity.BangQuydoi;
import org.hibernate.Session;
import java.util.List;

public class BangQuydoiDAO {

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
}
