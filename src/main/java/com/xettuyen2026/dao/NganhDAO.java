package com.xettuyen2026.dao;

import com.xettuyen2026.config.HibernateConfig;
import com.xettuyen2026.dao.base.BaseDAO;
import com.xettuyen2026.entity.Nganh;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class NganhDAO extends BaseDAO<Nganh>{
    
    public NganhDAO() {
    	super(Nganh.class);
    }
    
    // Tim nganh theo ma nganh 
    public Nganh findByMaNganh(String maNganh) {
    	return queryOne("FROM Nganh n Where n.manganh = :ma",
    			q -> q.setParameter("ma", maNganh));
    }
    
    // Tim nganh theo tu khoa (ma hoac ten)
    public List<Nganh> search(String keyword) {
    	String kw = "%" + keyword.toLowerCase() + "%";
    	return query("FROM Nganh n WHERE LOWER(n.manganh) LIKE :kw OR LOWER(n.tennganh) LIKE :kw",
    			q -> q.setParameter("kw", kw));
    }
	
    // Kiem tra ma nganh ton tai
    public boolean existsByMaNganh(String maNganh) {
    	long count = countQuery("SELECT COUNT(*) FROM Nganh n WHERE n.manganh = :ma",
    			q -> q.setParameter("ma", maNganh));
    	return count > 0;
    }
}
