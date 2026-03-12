package com.xettuyen2026.dao;

//dao/ThiSinhDAO.java
public class ThiSinhDAO {

 public ThiSinh findByCCCD(String cccd) {
     Session session = HibernateConfig.getSessionFactory().openSession();

     ThiSinh result = session.createQuery(
             "FROM ThiSinh WHERE cccd = :cccd", ThiSinh.class)
         .setParameter("cccd", cccd)
         .uniqueResult();

     session.close();
     return result;
 }
}