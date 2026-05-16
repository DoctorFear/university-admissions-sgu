package com.xettuyen2026.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import com.xettuyen2026.config.HibernateConfig;
import com.xettuyen2026.dao.base.BaseDAO;
import com.xettuyen2026.entity.DiemCongXetTuyen;
import com.xettuyen2026.entity.NganhTohop;
import com.xettuyen2026.entity.NguyenVongXetTuyen;

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

    public List<Object[]> findNguyenVongAndToHopByCccd(String cccd) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            // Tách truy vấn để tránh lỗi collation khi so sánh mã ngành giữa hai bảng.
            List<NguyenVongXetTuyen> nguyenVongs = session.createQuery(
                    "from NguyenVongXetTuyen nv where nv.nnCccd = :cccd",
                    NguyenVongXetTuyen.class)
                    .setParameter("cccd", cccd)
                    .getResultList();
            List<Object[]> result = new ArrayList<>();

            for (NguyenVongXetTuyen nv : nguyenVongs) {
                List<NganhTohop> toHops = session.createQuery(
                        "from NganhTohop nt where nt.manganh = :manganh",
                        NganhTohop.class)
                        .setParameter("manganh", nv.getNvManganh())
                        .getResultList();

                for (NganhTohop nt : toHops) {
                    result.add(new Object[] { nv, nt });
                }
            }

            return result;
        }
    }

    
}
