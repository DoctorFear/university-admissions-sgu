package com.xettuyen2026.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;

import com.xettuyen2026.config.HibernateConfig;
import com.xettuyen2026.entity.User;

public class AuthService {

    public boolean register(String username, String password, String email) {
        Transaction tx = null;

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // check exists
            User existing = session
                    .createQuery("FROM User WHERE username = :u", User.class)
                    .setParameter("u", username)
                    .uniqueResult();

            if (existing != null) return false;

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setRole("USER");

            // hash password
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt(10));
            user.setPassword(hashed);

            session.persist(user);
            tx.commit();

            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            return false;
        }
    }

    public User login(String username, String password) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {

            User user = session
                    .createQuery("FROM User WHERE username = :u", User.class)
                    .setParameter("u", username)
                    .uniqueResult();

            if (user == null) return null;

            if (!user.isEnabled()) return null;

            // check password
            if (BCrypt.checkpw(password, user.getPassword())) {
                return user;
            }

            return null;
        }
    }
}