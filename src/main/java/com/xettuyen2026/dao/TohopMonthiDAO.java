package com.xettuyen2026.dao;

import com.xettuyen2026.dao.base.BaseDAO;
import com.xettuyen2026.entity.TohopMonthi;

public class TohopMonthiDAO extends BaseDAO<TohopMonthi> {

    public TohopMonthiDAO() {
        super(TohopMonthi.class);
    }

    public TohopMonthi findByMaTohop(String maTohop) {
        return queryOne("FROM TohopMonthi t WHERE t.matohop = :ma",
                q -> q.setParameter("ma", maTohop));
    }
}
