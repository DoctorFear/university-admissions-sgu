package com.xettuyen2026.dao;

import org.hibernate.Session;
import org.mindrot.jbcrypt.BCrypt;

import com.xettuyen2026.config.HibernateConfig;
import com.xettuyen2026.dao.base.BaseDAO;
import com.xettuyen2026.entity.User;

public class UserDAO extends BaseDAO<User> {
    
    public UserDAO() {
        super(User.class);
    }

    @Override
    public void save(User user) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();

            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(10)));
            session.persist(user);

            tx.commit();
        }
    }

    @Override
    public void update(User user) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();

            User db = session.get(User.class, user.getId());

            db.setEmail(user.getEmail());
            db.setRole(user.getRole());
            db.setEnabled(user.isEnabled());

            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                db.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(10)));
            }

            session.merge(db);
            tx.commit();
        }
    }

    public void delete(Integer id) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();

            User u = session.get(User.class, id);
            if (u != null) session.remove(u);

            tx.commit();
        }
    }
}