package com.xettuyen2026.dao;

import com.xettuyen2026.dao.base.BaseDAO;
import com.xettuyen2026.entity.NganhTohop;

import java.util.List;

public class NganhTohopDAO extends BaseDAO<NganhTohop> {

    public NganhTohopDAO() {
        super(NganhTohop.class);
    }

    public List<NganhTohop> findByMaNganh(String maNganh) {
        return query("FROM NganhTohop n WHERE n.manganh = :maNganh",
                q -> q.setParameter("maNganh", maNganh));
    }

    public List<NganhTohop> findByMaTohop(String maTohop) {
        return query("FROM NganhTohop n WHERE n.matohop = :maTohop",
                q -> q.setParameter("maTohop", maTohop));
    }

    public NganhTohop findByTbKeys(String tbKeys) {
        return queryOne("FROM NganhTohop n WHERE n.tbKeys = :tbKeys",
                q -> q.setParameter("tbKeys", tbKeys));
    }
}
